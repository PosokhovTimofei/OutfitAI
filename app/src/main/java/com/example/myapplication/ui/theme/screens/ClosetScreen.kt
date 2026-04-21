package com.example.myapplication.ui.theme.screens

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
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
import java.io.FileOutputStream

val typeOptions = listOf("shirt", "tshirt", "jeans", "dress", "shoes", "hat")
val categoryOptions = listOf("top", "bottom", "shoes", "hat", "accessory")
val styleOptions = listOf("casual", "streetwear", "classic", "sport")

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

    var savedPath by remember { mutableStateOf<String?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var label by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(typeOptions.first()) }
    var category by remember { mutableStateOf(categoryOptions.first()) }
    var style by remember { mutableStateOf(styleOptions.first()) }

    val items by vm.items.collectAsState()

    // =========================
    // 📸 CAMERA
    // =========================
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && tempUri != null) {
                handleImage(
                    context,
                    tempUri!!,
                    scope,
                    onDone = {
                        savedPath = it
                        showForm = true
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

    // =========================
    // 🖼 GALLERY
    // =========================
    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                handleImage(
                    context,
                    it,
                    scope,
                    onDone = {
                        savedPath = it
                        showForm = true
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

        // GRID
        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            items(items, key = { it.id }) { item ->

                Card(
                    onClick = { navController.navigate("detail/${item.id}") },
                    modifier = Modifier.aspectRatio(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {

                    Box(Modifier.fillMaxSize()) {

                        Column {

                            AsyncImage(
                                model = File(item.imageUri),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )

                            Text(
                                text = item.label.ifEmpty { "Без названия" },
                                modifier = Modifier.padding(6.dp)
                            )
                        }

                        IconButton(
                            onClick = { vm.delete(item) },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить"
                            )
                        }
                    }
                }
            }
        }

        // FORM
        if (showForm && savedPath != null) {

            AlertDialog(
                onDismissRequest = {
                    showForm = false
                    savedPath = null
                },
                confirmButton = {
                    Button(onClick = {
                        vm.add(savedPath!!, type, category, style, label)
                        showForm = false
                        savedPath = null
                        label = ""
                    }) {
                        Text("Сохранить")
                    }
                },
                title = { Text("Добавить вещь") },
                text = {
                    Column {

                        OutlinedTextField(
                            value = label,
                            onValueChange = { label = it },
                            label = { Text("Название") }
                        )

                        Spacer(Modifier.height(8.dp))

                        Text("Тип: $type")
                        Dropdown(typeOptions) { type = it }

                        Text("Категория: $category")
                        Dropdown(categoryOptions) { category = it }

                        Text("Стиль: $style")
                        Dropdown(styleOptions) { style = it }
                    }
                }
            )
        }

        // BUTTONS
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

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

// =========================
// 🧠 ОБЩАЯ ОБРАБОТКА (фон + crop)
// =========================
private fun handleImage(
    context: android.content.Context,
    uri: Uri,
    scope: kotlinx.coroutines.CoroutineScope,
    onDone: (String) -> Unit,
    onLoading: (Boolean) -> Unit
) {
    scope.launch {
        onLoading(true)

        try {
            val tempFile = ImageStorage.saveTemp(context, uri)

            val noBg = withContext(Dispatchers.IO) {
                RemoveBgApi.removeBackground(
                    tempFile,
                    "h1T2zDy7B56cygQzdQVcVha2"
                )
            }

            val cropped = withContext(Dispatchers.IO) {
                cropTransparent(noBg)
            }

            onDone(cropped.absolutePath)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        onLoading(false)
    }
}

// =========================
// ✂️ CROP TRANSPARENT
// =========================
fun cropTransparent(file: File): File {

    val bitmap = BitmapFactory.decodeFile(file.absolutePath)

    var minX = bitmap.width
    var minY = bitmap.height
    var maxX = 0
    var maxY = 0

    for (y in 0 until bitmap.height) {
        for (x in 0 until bitmap.width) {

            val alpha = (bitmap.getPixel(x, y) shr 24) and 0xff

            if (alpha > 10) {

                if (x < minX) minX = x
                if (y < minY) minY = y
                if (x > maxX) maxX = x
                if (y > maxY) maxY = y
            }
        }
    }

    if (maxX <= minX || maxY <= minY) return file

    val cropped = Bitmap.createBitmap(
        bitmap,
        minX,
        minY,
        maxX - minX,
        maxY - minY
    )

    val newFile = File(file.parent, "cropped_${file.name}")
    val out = FileOutputStream(newFile)

    cropped.compress(Bitmap.CompressFormat.PNG, 100, out)

    out.flush()
    out.close()

    return newFile
}

@Composable
fun Dropdown(
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(options.first()) }

    Box {

        Button(onClick = { expanded = true }) {
            Text(selected)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

            options.forEach { option ->

                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        selected = option
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}