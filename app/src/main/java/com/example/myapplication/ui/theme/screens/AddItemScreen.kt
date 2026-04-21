package com.example.myapplication.ui.theme.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.MyApp
import java.io.File

// ✅ РУССКИЕ СПИСКИ
val typeOptionsRu = listOf("Футболка", "Рубашка", "Джинсы", "Платье", "Обувь", "Головной убор")
val categoryOptionsRu = listOf("Верх", "Низ", "Обувь", "Головной убор", "Аксессуар")
val styleOptionsRu = listOf("Повседневный", "Уличный", "Классический", "Спортивный")
val materialOptionsRu = listOf("Хлопок", "Шерсть", "Кожа", "Джинса", "Синтетика", "Лён")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    imagePath: String,
    navController: NavController
) {
    val context = LocalContext.current

    val vm: ClosetViewModel = viewModel(
        factory = ClosetViewModelFactory(
            (context.applicationContext as MyApp).repo
        )
    )

    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(typeOptionsRu.first()) }
    var category by remember { mutableStateOf(categoryOptionsRu.first()) }
    var style by remember { mutableStateOf(styleOptionsRu.first()) }
    var material by remember { mutableStateOf(materialOptionsRu.first()) }

    var brand by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    var currentOptions by remember { mutableStateOf(listOf<String>()) }
    var onSelect by remember { mutableStateOf<(String) -> Unit>({}) }
    var showSheet by remember { mutableStateOf(false) }

    if (showSheet) {
        SelectBottomSheet(
            options = currentOptions,
            onSelect = {
                onSelect(it)
                showSheet = false
            },
            onDismiss = { showSheet = false }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

        item {
            AsyncImage(
                model = File(imagePath),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(8.dp)
            )
        }

        item {

            Column(modifier = Modifier.padding(16.dp)) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // 🔥 КАТЕГОРИЯ (теперь с материалом)
                SectionCard("Категория", Icons.Default.Checkroom) {

                    SelectRow(
                        label = "Тип одежды",
                        value = type,
                        icon = Icons.Default.Style
                    ) {
                        currentOptions = typeOptionsRu
                        onSelect = { type = it }
                        showSheet = true
                    }

                    SelectRow(
                        label = "Раздел",
                        value = category,
                        icon = Icons.Default.Category
                    ) {
                        currentOptions = categoryOptionsRu
                        onSelect = { category = it }
                        showSheet = true
                    }

                    SelectRow(
                        label = "Стиль",
                        value = style,
                        icon = Icons.Default.AutoAwesome
                    ) {
                        currentOptions = styleOptionsRu
                        onSelect = { style = it }
                        showSheet = true
                    }

                    // ✅ МАТЕРИАЛ ПЕРЕНЕСЁН СЮДА
                    SelectRow(
                        label = "Материал",
                        value = material,
                        icon = Icons.Default.Texture
                    ) {
                        currentOptions = materialOptionsRu
                        onSelect = { material = it }
                        showSheet = true
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 🔥 ДОПОЛНИТЕЛЬНО
                SectionCard("Дополнительно", Icons.Default.Tune) {

                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Бренд") },
                        leadingIcon = { Icon(Icons.Default.Business, null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Теги") },
                        leadingIcon = { Icon(Icons.Default.Tag, null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Цена") },
                        leadingIcon = { Icon(Icons.Default.Euro, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(32.dp))

                // ✅ ВАЖНО: поднимаем кнопку
                Button(
                    onClick = {
                        vm.add(imagePath, type, category, style, name)
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding() // 🔥 фикс Samsung
                        .padding(bottom = 8.dp)
                ) {
                    Text("Сохранить")
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(12.dp))

            content()
        }
    }
}

@Composable
fun SelectRow(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null)
            Spacer(Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall)
                Text(value)
            }

            Icon(Icons.Default.ArrowDropDown, null)
        }
    }

    Divider()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectBottomSheet(
    options: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {

    var search by remember { mutableStateOf("") }

    val filtered = options.filter {
        it.contains(search, true)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {

        Column(Modifier.padding(16.dp)) {

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Поиск") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            LazyColumn {
                items(filtered) { item ->
                    ListItem(
                        headlineContent = { Text(item) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(item) }
                    )
                }
            }
        }
    }
}