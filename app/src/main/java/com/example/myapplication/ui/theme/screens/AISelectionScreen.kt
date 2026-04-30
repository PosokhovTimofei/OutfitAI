package com.example.myapplication.ui.theme.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.drawToBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.MyApp
import com.example.myapplication.data.ClosetItemEntity
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

// ================= STATE =================

data class DraggableItem(
    val item: ClosetItemEntity,
    var offset: Offset = Offset(100f, 100f)
)

fun isTop(type: String) = type.containsAny(
    "футболк", "рубашк", "лонгслив", "худи", "свит", "поло", "топ", "блуз", "майк", "корсет"
)

fun isBottom(type: String) = type.containsAny(
    "джинс", "брюк", "шорт", "юбк", "леггинс", "джоггер"
)

fun isDress(type: String) = type.containsAny(
    "плать", "сарафан"
)

fun isOuter(type: String) = type.containsAny(
    "куртк", "пальто", "пиджак", "ветровк", "пуховик", "тренч", "кардиган", "бомбер", "косух"
)

fun isShoes(type: String) = type.containsAny(
    "кроссов", "кед", "ботинк", "туфл", "сандал", "шлеп", "сапог", "лофер", "каблук"
)

// helper
fun String.containsAny(vararg words: String): Boolean {
    return words.any { this.contains(it, true) }
}

@Composable
fun GenerateOutfitScreen(
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val vm: ClosetViewModel = viewModel(
        factory = ClosetViewModelFactory(
            (context.applicationContext as MyApp).repo
        )
    )

    val items by vm.items.collectAsState(initial = emptyList())

    val styles = listOf("Кэжуал", "Спорт", "Офис", "Вечеринка")
    val events = listOf("Прогулка", "Работа", "Свидание", "Тренировка")

    var style by remember { mutableStateOf("Кэжуал") }
    var event by remember { mutableStateOf("Прогулка") }

    // ================= WEATHER STATE =================
    var temperature by remember { mutableStateOf("...") }
    var weatherDesc by remember { mutableStateOf("Загрузка...") }

    LaunchedEffect(Unit) {
        val result = loadWeather()
        temperature = result.first
        weatherDesc = result.second
    }

    val outfitItems = remember { mutableStateListOf<DraggableItem>() }
    var isCreated by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {

        // ================= INPUT =================
        if (!isCreated) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {

                // ===== WEATHER CARD =====
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Погода")
                        Spacer(Modifier.height(6.dp))
                        Text("🌡 $temperature")
                        Text("☁️ $weatherDesc")
                    }
                }

                Spacer(Modifier.height(12.dp))

                DropdownField("Стиль", styles, style) { style = it }

                Spacer(Modifier.height(12.dp))

                DropdownField("Мероприятие", events, event) { event = it }

                Spacer(Modifier.height(20.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    onClick = {

                        val base = items

                        val tops = base.filter { isTop(it.type) }
                        val bottoms = base.filter { isBottom(it.type) }
                        val dresses = base.filter { isDress(it.type) }
                        val outers = base.filter { isOuter(it.type) }
                        val shoes = base.filter { isShoes(it.type) }

// 👉 температура
                        val tempValue = temperature.replace("°C", "").toIntOrNull() ?: 20

                        outfitItems.clear()

// ================= СЦЕНАРИИ =================

// 👗 ПЛАТЬЕ (свидание / вечеринка)
                        if ((event == "Свидание" || event == "Прогулка") &&
                            dresses.isNotEmpty() &&
                            shoes.isNotEmpty() &&
                            (0..1).random() == 1
                        ) {
                            outfitItems.add(DraggableItem(dresses.random(), Offset(120f, 120f)))
                            outfitItems.add(DraggableItem(shoes.random(), Offset(140f, 380f)))

                            if (tempValue < 15 && outers.isNotEmpty()) {
                                outfitItems.add(DraggableItem(outers.random(), Offset(100f, 40f)))
                            }

                            isCreated = true
                            return@Button
                        }

// 👔 ОФИС
                        if (style == "Офис") {
                            val officeTop = tops.filter { it.type.containsAny("рубашк", "блуз") }
                            val officeBottom = bottoms.filter { it.type.containsAny("брюк", "юбк") }

                            if (officeTop.isNotEmpty() && officeBottom.isNotEmpty() && shoes.isNotEmpty()) {
                                outfitItems.add(DraggableItem(officeTop.random(), Offset(100f, 50f)))
                                outfitItems.add(DraggableItem(officeBottom.random(), Offset(120f, 220f)))
                                outfitItems.add(DraggableItem(shoes.random(), Offset(140f, 380f)))

                                if (outers.isNotEmpty()) {
                                    outfitItems.add(DraggableItem(outers.random(), Offset(90f, 0f)))
                                }

                                isCreated = true
                                return@Button
                            }
                        }

// 🏃 СПОРТ
                        if (style == "Спорт") {
                            val sportTop = tops.filter { it.type.containsAny("худи", "свит", "футболк") }
                            val sportBottom = bottoms.filter { it.type.containsAny("шорт", "джоггер", "леггинс") }
                            val sportShoes = shoes.filter { it.type.contains("кроссов", true) }

                            if (sportTop.isNotEmpty() && sportBottom.isNotEmpty() && sportShoes.isNotEmpty()) {
                                outfitItems.add(DraggableItem(sportTop.random(), Offset(100f, 50f)))
                                outfitItems.add(DraggableItem(sportBottom.random(), Offset(120f, 220f)))
                                outfitItems.add(DraggableItem(sportShoes.random(), Offset(140f, 380f)))

                                isCreated = true
                                return@Button
                            }
                        }

// 🌤 КЭЖУАЛ (универсал)
                        if (tops.isNotEmpty() && bottoms.isNotEmpty() && shoes.isNotEmpty()) {

                            outfitItems.add(DraggableItem(tops.random(), Offset(100f, 50f)))
                            outfitItems.add(DraggableItem(bottoms.random(), Offset(120f, 220f)))
                            outfitItems.add(DraggableItem(shoes.random(), Offset(140f, 380f)))

                            if (tempValue < 15 && outers.isNotEmpty()) {
                                outfitItems.add(DraggableItem(outers.random(), Offset(90f, 0f)))
                            }

                            isCreated = true
                        }
                    }
                ) {
                    Text("Создать образ")
                }
            }
        }

        // ================= EDITOR =================
        if (isCreated) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 140.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .height(500.dp)
                        .background(Color.White)
                ) {
                    outfitItems.forEach {
                        DraggableImage(it)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {

                        scope.launch {

                            val bitmap = view.drawToBitmap()

                            val cropped = Bitmap.createBitmap(
                                bitmap,
                                0,
                                0,
                                bitmap.width,
                                (bitmap.height * 0.70f).toInt()
                            )

                            val file = File(
                                context.cacheDir,
                                "outfit_${System.currentTimeMillis()}.png"
                            )

                            FileOutputStream(file).use {
                                cropped.compress(Bitmap.CompressFormat.PNG, 100, it)
                            }

                            vm.saveOutfit(
                                itemIds = outfitItems.map { it.item.id },
                                states = outfitItems.map {
                                    OutfitItemState(
                                        itemId = it.item.id,
                                        x = it.offset.x,
                                        y = it.offset.y,
                                        scale = 1f
                                    )
                                },
                                previewUri = file.absolutePath
                            )
                        }
                    }
                ) {
                    Text("💾 Сохранить образ")
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        isCreated = false
                        outfitItems.clear()
                    }
                ) {
                    Text("🔄 Другой образ")
                }
            }
        }
    }
}
// ================= DRAG =================

@Composable
fun DraggableImage(dragItem: DraggableItem) {

    var offsetX by remember { mutableStateOf(dragItem.offset.x) }
    var offsetY by remember { mutableStateOf(dragItem.offset.y) }

    val bitmap = remember(dragItem.item.imageUri) {
        BitmapFactory.decodeFile(dragItem.item.imageUri)
    }

    if (bitmap != null) {

        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .offset {
                    IntOffset(offsetX.roundToInt(), offsetY.roundToInt())
                }
                .size(150.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()

                        offsetX += dragAmount.x
                        offsetY += dragAmount.y

                        dragItem.offset = Offset(offsetX, offsetY)
                    }
                }
        )
    }
}

// ================= FIELD =================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {

        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

suspend fun loadWeather(): Pair<String, String> = withContext(Dispatchers.IO) {
    try {
        val apiKey = "3753e020ec2b83a2f4639e411783a0aa"
        val lat = 55.75
        val lon = 37.62

        val url = URL(
            "https://api.openweathermap.org/data/2.5/weather" +
                    "?lat=$lat&lon=$lon&units=metric&lang=ru&appid=$apiKey"
        )

        val connection = url.openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "GET"

        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream   // 🔥 ВАЖНО
        }

        val response = stream.bufferedReader().readText()

        val json = JSONObject(response)

        // если ошибка API
        if (json.has("cod") && json.getString("cod") != "200") {
            return@withContext "—" to json.optString("message", "ошибка API")
        }

        val temp = json.getJSONObject("main")
            .getDouble("temp")
            .toInt()
            .toString() + "°C"

        val desc = json.getJSONArray("weather")
            .getJSONObject(0)
            .getString("description")

        temp to desc

    } catch (e: Exception) {
        e.printStackTrace()
        "—" to "нет данных"
    }
}