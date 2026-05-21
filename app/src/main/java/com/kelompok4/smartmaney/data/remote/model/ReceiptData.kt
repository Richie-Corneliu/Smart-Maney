package com.kelompok4.smartmaney.data.remote.model

import org.json.JSONArray
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

data class ReceiptItem(
	val name: String,
	val quantity: Double?,
	val unitPrice: Int?,
	val lineTotal: Int?
)

data class ReceiptData(
	val isReceipt: Boolean,
	val merchantName: String?,
	val totalAmount: Int?,
	val transactionDateMillis: Long?,
	val paymentMethod: String?,
	val category: String?,
	val note: String?,
	val items: List<ReceiptItem>
) {
	companion object {
		fun empty(): ReceiptData {
			return ReceiptData(
				isReceipt = false,
				merchantName = null,
				totalAmount = null,
				transactionDateMillis = null,
				paymentMethod = null,
				category = null,
				note = null,
				items = emptyList()
			)
		}

		fun fromJson(rawText: String): ReceiptData {
			val jsonText = extractJsonObject(rawText) ?: return empty()
			return runCatching {
				val json = JSONObject(jsonText)
				if (!json.optBoolean("is_receipt", true)) return empty()
				val merchant = json.optString("merchant", "").ifBlank { null }
				val total = json.optString("total_amount", "").toIntOrNull()
				val payment = json.optString("payment_method", "").ifBlank { null }
				val category = json.optString("category", "").ifBlank { null }
				val note = json.optString("note", "").ifBlank { null }
				val dateText = json.optString("transaction_date", "").ifBlank { null }
				val dateMillis = parseDateMillis(dateText)
				val items = json.optJSONArray("items")?.toReceiptItems().orEmpty()
				ReceiptData(
					isReceipt = true,
					merchantName = merchant,
					totalAmount = total,
					transactionDateMillis = dateMillis,
					paymentMethod = payment,
					category = category,
					note = note,
					items = items
				)
			}.getOrElse { empty() }
		}

		private fun JSONArray.toReceiptItems(): List<ReceiptItem> {
			return (0 until length()).mapNotNull { index ->
				val item = optJSONObject(index) ?: return@mapNotNull null
				val name = item.optString("name", "").ifBlank { null } ?: return@mapNotNull null
				ReceiptItem(
					name = name,
					quantity = item.optString("quantity", "").toDoubleOrNull(),
					unitPrice = item.optString("unit_price", "").toIntOrNull(),
					lineTotal = item.optString("line_total", "").toIntOrNull()
				)
			}
		}

		private fun extractJsonObject(rawText: String): String? {
			val start = rawText.indexOf('{')
			val end = rawText.lastIndexOf('}')
			if (start == -1 || end == -1 || end <= start) return null
			return rawText.substring(start, end + 1)
		}

		private fun parseDateMillis(dateText: String?): Long? {
			if (dateText.isNullOrBlank()) return null
			val patterns = listOf(
				"yyyy-MM-dd HH:mm",
				"yyyy-MM-dd'T'HH:mm:ss",
				"yyyy-MM-dd'T'HH:mm",
				"yyyy-MM-dd",
				"dd/MM/yyyy HH:mm",
				"dd/MM/yyyy"
			)
			for (pattern in patterns) {
				try {
					val formatter = SimpleDateFormat(pattern, Locale.getDefault())
					formatter.isLenient = true
					val date = formatter.parse(dateText)
					if (date != null) return date.time
				} catch (_: ParseException) {
					// Try the next pattern.
				}
			}
			return null
		}
	}
}

