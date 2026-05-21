package com.kelompok4.smartmaney.ui.scheduledbills

enum class RepeatFrequency { NONE, WEEKLY, MONTHLY }

data class BillItem(
    val id: String,
    val title: String,
    val amount: Int,
    val dueDateLabel: String,
    val categoryLabel: String,
    val startDate: Long,
    val endDate: Long?,
    val repeatFrequency: RepeatFrequency = RepeatFrequency.NONE,
    val isOverdue: Boolean = false,
    val isPaid: Boolean = false
)

data class ScheduledBillsUiState(
    val upcomingBills: List<BillItem> = emptyList(),
    val paidBills: List<BillItem> = emptyList()
)