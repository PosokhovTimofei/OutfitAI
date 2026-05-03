package com.example.myapplication.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ClosetItemEntity::class,
        OutfitEntity::class,
        SettingsEntity::class
    ],
    version = 6
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun closetDao(): ClosetDao
}