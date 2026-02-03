package com.axondragonscale.topty.model

/**
 * Created by Ronak Harkhani on 27/10/24
 */
enum class Protocol {
    TOTP,
    HOTP;

    val code: String
        get() = this.name.lowercase()
}
