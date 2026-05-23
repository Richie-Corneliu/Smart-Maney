package com.kelompok4.smartmaney.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kelompok4.smartmaney.ui.theme.LocalCurrency
import com.kelompok4.smartmaney.ui.theme.SmPrimary
import com.kelompok4.smartmaney.ui.theme.SmartManeyTheme
import com.kelompok4.smartmaney.ui.wallet.CurrencyVisualTransformation
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    initialAmount: Int,
    initialNote: String,
    initialCategory: String,
    initialPaymentMethod: String,
    initialCreatedAtMillis: Long,
    onBackClick: () -> Unit,
    onSaveClick: (amount: Int, note: String, category: String, paymentMethod: String, createdAtMillis: Long) -> Unit
) {
    var amountInput by remember(initialAmount) { mutableStateOf(initialAmount.toString()) }
    var noteInput by remember(initialNote) { mutableStateOf(initialNote) }
    var categoryInput by remember(initialCategory) { mutableStateOf(initialCategory) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var paymentMethodInput by remember(initialPaymentMethod) { mutableStateOf(initialPaymentMethod) }
    var paymentMethodExpanded by remember { mutableStateOf(false) }
    var dateInput by remember(initialCreatedAtMillis) {
        mutableStateOf(formatMillisToInput(initialCreatedAtMillis))
    }
    var dateError by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Transaksi", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Batal")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = amountInput,
                visualTransformation = CurrencyVisualTransformation(),
                onValueChange = { amountInput = it.filter(Char::isDigit) },
                label = { Text("Nominal (${LocalCurrency.current.symbol})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = categoryInput,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    for (option in CATEGORIES) {
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                categoryInput = option
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = paymentMethodExpanded,
                onExpandedChange = { paymentMethodExpanded = it }
            ) {
                OutlinedTextField(
                    value = paymentMethodInput,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Metode Pembayaran") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentMethodExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = paymentMethodExpanded,
                    onDismissRequest = { paymentMethodExpanded = false }
                ) {
                    for (option in PAYMENT_METHODS) {
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                paymentMethodInput = option
                                paymentMethodExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = dateInput,
                onValueChange = {
                    dateInput = it
                    dateError = false
                },
                label = { Text("Tanggal (yyyy-MM-dd HH:mm)") },
                isError = dateError,
                supportingText = if (dateError) {
                    { Text("Format tidak valid. Gunakan yyyy-MM-dd HH:mm") }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                label = { Text("Catatan") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val amount = amountInput.toIntOrNull() ?: return@Button
                    val parsedMillis = parseDateInput(dateInput)
                    if (parsedMillis == null) {
                        dateError = true
                        return@Button
                    }
                    onSaveClick(amount, noteInput, categoryInput, paymentMethodInput, parsedMillis)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SmPrimary)
            ) {
                Text("Simpan Perubahan", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private val CATEGORIES = listOf(
    "Makanan & Minuman", "Transportasi", "Hiburan", "Tempat Tinggal", "Income", "Lain-lain"
)

private val PAYMENT_METHODS = listOf(
    "Cash", "Debit Card", "Credit Card", "E-Wallet", "QRIS", "Bank Transfer"
)

private fun formatMillisToInput(millis: Long): String {
    if (millis <= 0L) return ""
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(millis))
}

private fun parseDateInput(input: String): Long? {
    if (input.isBlank()) return null
    val patterns = listOf("yyyy-MM-dd HH:mm", "yyyy-MM-dd", "dd/MM/yyyy HH:mm", "dd/MM/yyyy")
    for (pattern in patterns) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.isLenient = false
            val pos = java.text.ParsePosition(0)
            val date = sdf.parse(input, pos)
            if (date != null && pos.index == input.length) return date.time
        } catch (_: ParseException) {
            // try next pattern
        }
    }
    return null
}

@Preview(showBackground = true)
@Composable
fun EditTransactionScreenPreview() {

    SmartManeyTheme {
        EditTransactionScreen(
            initialAmount = 50000,
            initialNote = "Makan siang di restoran",
            initialCategory = "Makanan & Minuman",
            initialPaymentMethod = "Debit Card",
            initialCreatedAtMillis = System.currentTimeMillis(),
            onBackClick = {},
            onSaveClick = { _, _, _, _, _ -> }
        )
    }
}