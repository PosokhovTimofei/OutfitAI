package com.example.myapplication.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "Профиль",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Имя: Timofei")
        Text("Стиль: Streetwear")
        Text("Размер: M")

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { /* позже настройки */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Редактировать профиль")
        }
    }
}