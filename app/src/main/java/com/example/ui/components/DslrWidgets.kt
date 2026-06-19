package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.*
import com.example.ui.theme.ActiveYellow
import com.example.ui.theme.FluorescentGreen
import com.example.ui.theme.FocusRed
import com.example.ui.theme.TactileOrange
import kotlin.math.sin

@Composable
fun CameraExposureMeter(
    state: CameraState,
    modifier: Modifier = Modifier
) {
    val deviation = state.calculatedExposureDeviation
    val animatedDeviation by animateFloatAsState(targetValue = deviation, label = "needle_ev")

    Column(
        modifier = modifier
            .testTag("exposure_meter_container")
            .fillMaxWidth()
            .background(Color(0xFF101214))
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // DSLR Style Index Tick Indicator
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .padding(horizontal = 24.dp)
        ) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f
            
            // Draw axis line
            drawLine(
                color = Color.White.copy(alpha = 0.25f),
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 1.dp.toPx()
            )

            // Draw notches from -3 to +3 (7 primary points, subnotch ticks in between)
            val totalMajorPoints = 7
            val spacing = width / (totalMajorPoints - 1)

            for (i in 0 until totalMajorPoints) {
                val x = i * spacing
                val value = i - 3
                
                // Draw main notch
                drawLine(
                    color = Color.White.copy(alpha = 0.8f),
                    start = Offset(x, centerY - 6.dp.toPx()),
                    end = Offset(x, centerY + 6.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )

                // Subnotches (2 inside each main segment)
                if (i < totalMajorPoints - 1) {
                    val sub1 = x + spacing / 3f
                    val sub2 = x + 2f * spacing / 3f
                    drawLine(
                        color = Color.White.copy(alpha = 0.4f),
                        start = Offset(sub1, centerY - 3.dp.toPx()),
                        end = Offset(sub1, centerY + 3.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.4f),
                        start = Offset(sub2, centerY - 3.dp.toPx()),
                        end = Offset(sub2, centerY + 3.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }

            // Draw current active deviation indicator needle
            // Map animatedDeviation (-3..3) to 0f..width
            val progressPercent = (animatedDeviation + 3f) / 6f
            val needleX = progressPercent * width
            
            // Needle caret
            val path = Path().apply {
                moveTo(needleX, centerY - 8.dp.toPx())
                lineTo(needleX - 5.dp.toPx(), centerY + 8.dp.toPx())
                lineTo(needleX + 5.dp.toPx(), centerY + 8.dp.toPx())
                close()
            }
            drawPath(path = path, color = TactileOrange)
            
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = Offset(needleX, centerY)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "-3", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
            Text(text = "-2", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
            Text(text = "-1", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
            Text(
                text = "0  ev",
                color = if (Math.abs(deviation) < 0.15f) FluorescentGreen else Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = "+1", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
            Text(text = "+2", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
            Text(text = "+3", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
        }
    }
}

@Composable
fun CameraHistogram(
    state: CameraState,
    modifier: Modifier = Modifier
) {
    // Generate simulated dynamic histogram channels based on state inputs
    val histogramBars = remember(state.iso, state.shutterSpeed, state.wbMode, state.calculatedExposureDeviation) {
        val size = 50
        val exp = state.calculatedExposureDeviation
        val wb = state.wbMode

        // Calculate offset shift in graph peaks depending on exposure
        val centerIndex = (25 + exp * 6).toInt().coerceIn(4, 45)
        
        // Red channel leans warmer (high shift if WB sunny/shade)
        val rShift = when (wb) {
            WBMode.SUNNY, WBMode.SHADE -> 4
            WBMode.INCANDESCENT -> -5
            else -> 0
        }
        val bShift = when (wb) {
            WBMode.INCANDESCENT -> 6
            WBMode.SUNNY, WBMode.SHADE -> -4
            else -> 0
        }

        val luminance = FloatArray(size)
        val red = FloatArray(size)
        val green = FloatArray(size)
        val blue = FloatArray(size)

        for (i in 0 until size) {
            // Basic bell-curve distribution formulas for photographic luminance peaks
            val distL = Math.exp(-Math.pow((i - centerIndex).toDouble(), 2.0) / 95.0)
            val distR = Math.exp(-Math.pow((i - (centerIndex + rShift)).toDouble(), 2.0) / 80.0)
            val distG = Math.exp(-Math.pow((i - centerIndex).toDouble(), 2.0) / 100.0)
            val distB = Math.exp(-Math.pow((i - (centerIndex + bShift)).toDouble(), 2.0) / 75.0)

            // Random slight ambient variance representing camera noise/vibrancy
            val randomNoise = sin(i.toDouble() * 0.4) * 0.04

            luminance[i] = (distL * 0.85 + 0.05 + randomNoise).toFloat().coerceIn(0.01f, 1f)
            red[i] = (distR * 0.70 + 0.04 + randomNoise).toFloat().coerceIn(0.01f, 1f)
            green[i] = (distG * 0.82 + 0.03 + randomNoise).toFloat().coerceIn(0.01f, 1f)
            blue[i] = (distB * 0.65 + 0.05 + randomNoise).toFloat().coerceIn(0.01f, 1f)
        }
        Triple(luminance, red, Triple(green, blue, null))
    }

    Box(
        modifier = modifier
            .testTag("histogram_overlay")
            .size(width = 125.dp, height = 75.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(2.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val barsCount = 50
            val barW = width / barsCount

            val (lum, r, gAndB) = histogramBars
            val (g, b, _) = gAndB

            // Draw RGB histograms using translucent brushes
            for (i in 0 until barsCount) {
                val x = i * barW
                
                // Red Channel
                val rHeight = r[i] * height * 0.8f
                drawRect(
                    color = Color.Red.copy(alpha = 0.25f),
                    topLeft = Offset(x, height - rHeight),
                    size = Size(barW - 1f, rHeight)
                )

                // Green Channel
                val gHeight = g[i] * height * 0.8f
                drawRect(
                    color = Color.Green.copy(alpha = 0.22f),
                    topLeft = Offset(x, height - gHeight),
                    size = Size(barW - 1f, gHeight)
                )

                // Blue Channel
                val bHeight = b[i] * height * 0.8f
                drawRect(
                    color = Color.Blue.copy(alpha = 0.25f),
                    topLeft = Offset(x, height - bHeight),
                    size = Size(barW - 1f, bHeight)
                )

                // Luminance line overlay
                val lumHeight = lum[i] * height * 0.85f
                drawRect(
                    color = Color.White.copy(alpha = 0.6f),
                    topLeft = Offset(x, height - lumHeight),
                    size = Size(barW - 1f, lumHeight)
                )
            }
        }

        // Channels label indicators
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(modifier = Modifier.size(6.dp).background(Color.Red, RoundedCornerShape(1.dp)))
            Box(modifier = Modifier.size(6.dp).background(Color.Green, RoundedCornerShape(1.dp)))
            Box(modifier = Modifier.size(6.dp).background(Color.Blue, RoundedCornerShape(1.dp)))
            Box(modifier = Modifier.size(6.dp).background(Color.White, RoundedCornerShape(1.dp)))
        }
    }
}

@Composable
fun ManualFocusScaleSlider(
    state: CameraState,
    modifier: Modifier = Modifier
) {
    val distances = listOf("0.1m", "0.3m", "1.0m", "3.0m", "10.0m", "INF")

    Column(
        modifier = modifier
            .testTag("focus_slider_container")
            .fillMaxWidth()
            .background(Color(0xFF16181A))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "MF SEGMENTS Distance scale", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            val activeDistanceLabel = when {
                state.manualFocusDistance <= 1.5f -> "Macro Closeup (${String.format("%.1fm", state.manualFocusDistance)})"
                state.manualFocusDistance <= 4.0f -> "Mid-range portrait focus (${String.format("%.1fm", state.manualFocusDistance)})"
                state.manualFocusDistance >= 9.0f -> "Landscape (Infinity LOCK)"
                else -> "Standard distance (${String.format("%.1fm", state.manualFocusDistance)})"
            }
            Text(
                text = activeDistanceLabel,
                color = TactileOrange,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Slider(
            value = state.manualFocusDistance,
            onValueChange = { state.manualFocusDistance = it },
            valueRange = 0.1f..10.0f,
            colors = SliderDefaults.colors(
                thumbColor = TactileOrange,
                activeTrackColor = TactileOrange,
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier
                .height(28.dp)
                .fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            distances.forEachIndexed { i, label ->
                val associatedValue = when (i) {
                    0 -> 0.1f
                    1 -> 0.3f
                    2 -> 1.0f
                    3 -> 3.0f
                    4 -> 10.0f
                    else -> 10.0f
                }
                Text(
                    text = label,
                    color = if (Math.abs(state.manualFocusDistance - associatedValue) < 1.0f) TactileOrange else Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { state.manualFocusDistance = associatedValue }
                )
            }
        }
    }
}
