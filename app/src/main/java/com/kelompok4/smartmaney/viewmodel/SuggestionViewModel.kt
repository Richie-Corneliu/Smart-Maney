package com.kelompok4.smartmaney.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.smartmaney.data.repository.SmartManeyRepository
import com.kelompok4.smartmaney.ui.suggestion.CategoryBudgetStatus
import com.kelompok4.smartmaney.ui.suggestion.CategorySpendData
import com.kelompok4.smartmaney.ui.suggestion.SuggestionUiState
import com.kelompok4.smartmaney.ui.suggestion.dayOfMonthToday
import com.kelompok4.smartmaney.ui.suggestion.daysInCurrentMonth
import com.kelompok4.smartmaney.ui.suggestion.generateSmartSuggestions
import com.kelompok4.smartmaney.ui.suggestion.shortCategoryLabel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

class SuggestionViewModel(
    private val repository: SmartManeyRepository
) : ViewModel() {

    private val currentRange = currentMonthRange()
    private val previousRange = previousMonthRange()

    private val currentSpending: Flow<Map<String, Int>> =
        repository.observeCategorySpendingBetween(currentRange.first, currentRange.second)

    private val previousSpending: Flow<Map<String, Int>> =
        repository.observeCategorySpendingBetween(previousRange.first, previousRange.second)

    private val currentIncome: Flow<Int> =
        repository.observeIncomeBetween(currentRange.first, currentRange.second)

    private val budgetMeta = repository.observeBudgetMeta()
    private val budgetCategories = repository.observeBudgetCategories()

    val uiState: StateFlow<SuggestionUiState> = combine(
        currentSpending,
        previousSpending,
        currentIncome,
        budgetMeta,
        budgetCategories
    ) { rawCurrent, rawPrevious, income, meta, categories ->
        // Collapse to canonical categories.
        val canonicalCurrent = rawCurrent.entries
            .groupBy({ repository.normalizeCategoryToCanonical(it.key) }, { it.value })
            .mapValues { (_, v) -> v.sum() }
            .filterKeys { it != "Income" }

        val canonicalPrevious = rawPrevious.entries
            .groupBy({ repository.normalizeCategoryToCanonical(it.key) }, { it.value })
            .mapValues { (_, v) -> v.sum() }
            .filterKeys { it != "Income" }

        val totalCurrent = canonicalCurrent.values.sum()
        val totalPrevious = canonicalPrevious.values.sum()
        val monthlyBudget = meta?.totalBudget ?: 0

        // Build budget category status (normalize the budget category names too).
        val budgetStatuses: List<CategoryBudgetStatus> = categories.map { entity ->
            val canonical = repository.normalizeCategoryToCanonical(entity.name)
            val spent = canonicalCurrent[canonical] ?: 0
            CategoryBudgetStatus(
                category = canonical,
                allocated = entity.allocated,
                spent = spent,
                percentUsed = if (entity.allocated > 0) spent.toFloat() / entity.allocated else 0f
            )
        }

        // Top spending category
        val topEntry = canonicalCurrent.maxByOrNull { it.value }
        val topCategory = topEntry?.key ?: "—"
        val topAmount = topEntry?.value ?: 0

        // Chart data (sorted desc, top 5)
        val maxAmount = canonicalCurrent.values.maxOrNull()?.toFloat() ?: 1f
        val chartData = canonicalCurrent.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { (cat, amount) ->
                CategorySpendData(
                    label = shortCategoryLabel(cat),
                    amount = amount,
                    percentage = if (maxAmount > 0) (amount / maxAmount).coerceIn(0.05f, 1f) else 0f
                )
            }

        // Projection: linearly extrapolate from elapsed days.
        val today = dayOfMonthToday()
        val totalDays = daysInCurrentMonth()
        val projected = if (today > 0) {
            ((totalCurrent.toLong() * totalDays) / today).toInt()
        } else {
            totalCurrent
        }

        val savingsRate = if (income > 0) {
            ((income - totalCurrent).toFloat() / income).coerceIn(-2f, 1f)
        } else 0f

        val budgetUtilization = if (monthlyBudget > 0) {
            (totalCurrent.toFloat() / monthlyBudget).coerceIn(0f, 2f)
        } else 0f

        val suggestions = generateSmartSuggestions(
            canonicalCurrent = canonicalCurrent,
            canonicalPrevious = canonicalPrevious,
            totalIncome = income,
            monthlyBudget = monthlyBudget,
            budgetStatuses = budgetStatuses,
            projectedMonthlySpending = projected,
            daysElapsedInMonth = today,
            daysInMonth = totalDays
        )

        SuggestionUiState(
            currentMonthSpendingByCategory = canonicalCurrent,
            previousMonthSpendingByCategory = canonicalPrevious,
            totalCurrentMonthSpending = totalCurrent,
            totalPreviousMonthSpending = totalPrevious,
            totalCurrentMonthIncome = income,
            monthlyBudget = monthlyBudget,
            budgetCategories = budgetStatuses,
            budgetUtilization = budgetUtilization,
            projectedMonthlySpending = projected,
            daysElapsedInMonth = today,
            daysInMonth = totalDays,
            savingsRate = savingsRate,
            topSpendingCategory = topCategory,
            topSpendingAmount = topAmount,
            categoryChartData = chartData,
            suggestions = suggestions,
            showLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SuggestionUiState()
    )

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

    private fun previousMonthRange(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            add(Calendar.MONTH, -1)
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
}
