package com.kelompok4.smartmaney.data.export

import com.kelompok4.smartmaney.data.local.entity.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    private val HEADER = listOf(
        "id", "title", "amount", "type", "category", "note", "paymentMethod", "createdAt"
    )

    fun buildCsv(transactions: List<TransactionEntity>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val sb = StringBuilder()
        sb.append(HEADER.joinToString(",")).append('\n')
        for (t in transactions) {
            sb.append(
                listOf(
                    t.id.toString(),
                    escape(t.title),
                    t.amount.toString(),
                    escape(t.type),
                    escape(t.category),
                    escape(t.note),
                    escape(t.paymentMethod),
                    escape(dateFormat.format(Date(t.createdAtMillis)))
                ).joinToString(",")
            ).append('\n')
        }
        return sb.toString()
    }

    fun buildFilename(nowMillis: Long = System.currentTimeMillis()): String {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(nowMillis))
        return "smart_maney_transactions_$stamp.csv"
    }

    private fun escape(value: String): String {
        val needsQuoting = value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        if (!needsQuoting) return value
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
