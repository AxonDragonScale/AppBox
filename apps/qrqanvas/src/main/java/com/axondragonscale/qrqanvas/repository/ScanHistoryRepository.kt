package com.axondragonscale.qrqanvas.repository

import com.axondragonscale.qrqanvas.database.dao.ScanHistoryDao

/**
 * Created by Ronak Harkhani on 09/07/25
 */
class ScanHistoryRepository(private val scanHistoryDao: ScanHistoryDao): ScanHistoryDao by scanHistoryDao
