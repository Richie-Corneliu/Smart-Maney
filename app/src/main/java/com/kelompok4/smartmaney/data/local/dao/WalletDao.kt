package com.kelompok4.smartmaney.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kelompok4.smartmaney.data.local.entity.WalletMetaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_meta WHERE id = 0 LIMIT 1")
    fun observeWalletMeta(): Flow<WalletMetaEntity?>

    @Query("SELECT * FROM wallet_meta WHERE id = 0 LIMIT 1")
    suspend fun getWalletMeta(): WalletMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWalletMeta(entity: WalletMetaEntity)
}

