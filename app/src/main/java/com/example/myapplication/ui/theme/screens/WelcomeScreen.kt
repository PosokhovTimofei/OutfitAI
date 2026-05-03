package com.example.myapplication.ui.theme.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(onStart: () -> Unit) {

    var visible by remember { mutableStateOf(false) }

    // ⏱ авто-переход через 2 секунды
    LaunchedEffect(Unit) {
        visible = true
        delay(1000)
        onStart()
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(800)) +
                    scaleIn(initialScale = 0.92f, animationSpec = tween(800))
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "OutfitAI",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = androidx.compose.ui.graphics.Color.Black
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "AI wardrobe stylist",
                    style = MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.ui.graphics.Color.Gray
                )
            }
        }
    }
}