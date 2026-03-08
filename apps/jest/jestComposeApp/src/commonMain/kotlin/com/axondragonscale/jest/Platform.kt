package com.axondragonscale.jest

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform