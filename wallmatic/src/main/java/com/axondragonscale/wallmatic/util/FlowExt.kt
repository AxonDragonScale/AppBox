package com.axondragonscale.wallmatic.util

import kotlinx.coroutines.flow.Flow

/**
 * Created by Ronak Harkhani on 21/09/24
 */

suspend fun <T> Flow<T>.collect() = this.collect {}
