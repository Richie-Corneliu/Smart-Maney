package com.kelompok4.smartmaney.ui.suggestion

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.ui.graphics.vector.ImageVector

data class CategorySpendData(
    val label: String,
    val percentage: Float // 0.0 to 1.0
)

data class SuggestionItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

data class SuggestionUiState(
    val currentMonthSpendingByCategory: Map<String, Int> = emptyMap(),
    val previousMonthSpendingByCategory: Map<String, Int> = emptyMap(),
    val totalCurrentMonthSpending: Int = 0,
    val totalPreviousMonthSpending: Int = 0,
    val topSpendingCategory: String = "Food",
    val topSpendingAmount: Int = 0,
    val categoryChartData: List<CategorySpendData> = emptyList(),
    val suggestions: List<SuggestionItem> = emptyList(),
    val showLoading: Boolean = true
)

// Helper function to generate suggestions based on spending patterns
fun generateSuggestionsFromSpending(
    currentSpending: Map<String, Int>,
    previousSpending: Map<String, Int>
): List<SuggestionItem> {
    val suggestions = mutableListOf<SuggestionItem>()

    // Check if transport spending increased
    val currentTransport = currentSpending.filterKeys {
        it.lowercase().contains("transport") || it.lowercase().contains("transp")
    }.values.sum()
    val previousTransport = previousSpending.filterKeys {
        it.lowercase().contains("transport") || it.lowercase().contains("transp")
    }.values.sum()

    if (currentTransport > previousTransport && currentTransport > 0) {
        suggestions.add(
            SuggestionItem(
                icon = Icons.Default.DirectionsCar,
                title = "Reduce transport spending",
                subtitle = "Optimize daily commute costs"
            )
        )
    }

    // Check subscription/other category
    val currentOther = currentSpending.filterKeys {
        it.lowercase().contains("subscription") || it.lowercase().contains("other") || it.lowercase().contains("misc")
    }.values.sum()

    if (currentOther > 0) {
        suggestions.add(
            SuggestionItem(
                icon = Icons.Default.CreditCard,
                title = "Manage subscriptions",
                subtitle = "Cancel 2 unused services"
            )
        )
    }

    // Check health/wellness category
    val currentHealth = currentSpending.filterKeys {
        it.lowercase().contains("health") || it.lowercase().contains("medicine")
    }.values.sum()

    if (currentHealth > 0 && currentHealth <= previousSpending.filterKeys {
        it.lowercase().contains("health") || it.lowercase().contains("medicine")
    }.values.sum()) {
        suggestions.add(
            SuggestionItem(
                icon = Icons.Default.VerifiedUser,
                title = "Maintain current budget",
                subtitle = "Health spending is on track"
            )
        )
    }

    // Default suggestions if none generated
    if (suggestions.isEmpty()) {
        suggestions.addAll(
            listOf(
                SuggestionItem(
                    icon = Icons.Default.DirectionsCar,
                    title = "Reduce transport spending",
                    subtitle = "Optimize daily commute costs"
                ),
                SuggestionItem(
                    icon = Icons.Default.CreditCard,
                    title = "Manage subscriptions",
                    subtitle = "Cancel 2 unused services"
                ),
                SuggestionItem(
                    icon = Icons.Default.VerifiedUser,
                    title = "Maintain current budget",
                    subtitle = "Health spending is on track"
                )
            )
        )
    }

    return suggestions
}

// Helper to normalize category names
fun normalizeCategoryName(category: String): String {
    return when {
        category.lowercase().contains("food") || category.lowercase().contains("dining") ||
        category.lowercase().contains("groceries") || category.lowercase().contains("eat") -> "Food"
        category.lowercase().contains("bill") -> "Bills"
        category.lowercase().contains("transport") || category.lowercase().contains("transp") -> "Transp"
        category.lowercase().contains("shop") -> "Shop"
        else -> "Other"
    }
}

