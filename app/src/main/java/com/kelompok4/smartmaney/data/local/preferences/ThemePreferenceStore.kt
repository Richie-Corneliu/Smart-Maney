package com.kelompok4.smartmaney.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart

enum class ThemeMode {
    SYSTEM, LIGHT, DARK;

    companion object {
        fun fromKey(key: String?): ThemeMode = when (key) {
            LIGHT.name -> LIGHT
            DARK.name -> DARK
            else -> SYSTEM
        }
    }
}

class ThemePreferenceStore(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val themeMode: Flow<ThemeMode> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
            if (key == KEY_THEME_MODE) {
                trySend(ThemeMode.fromKey(sp.getString(KEY_THEME_MODE, null)))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
        .onStart { emit(ThemeMode.fromKey(prefs.getString(KEY_THEME_MODE, null))) }
        .distinctUntilChanged()

    fun currentThemeMode(): ThemeMode =
        ThemeMode.fromKey(prefs.getString(KEY_THEME_MODE, null))

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    companion object {
        private const val PREFS_NAME = "smart_maney_settings"
        private const val KEY_THEME_MODE = "theme_mode"
    }
}
