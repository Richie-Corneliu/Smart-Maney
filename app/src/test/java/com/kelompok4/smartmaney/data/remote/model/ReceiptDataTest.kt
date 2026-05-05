package com.kelompok4.smartmaney.data.remote.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ReceiptDataTest {
    @Test
    fun fromJson_parsesExpectedFields() {
        val raw = """
            {
              "is_receipt": true,
              "merchant": "Sample Market",
              "total_amount": 120000,
              "transaction_date": "2026-05-04 13:05",
              "payment_method": "Card",
              "category": "Food",
              "note": "Lunch",
              "items": [
                {"name": "Rice", "quantity": 2, "unit_price": 15000, "line_total": 30000},
                {"name": "Tea", "quantity": 1, "unit_price": 5000, "line_total": 5000}
              ]
            }
        """.trimIndent()

        val parsed = ReceiptData.fromJson(raw)

        assertEquals(true, parsed.isReceipt)
        assertEquals("Sample Market", parsed.merchantName)
        assertEquals(120000, parsed.totalAmount)
        assertEquals("Card", parsed.paymentMethod)
        assertEquals("Food", parsed.category)
        assertEquals("Lunch", parsed.note)
        assertEquals(2, parsed.items.size)
        assertNotNull(parsed.transactionDateMillis)
    }

    @Test
    fun fromJson_nonReceipt_returnsEmpty() {
        val raw = """{"is_receipt": false}"""
        val parsed = ReceiptData.fromJson(raw)
        assertEquals(false, parsed.isReceipt)
        assertEquals(null, parsed.merchantName)
        assertEquals(null, parsed.totalAmount)
    }
}

