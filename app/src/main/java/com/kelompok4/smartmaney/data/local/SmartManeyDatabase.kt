package com.kelompok4.smartmaney.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kelompok4.smartmaney.data.local.dao.BudgetDao
import com.kelompok4.smartmaney.data.local.dao.ProfileDao
import com.kelompok4.smartmaney.data.local.dao.TransactionDao
import com.kelompok4.smartmaney.data.local.dao.WalletDao
import com.kelompok4.smartmaney.data.local.entity.BudgetCategoryEntity
import com.kelompok4.smartmaney.data.local.entity.BudgetMetaEntity
import com.kelompok4.smartmaney.data.local.entity.ProfileEntity
import com.kelompok4.smartmaney.data.local.entity.TransactionEntity
import com.kelompok4.smartmaney.data.local.entity.WalletMetaEntity

@Database(
    entities = [
        TransactionEntity::class,
        WalletMetaEntity::class,
        ProfileEntity::class,
        BudgetMetaEntity::class,
        BudgetCategoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SmartManeyDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun walletDao(): WalletDao
    abstract fun profileDao(): ProfileDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var instance: SmartManeyDatabase? = null

        fun getInstance(context: Context): SmartManeyDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    SmartManeyDatabase::class.java,
                    "smartmaney.db"
                ).build().also { instance = it }
            }
        }
    }
}

