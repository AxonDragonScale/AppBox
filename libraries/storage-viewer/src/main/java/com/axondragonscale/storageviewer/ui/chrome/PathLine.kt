package com.axondragonscale.storageviewer.ui.chrome

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.axondragonscale.storageviewer.model.FileRoot
import com.axondragonscale.storageviewer.ui.style.LocalStorageViewerStyle

/** Created by Ronak on 14/11/26 */

private const val ShortPathChars = 32

/**
 * A root's path as the surface names it: the root alias, then the path inside it, so the long
 * device directory never crowds out where the reader actually is. Past [ShortPathChars] the
 * middle collapses to `alias/…/last`, so the root and the current segment both survive.
 */
internal fun rootPath(root: FileRoot, path: String): String {
    val alias = root.kind.name.lowercase()
    val base = root.path.trimEnd('/')
    val remainder = when {
        path == base || path == "$base/" -> ""
        path.startsWith("$base/") -> path.removePrefix("$base/").trimEnd('/')
        else -> return alias
    }
    val joined = if (remainder.isEmpty()) "$alias/" else "$alias/$remainder"
    if (joined.length <= ShortPathChars) return joined
    return "$alias/…/${joined.trimEnd('/').substringAfterLast('/')}"
}

/**
 * The path above a folder listing; [rootPath] already resolved it, so the end ellipsis only guards.
 */
@Composable
internal fun StoragePathLine(
    path: String,
    modifier: Modifier = Modifier,
) {
    val style = LocalStorageViewerStyle.current
    Text(
        text = path,
        modifier = modifier.fillMaxWidth(),
        style = style.monoKey,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        softWrap = false,
    )
}
