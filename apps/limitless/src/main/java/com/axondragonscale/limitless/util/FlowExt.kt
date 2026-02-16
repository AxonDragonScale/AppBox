package com.axondragonscale.limitless.util

import kotlinx.coroutines.flow.Flow

/**
 * Created by Ronak Harkhani on 04/11/24
 */

suspend fun <T> Flow<T>.collect() = this.collect {}
