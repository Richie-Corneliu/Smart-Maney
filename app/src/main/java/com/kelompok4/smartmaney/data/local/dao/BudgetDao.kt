package com.kelompok4.smartmaney.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kelompok4.smartmaney.data.local.entity.BudgetCategoryEntity
import com.kelompok4.smartmaney.data.local.entity.BudgetMetaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget_meta WHERE id = 0 LIMIT 1")
    fun observeBudgetMeta(): Flow<BudgetMetaEntity?>

    @Query("SELECT * FROM budget_meta WHERE id = 0 LIMIT 1")
    suspend fun getBudgetMeta(): BudgetMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBudgetMeta(entity: BudgetMetaEntity)

    @Query("SELECT * FROM budget_category ORDER BY name ASC")
    fun observeBudgetCategories(): Flow<List<BudgetCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBudgetCategories(entities: List<BudgetCategoryEntity>)

    @Query("SELECT COUNT(*) FROM budget_category")
    suspend fun countBudgetCategories(): Long
}


