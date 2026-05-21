package com.kelompok4.smartmaney.ui.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.InsertChartOutlined
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kelompok4.smartmaney.DashboardTab
import com.kelompok4.smartmaney.R
import com.kelompok4.smartmaney.ui.theme.SmCategoryFood
import com.kelompok4.smartmaney.ui.theme.SmCategoryHealth
import com.kelompok4.smartmaney.ui.theme.SmCategoryRent
import com.kelompok4.smartmaney.ui.theme.SmCategoryShopping
import com.kelompok4.smartmaney.ui.theme.SmCategoryTransport
import com.kelompok4.smartmaney.ui.theme.SmDivider
import com.kelompok4.smartmaney.ui.theme.SmHeader
import com.kelompok4.smartmaney.ui.theme.SmMuted
import com.kelompok4.smartmaney.ui.theme.SmPrimary
import com.kelompok4.smartmaney.ui.theme.SmSuccess
import com.kelompok4.smartmaney.ui.theme.SmSuccessAlt
import com.kelompok4.smartmaney.ui.theme.SmTextPrimary
import com.kelompok4.smartmaney.ui.theme.SmartManeyTheme
import java.text.NumberFormat
import java.util.Locale

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
    spendingByCategory: Map<String, Int>,
    selectedTab: DashboardTab,
    onAdjustBudgetClick: () -> Unit,
    onTabSelected: (DashboardTab) -> Unit,
    onLogoutClick: () -> Unit,
    onScanReceiptClick: () -> Unit,
    onMonthlyRecapClick: () -> Unit,
    showNavigationBar: Boolean = true
) {
    val totalSpent = spendingByCategory.values.sum()
    val categories = spendingByCategory.entries
        .filter { it.value > 0 }
        .sortedByDescending { it.value }
        .map { entry ->
            SpendingCategory(
                name = entry.key,
                share = if (totalSpent > 0) entry.value.toFloat() / totalSpent.toFloat() else 0f,
                color = colorForCategory(entry.key)
            )
        }

    if (showNavigationBar) {
        Scaffold(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.background,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onScanReceiptClick,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier
                        .size(60.dp)
                        .offset(y = 60.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Scan Receipt",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
            bottomBar = {
                DashboardBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected
                )
            }
        ) { innerPadding ->
            DashboardContent(
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPadding,
                userName = userName,
                monthlySpent = monthlySpent,
                monthlyBudget = monthlyBudget,
                budgetProgress = budgetProgress,
                onAdjustBudgetClick = onAdjustBudgetClick,
                onLogoutClick = onLogoutClick,
                onMonthlyRecapClick = onMonthlyRecapClick,
                categories = categories
            )
        }
    } else {
        DashboardContent(
            modifier = modifier,
            contentPadding = PaddingValues(0.dp),
            userName = userName,
            monthlySpent = monthlySpent,
            monthlyBudget = monthlyBudget,
            budgetProgress = budgetProgress,
            onAdjustBudgetClick = onAdjustBudgetClick,
            onLogoutClick = onLogoutClick,
            onMonthlyRecapClick = onMonthlyRecapClick,
            categories = categories
        )
    }
}

@Composable
private fun DashboardContent(
    modifier: Modifier,
    contentPadding: PaddingValues,
    userName: String,
    monthlySpent: Int,
    monthlyBudget: Int,
    budgetProgress: Float,
    onAdjustBudgetClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onMonthlyRecapClick: () -> Unit,
    categories: List<SpendingCategory>
) {
    Column(
        modifier = modifier
            .padding(contentPadding)
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
                marker = stringResource(R.string.monthly_recap_marker),
                onClick = onMonthlyRecapClick
            )
        }
    }
}
@Composable
private fun HeaderSection(userName: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SmHeader)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(text = stringResource(R.string.monthly_spending_summary), color = SmMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatCurrency(monthlySpent),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = SmTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.change_vs_last_month),
                color = SmSuccess,
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
                color = MaterialTheme.colorScheme.primary,
                trackColor = SmDivider
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.budget_label, formatCurrency(monthlyBudget)),
                    color = SmMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = onAdjustBudgetClick) {
                    Text(
                        text = stringResource(R.string.adjust_budget),
                        color = SmSuccessAlt,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        if (categories.isEmpty()) {
            Text(
                text = stringResource(R.string.spending_distribution_empty),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                color = SmMuted,
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DonutChart(categories = categories)
                Spacer(modifier = Modifier.width(28.dp))
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
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(categories: List<SpendingCategory>) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(categories) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(durationMillis = 900))
    }
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(120.dp)) {
            var start = -90f
            categories.forEach { item ->
                val fullSweep = item.share * 360f
                val animatedSweep = fullSweep * progress.value
                drawArc(
                    color = item.color,
                    startAngle = start,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    style = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Butt)
                )
                start += animatedSweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.total_label),
                style = MaterialTheme.typography.labelSmall,
                color = SmMuted
            )
            Text(
                text = stringResource(R.string.total_items_count, categories.size),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SmTextPrimary
            )
        }
    }
}

private fun colorForCategory(category: String): Color {
    return when (category.trim().lowercase(Locale.ROOT)) {
        "food", "food & beverages", "makanan", "makanan & minuman", "kuliner" -> SmCategoryFood
        "rent", "housing", "tempat tinggal", "accommodation", "rent & bills", "tagihan" -> SmCategoryRent
        "transport", "transportation", "transportasi", "commute" -> SmCategoryTransport
        "shopping", "belanja", "hiburan", "entertainment" -> SmCategoryShopping
        "health", "healthcare", "kesehatan", "medical", "vitamin", "obat" -> SmCategoryHealth
        else -> SmPrimary
    }
}


@Composable
private fun InsightCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    marker: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    .background(SmHeader),
                contentAlignment = Alignment.Center
            ) {
                Text(text = marker, color = Color.White)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = SmMuted)
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
                BottomNavItem(imageVector = Icons.Default.Home, tab = DashboardTab.Home, selectedTab = selectedTab, onTabSelected = onTabSelected)
                BottomNavItem(imageVector = Icons.Default.Wallet, DashboardTab.Wallet, selectedTab, onTabSelected)
            }

            // Ruang kosong mutlak untuk FAB di tengah
            Spacer(modifier = Modifier.width(72.dp))

            // Sisi Kanan
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BottomNavItem(imageVector = Icons.Default.InsertChartOutlined, DashboardTab.Reports, selectedTab, onTabSelected)
                BottomNavItem(imageVector = Icons.Default.Person, DashboardTab.Profile, selectedTab, onTabSelected)
            }
        }
    }
}

// Fungsi pembantu agar kode Row di atas tidak kotor
@Composable
private fun BottomNavItem(
    imageVector: ImageVector,
    tab: DashboardTab,
    selectedTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit
) {
    val isSelected = tab == selectedTab
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = { onTabSelected(tab) }) {
            Icon(
                imageVector = imageVector,
                contentDescription = tab.name,
                tint = if (isSelected) SmPrimary else SmMuted
            )
        }

        Text(
            text = tab.name,
            color = if (isSelected) SmPrimary else SmMuted,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun formatCurrency(value: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return "Rp ${formatter.format(value)}"
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    SmartManeyTheme {
        DashboardScreen(
            modifier = Modifier.fillMaxSize(),
            userName = "Andra",
            monthlySpent = 4_500_000,
            monthlyBudget = 7_000_000,
            budgetProgress = 0.64f,
            spendingByCategory = mapOf(
                "Makanan & Minuman" to 2_025_000,
                "Tempat Tinggal" to 1_125_000,
                "Transportasi" to 1_350_000
            ),
            selectedTab = DashboardTab.Home,
            onAdjustBudgetClick = {},
            onTabSelected = {},
            onLogoutClick = {},
            onScanReceiptClick = {},
            onMonthlyRecapClick = {},
        )
    }
}


