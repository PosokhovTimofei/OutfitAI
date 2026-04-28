package com.example.myapplication.ui.theme.screens

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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.MyApp
import com.example.myapplication.data.ClosetItemEntity
import kotlin.math.roundToInt

@Composable
fun OutfitEditorScreen(
    itemIds: String,
    navController: NavController
) {

    val context = LocalContext.current

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

    val density = LocalDensity.current

    val screenW = with(density) {
        LocalConfiguration.current.screenWidthDp.dp.toPx()
    }

    val screenH = with(density) {
        LocalConfiguration.current.screenHeightDp.dp.toPx()
    }

    val bottomBarHeightPx = with(density) {
        250.dp.toPx()
    }

    val canvasHeight = screenH - bottomBarHeightPx

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEEEEE))
    ) {

        // ================= CANVAS =================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        )

        // ================= ITEMS =================
        selectedItems.forEach { item ->

            val (startX, startY) = when (item.category) {
                "Верх" -> screenW / 2f to 200f
                "Низ" -> screenW / 2f to 600f
                "Обувь" -> screenW / 2f to 1000f
                else -> screenW / 2f to 500f
            }

            DraggableZoomableItem(
                item = item,
                startX = startX,
                startY = startY,
                screenW = screenW,
                screenH = canvasHeight
            )
        }

        // ================= BUTTONS =================
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {

            Button(
                onClick = {
                    // TODO сохранить образ
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("💾 Сохранить образ")
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("⬅ Назад")
            }
        }
    }
}

@Composable
fun DraggableZoomableItem(
    item: ClosetItemEntity,
    startX: Float,
    startY: Float,
    screenW: Float,
    screenH: Float
) {

    var offsetX by remember(item.id) { mutableStateOf(startX) }
    var offsetY by remember(item.id) { mutableStateOf(startY) }
    var scale by remember(item.id) { mutableStateOf(1f) }

    val baseSize = 220f

    AsyncImage(
        model = item.imageUri,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .size(220.dp)
            .offset {
                IntOffset(
                    offsetX.roundToInt(),
                    offsetY.roundToInt()
                )
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

                    val size = baseSize * scale

                    // 🔥 ОГРАНИЧЕНИЕ ТОЛЬКО СПРАВА И СНИЗУ
                    val maxX = screenW - size
                    val maxY = screenH - size

                    offsetX = offsetX.coerceAtMost(maxX)
                    offsetY = offsetY.coerceAtMost(maxY)
                }
            }
    )
}