package com.kelompok4.smartmaney

enum class DashboardTab {
    Home,
    Wallet,
    Add,
    Reports,
    Profile
}

data class DashboardUiState(
    val selectedTab: DashboardTab = DashboardTab.Home,
    val monthlyBudget: Int = 7_000_000
)

sealed interface DashboardAction {
    data class SelectTab(val tab: DashboardTab) : DashboardAction
    data class UpdateMonthlyBudget(val value: Int) : DashboardAction
}

fun reduceDashboardState(current: DashboardUiState, action: DashboardAction): DashboardUiState {
    return when (action) {
        is DashboardAction.SelectTab -> current.copy(selectedTab = action.tab)
        is DashboardAction.UpdateMonthlyBudget -> current.copy(monthlyBudget = action.value)
    }
}

