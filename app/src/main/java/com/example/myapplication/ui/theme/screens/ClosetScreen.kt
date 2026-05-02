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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Category
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClosetScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {

    var showFilters by remember { mutableStateOf(false) }
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
    val isSelectionMode = selectedItems.isNotEmpty()

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

    // ===== ФИЛЬТРЫ =====
    var selectedFilter by remember { mutableStateOf("Все") }

    val filters = listOf(
        "Все",
        "Верх",
        "Низ",
        "Обувь",
        "Верхняя",
        "Другое"
    )

    val filteredItems = items.filter { item ->
        when (selectedFilter) {
            "Все" -> true
            "Верх" -> isTop(item.type)
            "Низ" -> isBottom(item.type)
            "Обувь" -> isShoes(item.type)
            "Верхняя" -> isOuter(item.type)
            "Другое" -> !(isTop(item.type) || isBottom(item.type) || isShoes(item.type) || isOuter(item.type))
            else -> true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ================= TOP FILTER BAR =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { filter ->

                        val isSelected = filter == selectedFilter

                        ModernCategoryChip(
                            text = filter,
                            isSelected = isSelected,
                            onClick = { selectedFilter = filter }
                        )
                    }
                }

                if (showFilters) {
                    ModalBottomSheet(
                        onDismissRequest = { showFilters = false }
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(
                                text = "Фильтры",
                                style = MaterialTheme.typography.titleLarge
                            )

                            Spacer(Modifier.height(12.dp))

                            val filters = listOf("Все", "Верх", "Низ", "Обувь", "Верхняя")

                            filters.forEach { filter ->

                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        selectedFilter = filter   // 👈 твоя текущая логика
                                        showFilters = false
                                    }
                                ) {
                                    Text(filter)
                                }

                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            // ================= GRID =================
            LazyVerticalGrid(
                columns = GridCells.Adaptive(160.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {

                items(filteredItems, key = { it.id }) { item ->

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

                                    onClick = {
                                        if (isSelectionMode) {
                                            toggleSelect(item, selectedItems) {
                                                selectedItems = it
                                            }
                                        } else {
                                            navController.navigate("detail/${item.id}")
                                        }
                                    },

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
        }

        // ================= CREATE BUTTON =================
        if (selectedItems.size == 3) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )

            Button(
                onClick = {
                    val encoded = selectedItems.joinToString(",") {
                        it.id.toString()
                    }

                    navController.navigate("outfit_editor/$encoded")
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.9f)
                    .height(70.dp)
            ) {
                Text("✨ Редактировать образ")
            }
        }

        // ================= BUTTONS =================
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 📸 CAMERA
            IconButton(
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
                },
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0A84FF))
            ) {
                Icon(Icons.Default.PhotoCamera, null, tint = Color.White)
            }

            // 🖼 GALLERY
            IconButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF2F2F7))
            ) {
                Icon(Icons.Default.Image, null, tint = Color.Black)
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

@Composable
fun BigActionButton(
    icon: ImageVector,
    text: String,
    background: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.White.copy(alpha = 0.95f),
        tonalElevation = 10.dp,
        modifier = Modifier.shadow(20.dp, RoundedCornerShape(28.dp))
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = background),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.height(56.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text(text)
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

@Composable
fun ModernCategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    val (icon, color) = when (text) {
        "Все" -> Icons.Default.Apps to Color(0xFF9E9E9E)
        "Верх" -> Icons.Default.Checkroom to Color(0xFF42A5F5)
        "Низ" -> Icons.Default.Accessibility to Color(0xFF66BB6A)
        "Обувь" -> Icons.Default.DirectionsWalk to Color(0xFFFFA726)
        "Верхняя" -> Icons.Default.AcUnit to Color(0xFFAB47BC)
        else -> Icons.Default.Category to Color(0xFFBDBDBD)
    }

    val background = if (isSelected)
        color.copy(alpha = 0.15f)
    else
        MaterialTheme.colorScheme.surface

    val borderColor = if (isSelected)
        color
    else
        Color.LightGray.copy(alpha = 0.4f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = background,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = if (isSelected) 4.dp else 1.dp
    ) {

        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )

            Spacer(Modifier.width(6.dp))

            Text(
                text = text,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
