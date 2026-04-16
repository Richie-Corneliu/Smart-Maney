package com.kelompok4.smartmaney.ui.wallet

import org.junit.Assert.assertEquals
import org.junit.Test

class WalletStateTest {

    @Test
    fun expenseTransaction_reducesCurrentBalance() {
        val state = WalletUiState(
            initialBalance = 1_000_000,
            transactions = listOf(
                WalletTransaction(
                    id = "1",
                    title = "Lunch",
                    amount = 50_000,
                    type = WalletTransactionType.Expense,
                    createdAtMillis = 1L
                )
            )
        )

        assertEquals(950_000, state.currentBalance)
    }

    @Test
    fun incomeTransaction_increasesCurrentBalance() {
        val state = WalletUiState(
            initialBalance = 1_000_000,
            transactions = listOf(
                WalletTransaction(
                    id = "1",
                    title = "Salary",
                    amount = 500_000,
                    type = WalletTransactionType.Income,
                    createdAtMillis = 1L
                )
            )
        )

        assertEquals(1_500_000, state.currentBalance)
    }

    @Test
    fun mixedTransactions_computesNetBalance() {
        val state = WalletUiState(
            initialBalance = 1_000_000,
            transactions = listOf(
                WalletTransaction("1", "Lunch", 20_000, WalletTransactionType.Expense, 1L),
                WalletTransaction("2", "Bonus", 120_000, WalletTransactionType.Income, 2L)
            )
        )

        assertEquals(1_100_000, state.currentBalance)
    }

    @Test
    fun hugeExpense_keepsBalanceNonNegative() {
        val state = WalletUiState(
            initialBalance = 30_000,
            transactions = listOf(
                WalletTransaction("1", "Emergency", 100_000, WalletTransactionType.Expense, 1L)
            )
        )

        assertEquals(0, state.currentBalance)
    }

}

