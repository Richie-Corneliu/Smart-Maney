package com.kelompok4.smartmaney.data.local.preferences

import java.text.NumberFormat
import java.util.Locale

enum class CurrencyOption(
    val symbol: String,
    val displayName: String,
    val localeTag: String,
    private val symbolFirst: Boolean
) {
    IDR(
        symbol = "Rp",
        displayName = "Indonesian Rupiah (Rp)",
        localeTag = "id-ID",
        symbolFirst = true
    ),
    USD(
        symbol = "$",
        displayName = "US Dollar ($)",
        localeTag = "en-US",
        symbolFirst = true
    );

    fun format(amount: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag(localeTag))
        val number = formatter.format(amount)
        return if (symbolFirst) "$symbol $number" else "$number $symbol"
    }

    companion object {
        fun fromKey(key: String?): CurrencyOption = when (key) {
            USD.name -> USD
            else -> IDR
        }
    }
}
