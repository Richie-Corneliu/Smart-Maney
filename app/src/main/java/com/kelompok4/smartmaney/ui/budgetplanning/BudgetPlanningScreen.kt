package com.kelompok4.smartmaney.ui.budgetplanning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    uiState: BudgetPlanningUiState,
    onBackClick: () -> Unit,
    onBudgetUpdated: (Int) -> Unit = {}
) {
    var showBudgetEditor by remember { mutableStateOf(false) }
    var budgetInput by remember(uiState.totalBudget) { mutableStateOf(uiState.totalBudget.toString()) }

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
                    onEditClick = {
                        budgetInput = uiState.totalBudget.toString()
                        showBudgetEditor = true
                    }
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

        if (showBudgetEditor) {
            AlertDialog(
                onDismissRequest = { showBudgetEditor = false },
                title = { Text("Edit Total Budget") },
                text = {
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it.filter(Char::isDigit) },
                        label = { Text("Total budget") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val newBudget = budgetInput.toIntOrNull()
                            if (newBudget != null) {
                                onBudgetUpdated(newBudget)
                            }
                            showBudgetEditor = false
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBudgetEditor = false }) {
                        Text("Cancel")
                    }
                }
            )
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

private val previewBudgets = listOf(
    BudgetCategoryItem("1", "Makanan & Minuman", 2500000, 2025000),
    BudgetCategoryItem("2", "Transportasi", 1500000, 1350000),
    BudgetCategoryItem("3", "Tempat Tinggal", 1500000, 1125000),
    BudgetCategoryItem("4", "Hiburan", 1000000, 0),
    BudgetCategoryItem("5", "Lain-lain", 500000, 0)
)

@Preview
@Composable
fun PreviewBudgetPlanning() {
    SmartManeyTheme {
        BudgetPlanningScreen(
            uiState = BudgetPlanningUiState(categoryBudgets = previewBudgets),
            onBackClick = {}
        )
    }
}