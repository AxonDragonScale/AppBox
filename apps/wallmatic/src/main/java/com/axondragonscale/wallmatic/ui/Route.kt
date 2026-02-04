package com.axondragonscale.wallmatic.ui

import com.axondragonscale.wallmatic.ui.bottombar.Tab
import kotlinx.serialization.Serializable

/**
 * Created by Ronak Harkhani on 23/06/24
 */
object Route {

    @Serializable
    data class Dashboard(val tab: Int = Tab.Home.position)

    @Serializable
    data class Album(val albumId: Int)

    @Serializable
    data class Folder(val folderId: Int)

    @Serializable
    data class Wallpaper(val wallpaperId: Int)

}
