package com.example.myapplication.ui.theme.screens

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
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

@Composable
fun ClosetScreen(
    modifier: Modifier = Modifier,
    vm: ClosetViewModel = viewModel()
) {

    val context = LocalContext.current
    val resolver = context.contentResolver

    var pendingUri by remember { mutableStateOf<android.net.Uri?>(null) }

    // 📸 CAMERA
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingUri != null) {
            vm.addItem(pendingUri.toString())
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
            uri?.let { vm.addItem(it.toString()) }
        }

    Box(modifier.fillMaxSize().padding(16.dp)) {

        Column(Modifier.fillMaxSize()) {

            Text("OutfitAI", style = MaterialTheme.typography.headlineMedium)

            Spacer(Modifier.height(12.dp))

            val items = vm.items

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
                                .size(600) // 🔥 фикс лагов
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

        // 🔥 FIXED BUTTONS
        Row(
            Modifier
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
    }
}