package com.example.myapplication.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

data class ClosetItem(
    val id: Int,
    val image: String
)

@Composable
fun ClosetScreen(modifier: Modifier = Modifier) {

    val items = listOf(
        ClosetItem(1, "https://source.unsplash.com/300x300/?hoodie"),
        ClosetItem(2, "https://source.unsplash.com/300x300/?glasses"),
        ClosetItem(3, "https://source.unsplash.com/300x300/?sneakers"),
        ClosetItem(4, "https://source.unsplash.com/300x300/?shorts"),
        ClosetItem(5, "https://source.unsplash.com/300x300/?tshirt")
    )

    Column(modifier.padding(16.dp)) {

        Text("CLOSET", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(columns = GridCells.Fixed(3)) {
            items(items) {
                AsyncImage(
                    model = it.image,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(100.dp)
                )
            }
        }
    }
}