package com.axondragonscale.jest

import android.content.Intent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import android.content.Context

private object ShareUtilsProvider : KoinComponent {
    val context: Context by inject()
}

actual fun shareText(text: String) {
    val intent = Intent.createChooser(
        Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        },
        null
    )
    ShareUtilsProvider.context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
