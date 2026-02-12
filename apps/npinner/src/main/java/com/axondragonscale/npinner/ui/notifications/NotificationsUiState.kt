package com.axondragonscale.npinner.ui.notifications

import com.axondragonscale.npinner.model.NPinnerNotification

/**
 * Created by Ronak Harkhani on 30/04/23
 */
sealed interface NotificationsUiState {

    object Loading : NotificationsUiState

    data class Success(val notifications: List<NPinnerNotification>) : NotificationsUiState

}
