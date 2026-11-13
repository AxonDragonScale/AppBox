package com.axondragonscale.wallmatic.ui

import com.axondragonscale.wallmatic.ui.bottombar.Tab
import kotlinx.serialization.Serializable

/**
 * Created by Ronak Harkhani on 23/06/24
 */
object Route {

    object Args {
        const val ALBUM_ID = "albumId"
        const val FOLDER_ID = "folderId"
        const val WALLPAPER_ID = "wallpaperId"
        const val EDIT = "edit"
    }

    @Serializable
    data class Dashboard(val tab: String = Tab.Home.name)

    @Serializable
    data class Album(val albumId: Int)

    @Serializable
    data class Folder(val folderId: Int)

    @Serializable
    data class Wallpaper(val wallpaperId: Int, val edit: Boolean = false)

    @Serializable
    data object DevTools

}
