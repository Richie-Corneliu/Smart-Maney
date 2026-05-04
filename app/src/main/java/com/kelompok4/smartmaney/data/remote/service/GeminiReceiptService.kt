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
		private const val PROMPT = """You are a helpful assistant that extracts structured data from receipt images.
Guidelines:
- Use numbers without currency symbols.
- Use transaction_date format yyyy-MM-dd HH:mm if available; else yyyy-MM-dd.
- Fill missing fields with empty string or null.
"""

		private fun receiptSchema(): Schema {
			return Schema.obj(
				properties = mapOf(
					"merchant" to Schema.string(),
					"total_amount" to Schema.integer(),
					"transaction_date" to Schema.string(),
					"payment_method" to Schema.string(),
					"category" to Schema.string(),
					"note" to Schema.string(),
					"items" to Schema.array(
						Schema.obj(
							properties = mapOf(
								"name" to Schema.string(),
								"quantity" to Schema.integer(),
								"unit_price" to Schema.integer(),
								"line_total" to Schema.integer()
							),
							optionalProperties = listOf("quantity", "unit_price", "line_total")
						)
					)
				),
				optionalProperties = listOf(
					"merchant",
					"total_amount",
					"transaction_date",
					"payment_method",
					"category",
					"note",
					"items"
				)
			)
		}
	}
}






