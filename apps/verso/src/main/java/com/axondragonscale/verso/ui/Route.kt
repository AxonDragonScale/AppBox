package com.axondragonscale.verso.ui

import kotlinx.serialization.Serializable

object Route {

    @Serializable
    data object Library

    @Serializable
    data object Stats

    @Serializable
    data object Settings
}
