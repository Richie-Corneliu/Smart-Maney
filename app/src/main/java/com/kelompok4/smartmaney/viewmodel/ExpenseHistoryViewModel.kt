package com.kelompok4.smartmaney.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.smartmaney.data.repository.SmartManeyRepository
import com.kelompok4.smartmaney.ui.expensehistory.ExpenseFilter
import com.kelompok4.smartmaney.ui.expensehistory.ExpenseHistoryUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ExpenseHistoryViewModel(
    repository: SmartManeyRepository
) : ViewModel() {
    private val selectedFilter = MutableStateFlow(ExpenseFilter.Daily)

    val uiState: StateFlow<ExpenseHistoryUiState> = repository.expenseHistoryUiState(selectedFilter).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExpenseHistoryUiState()
    )


    fun selectFilter(filter: ExpenseFilter) {
        selectedFilter.value = filter
    }
}

