package com.kelompok4.smartmaney.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    amount: Int,
    note: String,
    category: String,
    paymentMethod: String,
    createdAtMillis: Long,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF8F9FA), // Warna background abu-abu sangat muda khas aplikasi finance
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detail Transaksi", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
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
            // 1. KARTU NOMINAL UTAMA
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Ikon Kategori (Makan)
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)), // Hijau sangat muda
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = "Makan", tint = Color(0xFF4CAF50))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(formatCurrency(amount), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1D2438))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$category • ${formatDate(createdAtMillis)}", fontSize = 14.sp, color = Color.Gray)
                }
            }

            // 2. KARTU RINCIAN DETAIL
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DetailRow(label = "Tanggal", value = formatDate(createdAtMillis))
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    DetailRow(label = "Kategori", value = category)
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    DetailRow(label = "Metode Pembayaran", value = paymentMethod)
                }
            }

            // 3. KARTU CATATAN
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp)
                ) {
                    Text("CATATAN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(note.ifBlank { "-" }, fontSize = 14.sp, color = Color(0xFF1D2438))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Edit", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Fungsi pembantu untuk baris rincian agar kode tidak berulang
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1D2438))
    }
}

private fun formatCurrency(value: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    return "Rp ${formatter.format(value)}"
}

private fun formatDate(timestampMillis: Long): String {
    if (timestampMillis <= 0L) return "-"
    val formatter = SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("id-ID"))
    return formatter.format(Date(timestampMillis))
}

