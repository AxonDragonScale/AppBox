package com.axondragonscale.devinator.tile

import android.content.ComponentName
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.axondragonscale.devinator.R
import com.axondragonscale.devinator.model.DurationOption

/** Created by Ronak on 08/11/26 */

/** The five tiles: one place for their component, page title, icon and description. */
enum class DevTile(
    val className: String,
    @field:StringRes val titleRes: Int,
    @field:DrawableRes val iconRes: Int,
) {
    DEVELOPER_OPTIONS(
        DeveloperOptionsTileService::class.java.name,
        R.string.tile_developer_options,
        R.drawable.ic_tile_developer_mode,
    ),
    PAUSE(
        PauseDevOptionsTileService::class.java.name,
        R.string.tile_pause_dev_options,
        R.drawable.ic_tile_timer_pause,
    ),
    RESUME(
        ResumeDevOptionsTileService::class.java.name,
        R.string.tile_resume_dev_options,
        R.drawable.ic_tile_timer_play,
    ),
    USB(
        UsbDebuggingTileService::class.java.name,
        R.string.tile_usb_debugging,
        R.drawable.ic_tile_usb,
    ),
    WIRELESS(
        WirelessDebuggingTileService::class.java.name,
        R.string.tile_wireless_debugging,
        R.drawable.ic_tile_wireless,
    ),
    ;

    fun component(context: Context): ComponentName = ComponentName(context.packageName, className)

    fun title(context: Context): String = context.getString(titleRes)

    fun description(duration: DurationOption): String = when (this) {
        DEVELOPER_OPTIONS -> "Master switch for ADB"
        PAUSE -> "Off for ${duration.label}, then back on."
        RESUME -> "On for ${duration.label}, then back off."
        USB -> "ADB over USB"
        WIRELESS -> "ADB over Wi-Fi or hotspot"
    }
}
