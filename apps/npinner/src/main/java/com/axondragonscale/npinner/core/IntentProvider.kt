package com.axondragonscale.npinner.core

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.axondragonscale.npinner.NPinnerActivity
import com.axondragonscale.npinner.core.receiver.DeleteNotificationReceiver
import com.axondragonscale.npinner.core.receiver.UnpinNotificationReceiver
import com.axondragonscale.npinner.ui.Destination.NotificationEditor

/**
 * Created by Ronak Harkhani on 01/05/23
 */
object IntentProvider {

    fun getNotificationEditorPendingIntent(context: Context, id: String): PendingIntent {
        val deeplink = NotificationEditor.deeplink + "?${NotificationEditor.argId}=$id"
        val editorIntent =
            Intent(Intent.ACTION_EDIT, deeplink.toUri(), context, NPinnerActivity::class.java)
        return PendingIntent.getActivity(context, id.hashCode(), editorIntent, intentFlags)
    }

    fun getUnpinPendingIntent(context: Context, id: String) =
        PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            UnpinNotificationReceiver.getIntent(context, id),
            intentFlags
        )


    fun getDeletePendingIntent(context: Context, id: String) =
        PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            DeleteNotificationReceiver.getIntent(context, id),
            intentFlags
        )

    private const val intentFlags =
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

}
