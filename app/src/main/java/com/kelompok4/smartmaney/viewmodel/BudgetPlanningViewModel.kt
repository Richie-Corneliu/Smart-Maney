package com.kelompok4.smartmaney.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.smartmaney.data.repository.SmartManeyRepository
import com.kelompok4.smartmaney.ui.budgetplanning.BudgetPlanningUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BudgetPlanningViewModel(
    private val repository: SmartManeyRepository
) : ViewModel() {
    val uiState: StateFlow<BudgetPlanningUiState> = repository.budgetPlanningUiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BudgetPlanningUiState()
    )

    fun updateTotalBudget(value: Int) {
        viewModelScope.launch {
            repository.updateMonthlyBudget(value)
        }
    }
}

