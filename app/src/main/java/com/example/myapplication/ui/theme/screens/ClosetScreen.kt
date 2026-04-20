package com.example.myapplication.ui.theme.screens

import android.net.Uri
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.MyApp
import com.example.myapplication.data.ImageStorage
import java.io.File

val typeOptions = listOf("shirt", "tshirt", "jeans", "dress", "shoes", "hat")
val categoryOptions = listOf("top", "bottom", "shoes", "hat", "accessory")
val styleOptions = listOf("casual", "streetwear", "classic", "sport")

@Composable
fun ClosetScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    val vm: ClosetViewModel = viewModel(
        factory = ClosetViewModelFactory(
            (context.applicationContext as MyApp).repo
        )
    )

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var savedPath by remember { mutableStateOf<String?>(null) }
    var showForm by remember { mutableStateOf(false) }

    var label by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(typeOptions.first()) }
    var category by remember { mutableStateOf(categoryOptions.first()) }
    var style by remember { mutableStateOf(styleOptions.first()) }

    val items by vm.items.collectAsState()

    // 📸 PICK IMAGE
    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                selectedImageUri = it
                savedPath = ImageStorage.saveToInternalStorage(context, it)
                showForm = true
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
                            model = File(item.imageUri),
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

        // FORM
        if (showForm && savedPath != null) {

            AlertDialog(
                onDismissRequest = {
                    showForm = false
                    savedPath = null
                    label = ""
                },
                confirmButton = {
                    Button(onClick = {
                        vm.add(
                            savedPath!!,
                            type,
                            category,
                            style,
                            label
                        )
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
                            label = { Text("Название") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(10.dp))

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

        // BUTTON
        Button(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            onClick = {
                galleryLauncher.launch("image/*")
            }
        ) {
            Text("🖼 Добавить фото")
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