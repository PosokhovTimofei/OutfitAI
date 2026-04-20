package com.example.myapplication

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
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
    }
}