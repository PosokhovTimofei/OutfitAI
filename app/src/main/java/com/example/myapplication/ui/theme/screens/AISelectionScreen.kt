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

    var style by remember { mutableStateOf("") }
    var event by remember { mutableStateOf("") }

    val outfitItems = remember { mutableStateListOf<DraggableItem>() }
    var isCreated by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()

    ) {

        // ===================== INPUT MODE =====================
        if (!isCreated) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {

                SmallField("Погода", "🌧 Дождь, +12°C", enabled = false)
                Spacer(Modifier.height(12.dp))

                SmallField("Стиль", style) { style = it }
                Spacer(Modifier.height(12.dp))

                SmallField("Мероприятие", event) { event = it }

                Spacer(Modifier.height(20.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    onClick = {

                        val tops = items.filter { it.category.contains("верх", true) }
                        val bottoms = items.filter { it.category.contains("низ", true) }
                        val shoes = items.filter { it.category.contains("обув", true) }

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

        // ===================== EDITOR MODE =====================
        if (isCreated) {

            // 🔥 ОБРАЗ (центр экрана)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 140.dp) // место под кнопки
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .height(500.dp)
                        .background(androidx.compose.ui.graphics.Color.White)
                ) {
                    outfitItems.forEach { item ->
                        DraggableImage(item)
                    }
                }
            }

            // ===================== FIXED BOTTOM BUTTONS =====================
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 0.dp // 👈 меньше отступ = ниже кнопки
                    ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {

                        scope.launch {

                            val fullBitmap = view.drawToBitmap()

                            val croppedHeight = (fullBitmap.height * 0.72f).toInt()

                            val croppedBitmap = Bitmap.createBitmap(
                                fullBitmap,
                                0,
                                0,
                                fullBitmap.width,
                                croppedHeight
                            )

                            val file = File(
                                context.cacheDir,
                                "outfit_${System.currentTimeMillis()}.png"
                            )

                            FileOutputStream(file).use {
                                croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
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

@Composable
fun SmallField(
    label: String,
    value: String,
    enabled: Boolean = true,
    onChange: (String) -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        enabled = enabled,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        shape = RoundedCornerShape(10.dp)
    )
}