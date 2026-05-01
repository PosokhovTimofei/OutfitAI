package com.example.myapplication.ui.theme.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.view.drawToBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.MyApp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

// ================= STATE =================
data class OutfitItemState(
    val itemId: Long,
    val x: Float,
    val y: Float,
    val scale: Float,
    val zIndex: Float = 0f
)

// ================= SCREEN =================
@Composable
fun OutfitViewScreen(
    outfitId: Long,
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

    val outfits by vm.outfits.collectAsState(initial = emptyList())
    val items by vm.items.collectAsState(initial = emptyList())

    val outfit = outfits.find { it.id == outfitId } ?: return

    val type = object : TypeToken<List<OutfitItemState>>() {}.type

    val initialStates = remember(outfit.layoutJson) {
        try {
            Gson().fromJson<List<OutfitItemState>>(outfit.layoutJson, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    val editableStates = remember(initialStates) {
        mutableStateListOf<OutfitItemState>().apply {
            clear()
            addAll(initialStates)
        }
    }

    var showItemsDialog by remember { mutableStateOf(false) }
    var selectedItemId by remember { mutableStateOf<Long?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // ================= ITEMS =================
        editableStates.forEach { state ->

            val item = items.find { it.id == state.itemId } ?: return@forEach
            val isSelected = selectedItemId == state.itemId
            val isLocked = selectedItemId != null && !isSelected

            Box(
                modifier = Modifier.zIndex(state.zIndex)
            ) {

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(item.imageUri)
                        .allowHardware(false)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(220.dp)
                        .offset {
                            IntOffset(state.x.roundToInt(), state.y.roundToInt())
                        }
                        .graphicsLayer {
                            scaleX = state.scale
                            scaleY = state.scale
                            alpha = if (isLocked) 0.5f else 1f
                        }
                        .then(
                            if (isSelected)
                                Modifier.border(3.dp, Color.Red)
                            else Modifier
                        )
                        .clickable {
                            selectedItemId =
                                if (selectedItemId == state.itemId) null
                                else state.itemId
                        }
                        .pointerInput(state.itemId, selectedItemId) {
                            detectTransformGestures { _, pan, zoom, _ ->

                                // ❌ блокируем остальные
                                if (selectedItemId != null && selectedItemId != state.itemId) return@detectTransformGestures

                                val index = editableStates.indexOfFirst { it.itemId == state.itemId }

                                if (index != -1) {
                                    val current = editableStates[index]

                                    editableStates[index] = current.copy(
                                        x = current.x + pan.x,
                                        y = current.y + pan.y,
                                        scale = (current.scale * zoom).coerceIn(0.6f, 2.5f)
                                    )
                                }
                            }
                        }
                )
            }
        }

        // ================= PANEL =================
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White.copy(alpha = 0.9f),
                tonalElevation = 8.dp
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    AnimatedVisibility(selectedItemId != null) {
                        IconButton(
                            onClick = {
                                editableStates.removeAll { it.itemId == selectedItemId }
                                selectedItemId = null
                            },
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        ) {
                            Icon(Icons.Default.Delete, null, tint = Color.White)
                        }
                    }

                    IconButton(
                        onClick = { showItemsDialog = true },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF2F2F7))
                    ) {
                        Icon(Icons.Default.Add, null)
                    }

                    AnimatedVisibility(selectedItemId != null) {
                        IconButton(
                            onClick = {
                                selectedItemId?.let { id ->
                                    val index = editableStates.indexOfFirst { it.itemId == id }
                                    if (index != -1) {
                                        val maxZ = editableStates.maxOf { it.zIndex }
                                        val item = editableStates[index]
                                        editableStates[index] =
                                            item.copy(zIndex = maxZ + 1f)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF2F2F7))
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, null)
                        }
                    }

                    AnimatedVisibility(selectedItemId != null) {
                        IconButton(
                            onClick = {
                                selectedItemId?.let { id ->
                                    val index = editableStates.indexOfFirst { it.itemId == id }
                                    if (index != -1) {
                                        val minZ = editableStates.minOf { it.zIndex }
                                        val item = editableStates[index]
                                        editableStates[index] =
                                            item.copy(zIndex = minZ - 1f)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF2F2F7))
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, null)
                        }
                    }

                    IconButton(
                        onClick = {
                            scope.launch {

                                val json = Gson().toJson(editableStates)

                                // 1. полный скрин
                                val fullBitmap: Bitmap = view.drawToBitmap()

                                // 2. обрезаем нижнюю панель (как у тебя 0.79f)
                                val croppedHeight = (fullBitmap.height * 0.76f).toInt()

                                val croppedBitmap = Bitmap.createBitmap(
                                    fullBitmap,
                                    0,
                                    0,
                                    fullBitmap.width,
                                    croppedHeight
                                )

                                // 3. файл
                                val file = File(
                                    context.cacheDir,
                                    "outfit_${System.currentTimeMillis()}.png"
                                )

                                FileOutputStream(file).use {
                                    croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                                }

                                // 4. сохранение
                                vm.updateOutfit(
                                    outfit.copy(
                                        layoutJson = json,
                                        previewUri = file.absolutePath
                                    )
                                )

                                navController.navigate("favorites") {
                                    popUpTo("favorites") { inclusive = true }
                                }
                            }
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0A84FF))
                    ) {
                        Icon(Icons.Default.Save, null, tint = Color.White)
                    }
                }
            }
        }

        // ================= DIALOG =================
        if (showItemsDialog) {
            AlertDialog(
                onDismissRequest = { showItemsDialog = false },
                confirmButton = {},
                title = { Text("Выбери вещь") },
                text = {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(100.dp),
                        modifier = Modifier.height(300.dp)
                    ) {
                        items(items) { item ->
                            Card(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .shadow(
                                        elevation = 6.dp,
                                        shape = RoundedCornerShape(12.dp),
                                        clip = false
                                    )
                                    .clickable {
                                        editableStates.add(
                                            OutfitItemState(
                                                itemId = item.id,
                                                x = 150f,
                                                y = 150f,
                                                scale = 1f,
                                                zIndex = editableStates.size.toFloat()
                                            )
                                        )
                                        showItemsDialog = false
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                AsyncImage(
                                    model = item.imageUri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}