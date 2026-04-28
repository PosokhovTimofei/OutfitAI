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

data class OutfitItemState(
    val itemId: Long,
    val x: Float,
    val y: Float,
    val scale: Float
)

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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f)
            .background(Color.White)
    ) {

        selectedItems.forEach { item ->

            var x by remember { mutableStateOf(300f) }
            var y by remember { mutableStateOf(300f) }
            var scale by remember { mutableStateOf(1f) }

            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(context)
                    .data(item.imageUri)
                    .allowHardware(false)
                    .build()
            )

            fun emit() {
                itemStates.removeAll { it.itemId == item.id }
                itemStates.add(
                    OutfitItemState(item.id, x, y, scale)
                )
            }

            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(220.dp)
                    .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .pointerInput(item.id) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.6f, 2.5f)
                            x += pan.x
                            y += pan.y
                            emit()
                        }
                    }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {

                scope.launch {

                    // 🔥 1. СКРИН ТОЛЬКО КАНВАСА
                    val full = view.drawToBitmap()

                    val croppedHeight = (full.height * 0.75f).toInt()

                    val cropped = Bitmap.createBitmap(
                        full,
                        0,
                        0,
                        full.width,
                        croppedHeight
                    )

                    val file = File(
                        context.cacheDir,
                        "outfit_${System.currentTimeMillis()}.png"
                    )

                    FileOutputStream(file).use {
                        cropped.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }

                    // 🔥 2. СОХРАНЯЕМ:
                    vm.saveOutfit(
                        itemIds = idList,
                        states = itemStates,
                        previewUri = file.absolutePath // <- ЭТО ИДЕТ В FAVORITES
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