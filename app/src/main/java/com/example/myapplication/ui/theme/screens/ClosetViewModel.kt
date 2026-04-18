package com.example.myapplication.ui.theme.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ClosetRepository
import com.example.myapplication.data.ClosetItemEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClosetViewModel(
    private val repo: ClosetRepository
) : ViewModel() {

    val items = repo.getItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(uri: String) { // 🔥 FIX NAME
        viewModelScope.launch {
            repo.addItem(
                ClosetItemEntity(
                    id = System.currentTimeMillis(),
                    imageUri = uri
                )
            )
        }
    }
}