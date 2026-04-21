package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ClosetDao {

    @Query("SELECT * FROM closet_items ORDER BY id DESC")
    fun getAll(): Flow<List<ClosetItemEntity>>

    @Insert
    suspend fun insert(item: ClosetItemEntity)

    @androidx.room.Delete
    suspend fun delete(item: ClosetItemEntity)
}