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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector


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

    var name by remember { mutableStateOf(item.label) }
    var type by remember { mutableStateOf(item.type) }
    var category by remember { mutableStateOf(item.category) }
    var style by remember { mutableStateOf(item.style) }

    var brand by remember { mutableStateOf(item.brand ?: "") }
    var price by remember { mutableStateOf(item.price ?: "") }

    var color by remember { mutableStateOf(item.color ?: "Черный") }
    var material by remember { mutableStateOf(item.material ?: "Хлопок") }

    val imagePath = item.imageUri

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

    fun save() {
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
    }

    Scaffold(
        bottomBar = {
            Surface(
                color = Color.White,   // 💥 ВАЖНО: фиксируем белый фон
                shadowElevation = 10.dp
            ) {
                ElevatedButton(
                    onClick = { save() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Checkroom, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Сохранить изменения")
                }
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
                        .height(360.dp)
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background(Color(0xFFF5F5F5))
                ) {

                    AsyncImage(
                        model = File(imagePath),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    IconButton(
                        onClick = { navController.navigate("editor/$itemId") },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(Color.Transparent)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.Black // или Color.Gray для мягкого UI
                        )
                    }
                }
            }

            // ================= CONTENT =================
            item {
                Column(Modifier.padding(16.dp)) {

                    ModernTextField(
                        value = name,
                        onChange = { name = it },
                        label = "Название"
                    )

                    Spacer(Modifier.height(16.dp))

                    SectionCardModern("Категория", Icons.Default.Checkroom) {
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
                    }

                    Spacer(Modifier.height(16.dp))

                    SectionCardModern("Внешний вид", Icons.Default.Tune) {

                        SelectRow("Цвет", color, Icons.Default.Tune) {
                            currentOptions = colorOptions.map { it.first }
                            onSelect = { color = it }
                            showSheet = true
                        }

                        val selectedColor =
                            colorOptions.find { it.first == color }?.second ?: Color.Black

                        Box(
                            Modifier
                                .padding(start = 12.dp, top = 4.dp, bottom = 8.dp)
                                .size(22.dp)
                                .background(selectedColor, CircleShape)
                                .border(1.dp, Color.LightGray, CircleShape)
                        )

                        SelectRow("Материал", material, Icons.Default.Tune) {
                            currentOptions = materialOptions
                            onSelect = { material = it }
                            showSheet = true
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    SectionCardModern("Дополнительно", Icons.Default.Tune) {

                        ModernTextField(
                            value = brand,
                            onChange = { brand = it },
                            label = "Бренд"
                        )

                        Spacer(Modifier.height(10.dp))

                        ModernTextField(
                            value = price,
                            onChange = { price = it },
                            label = "Цена"
                        )
                    }

                    Spacer(Modifier.height(90.dp))
                }
            }
        }
    }
}

@Composable
fun ModernTextField(
    value: String,
    onChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),

        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Black,
            unfocusedBorderColor = Color.LightGray,
            cursorColor = Color.Black,

            focusedLabelColor = Color.Black,
            unfocusedLabelColor = Color.Gray,

            // 💜 убираем фиолетовые акценты полностью
            selectionColors = TextSelectionColors(
                handleColor = Color.Black,
                backgroundColor = Color(0x33222222)
            )
        )
    )
}

@Composable
fun SectionCardModern(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(12.dp))

            content()
        }
    }
}