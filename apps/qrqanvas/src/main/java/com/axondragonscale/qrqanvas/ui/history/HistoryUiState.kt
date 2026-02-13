package com.axondragonscale.qrqanvas.ui.history

import com.axondragonscale.qrqanvas.database.entity.ScanHistoryEntity

/**
 * Created by Ronak Harkhani on 09/07/25
 */
data class HistoryUiState(
    val scanHistory: List<ScanHistoryEntity> = emptyList(),
    val expandedItemId: Long? = null,
    val showClearAllDialog: Boolean = false,
)
