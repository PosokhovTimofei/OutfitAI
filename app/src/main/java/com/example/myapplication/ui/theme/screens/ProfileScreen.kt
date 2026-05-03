package com.example.myapplication.ui.theme.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.MyApp

@Composable
fun ProfileScreen(
    vm: ClosetViewModel = viewModel(
        factory = ClosetViewModelFactory(
            (LocalContext.current.applicationContext as MyApp).repo
        )
    )
) {

    val items by vm.items.collectAsState()
    val outfits by vm.outfits.collectAsState()

    val profileName by vm.profileName.collectAsState()
    var editMode by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(profileName) }

    LaunchedEffect(profileName) {
        tempName = profileName
    }

    val totalItems = items.size
    val totalOutfits = outfits.size

    val favoriteStyle = items.groupBy { it.style }
        .maxByOrNull { it.value.size }?.key ?: "—"

    val topType = items.groupBy { it.type }
        .maxByOrNull { it.value.size }?.key ?: "—"

    val topColor = items.mapNotNull { it.color }
        .groupBy { it }
        .maxByOrNull { it.value.size }?.key ?: "—"

    val totalBrands = items.mapNotNull { it.brand }.distinct().size

    val avgPrice = items.mapNotNull { it.price?.toIntOrNull() }
        .takeIf { it.isNotEmpty() }
        ?.average()?.toInt() ?: 0

    val styleScore = (
            (totalItems.coerceAtMost(50) / 50f) * 40 +
                    (totalOutfits.coerceAtMost(20) / 20f) * 30 +
                    (totalBrands.coerceAtMost(10) / 10f) * 30
            ).toInt().coerceIn(0, 100)

    val insight = when {
        styleScore < 40 -> "AI: гардероб можно усилить 🔝"
        styleScore < 70 -> "AI: хороший стиль, но есть потенциал ⚡"
        else -> "AI: стиль как у стилиста 🔥"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        item {
            Text(
                "Профиль",
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))
        }

        // ===== HEADER =====
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {

                    if (!editMode) {

                        Text(
                            profileName ?: "Имя",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            "$favoriteStyle • $totalItems вещей",
                            color = Color.Gray
                        )

                        Spacer(Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = styleScore / 100f,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Black,
                            trackColor = Color(0xFFE0E0E0)
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            "ИИ оценка: $styleScore / 100",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    } else {

                        TextField(
                            value = tempName ?: "",
                            onValueChange = { tempName = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,

                                focusedIndicatorColor = Color.Black,
                                unfocusedIndicatorColor = Color.LightGray,
                                cursorColor = Color.Black,

                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,

                                focusedLabelColor = Color.Black,
                                unfocusedLabelColor = Color.Gray,


                                selectionColors = TextSelectionColors(
                                    handleColor = Color.Black,
                                    backgroundColor = Color(0x33222222)
                                )
                            )
                        )

                        Spacer(Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                            Button(
                                onClick = {
                                    vm.updateProfileName(tempName ?: "")
                                    editMode = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Сохранить")
                            }

                            OutlinedButton(
                                onClick = {
                                    tempName = profileName
                                    editMode = false
                                }
                            ) {
                                Text("Отмена")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }

        // ===== STATS =====
        item {
            Text("Статистика", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(12.dp))
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Вещи", totalItems)
                StatCard("Образы", totalOutfits)
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Бренды", totalBrands)
                StatCard("Стили", items.map { it.style }.distinct().size)
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTextCard("Тип", topType)
                StatTextCard("Цвет", topColor, showDot = true)
            }

            Spacer(Modifier.height(12.dp))

            StatWideCard("Средняя цена", "$avgPrice ₽")

            Spacer(Modifier.height(12.dp))

            StatWideCard("Любимый стиль", favoriteStyle)

            Spacer(Modifier.height(20.dp))
        }

        item {
            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    insight,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(24.dp))
        }

        item {
            Button(
                onClick = {
                    tempName = profileName
                    editMode = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Редактировать профиль")
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

/* ================= COMPONENTS ================= */

@Composable
fun RowScope.StatCard(title: String, value: Int) {
    val anim = remember { Animatable(0f) }

    LaunchedEffect(value) {
        anim.animateTo(value.toFloat(), tween(800))
    }

    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .weight(1f)
            .height(100.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Color.Gray)
            Text(
                anim.value.toInt().toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RowScope.StatTextCard(
    title: String,
    value: String,
    showDot: Boolean = false
) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .weight(1f)
            .height(100.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Text(title, color = Color.Gray)

            Spacer(Modifier.height(6.dp))

            Row {
                if (showDot) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .background(Color.Black, CircleShape)
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(value)
            }
        }
    }
}

@Composable
fun StatWideCard(title: String, value: String) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Color.Gray)
            Spacer(Modifier.height(6.dp))
            Text(value, fontSize = 18.sp)
        }
    }
}