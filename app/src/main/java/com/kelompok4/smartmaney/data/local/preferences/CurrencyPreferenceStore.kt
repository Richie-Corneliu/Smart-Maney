package com.kelompok4.smartmaney.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart

class CurrencyPreferenceStore(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val currency: Flow<CurrencyOption> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
            if (key == KEY_CURRENCY) {
                trySend(CurrencyOption.fromKey(sp.getString(KEY_CURRENCY, null)))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
        .onStart { emit(CurrencyOption.fromKey(prefs.getString(KEY_CURRENCY, null))) }
        .distinctUntilChanged()

    fun currentCurrency(): CurrencyOption =
        CurrencyOption.fromKey(prefs.getString(KEY_CURRENCY, null))

    fun setCurrency(option: CurrencyOption) {
        prefs.edit().putString(KEY_CURRENCY, option.name).apply()
    }

    companion object {
        private const val PREFS_NAME = "smart_maney_settings"
        private const val KEY_CURRENCY = "currency"
    }
}
