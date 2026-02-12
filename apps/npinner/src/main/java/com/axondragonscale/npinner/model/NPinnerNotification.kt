package com.axondragonscale.npinner.model

import java.util.UUID

/**
 * Created by Ronak Harkhani on 30/04/23
 */
data class NPinnerNotification(
    val id: String,
    val title: String,
    val description: String,
    val isPinned: Boolean = true,
    val schedule: Schedule? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
) {
    companion object {
        fun newInstance() = NPinnerNotification(
            id = UUID.randomUUID().toString(),
            title = "",
            description = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
    }
}
