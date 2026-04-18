package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "closet_items")
data class ClosetItemEntity(
    @PrimaryKey val id: Long,
    val imageUri: String
)