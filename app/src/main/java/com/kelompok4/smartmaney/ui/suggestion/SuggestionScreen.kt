package com.kelompok4.smartmaney.ui.suggestion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kelompok4.smartmaney.ui.theme.SmDanger
import com.kelompok4.smartmaney.ui.theme.SmPrimary
import com.kelompok4.smartmaney.ui.theme.SmSuccess
import com.kelompok4.smartmaney.ui.theme.SmWarning
import com.kelompok4.smartmaney.viewmodel.SuggestionViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionScreen(
    onBackClick: () -> Unit,
    onSetBudgetClick: () -> Unit,
    viewModel: SuggestionViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Smart Insights",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onSetBudgetClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SmPrimary)
                ) {
                    Text(
                        "Atur Anggaran Bulanan",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SpendingTrendCard(uiState)

            BudgetHealthCard(uiState)

            CashflowCard(uiState)

            // Smart Suggestions
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Smart Saving Suggestions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                uiState.suggestions.forEach { suggestion ->
                    SuggestionItemCard(suggestion)
                }
            }

            // Category Analysis
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Category Analysis",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        if (uiState.categoryChartData.isNotEmpty()) {
                            SimpleBarChart(uiState.categoryChartData)
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Belum ada data pengeluaran",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Paling Banyak",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    uiState.topSpendingCategory,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "Total",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Rp ${formatRupiah(uiState.topSpendingAmount)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SmPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Per-category budget breakdown
            if (uiState.budgetCategories.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Status Anggaran per Kategori",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            uiState.budgetCategories.forEach { status ->
                                CategoryBudgetRow(status)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SpendingTrendCard(uiState: SuggestionUiState) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("SPENDING TREND", color = SmPrimary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(8.dp))

            val diff = uiState.totalCurrentMonthSpending - uiState.totalPreviousMonthSpending
            val pct = if (uiState.totalPreviousMonthSpending > 0) {
                ((diff.toFloat() / uiState.totalPreviousMonthSpending) * 100).toInt()
            } else 0

            val (label, icon, color) = when {
                uiState.totalPreviousMonthSpending == 0 -> Triple(
                    "Belum ada data bulan lalu",
                    Icons.Default.ArrowUpward,
                    MaterialTheme.colorScheme.onSurfaceVariant
                )
                diff > 0 -> Triple("naik ${abs(pct)}%", Icons.Default.ArrowUpward, SmDanger)
                diff < 0 -> Triple("turun ${abs(pct)}%", Icons.Default.ArrowDownward, SmSuccess)
                else -> Triple(
                    "sama dengan bulan lalu",
                    Icons.Default.ArrowUpward,
                    MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                "Pengeluaran bulan ini\n$label",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Perubahan sebesar ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Text(
                    "Rp ${formatRupiah(abs(diff))}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            val progressValue = if (uiState.totalPreviousMonthSpending > 0) {
                (uiState.totalCurrentMonthSpending.toFloat() / uiState.totalPreviousMonthSpending).coerceIn(0f, 1f)
            } else 0f
            LinearProgressIndicator(
                progress = { progressValue },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (progressValue < 1f) SmPrimary else SmDanger,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun BudgetHealthCard(uiState: SuggestionUiState) {
    if (uiState.monthlyBudget <= 0) return
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "KESEHATAN ANGGARAN",
                color = SmPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            val utilization = uiState.budgetUtilization
            val pctUsed = (utilization * 100).toInt()
            val budgetColor = when {
                utilization >= 1f -> SmDanger
                utilization >= 0.8f -> SmWarning
                else -> SmSuccess
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        "Rp ${formatRupiah(uiState.totalCurrentMonthSpending)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "dari Rp ${formatRupiah(uiState.monthlyBudget)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "$pctUsed%",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = budgetColor
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { utilization.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = budgetColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            val projected = uiState.projectedMonthlySpending
            val projectionText = when {
                projected > uiState.monthlyBudget -> {
                    val over = projected - uiState.monthlyBudget
                    "Proyeksi akhir bulan: over Rp ${formatRupiah(over)}"
                }
                else -> {
                    val left = uiState.monthlyBudget - projected
                    "Proyeksi akhir bulan: sisa Rp ${formatRupiah(left)}"
                }
            }
            Text(
                projectionText,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Hari ke-${uiState.daysElapsedInMonth} dari ${uiState.daysInMonth}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CashflowCard(uiState: SuggestionUiState) {
    if (uiState.totalCurrentMonthIncome <= 0 && uiState.totalCurrentMonthSpending <= 0) return
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "ARUS KAS BULAN INI",
                color = SmPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                CashflowMetric(
                    label = "Pemasukan",
                    value = "Rp ${formatRupiah(uiState.totalCurrentMonthIncome)}",
                    color = SmSuccess,
                    modifier = Modifier.weight(1f)
                )
                CashflowMetric(
                    label = "Pengeluaran",
                    value = "Rp ${formatRupiah(uiState.totalCurrentMonthSpending)}",
                    color = SmDanger,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            val net = uiState.totalCurrentMonthIncome - uiState.totalCurrentMonthSpending
            val savingsPct = (uiState.savingsRate * 100).toInt()
            val netColor = if (net >= 0) SmSuccess else SmDanger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Sisa / Tabungan",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Rp ${formatRupiah(net)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = netColor
                    )
                }
                if (uiState.totalCurrentMonthIncome > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Savings rate",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "$savingsPct%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = netColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CashflowMetric(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun CategoryBudgetRow(status: CategoryBudgetStatus) {
    val color = when {
        status.percentUsed >= 1f -> SmDanger
        status.percentUsed >= 0.8f -> SmWarning
        else -> SmPrimary
    }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    iconForCategory(status.category),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    status.category,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                "Rp ${formatRupiah(status.spent)} / ${formatRupiah(status.allocated)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { status.percentUsed.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun SuggestionItemCard(suggestion: SuggestionItem) {
    val tint = when (suggestion.severity) {
        SuggestionSeverity.Positive -> SmSuccess
        SuggestionSeverity.Info -> SmPrimary
        SuggestionSeverity.Warning -> SmWarning
        SuggestionSeverity.Critical -> SmDanger
    }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(suggestion.icon, contentDescription = suggestion.title, tint = tint)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    suggestion.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    suggestion.subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SimpleBarChart(chartData: List<CategorySpendData>) {
    val topAmount = chartData.maxOfOrNull { it.amount } ?: 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        chartData.forEach { data ->
            val isTop = data.amount == topAmount && topAmount > 0
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(
                    "Rp${formatRupiah(data.amount / 1000)}rb",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .fillMaxHeight(data.percentage)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(if (isTop) SmPrimary else MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    data.label,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
