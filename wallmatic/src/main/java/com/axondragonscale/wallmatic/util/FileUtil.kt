package com.axondragonscale.wallmatic.util

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

/**
 * Created by Ronak Harkhani on 06/07/24
 */

object FileUtil {

    private val supportedExtensions = listOf("jpg", "jpeg", "png", "heif", "webp")

    fun isValidFile(context: Context, uri: Uri): Boolean {
        val file = DocumentFile.fromSingleUri(context, uri) ?: return false
        return file.exists()
    }

    fun isValidFolder(context: Context, uri: Uri): Boolean {
        val folder = DocumentFile.fromTreeUri(context, uri) ?: return false
        return folder.exists() && folder.isDirectory
    }

    fun getAllWallpapersInFolder(context: Context, uri: Uri): List<Uri> {
        val folder = DocumentFile.fromTreeUri(context, uri) ?: return emptyList()
        return folder.getAllWallpapers()
    }

    fun getFolderName(context: Context, uri: Uri): String {
        val folder = DocumentFile.fromTreeUri(context, uri)
        return folder?.name ?: ""
    }

    private fun DocumentFile.getAllWallpapers(): List<Uri> {
        val wallpapers = mutableListOf<Uri>()
        this.listFiles().forEach { file ->
            if (file.isDirectory) {
                wallpapers.addAll(file.getAllWallpapers())
            } else if (file.getExtension().lowercase() in supportedExtensions) {
                wallpapers.add(file.uri)
            }
        }
        return wallpapers
    }

    private fun DocumentFile.getExtension(): String =
        this.name?.substringAfterLast('.') ?: ""
}
