package com.axondragonscale.npinner.ui.notificationEditor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.npinner.core.NPinnerNotificationManager
import com.axondragonscale.npinner.core.NPinnerNotificationScheduler
import com.axondragonscale.npinner.model.NPinnerNotification
import com.axondragonscale.npinner.model.Schedule
import com.axondragonscale.npinner.repository.NotificationRepository
import com.axondragonscale.npinner.ui.Destination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Created by Ronak Harkhani on 30/04/23
 */

class NotificationEditorViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: NotificationRepository,
    private val notificationManager: NPinnerNotificationManager,
    private val notificationScheduler: NPinnerNotificationScheduler,
) : ViewModel() {

    private val id = savedStateHandle.get<String>(Destination.NotificationEditor.argId)
    private val createNew = id == null
    private val notificationFlow = MutableStateFlow(getNotification(id))

    val uiState: StateFlow<NotificationEditorUiState> =
        notificationEditorUiState().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotificationEditorUiState.Loading
        )

    fun onContentChange(title: String, description: String) = viewModelScope.launch {
        notificationFlow.emit(notificationFlow.value.copy(title = title, description = description))
    }

    fun onScheduleChange(schedule: Schedule?) = viewModelScope.launch {
        notificationFlow.emit(notificationFlow.value.copy(schedule = schedule))
    }

    fun onSaveClick() = viewModelScope.launch {
        var notification = notificationFlow.value
        if (createNew) {
            notification = notification.copy(
                isPinned = notification.schedule == null,
                createdAt = System.currentTimeMillis(),
            )
        }
        launch { repository.upsert(notification) }

        // Post Notification or Update existing
        if (notification.isPinned)
            notificationManager.postNotification(notification)

        if (notification.schedule != null) {
            notificationScheduler.scheduleNotification(notification)
        } else {
            notificationScheduler.cancelScheduledNotification(notification.id)
        }
    }

    fun onDeleteClick() = viewModelScope.launch {
        val notification = notificationFlow.value.copy(isPinned = false)
        launch { repository.delete(notification) }

        notificationManager.dismissNotification(notification.id)
        notificationScheduler.cancelScheduledNotification(notification.id)
    }

    private fun notificationEditorUiState(): Flow<NotificationEditorUiState> =
        notificationFlow
            .map { NotificationEditorUiState.Success(createNew, it) }

    private fun getNotification(id: String?): NPinnerNotification {
        return if (id == null) NPinnerNotification.newInstance()
        else runBlocking { repository.getNotification(id) }
    }
}
