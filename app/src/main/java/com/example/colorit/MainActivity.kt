package com.example.colorit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.colorit.ui.screens.*
import com.example.colorit.ui.theme.ColorItTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Manage dark theme toggle state dynamically in the MainActivity
            var isDarkTheme by remember { mutableStateOf(false) }

            ColorItTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // 1. Splash Screen
                        composable("splash") {
                            SplashScreen(
                                onNavigateToHome = {
                                    navController.navigate("home") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 2. Home Dashboard
                        composable("home") {
                            HomeScreen(
                                onNavigateToColoringBook = { navController.navigate("coloring_book") },
                                onNavigateToFreeDraw = { navController.navigate("free_draw") },
                                onNavigateToGlowDraw = { navController.navigate("glow_draw") },
                                onNavigateToStickers = { navController.navigate("stickers") },
                                onNavigateToGallery = { navController.navigate("gallery") },
                                onNavigateToSettings = { navController.navigate("settings") }
                            )
                        }

                        // 3. Coloring Book
                        composable("coloring_book") {
                            ColoringBookScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 4. Free Draw
                        composable("free_draw") {
                            FreeDrawScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 5. Glow Draw
                        composable("glow_draw") {
                            GlowDrawScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 6. Stickers
                        composable("stickers") {
                            StickerScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 7. Gallery
                        composable("gallery") {
                            GalleryScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 8. Settings
                        composable("settings") {
                            SettingsScreen(
                                onNavigateBack = { navController.popBackStack() },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { isDarkTheme = !isDarkTheme }
                            )
                        }
                    }
                }
            }
        }
    }
}