package com.kelompok4.smartmaney.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.smartmaney.data.repository.SmartManeyRepository
import com.kelompok4.smartmaney.ui.suggestion.CategorySpendData
import com.kelompok4.smartmaney.ui.suggestion.SuggestionUiState
import com.kelompok4.smartmaney.ui.suggestion.generateSuggestionsFromSpending
import com.kelompok4.smartmaney.ui.suggestion.normalizeCategoryName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

class SuggestionViewModel(
    private val repository: SmartManeyRepository
) : ViewModel() {

    val uiState: StateFlow<SuggestionUiState> = combine(
        getCurrentMonthCategorySpending(),
        getPreviousMonthCategorySpending()
    ) { currentMonthMap, previousMonthMap ->
        val totalCurrent = currentMonthMap.values.sum()
        val totalPrevious = previousMonthMap.values.sum()

        // Find top spending category
        val topSpendingCategory = currentMonthMap.maxByOrNull { it.value }
        val topCategory = topSpendingCategory?.key ?: "Food"
        val topAmount = topSpendingCategory?.value ?: 0

        // Calculate max amount for chart scaling
        val maxAmount = currentMonthMap.values.maxOrNull()?.toFloat() ?: 1f

        // Create chart data
        val chartData = currentMonthMap.entries.map { (category, amount) ->
            CategorySpendData(
                label = normalizeCategoryName(category),
                percentage = if (maxAmount > 0) {
                    (amount.toFloat() / maxAmount).coerceIn(0f, 1f)
                } else {
                    0f
                }
            )
        }.distinctBy { it.label } // Remove duplicates

        // Generate suggestions
        val suggestions = generateSuggestionsFromSpending(currentMonthMap, previousMonthMap)

        SuggestionUiState(
            currentMonthSpendingByCategory = currentMonthMap,
            previousMonthSpendingByCategory = previousMonthMap,
            totalCurrentMonthSpending = totalCurrent,
            totalPreviousMonthSpending = totalPrevious,
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

    private fun getCurrentMonthCategorySpending(): Flow<Map<String, Int>> {
        val (startMillis, endMillis) = currentMonthRange()
        return repository.observeCategorySpendingBetween(startMillis, endMillis)
    }

    private fun getPreviousMonthCategorySpending(): Flow<Map<String, Int>> {
        val (startMillis, endMillis) = previousMonthRange()
        return repository.observeCategorySpendingBetween(startMillis, endMillis)
    }

    private fun currentMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startMillis = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endMillis = calendar.timeInMillis

        return Pair(startMillis, endMillis)
    }

    private fun previousMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startMillis = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endMillis = calendar.timeInMillis

        return Pair(startMillis, endMillis)
    }
}


