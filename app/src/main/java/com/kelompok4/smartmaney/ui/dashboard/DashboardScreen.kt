package com.kelompok4.smartmaney.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kelompok4.smartmaney.DashboardTab
import com.kelompok4.smartmaney.R
import com.kelompok4.smartmaney.ui.theme.SmartManeyTheme
import java.text.NumberFormat
import java.util.Locale

private val DashboardBackground = Color(0xFFF1F1F1)
private val HeaderGreen = Color(0xFF36A852)
private val CardSurface = Color(0xFFF8F8F8)
private val AccentGreen = Color(0xFF13D340)
private val MutedBlue = Color(0xFF6C7B95)
private val OrangeCategory = Color(0xFFF26716)
private val BlueCategory = Color(0xFF5E99E8)
private val MintCategory = Color(0xFF34C899)

private data class SpendingCategory(
    val name: String,
    val share: Float,
    val color: Color
)

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    userName: String,
    monthlySpent: Int,
    monthlyBudget: Int,
    budgetProgress: Float,
    selectedTab: DashboardTab,
    onAdjustBudgetClick: () -> Unit,
    onTabSelected: (DashboardTab) -> Unit,
    onLogoutClick: () -> Unit,
    onScanReceiptClick: () -> Unit // Tambahkan parameter aksi untuk tombol tengah
) {
    val categories = listOf(
        SpendingCategory(stringResource(R.string.category_food), 0.45f, OrangeCategory),
        SpendingCategory(stringResource(R.string.category_rent), 0.25f, BlueCategory),
        SpendingCategory(stringResource(R.string.category_transport), 0.30f, MintCategory)
    )

    Scaffold(
        modifier = modifier,
        containerColor = DashboardBackground,

        // 1. KEMBALIKAN TOMBOL MELAYANG KE SINI
        floatingActionButton = {
            FloatingActionButton(
                onClick = onScanReceiptClick,
                shape = CircleShape,
                containerColor = AccentGreen,
                contentColor = Color.White,
                modifier = Modifier
                    .size(60.dp)
                    // INI KUNCINYA: Tarik koordinat Y ke bawah.
                    // Angka positif menarik elemen ke bawah layar.
                    // Sesuaikan angkanya (misal 30.dp, 40.dp, atau 50.dp) sampai posisinya pas di tengah garis Figma lu.
                    .offset(y = 60.dp)
            ) {
                // 2. GANTI ICON MENJADI KAMERA
                 Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Scan Receipt",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,

        // 3. PANGGIL BOTTOM BAR LU
        bottomBar = {
            DashboardBottomBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            HeaderSection(userName = userName)
            Spacer(modifier = Modifier.height(14.dp))
            MonthlySummaryCard(
                monthlySpent = monthlySpent,
                monthlyBudget = monthlyBudget,
                budgetProgress = budgetProgress,
                onAdjustBudgetClick = onAdjustBudgetClick
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.spending_distribution),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            DistributionCard(categories = categories)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.quick_insights),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            QuickInsightsRow()
            Spacer(modifier = Modifier.height(10.dp))
            TextButton(onClick = onLogoutClick) {
                Text(text = stringResource(R.string.logout_action))
            }
        }
    }
}
@Composable
private fun HeaderSection(userName: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = HeaderGreen)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.welcome_back),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.greeting_name, userName),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.notification_placeholder), color = Color.White)
            }
        }
    }
}

@Composable
private fun MonthlySummaryCard(
    monthlySpent: Int,
    monthlyBudget: Int,
    budgetProgress: Float,
    onAdjustBudgetClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(text = stringResource(R.string.monthly_spending_summary), color = MutedBlue)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatCurrency(monthlySpent),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D2438)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.change_vs_last_month),
                color = Color(0xFF118A3A),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { budgetProgress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(40.dp)),
                color = AccentGreen,
                trackColor = Color(0xFFE3E7EE)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.budget_label, formatCurrency(monthlyBudget)),
                    color = MutedBlue,
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = onAdjustBudgetClick) {
                    Text(
                        text = stringResource(R.string.adjust_budget),
                        color = Color(0xFF00A86B),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun DistributionCard(categories: List<SpendingCategory>) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DonutChart(categories = categories)
            Spacer(modifier = Modifier.width(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(item.color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${item.name} (${(item.share * 100).toInt()}%)",
                            color = Color(0xFF3C4A61),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(categories: List<SpendingCategory>) {
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(120.dp)) {
            var start = -90f
            categories.forEach { item ->
                val sweep = item.share * 360f
                drawArc(
                    color = item.color,
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Butt)
                )
                start += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.total_label),
                style = MaterialTheme.typography.labelMedium,
                color = MutedBlue
            )
            Text(
                text = stringResource(R.string.total_items),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D2438)
            )
        }
    }
}

@Composable
private fun QuickInsightsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        InsightCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.scheduled_bills),
            subtitle = stringResource(R.string.scheduled_bills_subtitle),
            marker = stringResource(R.string.scheduled_bills_marker)
        )
        InsightCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.monthly_recap),
            subtitle = stringResource(R.string.monthly_recap_subtitle),
            marker = stringResource(R.string.monthly_recap_marker)
        )
    }
}

@Composable
private fun InsightCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    marker: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(HeaderGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(text = marker, color = Color.White)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MutedBlue)
        }
    }
}

@Composable
private fun DashboardBottomBar(
    selectedTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit
) {
    // BottomAppBar otomatis menghitung batas sistem operasi (Window Insets)
    BottomAppBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        contentPadding = PaddingValues(0.dp) // Reset padding default agar bisa diatur manual
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sisi Kiri
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BottomNavItem(DashboardTab.Home, selectedTab, onTabSelected)
                BottomNavItem(DashboardTab.Wallet, selectedTab, onTabSelected)
            }

            // Ruang kosong mutlak untuk FAB di tengah
            Spacer(modifier = Modifier.width(72.dp))

            // Sisi Kanan
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BottomNavItem(DashboardTab.Reports, selectedTab, onTabSelected)
                BottomNavItem(DashboardTab.Profile, selectedTab, onTabSelected)
            }
        }
    }
}

// Fungsi pembantu agar kode Row di atas tidak kotor
@Composable
private fun BottomNavItem(
    tab: DashboardTab,
    selectedTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit
) {
    val isSelected = tab == selectedTab
    TextButton(onClick = { onTabSelected(tab) }) {
        Text(
            text = tab.name,
            color = if (isSelected) AccentGreen else MutedBlue,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun formatCurrency(value: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return "Rp ${formatter.format(value)}"
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DashboardScreenPreview() {
    SmartManeyTheme {
        DashboardScreen(
            modifier = Modifier.fillMaxSize(),
            userName = "Andra",
            monthlySpent = 4_500_000,
            monthlyBudget = 7_000_000,
            budgetProgress = 0.64f,
            selectedTab = DashboardTab.Home,
            onAdjustBudgetClick = {},
            onTabSelected = {},
            onLogoutClick = {},
            onScanReceiptClick = {}
        )
    }
}


