package com.example.myapplication.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.MyApp

@Composable
fun FavoritesScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    val vm: ClosetViewModel = viewModel(
        factory = ClosetViewModelFactory(
            (context.applicationContext as MyApp).repo
        )
    )

    val outfits by vm.outfits.collectAsState(initial = emptyList())

    val styles = listOf(
        "Все",
        "Классический",
        "Повседневный",
        "Спортивный",
        "Уличный"
    )

    val grouped = outfits.groupBy { it.style }

    var selectedStyle by remember { mutableStateOf("Все") }

    val visibleOutfits = when (selectedStyle) {
        "Все" -> outfits
        else -> outfits.filter { it.style == selectedStyle }
    }

    Column(modifier = modifier.fillMaxSize()) {

        Text(
            text = "Сохраненные образы",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            )
        )

        // ================= CHIPS =================
        LazyRow(
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            items(styles.size) { index ->

                val style = styles[index]

                val count = if (style == "Все") {
                    outfits.size
                } else {
                    grouped[style]?.size ?: 0
                }

                val selected = selectedStyle == style

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (selected) Color.Black else Color.Transparent)
                        .border(
                            1.dp,
                            if (selected) Color.Black else Color.LightGray,
                            RoundedCornerShape(50)
                        )
                        .clickable {
                            selectedStyle = style
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = style,
                        color = if (selected) Color.White else Color.Black
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = count.toString(),
                        color = if (selected) Color.White.copy(0.8f) else Color.Gray
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ================= EMPTY =================
        if (visibleOutfits.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Нет сохранённых образов")
            }
            return
        }

        // ================= GRID =================
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            items(visibleOutfits) { outfit ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.5f)
                        .clickable {
                            navController.navigate("outfit_view/${outfit.id}")
                        },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {

                    Box(modifier = Modifier.fillMaxSize()) {

                        AsyncImage(
                            model = outfit.previewUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )


                        IconButton(
                            onClick = { vm.deleteOutfit(outfit) },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}