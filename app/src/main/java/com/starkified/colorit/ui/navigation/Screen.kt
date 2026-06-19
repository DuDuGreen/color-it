package com.starkified.colorit.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object ColoringBook : Screen("coloring_book")
    object ColoringArena : Screen("coloring_arena/{pageId}") {
        fun createRoute(pageId: String) = "coloring_arena/$pageId"
    }
    object FreeDraw : Screen("free_draw")
    object GlowDraw : Screen("glow_draw")
    object Stickers : Screen("stickers")
    object Gallery : Screen("gallery")
    object Settings : Screen("settings")
}
