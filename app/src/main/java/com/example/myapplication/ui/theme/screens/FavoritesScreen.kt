package com.example.myapplication.ui.theme.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Column(modifier = modifier.fillMaxSize()) {

        Text(
            text = "Сохранённые образы:",
            style = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.5).sp,
                color = Color.Black
            ),
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
                            .aspectRatio(0.5f),
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 8.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {

                        Box(modifier = Modifier.fillMaxSize()) {

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {
                                        navController.navigate("outfit_view/${outfit.id}")
                                    }
                            ) {

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
                                            contentScale = ContentScale.Crop,
                                            alignment = Alignment.TopCenter,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                    }
                                }
                            }

                            IconButton(
                                onClick = {
                                    vm.deleteOutfit(outfit)
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Удалить",
                                    tint = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
