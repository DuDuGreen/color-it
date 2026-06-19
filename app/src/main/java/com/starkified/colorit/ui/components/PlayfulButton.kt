package com.starkified.colorit.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.starkified.colorit.ui.theme.CountryOutline

/**
 * Shorthand helper to dynamically shift the Saturation and Value (brightness) channels of a Compose Color.
 * Useful for building harmonious, volumetric light/dark gradients and 3D depth shadows.
 */
private fun Color.shiftHsv(sFactor: Float, vFactor: Float, minVOffset: Float = 0f): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    hsv[1] = (hsv[1] * sFactor).coerceIn(0f, 1f)
    hsv[2] = (hsv[2] * vFactor + minVOffset).coerceIn(0f, 1f)
    return Color(android.graphics.Color.HSVToColor(hsv))
}

@Composable
fun PlayfulButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    shape: Shape = MaterialTheme.shapes.medium,
    border: BorderStroke? = BorderStroke(2.5.dp, CountryOutline),
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 3D tactile depth parameters
    val depthHeight = 4.dp
    val activeDepth = if (enabled) depthHeight else 0.dp

    // Bouncy spring animations for mechanical push-down feedback
    val scale by animateFloatAsState(
        targetValue = if (enabled && isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 350f),
        label = "ButtonScale"
    )
    val rotation by animateFloatAsState(
        targetValue = if (enabled && isPressed) -1.2f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "ButtonRotation"
    )
    val translationY by animateDpAsState(
        targetValue = if (enabled && isPressed) activeDepth else 0.dp,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 350f),
        label = "ButtonTranslation"
    )

    // Compute HSL-shifted gradients and depth colors based on the base background color
    val lighterColor = remember(backgroundColor) {
        backgroundColor.shiftHsv(sFactor = 0.85f, vFactor = 1.15f, minVOffset = 0.12f)
    }
    val darkerColor = remember(backgroundColor) {
        backgroundColor.shiftHsv(sFactor = 1.05f, vFactor = 0.88f)
    }
    val depthColor = remember(backgroundColor) {
        backgroundColor.shiftHsv(sFactor = 1.20f, vFactor = 0.62f)
    }

    // Styles for disabled vs enabled states
    val finalLighter = if (enabled) lighterColor else Color(0xFFE5E5E5)
    val finalDarker = if (enabled) darkerColor else Color(0xFFCCCCCC)
    val finalDepth = if (enabled) depthColor else Color(0xFF999999)
    val finalContent = if (enabled) contentColor else Color.LightGray

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
            }
            .padding(bottom = activeDepth)
    ) {
        // 1. Bottom 3D Depth backing layer (gives button depth)
        if (enabled && activeDepth > 0.dp) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = activeDepth)
                    .background(finalDepth, shape)
                    .then(
                        if (border != null) Modifier.border(border, shape) else Modifier
                    )
            )
        }

        // 2. Top Face layer (shifts down to align with depth backing when clicked)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(y = translationY)
                .clip(shape)
                .background(Brush.verticalGradient(listOf(finalLighter, finalDarker)))
                .then(
                    if (border != null) Modifier.border(border, shape) else Modifier
                )
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .drawWithContent {
                    drawContent()
                    // Glossy bubble reflection overlay (top half)
                    if (enabled) {
                        val shineBrush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.45f),
                                Color.White.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = size.height * 0.45f
                        )
                        drawRect(
                            brush = shineBrush,
                            size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.45f)
                        )
                    }
                }
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                ProvideTextStyle(value = MaterialTheme.typography.labelLarge.copy(color = finalContent)) {
                    content()
                }
            }
        }
    }
}

/**
 * A circular companion button designed for icons, back arrows, settings, trash, undo, and redo tools.
 */
@Composable
fun PlayfulIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    contentColor: Color = CountryOutline,
    shape: Shape = CircleShape,
    border: BorderStroke? = BorderStroke(2.5.dp, CountryOutline),
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    PlayfulButton(
        onClick = onClick,
        modifier = modifier,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        shape = shape,
        border = border,
        enabled = enabled
    ) {
        content()
    }
}
