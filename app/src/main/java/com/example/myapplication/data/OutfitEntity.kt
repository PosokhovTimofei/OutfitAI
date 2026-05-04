package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outfits")
data class OutfitEntity(
    @PrimaryKey val id: Long,
    val itemIds: String,      // "1,2,3"
    val layoutJson: String,   // позиции + scale (JSON)
    val previewUri: String? = null,
    val style: String,
    val createdAt: Long = System.currentTimeMillis()
)