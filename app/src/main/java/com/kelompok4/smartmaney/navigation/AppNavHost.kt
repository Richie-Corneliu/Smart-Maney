package com.kelompok4.smartmaney.navigation

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kelompok4.smartmaney.DashboardAction
import com.kelompok4.smartmaney.DashboardUiState
import com.kelompok4.smartmaney.reduceDashboardState
import com.kelompok4.smartmaney.ui.dashboard.DashboardScreen
import com.kelompok4.smartmaney.ui.login.LoginScreen
// IMPORT HARUS DITAMBAHKAN AGAR KOMPILER TAHU LOKASI HALAMAN KAMERA LU
import com.kelompok4.smartmaney.ui.scanreceipt.ScanReceiptScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    var dashboardState by remember { mutableStateOf(DashboardUiState()) }

    NavHost(
        navController = navController,
        startDestination = AppDestinations.LOGIN_ROUTE,
        modifier = modifier
    ) {
        composable(route = AppDestinations.LOGIN_ROUTE) {
            LoginScreen(
                onLoginClick = {
                    navController.navigate(AppDestinations.DASHBOARD_ROUTE) {
                        popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = AppDestinations.DASHBOARD_ROUTE) {
            DashboardScreen(
                modifier = Modifier.fillMaxSize().scrollable(
                    state = rememberScrollState(),
                    orientation = Orientation.Vertical
                ),
                userName = "Andra",
                monthlySpent = 4_500_000,
                monthlyBudget = dashboardState.monthlyBudget,
                budgetProgress = 0.64f,
                selectedTab = dashboardState.selectedTab,
                onAdjustBudgetClick = {
                    dashboardState = reduceDashboardState(
                        dashboardState,
                        DashboardAction.UpdateMonthlyBudget(dashboardState.monthlyBudget + 500_000)
                    )
                },
                onTabSelected = { tab ->
                    dashboardState = reduceDashboardState(
                        dashboardState,
                        DashboardAction.SelectTab(tab)
                    )
                },
                onLogoutClick = {
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                        popUpTo(AppDestinations.DASHBOARD_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                // INI YANG LU TAMBAHKAN: Memberikan perintah pindah rute saat tombol ditekan
                onScanReceiptClick = {
                    navController.navigate("scan_receipt_route")
                }
            )
        }

        // INI BLIND SPOT LU: Lu harus membuat "ruangan" baru untuk halaman kamera lu
        composable(route = "scan_receipt_route") {
            ScanReceiptScreen(
                onBackClick = {
                    navController.popBackStack() // Ini perintah baku Android untuk kembali ke layar sebelumnya
        })
    }
}}