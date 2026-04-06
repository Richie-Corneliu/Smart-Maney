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
import com.kelompok4.smartmaney.DashboardTab // Perlu diimport untuk mengecek Tab
import com.kelompok4.smartmaney.DashboardUiState
import com.kelompok4.smartmaney.reduceDashboardState
import com.kelompok4.smartmaney.ui.dashboard.DashboardScreen
import com.kelompok4.smartmaney.ui.login.LoginScreen
// IMPORT HARUS DITAMBAHKAN AGAR KOMPILER TAHU LOKASI HALAMAN KAMERA LU
import com.kelompok4.smartmaney.ui.scanreceipt.ScanReceiptScreen

// IMPORT HALAMAN BUATAN KAMU (Pastikan package-nya huruf kecil sesuai yang sudah kita perbaiki)
import com.kelompok4.smartmaney.ui.monthlyreport.MonthlyRecapScreen
import com.kelompok4.smartmaney.ui.budgetplanning.BudgetPlanningScreen

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
                    // TAMBAHAN: Pindah ke Budget Planning saat tombol ini ditekan
                    navController.navigate("budget_planning_route")
                },
                onTabSelected = { tab ->
                    dashboardState = reduceDashboardState(
                        dashboardState,
                        DashboardAction.SelectTab(tab)
                    )
                    // TAMBAHAN: Pindah ke Monthly Report jika tab Reports ditekan
                    if (tab == DashboardTab.Reports) {
                        navController.navigate("monthly_report_route")
                    }
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
                }
            )
        }

        // ==========================================
        // RUANGAN BARU UNTUK MONTHLY REPORT
        // ==========================================
        composable(route = "monthly_report_route") {
            MonthlyRecapScreen(
                onBackClick = {
                    // Agar icon tab kembali aktif di "Home" setelah user menekan tombol back
                    dashboardState = reduceDashboardState(
                        dashboardState,
                        DashboardAction.SelectTab(DashboardTab.Home)
                    )
                    navController.popBackStack()
                }
            )
        }

        // ==========================================
        // RUANGAN BARU UNTUK BUDGET PLANNING
        // ==========================================
        composable(route = "budget_planning_route") {
            BudgetPlanningScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}