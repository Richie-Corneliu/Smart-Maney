package com.kelompok4.smartmaney.ui.budgetplanning

// 1. Model Data
data class BudgetCategoryItem(
    val id: String,
    val name: String,
    val allocated: Int,
    val spent: Int
) {
    val remaining: Int get() = allocated - spent
    val progress: Float get() = if (allocated > 0) spent.toFloat() / allocated.toFloat() else 0f
}

// 2. UI State
data class BudgetPlanningUiState(
    val totalBudget: Int = 7000000,
    val totalSpent: Int = 4500000,
    val isEditingTotalBudget: Boolean = false,
    val categoryBudgets: List<BudgetCategoryItem> = emptyList()
) {
    val overallProgress: Float get() = if (totalBudget > 0) totalSpent.toFloat() / totalBudget.toFloat() else 0f
}

// 3. Dummy Data
object BudgetDummyData {
    val dummyBudgets = listOf(
        BudgetCategoryItem("1", "Makanan & Minuman", 2500000, 2025000),
        BudgetCategoryItem("2", "Transportasi", 1500000, 1350000),
        BudgetCategoryItem("3", "Tempat Tinggal", 1500000, 1125000),
        BudgetCategoryItem("4", "Hiburan", 1000000, 0),
        BudgetCategoryItem("5", "Lain-lain", 500000, 0)
    )
}