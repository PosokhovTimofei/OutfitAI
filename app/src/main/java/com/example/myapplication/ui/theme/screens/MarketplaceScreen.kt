package com.example.myapplication.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

data class ClothingItem(
    val id: Int,
    val name: String,
    val price: String,
    val imageUrl: String
)

@Composable
fun MarketplaceScreen(modifier: Modifier = Modifier) {

    val items = remember {
        listOf(
            ClothingItem(1, "Худи", "₽3 999", "https://static.vecteezy.com/system/resources/previews/035/440/087/non_2x/ai-generated-white-hoodie-isolated-on-transparent-background-free-png.png"),
            ClothingItem(2, "Платье", "₽2 499", "https://static.vecteezy.com/system/resources/previews/035/645/569/non_2x/ai-generated-woman-dress-isolated-on-transparent-background-created-with-generative-ai-free-png.png"),
            ClothingItem(3, "Футболка", "₽1 499", "https://png.pngtree.com/png-clipart/20240306/original/pngtree-white-t-shirt-with-hanger-png-image_14523532.png"),
            ClothingItem(4, "Джинсы", "₽4 299", "https://static.vecteezy.com/system/resources/previews/040/524/904/non_2x/ai-generated-blue-jeans-on-transparent-background-ai-generated-free-png.png"),
            ClothingItem(5, "Пальто", "₽7 999", "https://static.trendme.net/temp/thumbs/1200-630-2-90/Grey-Lapel-Collar-Duster-Coat-_Jakne-i-kaputi-MoonStone-full-24357-617639.png"),
            ClothingItem(6, "Юбка", "₽2 999", "https://static.vecteezy.com/system/resources/previews/032/067/537/non_2x/white-female-skirt-ai-generative-free-png.png")
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.padding(8.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(items) { item ->
            ProductCard(item)
        }
    }
}

@Composable
fun ProductCard(item: ClothingItem) {

    var liked by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {

        Column {

            Box {

                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                IconButton(
                    onClick = { liked = !liked },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(if (liked) "❤️" else "🤍")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Text(
                text = item.price,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}