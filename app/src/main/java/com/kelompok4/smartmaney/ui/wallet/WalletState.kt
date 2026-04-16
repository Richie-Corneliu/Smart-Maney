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


