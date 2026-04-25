package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "closet_items")
data class ClosetItemEntity(
    @PrimaryKey val id: Long,
    val imageUri: String,

    val type: String,        // shirt, jeans...
    val category: String,    // top, bottom, shoes, hat
    val style: String,       // streetwear, classic...
    val label: String,        // "Gucci кепка"
    val brand: String? = null,
    val material: String? = null,
    val price: String? = null,
    val color: String? = null
)