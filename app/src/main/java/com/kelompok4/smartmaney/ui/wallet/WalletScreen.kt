package com.kelompok4.smartmaney.ui.wallet

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kelompok4.smartmaney.ui.theme.SmDanger
import com.kelompok4.smartmaney.ui.theme.SmSuccess
import com.kelompok4.smartmaney.ui.theme.SmartManeyTheme
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    modifier: Modifier = Modifier,
    uiState: WalletUiState,
    onAddTransaction: (String, Int, WalletTransactionType) -> Unit,
    onDeleteTransaction: (String) -> Unit,
    onAdjustBaseBalance: (Int) -> Unit,
    onSuggestionClick: () -> Unit
) {
    var titleInput by rememberSaveable { mutableStateOf("") }
    var amountInput by rememberSaveable { mutableStateOf("") }

    fun addTransaction(type: WalletTransactionType) {
        val amount = amountInput.toIntOrNull() ?: return
        val normalizedTitle = titleInput.trim()
        if (normalizedTitle.isNotBlank()) {
            onAddTransaction(normalizedTitle, amount, type)
            titleInput = ""
            amountInput = ""
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Wallet", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                WalletSummaryCard(
                    baseBalance = uiState.initialBalance,
                    currentBalance = uiState.currentBalance,
                    onDecreaseBase = {
                        onAdjustBaseBalance(-100_000)
                    },
                    onIncreaseBase = {
                        onAdjustBaseBalance(100_000)
                    },
                    onSuggestionClick = onSuggestionClick
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = "Add Transaction", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { amountInput = it.filter(Char::isDigit) },
                            label = { Text("Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { addTransaction(WalletTransactionType.Income) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Add Income")
                            }
                            Button(
                                onClick = { addTransaction(WalletTransactionType.Expense) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Add Expense")
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (uiState.transactions.isEmpty()) {
                item {
                    Text(
                        text = "No transaction yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(uiState.transactions, key = { it.id }) { item ->
                    WalletTransactionItem(
                        transaction = item,
                        onDeleteClick = {
                            onDeleteTransaction(item.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WalletSummaryCard(
    baseBalance: Int,
    currentBalance: Int,
    onDecreaseBase: () -> Unit,
    onIncreaseBase: () -> Unit,
    onSuggestionClick: () -> Unit // PARAMETER BARU
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Current Balance",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatCurrency(currentBalance),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Base: ${formatCurrency(baseBalance)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onDecreaseBase) { Text("-100k") }
                    Button(onClick = onIncreaseBase) { Text("+100k") }
                }
            }

            // TOMBOL SUGGESTION BARU
            Button(
                onClick = onSuggestionClick,
                modifier = Modifier.fillMaxWidth(), // Membuat tombol melebar penuh
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text("Lihat Smart Suggestions", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun WalletTransactionItem(
    transaction: WalletTransaction,
    onDeleteClick: () -> Unit
) {
    val isIncome = transaction.type == WalletTransactionType.Income
    val amountColor = if (isIncome) SmSuccess else SmDanger
    val prefix = if (isIncome) "+" else "-"

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaction.title, fontWeight = FontWeight.SemiBold)
                Text(
                    text = transaction.type.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$prefix${formatCurrency(transaction.amount)}",
                color = amountColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp)
            )
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete transaction",
                    tint = Color.Gray
                )
            }
        }
    }
}

private fun formatCurrency(value: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return "Rp ${formatter.format(value)}"
}

@Preview(showBackground = true)
@Composable
private fun WalletScreenPreview() {
    SmartManeyTheme {
        WalletScreen(
            uiState = WalletUiState(
                initialBalance = 2_000_000,
                transactions = listOf(
                    WalletTransaction(
                        id = "preview-1",
                        title = "Makan Siang",
                        amount = 45_000,
                        type = WalletTransactionType.Expense,
                        createdAtMillis = System.currentTimeMillis()
                    )
                )
            ),
            onAddTransaction = { _, _, _ -> },
            onDeleteTransaction = {},
            onAdjustBaseBalance = {},
            onSuggestionClick = {}
        )
    }
}


