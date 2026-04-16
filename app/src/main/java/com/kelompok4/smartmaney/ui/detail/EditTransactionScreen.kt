package com.kelompok4.smartmaney.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    initialAmount: Int,
    initialNote: String,
    onBackClick: () -> Unit,
    onSaveClick: (Int, String) -> Unit
) {
    var nominalInput by remember(initialAmount) { mutableStateOf(initialAmount.toString()) }
    var noteInput by remember { mutableStateOf(initialNote) }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Transaksi", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Batal")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Field Input Nominal
            OutlinedTextField(
                value = nominalInput,
                onValueChange = { nominalInput = it.filter(Char::isDigit) },
                label = { Text("Nominal (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Field Input Catatan
            OutlinedTextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                label = { Text("Catatan") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.weight(1f)) // Mendorong tombol ke paling bawah

            // Tombol Simpan
            Button(
                onClick = {
                    val amount = nominalInput.toIntOrNull() ?: return@Button
                    onSaveClick(amount, noteInput)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Simpan Perubahan", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}