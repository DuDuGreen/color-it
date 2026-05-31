package com.example.colorit.data.repository

import com.example.colorit.data.model.ColoringPage
import com.example.colorit.data.model.ColoringShape
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ColoringRepository @Inject constructor() {

    private val pages = listOf(
        // ANIMALS CATEGORY
        ColoringPage(
            id = "fish",
            title = "Happy Fish",
            category = "Animals",
            shapes = listOf(
                ColoringShape("tail", "M 60 100 L 15 50 L 35 100 L 15 150 Z"),
                ColoringShape("body", "M 60 100 C 90 30, 170 30, 190 100 C 170 170, 90 170, 60 100 Z"),
                ColoringShape("stripe1", "M 120 48 Q 105 100 120 152 Q 135 100 120 48 Z"),
                ColoringShape("stripe2", "M 90 58 Q 78 100 90 142 Q 102 100 90 58 Z"),
                ColoringShape("eye", "M 160 90 A 8 8 0 1 1 160 89.9 Z")
            )
        ),
        ColoringPage(
            id = "duck",
            title = "Little Duck",
            category = "Animals",
            shapes = listOf(
                ColoringShape("body", "M 50 120 C 70 160, 150 160, 160 120 C 170 100, 150 70, 130 90 C 100 90, 80 80, 50 120 Z"),
                ColoringShape("head", "M 130 90 C 145 105, 170 95, 160 70 C 150 50, 120 55, 130 90 Z"),
                ColoringShape("beak", "M 155 75 L 180 80 L 155 85 Z"),
                ColoringShape("eye", "M 142 68 A 4 4 0 1 1 142 67.9 Z"),
                ColoringShape("wing", "M 80 125 C 95 105, 115 105, 120 125 C 115 140, 95 140, 80 125 Z")
            )
        ),

        // NATURE CATEGORY
        ColoringPage(
            id = "flower",
            title = "Sweet Flower",
            category = "Nature",
            shapes = listOf(
                ColoringShape("stem", "M 96 115 L 96 185 L 104 185 L 104 115 Z"),
                ColoringShape("leaf_left", "M 96 145 C 75 140, 60 150, 70 160 C 85 165, 96 155, 96 145 Z"),
                ColoringShape("leaf_right", "M 104 155 C 125 150, 140 160, 130 170 C 115 175, 104 165, 104 155 Z"),
                ColoringShape("petal_top", "M 100 80 C 85 40, 115 40, 100 80 Z"),
                ColoringShape("petal_bottom", "M 100 120 C 85 160, 115 160, 100 120 Z"),
                ColoringShape("petal_left", "M 80 100 C 40 85, 40 115, 80 100 Z"),
                ColoringShape("petal_right", "M 120 100 C 160 85, 160 115, 120 100 Z"),
                ColoringShape("center", "M 100 100 A 20 20 0 1 1 100 99.9 Z")
            )
        ),

        // VEHICLES CATEGORY
        ColoringPage(
            id = "car",
            title = "Cute Car",
            category = "Vehicles",
            shapes = listOf(
                ColoringShape("body", "M 30 130 L 30 110 L 50 90 L 130 90 L 150 110 L 170 110 L 170 130 L 155 130 A 15 15 0 0 0 125 130 L 75 130 A 15 15 0 0 0 45 130 Z"),
                ColoringShape("window_left", "M 55 95 L 85 95 L 85 110 L 45 110 Z"),
                ColoringShape("window_right", "M 95 95 L 125 95 L 135 110 L 95 110 Z"),
                ColoringShape("wheel_left", "M 60 130 A 15 15 0 1 1 60 129.9 Z"),
                ColoringShape("wheel_right", "M 140 130 A 15 15 0 1 1 140 129.9 Z")
            )
        ),

        // SPACE CATEGORY
        ColoringPage(
            id = "rocket",
            title = "Starry Rocket",
            category = "Space",
            shapes = listOf(
                ColoringShape("flame", "M 100 150 L 90 180 L 100 170 L 110 180 Z"),
                ColoringShape("wing_left", "M 80 120 L 60 150 L 80 150 Z"),
                ColoringShape("wing_right", "M 120 120 L 140 150 L 120 150 Z"),
                ColoringShape("body", "M 100 30 C 115 60, 120 90, 120 150 L 80 150 C 80 90, 85 60, 100 30 Z"),
                ColoringShape("window", "M 100 90 A 12 12 0 1 1 100 89.9 Z")
            )
        ),

        // NUMBERS CATEGORY
        ColoringPage(
            id = "number_five",
            title = "Happy Five",
            category = "Numbers",
            shapes = listOf(
                // Outline shape of Number 5
                ColoringShape("num5", "M 60 50 L 130 50 L 120 80 L 80 80 L 75 110 C 90 100, 140 100, 130 145 C 120 180, 70 180, 60 150 L 90 145 C 95 155, 108 155, 103 140 C 98 125, 65 125, 60 110 Z"),
                ColoringShape("star1", "M 150 50 L 153 58 L 161 58 L 155 63 L 157 71 L 150 66 L 143 71 L 145 63 L 139 58 L 147 58 Z"),
                ColoringShape("star2", "M 40 140 L 42 145 L 47 145 L 43 148 L 45 153 L 40 150 L 35 153 L 37 148 L 33 145 L 38 145 Z")
            )
        )
    )

    fun getPages(): List<ColoringPage> = pages

    fun getPagesByCategory(category: String): List<ColoringPage> =
        pages.filter { it.category.equals(category, ignoreCase = true) }

    fun getPageById(id: String): ColoringPage? =
        pages.find { it.id.equals(id, ignoreCase = true) }
}
