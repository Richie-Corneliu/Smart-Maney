package com.kelompok4.smartmaney.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_meta")
data class BudgetMetaEntity(
    @PrimaryKey val id: Int = 0,
    val totalBudget: Int
)

