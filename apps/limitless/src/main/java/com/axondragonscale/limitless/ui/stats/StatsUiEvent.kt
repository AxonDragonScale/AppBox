package com.axondragonscale.limitless.ui.stats

/**
 * Created by Ronak Harkhani on 22/07/25
 */
sealed interface StatsUiEvent {
    data object NavigateBack : StatsUiEvent
}
