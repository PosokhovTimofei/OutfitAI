package com.example.myapplication.ui.theme.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.drawToBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.MyApp
import com.example.myapplication.data.ClosetItemEntity
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown

// ================= STATE =================

data class DraggableItem(
    val item: ClosetItemEntity,
    var offset: Offset = Offset(100f, 100f)
)

@Composable
fun GenerateOutfitScreen(
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val vm: ClosetViewModel = viewModel(
        factory = ClosetViewModelFactory(
            (context.applicationContext as MyApp).repo
        )
    )

    val items by vm.items.collectAsState(initial = emptyList())

    val styles = listOf("Кэжуал", "Спорт", "Офис", "Вечеринка")
    val events = listOf("Прогулка", "Работа", "Свидание", "Тренировка")

    var style by remember { mutableStateOf("Кэжуал") }
    var event by remember { mutableStateOf("Прогулка") }

    val outfitItems = remember { mutableStateListOf<DraggableItem>() }
    var isCreated by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {

        // ================= INPUT =================
        if (!isCreated) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {

                DropdownField("Стиль", styles, style) { style = it }

                Spacer(Modifier.height(12.dp))

                DropdownField("Мероприятие", events, event) { event = it }

                Spacer(Modifier.height(20.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    onClick = {

                        // ================= STYLE FILTER =================
                        val styleFiltered = items.filter { item ->
                            when (style) {
                                "Кэжуал" -> true
                                "Спорт" -> item.type.contains("Кроссовки", true)
                                "Офис" -> item.type.contains("Рубашка", true) || item.type.contains("Джинсы", true)
                                "Вечеринка" -> true
                                else -> true
                            }
                        }

                        val base = if (styleFiltered.isEmpty()) items else styleFiltered

                        // ================= DRESS MODE =================
                        val dresses = base.filter {
                            it.type.equals("Платье", true)
                        }

                        if (dresses.isNotEmpty()) {

                            val dress = dresses.random()

                            val shoes = base.filter {
                                it.type.equals("Кроссовки", true)
                            }

                            if (shoes.isNotEmpty()) {

                                outfitItems.clear()

                                outfitItems.addAll(
                                    listOf(
                                        DraggableItem(dress, Offset(120f, 120f)),
                                        DraggableItem(shoes.random(), Offset(140f, 380f))
                                    )
                                )

                                isCreated = true
                                return@Button
                            }
                        }

                        // ================= NORMAL MODE =================
                        val tops = base.filter {
                            it.type.equals("Футболка", true) ||
                                    it.type.equals("Рубашка", true)
                        }

                        val bottoms = base.filter {
                            it.type.equals("Джинсы", true) ||
                                    it.type.equals("Шорты", true)
                        }

                        val shoes = base.filter {
                            it.type.equals("Кроссовки", true)
                        }

                        if (tops.isNotEmpty() && bottoms.isNotEmpty() && shoes.isNotEmpty()) {

                            outfitItems.clear()

                            outfitItems.addAll(
                                listOf(
                                    DraggableItem(tops.random(), Offset(100f, 50f)),
                                    DraggableItem(bottoms.random(), Offset(120f, 220f)),
                                    DraggableItem(shoes.random(), Offset(140f, 380f))
                                )
                            )

                            isCreated = true
                        }
                    }
                ) {
                    Text("Создать образ")
                }
            }
        }

        // ================= EDITOR =================
        if (isCreated) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 140.dp)
            ) {

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .height(500.dp)
                        .background(androidx.compose.ui.graphics.Color.White)
                ) {

                    outfitItems.forEach {
                        DraggableImage(it)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {

                        scope.launch {

                            val bitmap = view.drawToBitmap()

                            val cropped = Bitmap.createBitmap(
                                bitmap,
                                0,
                                0,
                                bitmap.width,
                                (bitmap.height * 0.70f).toInt()
                            )

                            val file = File(
                                context.cacheDir,
                                "outfit_${System.currentTimeMillis()}.png"
                            )

                            FileOutputStream(file).use {
                                cropped.compress(Bitmap.CompressFormat.PNG, 100, it)
                            }

                            vm.saveOutfit(
                                itemIds = outfitItems.map { it.item.id },
                                states = outfitItems.map {
                                    OutfitItemState(
                                        itemId = it.item.id,
                                        x = it.offset.x,
                                        y = it.offset.y,
                                        scale = 1f
                                    )
                                },
                                previewUri = file.absolutePath
                            )
                        }
                    }
                ) {
                    Text("💾 Сохранить образ")
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        isCreated = false
                        outfitItems.clear()
                    }
                ) {
                    Text("🔄 Другой образ")
                }
            }
        }
    }
}
// ================= DRAG =================

@Composable
fun DraggableImage(dragItem: DraggableItem) {

    var offsetX by remember { mutableStateOf(dragItem.offset.x) }
    var offsetY by remember { mutableStateOf(dragItem.offset.y) }

    val bitmap = remember(dragItem.item.imageUri) {
        BitmapFactory.decodeFile(dragItem.item.imageUri)
    }

    if (bitmap != null) {

        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .offset {
                    IntOffset(offsetX.roundToInt(), offsetY.roundToInt())
                }
                .size(150.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()

                        offsetX += dragAmount.x
                        offsetY += dragAmount.y

                        dragItem.offset = Offset(offsetX, offsetY)
                    }
                }
        )
    }
}

// ================= FIELD =================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {

        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}