package com.starkified.colorit.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.starkified.colorit.ui.theme.*
import kotlin.math.sin
import kotlin.random.Random

private data class CloudConfig(
    val initialX: Float,
    val y: Float,
    val speed: Float,
    val scale: Float
)

private data class FlowerConfig(
    val initialX: Float,
    val initialY: Float,
    val speed: Float,
    val sizeDp: Float,
    val swaySpeed: Float,
    val swayScale: Float,
    val color: Color
)

@Composable
fun BubbleBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Generate static cloud configurations
    val cloudConfigs = remember {
        val rand = Random(99)
        List(4) { index ->
            CloudConfig(
                initialX = rand.nextFloat(),
                y = 50f + index * 60f + rand.nextFloat() * 30f,
                speed = 0.005f + rand.nextFloat() * 0.008f,
                scale = 0.6f + rand.nextFloat() * 0.6f
            )
        }
    }

    // Generate static floating flower configurations
    val flowerConfigs = remember {
        val rand = Random(456)
        val colors = listOf(PastelPink, PastelYellow, PastelPurple, CozyBlush)
        List(10) { index ->
            FlowerConfig(
                initialX = rand.nextFloat(),
                initialY = rand.nextFloat() * 1.2f,
                speed = 0.02f + rand.nextFloat() * 0.03f,
                sizeDp = 8f + rand.nextFloat() * 12f,
                swaySpeed = 0.8f + rand.nextFloat() * 1.2f,
                swayScale = 0.03f + rand.nextFloat() * 0.03f,
                color = colors[index % colors.size]
            )
        }
    }

    // High performance time animation loop
    val infiniteTransition = rememberInfiniteTransition(label = "countryside_time_loop")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time_ticker"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CountrySky) // Soft sky blue background base
            .drawBehind {
                val w = size.width
                val h = size.height

                // 1. Draw Clouds drifting horizontally
                cloudConfigs.forEach { cloud ->
                    var xFraction = (cloud.initialX + time * cloud.speed) % 1.2f
                    if (xFraction < -0.2f) {
                        xFraction += 1.4f
                    }
                    val cx = (xFraction - 0.2f) * w
                    val cy = cloud.y.dp.toPx()
                    val r = 20.dp.toPx() * cloud.scale

                    // Draw cloud body (3 overlapping white circles)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f),
                        radius = r,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f),
                        radius = r * 1.3f,
                        center = Offset(cx + r * 0.8f, cy - r * 0.3f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f),
                        radius = r,
                        center = Offset(cx + r * 1.6f, cy)
                    )
                }

                // 2. Draw Rolling Green Hills at the bottom
                // Back Hill (darker green)
                val backHillPath = Path().apply {
                    moveTo(0f, h)
                    lineTo(0f, h * 0.70f)
                    quadraticTo(
                        w * 0.4f, h * 0.60f,
                        w, h * 0.75f
                    )
                    lineTo(w, h)
                    close()
                }
                drawPath(backHillPath, CountryGrassDark)

                // Back Hill outline
                val backHillOutline = Path().apply {
                    moveTo(0f, h * 0.70f)
                    quadraticTo(
                        w * 0.4f, h * 0.60f,
                        w, h * 0.75f
                    )
                }
                drawPath(
                    path = backHillOutline,
                    color = CountryOutline,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 3.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                )

                // Front Hill (lighter green)
                val frontHillPath = Path().apply {
                    moveTo(0f, h)
                    lineTo(0f, h * 0.84f)
                    quadraticTo(
                        w * 0.65f, h * 0.74f,
                        w, h * 0.82f
                    )
                    lineTo(w, h)
                    close()
                }
                drawPath(frontHillPath, CountryGrass)

                // Front Hill outline
                val frontHillOutline = Path().apply {
                    moveTo(0f, h * 0.84f)
                    quadraticTo(
                        w * 0.65f, h * 0.74f,
                        w, h * 0.82f
                    )
                }
                drawPath(
                    path = frontHillOutline,
                    color = CountryOutline,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 4.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                )

                // 3. Draw Flowers floating upwards
                flowerConfigs.forEach { flower ->
                    // Vertical position (wrap bottom to top)
                    var yFraction = (flower.initialY - time * flower.speed) % 1.2f
                    if (yFraction < -0.1f) {
                        yFraction += 1.2f
                    }
                    val cy = yFraction * h

                    // Horizontal sway wiggles
                    val sway = sin(time * flower.swaySpeed) * flower.swayScale
                    var xFraction = flower.initialX + sway
                    xFraction = xFraction.coerceIn(0f, 1f)
                    val cx = xFraction * w

                    val sizePx = flower.sizeDp.dp.toPx()
                    val petalRadius = sizePx / 3
                    val centerRadius = sizePx / 4

                    // Draw 5 flower petals
                    for (i in 0 until 5) {
                        val angle = Math.toRadians((i * 72).toDouble())
                        val px = cx + (petalRadius * 1.1f * kotlin.math.cos(angle)).toFloat()
                        val py = cy + (petalRadius * 1.1f * kotlin.math.sin(angle)).toFloat()
                        drawCircle(
                            color = flower.color,
                            radius = petalRadius,
                            center = Offset(px, py)
                        )
                        // Dark outline for petals
                        drawCircle(
                            color = CountryOutline,
                            radius = petalRadius,
                            center = Offset(px, py),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                        )
                    }

                    // Draw yellow flower center core
                    drawCircle(
                        color = PastelYellow,
                        radius = centerRadius,
                        center = Offset(cx, cy)
                    )
                    // Core outline
                    drawCircle(
                        color = CountryOutline,
                        radius = centerRadius,
                        center = Offset(cx, cy),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                    )
                }
            }
    ) {
        content()
    }
}
