package com.example.myapplication.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ClosetItemEntity::class,
        OutfitEntity::class   // 🔥 ДОБАВИТЬ
    ],
    version = 5 // 🔥 увеличить версию
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun closetDao(): ClosetDao
}