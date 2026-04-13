package com.kelompok4.smartmaney.ui.wallet

enum class WalletTransactionType {
    Income,
    Expense
}

data class WalletTransaction(
    val id: String,
    val title: String,
    val amount: Int,
    val type: WalletTransactionType,
    val createdAtMillis: Long
)

data class WalletUiState(
    val initialBalance: Int = 0,
    val transactions: List<WalletTransaction> = emptyList()
) {
    val currentBalance: Int
        get() {
            val income = transactions
                .filter { it.type == WalletTransactionType.Income }
                .sumOf { it.amount }
            val expense = transactions
                .filter { it.type == WalletTransactionType.Expense }
                .sumOf { it.amount }
            return (initialBalance + income - expense).coerceAtLeast(0)
        }
}

sealed interface WalletAction {
    data class AddTransaction(
        val title: String,
        val amount: Int,
        val type: WalletTransactionType,
        val createdAtMillis: Long = System.currentTimeMillis()
    ) : WalletAction

    data class RemoveTransaction(val transactionId: String) : WalletAction

    data class AdjustBaseBalance(val delta: Int) : WalletAction
}

fun reduceWalletState(current: WalletUiState, action: WalletAction): WalletUiState {
    return when (action) {
        is WalletAction.AddTransaction -> {
            val normalizedTitle = action.title.trim()
            if (normalizedTitle.isBlank() || action.amount <= 0) {
                current
            } else {
                current.copy(
                    transactions = listOf(
                        WalletTransaction(
                            id = "${action.createdAtMillis}-${current.transactions.size}",
                            title = normalizedTitle,
                            amount = action.amount,
                            type = action.type,
                            createdAtMillis = action.createdAtMillis
                        )
                    ) + current.transactions
                )
            }
        }

        is WalletAction.RemoveTransaction -> {
            current.copy(
                transactions = current.transactions.filterNot { it.id == action.transactionId }
            )
        }

        is WalletAction.AdjustBaseBalance -> {
            current.copy(initialBalance = (current.initialBalance + action.delta).coerceAtLeast(0))
        }
    }
}

