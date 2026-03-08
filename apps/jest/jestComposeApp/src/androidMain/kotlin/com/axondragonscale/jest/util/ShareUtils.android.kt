package com.axondragonscale.jest.util

import android.content.Intent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import android.content.Context
import org.koin.mp.KoinPlatform

actual fun shareText(text: String) {
    val intent = Intent.createChooser(
        Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
        null
    )
    val context = KoinPlatform.getKoin().get<Context>()
    context.startActivity(intent)
}
