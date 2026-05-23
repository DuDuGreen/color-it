package com.example.colorit.utils

import androidx.compose.ui.graphics.Path
import com.example.colorit.model.ColorableRegion
import com.example.colorit.model.ColoringPage

/**
 * Pre-configured vector coloring book pages.
 * Outlines are defined programmatically using Compose Path objects
 * which are fast, scalable, vector-perfect, and support clean tap-to-fill regions.
 */
object SampleColoringPages {

    fun getPages(): List<ColoringPage> {
        return listOf(
            // --- ANIMALS ---
            createCuteFishPage(),
            createCuteBearPage(),
            
            // --- DINOSAURS ---
            createBabyDinoPage(),
            
            // --- VEHICLES ---
            createRocketShipPage(),
            createToyCarPage(),
            
            // --- NATURE & SPACE ---
            createRainbowNaturePage(),
            createStarAndMoonPage(),
            
            // --- ALPHABETS & NUMBERS ---
            createAlphabetAPage(),
            createNumber1Page()
        )
    }

    private fun createCuteFishPage() = ColoringPage(
        id = 101,
        name = "Cute Little Fish",
        category = "Animals",
        regions = listOf(
            ColorableRegion(1, "Ocean Water") { path ->
                path.addRect(androidx.compose.ui.geometry.Rect(0f, 0f, 1000f, 1000f))
            },
            ColorableRegion(2, "Fish Body") { path ->
                // Large oval body in center
                path.addOval(androidx.compose.ui.geometry.Rect(200f, 300f, 700f, 700f))
            },
            ColorableRegion(3, "Tail Fin") { path ->
                path.moveTo(700f, 500f)
                path.lineTo(850f, 350f)
                path.lineTo(800f, 500f)
                path.lineTo(850f, 650f)
                path.close()
            },
            ColorableRegion(4, "Top Fin") { path ->
                path.moveTo(400f, 320f)
                path.quadraticBezierTo(500f, 200f, 600f, 320f)
                path.close()
            },
            ColorableRegion(5, "Big Bubble") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(120f, 180f, 200f, 260f))
            },
            ColorableRegion(6, "Small Bubble") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(150f, 100f, 200f, 150f))
            },
            ColorableRegion(7, "Cute Eye") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(300f, 420f, 360f, 480f))
            }
        )
    )

    private fun createCuteBearPage() = ColoringPage(
        id = 102,
        name = "Playful Teddy Bear",
        category = "Animals",
        regions = listOf(
            ColorableRegion(1, "Background") { path ->
                path.addRect(androidx.compose.ui.geometry.Rect(0f, 0f, 1000f, 1000f))
            },
            ColorableRegion(2, "Teddy Head") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(250f, 250f, 750f, 750f))
            },
            ColorableRegion(3, "Left Ear") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(200f, 180f, 350f, 330f))
            },
            ColorableRegion(4, "Right Ear") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(650f, 180f, 800f, 330f))
            },
            ColorableRegion(5, "Bear Muzzle") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(400f, 500f, 600f, 660f))
            },
            ColorableRegion(6, "Bear Nose") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(460f, 520f, 540f, 580f))
            },
            ColorableRegion(7, "Left Eye") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(380f, 380f, 440f, 440f))
            },
            ColorableRegion(8, "Right Eye") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(560f, 380f, 620f, 440f))
            }
        )
    )

    private fun createBabyDinoPage() = ColoringPage(
        id = 201,
        name = "Baby Dino Spike",
        category = "Dinosaurs",
        regions = listOf(
            ColorableRegion(1, "Background") { path ->
                path.addRect(androidx.compose.ui.geometry.Rect(0f, 0f, 1000f, 1000f))
            },
            ColorableRegion(2, "Dino Body") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(250f, 400f, 750f, 850f))
            },
            ColorableRegion(3, "Dino Head") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(320f, 200f, 680f, 500f))
            },
            ColorableRegion(4, "Dino Tail") { path ->
                path.moveTo(700f, 650f)
                path.quadraticBezierTo(850f, 650f, 900f, 500f)
                path.quadraticBezierTo(800f, 750f, 680f, 780f)
                path.close()
            },
            ColorableRegion(5, "Back Spike 1") { path ->
                path.moveTo(350f, 220f)
                path.lineTo(390f, 150f)
                path.lineTo(430f, 220f)
                path.close()
            },
            ColorableRegion(6, "Back Spike 2") { path ->
                path.moveTo(480f, 200f)
                path.lineTo(520f, 130f)
                path.lineTo(560f, 200f)
                path.close()
            },
            ColorableRegion(7, "Back Spike 3") { path ->
                path.moveTo(610f, 220f)
                path.lineTo(650f, 150f)
                path.lineTo(690f, 220f)
                path.close()
            },
            ColorableRegion(8, "Left Eye") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(440f, 300f, 490f, 360f))
            },
            ColorableRegion(9, "Right Eye") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(540f, 300f, 590f, 360f))
            }
        )
    )

    private fun createRocketShipPage() = ColoringPage(
        id = 301,
        name = "Space Rocket",
        category = "Vehicles",
        regions = listOf(
            ColorableRegion(1, "Dark Space") { path ->
                path.addRect(androidx.compose.ui.geometry.Rect(0f, 0f, 1000f, 1000f))
            },
            ColorableRegion(2, "Rocket Body") { path ->
                // Bullet shape body
                path.moveTo(400f, 250f)
                path.lineTo(400f, 700f)
                path.lineTo(600f, 700f)
                path.lineTo(600f, 250f)
                path.quadraticBezierTo(500f, 50f, 400f, 250f)
                path.close()
            },
            ColorableRegion(3, "Nose Cone") { path ->
                path.moveTo(400f, 250f)
                path.quadraticBezierTo(500f, 50f, 600f, 250f)
                path.close()
            },
            ColorableRegion(4, "Left Fin") { path ->
                path.moveTo(400f, 600f)
                path.lineTo(300f, 730f)
                path.lineTo(400f, 700f)
                path.close()
            },
            ColorableRegion(5, "Right Fin") { path ->
                path.moveTo(600f, 600f)
                path.lineTo(700f, 730f)
                path.lineTo(600f, 700f)
                path.close()
            },
            ColorableRegion(6, "Cabin Window") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(450f, 300f, 550f, 400f))
            },
            ColorableRegion(7, "Rocket Exhaust Flame") { path ->
                path.moveTo(450f, 700f)
                path.lineTo(500f, 850f)
                path.lineTo(550f, 700f)
                path.close()
            }
        )
    )

    private fun createToyCarPage() = ColoringPage(
        id = 302,
        name = "Cute Beep Beep Car",
        category = "Vehicles",
        regions = listOf(
            ColorableRegion(1, "Background Road") { path ->
                path.addRect(androidx.compose.ui.geometry.Rect(0f, 0f, 1000f, 1000f))
            },
            ColorableRegion(2, "Car Main Body") { path ->
                path.moveTo(150f, 600f)
                path.lineTo(850f, 600f)
                path.lineTo(800f, 450f)
                path.lineTo(600f, 450f)
                path.lineTo(500f, 300f)
                path.lineTo(300f, 300f)
                path.lineTo(200f, 450f)
                path.close()
            },
            ColorableRegion(3, "Cabin Window Grid") { path ->
                path.moveTo(320f, 330f)
                path.lineTo(480f, 330f)
                path.lineTo(550f, 430f)
                path.lineTo(260f, 430f)
                path.close()
            },
            ColorableRegion(4, "Front Wheel") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(250f, 520f, 390f, 660f))
            },
            ColorableRegion(5, "Back Wheel") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(600f, 520f, 740f, 660f))
            },
            ColorableRegion(6, "Wheel Hub 1") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(290f, 560f, 350f, 620f))
            },
            ColorableRegion(7, "Wheel Hub 2") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(640f, 560f, 700f, 620f))
            }
        )
    )

    private fun createRainbowNaturePage() = ColoringPage(
        id = 401,
        name = "Happy Rainbow Sky",
        category = "Nature",
        regions = listOf(
            ColorableRegion(1, "Background Sky") { path ->
                path.addRect(androidx.compose.ui.geometry.Rect(0f, 0f, 1000f, 1000f))
            },
            ColorableRegion(2, "Sun") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(100f, 100f, 250f, 250f))
            },
            ColorableRegion(3, "Rainbow Red Outer Arc") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(100f, 300f, 900f, 1100f))
            },
            ColorableRegion(4, "Rainbow Yellow Mid Arc") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(200f, 400f, 800f, 1000f))
            },
            ColorableRegion(5, "Rainbow Blue Inner Arc") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(300f, 500f, 700f, 900f))
            },
            ColorableRegion(6, "Rainbow Sky Core") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(400f, 600f, 600f, 800f))
            },
            ColorableRegion(7, "Cloud Left") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(50f, 550f, 300f, 750f))
            },
            ColorableRegion(8, "Cloud Right") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(700f, 550f, 950f, 750f))
            }
        )
    )

    private fun createStarAndMoonPage() = ColoringPage(
        id = 501,
        name = "Moon and Star",
        category = "Space",
        regions = listOf(
            ColorableRegion(1, "Night Sky") { path ->
                path.addRect(androidx.compose.ui.geometry.Rect(0f, 0f, 1000f, 1000f))
            },
            ColorableRegion(2, "Crescent Moon") { path ->
                // Outer circle subtract inner circle
                path.moveTo(250f, 200f)
                path.cubicTo(450f, 200f, 500f, 450f, 400f, 650f)
                path.cubicTo(200f, 650f, 150f, 400f, 250f, 200f)
                path.close()
            },
            ColorableRegion(3, "Happy Star Face") { path ->
                // Standard 5-point star
                path.moveTo(650f, 250f)  // top
                path.lineTo(680f, 330f)
                path.lineTo(760f, 330f)  // right
                path.lineTo(700f, 380f)
                path.lineTo(720f, 460f)  // bottom-right
                path.lineTo(650f, 410f)  // bottom-mid
                path.lineTo(580f, 460f)  // bottom-left
                path.lineTo(600f, 380f)
                path.lineTo(540f, 330f)  // left
                path.lineTo(620f, 330f)
                path.close()
            },
            ColorableRegion(4, "Little Star 1") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(200f, 100f, 240f, 140f))
            },
            ColorableRegion(5, "Little Star 2") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(800f, 180f, 830f, 210f))
            }
        )
    )

    private fun createAlphabetAPage() = ColoringPage(
        id = 601,
        name = "A is for Apple",
        category = "Alphabets",
        regions = listOf(
            ColorableRegion(1, "Play Board") { path ->
                path.addRect(androidx.compose.ui.geometry.Rect(0f, 0f, 1000f, 1000f))
            },
            ColorableRegion(2, "Letter A Outer") { path ->
                path.moveTo(200f, 800f)
                path.lineTo(400f, 200f)
                path.lineTo(600f, 800f)
                path.lineTo(500f, 800f)
                path.lineTo(450f, 620f)
                path.lineTo(350f, 620f)
                path.lineTo(300f, 800f)
                path.close()
            },
            ColorableRegion(3, "Letter A Core Triangle") { path ->
                path.moveTo(400f, 350f)
                path.lineTo(435f, 530f)
                path.lineTo(365f, 530f)
                path.close()
            },
            ColorableRegion(4, "Apple Base") { path ->
                path.addOval(androidx.compose.ui.geometry.Rect(650f, 500f, 850f, 720f))
            },
            ColorableRegion(5, "Apple Leaf") { path ->
                path.moveTo(750f, 500f)
                path.quadraticBezierTo(780f, 430f, 820f, 450f)
                path.quadraticBezierTo(780f, 520f, 750f, 500f)
                path.close()
            }
        )
    )

    private fun createNumber1Page() = ColoringPage(
        id = 701,
        name = "1 Cute Star",
        category = "Numbers",
        regions = listOf(
            ColorableRegion(1, "Play Board") { path ->
                path.addRect(androidx.compose.ui.geometry.Rect(0f, 0f, 1000f, 1000f))
            },
            ColorableRegion(2, "Number 1 Block") { path ->
                path.moveTo(350f, 280f)
                path.lineTo(260f, 350f)
                path.lineTo(290f, 400f)
                path.lineTo(350f, 350f)
                path.lineTo(350f, 750f)
                path.lineTo(250f, 750f)
                path.lineTo(250f, 820f)
                path.lineTo(550f, 820f)
                path.lineTo(550f, 750f)
                path.lineTo(440f, 750f)
                path.lineTo(440f, 280f)
                path.close()
            },
            ColorableRegion(3, "Big Star") { path ->
                path.moveTo(700f, 350f)  // top
                path.lineTo(730f, 430f)
                path.lineTo(810f, 430f)  // right
                path.lineTo(750f, 480f)
                path.lineTo(770f, 560f)  // bottom-right
                path.lineTo(700f, 510f)  // bottom-mid
                path.lineTo(630f, 560f)  // bottom-left
                path.lineTo(650f, 480f)
                path.lineTo(590f, 430f)  // left
                path.lineTo(670f, 430f)
                path.close()
            }
        )
    )
}
