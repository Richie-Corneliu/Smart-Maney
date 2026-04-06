package com.kelompok4.smartmaney

import org.junit.Assert.assertEquals
import org.junit.Test

class AppStateReducerTest {

    @Test
    fun selectTab_updatesSelectedTab() {
        val initial = DashboardUiState(selectedTab = DashboardTab.Home)

        val updated = reduceDashboardState(initial, DashboardAction.SelectTab(DashboardTab.Reports))

        assertEquals(DashboardTab.Reports, updated.selectedTab)
    }

    @Test
    fun updateMonthlyBudget_setsNewBudgetValue() {
        val initial = DashboardUiState(monthlyBudget = 7_000_000)

        val updated = reduceDashboardState(initial, DashboardAction.UpdateMonthlyBudget(8_000_000))

        assertEquals(8_000_000, updated.monthlyBudget)
    }
}

