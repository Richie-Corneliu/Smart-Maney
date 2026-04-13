package com.kelompok4.smartmaney.ui.budgetplanning

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kelompok4.smartmaney.ui.theme.SmDanger
import com.kelompok4.smartmaney.ui.theme.SmDivider
import com.kelompok4.smartmaney.ui.theme.SmMuted
import com.kelompok4.smartmaney.ui.theme.SmPrimary
import com.kelompok4.smartmaney.ui.theme.SmSurface
import com.kelompok4.smartmaney.ui.theme.SmWarning
import com.kelompok4.smartmaney.ui.theme.SmartManeyTheme
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetPlanningScreen(
    modifier: Modifier = Modifier,
    uiState: BudgetPlanningUiState = BudgetPlanningUiState(
        categoryBudgets = BudgetDummyData.dummyBudgets
    ),
    onBackClick: () -> Unit,
    // Callback if you want to update state later
    onBudgetUpdated: (Int) -> Unit = {}
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Budget Planning", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OverallBudgetCard(
                    totalBudget = uiState.totalBudget,
                    totalSpent = uiState.totalSpent,
                    progress = uiState.overallProgress,
                    onEditClick = { /* Handle Edit Action */ }
                )
            }

            item {
                Text(
                    text = "Budget per Kategori",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(uiState.categoryBudgets) { budgetItem ->
                CategoryBudgetCard(item = budgetItem)
            }
        }
    }
}

@Composable
private fun OverallBudgetCard(
    totalBudget: Int,
    totalSpent: Int,
    progress: Float,
    onEditClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Total Budget Bulanan", color = SmMuted)
                    Text(
                        text = formatCurrency(totalBudget),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Budget", tint = SmPrimary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dynamic progress bar color
            val progressColor = when {
                progress < 0.5f -> SmPrimary
                progress < 0.8f -> SmWarning
                else -> SmDanger
            }

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(40.dp)),
                color = progressColor,
                trackColor = SmDivider
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Terpakai: ${formatCurrency(totalSpent)}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Sisa: ${formatCurrency(totalBudget - totalSpent)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CategoryBudgetCard(item: BudgetCategoryItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SmSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = item.name, fontWeight = FontWeight.Bold)
                Text(text = formatCurrency(item.allocated), fontWeight = FontWeight.Bold, color = SmMuted)
            }

            Spacer(modifier = Modifier.height(12.dp))

            val progressColor = when {
                item.progress < 0.5f -> SmPrimary
                item.progress < 0.8f -> SmWarning
                else -> SmDanger
            }

            LinearProgressIndicator(
                progress = { item.progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(40.dp)),
                color = progressColor,
                trackColor = SmDivider
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tersisa: ${formatCurrency(item.remaining)}",
                style = MaterialTheme.typography.bodySmall,
                color = if (item.remaining < 0) SmDanger else SmMuted
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
fun PreviewBudgetPlanning() {
    SmartManeyTheme {
        BudgetPlanningScreen(onBackClick = {})
    }
}