package com.example.myapplication.ui.theme.screens

import android.graphics.*
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap

import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.MyApp
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.input.pointer.util.*
import com.example.myapplication.data.TFLiteClassifier

// ==========================
// 🔥 ЛАСТИК
// ==========================
@Composable
fun EraserEditor(
    file: File,
    bitmap: Bitmap?,
    onBitmapChange: (Bitmap) -> Unit,
    onSave: (File) -> Unit,
    onInteractionChange: (Boolean) -> Unit
) {
    val originalBitmap = remember {
        BitmapFactory.decodeFile(file.absolutePath)
    }

    var workingBitmap by remember {
        mutableStateOf(
            bitmap ?: originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        )
    }

    var brushSize by remember { mutableStateOf(50f) }
    var lastPoint by remember { mutableStateOf<Offset?>(null) }

    var scale by remember { mutableStateOf(1f) }
    var minScale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var initialized by remember { mutableStateOf(false) }

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

        // ===================== IMAGE EDITOR =====================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .clipToBounds()
                .pointerInput(Unit) {

                    awaitPointerEventScope {

                        val touchSlop = viewConfiguration.touchSlop
                        val drawDelay = 1L

                        while (true) {

                            val down = awaitFirstDown(requireUnconsumed = false)

                            onInteractionChange(true) // 🔥 блок скролла

                            var isDrawing = false
                            var isTransform = false
                            var startTime = System.currentTimeMillis()
                            var lastPos = down.position

                            lastPoint = null

                            while (true) {

                                val event = awaitPointerEvent()
                                val changes = event.changes

                                if (changes.isEmpty()) break

                                // ================= ZOOM =================
                                if (changes.size >= 2) {

                                    isTransform = true
                                    isDrawing = false
                                    lastPoint = null

                                    val zoom = event.calculateZoom()
                                    val pan = event.calculatePan()
                                    val centroid = event.calculateCentroid()

                                    val newScale =
                                        (scale * zoom).coerceIn(minScale, 6f)

                                    val scaleChange = newScale / scale

                                    offset =
                                        (offset - centroid) * scaleChange + centroid + pan

                                    scale = newScale

                                    changes.forEach { it.consume() }
                                    continue
                                }

                                val change = changes.first()

                                if (!change.pressed) break

                                val moveDistance =
                                    (change.position - lastPos).getDistance()

                                val timePassed =
                                    System.currentTimeMillis() - startTime

                                // ================= START DRAW =================
                                if (!isDrawing && !isTransform) {
                                    if (timePassed > drawDelay &&
                                        moveDistance > touchSlop
                                    ) {
                                        isDrawing = true
                                    } else {
                                        lastPos = change.position
                                        continue
                                    }
                                }

                                // ================= DRAW =================
                                if (isDrawing) {

                                    val x =
                                        (change.position.x - offset.x) / scale
                                    val y =
                                        (change.position.y - offset.y) / scale

                                    val canvas = Canvas(workingBitmap)

                                    if (lastPoint == null) {
                                        lastPoint = Offset(x, y)
                                    } else {

                                        paint.strokeWidth = brushSize / scale

                                        canvas.drawLine(
                                            lastPoint!!.x,
                                            lastPoint!!.y,
                                            x,
                                            y,
                                            paint
                                        )

                                        lastPoint = Offset(x, y)

                                        onBitmapChange(workingBitmap)
                                        redrawTrigger++
                                    }

                                    change.consume()
                                }

                                lastPos = change.position
                            }

                            onInteractionChange(false) // 🔥 разблок скролла
                        }
                    }
                }
        ) {

            key(redrawTrigger) {
                Canvas(modifier = Modifier.fillMaxSize()) {

                    if (!initialized) {

                        val imgW = workingBitmap.width.toFloat()
                        val imgH = workingBitmap.height.toFloat()

                        val fitScale = minOf(
                            size.width / imgW,
                            size.height / imgH
                        )

                        scale = fitScale
                        minScale = fitScale

                        val drawW = imgW * scale
                        val drawH = imgH * scale

                        offset = Offset(
                            (size.width - drawW) / 2f,
                            (size.height - drawH) / 2f
                        )

                        initialized = true
                    }

                    drawIntoCanvas { canvas ->
                        canvas.save()
                        canvas.translate(offset.x, offset.y)
                        canvas.scale(scale, scale)

                        canvas.nativeCanvas.drawBitmap(
                            workingBitmap,
                            0f,
                            0f,
                            null
                        )

                        canvas.restore()
                    }
                }
            }
        }

        // ===================== BRUSH SIZE =====================
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

        // ===================== RESET BUTTON =====================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Button(onClick = {
                workingBitmap =
                    originalBitmap.copy(Bitmap.Config.ARGB_8888, true)

                onBitmapChange(workingBitmap)
                redrawTrigger++
                initialized = false
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

    val colorOptionsRu = listOf(
        "Черный" to androidx.compose.ui.graphics.Color.Black,
        "Белый" to androidx.compose.ui.graphics.Color.White,
        "Синий" to androidx.compose.ui.graphics.Color(0xFF2196F3),
        "Красный" to androidx.compose.ui.graphics.Color(0xFFF44336),
        "Зеленый" to androidx.compose.ui.graphics.Color(0xFF4CAF50),
        "Желтый" to androidx.compose.ui.graphics.Color(0xFFFFEB3B)
    )

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var editBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var typeLocked by remember { mutableStateOf(false) }

    var currentImagePath by remember { mutableStateOf(imagePath) }

    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(typeOptionsRu.first()) }



    var category by remember { mutableStateOf(categoryOptionsRu.first()) }
    var style by remember { mutableStateOf(styleOptionsRu.first()) }

    var material by remember { mutableStateOf(materialOptionsRu.first()) }
    var color by remember { mutableStateOf(colorOptionsRu.first().first) }

    var brand by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    var currentOptions by remember { mutableStateOf(listOf<String>()) }
    var onSelect by remember { mutableStateOf<(String) -> Unit>({}) }
    var showSheet by remember { mutableStateOf(false) }
    var selectingColors by remember { mutableStateOf(false) }
    var lockScroll by remember { mutableStateOf(false) }
    val classifier = remember {
        TFLiteClassifier(
            context,
            "model.tflite",
            "labels.txt"
        )
    }
    var predictionDone by remember { mutableStateOf(false) }

    LaunchedEffect(imagePath) {

        if (typeLocked) return@LaunchedEffect

        val bmp = BitmapFactory.decodeFile(imagePath)
            ?: return@LaunchedEffect

        originalBitmap = bmp

        Log.d("ML_DEBUG", "CLASSIFY FROM ORIGINAL ONLY")

        val result = classifier.classify(bmp)
            .trim()
            .replace(Regex("^\\d+\\s*"), "")
            .lowercase()

        Log.d("ML_RESULT", result)

        type = when (result) {
            "джинсы" -> "Джинсы"
            "рубашка" -> "Рубашка"
            "футболка" -> "Футболка"
            "шорты" -> "Шорты"
            "обувь" -> "Обувь"
            else -> "Футболка"
        }

        typeLocked = true
    }


    // ================= BOTTOM SHEET =================
    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }) {

            if (selectingColors) {
                Column(Modifier.padding(16.dp)) {

                    Text("Выбери цвет")
                    Spacer(Modifier.height(12.dp))

                    colorOptionsRu.forEach { (nameColor, colorValue) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    color = nameColor
                                    showSheet = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(colorValue, shape = CircleShape)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(nameColor)
                        }
                    }
                }
            } else {
                SelectBottomSheet(
                    options = currentOptions,
                    onSelect = {
                        onSelect(it)
                        showSheet = false
                    },
                    onDismiss = { showSheet = false }
                )
            }
        }
    }

    // ================= MAIN UI =================
    Scaffold(

        bottomBar = {
            Button(
                onClick = {

                    val finalFile = File(
                        File(currentImagePath).parentFile,
                        "edited_${System.currentTimeMillis()}.png"
                    )

                    val bitmapToSave =
                        editBitmap ?: BitmapFactory.decodeFile(currentImagePath)

                    FileOutputStream(finalFile).use {
                        bitmapToSave.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }

                    vm.add(
                        finalFile.absolutePath,
                        type,
                        category,
                        style,
                        name,
                        brand,
                        material,
                        price,
                        color
                    )

                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Text("Сохранить")
            }
        }

    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp), // 👈 чтобы не перекрывалось кнопкой
            userScrollEnabled = !lockScroll
        ) {

            item {
                EraserEditor(
                    file = File(currentImagePath),
                    bitmap = editBitmap,
                    onBitmapChange = { editBitmap = it },
                    onSave = { savedFile ->
                        currentImagePath = savedFile.absolutePath
                        editBitmap = null
                    },
                    onInteractionChange = { lockScroll = it }
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
                            selectingColors = false
                            currentOptions = typeOptionsRu
                            onSelect = { type = it }
                            showSheet = true
                        }

                        SelectRow("Раздел", category, Icons.Default.Category) {
                            selectingColors = false
                            currentOptions = categoryOptionsRu
                            onSelect = { category = it }
                            showSheet = true
                        }

                        SelectRow("Стиль", style, Icons.Default.AutoAwesome) {
                            selectingColors = false
                            currentOptions = styleOptionsRu
                            onSelect = { style = it }
                            showSheet = true
                        }

                        SelectRow("Цвет", color, Icons.Default.Palette) {
                            selectingColors = true
                            showSheet = true
                        }

                        SelectRow("Материал", material, Icons.Default.Texture) {
                            selectingColors = false
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
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Цена") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Log.d("ML_CHECK", "type=$type")
                    }
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