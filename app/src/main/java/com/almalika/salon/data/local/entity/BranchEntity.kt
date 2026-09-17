package com.almalika.salon.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "branch")
data class BranchEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "json") val json: String,
    @ColumnInfo(name = "last_updated") val lastUpdated: Long = System.currentTimeMillis()
)
