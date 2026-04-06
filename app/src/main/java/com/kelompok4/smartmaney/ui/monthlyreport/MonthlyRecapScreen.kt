package com.kelompok4.smartmaney.ui.monthlyreport

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

// Reusing colors from your palette
private val DashboardBackground = Color(0xFFF1F1F1)
private val HeaderGreen = Color(0xFF36A852)
private val CardSurface = Color(0xFFF8F8F8)
private val MutedBlue = Color(0xFF6C7B95)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyRecapScreen(
    modifier: Modifier = Modifier,
    uiState: MonthlyReportUiState = MonthlyReportUiState(
        topCategories = MonthlyReportDummyData.dummyCategories,
        recentTransactions = MonthlyReportDummyData.dummyTransactions
    ),
    onBackClick: () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DashboardBackground,
        topBar = {
            TopAppBar(
                title = { Text("Laporan Bulanan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DashboardBackground
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Total Pengeluaran Card
            item {
                TotalExpenseCard(month = uiState.month, amount = uiState.totalExpense)
            }

            // 2. Grafik Distribusi Pengeluaran
            item {
                Text(
                    text = "Distribusi Kategori",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                ExpenseBarChartCard(categories = uiState.topCategories)
            }

            // 3. Riwayat Transaksi
            item {
                Text(
                    text = "Transaksi Bulan Ini",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(uiState.recentTransactions) { transaction ->
                TransactionItemCard(transaction = transaction)
            }
        }
    }
}

@Composable
private fun TotalExpenseCard(month: String, amount: Int) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = HeaderGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Total Pengeluaran $month", color = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatCurrency(amount),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ExpenseBarChartCard(categories: List<CategoryExpense>) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Horizontal Bar Chart Representation
            Canvas(modifier = Modifier.fillMaxWidth().height(24.dp)) {
                var currentX = 0f
                val totalWidth = size.width

                categories.forEach { category ->
                    val sectionWidth = totalWidth * category.percentage
                    drawRoundRect(
                        color = category.color,
                        topLeft = Offset(x = currentX, y = 0f),
                        size = Size(width = sectionWidth, height = size.height),
                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                    )
                    currentX += sectionWidth
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Legend
            categories.forEach { category ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(category.color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = category.category, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = formatCurrency(category.amount),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionItemCard(transaction: TransactionItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(transaction.iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = transaction.iconColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaction.title, fontWeight = FontWeight.Bold)
                Text(text = transaction.date, color = MutedBlue, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = "- ${formatCurrency(transaction.amount)}",
                color = Color(0xFFD32F2F), // Red for expense
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatCurrency(value: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return "Rp ${formatter.format(value)}"
}

@Preview
@Composable
fun PreviewMonthlyRecap() {
    MonthlyRecapScreen(onBackClick = {})
}