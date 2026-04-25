package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "closet_items")
data class ClosetItemEntity(
    @PrimaryKey val id: Long,
    val imageUri: String,

    val type: String,
    val category: String,
    val style: String,
    val label: String,
    val brand: String? = null,
    val material: String? = null,
    val price: String? = null,
    val color: String? = null
)