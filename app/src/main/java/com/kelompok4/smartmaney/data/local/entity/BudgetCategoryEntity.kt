package com.kelompok4.smartmaney.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_category")
data class BudgetCategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val allocated: Int
)

