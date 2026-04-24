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
    bitmap: Bitmap?,
    onBitmapChange: (Bitmap) -> Unit,
    onSave: (File) -> Unit
) {
    val originalBitmap = remember {
        BitmapFactory.decodeFile(file.absolutePath)
    }

    // 🔥 главный фикс — используем внешний bitmap
    val workingBitmap = bitmap ?: originalBitmap.copy(Bitmap.Config.ARGB_8888, true)

    var brushSize by remember { mutableStateOf(50f) }
    var lastPoint by remember { mutableStateOf<Offset?>(null) }

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var redrawTrigger by remember { mutableStateOf(0) }

    val paint = remember {
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
    }

    Column {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .pointerInput(bitmap, scale, offset) {

                    detectDragGestures(
                        onDragStart = { touch ->
                            val bx = (touch.x - offset.x) / scale
                            val by = (touch.y - offset.y) / scale
                            lastPoint = Offset(bx, by)
                        },

                        onDrag = { change, _ ->

                            val canvas = Canvas(workingBitmap)

                            val x = (change.position.x - offset.x) / scale
                            val y = (change.position.y - offset.y) / scale

                            lastPoint?.let {
                                paint.strokeWidth = brushSize / scale
                                canvas.drawLine(it.x, it.y, x, y, paint)
                            }

                            lastPoint = Offset(x, y)

                            // 🔥 сохраняем bitmap наружу
                            onBitmapChange(workingBitmap)

                            redrawTrigger++
                        },

                        onDragEnd = { lastPoint = null }
                    )
                }
        ) {

            key(redrawTrigger) {
                Canvas(modifier = Modifier.fillMaxSize()) {

                    val imgW = workingBitmap.width.toFloat()
                    val imgH = workingBitmap.height.toFloat()

                    scale = minOf(
                        size.width / imgW,
                        size.height / imgH
                    )

                    val drawW = imgW * scale
                    val drawH = imgH * scale

                    offset = Offset(
                        (size.width - drawW) / 2f,
                        (size.height - drawH) / 2f
                    )

                    drawIntoCanvas { canvas ->
                        canvas.save()
                        canvas.translate(offset.x, offset.y)
                        canvas.scale(scale, scale)
                        canvas.nativeCanvas.drawBitmap(workingBitmap, 0f, 0f, null)
                        canvas.restore()
                    }
                }
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
                val reset = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
                onBitmapChange(reset)
                redrawTrigger++
            }) {
                Text("Сброс")
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
    var editBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

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
                bitmap = editBitmap,
                onBitmapChange = { editBitmap = it },
                onSave = { savedFile ->
                    currentImagePath = savedFile.absolutePath
                    editBitmap = null
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

                        val finalFile = File(
                            File(currentImagePath).parentFile,
                            "edited_${System.currentTimeMillis()}.png"
                        )

                        val bitmapToSave = editBitmap
                            ?: BitmapFactory.decodeFile(currentImagePath)

                        FileOutputStream(finalFile).use {
                            bitmapToSave.compress(Bitmap.CompressFormat.PNG, 100, it)
                        }

                        vm.add(
                            finalFile.absolutePath,
                            type,
                            category,
                            style,
                            name
                        )

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