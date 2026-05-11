package com.kelompok4.smartmaney.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kelompok4.smartmaney.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY createdAtMillis DESC")
    fun observeAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY createdAtMillis DESC")
    fun observeTransactionsByType(type: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    fun observeTransactionById(id: Long): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = :type AND createdAtMillis BETWEEN :startMillis AND :endMillis")
    fun observeTotalAmountByTypeBetween(
        type: String,
        startMillis: Long,
        endMillis: Long
    ): Flow<Int>

    @Query(
        """
        SELECT category, COALESCE(SUM(amount), 0) AS total
        FROM transactions
        WHERE type = :type AND createdAtMillis BETWEEN :startMillis AND :endMillis
        GROUP BY category
        """
    )
    fun observeCategoryTotalsByTypeBetween(
        type: String,
        startMillis: Long,
        endMillis: Long
    ): Flow<List<CategoryTotalRow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(entity: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(entity: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("SELECT * FROM transactions ORDER BY createdAtMillis DESC")
    suspend fun getAllTransactions(): List<TransactionEntity>

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun countTransactions(): Long
}

data class CategoryTotalRow(
    val category: String,
    val total: Int
)

