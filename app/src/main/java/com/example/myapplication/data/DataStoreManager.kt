package com.example.myapplication.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore("closet")

class DataStoreManager(private val context: Context) {

    private val KEY = stringSetPreferencesKey("images")

    suspend fun saveImages(uris: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[KEY] = uris.toSet()
        }
    }

    suspend fun loadImages(): List<String> {
        val prefs = context.dataStore.data.first()
        return prefs[KEY]?.toList() ?: emptyList()
    }
}