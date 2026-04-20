package com.example.myapplication.ui.theme.screens

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.MyApp

@Composable
fun ClosetScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resolver = context.contentResolver

    val vm: ClosetViewModel = viewModel(
        factory = ClosetViewModelFactory(
            (context.applicationContext as MyApp).repo
        )
    )

    var pendingUri by remember { mutableStateOf<Uri?>(null) }

    // 👉 форма добавления
    var showForm by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<String?>(null) }

    var type by remember { mutableStateOf("shirt") }
    var category by remember { mutableStateOf("top") }
    var style by remember { mutableStateOf("casual") }
    var label by remember { mutableStateOf("") }

    // 📸 CAMERA
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingUri != null) {
            selectedUri = pendingUri.toString()
            showForm = true
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it && pendingUri != null) {
                cameraLauncher.launch(pendingUri!!)
            }
        }

    // 🖼 GALLERY
    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                selectedUri = it.toString()
                showForm = true
            }
        }

    val items by vm.items.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Column(Modifier.fillMaxSize()) {

            Text("OutfitAI", style = MaterialTheme.typography.headlineMedium)

            Spacer(Modifier.height(12.dp))

            if (items.isEmpty()) {
                Box(
                    Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Пока пусто 👀")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(160.dp),
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(item.imageUri)
                                .crossfade(true)
                                .size(400)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }

        // 📌 ФОРМА ПОСЛЕ ВЫБОРА ФОТО
        if (showForm && selectedUri != null) {

            AlertDialog(
                onDismissRequest = {
                    showForm = false
                    selectedUri = null
                },
                confirmButton = {
                    Button(onClick = {
                        vm.add(
                            selectedUri!!,
                            type,
                            category,
                            style,
                            label
                        )
                        showForm = false
                        selectedUri = null
                    }) {
                        Text("Сохранить")
                    }
                },
                title = { Text("Добавить вещь") },
                text = {

                    Column {

                        Text("Тип: $type")
                        Dropdown(typeOptions) { type = it }

                        Spacer(Modifier.height(8.dp))

                        Text("Категория: $category")
                        Dropdown(categoryOptions) { category = it }

                        Spacer(Modifier.height(8.dp))

                        Text("Стиль: $style")
                        Dropdown(styleOptions) { style = it }

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = label,
                            onValueChange = { label = it },
                            label = { Text("Название (Gucci кепка)") }
                        )
                    }
                }
            )
        }

        // 🔥 BUTTONS
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

                    if (uri != null) {
                        pendingUri = uri

                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (granted) {
                            cameraLauncher.launch(uri)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
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
    }
}

// 🔽 dropdown helper
@Composable
fun Dropdown(options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(options.first()) }

    Box {
        Button(onClick = { expanded = true }) {
            Text(selected)
        }

        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        selected = it
                        onSelect(it)
                        expanded = false
                    }
                )
            }
        }
    }
}

// 🔽 options
val typeOptions = listOf("shirt", "tshirt", "jeans", "dress", "shoes", "hat")
val categoryOptions = listOf("top", "bottom", "shoes", "hat", "accessory")
val styleOptions = listOf("casual", "streetwear", "classic", "sport")