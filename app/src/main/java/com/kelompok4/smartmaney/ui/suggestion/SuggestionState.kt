package com.kelompok4.smartmaney.ui.suggestion

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Calendar

data class CategorySpendData(
    val label: String,
    val amount: Int,
    val percentage: Float // 0.0 to 1.0 (relative to max in chart)
)

data class CategoryBudgetStatus(
    val category: String,
    val allocated: Int,
    val spent: Int,
    val percentUsed: Float
)

enum class SuggestionSeverity { Positive, Info, Warning, Critical }

data class SuggestionItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val severity: SuggestionSeverity = SuggestionSeverity.Info
)

data class SuggestionUiState(
    val currentMonthSpendingByCategory: Map<String, Int> = emptyMap(),
    val previousMonthSpendingByCategory: Map<String, Int> = emptyMap(),
    val totalCurrentMonthSpending: Int = 0,
    val totalPreviousMonthSpending: Int = 0,
    val totalCurrentMonthIncome: Int = 0,
    val monthlyBudget: Int = 0,
    val budgetCategories: List<CategoryBudgetStatus> = emptyList(),
    val budgetUtilization: Float = 0f, // spent / budget
    val projectedMonthlySpending: Int = 0,
    val daysElapsedInMonth: Int = 1,
    val daysInMonth: Int = 30,
    val savingsRate: Float = 0f, // (income - expense) / income
    val topSpendingCategory: String = "—",
    val topSpendingAmount: Int = 0,
    val categoryChartData: List<CategorySpendData> = emptyList(),
    val suggestions: List<SuggestionItem> = emptyList(),
    val showLoading: Boolean = true
)

/**
 * Normalize a raw transaction category string to one of the canonical
 * Indonesian budget categories used across the app:
 * "Makanan & Minuman", "Transportasi", "Hiburan", "Tempat Tinggal",
 * "Lain-lain", "Income".
 */
fun normalizeCategoryName(category: String): String {
    return when (category.trim().lowercase()) {
        "food", "food & beverages", "makanan", "makanan & minuman", "kuliner",
        "grocery", "groceries", "restaurant", "cafe", "warung", "belanja makanan" -> "Makanan & Minuman"
        "transport", "transportation", "transportasi", "commute",
        "fuel", "parking", "toll", "ride-hailing", "ojek", "taxi", "bus" -> "Transportasi"
        "shopping", "belanja", "entertainment", "hiburan",
        "subscription", "game", "cinema", "movie" -> "Hiburan"
        "rent", "housing", "tempat tinggal", "accommodation",
        "utilities", "electricity", "water", "internet", "listrik", "air" -> "Tempat Tinggal"
        "income", "pemasukan" -> "Income"
        else -> "Lain-lain"
    }
}

/** Short label used inside the bar chart (limited horizontal space). */
fun shortCategoryLabel(canonical: String): String = when (canonical) {
    "Makanan & Minuman" -> "Makan"
    "Transportasi" -> "Transp"
    "Hiburan" -> "Hiburan"
    "Tempat Tinggal" -> "Tempat"
    "Lain-lain" -> "Lainnya"
    "Income" -> "Income"
    else -> canonical.take(7)
}

fun iconForCategory(canonical: String): ImageVector = when (canonical) {
    "Makanan & Minuman" -> Icons.Default.Fastfood
    "Transportasi" -> Icons.Default.DirectionsCar
    "Hiburan" -> Icons.Default.Movie
    "Tempat Tinggal" -> Icons.Default.Home
    else -> Icons.Default.CreditCard
}

/**
 * Generate smart, data-driven suggestions to help the user improve their
 * financial condition. Ordered by severity (most actionable first).
 */
fun generateSmartSuggestions(
    canonicalCurrent: Map<String, Int>,
    canonicalPrevious: Map<String, Int>,
    totalIncome: Int,
    monthlyBudget: Int,
    budgetStatuses: List<CategoryBudgetStatus>,
    projectedMonthlySpending: Int,
    daysElapsedInMonth: Int,
    daysInMonth: Int
): List<SuggestionItem> {
    val totalCurrent = canonicalCurrent.values.sum()
    val totalPrevious = canonicalPrevious.values.sum()
    val out = mutableListOf<SuggestionItem>()

    // 1. Spending exceeds income (cashflow critical)
    if (totalIncome in 1..<totalCurrent) {
        val deficit = totalCurrent - totalIncome
        out += SuggestionItem(
            icon = Icons.Default.Warning,
            title = "Pengeluaran melebihi pemasukan",
            subtitle = "Defisit Rp ${formatRupiah(deficit)} bulan ini — kurangi belanja non-esensial.",
            severity = SuggestionSeverity.Critical
        )
    }

    // 2. Per-category budget overruns (sorted by largest overspend)
    val overruns = budgetStatuses
        .filter { it.allocated > 0 && it.spent > it.allocated }
        .sortedByDescending { it.spent - it.allocated }
    for (status in overruns.take(2)) {
        val over = status.spent - status.allocated
        out += SuggestionItem(
            icon = iconForCategory(status.category),
            title = "${status.category} over budget",
            subtitle = "Melebihi alokasi Rp ${formatRupiah(over)} (${(status.percentUsed * 100).toInt()}% terpakai).",
            severity = SuggestionSeverity.Critical
        )
    }

    // 3. Projected to exceed total monthly budget
    if (monthlyBudget in 1..<projectedMonthlySpending && totalCurrent <= monthlyBudget) {
        val projectedOver = projectedMonthlySpending - monthlyBudget
        out += SuggestionItem(
            icon = Icons.Default.Speed,
            title = "Kecepatan belanja terlalu tinggi",
            subtitle = "Pada laju ini, akhir bulan over ~Rp ${formatRupiah(projectedOver)}.",
            severity = SuggestionSeverity.Warning
        )
    }

    // 4. Category nearing its budget limit (80–100%)
    val nearingLimit = budgetStatuses
        .filter { it.allocated > 0 && it.percentUsed in 0.8f..1f && it.spent <= it.allocated }
        .sortedByDescending { it.percentUsed }
    for (status in nearingLimit.take(1)) {
        out += SuggestionItem(
            icon = iconForCategory(status.category),
            title = "${status.category} hampir habis",
            subtitle = "Sudah ${(status.percentUsed * 100).toInt()}% dari alokasi terpakai.",
            severity = SuggestionSeverity.Warning
        )
    }

    // 5. Significant MoM category increases (>30% jump on a non-trivial base)
    val momIncreases = canonicalCurrent.entries
        .filter { it.key != "Income" }
        .mapNotNull { (cat, cur) ->
            val prev = canonicalPrevious[cat] ?: 0
            if (prev in 50_000..<cur) {
                val deltaPct = ((cur - prev).toFloat() / prev) * 100f
                if (deltaPct >= 30f) Triple(cat, cur - prev, deltaPct.toInt()) else null
            } else null
        }
        .sortedByDescending { it.second }
    for ((cat, delta, pct) in momIncreases.take(2)) {
        out += SuggestionItem(
            icon = iconForCategory(cat),
            title = "$cat naik $pct%",
            subtitle = "Naik Rp ${formatRupiah(delta)} vs bulan lalu — cek pemicunya.",
            severity = SuggestionSeverity.Warning
        )
    }

    // 6. Savings rate insight
    if (totalIncome > 0) {
        val savings = totalIncome - totalCurrent
        val savingsRate = savings.toFloat() / totalIncome
        when {
            savingsRate >= 0.2f -> out += SuggestionItem(
                icon = Icons.Default.Savings,
                title = "Tingkat tabungan sehat",
                subtitle = "Menabung ${(savingsRate * 100).toInt()}% dari pemasukan — pertahankan!",
                severity = SuggestionSeverity.Positive
            )
            savingsRate in 0f..0.1f -> out += SuggestionItem(
                icon = Icons.Default.Savings,
                title = "Tingkat tabungan rendah",
                subtitle = "Hanya ${(savingsRate * 100).toInt()}% pemasukan ditabung — target 20%.",
                severity = SuggestionSeverity.Warning
            )
            else -> { /* moderate, no notice */ }
        }
    }

    // 7. Positive: overall spending decreased MoM
    if (totalPrevious > 0 && totalCurrent < totalPrevious) {
        val savedDelta = totalPrevious - totalCurrent
        val pct = ((savedDelta.toFloat() / totalPrevious) * 100).toInt()
        out += SuggestionItem(
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            title = "Pengeluaran menurun $pct%",
            subtitle = "Hemat Rp ${formatRupiah(savedDelta)} vs bulan lalu — kerja bagus!",
            severity = SuggestionSeverity.Positive
        )
    }

    // 8. Top category is dominant
    val topEntry = canonicalCurrent.entries
        .filter { it.key != "Income" }
        .maxByOrNull { it.value }
    if (topEntry != null && totalCurrent > 0) {
        val share = topEntry.value.toFloat() / totalCurrent
        if (share >= 0.4f && out.none { it.title.contains(topEntry.key) }) {
            out += SuggestionItem(
                icon = iconForCategory(topEntry.key),
                title = "${topEntry.key} mendominasi",
                subtitle = "${(share * 100).toInt()}% pengeluaran ada di kategori ini.",
                severity = SuggestionSeverity.Info
            )
        }
    }

    // Defaults if user has no data yet — guide them to start
    if (out.isEmpty()) {
        if (totalCurrent == 0 && totalIncome == 0) {
            out += SuggestionItem(
                icon = Icons.Default.Insights,
                title = "Mulai catat transaksi",
                subtitle = "Tambahkan pemasukan & pengeluaran untuk dapat insight personal.",
                severity = SuggestionSeverity.Info
            )
        }
        if (monthlyBudget == 0) {
            out += SuggestionItem(
                icon = Icons.Default.AccountBalanceWallet,
                title = "Atur anggaran bulanan",
                subtitle = "Tetapkan budget supaya kami bisa peringatkan saat hampir habis.",
                severity = SuggestionSeverity.Info
            )
        }
        if (out.isEmpty()) {
            out += SuggestionItem(
                icon = Icons.Default.VerifiedUser,
                title = "Keuangan terkendali",
                subtitle = "Tidak ada alarm — terus pantau dan disiplin.",
                severity = SuggestionSeverity.Positive
            )
        }
    }

    return out.take(6)
}

internal fun formatRupiah(amount: Int): String =
    java.text.NumberFormat.getInstance(java.util.Locale("in", "ID")).format(amount.toLong())

internal fun daysInCurrentMonth(): Int {
    val cal = Calendar.getInstance()
    return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
}

internal fun dayOfMonthToday(): Int {
    val cal = Calendar.getInstance()
    return cal.get(Calendar.DAY_OF_MONTH)
}
