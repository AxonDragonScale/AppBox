package com.axondragonscale.qrqanvas.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.qrqanvas.repository.ScanHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 09/07/25
 */
class HistoryVM(
    private val scanHistoryRepository: ScanHistoryRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(HistoryUiState())

    private val uiEffectChannel = Channel<HistoryUiEffect>(Channel.BUFFERED)
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    init {
        observeScanHistory()
    }

    fun onEvent(event: HistoryUiEvent): Unit = when (event) {
        is HistoryUiEvent.ItemClicked -> onItemClicked(event.id)
        is HistoryUiEvent.DeleteItem -> onDeleteItem(event.id)
        is HistoryUiEvent.ClearAll -> onClearAll()
        is HistoryUiEvent.ConfirmClearAll -> onConfirmClearAll()
        is HistoryUiEvent.DismissClearAll -> onDismissClearAll()
        is HistoryUiEvent.CopyItem -> onCopyItem(event.rawValue)
        is HistoryUiEvent.ShareItem -> onShareItem(event.rawValue)
        is HistoryUiEvent.OpenItem -> onOpenItem(event.rawValue)
    }

    private fun observeScanHistory() = viewModelScope.launch {
        scanHistoryRepository.getAll().collect { entries ->
            uiState.update { it.copy(scanHistory = entries) }
        }
    }

    private fun onItemClicked(id: Long) {
        uiState.update { state ->
            state.copy(
                expandedItemId = if (state.expandedItemId == id) null else id,
            )
        }
    }

    private fun onDeleteItem(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            scanHistoryRepository.delete(id)
            // Collapse if the deleted item was expanded
            uiState.update { state ->
                if (state.expandedItemId == id) state.copy(expandedItemId = null)
                else state
            }
            uiEffectChannel.send(HistoryUiEffect.ShowSnackbar("Scan entry deleted"))
        }
    }

    private fun onClearAll() {
        uiState.update { it.copy(showClearAllDialog = true) }
    }

    private fun onConfirmClearAll() {
        uiState.update { it.copy(showClearAllDialog = false, expandedItemId = null) }
        viewModelScope.launch(Dispatchers.IO) {
            scanHistoryRepository.deleteAll()
            uiEffectChannel.send(HistoryUiEffect.ShowSnackbar("Scan history cleared"))
        }
    }

    private fun onDismissClearAll() {
        uiState.update { it.copy(showClearAllDialog = false) }
    }

    private fun onCopyItem(rawValue: String) {
        viewModelScope.launch {
            uiEffectChannel.send(HistoryUiEffect.CopyToClipboard(rawValue))
        }
    }

    private fun onShareItem(rawValue: String) {
        viewModelScope.launch {
            uiEffectChannel.send(HistoryUiEffect.ShareText(rawValue))
        }
    }

    private fun onOpenItem(rawValue: String) {
        viewModelScope.launch {
            uiEffectChannel.send(HistoryUiEffect.OpenUrl(rawValue))
        }
    }
}
