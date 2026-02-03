package com.axondragonscale.topty.util

import com.axondragonscale.topty.core.HOTPGenerator
import com.axondragonscale.topty.core.TOTPGenerator
import com.axondragonscale.topty.database.entity.Token
import com.axondragonscale.topty.model.Protocol

/**
 * Created by Ronak Harkhani on 28/10/24
 */

fun Token.generateCode() = when (this.protocol) {
    Protocol.HOTP -> HOTPGenerator.generateCode(this)
    Protocol.TOTP -> TOTPGenerator.generateCode(this)
}
