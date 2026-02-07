package com.axondragonscale.topty.ui

import kotlinx.serialization.Serializable

/**
 * Created by Ronak Harkhani on 15/09/24
 */
object Route {

    @Serializable
    data object Onboarding

    @Serializable
    data object Home

    @Serializable
    data object QRScanner

    @Serializable
    data object Settings

    @Serializable
    sealed interface Token {
        @Serializable data class Add(val uri: String?): Token
        @Serializable data class Edit(val id: Long): Token
    }

    @Serializable
    data object DevTools

    @Serializable
    data object BackupRestore

}
