package com.starkified.colorit.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import com.starkified.colorit.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Helper to generate a Star path in DrawScope coordinates.
 */
fun createStarPath(center: Offset, radius: Float, innerRadius: Float, points: Int = 5): Path {
    val path = Path()
    val angleStep = Math.PI / points
    for (i in 0 until (points * 2)) {
        val r = if (i % 2 == 0) radius else innerRadius
        val angle = i * angleStep - Math.PI / 2
        val x = center.x + r * cos(angle).toFloat()
        val y = center.y + r * sin(angle).toFloat()
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    return path
}

/**
 * 1. Coloring Book Icon
 * Cute open book with pages and a small paintbrush swaying back and forth.
 */
@Composable
fun ColoringBookIcon(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "coloring_book_anim")
    
    // Paintbrush swaying rotation
    val brushRotation by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "brush_rotation"
    )

    // Paintbrush paint drip scale
    val dripScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drip_scale"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2, h / 2)
        
        // 1. Draw open book background/cover
        val coverPath = Path().apply {
            moveTo(w * 0.1f, h * 0.35f)
            quadraticTo(w * 0.3f, h * 0.25f, w * 0.5f, h * 0.35f)
            quadraticTo(w * 0.7f, h * 0.25f, w * 0.9f, h * 0.35f)
            lineTo(w * 0.9f, h * 0.85f)
            quadraticTo(w * 0.7f, h * 0.75f, w * 0.5f, h * 0.85f)
            quadraticTo(w * 0.3f, h * 0.75f, w * 0.1f, h * 0.85f)
            close()
        }
        drawPath(
            path = coverPath,
            color = CozyDeepRose
        )
        drawPath(
            path = coverPath,
            color = TextDark,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 2. Draw white/cream pages
        val pagesPath = Path().apply {
            moveTo(w * 0.13f, h * 0.33f)
            quadraticTo(w * 0.3f, h * 0.24f, w * 0.48f, h * 0.32f)
            lineTo(w * 0.48f, h * 0.82f)
            quadraticTo(w * 0.3f, h * 0.74f, w * 0.13f, h * 0.82f)
            close()
        }
        drawPath(
            path = pagesPath,
            color = CozyCreamBackground
        )
        drawPath(
            path = pagesPath,
            color = TextDark,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        val pagesRightPath = Path().apply {
            moveTo(w * 0.87f, h * 0.33f)
            quadraticTo(w * 0.7f, h * 0.24f, w * 0.52f, h * 0.32f)
            lineTo(w * 0.52f, h * 0.82f)
            quadraticTo(w * 0.7f, h * 0.74f, w * 0.87f, h * 0.82f)
            close()
        }
        drawPath(
            path = pagesRightPath,
            color = CozyCreamBackground
        )
        drawPath(
            path = pagesRightPath,
            color = TextDark,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Draw book center binder
        drawLine(
            color = TextDark,
            start = Offset(w * 0.5f, h * 0.34f),
            end = Offset(w * 0.5f, h * 0.84f),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 3. Draw a cute heart or drawing outline on the left page
        val heartPath = Path().apply {
            val hx = w * 0.3f
            val hy = h * 0.55f
            val hs = w * 0.08f
            moveTo(hx, hy)
            cubicTo(hx - hs, hy - hs, hx - hs * 2, hy + hs * 0.5f, hx, hy + hs * 1.8f)
            cubicTo(hx + hs * 2, hy + hs * 0.5f, hx + hs, hy - hs, hx, hy)
        }
        drawPath(
            path = heartPath,
            color = CozyRose.copy(alpha = 0.7f)
        )
        drawPath(
            path = heartPath,
            color = TextDark,
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw a colored splash on the right page
        drawCircle(
            color = PastelBlue,
            radius = w * 0.08f,
            center = Offset(w * 0.7f, h * 0.58f)
        )
        drawCircle(
            color = PastelYellow,
            radius = w * 0.05f,
            center = Offset(w * 0.76f, h * 0.52f)
        )

        // 4. Draw a paintbrush tilted over the book
        rotate(degrees = brushRotation, pivot = Offset(w * 0.8f, h * 0.2f)) {
            // Paintbrush Handle
            val handlePath = Path().apply {
                moveTo(w * 0.85f, h * 0.1f)
                lineTo(w * 0.65f, h * 0.45f)
                lineTo(w * 0.60f, h * 0.42f)
                lineTo(w * 0.80f, h * 0.07f)
                close()
            }
            drawPath(
                path = handlePath,
                color = Color(0xFFD2B48C) // Light wood brown
            )
            drawPath(
                path = handlePath,
                color = TextDark,
                style = Stroke(width = 2.dp.toPx())
            )

            // Ferrule (Metal part)
            val ferrulePath = Path().apply {
                moveTo(w * 0.66f, h * 0.43f)
                lineTo(w * 0.58f, h * 0.55f)
                lineTo(w * 0.53f, h * 0.52f)
                lineTo(w * 0.61f, h * 0.40f)
                close()
            }
            drawPath(
                path = ferrulePath,
                color = Color.LightGray
            )
            drawPath(
                path = ferrulePath,
                color = TextDark,
                style = Stroke(width = 2.dp.toPx())
            )

            // Brush tip (bristles)
            val bristlesPath = Path().apply {
                moveTo(w * 0.58f, h * 0.55f)
                quadraticTo(w * 0.54f, h * 0.62f, w * 0.48f, h * 0.68f) // Tip point
                quadraticTo(w * 0.46f, h * 0.60f, w * 0.53f, h * 0.52f)
                close()
            }
            drawPath(
                path = bristlesPath,
                color = CozyRose
            )
            drawPath(
                path = bristlesPath,
                color = TextDark,
                style = Stroke(width = 2.dp.toPx())
            )

            // Draw shiny paint drip on the tip
            drawCircle(
                color = CozyRose,
                radius = w * 0.025f * dripScale,
                center = Offset(w * 0.48f, h * 0.68f)
            )
        }
    }
}

/**
 * 2. Free Draw Icon
 * A friendly tilted crayon sketching a wavy color path.
 */
@Composable
fun FreeDrawIcon(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "free_draw_anim")

    // Draw offset to animate the drawing path
    val drawProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "draw_progress"
    )

    // Crayon bobbing up and down + tilt
    val crayonTranslationX by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "crayon_tx"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Draw a beautiful colorful path (wavy ribbon style)
        val ribbonPath = Path().apply {
            moveTo(w * 0.15f, h * 0.75f)
            cubicTo(
                w * 0.35f, h * 0.55f,
                w * 0.50f, h * 0.85f,
                w * 0.70f, h * 0.60f
            )
        }
        
        // Draw the trail of the crayon
        drawPath(
            path = ribbonPath,
            brush = Brush.linearGradient(
                colors = listOf(PastelPink, PastelMint, PastelBlue),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            ),
            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Add a dark pencil stroke outline
        drawPath(
            path = ribbonPath,
            color = TextDark.copy(alpha = 0.8f),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Draw small sparkle stars along the path
        val starPos = Offset(w * 0.4f, h * 0.68f)
        val star1 = createStarPath(starPos, w * 0.04f, w * 0.02f)
        drawPath(star1, PastelYellow)
        drawPath(star1, TextDark, style = Stroke(width = 1.5.dp.toPx()))

        // 2. Draw a friendly tilted crayon sketching
        // Position of the crayon tip changes dynamically matching the wiggle
        val tipX = w * 0.70f + crayonTranslationX
        val tipY = h * 0.58f

        translate(left = tipX - w * 0.3f, top = tipY - h * 0.7f) {
            rotate(degrees = 15f, pivot = Offset(w * 0.3f, h * 0.7f)) {
                // Crayon tip
                val tipPath = Path().apply {
                    moveTo(w * 0.28f, h * 0.72f)
                    lineTo(w * 0.30f, h * 0.76f) // Tip point
                    lineTo(w * 0.32f, h * 0.72f)
                    close()
                }
                drawPath(tipPath, CozyRose)
                drawPath(tipPath, TextDark, style = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Round))

                // Crayon body
                val bodyPath = Path().apply {
                    moveTo(w * 0.24f, h * 0.45f)
                    lineTo(w * 0.36f, h * 0.45f)
                    lineTo(w * 0.36f, h * 0.72f)
                    lineTo(w * 0.24f, h * 0.72f)
                    close()
                }
                drawPath(bodyPath, CozyRose)
                
                // Crayon wrapper (paper)
                val paperPath = Path().apply {
                    moveTo(w * 0.24f, h * 0.50f)
                    lineTo(w * 0.36f, h * 0.50f)
                    lineTo(w * 0.36f, h * 0.68f)
                    lineTo(w * 0.24f, h * 0.68f)
                    close()
                }
                drawPath(paperPath, PastelYellow)
                drawPath(bodyPath, TextDark, style = Stroke(width = 2.dp.toPx()))

                // Crayon label/stripes
                drawLine(
                    color = TextDark,
                    start = Offset(w * 0.24f, h * 0.54f),
                    end = Offset(w * 0.36f, h * 0.54f),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = TextDark,
                    start = Offset(w * 0.24f, h * 0.64f),
                    end = Offset(w * 0.36f, h * 0.64f),
                    strokeWidth = 2.dp.toPx()
                )

                // Crayon smiley face
                drawCircle(
                    color = TextDark,
                    radius = 2.dp.toPx(),
                    center = Offset(w * 0.28f, h * 0.58f)
                )
                drawCircle(
                    color = TextDark,
                    radius = 2.dp.toPx(),
                    center = Offset(w * 0.32f, h * 0.58f)
                )
                // Smile
                val smilePath = Path().apply {
                    arcTo(
                        rect = Rect(
                            left = w * 0.27f,
                            top = h * 0.58f,
                            right = w * 0.33f,
                            bottom = h * 0.62f
                        ),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )
                }
                drawPath(smilePath, TextDark, style = Stroke(width = 1.5.dp.toPx()))
            }
        }
    }
}

/**
 * 3. Glow Draw Icon
 * A magic wand emitting glowing/pulsing stars and particles.
 */
@Composable
fun GlowDrawIcon(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow_draw_anim")

    // Magic wand tilt animation
    val wandTilt by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wand_tilt"
    )

    // Glowing star pulse size
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    // Sparkles float offset
    val sparklesOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkles_offset"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Draw glowing neon aura behind the wand's tip
        val wandTipCenter = Offset(w * 0.35f, h * 0.35f)
        
        // Multi-layered glow
        drawCircle(
            color = CozyRose.copy(alpha = 0.15f),
            radius = w * 0.3f * glowPulse,
            center = wandTipCenter
        )
        drawCircle(
            color = CozyRose.copy(alpha = 0.3f),
            radius = w * 0.18f * glowPulse,
            center = wandTipCenter
        )
        drawCircle(
            color = PastelYellow.copy(alpha = 0.5f),
            radius = w * 0.1f * glowPulse,
            center = wandTipCenter
        )

        // 2. Draw sparkles rotating/pulsing around
        val sparkleRadius = w * 0.03f
        val numSparkles = 4
        for (i in 0 until numSparkles) {
            val angle = Math.toRadians((sparklesOffset + (i * (360 / numSparkles))).toDouble())
            val sx = wandTipCenter.x + cos(angle).toFloat() * (w * 0.22f)
            val sy = wandTipCenter.y + sin(angle).toFloat() * (h * 0.22f)
            
            val scale = 0.4f + 0.6f * sin(Math.toRadians((sparklesOffset * 2 + i * 90).toDouble())).toFloat()
            if (scale > 0.1f) {
                val sparkPath = createStarPath(Offset(sx, sy), sparkleRadius * scale, (sparkleRadius / 2) * scale, 4)
                drawPath(sparkPath, PastelYellow)
                drawPath(sparkPath, Color.White, style = Stroke(width = 1.dp.toPx()))
            }
        }

        // 3. Draw the Magic Wand
        rotate(degrees = wandTilt, pivot = Offset(w * 0.8f, h * 0.8f)) {
            // Wand shaft (diagonal stick)
            val shaftPath = Path().apply {
                moveTo(w * 0.78f, h * 0.82f)
                lineTo(w * 0.40f, h * 0.42f)
                lineTo(w * 0.44f, h * 0.38f)
                lineTo(w * 0.82f, h * 0.78f)
                close()
            }
            // Metallic/magical shaft color
            drawPath(
                path = shaftPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF8E44AD), CozyDeepRose),
                    start = Offset(w * 0.4f, h * 0.4f),
                    end = Offset(w * 0.8f, h * 0.8f)
                )
            )
            drawPath(shaftPath, TextDark, style = Stroke(width = 2.dp.toPx()))

            // Wand tip base collar
            drawCircle(
                color = PastelYellow,
                radius = w * 0.04f,
                center = Offset(w * 0.42f, h * 0.40f)
            )
            drawCircle(
                color = TextDark,
                radius = w * 0.04f,
                center = Offset(w * 0.42f, h * 0.40f),
                style = Stroke(width = 2.dp.toPx())
            )

            // Star on top of the wand
            val mainStar = createStarPath(
                center = Offset(w * 0.38f, h * 0.36f),
                radius = w * 0.14f,
                innerRadius = w * 0.06f
            )
            drawPath(mainStar, PastelYellow)
            drawPath(mainStar, TextDark, style = Stroke(width = 2.5.dp.toPx(), join = StrokeJoin.Round))

            // Star center smiley face for cuteness!
            val starCenterX = w * 0.38f
            val starCenterY = h * 0.36f
            drawCircle(TextDark, 2.dp.toPx(), Offset(starCenterX - 6f, starCenterY - 4f))
            drawCircle(TextDark, 2.dp.toPx(), Offset(starCenterX + 6f, starCenterY - 4f))
            
            val wandSmile = Path().apply {
                arcTo(
                    rect = Rect(starCenterX - 8f, starCenterY - 2f, starCenterX + 8f, starCenterY + 10f),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = false
                )
            }
            drawPath(wandSmile, TextDark, style = Stroke(width = 1.5.dp.toPx()))
        }
    }
}

/**
 * 4. Stickers Icon
 * A cute rounded teddy bear face with a peeled sticker corner effect.
 */
@Composable
fun StickersIcon(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "stickers_anim")

    // Ear wiggling animation
    val earScaleY by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ear_scale_y"
    )

    // Bear eye blink animation (short duration blink)
    val blinkProgress by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                1.0f at 0
                1.0f at 2800
                0.0f at 2900 // blink down
                1.0f at 3000 // open up
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "blink_progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Draw sticker backing shadow
        val stickerShapePath = Path().apply {
            // Circle-like outline but sliced at bottom-right
            arcTo(
                rect = Rect(w * 0.12f, h * 0.15f, w * 0.88f, h * 0.85f),
                startAngleDegrees = -45f,
                sweepAngleDegrees = 270f,
                forceMoveTo = true
            )
            // Peel corner diagonal line
            lineTo(w * 0.82f, h * 0.82f)
            close()
        }
        drawPath(stickerShapePath, Color.Black.copy(alpha = 0.12f), style = Stroke(width = 8.dp.toPx()))

        // 2. Draw sticker base (pastel peach/orange circle)
        drawPath(stickerShapePath, CozyBlush)
        drawPath(stickerShapePath, TextDark, style = Stroke(width = 3.dp.toPx(), join = StrokeJoin.Round))

        // 3. Draw Teddy bear face elements (inside the sticker)
        val fx = w * 0.48f
        val fy = h * 0.50f

        // Ears
        // Left Ear
        drawCircle(
            color = CozyRose,
            radius = w * 0.1f * earScaleY,
            center = Offset(w * 0.32f, h * 0.32f)
        )
        drawCircle(
            color = TextDark,
            radius = w * 0.1f * earScaleY,
            center = Offset(w * 0.32f, h * 0.32f),
            style = Stroke(width = 2.dp.toPx())
        )
        // Right Ear
        drawCircle(
            color = CozyRose,
            radius = w * 0.1f,
            center = Offset(w * 0.64f, h * 0.32f)
        )
        drawCircle(
            color = TextDark,
            radius = w * 0.1f,
            center = Offset(w * 0.64f, h * 0.32f),
            style = Stroke(width = 2.dp.toPx())
        )

        // Snout
        drawCircle(
            color = CozyCreamBackground,
            radius = w * 0.12f,
            center = Offset(fx, fy + h * 0.08f)
        )
        drawCircle(
            color = TextDark,
            radius = w * 0.12f,
            center = Offset(fx, fy + h * 0.08f),
            style = Stroke(width = 2.dp.toPx())
        )

        // Nose
        val nosePath = Path().apply {
            moveTo(fx - 10f, fy + h * 0.04f)
            lineTo(fx + 10f, fy + h * 0.04f)
            quadraticTo(fx, fy + h * 0.1f, fx - 10f, fy + h * 0.04f)
        }
        drawPath(nosePath, TextDark)

        // Mouth (w-shape)
        val leftMouth = Path().apply {
            arcTo(
                rect = Rect(fx - 16f, fy + h * 0.07f, fx, fy + h * 0.13f),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
        }
        val rightMouth = Path().apply {
            arcTo(
                rect = Rect(fx, fy + h * 0.07f, fx + 16f, fy + h * 0.13f),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
        }
        drawPath(leftMouth, TextDark, style = Stroke(width = 2.dp.toPx()))
        drawPath(rightMouth, TextDark, style = Stroke(width = 2.dp.toPx()))

        // Eyes
        val eyeRadius = 4.5.dp.toPx()
        // Left Eye (blinking)
        if (blinkProgress < 0.1f) {
            drawLine(
                color = TextDark,
                start = Offset(fx - w * 0.12f - eyeRadius, fy),
                end = Offset(fx - w * 0.12f + eyeRadius, fy),
                strokeWidth = 2.dp.toPx()
            )
        } else {
            drawCircle(
                color = TextDark,
                radius = eyeRadius,
                center = Offset(fx - w * 0.12f, fy)
            )
        }

        // Right Eye
        drawCircle(
            color = TextDark,
            radius = eyeRadius,
            center = Offset(fx + w * 0.12f, fy)
        )

        // Rosy cheeks
        drawCircle(
            color = CozyRose.copy(alpha = 0.6f),
            radius = w * 0.04f,
            center = Offset(fx - w * 0.18f, fy + h * 0.06f)
        )
        drawCircle(
            color = CozyRose.copy(alpha = 0.6f),
            radius = w * 0.04f,
            center = Offset(fx + w * 0.18f, fy + h * 0.06f)
        )

        // 4. Draw Peeled Corner Fold (Bottom Right)
        val peelPath = Path().apply {
            moveTo(w * 0.82f, h * 0.55f)
            quadraticTo(w * 0.65f, h * 0.65f, w * 0.55f, h * 0.82f)
            lineTo(w * 0.82f, h * 0.82f)
            close()
        }
        // White flipped page color
        drawPath(peelPath, Color.White)
        drawPath(peelPath, TextDark, style = Stroke(width = 3.dp.toPx(), join = StrokeJoin.Round))
    }
}

/**
 * 5. Gallery Icon
 * A playful picture frame containing a smiling, spinning/pulsing sun.
 */
@Composable
fun GalleryIcon(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "gallery_anim")

    // Sun spinning rotation
    val sunRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sun_rotation"
    )

    // Sun rays pulse scale
    val rayPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ray_pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Draw outer Polaroid / Canvas frame
        val frameOuter = Rect(w * 0.15f, h * 0.15f, w * 0.85f, h * 0.85f)
        drawRoundRect(
            color = CozyCreamBackground,
            topLeft = frameOuter.topLeft,
            size = frameOuter.size,
            cornerRadius = CornerRadius(16f, 16f)
        )
        drawRoundRect(
            color = TextDark,
            topLeft = frameOuter.topLeft,
            size = frameOuter.size,
            cornerRadius = CornerRadius(16f, 16f),
            style = Stroke(width = 3.dp.toPx())
        )

        // 2. Draw inner drawing area / picture content
        val frameInner = Rect(w * 0.20f, h * 0.20f, w * 0.80f, h * 0.70f)
        drawRoundRect(
            color = PastelBlue,
            topLeft = frameInner.topLeft,
            size = frameInner.size,
            cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
            color = TextDark,
            topLeft = frameInner.topLeft,
            size = frameInner.size,
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw green hills inside the picture frame (bottom of the drawing)
        val hillPath = Path().apply {
            moveTo(w * 0.20f, h * 0.70f)
            quadraticTo(w * 0.35f, h * 0.58f, w * 0.52f, h * 0.65f)
            quadraticTo(w * 0.68f, h * 0.52f, w * 0.80f, h * 0.70f)
            close()
        }
        drawPath(hillPath, PastelMint)
        drawPath(hillPath, TextDark, style = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Round))

        // 3. Draw a smiling Sun
        val sunCenter = Offset(w * 0.50f, h * 0.42f)
        val sunRadius = w * 0.1f

        // Draw sun rays with rotation
        rotate(degrees = sunRotation, pivot = sunCenter) {
            val numRays = 8
            for (i in 0 until numRays) {
                val angle = Math.toRadians((i * (360 / numRays)).toDouble())
                val startDist = sunRadius + 4f
                val endDist = (sunRadius + 18f) * rayPulseScale
                
                val startX = sunCenter.x + cos(angle).toFloat() * startDist
                val startY = sunCenter.y + sin(angle).toFloat() * startDist
                val endX = sunCenter.x + cos(angle).toFloat() * endDist
                val endY = sunCenter.y + sin(angle).toFloat() * endDist

                drawLine(
                    color = PastelYellow,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Outline for the ray
                drawLine(
                    color = TextDark,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Draw Sun Center circle
        drawCircle(
            color = PastelYellow,
            radius = sunRadius,
            center = sunCenter
        )
        drawCircle(
            color = TextDark,
            radius = sunRadius,
            center = sunCenter,
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw Sun smiling face
        // Eyes
        drawCircle(TextDark, 1.5.dp.toPx(), Offset(sunCenter.x - 8f, sunCenter.y - 2f))
        drawCircle(TextDark, 1.5.dp.toPx(), Offset(sunCenter.x + 8f, sunCenter.y - 2f))
        
        // Smile
        val sunSmile = Path().apply {
            arcTo(
                rect = Rect(sunCenter.x - 10f, sunCenter.y - 4f, sunCenter.x + 10f, sunCenter.y + 10f),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
        }
        drawPath(sunSmile, TextDark, style = Stroke(width = 1.5.dp.toPx()))

        // 4. Draw Polaroid bottom signature bar/dots
        drawCircle(
            color = CozyRose,
            radius = w * 0.015f,
            center = Offset(w * 0.3f, h * 0.78f)
        )
        drawCircle(
            color = PastelBlue,
            radius = w * 0.015f,
            center = Offset(w * 0.38f, h * 0.78f)
        )
        drawCircle(
            color = PastelYellow,
            radius = w * 0.015f,
            center = Offset(w * 0.46f, h * 0.78f)
        )
    }
}
