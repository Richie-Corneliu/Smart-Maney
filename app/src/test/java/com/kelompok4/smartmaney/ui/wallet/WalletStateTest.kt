package com.kelompok4.smartmaney.ui.wallet

import org.junit.Assert.assertEquals
import org.junit.Test

class WalletStateTest {

    @Test
    fun addExpense_reducesCurrentBalance() {
        val initial = WalletUiState(initialBalance = 1_000_000, transactions = emptyList())

        val updated = reduceWalletState(
            initial,
            WalletAction.AddTransaction(
                title = "Lunch",
                amount = 50_000,
                type = WalletTransactionType.Expense,
                createdAtMillis = 1L
            )
        )

        assertEquals(950_000, updated.currentBalance)
        assertEquals(1, updated.transactions.size)
    }

    @Test
    fun addIncome_increasesCurrentBalance() {
        val initial = WalletUiState(initialBalance = 1_000_000, transactions = emptyList())

        val updated = reduceWalletState(
            initial,
            WalletAction.AddTransaction(
                title = "Salary",
                amount = 500_000,
                type = WalletTransactionType.Income,
                createdAtMillis = 1L
            )
        )

        assertEquals(1_500_000, updated.currentBalance)
    }

    @Test
    fun removeTransaction_updatesBalance() {
        val initial = WalletUiState(
            initialBalance = 1_000_000,
            transactions = listOf(
                WalletTransaction("1", "Lunch", 20_000, WalletTransactionType.Expense, 1L)
            )
        )

        val updated = reduceWalletState(initial, WalletAction.RemoveTransaction("1"))

        assertEquals(1_000_000, updated.currentBalance)
        assertEquals(0, updated.transactions.size)
    }

    @Test
    fun addTransaction_withBlankTitleOrInvalidAmount_keepsState() {
        val initial = WalletUiState(initialBalance = 1_000_000, transactions = emptyList())

        val blankTitle = reduceWalletState(
            initial,
            WalletAction.AddTransaction(
                title = " ",
                amount = 20_000,
                type = WalletTransactionType.Expense,
                createdAtMillis = 1L
            )
        )
        val invalidAmount = reduceWalletState(
            initial,
            WalletAction.AddTransaction(
                title = "Lunch",
                amount = 0,
                type = WalletTransactionType.Expense,
                createdAtMillis = 1L
            )
        )

        assertEquals(initial, blankTitle)
        assertEquals(initial, invalidAmount)
    }

    @Test
    fun adjustBaseBalance_doesNotGoBelowZero() {
        val initial = WalletUiState(initialBalance = 30_000, transactions = emptyList())

        val updated = reduceWalletState(initial, WalletAction.AdjustBaseBalance(-50_000))

        assertEquals(0, updated.initialBalance)
        assertEquals(0, updated.currentBalance)
    }

}

