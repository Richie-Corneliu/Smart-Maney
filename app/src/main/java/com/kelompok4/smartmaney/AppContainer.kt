package com.kelompok4.smartmaney

import android.content.Context
import com.kelompok4.smartmaney.data.local.DatabaseSeeder
import com.kelompok4.smartmaney.data.local.SmartManeyDatabase
import com.kelompok4.smartmaney.data.local.preferences.CurrencyPreferenceStore
import com.kelompok4.smartmaney.data.local.preferences.ThemePreferenceStore
import com.kelompok4.smartmaney.data.remote.repository.FirestoreRepository
import com.kelompok4.smartmaney.data.remote.service.GeminiReceiptService
import com.kelompok4.smartmaney.data.repository.SmartManeyRepository

class AppContainer(context: Context) {
    private val database = SmartManeyDatabase.getInstance(context)
    private val firestoreRepository = FirestoreRepository()
    val repository: SmartManeyRepository = SmartManeyRepository(database, firestoreRepository)
    val geminiReceiptService: GeminiReceiptService = GeminiReceiptService()
    val seeder: DatabaseSeeder = DatabaseSeeder(database)
    val themePreferenceStore: ThemePreferenceStore = ThemePreferenceStore(context)
    val currencyPreferenceStore: CurrencyPreferenceStore = CurrencyPreferenceStore(context)
}

