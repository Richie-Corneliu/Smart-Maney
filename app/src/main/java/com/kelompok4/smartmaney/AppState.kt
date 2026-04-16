package com.kelompok4.smartmaney

enum class DashboardTab {
    Home,
    Wallet,
    Add,
    Reports,
    Profile
}

data class DashboardUiState(
    val selectedTab: DashboardTab = DashboardTab.Home
)

sealed interface DashboardAction {
    data class SelectTab(val tab: DashboardTab) : DashboardAction
}

fun reduceDashboardState(current: DashboardUiState, action: DashboardAction): DashboardUiState {
    return when (action) {
        is DashboardAction.SelectTab -> current.copy(selectedTab = action.tab)
    }
}

