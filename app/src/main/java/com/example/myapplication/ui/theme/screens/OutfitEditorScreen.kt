package com.example.myapplication.ui.theme.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.drawToBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.myapplication.MyApp
import com.example.myapplication.data.ClosetItemEntity
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

// ================= STATE =================
data class OutfitItemState(
    val itemId: Long,
    val x: Float,
    val y: Float,
    val scale: Float
)

// ================= SCREEN =================
@Composable
fun OutfitEditorScreen(
    itemIds: String,
    navController: NavController
) {

    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    val vm: ClosetViewModel = viewModel(
        factory = ClosetViewModelFactory(
            (context.applicationContext as MyApp).repo
        )
    )

    val items by vm.items.collectAsState(initial = emptyList())

    val idList = remember(itemIds) {
        itemIds.split(",").mapNotNull { it.toLongOrNull() }
    }

    val selectedItems = remember(items, idList) {
        items.filter { it.id in idList }
    }

    val itemStates = remember { mutableStateListOf<OutfitItemState>() }

    Column(modifier = Modifier.fillMaxSize()) {

        // ================= CANVAS =================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .background(Color.White)
        ) {

            selectedItems.forEach { item ->

                val (startX, startY) = when (item.category) {
                    "Верх" -> 300f to 200f
                    "Низ" -> 300f to 600f
                    "Обувь" -> 300f to 900f
                    else -> 300f to 500f
                }

                DraggableItem(
                    item = item,
                    startX = startX,
                    startY = startY,
                    onStateChange = { state ->
                        itemStates.removeAll { it.itemId == state.itemId }
                        itemStates.add(state)
                    }
                )
            }
        }

        // ================= BUTTONS =================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {

                    scope.launch {

                        val bitmap = view.drawToBitmap()

                        val file = File(
                            context.cacheDir,
                            "outfit_${System.currentTimeMillis()}.png"
                        )

                        FileOutputStream(file).use {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                        }

                        vm.saveOutfit(
                            itemIds = idList,
                            states = itemStates,
                            previewUri = file.absolutePath
                        )
                    }
                }
            ) {
                Text("💾 Сохранить образ")
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.popBackStack() }
            ) {
                Text("⬅ Назад")
            }
        }
    }
}

// ================= ITEM =================
@Composable
fun DraggableItem(
    item: ClosetItemEntity,
    startX: Float,
    startY: Float,
    onStateChange: (OutfitItemState) -> Unit
) {

    var offsetX by remember(item.id) { mutableStateOf(startX) }
    var offsetY by remember(item.id) { mutableStateOf(startY) }
    var scale by remember(item.id) { mutableStateOf(1f) }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(item.imageUri)
            .allowHardware(false) // 🔥 фикс краша
            .build()
    )

    fun emit() {
        onStateChange(
            OutfitItemState(item.id, offsetX, offsetY, scale)
        )
    }

    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .size(220.dp)
            .offset {
                IntOffset(offsetX.roundToInt(), offsetY.roundToInt())
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(item.id) {
                detectTransformGestures { _, pan, zoom, _ ->

                    scale = (scale * zoom).coerceIn(0.6f, 2.5f)

                    offsetX += pan.x
                    offsetY += pan.y

                    emit()
                }
            }
    )
}