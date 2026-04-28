package com.example.myapplication

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.myapplication.ui.theme.screens.*

@Composable
fun AppNavHost() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {

        composable("welcome") {
            WelcomeScreen(
                onStartClick = {
                    navController.navigate("main")
                }
            )
        }

        composable("main") {
            MainScreen(navController)
        }

        composable("detail/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()

            if (id != null) {
                ClosetDetailScreen(
                    itemId = id,
                    navController = navController
                )
            }
        }

        // 🔥 НОВЫЙ ЭКРАН
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

        composable(
            route = "outfit_editor/{ids}",
            arguments = listOf(
                navArgument("ids") { type = NavType.StringType }
            )
        ) { backStackEntry ->

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