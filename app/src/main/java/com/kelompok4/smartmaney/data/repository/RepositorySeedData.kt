package com.kelompok4.smartmaney.data.repository

data class SeedExpense(
    val title: String,
    val categoryLabel: String,
    val amount: Int,
    val timestampMillis: Long
)

data class SeedBudgetCategory(
    val id: String,
    val name: String,
    val allocated: Int
)

object RepositorySeedData {
    private fun timestamp(day: Int, hour: Int, minute: Int): Long {
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, 2024)
            set(java.util.Calendar.MONTH, java.util.Calendar.MAY)
            set(java.util.Calendar.DAY_OF_MONTH, day)
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    val expenses = listOf(
        SeedExpense("Makan Siang", "Food & Beverages", 45_000, timestamp(day = 24, hour = 12, minute = 45)),
        SeedExpense("Gojek Ke Kantor", "Transport", 20_000, timestamp(day = 24, hour = 8, minute = 15)),
        SeedExpense("Sepatu Baru", "Shopping", 100_000, timestamp(day = 24, hour = 10, minute = 30)),
        SeedExpense("Vitamin C", "Health", 35_000, timestamp(day = 23, hour = 16, minute = 20)),
        SeedExpense("Kopi Pagi", "Food & Beverages", 15_000, timestamp(day = 22, hour = 9, minute = 0))
    )

    val budgetCategories = listOf(
        SeedBudgetCategory("1", "Makanan & Minuman", 2500000),
        SeedBudgetCategory("2", "Transportasi", 1500000),
        SeedBudgetCategory("3", "Tempat Tinggal", 1500000),
        SeedBudgetCategory("4", "Hiburan", 1000000),
        SeedBudgetCategory("5", "Lain-lain", 500000)
    )
}

