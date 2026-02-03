package com.axondragonscale.topty.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.axondragonscale.topty.model.HashAlgo
import com.axondragonscale.topty.model.Protocol

/**
 * Created by Ronak Harkhani on 14/10/24
 */
@Entity(tableName = "token")
data class Token(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val name: String,
    val issuer: String,
    val group: String,
    val note: String,
    val secret: String,
    val protocol: Protocol,
    val hashAlgo: HashAlgo,
    val period: Int, // TODO: Convert to duration class ?
    val counter: Long,
    val digits: Int,
) {
    
    companion object {
        fun emptyToken() = Token(
            name = "",
            issuer = "",
            group = "",
            note = "",
            secret = "",
            protocol = Protocol.TOTP,
            hashAlgo = HashAlgo.SHA1,
            period = 30,
            counter = 0L,
            digits = 6
        )    
    }
    
}
