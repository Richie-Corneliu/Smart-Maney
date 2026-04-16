package com.kelompok4.smartmaney.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 0,
    val fullName: String,
    val email: String,
    val notificationsEnabled: Boolean,
    val darkModeEnabled: Boolean
)

