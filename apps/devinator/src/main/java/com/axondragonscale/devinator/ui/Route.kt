package com.axondragonscale.devinator.ui

import kotlinx.serialization.Serializable

/** Created by Ronak on 11/06/26 */
object Route {
    @Serializable data object Setup
    @Serializable data object Home
    @Serializable data object Settings
    @Serializable data object QuickSettingsTiles
}
