package com.example.myapplication.ui.theme.screens

import android.graphics.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap

import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.MyApp
import java.io.File
import java.io.FileOutputStream

// ==========================
// 🔥 ЛАСТИК
// ==========================
@Composable
fun EraserEditor(
    file: File,
    onSave: (File) -> Unit
) {
    var bitmap by remember {
        mutableStateOf(
            BitmapFactory.decodeFile(file.absolutePath)
                .copy(Bitmap.Config.ARGB_8888, true)
        )
    }

    val paint = remember {
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 50f
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
    }

    var lastPoint by remember { mutableStateOf<Offset?>(null) }
    var brushSize by remember { mutableStateOf(50f) }

    Column {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { lastPoint = it },
                            onDrag = { change, _ ->
                                val current = change.position
                                val canvas = Canvas(bitmap)

                                lastPoint?.let {
                                    canvas.drawLine(
                                        it.x, it.y,
                                        current.x, current.y,
                                        paint.apply { strokeWidth = brushSize }
                                    )
                                }

                                lastPoint = current

                                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                            },
                            onDragEnd = { lastPoint = null }
                        )
                    }
            ) {
                drawImage(bitmap.asImageBitmap())
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "Размер кисти: ${brushSize.toInt()}",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Slider(
            value = brushSize,
            onValueChange = { brushSize = it },
            valueRange = 10f..120f,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Button(onClick = {
                bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    .copy(Bitmap.Config.ARGB_8888, true)
            }) {
                Text("Сброс")
            }

            Button(onClick = {
                val outFile = File(
                    file.parentFile,
                    "edited_${System.currentTimeMillis()}.png"
                )

                FileOutputStream(outFile).use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }

                onSave(outFile)
            }) {
                Text("Сохранить")
            }
        }
    }
}

// ==========================
// 📦 AddItemScreen
// ==========================

val typeOptionsRu = listOf("Футболка", "Рубашка", "Джинсы", "Платье", "Обувь", "Головной убор")
val categoryOptionsRu = listOf("Верх", "Низ", "Обувь", "Головной убор", "Аксессуар")
val styleOptionsRu = listOf("Повседневный", "Уличный", "Классический", "Спортивный")
val materialOptionsRu = listOf("Хлопок", "Шерсть", "Кожа", "Джинса", "Синтетика", "Лён")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    imagePath: String,
    navController: NavController
) {
    val context = LocalContext.current
    val vm: ClosetViewModel = viewModel(
        factory = ClosetViewModelFactory(
            (context.applicationContext as MyApp).repo
        )
    )

    var currentImagePath by remember { mutableStateOf(imagePath) }

    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(typeOptionsRu.first()) }
    var category by remember { mutableStateOf(categoryOptionsRu.first()) }
    var style by remember { mutableStateOf(styleOptionsRu.first()) }
    var material by remember { mutableStateOf(materialOptionsRu.first()) }

    var brand by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

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

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

        // ================= ЛАСТИК ВМЕСТО AsyncImage =================
        item {
            EraserEditor(
                file = File(currentImagePath),
                onSave = { savedFile ->
                    currentImagePath = savedFile.absolutePath
                }
            )
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                SectionCard("Категория", Icons.Default.Checkroom) {

                    SelectRow("Тип одежды", type, Icons.Default.Style) {
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

                    SelectRow("Материал", material, Icons.Default.Texture) {
                        currentOptions = materialOptionsRu
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
                        leadingIcon = { Icon(Icons.Default.Business, null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Теги") },
                        leadingIcon = { Icon(Icons.Default.Tag, null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Цена") },
                        leadingIcon = { Icon(Icons.Default.Euro, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        vm.add(currentImagePath, type, category, style, name)
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 8.dp)
                ) {
                    Text("Сохранить")
                }
            }
        }
    }
}

// ================= UI HELPERS =================

@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row {
                Icon(icon, null)
                Spacer(Modifier.width(8.dp))
                Text(title)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SelectRow(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Row {
            Icon(icon, null)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(label)
                Text(value)
            }
            Icon(Icons.Default.ArrowDropDown, null)
        }
        Divider()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectBottomSheet(
    options: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var search by remember { mutableStateOf("") }

    val filtered = options.filter {
        it.contains(search, true)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp)) {

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Поиск") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            LazyColumn {
                items(filtered) { item ->
                    ListItem(
                        headlineContent = { Text(item) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(item) }
                    )
                }
            }
        }
    }
}