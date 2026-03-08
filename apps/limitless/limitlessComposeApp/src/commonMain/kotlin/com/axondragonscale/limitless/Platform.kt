package com.axondragonscale.limitless

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform