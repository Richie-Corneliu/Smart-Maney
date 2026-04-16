package com.kelompok4.smartmaney.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.smartmaney.data.repository.SmartManeyRepository
import com.kelompok4.smartmaney.ui.wallet.WalletTransactionType
import com.kelompok4.smartmaney.ui.wallet.WalletUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WalletViewModel(
    private val repository: SmartManeyRepository
) : ViewModel() {
    val uiState: StateFlow<WalletUiState> = repository.walletUiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WalletUiState()
    )

    fun addTransaction(title: String, amount: Int, type: WalletTransactionType) {
        viewModelScope.launch {
            repository.addWalletTransaction(title, amount, type)
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            repository.deleteWalletTransaction(transactionId)
        }
    }

    fun adjustInitialBalance(delta: Int) {
        viewModelScope.launch {
            repository.adjustInitialBalance(delta)
        }
    }
}

