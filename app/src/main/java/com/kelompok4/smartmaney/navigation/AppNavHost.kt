package com.kelompok4.smartmaney.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.kelompok4.smartmaney.AppContainer
import com.kelompok4.smartmaney.DashboardAction
import com.kelompok4.smartmaney.DashboardTab
import com.kelompok4.smartmaney.DashboardUiState
import com.kelompok4.smartmaney.R
import com.kelompok4.smartmaney.reduceDashboardState
import com.kelompok4.smartmaney.ui.budgetplanning.BudgetPlanningScreen
import com.kelompok4.smartmaney.ui.dashboard.DashboardScreen
import com.kelompok4.smartmaney.ui.detail.EditTransactionScreen
import com.kelompok4.smartmaney.ui.detail.TransactionDetailScreen
import com.kelompok4.smartmaney.ui.expensehistory.ExpenseFilter
import com.kelompok4.smartmaney.ui.expensehistory.ExpenseHistoryScreen
import com.kelompok4.smartmaney.ui.login.LoginScreen
import com.kelompok4.smartmaney.ui.onboarding.OnboardingScreen
import com.kelompok4.smartmaney.ui.profile.ProfileScreen
import com.kelompok4.smartmaney.ui.scanreceipt.ScanReceiptScreen
import com.kelompok4.smartmaney.ui.scheduledbills.ScheduledBillsScreen
import com.kelompok4.smartmaney.ui.settings.DeleteAccountState
import com.kelompok4.smartmaney.ui.settings.SettingsScreen
import com.kelompok4.smartmaney.ui.suggestion.SuggestionScreen
import com.kelompok4.smartmaney.ui.theme.SmMuted
import com.kelompok4.smartmaney.ui.theme.SmPrimary
import com.kelompok4.smartmaney.ui.wallet.WalletScreen
import com.kelompok4.smartmaney.viewmodel.BudgetPlanningViewModel
import com.kelompok4.smartmaney.viewmodel.DashboardViewModel
import com.kelompok4.smartmaney.viewmodel.ExpenseHistoryViewModel
import com.kelompok4.smartmaney.viewmodel.OnboardingViewModel
import com.kelompok4.smartmaney.viewmodel.ProfileViewModel
import com.kelompok4.smartmaney.viewmodel.ScanReceiptViewModel
import com.kelompok4.smartmaney.viewmodel.ScheduledBillsViewModel
import com.kelompok4.smartmaney.viewmodel.SettingsViewModel
import com.kelompok4.smartmaney.viewmodel.SmartManeyViewModelFactory
import com.kelompok4.smartmaney.viewmodel.SuggestionViewModel
import com.kelompok4.smartmaney.viewmodel.TransactionDetailViewModel
import com.kelompok4.smartmaney.viewmodel.WalletViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    appContainer: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    var dashboardState by remember { mutableStateOf(DashboardUiState()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val resources = LocalResources.current
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    var authenticatedUserId by remember { mutableStateOf(firebaseAuth.currentUser?.uid) }
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val defaultWebClientId = remember(context) {
        val stringId = resources.getIdentifier(
            "default_web_client_id",
            "string",
            context.packageName
        )
        if (stringId == 0) "" else resources.getString(stringId)
    }
    val googleSignInFailedMessage = stringResource(R.string.google_sign_in_failed)
    val googleSignInCancelledMessage = stringResource(R.string.google_sign_in_cancelled)

    val baseFactory = remember(appContainer) {
        SmartManeyViewModelFactory(appContainer.repository)
    }
    val scanFactory = remember(appContainer) {
        SmartManeyViewModelFactory(
            repository = appContainer.repository,
            geminiReceiptService = appContainer.geminiReceiptService
        )
    }
    val settingsFactory = remember(appContainer) {
        SmartManeyViewModelFactory(
            repository = appContainer.repository,
            themePreferenceStore = appContainer.themePreferenceStore,
            currencyPreferenceStore = appContainer.currencyPreferenceStore
        )
    }

    val onboardingViewModel: OnboardingViewModel = viewModel(factory = baseFactory)

    var startDestination by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        val user = firebaseAuth.currentUser
        startDestination = when {
            user == null -> AppDestinations.LOGIN_ROUTE
            appContainer.repository.isOnboardingComplete() -> AppDestinations.DASHBOARD_ROUTE
            else -> {
                onboardingViewModel.setUserName(user.displayName.orEmpty())
                AppDestinations.ONBOARDING_ROUTE
            }
        }
    }
    val dashboardViewModel: DashboardViewModel = viewModel(factory = baseFactory)
    val walletViewModel: WalletViewModel = viewModel(factory = baseFactory)
    val expenseHistoryViewModel: ExpenseHistoryViewModel = viewModel(factory = baseFactory)
    val budgetPlanningViewModel: BudgetPlanningViewModel = viewModel(factory = baseFactory)
    val profileViewModel: ProfileViewModel = viewModel(factory = baseFactory)
    val scanReceiptViewModel: ScanReceiptViewModel = viewModel(factory = scanFactory)

    val dashboardSummary by dashboardViewModel.summary.collectAsState()
    val walletUiState by walletViewModel.uiState.collectAsState()
    val expenseHistoryUiState by expenseHistoryViewModel.uiState.collectAsState()
    val budgetPlanningUiState by budgetPlanningViewModel.uiState.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val scanReceiptUiState by scanReceiptViewModel.uiState.collectAsState()

    val scheduledBillsViewModel: ScheduledBillsViewModel = viewModel()
    val scheduledBillsUiState by scheduledBillsViewModel.uiState.collectAsState()

    fun syncProfileWithAuthenticatedUser(user: FirebaseUser?) {
        if (user == null) return
        profileViewModel.syncAuthenticatedProfile(
            fullName = user.displayName,
            email = user.email
        )
    }

    LaunchedEffect(authenticatedUserId) {
        syncProfileWithAuthenticatedUser(firebaseAuth.currentUser)
    }

    fun navigateToTab(tab: DashboardTab) {
        val targetRoute = routeForTab(tab) ?: return

        // 1. JIKA MENGKLIK TAB YANG SEDANG AKTIF (Contoh: Sedang di Adjust Budget lalu klik Home)
        if (tab == dashboardState.selectedTab) {
            navController.popBackStack(targetRoute, inclusive = false)
            return
        }

        // 2. JIKA PINDAH DARI TAB LAIN (Contoh: Dari Wallet lalu klik Home)
        dashboardState = reduceDashboardState(dashboardState, DashboardAction.SelectTab(tab))
        navController.navigate(targetRoute) {
            launchSingleTop = true

            // KUNCI PERBAIKAN: Jika tujuan akhir adalah Home, set ke false agar halaman anak (Adjust Budget) tidak ikut dipanggil kembali
            restoreState = (tab != DashboardTab.Home)

            popUpTo(AppDestinations.DASHBOARD_ROUTE) {
                saveState = true
            }
        }
    }

    fun navigateToDashboardFromLogin() {
        navController.navigate(AppDestinations.DASHBOARD_ROUTE) {
            popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
            launchSingleTop = true
        }
    }

    val resolvedStart = startDestination ?: return

    NavHost(
        navController = navController,
        startDestination = resolvedStart,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(route = AppDestinations.LOGIN_ROUTE) {
            var isGoogleSigningIn by remember { mutableStateOf(false) }
            var googleSignInError by remember { mutableStateOf<String?>(null) }

            LoginScreen(
                onGoogleClick = {
                    googleSignInError = null
                    isGoogleSigningIn = true
                    scope.launch {
                        if (defaultWebClientId.isBlank()) {
                            isGoogleSigningIn = false
                            googleSignInError = googleSignInFailedMessage
                            return@launch
                        }
                        try {
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setServerClientId(defaultWebClientId)
                                .setFilterByAuthorizedAccounts(false)
                                .setAutoSelectEnabled(false)
                                .build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()
                            val credentialResult = credentialManager.getCredential(
                                context = context,
                                request = request
                            )
                            val credential = credentialResult.credential
                            if (
                                credential is CustomCredential &&
                                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                            ) {
                                val googleIdTokenCredential = GoogleIdTokenCredential
                                    .createFrom(credential.data)
                                val firebaseCredential = GoogleAuthProvider.getCredential(
                                    googleIdTokenCredential.idToken,
                                    null
                                )
                                firebaseAuth.signInWithCredential(firebaseCredential)
                                    .addOnCompleteListener { authTask ->
                                        if (authTask.isSuccessful) {
                                            val user = authTask.result?.user
                                            authenticatedUserId = user?.uid
                                            syncProfileWithAuthenticatedUser(user)
                                            googleSignInError = null
                                            scope.launch {
                                                try {
                                                    val uid = user?.uid
                                                    if (uid != null) {
                                                        runCatching {
                                                            appContainer.repository.syncFromFirestore(uid)
                                                        }
                                                    }
                                                    val displayName = user?.displayName.orEmpty()
                                                    if (appContainer.repository.isOnboardingComplete()) {
                                                        navigateToDashboardFromLogin()
                                                    } else {
                                                        onboardingViewModel.setUserName(displayName)
                                                        navController.navigate(AppDestinations.ONBOARDING_ROUTE) {
                                                            popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                                                            launchSingleTop = true
                                                        }
                                                    }
                                                } finally {
                                                    isGoogleSigningIn = false
                                                }
                                            }
                                        } else {
                                            isGoogleSigningIn = false
                                            googleSignInError = googleSignInFailedMessage
                                        }
                                    }
                            } else {
                                isGoogleSigningIn = false
                                googleSignInError = googleSignInFailedMessage
                            }
                        } catch (_: GetCredentialException) {
                            isGoogleSigningIn = false
                            googleSignInError = googleSignInCancelledMessage
                        } catch (_: GoogleIdTokenParsingException) {
                            isGoogleSigningIn = false
                            googleSignInError = googleSignInFailedMessage
                        }
                    }
                },
                isGoogleSigningIn = isGoogleSigningIn,
                googleSignInError = googleSignInError
            )
        }

        composable(route = AppDestinations.ONBOARDING_ROUTE) {
            val onboardingUiState by onboardingViewModel.uiState.collectAsState()
            OnboardingScreen(
                uiState = onboardingUiState,
                onNextStep = onboardingViewModel::nextStep,
                onPrevStep = onboardingViewModel::prevStep,
                onBalanceChanged = onboardingViewModel::onBalanceChanged,
                onBudgetChanged = onboardingViewModel::onBudgetChanged,
                onNavigateToDashboard = {
                    navController.navigate(AppDestinations.DASHBOARD_ROUTE) {
                        popUpTo(AppDestinations.ONBOARDING_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = AppDestinations.DASHBOARD_ROUTE) {
            AppShellScaffold(
                selectedTab = dashboardState.selectedTab,
                onTabSelected = ::navigateToTab,
                onScanReceiptClick = { navController.navigate(AppDestinations.SCAN_RECEIPT_ROUTE) }
            ) { innerPadding ->
                DashboardScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    userName = dashboardSummary.userName,
                    monthlySpent = dashboardSummary.monthlySpent,
                    monthlyBudget = dashboardSummary.monthlyBudget,
                    budgetProgress = dashboardSummary.budgetProgress,
                    spendingByCategory = dashboardSummary.spendingByCategory,
                    monthlyChangePercent = dashboardSummary.monthlyChangePercent,
                    selectedTab = dashboardState.selectedTab,
                    onAdjustBudgetClick = {
                        navController.navigate(AppDestinations.BUDGET_PLANNING_ROUTE)
                    },
                    onTabSelected = ::navigateToTab,
                    onLogoutClick = {
                        firebaseAuth.signOut()
                        authenticatedUserId = null
                        scope.launch {
                            credentialManager.clearCredentialState(ClearCredentialStateRequest())
                            runCatching { appContainer.repository.clearLocalData() }
                        }
                        navController.navigate(AppDestinations.LOGIN_ROUTE) {
                            popUpTo(AppDestinations.DASHBOARD_ROUTE) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onScanReceiptClick = { navController.navigate(AppDestinations.SCAN_RECEIPT_ROUTE) },
                    onScheduledBillsClick = { navController.navigate("scheduled_bills_route") },
                    showNavigationBar = false,
                    onMonthlyRecapClick = {
                        expenseHistoryViewModel.selectFilter(ExpenseFilter.Monthly)
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
                onBackClick = { navController.popBackStack() },
                uiState = scanReceiptUiState,
                onImageCaptured = scanReceiptViewModel::submitReceiptImage,
                onNavigateToDetail = { transactionId ->
                    navController.navigate(AppDestinations.transactionDetailRoute(transactionId)) {
                        popUpTo(AppDestinations.SCAN_RECEIPT_ROUTE) { inclusive = true }
                    }
                },
                onNavigationConsumed = scanReceiptViewModel::consumeNavigation,
                onDismissError = scanReceiptViewModel::dismissError
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
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString(AppDestinations.EXPENSE_HISTORY_MODE_ARG)
            LaunchedEffect(mode) {
                if (mode == AppDestinations.EXPENSE_HISTORY_MODE_MONTHLY_RECAP) {
                    expenseHistoryViewModel.selectFilter(ExpenseFilter.Monthly)
                }
            }
            AppShellScaffold(
                selectedTab = dashboardState.selectedTab,
                onTabSelected = ::navigateToTab,
                onScanReceiptClick = { navController.navigate(AppDestinations.SCAN_RECEIPT_ROUTE) }
            ) { innerPadding ->
                ExpenseHistoryScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding()),
                    uiState = expenseHistoryUiState,
                    onFilterSelected = expenseHistoryViewModel::selectFilter,

                    // 👉 PASTIKAN DUA BARIS INI BENAR-BENAR ADA DI KODEMU:
                    onSearchQueryChange = expenseHistoryViewModel::updateSearchQuery,
                    onSortOrderChange = expenseHistoryViewModel::updateSortOrder
                )
            }
        }

        composable(route = AppDestinations.BUDGET_PLANNING_ROUTE) {
            AppShellScaffold(
                selectedTab = dashboardState.selectedTab,
                onTabSelected = ::navigateToTab,
                onScanReceiptClick = { navController.navigate(AppDestinations.SCAN_RECEIPT_ROUTE) }
            ) { innerPadding ->
                BudgetPlanningScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding()),
                    uiState = budgetPlanningUiState,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onBudgetUpdated = { newBudget ->
                        dashboardViewModel.updateMonthlyBudget(newBudget)
                    }
                )
            }
        }

        composable(route = AppDestinations.WALLET_ROUTE) {
            AppShellScaffold(
                selectedTab = dashboardState.selectedTab,
                onTabSelected = ::navigateToTab,
                onScanReceiptClick = { navController.navigate(AppDestinations.SCAN_RECEIPT_ROUTE) }
            ) { innerPadding ->
                WalletScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding()),
                    uiState = walletUiState,
                    onAddTransaction = walletViewModel::addTransaction,
                    onDeleteTransaction = walletViewModel::deleteTransaction,
                    onAdjustBaseBalance = walletViewModel::adjustInitialBalance,
                    onSuggestionClick = {
                        navController.navigate(AppDestinations.SUGGESTION_ROUTE)
                    }
                )
            }
        }

        composable(route = AppDestinations.PROFILE_ROUTE) {
            AppShellScaffold(
                selectedTab = dashboardState.selectedTab,
                onTabSelected = ::navigateToTab,
                onScanReceiptClick = { navController.navigate(AppDestinations.SCAN_RECEIPT_ROUTE) }
            ) { innerPadding ->
                ProfileScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding()),
                    uiState = profileUiState,
                    onAction = profileViewModel::dispatch,
                    isEmailEditable = firebaseAuth.currentUser == null,
                    onSettingsClick = {
                        navController.navigate(AppDestinations.SETTINGS_ROUTE)
                    },
                    onLogoutClick = {
                        firebaseAuth.signOut()
                        authenticatedUserId = null
                        scope.launch {
                            credentialManager.clearCredentialState(ClearCredentialStateRequest())
                            runCatching { appContainer.repository.clearLocalData() }
                        }
                        navController.navigate(AppDestinations.LOGIN_ROUTE) {
                            popUpTo(AppDestinations.DASHBOARD_ROUTE) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(
            route = AppDestinations.TRANSACTION_DETAIL_ROUTE_PATTERN,
            arguments = listOf(
                navArgument(AppDestinations.TRANSACTION_ID_ARG) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong(AppDestinations.TRANSACTION_ID_ARG) ?: 0L
            val detailFactory = remember(transactionId) {
                SmartManeyViewModelFactory(appContainer.repository, transactionId)
            }
            val detailViewModel: TransactionDetailViewModel = viewModel(factory = detailFactory)
            val detailState by detailViewModel.uiState.collectAsState()

            TransactionDetailScreen(
                amount = detailState.amount,
                note = detailState.note,
                category = detailState.category.ifBlank { "Lain-lain" },
                paymentMethod = detailState.paymentMethod.ifBlank { "Cash" },
                createdAtMillis = detailState.createdAtMillis,
                onBackClick = { navController.popBackStack() },
                onEditClick = {
                    navController.navigate(AppDestinations.transactionEditRoute(transactionId))
                }
            )
        }

        composable(
            route = AppDestinations.TRANSACTION_EDIT_ROUTE_PATTERN,
            arguments = listOf(
                navArgument(AppDestinations.TRANSACTION_ID_ARG) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong(AppDestinations.TRANSACTION_ID_ARG) ?: 0L
            val detailFactory = remember(transactionId) {
                SmartManeyViewModelFactory(appContainer.repository, transactionId)
            }
            val detailViewModel: TransactionDetailViewModel = viewModel(factory = detailFactory)
            val detailState by detailViewModel.uiState.collectAsState()

            EditTransactionScreen(
                initialAmount = detailState.amount,
                initialNote = detailState.note,
                initialCategory = detailState.category,
                initialPaymentMethod = detailState.paymentMethod,
                initialCreatedAtMillis = detailState.createdAtMillis,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { newAmount, newNote, newCategory, newPaymentMethod, newCreatedAtMillis ->
                    detailViewModel.updateTransaction(newAmount, newNote, newCategory, newPaymentMethod, newCreatedAtMillis)
                    navController.popBackStack()
                }
            )
        }

        composable(route = AppDestinations.SUGGESTION_ROUTE) {
            val suggestionFactory = remember {
                SmartManeyViewModelFactory(appContainer.repository)
            }
            val suggestionViewModel: SuggestionViewModel = viewModel(factory = suggestionFactory)

            SuggestionScreen(
                onBackClick = { navController.popBackStack() },
                onSetBudgetClick = {
                    navController.navigate(AppDestinations.BUDGET_PLANNING_ROUTE)
                },
                viewModel = suggestionViewModel
            )
        }

        composable(route = "scheduled_bills_route") {
            ScheduledBillsScreen(
                uiState = scheduledBillsUiState,
                onBackClick = { navController.popBackStack() },
                onPayClick = { billId -> scheduledBillsViewModel.markAsPaid(billId) },
                onAddBill = { title, amt, start, end, freq ->
                    scheduledBillsViewModel.addBill(title, amt, "Umum", start, end, freq)
                },
                onUpdateBill = { id, title, amt, start, end, freq ->
                    scheduledBillsViewModel.updateBill(id, title, amt, start, end, freq)
                }
            )
        }

        composable(route = AppDestinations.SETTINGS_ROUTE) {
            val settingsViewModel: SettingsViewModel = viewModel(factory = settingsFactory)
            val settingsUiState by settingsViewModel.uiState.collectAsState()

            LaunchedEffect(settingsUiState.deleteState) {
                if (settingsUiState.deleteState == DeleteAccountState.Success) {
                    firebaseAuth.signOut()
                    authenticatedUserId = null
                    runCatching {
                        credentialManager.clearCredentialState(ClearCredentialStateRequest())
                    }
                    settingsViewModel.acknowledgeDeleteResult()
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                        popUpTo(AppDestinations.DASHBOARD_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            SettingsScreen(
                uiState = settingsUiState,
                exportEvents = settingsViewModel.exportEvents,
                onBackClick = { navController.popBackStack() },
                onThemeModeSelected = settingsViewModel::setThemeMode,
                onCurrencySelected = settingsViewModel::setCurrency,
                onDeleteAccountRequest = settingsViewModel::requestDeleteAccount,
                onDeleteAccountConfirm = settingsViewModel::confirmDeleteAccount,
                onDeleteAccountCancel = settingsViewModel::cancelDeleteAccount,
                onDeleteAccountResultAcknowledged = settingsViewModel::acknowledgeDeleteResult,
                onExportCsvClick = settingsViewModel::exportTransactionsCsv
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
        containerColor = MaterialTheme.colorScheme.surface,
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