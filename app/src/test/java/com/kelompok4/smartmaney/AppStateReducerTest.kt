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
    fun selectTab_profile_updatesSelectedTab() {
        val initial = DashboardUiState(selectedTab = DashboardTab.Home)

        val updated = reduceDashboardState(initial, DashboardAction.SelectTab(DashboardTab.Profile))

        assertEquals(DashboardTab.Profile, updated.selectedTab)
    }
}

