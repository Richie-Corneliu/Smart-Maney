package com.kelompok4.smartmaney.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.smartmaney.data.remote.service.GeminiReceiptService
import com.kelompok4.smartmaney.data.repository.SmartManeyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScanReceiptUiState(
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val navigateToTransactionId: Long? = null
)

class ScanReceiptViewModel(
    private val repository: SmartManeyRepository,
    private val receiptService: GeminiReceiptService
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScanReceiptUiState())
    val uiState: StateFlow<ScanReceiptUiState> = _uiState

    fun submitReceiptImage(imageBytes: ByteArray) {
        _uiState.update {
            it.copy(
                isProcessing = true,
                errorMessage = null,
                navigateToTransactionId = null
            )
        }
        viewModelScope.launch {
            val receipt = runCatching { receiptService.parseReceipt(imageBytes) }
                .getOrElse {
                    _uiState.update { state ->
                        state.copy(isProcessing = false, errorMessage = "Receipt parsing failed. Try again.")
                    }
                    return@launch
                }
            if (!receipt.isReceipt) {
                _uiState.update { state ->
                    state.copy(isProcessing = false, errorMessage = "No receipt detected. Point the camera at a receipt or invoice.")
                }
                return@launch
            }
            val transactionId = repository.createDraftTransactionFromReceipt(receipt)
            _uiState.update {
                it.copy(
                    isProcessing = false,
                    navigateToTransactionId = transactionId
                )
            }
        }
    }

    fun consumeNavigation() {
        _uiState.update { it.copy(navigateToTransactionId = null) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

}