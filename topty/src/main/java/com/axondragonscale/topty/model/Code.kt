package com.axondragonscale.topty.model

import com.axondragonscale.topty.database.entity.Token

/**
 * Created by Ronak Harkhani on 28/10/24
 */
sealed class Code {
    abstract val code: String
    abstract val token: Token
}

data class HOTPCode(
    override val code: String,
    override val token: Token,
) : Code()

data class TOTPCode(
    override val code: String,
    override val token: Token,
    val periodStart: Long,
    val periodEnd: Long,
) : Code()
