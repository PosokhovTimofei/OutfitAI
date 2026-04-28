package com.example.myapplication.ui.theme.screens

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
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

    val states = remember(outfit.layoutJson) {
        try {
            Gson().fromJson<List<OutfitItemState>>(outfit.layoutJson, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    val editableStates = remember(states) {
        mutableStateListOf<OutfitItemState>().apply {
            clear()
            addAll(states)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        editableStates.forEach { state ->

            val item = items.find { it.id == state.itemId }

            if (item != null) {

                var x by remember { mutableStateOf(state.x) }
                var y by remember { mutableStateOf(state.y) }
                var scale by remember { mutableStateOf(state.scale) }

                Box(
                    modifier = Modifier
                        .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
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
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .pointerInput(item.id) {
                                detectTransformGestures { _, pan, zoom, _ ->

                                    scale = (scale * zoom).coerceIn(0.6f, 2.5f)
                                    x += pan.x
                                    y += pan.y

                                    val index = editableStates.indexOfFirst { it.itemId == state.itemId }

                                    if (index != -1) {
                                        editableStates[index] =
                                            state.copy(x = x, y = y, scale = scale)
                                    }
                                }
                            }
                    )

                    // ================= АККУРАТНОЕ УДАЛЕНИЕ =================
                    IconButton(
                        onClick = {
                            editableStates.remove(state)
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить"
                        )
                    }
                }
            }
        }

        // ================= BUTTONS =================
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {

                    scope.launch {

                        val json = Gson().toJson(editableStates)

                        val bitmap: Bitmap = view.drawToBitmap()

                        val file = File(
                            context.cacheDir,
                            "outfit_${System.currentTimeMillis()}.png"
                        )

                        FileOutputStream(file).use {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                        }

                        vm.updateOutfit(
                            outfit.copy(
                                layoutJson = json,
                                previewUri = file.absolutePath
                            )
                        )
                    }
                }
            ) {
                Text("💾 Сохранить изменения")
            }

            Spacer(Modifier.height(8.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.popBackStack() }
            ) {
                Text("⬅ Назад")
            }
        }
    }
}