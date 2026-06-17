package com.example.colorit.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colorit.ui.theme.CozyBlush
import com.example.colorit.ui.theme.CozyRose
import com.example.colorit.ui.theme.TextDark
import com.example.colorit.ui.theme.CountryOutline
import com.example.colorit.ui.theme.CardYellow

@Composable
fun ColorSpectrumDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var hsv by remember {
        val hsvArray = FloatArray(3)
        android.graphics.Color.colorToHSV(initialColor.toArgb(), hsvArray)
        mutableStateOf(hsvArray)
    }

    val selectedColor = Color(android.graphics.Color.HSVToColor(hsv))

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.border(3.dp, CountryOutline, MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        containerColor = CardYellow,
        titleContentColor = TextDark,
        textContentColor = TextDark.copy(alpha = 0.8f),
        title = {
            Text("Select Custom Color 🌈", fontWeight = FontWeight.ExtraBold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Color Preview Box
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(selectedColor)
                        .border(3.dp, CozyBlush.copy(alpha = 0.6f), MaterialTheme.shapes.large)
                        .shadow(elevation = 2.dp, shape = MaterialTheme.shapes.large)
                )

                // Sliders
                ColorSpectrumSlider(
                    label = "Hue (Color Type)",
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
                    ),
                    valueFraction = hsv[0] / 360f,
                    onFractionChanged = { fraction ->
                        val newHsv = hsv.clone()
                        newHsv[0] = fraction * 360f
                        hsv = newHsv
                    }
                )

                val satBaseColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], 1f, 1f)))
                ColorSpectrumSlider(
                    label = "Saturation (Intensity)",
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.White, satBaseColor)
                    ),
                    valueFraction = hsv[1],
                    onFractionChanged = { fraction ->
                        val newHsv = hsv.clone()
                        newHsv[1] = fraction
                        hsv = newHsv
                    }
                )

                val valBaseColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], 1f)))
                ColorSpectrumSlider(
                    label = "Brightness",
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Black, valBaseColor)
                    ),
                    valueFraction = hsv[2],
                    onFractionChanged = { fraction ->
                        val newHsv = hsv.clone()
                        newHsv[2] = fraction
                        hsv = newHsv
                    }
                )
            }
        },
        confirmButton = {
            PlayfulButton(
                onClick = {
                    onColorSelected(selectedColor)
                    onDismiss()
                },
                backgroundColor = CozyRose
            ) {
                Text("Select", color = Color.White)
            }
        },
        dismissButton = {
            PlayfulButton(
                onClick = onDismiss,
                backgroundColor = Color.LightGray.copy(alpha = 0.3f),
                contentColor = TextDark
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ColorSpectrumSlider(
    label: String,
    brush: Brush,
    valueFraction: Float,
    onFractionChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextDark, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(CircleShape)
                .background(brush)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        onFractionChanged(fraction)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                        onFractionChanged(fraction)
                    }
                }
        ) {
            // Draw thumb indicator
            Canvas(modifier = Modifier.fillMaxSize()) {
                val x = valueFraction * size.width
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = Offset(x, size.height / 2),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = Color.Black.copy(alpha = 0.4f),
                    radius = 9.dp.toPx(),
                    center = Offset(x, size.height / 2),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )
            }
        }
    }
}
