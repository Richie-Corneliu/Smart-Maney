package com.kelompok4.smartmaney.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Int,
    val type: String,
    val category: String,
    val note: String,
    val paymentMethod: String,
    val createdAtMillis: Long
)

