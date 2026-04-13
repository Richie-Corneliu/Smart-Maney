package com.kelompok4.smartmaney.ui.monthlyreport

import androidx.compose.ui.graphics.Color
import com.kelompok4.smartmaney.ui.theme.SmCategoryFood
import com.kelompok4.smartmaney.ui.theme.SmCategoryRent
import com.kelompok4.smartmaney.ui.theme.SmCategoryTransport

// 1. Model Data
data class TransactionItem(
    val id: String,
    val title: String,
    val category: String,
    val amount: Int,
    val date: String,
    val iconColor: Color
)

data class CategoryExpense(
    val category: String,
    val amount: Int,
    val percentage: Float,
    val color: Color
)

// 2. UI State
data class MonthlyReportUiState(
    val month: String = "Oktober 2023",
    val totalExpense: Int = 4500000,
    val topCategories: List<CategoryExpense> = emptyList(),
    val recentTransactions: List<TransactionItem> = emptyList()
)

// 3. Dummy Data Provider
object MonthlyReportDummyData {
    val dummyCategories = listOf(
        CategoryExpense("Makanan & Minuman", 2025000, 0.45f, SmCategoryFood),
        CategoryExpense("Transportasi", 1350000, 0.30f, SmCategoryTransport),
        CategoryExpense("Tempat Tinggal", 1125000, 0.25f, SmCategoryRent)
    )

    val dummyTransactions = listOf(
        TransactionItem("1", "Makan Siang Bareng", "Makanan & Minuman", 150000, "24 Okt 2023", SmCategoryFood),
        TransactionItem("2", "Isi Bensin Motor", "Transportasi", 50000, "23 Okt 2023", SmCategoryTransport),
        TransactionItem("3", "Bayar Kos", "Tempat Tinggal", 1125000, "01 Okt 2023", SmCategoryRent),
        TransactionItem("4", "Kopi Pagi", "Makanan & Minuman", 35000, "22 Okt 2023", SmCategoryFood),
        TransactionItem("5", "Grab ke Kampus", "Transportasi", 25000, "22 Okt 2023", SmCategoryTransport)
    )
}