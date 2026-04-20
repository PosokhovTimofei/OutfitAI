package com.example.myapplication.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ClosetItemEntity::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun closetDao(): ClosetDao
}