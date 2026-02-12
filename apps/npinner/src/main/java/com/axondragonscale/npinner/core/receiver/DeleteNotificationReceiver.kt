package com.axondragonscale.npinner.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.axondragonscale.npinner.core.NPinnerNotificationManager
import com.axondragonscale.npinner.repository.NotificationRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Ronak Harkhani on 01/05/23
 */

class DeleteNotificationReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        private const val DELETE_ACTION = "npinner.delete_notification"
        private const val EXTRA_NOTIFICATION_ID = "notificationId"

        fun getIntent(context: Context, notificationId: String) =
            Intent(context, DeleteNotificationReceiver::class.java).apply {
                action = DELETE_ACTION
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            }
    }

    private val repository: NotificationRepository by inject()
    private val notificationManager: NPinnerNotificationManager by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationId = intent?.getStringExtra(EXTRA_NOTIFICATION_ID)
        if (notificationId == null || intent.action != DELETE_ACTION) return

        notificationManager.dismissNotification(notificationId)

        // TODO: Write NPinner Dispatcher
        GlobalScope.launch {
            repository.delete(repository.getNotification(notificationId))
        }
    }

}
