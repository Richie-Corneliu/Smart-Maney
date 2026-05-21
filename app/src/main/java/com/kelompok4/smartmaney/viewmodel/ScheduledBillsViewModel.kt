package com.kelompok4.smartmaney.viewmodel

import androidx.lifecycle.ViewModel
import com.kelompok4.smartmaney.ui.scheduledbills.BillItem
import com.kelompok4.smartmaney.ui.scheduledbills.RepeatFrequency
import com.kelompok4.smartmaney.ui.scheduledbills.ScheduledBillsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ScheduledBillsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduledBillsUiState())
    val uiState: StateFlow<ScheduledBillsUiState> = _uiState.asStateFlow()

    init {
        loadDummyBills()
    }

    private fun loadDummyBills() {
        val now = System.currentTimeMillis()
        val upcoming = listOf(
            BillItem("1", "Bayar Kos Bulanan", 850_000, "Jatuh tempo dalam 2 hari", "Tempat Tinggal", now, null, RepeatFrequency.MONTHLY, isOverdue = false),
            BillItem("2", "Tagihan Listrik & Air", 175_000, "Terlewat 1 hari!", "Lain-lain", now, null, RepeatFrequency.MONTHLY, isOverdue = true),
            BillItem("3", "Langganan Wi-Fi Kos", 250_000, "Jatuh tempo dalam 5 hari", "Lain-lain", now, null, RepeatFrequency.MONTHLY, isOverdue = false)
        )
        val paid = listOf(
            BillItem("4", "Uang Kas Kelompok", 50_000, "Lunas pada 18 Mei", "Lain-lain", now, null, RepeatFrequency.NONE, isPaid = true)
        )
        _uiState.update { it.copy(upcomingBills = upcoming, paidBills = paid) }
    }

    fun markAsPaid(billId: String) {
        _uiState.update { currentState ->
            val targetBill = currentState.upcomingBills.find { it.id == billId } ?: return@update currentState

            // Pindah ke Sudah Bayar
            val updatedUpcoming = currentState.upcomingBills.filter { it.id != billId }
            val updatedPaid = currentState.paidBills + targetBill.copy(isPaid = true, dueDateLabel = "Lunas Baru Saja")

            // Logika Berlangganan (Auto-Reschedule)
            val nextUpcomingBills = if (targetBill.repeatFrequency != RepeatFrequency.NONE) {
                val nextDate = calculateNextDate(targetBill.startDate, targetBill.repeatFrequency)
                val newBill = targetBill.copy(
                    id = System.currentTimeMillis().toString(), // Bikin ID baru
                    isPaid = false,
                    startDate = nextDate,
                    dueDateLabel = "Mulai: ${formatDate(nextDate)}"
                )
                updatedUpcoming + newBill
            } else {
                updatedUpcoming
            }

            currentState.copy(upcomingBills = nextUpcomingBills, paidBills = updatedPaid)
        }
    }

    fun addBill(title: String, amount: Int, category: String, start: Long, end: Long?, frequency: RepeatFrequency) {
        val newBill = BillItem(
            id = System.currentTimeMillis().toString(),
            title = title,
            amount = amount,
            dueDateLabel = "Mulai: ${formatDate(start)}",
            categoryLabel = category,
            startDate = start,
            endDate = end,
            repeatFrequency = frequency
        )
        _uiState.update { it.copy(upcomingBills = it.upcomingBills + newBill) }
    }

    fun updateBill(billId: String, newTitle: String, newAmount: Int, newStart: Long, newEnd: Long?, frequency: RepeatFrequency) {
        _uiState.update { currentState ->
            val updatedUpcoming = currentState.upcomingBills.map {
                if (it.id == billId) {
                    it.copy(title = newTitle, amount = newAmount, startDate = newStart, endDate = newEnd, repeatFrequency = frequency)
                } else it
            }
            currentState.copy(upcomingBills = updatedUpcoming)
        }
    }

    private fun calculateNextDate(currentDate: Long, frequency: RepeatFrequency): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentDate }
        when (frequency) {
            RepeatFrequency.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            RepeatFrequency.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            else -> {}
        }
        return calendar.timeInMillis
    }

    private fun formatDate(timestamp: Long): String {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }
}