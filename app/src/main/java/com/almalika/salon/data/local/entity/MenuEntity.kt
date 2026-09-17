package com.almalika.salon.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Simple entity to cache the menu configuration JSON.
 * For simplicity we store the whole JSON as a string.
 */
@Entity(tableName = "menu")
data class MenuEntity(
    @PrimaryKey val id: Int = 0,
    @ColumnInfo(name = "json") val json: String,
    @ColumnInfo(name = "last_updated") val lastUpdated: Long = System.currentTimeMillis()
)
