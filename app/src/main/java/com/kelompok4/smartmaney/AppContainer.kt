package com.kelompok4.smartmaney

import android.content.Context
import com.kelompok4.smartmaney.data.local.DatabaseSeeder
import com.kelompok4.smartmaney.data.local.SmartManeyDatabase
import com.kelompok4.smartmaney.data.remote.service.GeminiReceiptService
import com.kelompok4.smartmaney.data.repository.SmartManeyRepository

class AppContainer(context: Context) {
    private val database = SmartManeyDatabase.getInstance(context)
    val repository: SmartManeyRepository = SmartManeyRepository(database)
    val geminiReceiptService: GeminiReceiptService = GeminiReceiptService()
    val seeder: DatabaseSeeder = DatabaseSeeder(database)
}

