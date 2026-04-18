package com.example.myapplication.ui.theme.screens

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClosetViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("closet", 0)

    val items = mutableStateListOf<ClosetItem>()

    init {
        loadItems()
    }

    fun addItem(uri: String) {
        val item = ClosetItem(
            id = System.currentTimeMillis(),
            imageUri = uri
        )

        items.add(item)
        saveItems()
    }

    private fun saveItems() {
        val data = items.joinToString("|") { "${it.id},${it.imageUri}" }
        prefs.edit().putString("items", data).apply()
    }

    private fun loadItems() {
        val data = prefs.getString("items", "") ?: return

        if (data.isEmpty()) return

        val list = data.split("|").mapNotNull {
            val parts = it.split(",")
            if (parts.size == 2) {
                ClosetItem(parts[0].toLong(), parts[1])
            } else null
        }

        items.addAll(list)
    }
}