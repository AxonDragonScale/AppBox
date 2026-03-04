package com.axondragonscale.qrqanvas.core

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File

/**
 * Created by Ronak Harkhani on 09/07/25
 *
 * Handles exporting QR code bitmaps to the file system and sharing via the Android share sheet.
 *
 * Supports:
 * - Saving to cache for sharing via [FileProvider]
 * - Saving to Downloads via [MediaStore]
 * - Launching the Android share sheet with a QR code image
 */
class QRExporter(private val context: Context) {

    /**
     * Saves the [bitmap] as a PNG to the app's cache directory and returns a
     * content [Uri] via [FileProvider] that can be shared with other apps.
     *
     * @param bitmap The QR code bitmap to share.
     * @param fileName The file name (without extension) to use for the cached file.
     * @return A shareable [Uri] for the cached PNG file.
     */
    fun saveToCacheAndGetUri(bitmap: Bitmap, fileName: String = "qr_code"): Uri {
        val shareDir = File(context.cacheDir, "shared_qr").apply { mkdirs() }
        val file = File(shareDir, "${sanitizeFileName(fileName)}.png")

        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /**
     * Saves the [bitmap] as a PNG to the device's Downloads folder via [MediaStore].
     *
     * @param bitmap The QR code bitmap to export.
     * @param fileName The file name (without extension) for the exported file.
     * @return `true` if the export succeeded, `false` otherwise.
     */
    fun saveToDownloads(bitmap: Bitmap, fileName: String = "qr_code"): Boolean {
        val sanitized = sanitizeFileName(fileName)
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$sanitized.png")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/QRQanvas")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues) ?: return false

        return try {
            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
            true
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            false
        }
    }

    /**
     * Launches the Android share sheet with the QR code [bitmap].
     *
     * @param bitmap The QR code bitmap to share.
     * @param title The title used for the cached file name and share sheet title.
     */
    fun share(bitmap: Bitmap, title: String = "qr_code") {
        val uri = saveToCacheAndGetUri(bitmap, title)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooser = Intent.createChooser(shareIntent, "Share QR Code").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Sanitizes a file name by replacing characters that are not allowed in file names.
     */
    private fun sanitizeFileName(name: String): String =
        name.replace(Regex("[^a-zA-Z0-9._\\-]"), "_").take(100)
}
