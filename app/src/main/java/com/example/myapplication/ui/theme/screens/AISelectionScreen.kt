package com.example.myapplication.ui.theme.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp

@Composable
fun GenerateOutfitScreen(modifier: Modifier = Modifier) {

    var style by remember { mutableStateOf("") }
    var event by remember { mutableStateOf("") }

    // пока заглушка погоды
    val weather = "🌧 Дождь, +12°C"

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column {

            // Погода (авто)
            OutlinedTextField(
                value = weather,
                onValueChange = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Погода") }
            )

            Spacer(Modifier.height(16.dp))

            // Стиль
            OutlinedTextField(
                value = style,
                onValueChange = { style = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Стиль") }
            )

            Spacer(Modifier.height(16.dp))

            // Мероприятие
            OutlinedTextField(
                value = event,
                onValueChange = { event = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Мероприятие") }
            )
        }

        // Кнопка
        Button(
            onClick = {
                // тут потом AI
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text("Создать образ")
        }
    }
}