package com.example.myapplication.ui.theme.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

@Composable
fun BottomBar(
    selected: Int,
    onSelect: (Int) -> Unit
) {
    NavigationBar {

        NavigationBarItem(
            selected = selected == 0,
            onClick = { onSelect(0) },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = selected == 1,
            onClick = { onSelect(1) },
            icon = { Icon(Icons.Default.Checkroom, null) },
            label = { Text("Closet") }
        )

        NavigationBarItem(
            selected = selected == 2,
            onClick = { onSelect(2) },
            icon = { Icon(Icons.Default.Add, null) }, // 👈 центр кнопка
            label = { Text("Create") }
        )

        NavigationBarItem(
            selected = selected == 3,
            onClick = { onSelect(3) },
            icon = { Icon(Icons.Default.FavoriteBorder, null) },
            label = { Text("Fav") }
        )

        NavigationBarItem(
            selected = selected == 4,
            onClick = { onSelect(4) },
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("Profile") }
        )
    }
}