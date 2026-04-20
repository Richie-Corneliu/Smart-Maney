package com.kelompok4.smartmaney.ui.suggestion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Warna kustom sesuai desain
private val BackgroundColor = Color(0xFFF8F9FA)
private val PrimaryGreen = Color(0xFF22C55E)
private val LightGreenBg = Color(0xFFDCFCE7)
private val TextDark = Color(0xFF1E293B)
private val TextGray = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionScreen(
    onBackClick: () -> Unit,
    onSetBudgetClick: () -> Unit // Jembatan menuju BudgetPlanningScreen
) {
    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Smart Insights", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            // Tombol Set Monthly Budget yang selalu menempel di bawah
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundColor)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onSetBudgetClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Set Monthly Budget", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. KARTU SPENDING TREND
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("SPENDING TREND", color = PrimaryGreen, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Pengeluaran makan naik 20%\ndibanding bulan lalu",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Naik", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kenaikan sebesar ", color = TextGray, fontSize = 14.sp)
                        Text("Rp 70.000", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { 0.7f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = PrimaryGreen,
                        trackColor = Color(0xFFF1F5F9)
                    )
                }
            }

            // 2. KELOMPOK SMART SAVING SUGGESTIONS
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Smart Saving Suggestions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)

                SuggestionItem(
                    icon = Icons.Default.DirectionsCar,
                    title = "Reduce transport spending",
                    subtitle = "Optimize daily commute costs"
                )
                SuggestionItem(
                    icon = Icons.Default.CreditCard,
                    title = "Manage subscriptions",
                    subtitle = "Cancel 2 unused services"
                )
                SuggestionItem(
                    icon = Icons.Default.VerifiedUser,
                    title = "Maintain current budget",
                    subtitle = "Health spending is on track"
                )
            }

            // 3. KARTU CATEGORY ANALYSIS (GRAFIK)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Category Analysis", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text("VIEW ALL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Komponen Custom Bar Chart
                        SimpleBarChart()

                        Spacer(modifier = Modifier.height(24.dp))

                        // Footer Detail
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Most Spent", fontSize = 12.sp, color = TextGray)
                                Text("Dining & Groceries", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total", fontSize = 12.sp, color = TextGray)
                                Text("Rp 1.420.000", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                            }
                        }
                    }
                }
            }

            // Jarak tambahan agar isi tidak tertutup tombol di bawah
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SuggestionItem(icon: ImageVector, title: String, subtitle: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(LightGreenBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = PrimaryGreen)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text(subtitle, fontSize = 12.sp, color = TextGray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Detail", tint = Color.LightGray)
        }
    }
}

// FUNGSI KHUSUS UNTUK MEMBANGUN GRAFIK BATANG TANPA LIBRARY
@Composable
private fun SimpleBarChart() {
    // Data dummy untuk persentase tinggi grafik (0.0 sampai 1.0)
    val chartData = listOf(
        Pair("Food", 0.9f),
        Pair("Bills", 0.4f),
        Pair("Transp", 0.6f),
        Pair("Shop", 0.3f),
        Pair("Other", 0.2f)
    )

    Row(
        modifier = Modifier.fillMaxWidth().height(140.dp), // Tinggi area grafik
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        chartData.forEach { (label, value) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.fillMaxHeight()
            ) {
                // Batang Grafik
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .fillMaxHeight(value) // Mengisi tinggi sesuai nilai data
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(if (label == "Food") PrimaryGreen else Color(0xFFE2E8F0)) // Food diberi highlight
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Label Teks di Bawah
                Text(label, fontSize = 10.sp, color = TextGray)
            }
        }
    }
}