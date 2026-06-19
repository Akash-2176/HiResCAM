package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CameraMode
import com.example.ui.theme.MatteGraphite
import com.example.ui.theme.TactileOrange
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DslrModeDial(
    selectedMode: CameraMode,
    onModeChange: (CameraMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = CameraMode.values()
    val selectedIndex = modes.indexOf(selectedMode)

    // Smooth rotation angle animation based on selection index
    // Each mode takes up 36 degrees on our physical virtual wheel (total offset is dynamic)
    val spacingDegrees = 36f
    val targetAngle = -selectedIndex * spacingDegrees
    val animatedAngle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.75f),
        label = "dial_rotation"
    )

    Column(
        modifier = modifier
            .testTag("dslr_mode_dial_container")
            .width(100.dp)
            .fillMaxHeight()
            .background(Color(0xFF0F1012))
            .pointerInput(Unit) {
                var dragAccumulator = 0f
                detectDragGestures(
                    onDragStart = { dragAccumulator = 0f },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumulator += dragAmount.y
                        if (dragAccumulator > 80f) {
                            val nextIndex = (selectedIndex - 1).coerceAtLeast(0)
                            onModeChange(modes[nextIndex])
                            dragAccumulator = 0f
                        } else if (dragAccumulator < -80f) {
                            val nextIndex = (selectedIndex + 1).coerceAtMost(modes.size - 1)
                            onModeChange(modes[nextIndex])
                            dragAccumulator = 0f
                        }
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Label header for Dial System
        Text(
            text = "MODE DIAL",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Box(
            modifier = Modifier
                .size(110.dp)
                .graphicsLayer {
                    // Let the dial extend slightly off the side screen or float elegantly
                    translationX = 14.dp.toPx()
                },
            contentAlignment = Alignment.Center
        ) {
            // Draw Circular Dial Base (Tactile, knurled metal border with notch ticks)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f

                // Outer knurled physical ring shadow and ridges
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF232529), Color(0xFF141517)),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )
                // Draw metal tactile ridges (notches) every 10 degrees to feel like a real lens dial
                for (angleDeg in 0..360 step 8) {
                    val angleRad = Math.toRadians(angleDeg.toDouble())
                    val innerR = radius - 8.dp.toPx()
                    val outerR = radius
                    drawLine(
                        color = Color(0xFF37474F),
                        start = Offset(
                            (center.x + innerR * cos(angleRad)).toFloat(),
                            (center.y + innerR * sin(angleRad)).toFloat()
                        ),
                        end = Offset(
                            (center.x + outerR * cos(angleRad)).toFloat(),
                            (center.y + outerR * sin(angleRad)).toFloat()
                        ),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Inner core dial cover
                drawCircle(
                    color = Color(0xFF0A0B0C),
                    radius = radius - 10.dp.toPx(),
                    center = center
                )

                // Highlit orange arrow indicator matching current point orientation
                val p = Path().apply {
                    moveTo(0f, center.y)
                    lineTo(12.dp.toPx(), center.y - 7.dp.toPx())
                    lineTo(12.dp.toPx(), center.y + 7.dp.toPx())
                    close()
                }
                drawPath(p, color = TactileOrange)
            }

            // Draw individual mode symbols on rotating dial face overlay
            modes.forEachIndexed { index, mode ->
                // Calculate mode text placement angle on the circumference
                val relativeAngle = (index * spacingDegrees) + animatedAngle - 180f
                val activeModeRadius = 34.dp

                val angleRad = Math.toRadians(relativeAngle.toDouble())
                val labelX = (activeModeRadius.value * cos(angleRad)).toFloat()
                val labelY = (activeModeRadius.value * sin(angleRad)).toFloat()

                // Highlight active mode color
                val isActive = selectedMode == mode
                val labelColor = if (isActive) TactileOrange else Color.White.copy(alpha = 0.5f)
                val weight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold
                val fontSize = if (isActive) 12.sp else 10.sp

                Box(
                    modifier = Modifier
                        .offset(x = labelX.dp, y = labelY.dp)
                        .clip(CircleShape)
                        .clickable { onModeChange(mode) }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.label,
                        color = labelColor,
                        fontSize = fontSize,
                        fontWeight = weight,
                        letterSpacing = (-0.5).sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Mode Indicator Text
        Text(
            text = selectedMode.shortDesc,
            color = TactileOrange,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
