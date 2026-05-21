package com.kelompok4.smartmaney.ui.scheduledbills

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kelompok4.smartmaney.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledBillsScreen(
    modifier: Modifier = Modifier,
    uiState: ScheduledBillsUiState,
    onBackClick: () -> Unit,
    onPayClick: (String) -> Unit,
    onAddBill: (String, Int, Long, Long?, RepeatFrequency) -> Unit,
    onUpdateBill: (String, String, Int, Long, Long?, RepeatFrequency) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var billToEdit by remember { mutableStateOf<BillItem?>(null) }
    val tabs = listOf("Belum Bayar", "Sudah Bayar")

    if (showDialog) {
        BillEditorDialog(
            onDismiss = { showDialog = false; billToEdit = null },
            onSave = { title, amt, start, end, frequency ->
                if (billToEdit == null) {
                    onAddBill(title, amt, start, end, frequency)
                } else {
                    onUpdateBill(billToEdit!!.id, title, amt, start, end, frequency)
                }
                showDialog = false
            },
            initialBill = billToEdit
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = SmBackgroundAlt,
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true; billToEdit = null }) {
                Text("+", fontSize = 24.sp)
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Tagihan Terjadwal", fontWeight = FontWeight.Bold, color = SmTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = SmTextPrimary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SmPrimary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            val currentList = if (selectedTab == 0) uiState.upcomingBills else uiState.paidBills

            if (currentList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tidak ada tagihan di menu ini.", color = SmMuted, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentList, key = { it.id }) { bill ->
                        BillRowItem(
                            bill = bill,
                            onPayClick = { onPayClick(bill.id) },
                            onEditClick = { billToEdit = bill; showDialog = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BillRowItem(bill: BillItem, onPayClick: () -> Unit, onEditClick: () -> Unit) {
    val cardBg = if (bill.isOverdue) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.surface
    val statusColor = if (bill.isOverdue) Color(0xFFEF4444) else if (bill.isPaid) SmPrimary else SmMuted

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(if (bill.isPaid) Color(0xFFDCFCE7) else Color(0xFFF1F5F9), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (bill.isPaid) Icons.Default.CheckCircle else Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = if (bill.isPaid) SmPrimary else SmMuted
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(bill.title, fontWeight = FontWeight.Bold, color = SmTextPrimary, fontSize = 15.sp)
                Text(bill.dueDateLabel, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(formatRupiahLocal(bill.amount), fontWeight = FontWeight.ExtraBold, color = SmTextPrimary, fontSize = 15.sp)
                if (!bill.isPaid) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bayar",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SmPrimary)
                            .clickable { onPayClick() }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private fun formatRupiahLocal(amount: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    return "Rp ${formatter.format(amount)}"
}

@Composable
fun BillEditorDialog(
    onDismiss: () -> Unit,
    onSave: (String, Int, Long, Long?, RepeatFrequency) -> Unit,
    initialBill: BillItem? = null
) {
    var title by remember { mutableStateOf(initialBill?.title ?: "") }
    var amount by remember { mutableStateOf(initialBill?.amount?.toString() ?: "") }
    var repeatFrequency by remember { mutableStateOf(initialBill?.repeatFrequency ?: RepeatFrequency.NONE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        titleContentColor = Color.Black,
        textContentColor = Color.Black,
        title = { Text(if (initialBill == null) "Tambah Tagihan" else "Edit Tagihan") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Tagihan") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color.DarkGray,
                        unfocusedLabelColor = Color.Gray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter(Char::isDigit) },
                    label = { Text("Jumlah (Rp)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color.DarkGray,
                        unfocusedLabelColor = Color.Gray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Ulangi Tagihan:", fontWeight = FontWeight.Bold, color = Color.Black)
                RepeatFrequency.entries.forEach { freq ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = repeatFrequency == freq, onClick = { repeatFrequency = freq })
                        Text(freq.name.lowercase().replaceFirstChar { it.uppercase() }, color = Color.Black)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(title, amount.toIntOrNull() ?: 0, System.currentTimeMillis(), null, repeatFrequency)
                onDismiss()
            }) { Text("Simpan", color = Color(0xFF6200EE)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) }
        }
    )
}