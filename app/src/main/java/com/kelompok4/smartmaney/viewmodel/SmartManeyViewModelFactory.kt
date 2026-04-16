package com.kelompok4.smartmaney.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kelompok4.smartmaney.data.repository.SmartManeyRepository

class SmartManeyViewModelFactory(
    private val repository: SmartManeyRepository,
    private val transactionId: Long? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel(repository) as T
            modelClass.isAssignableFrom(WalletViewModel::class.java) -> WalletViewModel(repository) as T
            modelClass.isAssignableFrom(ExpenseHistoryViewModel::class.java) -> ExpenseHistoryViewModel(repository) as T
            modelClass.isAssignableFrom(BudgetPlanningViewModel::class.java) -> BudgetPlanningViewModel(repository) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel(repository) as T
            modelClass.isAssignableFrom(TransactionDetailViewModel::class.java) -> {
                val id = requireNotNull(transactionId) { "Transaction ID is required for TransactionDetailViewModel." }
                TransactionDetailViewModel(repository, id) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

