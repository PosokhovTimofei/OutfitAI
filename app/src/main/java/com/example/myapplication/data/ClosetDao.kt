package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClosetDao {

    // ================= CLOSET ITEMS =================
    @Query("SELECT * FROM closet_items ORDER BY id DESC")
    fun getAll(): Flow<List<ClosetItemEntity>>

    @Insert
    suspend fun insert(item: ClosetItemEntity)

    @Delete
    suspend fun delete(item: ClosetItemEntity)

    @Update
    suspend fun updateItem(item: ClosetItemEntity)


    // ================= OUTFITS (НОВОЕ) =================

    @Query("SELECT * FROM outfits ORDER BY createdAt DESC")
    fun getOutfits(): Flow<List<OutfitEntity>>

    @Insert
    suspend fun insertOutfit(outfit: OutfitEntity)

    @Delete
    suspend fun deleteOutfit(outfit: OutfitEntity)

    @Update
    suspend fun updateOutfit(outfit: OutfitEntity)

    @Query("SELECT profileName FROM settings WHERE id = 0")
    fun getProfileName(): Flow<String?>

    @Insert
    suspend fun insertSettings(settings: SettingsEntity)

    @Update
    suspend fun updateSettings(settings: SettingsEntity)
}