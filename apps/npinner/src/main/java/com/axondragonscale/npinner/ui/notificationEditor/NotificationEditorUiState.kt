package com.axondragonscale.npinner.ui.notificationEditor

import com.axondragonscale.npinner.model.NPinnerNotification

/**
 * Created by Ronak Harkhani on 30/04/23
 */
sealed interface NotificationEditorUiState {

    object Loading : NotificationEditorUiState

    data class Success(
        val createNew: Boolean,
        val notification: NPinnerNotification,
    ) : NotificationEditorUiState {

        val topBarTitle: String
            get() = if (createNew) "Create" else "Edit"

        val bottomButtonText: String
            get() = if (createNew) "SAVE AND PIN" else "SAVE"

        val bottomButtonEnabled: Boolean
            get() = notification.title.isNotBlank()

        val showDeleteButton = !createNew

    }

}
