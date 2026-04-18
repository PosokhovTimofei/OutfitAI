package com.example.myapplication.ui.theme.screens


import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import com.example.myapplication.ui.theme.components.BottomBar

@Composable
fun MainScreen() {

    var tab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomBar(selected = tab, onSelect = { tab = it })
        }
    ) { padding ->

        when (tab) {
            0 -> MarketplaceScreen(Modifier.padding(padding)) // HOME
            1 -> ClosetScreen(Modifier.padding(padding))
            2 -> GenerateOutfitScreen(Modifier.padding(padding)) // центр кнопка
            3 -> FavoritesScreen(Modifier.padding(padding))
            4 -> ProfileScreen(Modifier.padding(padding))
        }
    }
}
