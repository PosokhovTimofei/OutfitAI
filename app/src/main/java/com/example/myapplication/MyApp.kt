package com.example.myapplication

import android.app.Application
import androidx.room.Room
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.ClosetRepository

class MyApp : Application() {

    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "closet_db"
        ).build()
    }

    val repo by lazy {
        ClosetRepository(database.closetDao())
    }
}