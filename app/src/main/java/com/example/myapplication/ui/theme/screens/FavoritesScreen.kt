package com.example.myapplication.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.MyApp

@Composable
fun FavoritesScreen(modifier: Modifier = Modifier) {

    val context = LocalContext.current

    val vm: ClosetViewModel = viewModel(
        factory = ClosetViewModelFactory(
            (context.applicationContext as MyApp).repo
        )
    )

    val outfits by vm.outfits.collectAsState(initial = emptyList())

    Column(modifier = modifier.fillMaxSize()) {

        Text(
            text = "Избранные образы ❤️",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        if (outfits.isEmpty()) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Пока нет сохранённых образов")
            }

        } else {

            // ================= GRID =================
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                items(outfits) { outfit ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.5f) // 🔥 ВСЕ КАРТОЧКИ ОДИНАКОВЫЕ (vertical fashion style)
                    ) {

                        Column {

                            // ================= IMAGE =================
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {

                                if (!outfit.previewUri.isNullOrEmpty()) {

                                    AsyncImage(
                                        model = outfit.previewUri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,      // 🔥 ОБРЕЗКА
                                        alignment = Alignment.TopCenter,       // 🔥 РЕЖЕМ СНИЗУ
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(300.dp)
                                    )

                                } else {

                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No preview")
                                    }
                                }
                            }

                            // ================= INFO =================
                            Column(
                                modifier = Modifier.padding(6.dp)
                            ) {

                                Text(
                                    text = "Outfit #${outfit.id}",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1
                                )

                                Text(
                                    text = "${outfit.itemIds.split(",").size} items",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}