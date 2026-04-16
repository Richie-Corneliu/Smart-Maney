package com.kelompok4.smartmaney.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.InsertChartOutlined
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kelompok4.smartmaney.DashboardAction
import com.kelompok4.smartmaney.DashboardTab
import com.kelompok4.smartmaney.DashboardUiState
import com.kelompok4.smartmaney.reduceDashboardState
import com.kelompok4.smartmaney.ui.budgetplanning.BudgetPlanningScreen
import com.kelompok4.smartmaney.ui.dashboard.DashboardScreen
import com.kelompok4.smartmaney.ui.detail.TransactionDetailScreen
import com.kelompok4.smartmaney.ui.expensehistory.ExpenseHistoryScreen
import com.kelompok4.smartmaney.ui.login.LoginScreen
import com.kelompok4.smartmaney.ui.profile.ProfileScreen
import com.kelompok4.smartmaney.ui.scanreceipt.ScanReceiptScreen
import com.kelompok4.smartmaney.ui.theme.SmMuted
import com.kelompok4.smartmaney.ui.theme.SmPrimary
import com.kelompok4.smartmaney.ui.transaction.EditTransactionScreen
import com.kelompok4.smartmaney.ui.wallet.WalletScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    var dashboardState by remember { mutableStateOf(DashboardUiState()) }

    // --- STATE SEMENTARA (IN-MEMORY) UNTUK TRANSAKSI ---
    var currentNominal by remember { mutableStateOf("Rp 350,000") }
    var currentCatatan by remember { mutableStateOf("Mokel with arek arek di gacoan saat puasa") }

    fun navigateToTab(tab: DashboardTab) {
        dashboardState = reduceDashboardState(dashboardState, DashboardAction.SelectTab(tab))
        val targetRoute = routeForTab(tab) ?: return
        navController.navigate(targetRoute) {
            launchSingleTop = true
            restoreState = true
            popUpTo(AppDestinations.DASHBOARD_ROUTE) {
                saveState = true
            }
        }
    }

    fun syncTab(tab: DashboardTab) {
        dashboardState = reduceDashboardState(dashboardState, DashboardAction.SelectTab(tab))
    }

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
            syncTab(DashboardTab.Home)
            AppShellScaffold(
                selectedTab = dashboardState.selectedTab,
                onTabSelected = ::navigateToTab,
                onScanReceiptClick = { navController.navigate(AppDestinations.SCAN_RECEIPT_ROUTE) }
            ) { innerPadding ->
                DashboardScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    userName = "Andra",
                    monthlySpent = 4_500_000,
                    monthlyBudget = dashboardState.monthlyBudget,
                    budgetProgress = 0.64f,
                    selectedTab = dashboardState.selectedTab,
                    onAdjustBudgetClick = {
                        navController.navigate(AppDestinations.BUDGET_PLANNING_ROUTE)
                    },
                    onTabSelected = ::navigateToTab,
                    onLogoutClick = {
                        navController.navigate(AppDestinations.LOGIN_ROUTE) {
                            popUpTo(AppDestinations.DASHBOARD_ROUTE) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onScanReceiptClick = { navController.navigate(AppDestinations.SCAN_RECEIPT_ROUTE) },
                    showNavigationBar = false,
                    onMonthlyRecapClick = {
                        navController.navigate(
                            AppDestinations.expenseHistoryRoute(
                                AppDestinations.EXPENSE_HISTORY_MODE_MONTHLY_RECAP
                            )
                        )
                    },
                )
            }
        }

        composable(route = AppDestinations.SCAN_RECEIPT_ROUTE) {
            ScanReceiptScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onPhotoSaved = {
                    // KETIKA FOTO SUKSES, LANGSUNG ARAHKAN KE LAYAR DETAIL
                    navController.navigate("transaction_detail_route") {
                        // Opsi tambahan: Hapus layar kamera dari tumpukan (backstack)
                        // supaya kalau user tekan tombol 'Back' di layar detail, dia nggak balik ke kamera lagi.
                        popUpTo("scan_receipt_route") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = AppDestinations.EXPENSE_HISTORY_ROUTE_PATTERN,
            arguments = listOf(
                navArgument(AppDestinations.EXPENSE_HISTORY_MODE_ARG) {
                    type = NavType.StringType
                    defaultValue = AppDestinations.EXPENSE_HISTORY_MODE_DEFAULT
                }
            )
        ) {
            syncTab(DashboardTab.Reports)
            AppShellScaffold(
                selectedTab = dashboardState.selectedTab,
                onTabSelected = ::navigateToTab,
                onScanReceiptClick = { navController.navigate(AppDestinations.SCAN_RECEIPT_ROUTE) }
            ) { innerPadding ->
                ExpenseHistoryScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                )
            }
        }

        composable(route = AppDestinations.BUDGET_PLANNING_ROUTE) {
            syncTab(DashboardTab.Wallet)
            AppShellScaffold(
                selectedTab = dashboardState.selectedTab,
                onTabSelected = ::navigateToTab,
                onScanReceiptClick = { navController.navigate(AppDestinations.SCAN_RECEIPT_ROUTE) }
            ) { innerPadding ->
                BudgetPlanningScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding()),
                    onBackClick = {
                        navigateToTab(DashboardTab.Home)
                    }
                )
            }
        }

        composable(route = AppDestinations.WALLET_ROUTE) {
            syncTab(DashboardTab.Wallet)
            AppShellScaffold(
                selectedTab = dashboardState.selectedTab,
                onTabSelected = ::navigateToTab,
                onScanReceiptClick = { navController.navigate(AppDestinations.SCAN_RECEIPT_ROUTE) }
            ) { innerPadding ->
                WalletScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding()))
            }
        }

        composable(route = AppDestinations.PROFILE_ROUTE) {
            syncTab(DashboardTab.Profile)
            AppShellScaffold(
                selectedTab = dashboardState.selectedTab,
                onTabSelected = ::navigateToTab,
                onScanReceiptClick = { navController.navigate(AppDestinations.SCAN_RECEIPT_ROUTE) }
            ) { innerPadding ->
                ProfileScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding()),
                    onLogoutClick = {
                        navController.navigate(AppDestinations.LOGIN_ROUTE) {
                            popUpTo(AppDestinations.DASHBOARD_ROUTE) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(route = "transaction_detail_route") {
            TransactionDetailScreen(
                nominal = currentNominal,   // Masukkan state ke layar detail
                catatan = currentCatatan,   // Masukkan state ke layar detail
                onBackClick = { navController.popBackStack() },
                onSaveClick = { /* Nanti untuk db beneran */ },
                onEditClick = {
                    // Loncat ke layar edit saat ditekan
                    navController.navigate("edit_transaction_route")
                }
            )
        }

        // 2. TAMBAHKAN RUTE EDIT BARU
        composable(route = "edit_transaction_route") {
            EditTransactionScreen(
                initialNominal = currentNominal,
                initialNote = currentCatatan,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { newNominal, newNote ->
                    // Saat user klik simpan, perbarui state memori kita
                    currentNominal = newNominal
                    currentCatatan = newNote
                    // Lalu kembalikan user ke layar detail
                    navController.popBackStack()
                }
            )
        }
    }
}

private fun routeForTab(tab: DashboardTab): String? = when (tab) {
    DashboardTab.Home -> AppDestinations.DASHBOARD_ROUTE
    DashboardTab.Wallet -> AppDestinations.WALLET_ROUTE
    DashboardTab.Reports -> AppDestinations.EXPENSE_HISTORY_ROUTE
    DashboardTab.Profile -> AppDestinations.PROFILE_ROUTE
    DashboardTab.Add -> null
}

@Composable
private fun AppShellScaffold(
    selectedTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit,
    onScanReceiptClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onScanReceiptClick,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier
                    .size(60.dp)
                    .offset(y = 60.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Scan Receipt",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            AppBottomBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

@Composable
private fun AppBottomBar(
    selectedTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit
) {
    BottomAppBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AppBottomNavItem(Icons.Default.Home, DashboardTab.Home, selectedTab, onTabSelected)
                AppBottomNavItem(Icons.Default.Wallet, DashboardTab.Wallet, selectedTab, onTabSelected)
            }

            Spacer(modifier = Modifier.width(72.dp))

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AppBottomNavItem(Icons.Default.InsertChartOutlined, DashboardTab.Reports, selectedTab, onTabSelected)
                AppBottomNavItem(Icons.Default.Person, DashboardTab.Profile, selectedTab, onTabSelected)
            }
        }
    }
}

@Composable
private fun AppBottomNavItem(
    imageVector: ImageVector,
    tab: DashboardTab,
    selectedTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit
) {
    val isSelected = tab == selectedTab
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = { onTabSelected(tab) }) {
            Icon(
                imageVector = imageVector,
                contentDescription = tab.name,
                tint = if (isSelected) SmPrimary else SmMuted
            )
        }

        Text(
            text = tab.name,
            color = if (isSelected) SmPrimary else SmMuted,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}