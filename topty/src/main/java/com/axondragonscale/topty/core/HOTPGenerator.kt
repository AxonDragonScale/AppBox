package com.axondragonscale.topty.core

import com.axondragonscale.topty.database.entity.Token
import com.axondragonscale.topty.model.HOTPCode

/**
 * Created by Ronak Harkhani on 28/10/24
 */

object HOTPGenerator {

    fun generateCode(token: Token): HOTPCode {
        val code = HMACGenerator.generateCode(
            secret = token.secret,
            counter = token.counter,
            hashAlgo = token.hashAlgo,
            digits = token.digits
        )

        return HOTPCode(
            code = code,
            token = token,
        )
    }

}
