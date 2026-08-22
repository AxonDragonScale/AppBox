/** Created by Ronak on 05/30/26 */
package com.axondragonscale.cardy.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.axondragonscale.cardy.emv.CardNetwork
import com.axondragonscale.cardy.emv.EmvDataExtractor
import com.axondragonscale.cardy.model.CardReadResult
import com.axondragonscale.cardy.model.EmvApplication
import com.axondragonscale.cardy.model.ParsedTlvNode
import com.axondragonscale.cardy.repository.CardRepository
import com.axondragonscale.cardy.ui.Route
import com.axondragonscale.commons.logD
import com.axondragonscale.commons.logE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "DetailVM"

class DetailVM(
    savedStateHandle: SavedStateHandle,
    private val cardRepository: CardRepository,
) : ViewModel() {

    // region State

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState = _uiState.asStateFlow()

    // endregion

    private val initialCardId: Long = savedStateHandle.toRoute<Route.CardDetail>().cardId
    val isFreshRead: Boolean = initialCardId == -1L

    init {
        TAG.logD { "DetailVM created, cardId=$initialCardId, isFreshRead=$isFreshRead" }
        if (isFreshRead) {
            loadFromLastRead()
        } else {
            loadFromVault()
        }
    }

    // region Event Handling

    fun onEvent(event: DetailUiEvent) {
        when (event) {
            is DetailUiEvent.UpdateLabel -> updateLabel(event.newLabel)
            is DetailUiEvent.DeleteCard -> deleteCard()
            is DetailUiEvent.SelectApplication -> selectApplication(event.index)
            is DetailUiEvent.SaveToVault -> saveToVault()
        }
    }

    // endregion

    private fun loadFromLastRead() {
        val result = cardRepository.pendingRead ?: run {
            TAG.logD { "loadFromLastRead: lastReadResult is null" }
            return
        }

        val tlvTrees = result.applications.map {
            EmvDataExtractor.buildTlvTree(it.rawTlvs, it.network)
        }
        val network = result.primaryNetwork

        TAG.logD { "loadFromLastRead: ${result.applications.size} application(s), network=$network" }

        _uiState.update {
            it.copy(
                isLoaded = true,
                mode = DetailMode.FreshRead,
                cardId = -1L,
                label = result.displayLabel,
                network = network,
                cardData = result,
                tlvTrees = tlvTrees,
                selectedAppIndex = 0,
            )
        }
    }

    private fun loadFromVault() = viewModelScope.launch {
        val entity = cardRepository.getCardById(initialCardId) ?: return@launch
        val decryptedData = cardRepository.decryptCardData(entity)

        val network = CardNetwork.fromName(entity.network)

        val tlvTrees = decryptedData?.applications?.map { app ->
            EmvDataExtractor.buildTlvTree(app.rawTlvs, CardNetwork.fromAid(app.aid))
        }.orEmpty()

        TAG.logD { "loadFromVault: cardId=$initialCardId, network=$network" }

        _uiState.update {
            it.copy(
                isLoaded = true,
                mode = DetailMode.SavedCard,
                cardId = entity.id,
                label = entity.label,
                network = network,
                cardData = decryptedData,
                tlvTrees = tlvTrees,
                selectedAppIndex = 0,
            )
        }
    }

    private fun saveToVault() = viewModelScope.launch {
        val result = _uiState.value.cardData ?: return@launch
        TAG.logD { "saveToVault: saving card" }
        _uiState.update { it.copy(isSaving = true) }
        try {
            val savedId = cardRepository.saveCard(result)
            TAG.logD { "saveToVault: saved with id=$savedId" }
            _uiState.update { it.copy(isSaving = false, mode = DetailMode.SavedCard, cardId = savedId) }
        } catch (e: Exception) {
            TAG.logE { "saveToVault: failed — ${e.message}" }
            _uiState.update { it.copy(isSaving = false, saveError = e.message) }
        }
    }

    private fun updateLabel(newLabel: String) = viewModelScope.launch {
        val cardId = _uiState.value.cardId
        if (cardId > 0) {
            cardRepository.updateLabel(cardId, newLabel)
            _uiState.update { it.copy(label = newLabel) }
        }
    }

    private fun deleteCard() = viewModelScope.launch {
        val cardId = _uiState.value.cardId
        if (cardId > 0) {
            cardRepository.deleteCard(cardId)
            _uiState.update { it.copy(isDeleted = true) }
        }
    }

    private fun selectApplication(index: Int) {
        _uiState.update { it.copy(selectedAppIndex = index) }
    }
}

enum class DetailMode {
    FreshRead,
    SavedCard,
}

data class DetailUiState(
    val isLoaded: Boolean = false,
    val mode: DetailMode = DetailMode.FreshRead,
    val cardId: Long = 0,
    val label: String = "",
    val network: CardNetwork = CardNetwork.UNKNOWN,
    val cardData: CardReadResult? = null,
    val tlvTrees: List<List<ParsedTlvNode>> = emptyList(),
    val selectedAppIndex: Int = 0,
    val isDeleted: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
) {
    val selectedApp: EmvApplication?
        get() = cardData?.applications?.getOrNull(selectedAppIndex)

    val selectedTlvTree: List<ParsedTlvNode>
        get() = tlvTrees.getOrNull(selectedAppIndex).orEmpty()

    val hasMultipleApps: Boolean
        get() = (cardData?.applications?.size ?: 0) > 1

    val isFreshRead: Boolean get() = mode == DetailMode.FreshRead
    val isSavedCard: Boolean get() = mode == DetailMode.SavedCard
}

sealed interface DetailUiEvent {
    data class UpdateLabel(val newLabel: String) : DetailUiEvent
    data object DeleteCard : DetailUiEvent
    data class SelectApplication(val index: Int) : DetailUiEvent
    data object SaveToVault : DetailUiEvent
}
