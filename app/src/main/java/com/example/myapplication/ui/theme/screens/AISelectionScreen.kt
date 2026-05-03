package com.example.myapplication.ui.theme.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import androidx.compose.material3.ripple
import androidx.compose.foundation.indication
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Save
import androidx.compose.ui.graphics.SolidColor

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
        "Классика", "Кэжуал", "Спорт", "Офис",
        "Вечеринка", "Минимализм", "Streetwear",
        "Романтика", "Смарт-кэжуал"
    )

    var style by remember { mutableStateOf("Кэжуал") }

    var temperature by remember { mutableStateOf("...") }
    var weatherDesc by remember { mutableStateOf("Загрузка...") }

    LaunchedEffect(Unit) {
        try {
            val result = loadWeather()
            temperature = result.first
            weatherDesc = result.second
        } catch (e: Exception) {
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

                LottieWeatherCard(
                    temperature = temperature,
                    description = weatherDesc
                )

                Spacer(Modifier.height(12.dp))

                DropdownField("Стиль", styles, style) { style = it }

                Spacer(Modifier.height(20.dp))
            }

            // ================= КНОПКА (ВАЖНО) =================
            FancyFAB(
                onClick = {

                    val tempValue =
                        temperature.replace("°C", "").toIntOrNull() ?: 20

                    val isCold = tempValue < 15
                    val isRain = weatherDesc.containsAny(
                        "дожд", "ливень", "гроза", "rain", "пасмурн"
                    )
                    val isBadWeather = isCold || isRain

                    val tops = items.filter { isTop(it.type) }
                    val bottoms = items.filter { isBottom(it.type) }
                    val dresses = items.filter { isDress(it.type) }
                    val outers = items.filter { isOuter(it.type) }
                    val shoes = items.filter { isShoes(it.type) }

                    outfitItems.clear()

                    val validShoes = shoes
                    if (validShoes.isEmpty()) return@FancyFAB

                    val top = tops.randomOrNull() ?: return@FancyFAB
                    val bottom = bottoms.randomOrNull() ?: return@FancyFAB

                    outfitItems.add(DraggableItem(top, Offset(100f, 50f)))
                    outfitItems.add(DraggableItem(bottom, Offset(120f, 220f)))
                    outfitItems.add(
                        DraggableItem(
                            validShoes.random(),
                            Offset(140f, 380f)
                        )
                    )

                    isCreated = outfitItems.size >= 2
                }
            )
        }

        // ================= RESULT =================
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
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
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Сохранить образ")
                }

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    onClick = {
                        isCreated = false
                        outfitItems.clear()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Black
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = SolidColor(Color.Black),
                        width = 1.dp
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Другой образ")
                }
            }
        }
    }
}

@Composable
fun FancyFAB(
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        label = ""
    )

    Box(modifier = Modifier.fillMaxSize()) {

        FloatingActionButton(
            onClick = onClick,
            interactionSource = interaction,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp) // 👈 ниже чем было
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .size(88.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 12.dp,
                pressedElevation = 6.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Создать образ",
                modifier = Modifier.size(34.dp)
            )
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
            shape = RoundedCornerShape(16.dp),

            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },

            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),

            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.LightGray,
                cursorColor = Color.Black,

                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Gray
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->

                DropdownMenuItem(
                    text = {
                        Text(
                            option,
                            color = if (option == selected) Color.Black else Color.DarkGray
                        )
                    },
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