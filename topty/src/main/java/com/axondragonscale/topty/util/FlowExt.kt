package com.axondragonscale.topty.util

import kotlinx.coroutines.flow.Flow

/**
 * Created by Ronak Harkhani on 13/10/24
 */

suspend fun <T> Flow<T>.collect() = this.collect {}
