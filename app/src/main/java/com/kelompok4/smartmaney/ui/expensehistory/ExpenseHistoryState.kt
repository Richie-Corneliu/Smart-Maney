package com.kelompok4.smartmaney.ui.expensehistory

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ExpenseFilter {
    Daily,
    Weekly,
    Monthly
}

enum class ExpenseCategory {
    Food,
    Transport,
    Shopping,
    Health
}

data class ExpenseTransaction(
    val id: String,
    val title: String,
    val categoryLabel: String,
    val amount: Int,
    val timestampMillis: Long,
    val timeLabel: String,
    val category: ExpenseCategory
)

data class ExpenseDayGroup(
    val headerLabel: String,
    val totalAmount: Int,
    val items: List<ExpenseTransaction>
)

data class ExpenseHistoryUiState(
    val selectedFilter: ExpenseFilter = ExpenseFilter.Daily,
    val groups: List<ExpenseDayGroup> = emptyList()
)

fun buildExpenseHistoryState(
    selectedFilter: ExpenseFilter,
    nowMillis: Long = System.currentTimeMillis(),
    source: List<ExpenseTransaction> = emptyList()
): ExpenseHistoryUiState {
    val filtered = source
        .filter { transaction -> matchesFilter(transaction.timestampMillis, selectedFilter, nowMillis) }
        .sortedByDescending { it.timestampMillis }

    val dayGroups = filtered
        .groupBy { startOfDayMillis(it.timestampMillis) }
        .toList()
        .sortedByDescending { it.first }
        .map { (dayStartMillis, items) ->
            ExpenseDayGroup(
                headerLabel = formatDayHeader(dayStartMillis, nowMillis),
                totalAmount = items.sumOf { it.amount },
                items = items.sortedByDescending { it.timestampMillis }
            )
        }

    return ExpenseHistoryUiState(
        selectedFilter = selectedFilter,
        groups = dayGroups
    )
}

fun formatRupiah(amount: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    return "Rp ${formatter.format(amount)}"
}

private fun matchesFilter(timestampMillis: Long, filter: ExpenseFilter, nowMillis: Long): Boolean {
    val target = Calendar.getInstance().apply { timeInMillis = timestampMillis }
    val now = Calendar.getInstance().apply { timeInMillis = nowMillis }

    return when (filter) {
        ExpenseFilter.Daily -> isSameDay(target, now)
        ExpenseFilter.Weekly -> isSameWeek(target, now)
        ExpenseFilter.Monthly -> {
            target.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                target.get(Calendar.MONTH) == now.get(Calendar.MONTH)
        }
    }
}

private fun isSameDay(first: Calendar, second: Calendar): Boolean {
    return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
        first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
}

private fun isSameWeek(first: Calendar, second: Calendar): Boolean {
    return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
        first.get(Calendar.WEEK_OF_YEAR) == second.get(Calendar.WEEK_OF_YEAR)
}

private fun startOfDayMillis(timestampMillis: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timestampMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun formatDayHeader(dayMillis: Long, nowMillis: Long): String {
    val locale = Locale.forLanguageTag("id-ID")
    val day = Calendar.getInstance().apply { timeInMillis = dayMillis }
    val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
    val yesterday = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        add(Calendar.DAY_OF_YEAR, -1)
    }

    val dayMonthFormatter = SimpleDateFormat("d MMM", locale)
    val fullDateFormatter = SimpleDateFormat("d MMM yyyy", locale)

    return when {
        isSameDay(day, now) -> "HARI INI, ${dayMonthFormatter.format(Date(dayMillis)).uppercase(locale)}"
        isSameDay(day, yesterday) -> "KEMARIN, ${dayMonthFormatter.format(Date(dayMillis)).uppercase(locale)}"
        else -> fullDateFormatter.format(Date(dayMillis)).uppercase(locale)
    }
}

