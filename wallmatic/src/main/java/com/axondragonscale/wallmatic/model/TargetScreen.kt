package com.axondragonscale.wallmatic.model

/**
 * Created by Ronak Harkhani on 21/09/24
 */
enum class TargetScreen {
    Home,
    Lock,
    Both;

    fun isHome() = this == Home || this == Both
    fun isLock() = this == Lock || this == Both
}
