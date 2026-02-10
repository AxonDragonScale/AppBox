package com.axondragonscale.topty.core

import com.axondragonscale.commons.logD
import com.axondragonscale.topty.database.entity.Token
import com.axondragonscale.topty.model.TOTPCode

/**
 * Created by Ronak Harkhani on 28/10/24
 */
object TOTPGenerator {

    private const val TAG = "TOTPGenerator"

    fun generateCode(token: Token): TOTPCode {
        TAG.logD { "generateCode() for ${token.id}" }

        val counter = Math.floorDiv(System.currentTimeMillis(), token.period * 1000)
        val periodStart = counter * token.period * 1000
        val periodEnd = periodStart + token.period * 1000

        val code = HMACGenerator.generateCode(
            secret = token.secret,
            counter = counter,
            hashAlgo = token.hashAlgo,
            digits = token.digits
        )

        return TOTPCode(
            code = code,
            token = token,
            periodStart = periodStart,
            periodEnd = periodEnd,
        )
    }

}
