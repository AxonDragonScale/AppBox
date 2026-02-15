package com.axondragonscale.qrqanvas.repository

import com.axondragonscale.qrqanvas.database.dao.QRCodeDao

/**
 * Created by Ronak Harkhani on 09/07/25
 */
class QRCodeRepository(private val qrCodeDao: QRCodeDao): QRCodeDao by qrCodeDao
