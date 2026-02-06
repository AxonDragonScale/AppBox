package com.axondragonscale.topty.ui.home

import android.content.Context
import android.net.Uri
import com.axondragonscale.topty.model.Code

/**
 * Created by Ronak Harkhani on 22/09/24
 */
sealed interface HomeUiEvent {
    data object NavigateToQRScanner: HomeUiEvent
    data object NavigateToAddToken: HomeUiEvent
    data object NavigateToSettings: HomeUiEvent
    data class ScanImage(val context: Context, val uri: Uri): HomeUiEvent
    data class NavigateToEditToken(val tokenId: Long): HomeUiEvent
    data class DeleteToken(val tokenId: Long): HomeUiEvent
    data class RefreshCode(val code: Code): HomeUiEvent
    data class SpeakOut(val code: Code): HomeUiEvent
    data class CodeCopied(val tokenId: Long): HomeUiEvent
    data class UpdateSearchQuery(val query: String): HomeUiEvent
    data class SelectGroup(val group: String?): HomeUiEvent
    data object ClearFilters: HomeUiEvent
}
