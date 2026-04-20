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
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // ✅ теперь доступен из UI
    fun add(
        uri: String,
        type: String,
        category: String,
        style: String,
        label: String
    ) {
        viewModelScope.launch {
            repo.addItem(
                ClosetItemEntity(
                    id = System.currentTimeMillis(),
                    imageUri = uri,
                    type = type,
                    category = category,
                    style = style,
                    label = label
                )
            )
        }
    }
}