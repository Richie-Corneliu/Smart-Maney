package com.kelompok4.smartmaney.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kelompok4.smartmaney.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE id = 0 LIMIT 1")
    fun observeProfile(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile WHERE id = 0 LIMIT 1")
    suspend fun getProfile(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(entity: ProfileEntity)
}


