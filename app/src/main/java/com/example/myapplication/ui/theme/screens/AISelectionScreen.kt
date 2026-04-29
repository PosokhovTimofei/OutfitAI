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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
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
import com.example.myapplication.ui.theme.screens.OutfitEditorScreen

// ================= STATE =================

data class DraggableItem(
    val item: ClosetItemEntity,
    var offset: Offset = Offset(100f, 100f)
)


@Composable
fun GenerateOutfitScreen(
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    val scrollState = rememberScrollState()

    val vm: ClosetViewModel = viewModel(
        factory = ClosetViewModelFactory(
            (context.applicationContext as MyApp).repo
        )
    )

    val items by vm.items.collectAsState(initial = emptyList())

    var style by remember { mutableStateOf("") }
    var event by remember { mutableStateOf("") }

    val weather = "🌧 Дождь, +12°C"

    val outfitItems = remember { mutableStateListOf<DraggableItem>() }

    var isCreated by remember { mutableStateOf(false) }
    var scrollToBottom by remember { mutableStateOf(false) }

    // ================= SCROLL =================

    LaunchedEffect(scrollToBottom) {
        if (scrollToBottom) {
            scrollState.animateScrollTo(scrollState.maxValue)
            scrollToBottom = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            }
            .padding(24.dp),
    ) {

        // ================= INPUT MODE =================

        if (!isCreated) {

            SmallField("Погода", weather, enabled = false)
            Spacer(Modifier.height(12.dp))

            SmallField("Стиль", style) { style = it }
            Spacer(Modifier.height(12.dp))

            SmallField("Мероприятие", event) { event = it }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {

                    val tops = items.filter { it.category.contains("верх", true) }
                    val bottoms = items.filter { it.category.contains("низ", true) }
                    val shoesList = items.filter { it.category.contains("обув", true) }

                    if (tops.isNotEmpty() && bottoms.isNotEmpty() && shoesList.isNotEmpty()) {

                        outfitItems.clear()

                        outfitItems.addAll(
                            listOf(
                                DraggableItem(tops.random(), Offset(100f, 50f)),
                                DraggableItem(bottoms.random(), Offset(120f, 220f)),
                                DraggableItem(shoesList.random(), Offset(140f, 380f))
                            )
                        )

                        isCreated = true
                        scrollToBottom = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text("Создать образ")
            }
        }

        // ================= EDITOR MODE =================

        if (isCreated) {

            Spacer(Modifier.height(20.dp))

            Text("✨ Ваш образ", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(12.dp))

            // 🔥 CANVAS ДЛЯ СКРИНА
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .background(androidx.compose.ui.graphics.Color.White)
            ) {
                outfitItems.forEach { item ->
                    DraggableImage(item)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ================= SAVE (КАК В OutfitEditorScreen) =================

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {

                    scope.launch {

                        // 🔥 1. SCREENSHOT CANVAS
                        val full = view.drawToBitmap()

                        val croppedHeight = (full.height * 0.75f).toInt()

                        val cropped = Bitmap.createBitmap(
                            full,
                            0,
                            0,
                            full.width,
                            croppedHeight
                        )

                        // 🔥 2. SAVE FILE
                        val file = File(
                            context.cacheDir,
                            "outfit_${System.currentTimeMillis()}.png"
                        )

                        FileOutputStream(file).use {
                            cropped.compress(Bitmap.CompressFormat.PNG, 100, it)
                        }

                        // 🔥 3. STATES (как в OutfitEditorScreen)
                        val states = outfitItems.map {
                            OutfitItemState(
                                itemId = it.item.id,
                                x = it.offset.x,
                                y = it.offset.y,
                                scale = 1f
                            )
                        }

                        // 🔥 4. SAVE IN DB
                        vm.saveOutfit(
                            itemIds = outfitItems.map { it.item.id },
                            states = states,
                            previewUri = file.absolutePath
                        )
                    }
                }
            ) {
                Text("💾 Сохранить образ")
            }

            Spacer(Modifier.height(10.dp))

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

        Spacer(Modifier.height(30.dp))
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