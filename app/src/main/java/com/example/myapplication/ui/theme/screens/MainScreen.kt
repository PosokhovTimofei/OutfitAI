package com.example.myapplication.ui.theme.screens

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.myapplication.ui.theme.components.BottomBar

@Composable
fun MainScreen(
    navController: NavController
) {

    var tab by remember { mutableStateOf(1) }

    Scaffold(
        bottomBar = {
            BottomBar(selected = tab, onSelect = { tab = it })
        }
    ) { padding ->

        when (tab) {
            0 -> MarketplaceScreen(Modifier.padding(padding))
            1 -> ClosetScreen(
                navController = navController,
                modifier = Modifier.padding(padding)
            )
            2 -> GenerateOutfitScreen(Modifier.padding(padding))
            3 -> FavoritesScreen(
                navController = navController,
                Modifier.padding(padding))
            4 -> ProfileScreen(Modifier.padding(padding))
        }
    }
}