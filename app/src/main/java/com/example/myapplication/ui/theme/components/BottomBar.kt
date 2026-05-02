package com.example.myapplication.ui.theme.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.unit.dp

@Composable
fun BottomBar(
    selected: Int,
    onSelect: (Int) -> Unit
) {
    NavigationBar(
        containerColor = androidx.compose.ui.graphics.Color.White,
        tonalElevation = 0.dp
    ) {

        val black = androidx.compose.ui.graphics.Color.Black
        val grayBg = androidx.compose.ui.graphics.Color(0xFFF2F2F2)

        NavigationBarItem(
            selected = selected == 0,
            onClick = { onSelect(0) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Главная") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = black,
                unselectedIconColor = black,
                selectedTextColor = black,
                unselectedTextColor = black,
                indicatorColor = grayBg
            )
        )

        NavigationBarItem(
            selected = selected == 1,
            onClick = { onSelect(1) },
            icon = { Icon(Icons.Default.Checkroom, contentDescription = null) },
            label = { Text("Гардероб") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = black,
                unselectedIconColor = black,
                selectedTextColor = black,
                unselectedTextColor = black,
                indicatorColor = grayBg
            )
        )

        NavigationBarItem(
            selected = selected == 2,
            onClick = { onSelect(2) },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            label = { Text("Создать") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = black,
                unselectedIconColor = black,
                selectedTextColor = black,
                unselectedTextColor = black,
                indicatorColor = grayBg
            )
        )

        NavigationBarItem(
            selected = selected == 3,
            onClick = { onSelect(3) },
            icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = null) },
            label = { Text("Образы") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = black,
                unselectedIconColor = black,
                selectedTextColor = black,
                unselectedTextColor = black,
                indicatorColor = grayBg
            )
        )

        NavigationBarItem(
            selected = selected == 4,
            onClick = { onSelect(4) },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Профиль") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = black,
                unselectedIconColor = black,
                selectedTextColor = black,
                unselectedTextColor = black,
                indicatorColor = grayBg
            )
        )
    }
}