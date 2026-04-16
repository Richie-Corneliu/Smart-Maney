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
    val categoryBudgets: List<BudgetCategoryItem> = emptyList()
) {
    val overallProgress: Float get() = if (totalBudget > 0) totalSpent.toFloat() / totalBudget.toFloat() else 0f
}
