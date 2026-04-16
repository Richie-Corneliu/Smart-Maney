package com.kelompok4.smartmaney.data.repository

import com.kelompok4.smartmaney.data.local.SmartManeyDatabase
import com.kelompok4.smartmaney.data.local.entity.BudgetCategoryEntity
import com.kelompok4.smartmaney.data.local.entity.BudgetMetaEntity
import com.kelompok4.smartmaney.data.local.entity.ProfileEntity
import com.kelompok4.smartmaney.data.local.entity.TransactionEntity
import com.kelompok4.smartmaney.data.local.entity.WalletMetaEntity
import com.kelompok4.smartmaney.ui.budgetplanning.BudgetCategoryItem
import com.kelompok4.smartmaney.ui.budgetplanning.BudgetPlanningUiState
import com.kelompok4.smartmaney.ui.expensehistory.ExpenseCategory
import com.kelompok4.smartmaney.ui.expensehistory.ExpenseFilter
import com.kelompok4.smartmaney.ui.expensehistory.ExpenseHistoryUiState
import com.kelompok4.smartmaney.ui.expensehistory.ExpenseTransaction
import com.kelompok4.smartmaney.ui.expensehistory.buildExpenseHistoryState
import com.kelompok4.smartmaney.ui.profile.ProfileUiState
import com.kelompok4.smartmaney.ui.wallet.WalletTransaction
import com.kelompok4.smartmaney.ui.wallet.WalletTransactionType
import com.kelompok4.smartmaney.ui.wallet.WalletUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SmartManeyRepository(
    private val database: SmartManeyDatabase
) {
    private val transactionDao = database.transactionDao()
    private val walletDao = database.walletDao()
    private val budgetDao = database.budgetDao()
    private val profileDao = database.profileDao()

    val walletUiState: Flow<WalletUiState> = combine(
        walletDao.observeWalletMeta(),
        transactionDao.observeAllTransactions()
    ) { walletMeta, transactions ->
        WalletUiState(
            initialBalance = walletMeta?.initialBalance ?: DEFAULT_INITIAL_BALANCE,
            transactions = transactions.map { entity ->
                WalletTransaction(
                    id = entity.id.toString(),
                    title = entity.title,
                    amount = entity.amount,
                    type = entity.toWalletTransactionType(),
                    createdAtMillis = entity.createdAtMillis
                )
            }
        )
    }

    val profileUiState: Flow<ProfileUiState> = profileDao.observeProfile().map { entity ->
        entity?.toProfileUiState() ?: DEFAULT_PROFILE
    }

    val dashboardSummary: Flow<DashboardSummary> = combine(
        profileUiState,
        budgetDao.observeBudgetMeta(),
        observeMonthlyExpenseTotal()
    ) { profile, budgetMeta, monthlyExpense ->
        val monthlyBudget = budgetMeta?.totalBudget ?: DEFAULT_MONTHLY_BUDGET
        val progress = if (monthlyBudget > 0) {
            (monthlyExpense.toFloat() / monthlyBudget.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
        DashboardSummary(
            userName = profile.fullName.ifBlank { "User" },
            monthlySpent = monthlyExpense,
            monthlyBudget = monthlyBudget,
            budgetProgress = progress
        )
    }

    val budgetPlanningUiState: Flow<BudgetPlanningUiState> = combine(
        budgetDao.observeBudgetMeta(),
        budgetDao.observeBudgetCategories(),
        observeCurrentMonthCategorySpendMap()
    ) { budgetMeta, categories, spentByCategory ->
        val totalBudget = budgetMeta?.totalBudget ?: DEFAULT_MONTHLY_BUDGET
        val categoryBudgets = categories.map { entity ->
            BudgetCategoryItem(
                id = entity.id,
                name = entity.name,
                allocated = entity.allocated,
                spent = spentByCategory[entity.name].orZero()
            )
        }
        val totalSpent = categoryBudgets.sumOf { it.spent }
        BudgetPlanningUiState(
            totalBudget = totalBudget,
            totalSpent = totalSpent,
            categoryBudgets = categoryBudgets
        )
    }

    fun expenseHistoryUiState(selectedFilter: Flow<ExpenseFilter>): Flow<ExpenseHistoryUiState> {
        val expenses = transactionDao.observeTransactionsByType(TRANSACTION_TYPE_EXPENSE).map { rows ->
            rows.map { row ->
                ExpenseTransaction(
                    id = row.id.toString(),
                    title = row.title,
                    categoryLabel = row.category,
                    amount = row.amount,
                    timestampMillis = row.createdAtMillis,
                    timeLabel = timeFormatter.format(Date(row.createdAtMillis)),
                    category = row.toExpenseCategory()
                )
            }
        }
        return combine(selectedFilter, expenses) { filter, source ->
            buildExpenseHistoryState(selectedFilter = filter, source = source)
        }
    }

    fun observeTransaction(transactionId: Long): Flow<TransactionEntity?> {
        return transactionDao.observeTransactionById(transactionId)
    }

    suspend fun seedIfEmpty() = withContext(Dispatchers.IO) {
        val hasTransactions = transactionDao.countTransactions() > 0
        if (!hasTransactions) {
            transactionDao.insertTransaction(
                TransactionEntity(
                    title = "Gaji Bulanan",
                    amount = 6_500_000,
                    type = TRANSACTION_TYPE_INCOME,
                    category = "Income",
                    note = "Auto seeded data",
                    paymentMethod = "Bank Transfer",
                    createdAtMillis = System.currentTimeMillis() - 86_400_000L
                )
            )
            RepositorySeedData.expenses.forEach { item ->
                transactionDao.insertTransaction(
                    TransactionEntity(
                        title = item.title,
                        amount = item.amount,
                        type = TRANSACTION_TYPE_EXPENSE,
                        category = item.categoryLabel,
                        note = "",
                        paymentMethod = "E-Wallet",
                        createdAtMillis = item.timestampMillis
                    )
                )
            }
        }

        if (budgetDao.countBudgetCategories() == 0L) {
            budgetDao.upsertBudgetCategories(
                RepositorySeedData.budgetCategories.map {
                    BudgetCategoryEntity(
                        id = it.id,
                        name = it.name,
                        allocated = it.allocated
                    )
                }
            )
        }

        if (walletDao.getWalletMeta() == null) {
            walletDao.upsertWalletMeta(WalletMetaEntity(initialBalance = DEFAULT_INITIAL_BALANCE))
        }
        if (budgetDao.getBudgetMeta() == null) {
            budgetDao.upsertBudgetMeta(BudgetMetaEntity(totalBudget = DEFAULT_MONTHLY_BUDGET))
        }
        if (profileDao.getProfile() == null) {
            profileDao.upsertProfile(
                ProfileEntity(
                    fullName = DEFAULT_PROFILE.fullName,
                    email = DEFAULT_PROFILE.email,
                    notificationsEnabled = DEFAULT_PROFILE.notificationsEnabled,
                    darkModeEnabled = DEFAULT_PROFILE.darkModeEnabled
                )
            )
        }
    }

    suspend fun addWalletTransaction(title: String, amount: Int, type: WalletTransactionType) {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isBlank() || amount <= 0) return
        val transactionType = if (type == WalletTransactionType.Income) {
            TRANSACTION_TYPE_INCOME
        } else {
            TRANSACTION_TYPE_EXPENSE
        }
        transactionDao.insertTransaction(
            TransactionEntity(
                title = normalizedTitle,
                amount = amount,
                type = transactionType,
                category = if (transactionType == TRANSACTION_TYPE_EXPENSE) "Lain-lain" else "Income",
                note = "",
                paymentMethod = "Cash",
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteWalletTransaction(transactionId: String) {
        transactionId.toLongOrNull()?.let { transactionDao.deleteTransactionById(it) }
    }

    suspend fun adjustInitialBalance(delta: Int) {
        val current = walletDao.getWalletMeta() ?: WalletMetaEntity(initialBalance = DEFAULT_INITIAL_BALANCE)
        walletDao.upsertWalletMeta(
            current.copy(initialBalance = (current.initialBalance + delta).coerceAtLeast(0))
        )
    }

    suspend fun saveProfile(profileUiState: ProfileUiState) {
        profileDao.upsertProfile(
            ProfileEntity(
                fullName = profileUiState.fullName.trim(),
                email = profileUiState.email.trim(),
                notificationsEnabled = profileUiState.notificationsEnabled,
                darkModeEnabled = profileUiState.darkModeEnabled
            )
        )
    }

    suspend fun updateMonthlyBudget(totalBudget: Int) {
        val safeBudget = totalBudget.coerceAtLeast(0)
        budgetDao.upsertBudgetMeta(BudgetMetaEntity(totalBudget = safeBudget))
    }

    suspend fun createDraftTransactionFromReceipt(): Long {
        return transactionDao.insertTransaction(
            TransactionEntity(
                title = "Receipt Draft",
                amount = 0,
                type = TRANSACTION_TYPE_EXPENSE,
                category = "Lain-lain",
                note = "",
                paymentMethod = "E-Wallet",
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateTransaction(transactionId: Long, newAmount: Int, newNote: String) {
        val current = transactionDao.getTransactionById(transactionId) ?: return
        transactionDao.updateTransaction(
            current.copy(
                amount = newAmount.coerceAtLeast(0),
                note = newNote.trim()
            )
        )
    }

    private fun observeMonthlyExpenseTotal(): Flow<Int> {
        val (startMillis, endMillis) = currentMonthRange()
        return transactionDao.observeTotalAmountByTypeBetween(
            type = TRANSACTION_TYPE_EXPENSE,
            startMillis = startMillis,
            endMillis = endMillis
        )
    }

    private fun observeCurrentMonthCategorySpendMap(): Flow<Map<String, Int>> {
        val (startMillis, endMillis) = currentMonthRange()
        return transactionDao.observeCategoryTotalsByTypeBetween(
            type = TRANSACTION_TYPE_EXPENSE,
            startMillis = startMillis,
            endMillis = endMillis
        ).map { rows ->
            rows.associate { row -> row.category to row.total }
        }
    }

    private fun TransactionEntity.toWalletTransactionType(): WalletTransactionType {
        return if (type == TRANSACTION_TYPE_INCOME) WalletTransactionType.Income else WalletTransactionType.Expense
    }

    private fun TransactionEntity.toExpenseCategory(): ExpenseCategory {
        return when (category.lowercase(Locale.ROOT)) {
            "food", "food & beverages", "makanan & minuman" -> ExpenseCategory.Food
            "transport", "transportasi" -> ExpenseCategory.Transport
            "shopping" -> ExpenseCategory.Shopping
            "health", "kesehatan" -> ExpenseCategory.Health
            else -> ExpenseCategory.Food
        }
    }

    private fun ProfileEntity.toProfileUiState(): ProfileUiState {
        return ProfileUiState(
            fullName = fullName,
            email = email,
            notificationsEnabled = notificationsEnabled,
            darkModeEnabled = darkModeEnabled,
            status = null
        )
    }

    private fun Int?.orZero(): Int = this ?: 0

    private fun currentMonthRange(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = Calendar.getInstance().apply {
            timeInMillis = start.timeInMillis
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }
        return start.timeInMillis to end.timeInMillis
    }

    companion object {
        private const val TRANSACTION_TYPE_INCOME = "INCOME"
        private const val TRANSACTION_TYPE_EXPENSE = "EXPENSE"
        private const val DEFAULT_INITIAL_BALANCE = 2_000_000
        private const val DEFAULT_MONTHLY_BUDGET = 7_000_000

        private val DEFAULT_PROFILE = ProfileUiState(
            fullName = "Andra Pratama",
            email = "andra@example.com",
            notificationsEnabled = true,
            darkModeEnabled = false
        )

        private val timeFormatter = SimpleDateFormat("HH:mm", Locale.forLanguageTag("id-ID"))
    }
}

data class DashboardSummary(
    val userName: String,
    val monthlySpent: Int,
    val monthlyBudget: Int,
    val budgetProgress: Float
)


