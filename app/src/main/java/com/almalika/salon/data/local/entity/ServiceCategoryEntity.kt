package com.almalika.salon.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Caches a service category (including its list of services) as JSON.
 */
@Entity(tableName = "service_category")
data class ServiceCategoryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "json") val json: String,
    @ColumnInfo(name = "last_updated") val lastUpdated: Long = System.currentTimeMillis()
)
