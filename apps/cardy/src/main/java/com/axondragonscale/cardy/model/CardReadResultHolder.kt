package com.axondragonscale.cardy.model

/**
 * Koin singleton that holds the last NFC card read result.
 * Used to pass data from HomeVM (where the read happens) to DetailVM (where it's displayed).
 * Replaces the previous companion object static variable approach for better testability.
 */
class CardReadResultHolder {
    var result: CardReadResult? = null
}
