package com.axondragonscale.cardy.model

/** In-memory handoff for a fresh read that has not been saved to the vault. */
class CardReadSession {
    var latest: CardReadResult? = null
        private set

    fun update(result: CardReadResult) {
        latest = result
    }
}
