package com.example.myapplication.ui.theme.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.MyApp
import com.example.myapplication.data.ClosetItemEntity

@Composable
fun GenerateOutfitScreen(
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    val vm: ClosetViewModel = viewModel(
        factory = ClosetViewModelFactory(
            (context.applicationContext as MyApp).repo
        )
    )

    val items by vm.items.collectAsState(initial = emptyList())

    var style by remember { mutableStateOf("") }
    var event by remember { mutableStateOf("") }

    val weather = "🌧 Дождь, +12°C"

    var top by remember { mutableStateOf<ClosetItemEntity?>(null) }
    var bottom by remember { mutableStateOf<ClosetItemEntity?>(null) }
    var shoes by remember { mutableStateOf<ClosetItemEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {

        // ===== INPUTS =====

        SmallField("Погода", weather, enabled = false)
        Spacer(Modifier.height(12.dp))

        SmallField("Стиль", style) { style = it }
        Spacer(Modifier.height(12.dp))

        SmallField("Мероприятие", event) { event = it }

        Spacer(Modifier.height(20.dp))

        // ===== RESULT =====

        if (top != null && bottom != null && shoes != null) {

            Text("Образ:", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(12.dp))

            OutfitColumn(
                top = top!!,
                bottom = bottom!!,
                shoes = shoes!!
            )

            Spacer(Modifier.height(20.dp))
        }

        // ===== BUTTON =====

        Button(
            onClick = {

                val tops = items.filter {
                    it.category.contains("верх", ignoreCase = true)
                }

                val bottoms = items.filter {
                    it.category.contains("низ", ignoreCase = true)
                }

                val shoesList = items.filter {
                    it.category.contains("обув", ignoreCase = true)
                }

                if (tops.isNotEmpty() && bottoms.isNotEmpty() && shoesList.isNotEmpty()) {
                    top = tops.random()
                    bottom = bottoms.random()
                    shoes = shoesList.random()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text("Создать образ")
        }

        Spacer(Modifier.height(20.dp))
    }
}

// ===== МАЛЕНЬКОЕ ПОЛЕ =====

@Composable
fun SmallField(
    label: String,
    value: String,
    enabled: Boolean = true,
    onChange: (String) -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        enabled = enabled,
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        label = { Text(label) }
    )
}

// ===== ВЕРТИКАЛЬНЫЙ ОБРАЗ =====

@Composable
fun OutfitColumn(
    top: ClosetItemEntity,
    bottom: ClosetItemEntity,
    shoes: ClosetItemEntity
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        OutfitItem(top)
        OutfitItem(bottom)
        OutfitItem(shoes)
    }
}

// ===== ЭЛЕМЕНТ =====

@Composable
fun OutfitItem(item: ClosetItemEntity) {

    val bitmap = remember(item.imageUri) {
        BitmapFactory.decodeFile(item.imageUri)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Fit
            )
        }
    }
}