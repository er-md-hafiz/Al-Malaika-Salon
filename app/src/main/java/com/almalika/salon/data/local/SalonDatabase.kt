package com.almalika.salon.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.almalika.salon.data.local.dao.SalonConfigDao
import com.almalika.salon.data.local.entity.MenuEntity
import com.almalika.salon.data.local.entity.HomeEntity
import com.almalika.salon.data.local.entity.ServiceCategoryEntity
import com.almalika.salon.data.local.entity.OfferEntity
import com.almalika.salon.data.local.entity.GalleryImageEntity
import com.almalika.salon.data.local.entity.BranchEntity

@Database(
    entities = [
        MenuEntity::class,
        HomeEntity::class,
        ServiceCategoryEntity::class,
        OfferEntity::class,
        GalleryImageEntity::class,
        BranchEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SalonDatabase : RoomDatabase() {
    abstract fun salonConfigDao(): SalonConfigDao
}
