package com.almalika.salon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.almalika.salon.data.local.entity.MenuEntity
import com.almalika.salon.data.local.entity.HomeEntity
import com.almalika.salon.data.local.entity.ServiceCategoryEntity
import com.almalika.salon.data.local.entity.OfferEntity
import com.almalika.salon.data.local.entity.GalleryImageEntity
import com.almalika.salon.data.local.entity.BranchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SalonConfigDao {
    // ----- Menu -----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenu(menu: MenuEntity)

    @Query("SELECT * FROM menu LIMIT 1")
    fun observeMenu(): Flow<MenuEntity?>

    @Query("SELECT * FROM menu LIMIT 1")
    suspend fun getMenu(): MenuEntity?

    // ----- Home -----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHome(home: HomeEntity)

    @Query("SELECT * FROM home LIMIT 1")
    fun observeHome(): Flow<HomeEntity?>

    @Query("SELECT * FROM home LIMIT 1")
    suspend fun getHome(): HomeEntity?

    // ----- Services -----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceCategories(categories: List<ServiceCategoryEntity>)

    @Query("SELECT * FROM service_category")
    fun observeServiceCategories(): Flow<List<ServiceCategoryEntity>>

    // ----- Offers -----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffers(offers: List<OfferEntity>)

    @Query("SELECT * FROM offer")
    fun observeOffers(): Flow<List<OfferEntity>>

    // ----- Gallery -----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGalleryImages(images: List<GalleryImageEntity>)

    @Query("SELECT * FROM gallery_image")
    fun observeGalleryImages(): Flow<List<GalleryImageEntity>>

    // ----- Branches -----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranches(branches: List<BranchEntity>)

    @Query("SELECT * FROM branch")
    fun observeBranches(): Flow<List<BranchEntity>>

    @Query("DELETE FROM menu")
    suspend fun clearMenu()

    @Query("DELETE FROM home")
    suspend fun clearHome()

    @Query("DELETE FROM service_category")
    suspend fun clearServiceCategories()

    @Query("DELETE FROM offer")
    suspend fun clearOffers()

    @Query("DELETE FROM gallery_image")
    suspend fun clearGalleryImages()

    @Query("DELETE FROM branch")
    suspend fun clearBranches()

    @Transaction
    suspend fun replaceConfig(
        menu: MenuEntity,
        home: HomeEntity,
        serviceCategories: List<ServiceCategoryEntity>,
        offers: List<OfferEntity>,
        galleryImages: List<GalleryImageEntity>,
        branches: List<BranchEntity>
    ) {
        clearMenu()
        clearHome()
        clearServiceCategories()
        clearOffers()
        clearGalleryImages()
        clearBranches()

        insertMenu(menu)
        insertHome(home)
        insertServiceCategories(serviceCategories)
        insertOffers(offers)
        insertGalleryImages(galleryImages)
        insertBranches(branches)
    }
}
