package com.kelompok4.smartmaney.data.repository

import com.kelompok4.smartmaney.data.local.SmartManeyDatabase
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
        observeCurrentMonthCategorySpendMap()
    ) { profile, budgetMeta, spentByCategory ->
        val normalizedSpentByCategory = spentByCategory.entries
            .groupBy(keySelector = { normalizeBudgetCategoryName(it.key) }, valueTransform = { it.value })
            .mapValues { (_, values) -> values.sum() }

        val monthlyExpense = normalizedSpentByCategory.values.sum()
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
            budgetProgress = progress,
            spendingByCategory = normalizedSpentByCategory
        )
    }

    val budgetPlanningUiState: Flow<BudgetPlanningUiState> = combine(
        budgetDao.observeBudgetMeta(),
        budgetDao.observeBudgetCategories(),
        observeCurrentMonthCategorySpendMap()
    ) { budgetMeta, categories, spentByCategory ->
        val normalizedSpentByCategory = spentByCategory.entries
            .groupBy(keySelector = { normalizeBudgetCategoryName(it.key) }, valueTransform = { it.value })
            .mapValues { (_, values) -> values.sum() }

        val totalBudget = budgetMeta?.totalBudget ?: DEFAULT_MONTHLY_BUDGET
        val categoryBudgets = categories.map { entity ->
            BudgetCategoryItem(
                id = entity.id,
                name = entity.name,
                allocated = entity.allocated,
                spent = normalizedSpentByCategory[normalizeBudgetCategoryName(entity.name)].orZero()
            )
        }
        val totalSpent = normalizedSpentByCategory.values.sum()
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
                    categoryLabel = normalizeDisplayCategoryLabel(row.category),
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
                category = if (transactionType == TRANSACTION_TYPE_EXPENSE) {
                    DEFAULT_EXPENSE_BUDGET_CATEGORY
                } else {
                    DEFAULT_INCOME_CATEGORY
                },
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

    suspend fun syncProfileFromAuthenticatedUser(
        authDisplayName: String?,
        authEmail: String?
    ) {
        val current = profileDao.getProfile()
        val currentState = current?.toProfileUiState() ?: DEFAULT_PROFILE
        val mergedName = authDisplayName?.trim().takeUnless { it.isNullOrBlank() } ?: currentState.fullName
        val mergedEmail = authEmail?.trim().takeUnless { it.isNullOrBlank() } ?: currentState.email
        profileDao.upsertProfile(
            ProfileEntity(
                fullName = mergedName,
                email = mergedEmail,
                notificationsEnabled = currentState.notificationsEnabled,
                darkModeEnabled = currentState.darkModeEnabled
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
                category = DEFAULT_EXPENSE_BUDGET_CATEGORY,
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
        return when (category.trim().lowercase(Locale.ROOT)) {
            "food", "food & beverages", "makanan", "makanan & minuman", "kuliner" -> ExpenseCategory.Food
            "transport", "transportation", "transportasi", "commute" -> ExpenseCategory.Transport
            "health", "healthcare", "kesehatan", "medical", "vitamin", "obat" -> ExpenseCategory.Health
            else -> ExpenseCategory.Shopping
        }
    }

    private fun normalizeBudgetCategoryName(rawCategory: String): String {
        return when (rawCategory.trim().lowercase(Locale.ROOT)) {
            "food", "food & beverages", "makanan", "makanan & minuman", "kuliner" -> "Makanan & Minuman"
            "transport", "transportation", "transportasi", "commute" -> "Transportasi"
            "shopping", "belanja", "entertainment", "hiburan" -> "Hiburan"
            "rent", "housing", "tempat tinggal", "accommodation" -> "Tempat Tinggal"
            "lain-lain", "other", "others", "misc", "miscellaneous" -> "Lain-lain"
            "income", "pemasukan" -> DEFAULT_INCOME_CATEGORY
            else -> "Lain-lain"
        }
    }

    private fun normalizeDisplayCategoryLabel(rawCategory: String): String {
        return normalizeBudgetCategoryName(rawCategory)
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
        private const val DEFAULT_INCOME_CATEGORY = "Income"
        private const val DEFAULT_EXPENSE_BUDGET_CATEGORY = "Lain-lain"
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
    val budgetProgress: Float,
    val spendingByCategory: Map<String, Int>
)


