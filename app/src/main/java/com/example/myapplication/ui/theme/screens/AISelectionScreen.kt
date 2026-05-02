package com.example.myapplication.ui.theme.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import com.example.myapplication.R

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

fun isLightClothes(type: String): Boolean {
    return type.containsAny(
        "шорт",
        "юбк",
        "майк",
        "топ",
        "сарафан"
    )
}



fun isSneakers(type: String) = type.containsAny("кроссов", "кед")
fun isBoots(type: String) = type.containsAny("ботинк", "сапог")
fun isClassicShoes(type: String) = type.containsAny("туф", "лофер", "каблук")
fun isSummerShoes(type: String) = type.containsAny("сандал", "шлеп")

fun isWindbreaker(type: String) =
    type.containsAny("ветровк", "анорак")
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

    val styles = listOf(
        "Классика",
        "Кэжуал",
        "Спорт",
        "Офис",
        "Вечеринка",
        "Минимализм",
        "Streetwear",
        "Романтика",
        "Смарт-кэжуал"
    )

    var style by remember { mutableStateOf("Кэжуал") }

    // ===== WEATHER =====
    var temperature by remember { mutableStateOf("...") }
    var weatherDesc by remember { mutableStateOf("Загрузка...") }

    LaunchedEffect(Unit) {
        try {
            Log.d("WEATHER", "Start loading weather")

            val result = loadWeather()

            Log.d("WEATHER", "Loaded: $result")

            temperature = result.first
            weatherDesc = result.second

        } catch (e: Exception) {
            Log.e("WEATHER", "FAILED", e)
            temperature = "20°C"
            weatherDesc = "Ошибка загрузки"
        }
    }

    val outfitItems = remember { mutableStateListOf<DraggableItem>() }
    var isCreated by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {

        if (!isCreated) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {

                // ===== WEATHER =====
                LottieWeatherCard(
                    temperature = temperature,
                    description = weatherDesc
                )

                Spacer(Modifier.height(12.dp))

                DropdownField("Стиль", styles, style) { style = it }

                Spacer(Modifier.height(20.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    onClick = {

                        val tempValue = temperature.replace("°C", "").toIntOrNull() ?: 20

                        val isCold = tempValue < 15
                        val isRain = weatherDesc.containsAny("дожд", "ливень", "гроза", "rain", "пасмурн")
                        val isBadWeather = isCold || isRain

                        val ignoreWeather = style in listOf("Вечеринка", "Романтика")

                        val tops = items.filter { isTop(it.type) }
                        val bottoms = items.filter { isBottom(it.type) }
                        val dresses = items.filter { isDress(it.type) }
                        val outers = items.filter { isOuter(it.type) }
                        val shoes = items.filter { isShoes(it.type) }

                        outfitItems.clear()

                        // ================= PROFILE =================
                        data class Profile(
                            val top: List<String>,
                            val bottom: List<String>,
                            val shoesFilter: (ClosetItemEntity) -> Boolean,
                            val allowDress: Boolean,
                            val allowOuterwear: Boolean
                        )

                        val profile = when (style) {

                            "Классика" -> Profile(
                                top = listOf("рубашк", "блуз", "пиджак"),
                                bottom = listOf("брюк", "юбк"),
                                shoesFilter = { isClassicShoes(it.type) },
                                allowDress = true,
                                allowOuterwear = true
                            )

                            "Офис" -> Profile(
                                top = listOf("рубашк", "блуз", "пиджак"),
                                bottom = listOf("брюк", "юбк"),
                                shoesFilter = { isSneakers(it.type) || isClassicShoes(it.type) }, // 👈 БЕЗ БОТИНОК
                                allowDress = true,
                                allowOuterwear = false
                            )

                            "Спорт" -> Profile(
                                top = listOf("худи", "футболк", "лонгслив"),
                                bottom = listOf("джоггер", "леггинс"),
                                shoesFilter = { isSneakers(it.type) },
                                allowDress = false,
                                allowOuterwear = true
                            )

                            "Смарт-кэжуал" -> Profile(
                                top = listOf("рубашк", "поло", "свитер"),
                                bottom = listOf("джинс", "брюк"),
                                shoesFilter = { isSneakers(it.type) || isClassicShoes(it.type) },
                                allowDress = true,
                                allowOuterwear = true
                            )

                            "Streetwear" -> Profile(
                                top = listOf("худи", "свитшот"),
                                bottom = listOf("джинс"),
                                shoesFilter = { isSneakers(it.type) },
                                allowDress = false,
                                allowOuterwear = true
                            )

                            "Романтика", "Вечеринка" -> Profile(
                                top = listOf("топ", "блуз"),
                                bottom = listOf("юбк"),
                                shoesFilter = { isClassicShoes(it.type) },
                                allowDress = true,
                                allowOuterwear = true
                            )

                            else -> Profile(
                                emptyList(),
                                emptyList(),
                                { true },
                                true,
                                true
                            )
                        }

                        // ================= ОБУВЬ =================
                        val validShoes = shoes.filter { profile.shoesFilter(it) }

                        if (validShoes.isEmpty()) return@Button

                        // ================= ПЛАТЬЕ =================
                        val useDress =
                            profile.allowDress &&
                                    dresses.isNotEmpty() &&
                                    !isBadWeather &&
                                    (0..100).random() < 40

                        if (useDress) {

                            val dress = dresses.random()
                            outfitItems.add(DraggableItem(dress, Offset(120f, 120f)))
                            outfitItems.add(DraggableItem(validShoes.random(), Offset(140f, 380f)))

                        } else {

                            val top = tops
                                .filter { profile.top.isEmpty() || it.type.containsAny(*profile.top.toTypedArray()) }
                                .randomOrNull() ?: return@Button

                            val bottom = bottoms
                                .filter { profile.bottom.isEmpty() || it.type.containsAny(*profile.bottom.toTypedArray()) }
                                .randomOrNull() ?: return@Button

                            outfitItems.add(DraggableItem(top, Offset(100f, 50f)))
                            outfitItems.add(DraggableItem(bottom, Offset(120f, 220f)))
                            outfitItems.add(DraggableItem(validShoes.random(), Offset(140f, 380f)))
                        }

                        // ================= OUTERWEAR =================
                        if (profile.allowOuterwear) {

                            val outerCandidate = when (style) {

                                "Спорт" -> outers.filter { isWindbreaker(it.type) }

                                else -> outers
                            }

                            // 🔥 ВАЖНО: максимум 1 слой ВСЕГДА
                            if (outerCandidate.isNotEmpty()) {

                                val shouldAddOuter =
                                    when {
                                        isBadWeather -> true
                                        isCold -> (0..100).random() < 50
                                        else -> false
                                    }

                                if (shouldAddOuter) {
                                    outerCandidate.randomOrNull()?.let {
                                        outfitItems.add(DraggableItem(it, Offset(90f, 0f)))
                                    }
                                }
                            }
                        }

                        if (outfitItems.size >= 2) {
                            isCreated = true
                        }
                    }
                ) {
                    Text("Создать образ")
                }
            }
        }

        // ===== EDITOR (без изменений) =====
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
                    .padding(16.dp)
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        scope.launch {

                            // 1. собираем состояния
                            val states = outfitItems.map {
                                OutfitItemState(
                                    itemId = it.item.id,
                                    x = it.offset.x,
                                    y = it.offset.y,
                                    scale = 1f,     // пока нет зума — ставим 1
                                    zIndex = 0f     // пока нет слоёв — 0
                                )
                            }

                            val itemIds = outfitItems.map { it.item.id }

                            // 2. делаем скрин
                            val fullBitmap = view.drawToBitmap()

                            // 3. обрезаем низ (где кнопки)
                            val croppedHeight = (fullBitmap.height * 0.70f).toInt()

                            val croppedBitmap = Bitmap.createBitmap(
                                fullBitmap,
                                0,
                                0,
                                fullBitmap.width,
                                croppedHeight
                            )

                            // 4. сохраняем файл
                            val file = File(
                                context.cacheDir,
                                "outfit_${System.currentTimeMillis()}.png"
                            )

                            FileOutputStream(file).use {
                                croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                            }

                            // 5. сохраняем через ViewModel
                            vm.saveOutfit(
                                itemIds = itemIds,
                                states = states,
                                previewUri = file.absolutePath
                            )

                            // 6. сброс UI
                            isCreated = false
                            outfitItems.clear()
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

@Composable
fun LottieWeatherCard(
    temperature: String,
    description: String
) {
    val weatherType = remember(description) {
        when {
            description.contains("дожд", true) -> "rain"
            description.contains("снег", true) -> "snow"
            description.contains("ясн", true) -> "sun"
            else -> "cloud"
        }
    }

    val resId = when (weatherType) {
        "sun" -> R.raw.sun
        "rain" -> R.raw.rain
        "snow" -> R.raw.snow
        else -> R.raw.cloud
    }

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(resId)
    )

    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )

    // 🌊 анимации движения карточки
    val infinite = rememberInfiniteTransition(label = "weather_card")

    val float by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val scale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            tween(3500),
            RepeatMode.Reverse
        ),
        label = "scale"
    )

    // 🌌 ТЁМНЫЙ ПРЕМИУМ ФОН
    val background = when (weatherType) {

        "sun" -> Brush.radialGradient(
            colors = listOf(
                Color(0xFF1A1A2E),
                Color(0xFF16213E),
                Color(0xFF0F3460)
            )
        )

        "rain" -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0B1320),
                Color(0xFF1B2A41),
                Color(0xFF324A5F)
            )
        )

        "snow" -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F172A),
                Color(0xFF1E293B),
                Color(0xFF334155)
            )
        )

        else -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0B0F1A),
                Color(0xFF151B2E),
                Color(0xFF1C2541)
            )
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = float
            },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(16.dp)
        ) {

            // 🌑 ВИНЬЕТКА (глубина как в iOS)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xAA000000)
                            ),
                            radius = 900f
                        )
                    )
            )

            // ✨ СВЕЧЕНИЕ (атмосфера)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            radius = 700f
                        )
                    )
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // 🌤 Lottie (слегка “плавает”)
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer {
                            translationY = -float / 2
                        }
                )

                Spacer(Modifier.width(12.dp))

                Column {

                    Text(
                        text = temperature,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            // 🌬 декоративные частицы атмосферы
            Text(
                text = "✦  ✦   ✦",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .graphicsLayer {
                        alpha = 0.25f
                        translationY = float
                    },
                color = Color.White
            )
        }
    }
}