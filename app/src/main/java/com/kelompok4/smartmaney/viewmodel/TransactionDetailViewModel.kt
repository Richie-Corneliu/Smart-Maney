package com.kelompok4.smartmaney.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.smartmaney.data.repository.SmartManeyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TransactionDetailUiState(
    val transactionId: Long = 0,
    val title: String = "",
    val amount: Int = 0,
    val note: String = "",
    val category: String = "",
    val paymentMethod: String = "",
    val createdAtMillis: Long = 0,
    val isReady: Boolean = false
)

class TransactionDetailViewModel(
    private val repository: SmartManeyRepository,
    transactionId: Long
) : ViewModel() {
    val uiState: StateFlow<TransactionDetailUiState> = repository.observeTransaction(transactionId).map { entity ->
        if (entity == null) {
            TransactionDetailUiState(transactionId = transactionId, isReady = false)
        } else {
            TransactionDetailUiState(
                transactionId = entity.id,
                title = entity.title,
                amount = entity.amount,
                note = entity.note,
                category = entity.category,
                paymentMethod = entity.paymentMethod,
                createdAtMillis = entity.createdAtMillis,
                isReady = true
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionDetailUiState(transactionId = transactionId)
    )

    fun updateTransaction(amount: Int, note: String) {
        viewModelScope.launch {
            repository.updateTransaction(
                transactionId = uiState.value.transactionId,
                newAmount = amount,
                newNote = note
            )
        }
    }
}

