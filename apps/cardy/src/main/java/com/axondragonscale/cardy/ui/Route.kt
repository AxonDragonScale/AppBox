package com.axondragonscale.cardy.ui

import kotlinx.serialization.Serializable

/** Created by Ronak on 05/30/26 */
object Route {
    @Serializable data object Home
    @Serializable data class CardDetail(val cardId: Long = -1L)
    @Serializable data object Vault
    @Serializable data object Settings
}
