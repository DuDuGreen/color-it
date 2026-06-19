package com.starkified.colorit.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.starkified.colorit.ui.home.HomeScreen
import com.starkified.colorit.ui.home.HomeViewModel
import com.starkified.colorit.ui.splash.SplashScreen
import com.starkified.colorit.util.SoundHelper
import com.starkified.colorit.ui.coloring.CategorySelectionScreen
import com.starkified.colorit.ui.coloring.ColoringBookViewModel
import com.starkified.colorit.ui.coloring.ColoringScreen
import com.starkified.colorit.ui.coloring.ColoringViewModel
import com.starkified.colorit.ui.freedraw.FreeDrawScreen
import com.starkified.colorit.ui.freedraw.FreeDrawViewModel
import com.starkified.colorit.ui.glow.GlowDrawScreen
import com.starkified.colorit.ui.glow.GlowDrawViewModel
import com.starkified.colorit.ui.stickers.StickersScreen
import com.starkified.colorit.ui.stickers.StickersViewModel
import com.starkified.colorit.ui.gallery.GalleryScreen
import com.starkified.colorit.ui.gallery.GalleryViewModel
import com.starkified.colorit.ui.settings.SettingsScreen
import com.starkified.colorit.ui.settings.SettingsViewModel

@Composable
fun AppNavGraph(
    soundHelper: SoundHelper
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                soundHelper = soundHelper,
                onSplashFinished = {
                    navController.navigate(Screen.Home.route)
                }
            )
        }
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            val context = androidx.compose.ui.platform.LocalContext.current
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToColoringBook = {
                    val activity = context as? android.app.Activity
                    if (activity != null) {
                        com.starkified.colorit.util.AdManager.showAdIfReady(activity) {
                            navController.navigate(Screen.ColoringBook.route)
                        }
                    } else {
                        navController.navigate(Screen.ColoringBook.route)
                    }
                },
                onNavigateToFreeDraw = { navController.navigate(Screen.FreeDraw.route) },
                onNavigateToGlowDraw = { navController.navigate(Screen.GlowDraw.route) },
                onNavigateToStickers = { navController.navigate(Screen.Stickers.route) },
                onNavigateToGallery = { navController.navigate(Screen.Gallery.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ColoringBook.route) {
            val coloringBookViewModel: ColoringBookViewModel = hiltViewModel()
            CategorySelectionScreen(
                viewModel = coloringBookViewModel,
                soundHelper = soundHelper,
                onPageSelected = { pageId ->
                    navController.navigate(Screen.ColoringArena.createRoute(pageId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ColoringArena.route) { backStackEntry ->
            val pageId = backStackEntry.arguments?.getString("pageId") ?: ""
            val coloringViewModel: ColoringViewModel = hiltViewModel()
            ColoringScreen(
                viewModel = coloringViewModel,
                soundHelper = soundHelper,
                pageId = pageId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.FreeDraw.route) {
            val freeDrawViewModel: FreeDrawViewModel = hiltViewModel()
            FreeDrawScreen(
                viewModel = freeDrawViewModel,
                soundHelper = soundHelper,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.GlowDraw.route) {
            val glowDrawViewModel: GlowDrawViewModel = hiltViewModel()
            GlowDrawScreen(
                viewModel = glowDrawViewModel,
                soundHelper = soundHelper,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Stickers.route) {
            val stickersViewModel: StickersViewModel = hiltViewModel()
            StickersScreen(
                viewModel = stickersViewModel,
                soundHelper = soundHelper,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Gallery.route) {
            val galleryViewModel: GalleryViewModel = hiltViewModel()
            GalleryScreen(
                viewModel = galleryViewModel,
                soundHelper = soundHelper,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = settingsViewModel,
                soundHelper = soundHelper,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable { onBack() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$title\n\n(Tap to go back)",
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}
