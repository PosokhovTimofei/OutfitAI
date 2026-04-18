package com.example.myapplication.ui.theme.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.ClosetRepository

class ClosetViewModelFactory(
    private val repo: ClosetRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClosetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClosetViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}