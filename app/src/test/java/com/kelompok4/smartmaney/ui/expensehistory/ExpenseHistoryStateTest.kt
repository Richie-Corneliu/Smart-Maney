package com.kelompok4.smartmaney.ui.expensehistory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseHistoryStateTest {

    private val nowMillis = ExpenseHistoryDummyData.transactions.first().timestampMillis

    @Test
    fun dailyFilter_showsOnlyTodayTransactions() {
        val state = buildExpenseHistoryState(
            selectedFilter = ExpenseFilter.Daily,
            nowMillis = nowMillis
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
            nowMillis = nowMillis
        )

        assertEquals(3, state.groups.size)
        assertEquals(5, state.groups.sumOf { it.items.size })
        assertEquals(215_000, state.groups.sumOf { it.totalAmount })
    }

    @Test
    fun monthlyFilter_matchesAllMayTransactions() {
        val state = reduceExpenseHistoryState(
            action = ExpenseHistoryAction.SelectFilter(ExpenseFilter.Monthly),
            nowMillis = nowMillis
        )

        assertEquals(ExpenseFilter.Monthly, state.selectedFilter)
        assertEquals(3, state.groups.size)
    }
}

