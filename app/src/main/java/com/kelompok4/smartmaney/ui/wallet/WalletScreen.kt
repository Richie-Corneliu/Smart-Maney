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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.kelompok4.smartmaney.ui.theme.SmDanger
import com.kelompok4.smartmaney.ui.theme.SmSuccess
import com.kelompok4.smartmaney.ui.theme.SmartManeyTheme
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onOpenBudgetPlanning: () -> Unit = {}
) {
    var uiState by remember {
        mutableStateOf(
            WalletUiState(
                initialBalance = 2_000_000,
                transactions = emptyList()
            )
        )
    }
    var titleInput by rememberSaveable { mutableStateOf("") }
    var amountInput by rememberSaveable { mutableStateOf("") }

    fun addTransaction(type: WalletTransactionType) {
        val amount = amountInput.toIntOrNull() ?: return
        val previous = uiState
        uiState = reduceWalletState(
            uiState,
            WalletAction.AddTransaction(
                title = titleInput,
                amount = amount,
                type = type
            )
        )
        if (uiState != previous) {
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
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = onOpenBudgetPlanning,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Text(text = "Budget")
                    }
                },
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
                        uiState = reduceWalletState(uiState, WalletAction.AdjustBaseBalance(-100_000))
                    },
                    onIncreaseBase = {
                        uiState = reduceWalletState(uiState, WalletAction.AdjustBaseBalance(100_000))
                    }
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
                            uiState = reduceWalletState(uiState, WalletAction.RemoveTransaction(item.id))
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
    onIncreaseBase: () -> Unit
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
        WalletScreen()
    }
}


