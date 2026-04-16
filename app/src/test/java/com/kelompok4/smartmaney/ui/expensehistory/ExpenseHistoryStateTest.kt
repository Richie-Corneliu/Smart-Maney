package com.kelompok4.smartmaney.ui.expensehistory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseHistoryStateTest {

    private val nowMillis = timestamp(day = 24, hour = 12, minute = 45)
    private val source = listOf(
        ExpenseTransaction(
            id = "1",
            title = "Makan Siang",
            categoryLabel = "Food & Beverages",
            amount = 45_000,
            timestampMillis = timestamp(day = 24, hour = 12, minute = 45),
            timeLabel = "12:45",
            category = ExpenseCategory.Food
        ),
        ExpenseTransaction(
            id = "2",
            title = "Gojek Ke Kantor",
            categoryLabel = "Transport",
            amount = 20_000,
            timestampMillis = timestamp(day = 24, hour = 8, minute = 15),
            timeLabel = "08:15",
            category = ExpenseCategory.Transport
        ),
        ExpenseTransaction(
            id = "3",
            title = "Sepatu Baru",
            categoryLabel = "Shopping",
            amount = 100_000,
            timestampMillis = timestamp(day = 24, hour = 10, minute = 30),
            timeLabel = "10:30",
            category = ExpenseCategory.Shopping
        ),
        ExpenseTransaction(
            id = "4",
            title = "Vitamin C",
            categoryLabel = "Health",
            amount = 35_000,
            timestampMillis = timestamp(day = 23, hour = 16, minute = 20),
            timeLabel = "16:20",
            category = ExpenseCategory.Health
        ),
        ExpenseTransaction(
            id = "5",
            title = "Kopi Pagi",
            categoryLabel = "Food & Beverages",
            amount = 15_000,
            timestampMillis = timestamp(day = 22, hour = 9, minute = 0),
            timeLabel = "09:00",
            category = ExpenseCategory.Food
        )
    )

    @Test
    fun dailyFilter_showsOnlyTodayTransactions() {
        val state = buildExpenseHistoryState(
            selectedFilter = ExpenseFilter.Daily,
            nowMillis = nowMillis,
            source = source
        )

        assertEquals(1, state.groups.size)
        assertEquals(3, state.groups.first().items.size)
        assertEquals(165_000, state.groups.first().totalAmount)
        assertTrue(state.groups.first().headerLabel.startsWith("HARI INI"))
    }

    @Test
    fun weeklyFilter_includesThreeDaysOfData() {
        val state = buildExpenseHistoryState(
            selectedFilter = ExpenseFilter.Weekly,
            nowMillis = nowMillis,
            source = source
        )

        assertEquals(3, state.groups.size)
        assertEquals(5, state.groups.sumOf { it.items.size })
        assertEquals(215_000, state.groups.sumOf { it.totalAmount })
    }

    @Test
    fun monthlyFilter_matchesAllMayTransactions() {
        val state = buildExpenseHistoryState(
            selectedFilter = ExpenseFilter.Monthly,
            nowMillis = nowMillis,
            source = source
        )

        assertEquals(ExpenseFilter.Monthly, state.selectedFilter)
        assertEquals(3, state.groups.size)
    }

    private fun timestamp(day: Int, hour: Int, minute: Int): Long {
        return java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, 2024)
            set(java.util.Calendar.MONTH, java.util.Calendar.MAY)
            set(java.util.Calendar.DAY_OF_MONTH, day)
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}

