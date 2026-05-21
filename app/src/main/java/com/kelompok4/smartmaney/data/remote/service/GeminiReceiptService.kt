package com.kelompok4.smartmaney.data.remote.service

import android.graphics.BitmapFactory
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.kelompok4.smartmaney.data.remote.model.ReceiptData

class GeminiReceiptService(
	private val model: GenerativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
		.generativeModel(
			modelName = "gemini-3-flash-preview",
			generationConfig = generationConfig {
				responseMimeType = "application/json"
				responseSchema = receiptSchema()
			}
		)
) {
	suspend fun parseReceipt(imageBytes: ByteArray): ReceiptData {
		val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
			?: return ReceiptData.empty()
		return runCatching {
			val response = model.generateContent(
				content {
					text(PROMPT)
					image(bitmap)
				}
			)
			ReceiptData.fromJson(response.text.orEmpty())
		}.getOrElse {
			Log.w("GeminiReceiptService", "Gemini receipt parsing failed", it)
			ReceiptData.empty()
		}
	}

	companion object {
		private const val PROMPT = """
You are a financial assistant extracting data from Indonesian receipt or invoice images.

Rules:
- Set is_receipt=false if the image is not a receipt, invoice, or proof of payment.
- merchant: the store or business name printed on the receipt. Omit if not visible.
- total_amount: the grand total paid in IDR as a whole integer with no decimals (e.g. 50000 for Rp 50.000). Never multiply by 100.
- transaction_date: format as "yyyy-MM-dd HH:mm", or "yyyy-MM-dd" when time is absent. Omit if not visible.
- category: pick exactly one of these values:
    "Makanan & Minuman" — restaurants, cafes, groceries, warung, food delivery
    "Transportasi"      — fuel, parking, toll, ride-hailing (Gojek, Grab), bus, train
    "Hiburan"           — shopping, movies, games, subscriptions, fashion, electronics
    "Tempat Tinggal"    — rent, utilities, electricity, water, internet, home supplies
    "Income"            — money received (salary, transfer in, top-up)
    "Lain-lain"         — anything that does not fit the above
- payment_method: pick exactly one — "Cash", "Debit Card", "Credit Card", "E-Wallet", "QRIS", or "Bank Transfer".
  Use "E-Wallet" for GoPay, OVO, Dana, ShopeePay, LinkAja. Omit if not visible.
- note: a brief description of what was purchased (optional).
- items: list each line item. quantity may be a decimal (e.g. 0.5 for half kg). Omit the list if no itemization is visible.
"""

		private fun receiptSchema(): Schema {
			return Schema.obj(
				properties = mapOf(
					"is_receipt" to Schema.boolean("True if the image is a receipt, invoice, or proof of payment"),
					"merchant" to Schema.string("Store or business name as printed on the receipt"),
					"total_amount" to Schema.integer(
						description = "Grand total paid in IDR as a whole integer, no decimals (e.g. 50000 for Rp 50.000)",
						minimum = 0.0
					),
					"transaction_date" to Schema.string("Transaction date as yyyy-MM-dd HH:mm, or yyyy-MM-dd when time is absent"),
					"payment_method" to Schema.enumeration(
						values = listOf("Cash", "Debit Card", "Credit Card", "E-Wallet", "QRIS", "Bank Transfer"),
						description = "Use E-Wallet for GoPay, OVO, Dana, ShopeePay, LinkAja"
					),
					"category" to Schema.enumeration(
						values = listOf("Makanan & Minuman", "Transportasi", "Hiburan", "Tempat Tinggal", "Income", "Lain-lain"),
						description = "Budget category this transaction belongs to"
					),
					"note" to Schema.string("Brief description of what was purchased"),
					"items" to Schema.array(
						Schema.obj(
							properties = mapOf(
								"name" to Schema.string(),
								"quantity" to Schema.double(minimum = 0.0),
								"unit_price" to Schema.integer(minimum = 0.0),
								"line_total" to Schema.integer(minimum = 0.0)
							),
						)
					),
				),
				optionalProperties = listOf("note")
			)
		}
	}
}






