package com.example.myapplication.ui.theme.screens

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.MyApp
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt



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

    var selectedId by remember { mutableStateOf<Long?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // ================= ITEMS =================
        selectedItems.forEach { item ->

            val index = itemStates.indexOfFirst { it.itemId == item.id }

            val state = if (index != -1)
                itemStates[index]
            else {
                val new = OutfitItemState(item.id, 300f, 300f, 1f)
                itemStates.add(new)
                new
            }

            val isSelected = selectedId == item.id

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(state.x.roundToInt(), state.y.roundToInt())
                    }
                    .graphicsLayer {
                        scaleX = state.scale
                        scaleY = state.scale
                    }
                    .then(Modifier)
                    .pointerInput(item.id) {
                        detectTapGestures(
                            onTap = {
                                selectedId =
                                    if (selectedId == item.id) null else item.id
                            }
                        )
                    }
                    .pointerInput(item.id) {
                        detectTransformGestures { _, pan, zoom, _ ->

                            val i = itemStates.indexOfFirst { it.itemId == item.id }
                            if (i == -1) return@detectTransformGestures

                            val current = itemStates[i]

                            itemStates[i] = current.copy(
                                x = current.x + pan.x,
                                y = current.y + pan.y,
                                scale = (current.scale * zoom).coerceIn(0.6f, 2.5f)
                            )
                        }
                    }
            ) {

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(item.imageUri)
                        .allowHardware(false)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(220.dp)
                )
            }
        }
    }

    // ================= CENTER BUTTONS =================
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {

        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White.copy(alpha = 0.95f),
            tonalElevation = 10.dp,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .shadow(20.dp, RoundedCornerShape(28.dp))
        ) {

            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // 💾 SAVE
                IconButton(
                    onClick = {
                        scope.launch {

                            val fullBitmap: Bitmap = view.drawToBitmap()

                            // 🔥 обрезаем нижнюю панель (примерно 0.82f — чуть выше кнопок)
                            val croppedHeight = (fullBitmap.height * 0.76f).toInt()

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
                                itemIds = idList,
                                states = itemStates,
                                previewUri = file.absolutePath
                            )

                            navController.navigate("favorites")
                        }
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0A84FF))
                ) {
                    Icon(Icons.Default.Save, null, tint = Color.White)
                }

                Spacer(Modifier.width(12.dp))

                // ⬅ BACK
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF2F2F7))
                ) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.Black)
                }
            }
        }
    }
}