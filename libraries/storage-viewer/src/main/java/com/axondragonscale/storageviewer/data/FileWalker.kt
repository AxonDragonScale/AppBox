package com.axondragonscale.storageviewer.data

import com.axondragonscale.storageviewer.model.FileNode
import com.axondragonscale.storageviewer.model.FilePreview
import com.axondragonscale.storageviewer.model.FileRoot
import com.axondragonscale.storageviewer.model.FolderListing
import com.axondragonscale.storageviewer.model.PreviewKind
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import kotlin.math.min

/** Created by Ronak on 14/11/26 */

private const val BINARY_SCAN_BYTES = 4 * 1024
private const val HEX_BYTES_PER_LINE = 8
private const val HEX_MAX_LINES = 32
private const val HEX_PAIR_WIDTH = HEX_BYTES_PER_LINE * 3 - 1
private const val HEX_DIGITS = "0123456789ABCDEF"
private const val PRINTABLE_MIN = 0x20
private const val PRINTABLE_MAX = 0x7E
private const val UTF8_MAX_SEQUENCE = 4

/**
 * Blocking reads over the app's own file roots. A call touches one path only — one [File.listFiles]
 * or one capped stream — so a deep, volatile cache tree costs no recursive walk.
 */
internal object FileWalker {

    private val byName: Comparator<File> =
        compareBy(String.CASE_INSENSITIVE_ORDER) { entry: File -> entry.name }
            .thenBy { entry -> entry.name }

    fun children(root: FileRoot, path: String): FolderListing {
        val rootDir = rootDirectory(root)
        val directory = rootDir?.let { dir -> within(dir, path) } ?: return FolderListing(
            path = path.ifBlank { root.path },
            folders = emptyList(),
            files = emptyList(),
        )
        if (!directory.isDirectory) throw FileNotFoundException("Not a directory: ${directory.path}")
        val entries = directory.listFiles() ?: throw IOException("Cannot list ${directory.path}")

        return FolderListing(
            path = directory.path,
            folders = entries.filter { entry -> entry.isDirectory }
                .sortedWith(byName)
                .map { entry -> entry.toNode(isDirectory = true) },
            files = entries.filterNot { entry -> entry.isDirectory }
                .sortedWith(byName)
                .map { entry -> entry.toNode(isDirectory = false) },
        )
    }

    fun preview(root: FileRoot, path: String, maxBytes: Int): FilePreview {
        val rootDir = rootDirectory(root) ?: throw FileNotFoundException("No directory for ${root.kind}")
        val file = within(rootDir, path) ?: throw SecurityException("Path outside ${rootDir.path}")
        val totalBytes = file.length()
        val read = readUpTo(file, maxBytes)
        val hex = hexLines(read)
        val contentEnd = if (totalBytes > read.size) utf8ContentEnd(read) else read.size
        val text = if (hasNul(read)) null else decodeUtf8(read, contentEnd)
        if (text == null) {
            return FilePreview(
                kind = PreviewKind.BINARY,
                text = null,
                hexLines = hex,
                shownBytes = read.size,
                totalBytes = totalBytes,
                lastModified = file.lastModified(),
            )
        }
        return FilePreview(
            kind = if (totalBytes > maxBytes) PreviewKind.TRUNCATED else PreviewKind.TEXT,
            text = text,
            hexLines = hex,
            shownBytes = contentEnd,
            totalBytes = totalBytes,
            lastModified = file.lastModified(),
        )
    }

    private fun rootDirectory(root: FileRoot): File? =
        root.path.takeIf { it.isNotBlank() }?.let { path -> File(path).canonicalFile }

    // Canonical paths resolve symlinks, so a link pointing out of the root cannot pass: the
    // resolved target must sit under the root, not merely be reached from it.
    private fun within(rootDir: File, path: String): File? {
        val target = File(path.ifBlank { rootDir.path }).canonicalFile
        val prefix = rootDir.path + File.separator
        return target.takeIf { entry -> entry.path == rootDir.path || entry.path.startsWith(prefix) }
    }

    private fun File.toNode(isDirectory: Boolean): FileNode = FileNode(
        name = name,
        path = path,
        isDirectory = isDirectory,
        sizeBytes = if (isDirectory) 0L else length(),
        lastModified = lastModified(),
    )

    private fun readUpTo(file: File, maxBytes: Int): ByteArray {
        val buffer = ByteArray(maxBytes)
        var offset = 0
        FileInputStream(file).use { stream ->
            while (offset < maxBytes) {
                val count = stream.read(buffer, offset, maxBytes - offset)
                if (count <= 0) break
                offset += count
            }
        }
        return if (offset == maxBytes) buffer else buffer.copyOf(offset)
    }

    private fun hasNul(bytes: ByteArray): Boolean {
        val end = min(bytes.size, BINARY_SCAN_BYTES)
        for (index in 0 until end) {
            if (bytes[index].toInt() == 0) return true
        }
        return false
    }

    // -1 means the bytes are not UTF-8; a null char is caught before this runs.
    private fun decodeUtf8(bytes: ByteArray, length: Int): String? =
        try {
            Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes, 0, length))
                .toString()
        } catch (_: CharacterCodingException) {
            null
        }

    // The cap cut the read mid-character; drop that partial sequence so a text file is not binary.
    private fun utf8ContentEnd(bytes: ByteArray): Int {
        var lead = bytes.size - 1
        var back = 1
        while (lead >= 0 && back <= UTF8_MAX_SEQUENCE) {
            val byte = bytes[lead].toInt() and 0xFF
            if (byte and 0xC0 != 0x80) {
                val needed = when {
                    byte and 0x80 == 0x00 -> 1
                    byte and 0xE0 == 0xC0 -> 2
                    byte and 0xF0 == 0xE0 -> 3
                    byte and 0xF8 == 0xF0 -> 4
                    else -> return bytes.size
                }
                return if (back < needed) lead else bytes.size
            }
            lead--
            back++
        }
        return bytes.size
    }

    private fun hexLines(bytes: ByteArray): List<String> {
        val shown = min(bytes.size, HEX_BYTES_PER_LINE * HEX_MAX_LINES)
        return buildList {
            var offset = 0
            while (offset < shown) {
                val end = min(offset + HEX_BYTES_PER_LINE, shown)
                add(hexLine(bytes, offset, end))
                offset = end
            }
        }
    }

    private fun hexLine(bytes: ByteArray, offset: Int, end: Int): String = buildString {
        append(offsetLabel(offset))
        append("  ")
        val hexStart = length
        for (index in offset until end) {
            if (index > offset) append(' ')
            val value = bytes[index].toInt() and 0xFF
            append(HEX_DIGITS[value shr 4])
            append(HEX_DIGITS[value and 0x0F])
        }
        repeat(HEX_PAIR_WIDTH - (length - hexStart)) { append(' ') }
        append("  |")
        for (index in offset until end) {
            val value = bytes[index].toInt() and 0xFF
            append(if (value in PRINTABLE_MIN..PRINTABLE_MAX) value.toChar() else '.')
        }
        append('|')
    }

    private fun offsetLabel(offset: Int): String {
        val digits = CharArray(8)
        var value = offset
        for (index in digits.lastIndex downTo 0) {
            digits[index] = HEX_DIGITS[value and 0x0F]
            value = value ushr 4
        }
        return String(digits)
    }
}
