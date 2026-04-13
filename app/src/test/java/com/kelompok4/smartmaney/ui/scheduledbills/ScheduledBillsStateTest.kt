package com.kelompok4.smartmaney.ui.scheduledbills

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduledBillsStateTest {

    @Test
    fun billStatus_returnsExpectedBuckets() {
        val now = 1_000_000L

        val overdue = ScheduledBill(
            id = "a",
            title = "A",
            amount = 10,
            dueAtMillis = now - 1,
            frequency = BillFrequency.Monthly,
            autoPay = false
        )
        val dueSoon = overdue.copy(id = "b", dueAtMillis = now + 1_000L)
        val upcoming = overdue.copy(id = "c", dueAtMillis = now + DUE_SOON_WINDOW_MILLIS + 1)

        assertEquals(BillStatus.Overdue, billStatus(overdue, now))
        assertEquals(BillStatus.DueSoon, billStatus(dueSoon, now))
        assertEquals(BillStatus.Upcoming, billStatus(upcoming, now))
    }

    @Test
    fun reduceScheduledBillsState_markPaid_advancesDueDateAndUpdatesLastPaid() {
        val now = 10L * 24L * 60L * 60L * 1000L
        val bill = ScheduledBill(
            id = "bill",
            title = "Internet",
            amount = 200,
            dueAtMillis = now - 24L * 60L * 60L * 1000L,
            frequency = BillFrequency.Weekly,
            autoPay = true
        )
        val state = ScheduledBillsUiState(nowMillis = now, bills = listOf(bill))

        val updated = reduceScheduledBillsState(state, ScheduledBillsAction.MarkPaid("bill"))
        val updatedBill = updated.bills.first()

        assertTrue(updatedBill.dueAtMillis > now)
        assertEquals(now, updatedBill.lastPaidAtMillis)
    }

    @Test
    fun reduceScheduledBillsState_snoozeAndToggleAutoPay_updatesTargetBill() {
        val bill = ScheduledBill(
            id = "bill",
            title = "Electricity",
            amount = 500,
            dueAtMillis = 3_000_000L,
            frequency = BillFrequency.Monthly,
            autoPay = false
        )
        val state = ScheduledBillsUiState(nowMillis = 2_000_000L, bills = listOf(bill))

        val toggled = reduceScheduledBillsState(state, ScheduledBillsAction.ToggleAutoPay("bill"))
        assertTrue(toggled.bills.first().autoPay)

        val snoozed = reduceScheduledBillsState(toggled, ScheduledBillsAction.Snooze("bill", days = 2))
        assertEquals(bill.dueAtMillis + 2L * 24L * 60L * 60L * 1000L, snoozed.bills.first().dueAtMillis)
        assertNotNull(snoozed.bills.first())
        assertFalse(state.bills.first().autoPay)
    }
}

