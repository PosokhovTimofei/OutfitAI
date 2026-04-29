package com.example.myapplication.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.MyApp
import java.io.File
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Tune

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.ui.graphics.Color


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
    val item = items.find { it.id == itemId } ?: return

    // ================= STATE =================
    var name by remember { mutableStateOf(item.label) }
    var type by remember { mutableStateOf(item.type) }
    var category by remember { mutableStateOf(item.category) }
    var style by remember { mutableStateOf(item.style) }

    var brand by remember { mutableStateOf(item.brand ?: "") }
    var price by remember { mutableStateOf(item.price ?: "") }

    var color by remember { mutableStateOf(item.color ?: "Черный") }
    var material by remember { mutableStateOf(item.material ?: "Хлопок") }

    val imagePath = item.imageUri

    // ================= OPTIONS =================
    val colorOptions = listOf(
        "Черный" to Color.Black,
        "Белый" to Color.White,
        "Синий" to Color(0xFF2196F3),
        "Красный" to Color(0xFFF44336),
        "Зеленый" to Color(0xFF4CAF50),
        "Желтый" to Color(0xFFFFEB3B)
    )

    val materialOptions = listOf(
        "Хлопок", "Шерсть", "Деним", "Кожа", "Лён", "Синтетика"
    )

    var currentOptions by remember { mutableStateOf(listOf<String>()) }
    var onSelect by remember { mutableStateOf<(String) -> Unit>({}) }
    var showSheet by remember { mutableStateOf(false) }

    if (showSheet) {
        SelectBottomSheet(
            options = currentOptions,
            onSelect = {
                onSelect(it)
                showSheet = false
            },
            onDismiss = { showSheet = false }
        )
    }

    // ================= LAYOUT =================
    Scaffold(
        bottomBar = {
            Button(
                onClick = {
                    vm.updateItem(
                        item.copy(
                            label = name,
                            type = type,
                            category = category,
                            style = style,
                            brand = brand,
                            price = price,
                            color = color,
                            material = material,
                            imageUri = imagePath
                        )
                    )
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()

            ) {
                Text("Сохранить изменения")
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            // ================= IMAGE =================
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {

                    AsyncImage(
                        model = File(imagePath),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )

                    IconButton(
                        onClick = { navController.navigate("editor/$itemId") },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(Icons.Default.Edit, null)
                    }
                }
            }

            // ================= CONTENT =================
            item {
                Column(Modifier.padding(16.dp)) {

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Название") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    SectionCard("Категория", Icons.Default.Checkroom) {

                        SelectRow("Тип", type, Icons.Default.Style) {
                            currentOptions = typeOptionsRu
                            onSelect = { type = it }
                            showSheet = true
                        }

                        SelectRow("Раздел", category, Icons.Default.Category) {
                            currentOptions = categoryOptionsRu
                            onSelect = { category = it }
                            showSheet = true
                        }

                        SelectRow("Стиль", style, Icons.Default.AutoAwesome) {
                            currentOptions = styleOptionsRu
                            onSelect = { style = it }
                            showSheet = true
                        }

                        // ===== COLOR =====
                        SelectRow("Цвет", color, Icons.Default.Tune) {
                            currentOptions = colorOptions.map { it.first }
                            onSelect = { color = it }
                            showSheet = true
                        }

                        val selectedColor =
                            colorOptions.find { it.first == color }?.second ?: Color.Black

                        Box(
                            modifier = Modifier
                                .padding(start = 16.dp, top = 4.dp, bottom = 8.dp)
                                .size(24.dp)
                                .background(selectedColor, CircleShape)
                                .border(1.dp, Color.Gray, CircleShape)
                        )

                        // ===== MATERIAL =====
                        SelectRow("Материал", material, Icons.Default.Tune) {
                            currentOptions = materialOptions
                            onSelect = { material = it }
                            showSheet = true
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    SectionCard("Дополнительно", Icons.Default.Tune) {

                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("Бренд") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Цена") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(80.dp)) // 👈 чтобы контент не прятался под кнопкой
                }
            }
        }
    }
}