package com.kelompok4.smartmaney.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.smartmaney.data.repository.DashboardSummary
import com.kelompok4.smartmaney.data.repository.SmartManeyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: SmartManeyRepository
) : ViewModel() {
    val summary: StateFlow<DashboardSummary> = repository.dashboardSummary.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardSummary(
            userName = "User",
            monthlySpent = 0,
            monthlyBudget = 0,
            budgetProgress = 0f,
            spendingByCategory = emptyMap()
        )
    )

    fun updateMonthlyBudget(value: Int) {
        viewModelScope.launch {
            repository.updateMonthlyBudget(value)
        }
    }
}

