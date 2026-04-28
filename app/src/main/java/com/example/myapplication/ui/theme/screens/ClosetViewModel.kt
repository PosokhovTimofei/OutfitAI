package com.example.myapplication.ui.theme.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ClosetRepository
import com.example.myapplication.data.ClosetItemEntity
import com.example.myapplication.data.OutfitEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.google.gson.Gson



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
        label: String,
        brand: String,
        material: String,
        price: String,
        color: String
    ) {
        viewModelScope.launch {
            repo.addItem(
                ClosetItemEntity(
                    id = System.currentTimeMillis(),
                    imageUri = uri,
                    type = type,
                    category = category,
                    style = style,
                    label = label,
                    brand = brand,
                    material = material,
                    price = price,
                    color = color
                )
            )
        }
    }
    fun delete(item: ClosetItemEntity) {
        viewModelScope.launch {

            val file = java.io.File(item.imageUri)
            if (file.exists()) file.delete()

            repo.deleteItem(item)
        }
    }

    fun updateItem(item: ClosetItemEntity) {
        viewModelScope.launch {
            repo.updateItem(item)
        }
    }

    val outfits = repo.getOutfits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveOutfit(
        itemIds: List<Long>,
        states: List<OutfitItemState>,
        previewUri: String?
    ) {
        viewModelScope.launch {

            val json = Gson().toJson(states)

            repo.addOutfit(
                OutfitEntity(
                    id = System.currentTimeMillis(),
                    itemIds = itemIds.joinToString(","),
                    layoutJson = json,
                    previewUri = previewUri,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateOutfit(outfit: OutfitEntity) {
        viewModelScope.launch {
            repo.updateOutfit(outfit)
        }
    }

    fun deleteOutfit(outfit: OutfitEntity) {
        viewModelScope.launch {
            repo.deleteOutfit(outfit)
        }
    }
}

