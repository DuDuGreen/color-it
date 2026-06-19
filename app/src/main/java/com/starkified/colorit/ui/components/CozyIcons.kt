package com.starkified.colorit.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.starkified.colorit.ui.theme.*

/**
 * Chunky arrow path filled with soft orange/peach, thick dark outline,
 * and a glossy white shine streak.
 */
@Composable
fun CozyBackIcon(
    modifier: Modifier = Modifier,
    color: Color = TextDark
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.10f
        
        // Volumetric arrow shape (head + stem)
        val arrowPath = Path().apply {
            moveTo(w * 0.15f, h * 0.5f)
            lineTo(w * 0.50f, h * 0.18f)
            lineTo(w * 0.50f, h * 0.38f)
            lineTo(w * 0.85f, h * 0.38f)
            lineTo(w * 0.85f, h * 0.62f)
            lineTo(w * 0.50f, h * 0.62f)
            lineTo(w * 0.50f, h * 0.82f)
            close()
        }
        
        // Fill base shape with pastel peach
        drawPath(path = arrowPath, color = PastelPeach)
        
        // Thick outline
        drawPath(
            path = arrowPath,
            color = TextDark,
            style = Stroke(width = strokeWidth, join = StrokeJoin.Round, cap = StrokeCap.Round)
        )
        
        // Shiny reflection highlight
        val shinePath = Path().apply {
            moveTo(w * 0.45f, h * 0.28f)
            lineTo(w * 0.22f, h * 0.47f)
            moveTo(w * 0.52f, h * 0.44f)
            lineTo(w * 0.80f, h * 0.44f)
        }
        drawPath(
            path = shinePath,
            color = Color.White.copy(alpha = 0.75f),
            style = Stroke(width = strokeWidth * 0.6f, cap = StrokeCap.Round)
        )
    }
}

/**
 * Curved loop-back arrow for Undo actions, with yellow fill and white gloss.
 */
@Composable
fun CozyUndoIcon(
    modifier: Modifier = Modifier,
    color: Color = TextDark
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.10f
        val arcSize = Size(w * 0.50f, h * 0.50f)
        
        // Wide fill strokes (PastelYellow)
        val fillStyle = Stroke(width = strokeWidth * 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawArc(
            color = PastelYellow,
            startAngle = 0f,
            sweepAngle = -240f,
            useCenter = false,
            topLeft = Offset(w * 0.25f, h * 0.25f),
            size = arcSize,
            style = fillStyle
        )
        drawLine(
            color = PastelYellow,
            start = Offset(w * 0.25f, h * 0.50f),
            end = Offset(w * 0.12f, h * 0.37f),
            strokeWidth = strokeWidth * 2.5f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = PastelYellow,
            start = Offset(w * 0.25f, h * 0.50f),
            end = Offset(w * 0.42f, h * 0.40f),
            strokeWidth = strokeWidth * 2.5f,
            cap = StrokeCap.Round
        )

        // Outline strokes (TextDark)
        val outlineStyle = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawArc(
            color = TextDark,
            startAngle = 0f,
            sweepAngle = -240f,
            useCenter = false,
            topLeft = Offset(w * 0.25f, h * 0.25f),
            size = arcSize,
            style = outlineStyle
        )
        drawLine(
            color = TextDark,
            start = Offset(w * 0.25f, h * 0.50f),
            end = Offset(w * 0.12f, h * 0.37f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = TextDark,
            start = Offset(w * 0.25f, h * 0.50f),
            end = Offset(w * 0.42f, h * 0.40f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        
        // White shine (Gloss)
        drawArc(
            color = Color.White.copy(alpha = 0.75f),
            startAngle = -30f,
            sweepAngle = -100f,
            useCenter = false,
            topLeft = Offset(w * 0.25f, h * 0.25f),
            size = arcSize,
            style = Stroke(width = strokeWidth * 0.6f, cap = StrokeCap.Round)
        )
    }
}

/**
 * Curved loop-forward arrow for Redo actions.
 */
@Composable
fun CozyRedoIcon(
    modifier: Modifier = Modifier,
    color: Color = TextDark
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.10f
        val arcSize = Size(w * 0.50f, h * 0.50f)
        
        // Wide fill strokes (PastelYellow)
        val fillStyle = Stroke(width = strokeWidth * 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawArc(
            color = PastelYellow,
            startAngle = 180f,
            sweepAngle = 240f,
            useCenter = false,
            topLeft = Offset(w * 0.23f, h * 0.25f),
            size = arcSize,
            style = fillStyle
        )
        drawLine(
            color = PastelYellow,
            start = Offset(w * 0.75f, h * 0.50f),
            end = Offset(w * 0.88f, h * 0.37f),
            strokeWidth = strokeWidth * 2.5f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = PastelYellow,
            start = Offset(w * 0.75f, h * 0.50f),
            end = Offset(w * 0.58f, h * 0.40f),
            strokeWidth = strokeWidth * 2.5f,
            cap = StrokeCap.Round
        )

          // Outline strokes (TextDark)
        val outlineStyle = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawArc(
            color = TextDark,
            startAngle = 180f,
            sweepAngle = 240f,
            useCenter = false,
            topLeft = Offset(w * 0.23f, h * 0.25f),
            size = arcSize,
            style = outlineStyle
        )
        drawLine(
            color = TextDark,
            start = Offset(w * 0.75f, h * 0.50f),
            end = Offset(w * 0.88f, h * 0.37f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = TextDark,
            start = Offset(w * 0.75f, h * 0.50f),
            end = Offset(w * 0.58f, h * 0.40f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        
        // White shine (Gloss)
        drawArc(
            color = Color.White.copy(alpha = 0.75f),
            startAngle = -150f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(w * 0.23f, h * 0.25f),
            size = arcSize,
            style = Stroke(width = strokeWidth * 0.6f, cap = StrokeCap.Round)
        )
    }
}

/**
 * Cute retro floppy disk save icon with a green body, white label card,
 * and white highlights.
 */
@Composable
fun CozySaveIcon(
    modifier: Modifier = Modifier,
    color: Color = TextDark
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.09f
        
        val path = Path().apply {
            moveTo(w * 0.15f, h * 0.15f)
            lineTo(w * 0.65f, h * 0.15f)
            lineTo(w * 0.85f, h * 0.35f)
            lineTo(w * 0.85f, h * 0.85f)
            lineTo(w * 0.15f, h * 0.85f)
            close()
        }
        
        // 1. Fill base with PastelMint
        drawPath(path = path, color = PastelMint)
        
        // 2. White label card
        val labelRect = Rect(w * 0.28f, h * 0.50f, w * 0.72f, h * 0.82f)
        drawRoundRect(
            color = Color.White,
            topLeft = labelRect.topLeft,
            size = labelRect.size,
            cornerRadius = CornerRadius(w * 0.05f, h * 0.05f)
        )
        drawRoundRect(
            color = TextDark,
            topLeft = labelRect.topLeft,
            size = labelRect.size,
            cornerRadius = CornerRadius(w * 0.05f, h * 0.05f),
            style = Stroke(width = strokeWidth * 0.8f)
        )
        
        // Draw tiny note lines on the label
        drawLine(color = PastelPink, start = Offset(w * 0.38f, h * 0.62f), end = Offset(w * 0.62f, h * 0.62f), strokeWidth = strokeWidth * 0.6f, cap = StrokeCap.Round)
        drawLine(color = PastelBlue, start = Offset(w * 0.38f, h * 0.72f), end = Offset(w * 0.62f, h * 0.72f), strokeWidth = strokeWidth * 0.6f, cap = StrokeCap.Round)

        // 3. Metal slider cover (top part of floppy)
        val sliderRect = Rect(w * 0.32f, h * 0.15f, w * 0.58f, h * 0.38f)
        drawRect(color = Color(0xFFE2E8F0), topLeft = sliderRect.topLeft, size = sliderRect.size)
        drawRect(color = TextDark, topLeft = sliderRect.topLeft, size = sliderRect.size, style = Stroke(width = strokeWidth * 0.8f))
        // Tiny slider hole
        drawRect(color = TextDark, topLeft = Offset(w * 0.38f, h * 0.22f), size = Size(w * 0.06f, h * 0.10f))

        // 4. Outer thick outline
        drawPath(
            path = path,
            color = TextDark,
            style = Stroke(width = strokeWidth, join = StrokeJoin.Round, cap = StrokeCap.Round)
        )
        
        // 5. Shiny white highlight on the top left
        drawLine(
            color = Color.White.copy(alpha = 0.75f),
            start = Offset(w * 0.22f, h * 0.22f),
            end = Offset(w * 0.22f, h * 0.45f),
            strokeWidth = strokeWidth * 0.6f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Cute pink waste bin for Delete / Trash actions.
 */
@Composable
fun CozyTrashIcon(
    modifier: Modifier = Modifier,
    color: Color = TextDark
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.09f
        
        val bodyPath = Path().apply {
            moveTo(w * 0.25f, h * 0.32f)
            lineTo(w * 0.30f, h * 0.82f)
            quadraticTo(w * 0.32f, h * 0.88f, w * 0.38f, h * 0.88f)
            lineTo(w * 0.62f, h * 0.88f)
            quadraticTo(w * 0.68f, h * 0.88f, w * 0.70f, h * 0.82f)
            lineTo(w * 0.75f, h * 0.32f)
            close()
        }
        
        // 1. Fill body with PastelPink
        drawPath(path = bodyPath, color = PastelPink)
        
        // 2. Fill lid bar with CozyRose
        val lidRect = Rect(w * 0.12f, h * 0.22f, w * 0.88f, h * 0.32f)
        drawRoundRect(
            color = CozyRose,
            topLeft = lidRect.topLeft,
            size = lidRect.size,
            cornerRadius = CornerRadius(w * 0.04f, h * 0.04f)
        )
        
        // 3. Outlines
        drawPath(
            path = bodyPath,
            color = TextDark,
            style = Stroke(width = strokeWidth, join = StrokeJoin.Round, cap = StrokeCap.Round)
        )
        drawRoundRect(
            color = TextDark,
            topLeft = lidRect.topLeft,
            size = lidRect.size,
            cornerRadius = CornerRadius(w * 0.04f, h * 0.04f),
            style = Stroke(width = strokeWidth, join = StrokeJoin.Round, cap = StrokeCap.Round)
        )
        
        // Handle on lid
        drawArc(
            color = TextDark,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.38f, h * 0.08f),
            size = Size(w * 0.24f, h * 0.16f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // Vertical lines
        drawLine(color = TextDark, start = Offset(w * 0.42f, h * 0.44f), end = Offset(w * 0.42f, h * 0.76f), strokeWidth = strokeWidth * 0.9f, cap = StrokeCap.Round)
        drawLine(color = TextDark, start = Offset(w * 0.58f, h * 0.44f), end = Offset(w * 0.58f, h * 0.76f), strokeWidth = strokeWidth * 0.9f, cap = StrokeCap.Round)
        
        // 4. White reflections
        drawCircle(color = Color.White.copy(alpha = 0.8f), radius = w * 0.04f, center = Offset(w * 0.33f, h * 0.40f))
        drawLine(color = Color.White.copy(alpha = 0.8f), start = Offset(w * 0.18f, h * 0.25f), end = Offset(w * 0.35f, h * 0.25f), strokeWidth = strokeWidth * 0.5f, cap = StrokeCap.Round)
    }
}

/**
 * Cozy paintbrush illustration with tan wooden handle, silver ferrule, and pink bristles.
 */
@Composable
fun CozyBrushIcon(
    modifier: Modifier = Modifier,
    color: Color = TextDark
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.09f
        
        // 1. Draw wood handle (tan brown)
        drawLine(
            color = Color(0xFFE5C4A3),
            start = Offset(w * 0.2f, h * 0.8f),
            end = Offset(w * 0.55f, h * 0.45f),
            strokeWidth = w * 0.18f,
            cap = StrokeCap.Round
        )
        
        // Ferrule (silver metal band)
        drawLine(
            color = Color(0xFFBDD0EC),
            start = Offset(w * 0.54f, h * 0.46f),
            end = Offset(w * 0.68f, h * 0.32f),
            strokeWidth = w * 0.22f,
            cap = StrokeCap.Square
        )
        
        // Bristles (CozyRose pink paint tip)
        val bristlePath = Path().apply {
            moveTo(w * 0.65f, h * 0.35f)
            quadraticTo(w * 0.88f, h * 0.33f, w * 0.90f, h * 0.10f)
            quadraticTo(w * 0.67f, h * 0.12f, w * 0.54f, h * 0.46f)
            close()
        }
        drawPath(path = bristlePath, color = CozyRose)
        
        // 2. Outlines
        drawLine(
            color = TextDark,
            start = Offset(w * 0.2f, h * 0.8f),
            end = Offset(w * 0.55f, h * 0.45f),
            strokeWidth = w * 0.12f,
            cap = StrokeCap.Round
        )
        
        // Ferrule outline
        drawLine(
            color = TextDark,
            start = Offset(w * 0.54f, h * 0.46f),
            end = Offset(w * 0.68f, h * 0.32f),
            strokeWidth = w * 0.22f,
            cap = StrokeCap.Square
        )
        
        // Bristles outline
        drawPath(
            path = bristlePath,
            color = TextDark,
            style = Stroke(width = strokeWidth, join = StrokeJoin.Round, cap = StrokeCap.Round)
        )
        
        // 3. Shiny highlight
        drawCircle(color = Color.White.copy(alpha = 0.8f), radius = w * 0.03f, center = Offset(w * 0.72f, h * 0.22f))
    }
}

/**
 * Cozy paint bucket (Fill) icon spilling a droplet.
 */
@Composable
fun CozyFillIcon(
    modifier: Modifier = Modifier,
    color: Color = TextDark
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.09f
        
        val bucketPath = Path().apply {
            moveTo(w * 0.22f, h * 0.45f)
            lineTo(w * 0.45f, h * 0.22f)
            lineTo(w * 0.68f, h * 0.48f)
            lineTo(w * 0.45f, h * 0.71f)
            close()
        }
        
        // 1. Fill bucket with PastelBlue
        drawPath(path = bucketPath, color = PastelBlue)
        
        // 2. Fill spilling paint with CozyRose
        val dropPath = Path().apply {
            moveTo(w * 0.68f, h * 0.62f)
            quadraticTo(w * 0.82f, h * 0.76f, w * 0.68f, h * 0.88f)
            quadraticTo(w * 0.54f, h * 0.76f, w * 0.68f, h * 0.62f)
            close()
        }
        drawPath(path = dropPath, color = CozyRose)
        
        // 3. Outlines
        drawPath(
            path = bucketPath,
            color = TextDark,
            style = Stroke(width = strokeWidth, join = StrokeJoin.Round, cap = StrokeCap.Round)
        )
        drawPath(
            path = dropPath,
            color = TextDark,
            style = Stroke(width = strokeWidth, join = StrokeJoin.Round, cap = StrokeCap.Round)
        )
        
        // Wire handle
        drawArc(
            color = TextDark,
            startAngle = 185f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = Offset(w * 0.15f, h * 0.15f),
            size = Size(w * 0.5f, h * 0.5f),
            style = Stroke(width = strokeWidth * 0.8f, cap = StrokeCap.Round)
        )
        
        // 4. Shiny white reflections
        drawCircle(color = Color.White.copy(alpha = 0.8f), radius = w * 0.04f, center = Offset(w * 0.38f, h * 0.38f))
        drawCircle(color = Color.White.copy(alpha = 0.8f), radius = w * 0.03f, center = Offset(w * 0.66f, h * 0.74f))
    }
}

/**
 * Colorful premium rainbow arch for Spectrum selector nestled between two white fluffy clouds.
 */
@Composable
fun CozySpectrumIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.08f
        val archStroke = w * 0.10f
        
        // 1. Rainbow fills (Red, Orange, Yellow, Green, Blue arches)
        // Red arch (outer)
        drawArc(
            color = Color(0xFFFF3B30),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.10f, h * 0.22f),
            size = Size(w * 0.80f, h * 0.80f),
            style = Stroke(width = archStroke, cap = StrokeCap.Round)
        )
        // Orange arch
        drawArc(
            color = Color(0xFFFF9500),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.17f, h * 0.29f),
            size = Size(w * 0.66f, h * 0.66f),
            style = Stroke(width = archStroke, cap = StrokeCap.Round)
        )
        // Yellow arch
        drawArc(
            color = Color(0xFFFFCC00),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.24f, h * 0.36f),
            size = Size(w * 0.52f, h * 0.52f),
            style = Stroke(width = archStroke, cap = StrokeCap.Round)
        )
        // Green arch
        drawArc(
            color = Color(0xFF34C759),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.31f, h * 0.43f),
            size = Size(w * 0.38f, h * 0.38f),
            style = Stroke(width = archStroke, cap = StrokeCap.Round)
        )
        // Blue arch (inner)
        drawArc(
            color = Color(0xFF007AFF),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.38f, h * 0.50f),
            size = Size(w * 0.24f, h * 0.24f),
            style = Stroke(width = archStroke, cap = StrokeCap.Round)
        )
        
        // 2. Thick Outlines
        // Outer outline
        drawArc(
            color = TextDark,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.10f, h * 0.22f),
            size = Size(w * 0.80f, h * 0.80f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        // Innermost outline
        drawArc(
            color = TextDark,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.38f, h * 0.50f),
            size = Size(w * 0.24f, h * 0.24f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // 3. Two Fluffy White Clouds at the bases
        val cloudPathLeft = Path().apply {
            addOval(Rect(Offset(w * 0.02f, h * 0.55f), Size(w * 0.28f, h * 0.28f)))
            addOval(Rect(Offset(w * 0.12f, h * 0.45f), Size(w * 0.28f, h * 0.28f)))
            addOval(Rect(Offset(w * 0.22f, h * 0.55f), Size(w * 0.28f, h * 0.28f)))
        }
        val cloudPathRight = Path().apply {
            addOval(Rect(Offset(w * 0.52f, h * 0.55f), Size(w * 0.28f, h * 0.28f)))
            addOval(Rect(Offset(w * 0.62f, h * 0.45f), Size(w * 0.28f, h * 0.28f)))
            addOval(Rect(Offset(w * 0.72f, h * 0.55f), Size(w * 0.28f, h * 0.28f)))
        }
        
        drawPath(path = cloudPathLeft, color = Color.White)
        drawPath(path = cloudPathLeft, color = TextDark, style = Stroke(width = strokeWidth * 0.8f))
        drawPath(path = cloudPathRight, color = Color.White)
        drawPath(path = cloudPathRight, color = TextDark, style = Stroke(width = strokeWidth * 0.8f))
    }
}

/**
 * Playful gear wheel for Settings access with volumetric purple fill and shines.
 */
@Composable
fun CozySettingsIcon(
    modifier: Modifier = Modifier,
    color: Color = TextDark
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val cy = h * 0.5f
        val rOuter = w * 0.26f
        val rInner = w * 0.10f
        val strokeWidth = w * 0.09f
        
        // 1. Fill body with PastelPurple
        drawCircle(color = PastelPurple, radius = rOuter, center = Offset(cx, cy))
        
        // Draw teeth fill
        val teethCount = 8
        for (i in 0 until teethCount) {
            val angle = i * (2.0 * Math.PI / teethCount)
            val cosVal = Math.cos(angle).toFloat()
            val sinVal = Math.sin(angle).toFloat()
            drawLine(
                color = PastelPurple,
                start = Offset(cx, cy),
                end = Offset(cx + (rOuter + w * 0.10f) * cosVal, cy + (rOuter + w * 0.10f) * sinVal),
                strokeWidth = strokeWidth * 2.8f,
                cap = StrokeCap.Round
            )
        }
        
        // Hollow center hole
        drawCircle(color = Color.White, radius = rInner, center = Offset(cx, cy))
        
        // 2. Outlines
        drawCircle(color = TextDark, radius = rInner, center = Offset(cx, cy), style = Stroke(width = strokeWidth))
        drawCircle(color = TextDark, radius = rOuter, center = Offset(cx, cy), style = Stroke(width = strokeWidth))
        
        for (i in 0 until teethCount) {
            val angle = i * (2.0 * Math.PI / teethCount)
            val cosVal = Math.cos(angle).toFloat()
            val sinVal = Math.sin(angle).toFloat()
            val startX = cx + rOuter * cosVal
            val startY = cy + rOuter * sinVal
            val endX = cx + (rOuter + w * 0.10f) * cosVal
            val endY = cy + (rOuter + w * 0.10f) * sinVal
            drawLine(
                color = TextDark,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = strokeWidth * 1.2f,
                cap = StrokeCap.Round
            )
        }
        
        // 3. Shiny white reflection
        drawCircle(color = Color.White.copy(alpha = 0.75f), radius = w * 0.04f, center = Offset(cx - w * 0.12f, cy - h * 0.12f))
    }
}

/**
 * Volumetric picture frame icon for Gallery access with mountain fills and sun details.
 */
@Composable
fun CozyGalleryIcon(
    modifier: Modifier = Modifier,
    color: Color = TextDark
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.09f
        
        // Frame rectangle
        val frameRect = Rect(w * 0.15f, h * 0.15f, w * 0.85f, h * 0.85f)
        
        // 1. Fill frame with CardYellow (warm cream)
        drawRoundRect(
            color = CardYellow,
            topLeft = frameRect.topLeft,
            size = frameRect.size,
            cornerRadius = CornerRadius(w * 0.10f, h * 0.10f)
        )
        
        // 2. Mountains fill (PastelMint)
        val mtnPath = Path().apply {
            moveTo(w * 0.17f, h * 0.83f)
            lineTo(w * 0.45f, h * 0.45f)
            lineTo(w * 0.62f, h * 0.65f)
            lineTo(w * 0.72f, h * 0.52f)
            lineTo(w * 0.83f, h * 0.83f)
            close()
        }
        drawPath(path = mtnPath, color = PastelMint)
        
        // 3. Sun fill (PastelPeach)
        drawCircle(
            color = PastelPeach,
            radius = w * 0.08f,
            center = Offset(w * 0.65f, h * 0.35f)
        )
        
        // 4. Outlines
        drawRoundRect(
            color = TextDark,
            topLeft = frameRect.topLeft,
            size = frameRect.size,
            cornerRadius = CornerRadius(w * 0.10f, h * 0.10f),
            style = Stroke(width = strokeWidth)
        )
        drawPath(
            path = mtnPath,
            color = TextDark,
            style = Stroke(width = strokeWidth * 0.9f, join = StrokeJoin.Round, cap = StrokeCap.Round)
        )
        drawCircle(
            color = TextDark,
            radius = w * 0.08f,
            center = Offset(w * 0.65f, h * 0.35f),
            style = Stroke(width = strokeWidth * 0.9f)
        )
        
        // 5. White highlight reflection
        drawLine(
            color = Color.White.copy(alpha = 0.8f),
            start = Offset(w * 0.22f, h * 0.22f),
            end = Offset(w * 0.45f, h * 0.22f),
            strokeWidth = strokeWidth * 0.6f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Volumetric pastel sky-blue tray carrying a soft orange arrow pointing upwards.
 */
@Composable
fun CozyShareIcon(
    modifier: Modifier = Modifier,
    color: Color = TextDark
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.09f
        
        val trayPath = Path().apply {
            moveTo(w * 0.2f, h * 0.48f)
            lineTo(w * 0.2f, h * 0.82f)
            quadraticTo(w * 0.22f, h * 0.88f, w * 0.28f, h * 0.88f)
            lineTo(w * 0.72f, h * 0.88f)
            quadraticTo(w * 0.78f, h * 0.88f, w * 0.80f, h * 0.82f)
            lineTo(w * 0.80f, h * 0.48f)
            close()
        }
        
        // 1. Fill tray with PastelBlue
        drawPath(path = trayPath, color = PastelBlue)
        
        // 2. Fill upward arrow with PastelPeach / Orange
        val arrowPath = Path().apply {
            moveTo(w * 0.5f, h * 0.12f)
            lineTo(w * 0.70f, h * 0.35f)
            lineTo(w * 0.58f, h * 0.35f)
            lineTo(w * 0.58f, h * 0.65f)
            lineTo(w * 0.42f, h * 0.65f)
            lineTo(w * 0.42f, h * 0.35f)
            lineTo(w * 0.30f, h * 0.35f)
            close()
        }
        drawPath(path = arrowPath, color = PastelPeach)
        
        // 3. Outlines
        drawPath(path = trayPath, color = TextDark, style = Stroke(width = strokeWidth, join = StrokeJoin.Round, cap = StrokeCap.Round))
        drawPath(path = arrowPath, color = TextDark, style = Stroke(width = strokeWidth, join = StrokeJoin.Round, cap = StrokeCap.Round))
        
        // 4. White shine highlights
        drawCircle(color = Color.White.copy(alpha = 0.8f), radius = w * 0.03f, center = Offset(w * 0.48f, h * 0.24f))
    }
}

/**
 * Volumetric pencil with yellow body, eraser, and metal ferrule.
 */
@Composable
fun CozyPencilIcon(
    modifier: Modifier = Modifier,
    color: Color = TextDark
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.09f
        
        // Yellow pencil body rect
        val bodyPath = Path().apply {
            moveTo(w * 0.28f, h * 0.72f)
            lineTo(w * 0.38f, h * 0.82f)
            lineTo(w * 0.78f, h * 0.42f)
            lineTo(w * 0.68f, h * 0.32f)
            close()
        }
        drawPath(path = bodyPath, color = PastelYellow)
        
        // Tan wood collar tip
        val collarPath = Path().apply {
            moveTo(w * 0.28f, h * 0.72f)
            lineTo(w * 0.18f, h * 0.82f)
            lineTo(w * 0.38f, h * 0.82f)
            close()
        }
        drawPath(path = collarPath, color = Color(0xFFF6D5B8))
        
        // Lead point tip (dark)
        val leadPath = Path().apply {
            moveTo(w * 0.22f, h * 0.78f)
            lineTo(w * 0.12f, h * 0.88f)
            lineTo(w * 0.28f, h * 0.88f)
            close()
        }
        drawPath(path = leadPath, color = TextDark)
        
        // Pink eraser end
        val eraserPath = Path().apply {
            moveTo(w * 0.68f, h * 0.32f)
            lineTo(w * 0.78f, h * 0.42f)
            lineTo(w * 0.85f, h * 0.35f)
            lineTo(w * 0.75f, h * 0.25f)
            close()
        }
        drawPath(path = eraserPath, color = PastelPink)
        
        // Silver metal band (ferrule)
        val bandPath = Path().apply {
            moveTo(w * 0.64f, h * 0.28f)
            lineTo(w * 0.74f, h * 0.38f)
            lineTo(w * 0.71f, h * 0.41f)
            lineTo(w * 0.61f, h * 0.31f)
            close()
        }
        drawPath(path = bandPath, color = Color(0xFFCBD5E1))

        // Outlines
        drawPath(path = bodyPath, color = TextDark, style = Stroke(width = strokeWidth, join = StrokeJoin.Round, cap = StrokeCap.Round))
        drawPath(path = collarPath, color = TextDark, style = Stroke(width = strokeWidth, join = StrokeJoin.Round, cap = StrokeCap.Round))
        drawPath(path = eraserPath, color = TextDark, style = Stroke(width = strokeWidth, join = StrokeJoin.Round, cap = StrokeCap.Round))
        
        // White reflection streak
        drawLine(
            color = Color.White.copy(alpha = 0.8f),
            start = Offset(w * 0.45f, h * 0.55f),
            end = Offset(w * 0.62f, h * 0.38f),
            strokeWidth = strokeWidth * 0.6f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Volumetric highlighter marker pen with flat chisel neon yellow tip.
 */
@Composable
fun CozyMarkerIcon(
    modifier: Modifier = Modifier,
    color: Color = TextDark
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.09f
        
        // Body (Neon green/mint)
        drawRoundRect(
            color = PastelMint,
            topLeft = Offset(w * 0.30f, h * 0.32f),
            size = Size(w * 0.40f, h * 0.54f),
            cornerRadius = CornerRadius(w * 0.08f, h * 0.08f)
        )
        
        // Flat tip (Neon yellow / green)
        val tipPath = Path().apply {
            moveTo(w * 0.38f, h * 0.32f)
            lineTo(w * 0.42f, h * 0.14f)
            lineTo(w * 0.58f, h * 0.14f)
            lineTo(w * 0.62f, h * 0.32f)
            close()
        }
        drawPath(path = tipPath, color = PastelYellow)
        
        // Outlines
        drawRoundRect(
            color = TextDark,
            topLeft = Offset(w * 0.30f, h * 0.32f),
            size = Size(w * 0.40f, h * 0.54f),
            cornerRadius = CornerRadius(w * 0.08f, h * 0.08f),
            style = Stroke(width = strokeWidth)
        )
        drawPath(path = tipPath, color = TextDark, style = Stroke(width = strokeWidth, join = StrokeJoin.Round))
        
        // Highlight
        drawCircle(color = Color.White.copy(alpha = 0.8f), radius = w * 0.04f, center = Offset(w * 0.38f, h * 0.42f))
    }
}

/**
 * Cozy slanted eraser block with pink body and white sleeve.
 */
@Composable
fun CozyEraserIcon(
    modifier: Modifier = Modifier,
    color: Color = TextDark
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.09f
        
        // Slanted eraser block
        val eraserPath = Path().apply {
            moveTo(w * 0.25f, h * 0.45f)
            lineTo(w * 0.55f, h * 0.15f)
            lineTo(w * 0.85f, h * 0.45f)
            lineTo(w * 0.55f, h * 0.75f)
            close()
        }
        drawPath(path = eraserPath, color = CozyRose)
        
        // Cardboard sleeve cover (middle part)
        val sleevePath = Path().apply {
            moveTo(w * 0.35f, h * 0.35f)
            lineTo(w * 0.55f, h * 0.15f)
            lineTo(w * 0.70f, h * 0.30f)
            lineTo(w * 0.50f, h * 0.50f)
            close()
        }
        drawPath(path = sleevePath, color = Color.White)
        
        // Outlines
        drawPath(path = eraserPath, color = TextDark, style = Stroke(width = strokeWidth, join = StrokeJoin.Round))
        drawPath(path = sleevePath, color = TextDark, style = Stroke(width = strokeWidth, join = StrokeJoin.Round))
        
        // Details & highlight
        drawCircle(color = Color.White.copy(alpha = 0.8f), radius = w * 0.04f, center = Offset(w * 0.55f, h * 0.35f))
    }
}

/**
 * Straight line icon: a diagonal line with circle endpoints, like a ruler line tool.
 */
@Composable
fun CozyLineIcon(
    modifier: Modifier = Modifier,
    color: Color = TextDark
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.10f

        // Main diagonal line from top-left to bottom-right
        drawLine(
            color = color,
            start = Offset(w * 0.15f, h * 0.15f),
            end = Offset(w * 0.85f, h * 0.85f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Start endpoint dot
        drawCircle(
            color = color,
            radius = strokeWidth * 0.85f,
            center = Offset(w * 0.15f, h * 0.15f)
        )

        // End endpoint dot
        drawCircle(
            color = color,
            radius = strokeWidth * 0.85f,
            center = Offset(w * 0.85f, h * 0.85f)
        )
    }
}
