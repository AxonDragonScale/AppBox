package com.axondragonscale.limitless.ui.editor

/**
 * Created by Ronak Harkhani on 09/11/24
 */
sealed interface EditorUiEffect {
    data object NavigateBack : EditorUiEffect
}
