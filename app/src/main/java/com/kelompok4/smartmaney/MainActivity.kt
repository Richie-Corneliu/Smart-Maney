package com.kelompok4.smartmaney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.kelompok4.smartmaney.data.local.preferences.CurrencyOption
import com.kelompok4.smartmaney.data.local.preferences.ThemeMode
import com.kelompok4.smartmaney.navigation.AppNavHost
import com.kelompok4.smartmaney.ui.theme.LocalCurrency
import com.kelompok4.smartmaney.ui.theme.SmartManeyTheme
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private val appContainer: AppContainer by lazy {
        AppContainer(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
           appContainer.seeder.seedIfEmpty()
        }
        setContent {
            val themeMode by appContainer.themePreferenceStore.themeMode
                .collectAsState(initial = ThemeMode.SYSTEM)
            val currency by appContainer.currencyPreferenceStore.currency
                .collectAsState(initial = CurrencyOption.IDR)
            SmartManeyTheme(themeMode = themeMode) {
                CompositionLocalProvider(LocalCurrency provides currency) {
                    AppNavHost(Modifier.fillMaxSize(), appContainer)
                }
            }
        }
    }
}
