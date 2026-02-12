package com.axondragonscale.npinner.model

/**
 * Created by Ronak Harkhani on 14/05/23
 */
enum class DarkModeConfig {
    ON, AUTO, OFF;
    
    companion object {
        private val values = values()
        
        val stringValues = values.map { it.name }
        
        fun fromOrdinal(ordinal: Int) = values[ordinal]
    }
}
