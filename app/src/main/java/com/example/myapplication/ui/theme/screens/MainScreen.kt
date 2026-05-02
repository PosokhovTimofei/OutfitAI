package com.example.myapplication.ui.theme.screens

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.myapplication.ui.theme.components.BottomBar
import androidx.compose.runtime.getValue

@Composable
fun MainScreen() {

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val hideBottomBarRoutes = listOf(
        "editor/{itemId}",
        "addItem/{imagePath}",
        "outfit_editor/{ids}"
    )


    val selectedIndex = when (currentRoute) {
        "home" -> 0
        "closet" -> 1
        "create" -> 2
        "favorites" -> 3
        "profile" -> 4
        else -> 0
    }

    val shouldShowBottomBar = currentRoute !in hideBottomBarRoutes

    Scaffold(
        bottomBar = {

            if (shouldShowBottomBar) {

                BottomBar(
                    selected = selectedIndex,
                    onSelect = { index ->
                        when (index) {
                            0 -> navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                            1 -> navController.navigate("closet") {
                                popUpTo("closet") { inclusive = true }
                            }
                            2 -> navController.navigate("create")
                            3 -> navController.navigate("favorites")
                            4 -> navController.navigate("profile")
                        }
                    }
                )
            }
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = "closet",
            modifier = Modifier.padding(padding)
        ) {

            composable("home") { MarketplaceScreen() }
            composable("closet") { ClosetScreen(navController) }
            composable("create") { GenerateOutfitScreen() }
            composable("favorites") { FavoritesScreen(navController) }
            composable("profile") { ProfileScreen() }

            composable("detail/{id}") { backStackEntry ->
                val id = backStackEntry.arguments
                    ?.getString("id")
                    ?.toLongOrNull() ?: return@composable

                ClosetDetailScreen(itemId = id, navController = navController)
            }

            composable("addItem/{imagePath}") { backStackEntry ->
                val path = backStackEntry.arguments?.getString("imagePath")

                if (path != null) {
                    AddItemScreen(
                        imagePath = Uri.decode(path),
                        navController = navController
                    )
                }
            }

            composable("editor/{itemId}") { backStackEntry ->
                val itemId = backStackEntry.arguments
                    ?.getString("itemId")
                    ?.toLongOrNull() ?: return@composable

                EditorScreen(
                    itemId = itemId,
                    navController = navController
                )
            }

            composable("outfit_editor/{ids}") { backStackEntry ->
                val ids = backStackEntry.arguments?.getString("ids") ?: ""

                OutfitEditorScreen(
                    itemIds = ids,
                    navController = navController
                )
            }

            composable("outfit_view/{id}") { backStackEntry ->
                val id = backStackEntry.arguments
                    ?.getString("id")
                    ?.toLongOrNull() ?: return@composable

                OutfitViewScreen(
                    outfitId = id,
                    navController = navController
                )
            }
        }
    }
}