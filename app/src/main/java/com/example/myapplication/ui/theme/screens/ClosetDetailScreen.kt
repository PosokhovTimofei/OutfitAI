package com.example.myapplication.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.MyApp
import java.io.File

@Composable
fun ClosetDetailScreen(
    itemId: Long,
    navController: NavController,
    vm: ClosetViewModel = viewModel(
        factory = ClosetViewModelFactory(
            (LocalContext.current.applicationContext as MyApp).repo
        )
    )
) {

    val items by vm.items.collectAsState()
    val item = items.find { it.id == itemId }

    if (item == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Не найдено")
        }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Button(onClick = { navController.popBackStack() }) {
            Text("← Назад")
        }

        Spacer(Modifier.height(16.dp))

        AsyncImage(
            model = File(item.imageUri),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(16.dp))

        Text("Название: ${item.label}", style = MaterialTheme.typography.titleLarge)
        Text("Тип: ${item.type}")
        Text("Категория: ${item.category}")
        Text("Стиль: ${item.style}")
    }
}