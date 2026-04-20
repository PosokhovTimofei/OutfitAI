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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.MyApp

val typeOptions = listOf("shirt", "tshirt", "jeans", "dress", "shoes", "hat")
val categoryOptions = listOf("top", "bottom", "shoes", "hat", "accessory")
val styleOptions = listOf("casual", "streetwear", "classic", "sport")

@Composable
fun ClosetScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val resolver = context.contentResolver

    val vm: ClosetViewModel = viewModel(
        factory = ClosetViewModelFactory(
            (context.applicationContext as MyApp).repo
        )
    )

    // 📸 image states
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var selectedUri by remember { mutableStateOf<String?>(null) }
    var showForm by remember { mutableStateOf(false) }

    // 🧠 form states
    var type by remember { mutableStateOf(typeOptions.first()) }
    var category by remember { mutableStateOf(categoryOptions.first()) }
    var style by remember { mutableStateOf(styleOptions.first()) }
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

        // 📦 GRID
        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {

            items(items, key = { it.id }) { item ->

                Card(
                    onClick = {
                        navController.navigate("detail/${item.id}")
                    },
                    modifier = Modifier.aspectRatio(1f)
                ) {

                    Column {

                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(item.imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )

                        Text(
                            text = item.label.ifEmpty { "Без названия" },
                            modifier = Modifier.padding(6.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // 📌 DIALOG
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
                        label = ""
                    }) {
                        Text("Сохранить")
                    }
                },
                title = { Text("Добавить вещь") },
                text = {

                    Column {

                        // 🏷 label
                        OutlinedTextField(
                            value = label,
                            onValueChange = { label = it },
                            label = { Text("Название (например: Nike Hoodie)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        // 👕 type
                        Text("Тип: $type")
                        Dropdown(typeOptions) { type = it }

                        Spacer(Modifier.height(8.dp))

                        // 📂 category
                        Text("Категория: $category")
                        Dropdown(categoryOptions) { category = it }

                        Spacer(Modifier.height(8.dp))

                        // 🎨 style
                        Text("Стиль: $style")
                        Dropdown(styleOptions) { style = it }
                    }
                }
            )
        }

        // 🔘 BUTTONS
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
                onClick = {
                    galleryLauncher.launch("image/*")
                }
            ) {
                Text("🖼 Галерея")
            }
        }
    }
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