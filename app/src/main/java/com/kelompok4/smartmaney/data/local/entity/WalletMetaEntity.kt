package com.kelompok4.smartmaney.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet_meta")
data class WalletMetaEntity(
    @PrimaryKey val id: Int = 0,
    val initialBalance: Int
)

