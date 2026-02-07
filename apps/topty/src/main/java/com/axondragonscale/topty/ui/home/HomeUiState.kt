package com.axondragonscale.topty.ui.home

import com.axondragonscale.topty.model.Code
import com.axondragonscale.topty.model.CodeDesign
import com.axondragonscale.topty.model.SortOrder

/**
 * Created by Ronak Harkhani on 22/09/24
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val codeDesign: CodeDesign = CodeDesign.DEFAULT,
    val codes: List<Code> = emptyList(),
    val filteredCodes: List<Code> = emptyList(),
    val speakOutOnTap: Boolean = false,
    val searchQuery: String = "",
    val selectedGroup: String? = null,
    val availableGroups: List<String> = emptyList(),
    val sortOrder: SortOrder = SortOrder.DEFAULT,
)
