package com.axondragonscale.storageviewer.model

/** Created by Ronak on 14/11/26 */

/** [lastModified] is the entry's epoch millis, 0 when the platform does not report one. */
public data class FileNode(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModified: Long = 0,
)

/** One directory as the surface shows it: every folder and file it holds, nothing filtered out. */
public data class FolderListing(
    val path: String,
    val folders: List<FileNode>,
    val files: List<FileNode>,
)

/** [TRUNCATED] means the preview stopped at its cap; [BINARY] means it never rendered as text. */
public enum class PreviewKind { TEXT, BINARY, TRUNCATED }

/**
 * [shownBytes] is what [text] or [hexLines] covers; [totalBytes] is the file's size and
 * [lastModified] its epoch millis, 0 when unknown. A text preview fills both [text] and
 * [hexLines] from the same bytes, so switching representation never reads the file again.
 */
public data class FilePreview(
    val kind: PreviewKind,
    val text: String?,
    val hexLines: List<String>,
    val shownBytes: Int,
    val totalBytes: Long,
    val lastModified: Long = 0,
)
