package com.example.myapplication.ui.theme.screens

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.MyApp
import com.example.myapplication.data.ImageStorage
import com.example.myapplication.data.RemoveBgApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import com.example.myapplication.data.ClosetItemEntity

@Composable
fun ClosetScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val resolver = context.contentResolver

    val vm: ClosetViewModel = viewModel(
        factory = ClosetViewModelFactory(
            (context.applicationContext as MyApp).repo
        )
    )

    var isLoading by remember { mutableStateOf(false) }
    val items by vm.items.collectAsState()

    var tempUri by remember { mutableStateOf<Uri?>(null) }

    // 🔥 selection
    var selectedItems by remember { mutableStateOf<List<ClosetItemEntity>>(emptyList()) }
    var outfitBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val isSelectionMode = selectedItems.isNotEmpty()

    // ================= CAMERA =================
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && tempUri != null) {
                processImage(
                    context = context,
                    uri = tempUri!!,
                    scope = scope,
                    onDone = { file ->
                        navController.navigate("addItem/${Uri.encode(file.absolutePath)}")
                    },
                    onLoading = { isLoading = it }
                )
            }
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted && tempUri != null) {
                cameraLauncher.launch(tempUri!!)
            }
        }

    // ================= GALLERY =================
    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                processImage(
                    context = context,
                    uri = it,
                    scope = scope,
                    onDone = { file ->
                        navController.navigate("addItem/${Uri.encode(file.absolutePath)}")
                    },
                    onLoading = { isLoading = it }
                )
            }
        }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // ================= GRID =================
        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            items(items, key = { it.id }) { item ->

                val isSelected = selectedItems.contains(item)

                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .then(
                            if (isSelected)
                                Modifier.border(3.dp, MaterialTheme.colorScheme.primary)
                            else Modifier
                        )
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .combinedClickable(

                                // ОДИН КЛИК
                                onClick = {
                                    if (isSelectionMode) {
                                        toggleSelect(item, selectedItems) {
                                            selectedItems = it
                                        }
                                    } else {
                                        navController.navigate("detail/${item.id}")
                                    }
                                },

                                // ДОЛГОЕ НАЖАТИЕ → включаем режим выбора
                                onLongClick = {
                                    toggleSelect(item, selectedItems) {
                                        selectedItems = it
                                    }
                                }
                            )
                    ) {

                        AsyncImage(
                            model = item.imageUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        IconButton(
                            onClick = { vm.delete(item) },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить")
                        }
                    }
                }
            }
        }

        // ================= CREATE BUTTON (ПОДСВЕТКА + ВЫШЕ) =================
        if (selectedItems.size == 3) {

            // затемнение фона
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )

            Button(
                onClick = {
                    outfitBitmap = createOutfit(selectedItems)
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.9f)
                    .height(70.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("✨ Создать образ")
            }
        }

        // ================= POPUP =================
        if (outfitBitmap != null) {

            AlertDialog(
                onDismissRequest = { outfitBitmap = null },
                confirmButton = {
                    Button(onClick = {
                        outfitBitmap = null
                        selectedItems = emptyList()
                    }) {
                        Text("Добавить образ")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { outfitBitmap = null }) {
                        Text("Закрыть")
                    }
                },
                text = {
                    Image(
                        bitmap = outfitBitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                }
            )
        }

        // ================= BUTTONS =================
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Button(
                modifier = Modifier.weight(1f),
                onClick = {

                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, "img_${System.currentTimeMillis()}.jpg")
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/OutfitAI")
                    }

                    val uri = resolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                    )

                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (uri != null) {
                        tempUri = uri
                        if (granted) cameraLauncher.launch(uri)
                        else permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            ) {
                Text("📸 Камера")
            }

            Button(
                modifier = Modifier.weight(1f),
                onClick = { galleryLauncher.launch("image/*") }
            ) {
                Text("🖼 Галерея")
            }
        }

        // ================= LOADING =================
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

/**
 * 🔥 ОБРАБОТКА КАРТИНКИ (File → File)
 */
private fun processImage(
    context: android.content.Context,
    uri: Uri,
    scope: kotlinx.coroutines.CoroutineScope,
    onDone: (File) -> Unit,
    onLoading: (Boolean) -> Unit
) {
    scope.launch {
        onLoading(true)

        try {
            val tempFile = ImageStorage.saveTemp(context, uri)

            val resultFile = withContext(kotlinx.coroutines.Dispatchers.IO) {
                RemoveBgApi.removeBackground(tempFile)
            }

            onDone(resultFile)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        onLoading(false)
    }
}

fun toggleSelect(
    item: ClosetItemEntity,
    current: List<ClosetItemEntity>,
    update: (List<ClosetItemEntity>) -> Unit
) {
    val list = current.toMutableList()

    if (list.contains(item)) {
        list.remove(item)
    } else if (list.size < 3) {
        list.add(item)
    }

    update(list)
}

fun createOutfit(items: List<ClosetItemEntity>): Bitmap {

    val bitmaps = items.mapNotNull {
        BitmapFactory.decodeFile(it.imageUri)
    }

    val width = bitmaps.maxOf { it.width }
    val height = bitmaps.sumOf { it.height }

    val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)

    var top = 0

    bitmaps.forEach {
        canvas.drawBitmap(it, 0f, top.toFloat(), null)
        top += it.height
    }

    return result
}