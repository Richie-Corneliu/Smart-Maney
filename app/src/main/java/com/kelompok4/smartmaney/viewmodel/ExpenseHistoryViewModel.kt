package com.kelompok4.smartmaney.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.smartmaney.data.repository.SmartManeyRepository
import com.kelompok4.smartmaney.ui.expensehistory.ExpenseFilter
import com.kelompok4.smartmaney.ui.expensehistory.ExpenseHistoryUiState
import com.kelompok4.smartmaney.ui.expensehistory.ExpenseSortOrder // Pastikan ini ter-import
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ExpenseHistoryViewModel(
    repository: SmartManeyRepository
) : ViewModel() {
    private val selectedFilter = MutableStateFlow(ExpenseFilter.Daily)
    private val searchQuery = MutableStateFlow("")                     // State penampung ketikan search
    private val sortOrder = MutableStateFlow(ExpenseSortOrder.DateNewest) // State penampung pilihan sort

    // Menyambungkan state ke Repository
    val uiState: StateFlow<ExpenseHistoryUiState> = repository.expenseHistoryUiState(
        selectedFilter = selectedFilter,
        searchQuery = searchQuery,
        sortOrder = sortOrder
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExpenseHistoryUiState()
    )

    fun selectFilter(filter: ExpenseFilter) {
        selectedFilter.value = filter
    }

    // FUNGSI INI YANG DICARI SAMA APPNAVHOST LU
    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    // FUNGSI INI JUGA YANG DICARI SAMA APPNAVHOST LU
    fun updateSortOrder(order: ExpenseSortOrder) {
        sortOrder.value = order
    }
}