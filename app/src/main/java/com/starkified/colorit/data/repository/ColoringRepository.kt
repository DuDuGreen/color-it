package com.starkified.colorit.data.repository

import com.starkified.colorit.data.model.ColoringPage
import com.starkified.colorit.data.model.ColoringShape
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ColoringRepository @Inject constructor() {

    private val pages = listOf(
        // ANIMALS CATEGORY
        ColoringPage(
            id = "kitten",
            title = "Sleepy Kitty",
            category = "Animals",
            shapes = emptyList(),
            imageResName = "page_kitten"
        ),
        ColoringPage(
            id = "puppy",
            title = "Friendly Pup",
            category = "Animals",
            shapes = emptyList(),
            imageResName = "page_puppy"
        ),
        ColoringPage(
            id = "lion",
            title = "Proud Lion",
            category = "Animals",
            shapes = emptyList(),
            imageResName = "page_lion"
        ),
        ColoringPage(
            id = "rabbit",
            title = "Bouncy Bunny",
            category = "Animals",
            shapes = emptyList(),
            imageResName = "page_rabbit"
        ),
        ColoringPage(
            id = "elephant",
            title = "Gentle Elephant",
            category = "Animals",
            shapes = emptyList(),
            imageResName = "page_elephant"
        ),
        ColoringPage(
            id = "giraffe",
            title = "Happy Giraffe",
            category = "Animals",
            shapes = emptyList(),
            imageResName = "page_giraffe"
        ),
        ColoringPage(
            id = "monkey",
            title = "Playful Monkey",
            category = "Animals",
            shapes = emptyList(),
            imageResName = "page_monkey"
        ),
        ColoringPage(
            id = "fox",
            title = "Clever Fox",
            category = "Animals",
            shapes = emptyList(),
            imageResName = "page_fox"
        ),
        ColoringPage(
            id = "hippo",
            title = "Cheerful Hippo",
            category = "Animals",
            shapes = emptyList(),
            imageResName = "page_hippo"
        ),

        // NATURE CATEGORY
        ColoringPage(
            id = "butterfly",
            title = "Dancing Butterfly",
            category = "Nature",
            shapes = emptyList(),
            imageResName = "page_butterfly"
        ),
        ColoringPage(
            id = "flower",
            title = "Smiling Daisy",
            category = "Nature",
            shapes = emptyList(),
            imageResName = "page_flower"
        ),
        ColoringPage(
            id = "nature_rainbow",
            title = "Radiant Rainbow",
            category = "Nature",
            shapes = emptyList(),
            imageResName = "page_nature_rainbow"
        ),
        ColoringPage(
            id = "nature_forest",
            title = "Whispering Forest",
            category = "Nature",
            shapes = emptyList(),
            imageResName = "page_nature_forest"
        ),
        ColoringPage(
            id = "nature_mountain",
            title = "Scenic Mountain",
            category = "Nature",
            shapes = emptyList(),
            imageResName = "page_nature_mountain"
        ),

        // VEHICLES CATEGORY
        ColoringPage(
            id = "car",
            title = "Shiny Racecar",
            category = "Vehicles",
            shapes = emptyList(),
            imageResName = "page_car"
        ),
        ColoringPage(
            id = "train",
            title = "Chugging Locomotive",
            category = "Vehicles",
            shapes = emptyList(),
            imageResName = "page_train"
        ),
        ColoringPage(
            id = "vehicle_firetruck",
            title = "Brave Firetruck",
            category = "Vehicles",
            shapes = emptyList(),
            imageResName = "page_vehicle_firetruck"
        ),
        ColoringPage(
            id = "vehicle_submarine",
            title = "Diving Submarine",
            category = "Vehicles",
            shapes = emptyList(),
            imageResName = "page_vehicle_submarine"
        ),
        ColoringPage(
            id = "vehicle_helicopter",
            title = "Whirly Helicopter",
            category = "Vehicles",
            shapes = emptyList(),
            imageResName = "page_vehicle_helicopter"
        ),

        // SPACE CATEGORY
        ColoringPage(
            id = "rocket",
            title = "Speedy Spaceship",
            category = "Space",
            shapes = emptyList(),
            imageResName = "page_rocket"
        ),
        ColoringPage(
            id = "planet",
            title = "Ringed Planet",
            category = "Space",
            shapes = emptyList(),
            imageResName = "page_planet"
        ),
        ColoringPage(
            id = "space_astronaut",
            title = "Cosmic Astronaut",
            category = "Space",
            shapes = emptyList(),
            imageResName = "page_space_astronaut"
        ),
        ColoringPage(
            id = "space_ufo",
            title = "Starry UFO",
            category = "Space",
            shapes = emptyList(),
            imageResName = "page_space_ufo"
        ),
        ColoringPage(
            id = "space_satellite",
            title = "Orbiting Satellite",
            category = "Space",
            shapes = emptyList(),
            imageResName = "page_space_satellite"
        ),

        // NUMBERS CATEGORY
        ColoringPage(
            id = "number_zero",
            title = "Magic Zero",
            category = "Numbers",
            shapes = emptyList(),
            imageResName = "page_number_zero"
        ),
        ColoringPage(
            id = "number_one",
            title = "Golden One",
            category = "Numbers",
            shapes = emptyList(),
            imageResName = "page_number_one"
        ),
        ColoringPage(
            id = "number_two",
            title = "Twinkling Two",
            category = "Numbers",
            shapes = emptyList(),
            imageResName = "page_number_two"
        ),
        ColoringPage(
            id = "number_three",
            title = "Cheerful Three",
            category = "Numbers",
            shapes = emptyList(),
            imageResName = "page_number_three"
        ),
        ColoringPage(
            id = "number_four",
            title = "Jolly Four",
            category = "Numbers",
            shapes = emptyList(),
            imageResName = "page_number_four"
        ),
        ColoringPage(
            id = "number_five",
            title = "Starlight Five",
            category = "Numbers",
            shapes = emptyList(),
            imageResName = "page_number_five"
        ),
        ColoringPage(
            id = "number_six",
            title = "Silly Six",
            category = "Numbers",
            shapes = emptyList(),
            imageResName = "page_number_six"
        ),
        ColoringPage(
            id = "number_seven",
            title = "Lucky Seven",
            category = "Numbers",
            shapes = emptyList(),
            imageResName = "page_number_seven"
        ),
        ColoringPage(
            id = "number_eight",
            title = "Bubbly Eight",
            category = "Numbers",
            shapes = emptyList(),
            imageResName = "page_number_eight"
        ),
        ColoringPage(
            id = "number_nine",
            title = "Dreamy Nine",
            category = "Numbers",
            shapes = emptyList(),
            imageResName = "page_number_nine"
        ),

        // DINOSAURS CATEGORY
        ColoringPage(
            id = "dino_brontosaurus",
            title = "Brontosaurus",
            category = "Dinosaurs",
            shapes = emptyList(),
            imageResName = "page_dino_brontosaurus"
        ),
        ColoringPage(
            id = "dino_trex",
            title = "T-Rex",
            category = "Dinosaurs",
            shapes = emptyList(),
            imageResName = "page_dino_trex"
        ),
        ColoringPage(
            id = "dino_pterodactyl",
            title = "Pterodactyl",
            category = "Dinosaurs",
            shapes = emptyList(),
            imageResName = "page_dino_pterodactyl"
        ),
        ColoringPage(
            id = "dino_triceratops",
            title = "Triceratops",
            category = "Dinosaurs",
            shapes = emptyList(),
            imageResName = "page_dino_triceratops"
        ),
        ColoringPage(
            id = "dino_stegosaurus",
            title = "Stegosaurus",
            category = "Dinosaurs",
            shapes = emptyList(),
            imageResName = "page_dino_stegosaurus"
        ),
        ColoringPage(
            id = "dino_spinosaurus",
            title = "Spinosaurus",
            category = "Dinosaurs",
            shapes = emptyList(),
            imageResName = "page_dino_spinosaurus"
        ),
        ColoringPage(
            id = "dino_dilophosaurus",
            title = "Dilophosaurus",
            category = "Dinosaurs",
            shapes = emptyList(),
            imageResName = "page_dino_dilophosaurus"
        ),

        // BIRDS CATEGORY
        ColoringPage(
            id = "bird_owl",
            title = "Wise Owl",
            category = "Birds",
            shapes = emptyList(),
            imageResName = "page_bird_owl"
        ),
        ColoringPage(
            id = "bird_parrot",
            title = "Tropical Parrot",
            category = "Birds",
            shapes = emptyList(),
            imageResName = "page_bird_parrot"
        ),
        ColoringPage(
            id = "bird_penguin",
            title = "Waddling Penguin",
            category = "Birds",
            shapes = emptyList(),
            imageResName = "page_bird_penguin"
        ),
        ColoringPage(
            id = "bird_duck",
            title = "Splashy Duck",
            category = "Birds",
            shapes = emptyList(),
            imageResName = "page_bird_duck"
        )
    )

    fun getPages(): List<ColoringPage> = pages

    fun getPagesByCategory(category: String): List<ColoringPage> =
        if (category.equals("All", ignoreCase = true)) {
            pages
        } else {
            pages.filter { it.category.equals(category, ignoreCase = true) }
        }

    fun getPageById(id: String): ColoringPage? =
        pages.find { it.id.equals(id, ignoreCase = true) }
}
