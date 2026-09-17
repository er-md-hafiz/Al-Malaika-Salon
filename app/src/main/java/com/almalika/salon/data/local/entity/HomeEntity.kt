package com.almalika.salon.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Caches the home screen configuration JSON.
 */
@Entity(tableName = "home")
data class HomeEntity(
    @PrimaryKey val id: Int = 0,
    @ColumnInfo(name = "json") val json: String,
    @ColumnInfo(name = "last_updated") val lastUpdated: Long = System.currentTimeMillis()
)
