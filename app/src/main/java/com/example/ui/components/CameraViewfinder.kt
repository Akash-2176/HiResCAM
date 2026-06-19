package com.example.ui.components

import android.graphics.BlurMaskFilter
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.*
import com.example.ui.theme.MatteGraphite
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.TactileOrange
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun CameraViewfinder(
    state: CameraState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraDevice by remember { mutableStateOf(false) }
    var useSimulatorFallback by remember { mutableStateOf(true) }

    // Attempt CameraX binding
    LaunchedEffect(state.currentLens) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                // Select camera based on lens state
                val cameraSelector = if (state.currentLens == LensType.MAIN) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    // Try back camera, secondary
                    CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()
                }
                
                // If we got here, check if we can bind
                if (cameraProvider.hasCamera(cameraSelector)) {
                    hasCameraDevice = true
                    useSimulatorFallback = false
                } else {
                    hasCameraDevice = false
                    useSimulatorFallback = true
                }
            } catch (e: Exception) {
                hasCameraDevice = false
                useSimulatorFallback = true
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(
        modifier = modifier
            .testTag("camera_viewfinder_container")
            .background(Color.Black)
            .border(width = 1.dp, color = Color(0xFF333333))
    ) {
        if (!useSimulatorFallback && hasCameraDevice) {
            // Real CameraX Viewfinder
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { previewView ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            cameraProvider.unbindAll()
                            
                            val preview = Preview.Builder().build().also {
                                  it.surfaceProvider = previewView.surfaceProvider
                            }
                            
                            val cameraSelector = if (state.currentLens == LensType.MAIN) {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            } else {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }
                            
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview
                            )
                        } catch (e: Exception) {
                            useSimulatorFallback = true
                        }
                    }, ContextCompat.getMainExecutor(context))
                }
            )
        } else {
            // High-fidelity Simulator fall-back
            CameraSceneSimulator(state = state)
        }

        // Overlay grids, histograms, peaking indicators and zebras
        ViewfinderOverlays(state = state)
    }
}

@Composable
fun CameraSceneSimulator(
    state: CameraState,
    modifier: Modifier = Modifier
) {
    // We animate some noise and color fluctuations based on settings
    val infiniteTransition = rememberInfiniteTransition(label = "simulated_scene")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambient_wave"
    )

    val noiseSeed by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "grain_seed"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Core Tint color base based on WB Mode
        val wbColor = when (state.wbMode) {
            WBMode.AUTO -> Color.White
            WBMode.SUNNY -> Color(0xFFFFD180) // Warm sunset tone
            WBMode.CLOUDY -> Color(0xFFECEFF1) // Balanced gray cool
            WBMode.INCANDESCENT -> Color(0xFFA0C0FF) // Cool heavy blue
            WBMode.FLUORESCENT -> Color(0xFFC0FFEB) // Cyan/green balance
            WBMode.SHADE -> Color(0xFFFF9E80) // Sunset reddish warm
        }

        // 2. Calculated exposure value based on state
        val expDev = state.calculatedExposureDeviation
        val brightnessMultiplier = when {
            state.currentMode == CameraMode.AUTO -> 1f
            else -> Math.pow(2.0, expDev.toDouble()).toFloat().coerceIn(0.1f, 3.5f)
        }

        val baseAmbient = Color(0xFF0F141C).copy(alpha = 1f)
        drawRect(color = baseAmbient)

        // Draw sky / environment gradient
        val skyGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF001F3F).times(brightnessMultiplier).tintWith(wbColor, 0.4f),
                Color(0xFFD84315).times(brightnessMultiplier).tintWith(wbColor, 0.5f)
            ),
            startY = 0f,
            endY = height * 0.7f
        )
        drawRect(brush = skyGradient, size = Size(width, height * 0.7f))

        // Draw a simulated sun or glowing orb representing focus target
        val orbColor = Color(0xFFFFEB3B).times(brightnessMultiplier).tintWith(wbColor, 0.3f)
        val orbRadius = 80.dp.toPx()
        val orbX = width * 0.5f + (sin(waveOffset.toDouble()) * 30).toFloat()
        val orbY = height * 0.35f
        
        // Render Blur level based on focusing accuracy
        // In this simulation, focus value matches around 4.5f for the core orb.
        val focusMismatch = if (state.isManualFocus) {
            Math.abs(state.manualFocusDistance - 4.5f)
        } else {
            0.0f
        }
        val blurLevel = (focusMismatch * 25f).coerceAtLeast(0.5f)

        // Simulated focus blur using custom layers or drawing multiple concentric circles
        if (blurLevel > 1.5f) {
            for (step in 1..8) {
                val alpha = (1.0f / step) * 0.4f
                val radiusExtra = step * blurLevel * 1.5f
                drawCircle(
                    color = orbColor.copy(alpha = alpha),
                    radius = orbRadius + radiusExtra,
                    center = Offset(orbX, orbY)
                )
            }
        } else {
            drawCircle(
                color = orbColor,
                radius = orbRadius,
                center = Offset(orbX, orbY)
            )
            // Glowing core
            drawCircle(
                color = Color.White,
                radius = orbRadius * 0.4f,
                center = Offset(orbX, orbY)
            )
        }

        // 3. Draw mountains/landscape objects to check sharpness
        val horizonY = height * 0.7f
        val mountainPath = Path().apply {
            moveTo(0f, horizonY)
            lineTo(width * 0.2f, height * 0.45f)
            lineTo(width * 0.45f, height * 0.62f)
            lineTo(width * 0.65f, height * 0.38f)
            lineTo(width * 0.85f, height * 0.7f)
            lineTo(width, height * 0.52f)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        val landBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1E293B).times(brightnessMultiplier).tintWith(wbColor, 0.3f),
                Color(0xFF0F172A).times(brightnessMultiplier).tintWith(wbColor, 0.2f)
            ),
            startY = height * 0.45f,
            endY = height
        )
        drawPath(path = mountainPath, brush = landBrush)

        // Draw a simulated foreground tree/flower branch to showcase micro focus details
        val branchY = height * 0.75f
        val focusMismatchBranch = if (state.isManualFocus) {
            Math.abs(state.manualFocusDistance - 1.5f) // Branch is close! Peak focus is at 1.5m
        } else {
            0.0f
        }

        val branchBlur = (focusMismatchBranch * 30f).coerceAtLeast(0.5f)
        val branchColor = Color(0xFF4CAF50).times(brightnessMultiplier).tintWith(wbColor, 0.2f)

        // Draw foreground flower object
        val flowerX = width * 0.25f
        val flowerY = height * 0.8f
        val flowerRadius = 45.dp.toPx()

        if (branchBlur > 2f) {
            for (step in 1..6) {
                val alpha = (1.0f / step) * 0.5f
                val sizeExtra = step * branchBlur * 1.2f
                drawCircle(
                    color = branchColor.copy(alpha = alpha),
                    radius = flowerRadius + sizeExtra,
                    center = Offset(flowerX, flowerY)
                )
            }
        } else {
            // Draw clean focused flower pedals
            drawCircle(
                color = branchColor,
                radius = flowerRadius,
                center = Offset(flowerX, flowerY)
            )
            drawCircle(
                color = Color(0xFFFFC107).times(brightnessMultiplier),
                radius = flowerRadius * 0.4f,
                center = Offset(flowerX, flowerY)
            )
            // Stamens outlines
            drawCircle(
                color = Color.White,
                radius = flowerRadius * 0.2f,
                center = Offset(flowerX, flowerY)
            )
        }

        // 4. ISO Grain Noise Generation (if ISO is high, draw random digital noise)
        if (state.iso > 800) {
            val noiseIntensity = ((state.iso - 800) / 12000f).coerceIn(0f, 0.25f)
            val random = Random((noiseSeed * 1000).toInt())
            for (i in 0..120) {
                val nx = random.nextFloat() * width
                val ny = random.nextFloat() * height
                val ncolor = if (random.nextBoolean()) Color.White else Color.Black
                drawCircle(
                    color = ncolor.copy(alpha = noiseIntensity),
                    radius = random.nextFloat() * 1.5.dp.toPx() + 0.5f,
                    center = Offset(nx, ny)
                )
            }
        }
    }
}

@Composable
fun ViewfinderOverlays(state: CameraState) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // I. Zebra Stripes Overlay (Highlighting overexposed pixels)
        // If simulated exposure level is high, zebra stripes appear in the brightest sky parts.
        val expDev = state.calculatedExposureDeviation
        val brightnessScore = if (state.currentMode == CameraMode.AUTO) 1f else Math.pow(2.0, expDev.toDouble()).toFloat()
        
        if (state.showZebraStripes && brightnessScore > 1.2f) {
            val thresholdFactor = when (state.zebraThreshold) {
                ZebraThreshold.T_95 -> 1.25f
                ZebraThreshold.T_98 -> 1.5f
                ZebraThreshold.T_100 -> 1.8f
            }
            if (brightnessScore >= thresholdFactor) {
                // Draw diagonal stripes in top segment (sky region, overexposed)
                val strokeWidth = 3.dp.toPx()
                val stepSize = 16.dp.toPx()
                var offset = 0f
                while (offset < width) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.8f),
                        start = Offset(offset, 0f),
                        end = Offset((offset - 100.dp.toPx()).coerceAtLeast(0f), height * 0.35f),
                        strokeWidth = strokeWidth
                    )
                    drawLine(
                        color = Color.Black.copy(alpha = 0.8f),
                        start = Offset(offset + 4.dp.toPx(), 0f),
                        end = Offset((offset + 4.dp.toPx() - 100.dp.toPx()).coerceAtLeast(0f), height * 0.35f),
                        strokeWidth = strokeWidth
                    )
                    offset += stepSize
                }
            }
        }

        // II. Focus Peaking Overlay (Highlighting razor-sharp edges)
        // If focus distance is matching we overlay colored sharp contours.
        if (state.showFocusPeaking && state.isManualFocus) {
            val peakingColor = state.peakingColor.color
            
            // Orb Focus (Focus matches around distance = 4.5m)
            val focusMismatchOrb = Math.abs(state.manualFocusDistance - 4.5f)
            if (focusMismatchOrb < 1.0f) {
                val orbX = width * 0.5f
                val orbY = height * 0.35f
                val orbRadius = 80.dp.toPx()
                val alphaMultiplier = (1.0f - focusMismatchOrb).coerceIn(0f, 1f)
                
                // Draw bright edge sparkles
                drawCircle(
                    color = peakingColor.copy(alpha = alphaMultiplier * 0.9f),
                    radius = orbRadius,
                    center = Offset(orbX, orbY),
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                    )
                )
                drawCircle(
                    color = peakingColor.copy(alpha = alphaMultiplier * 0.7f),
                    radius = orbRadius * 0.4f,
                    center = Offset(orbX, orbY),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Target branch focus (Focus matches distance = 1.5m)
            val focusMismatchForeground = Math.abs(state.manualFocusDistance - 1.5f)
            if (focusMismatchForeground < 0.8f) {
                val flowerX = width * 0.25f
                val flowerY = height * 0.8f
                val flowerRadius = 45.dp.toPx()
                val alphaMultiplier = (1.0f - focusMismatchForeground / 0.8f).coerceIn(0f, 1f)
                
                drawCircle(
                    color = peakingColor.copy(alpha = alphaMultiplier * 0.9f),
                    radius = flowerRadius,
                    center = Offset(flowerX, flowerY),
                    style = Stroke(width = 3.0.dp.toPx())
                )
            }
        }

        // III. Grid Overlay Systems
        when (state.gridType) {
            GridType.NONE -> {}
            GridType.RULE_OF_THIRDS -> {
                val strokeColor = Color.White.copy(alpha = 0.4f)
                val strokeW = 1.dp.toPx()
                // Verticals
                drawLine(strokeColor, Offset(width * 0.333f, 0f), Offset(width * 0.333f, height), strokeW)
                drawLine(strokeColor, Offset(width * 0.666f, 0f), Offset(width * 0.666f, height), strokeW)
                // Horizontals
                drawLine(strokeColor, Offset(0f, height * 0.333f), Offset(width, height * 0.333f), strokeW)
                drawLine(strokeColor, Offset(0f, height * 0.666f), Offset(width, height * 0.666f), strokeW)
            }
            GridType.GOLDEN_RATIO -> {
                val strokeColor = Color.White.copy(alpha = 0.4f)
                val strokeW = 1.dp.toPx()
                // Phi averages: approx 0.382 and 0.618
                // Verticals
                drawLine(strokeColor, Offset(width * 0.382f, 0f), Offset(width * 0.382f, height), strokeW)
                drawLine(strokeColor, Offset(width * 0.618f, 0f), Offset(width * 0.618f, height), strokeW)
                // Horizontals
                drawLine(strokeColor, Offset(0f, height * 0.382f), Offset(width, height * 0.382f), strokeW)
                drawLine(strokeColor, Offset(0f, height * 0.618f), Offset(width, height * 0.618f), strokeW)
            }
            GridType.SQUARE -> {
                val strokeColor = Color.White.copy(alpha = 0.5f)
                val squareWidth = height
                val left = (width - squareWidth) / 2f
                val right = left + squareWidth
                // Draw side dimmers
                drawRect(Color.Black.copy(alpha = 0.6f), Offset(0f, 0f), Size(left, height))
                drawRect(Color.Black.copy(alpha = 0.6f), Offset(right, 0f), Size(width - right, height))
                // Draw crop boundaries
                drawLine(strokeColor, Offset(left, 0f), Offset(left, height), 2.0.dp.toPx())
                drawLine(strokeColor, Offset(right, 0f), Offset(right, height), 2.0.dp.toPx())
            }
            GridType.CENTER_CROSS -> {
                val strokeColor = Color.White.copy(alpha = 0.6f)
                val len = 20.dp.toPx()
                val cx = width / 2f
                val cy = height / 2f
                drawLine(strokeColor, Offset(cx - len, cy), Offset(cx + len, cy), 2.0.dp.toPx())
                drawLine(strokeColor, Offset(cx, cy - len), Offset(cx, cy + len), 2.0.dp.toPx())
            }
        }

        // IV. RAW / High resolution Badge Overlay (DSLR Viewfinder style metadata indicators)
        val metaStyle = TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        
        // Active metadata tags
        val tagsList = mutableListOf<String>()
        if (state.rawMode != RAWMode.JPEG) tagsList.add("[RAW: DNG]")
        if (state.currentMode == CameraMode.HR) tagsList.add("[50M SUPER-RES]")
        if (state.bracketingMode != BracketingMode.OFF) tagsList.add("[AEB: ${state.bracketingMode.shots}F]")
        if (state.isManualFocus) tagsList.add("[MF: ${String.format("%.1fm", state.manualFocusDistance)}]") else tagsList.add("[AF-C]")
        
        var startX = 16.dp.toPx()
        tagsList.forEach { tag ->
            drawText(
                textMeasurer = textMeasurer,
                text = tag,
                style = metaStyle,
                topLeft = Offset(startX, 12.dp.toPx())
            )
            val measurement = textMeasurer.measure(tag, metaStyle)
            startX += measurement.size.width + 12.dp.toPx()
        }
    }

    // Active Processing Panel
    AnimatedVisibility(
        visible = state.captureProgress >= 0f,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "GTCAM PIPELINE ENGAGED",
                    color = TactileOrange,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                CircularProgressIndicator(
                    progress = { state.captureProgress },
                    color = TactileOrange,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = state.captureStageString.uppercase(),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Syncing multi-frame Sony IMX896 sub-pixels to OIS matrix...",
                    color = SlateTextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// Inline extension to multiply colors representing exposure change
private operator fun Color.times(factor: Float): Color {
    return Color(
        red = (this.red * factor).coerceIn(0f, 1f),
        green = (this.green * factor).coerceIn(0f, 1f),
        blue = (this.blue * factor).coerceIn(0f, 1f),
        alpha = this.alpha
    )
}

// Blend target color towards another tint color
private fun Color.tintWith(tint: Color, amount: Float): Color {
    return Color(
        red = (this.red * (1f - amount) + tint.red * amount).coerceIn(0f, 1f),
        green = (this.green * (1f - amount) + tint.green * amount).coerceIn(0f, 1f),
        blue = (this.blue * (1f - amount) + tint.blue * amount).coerceIn(0f, 1f),
        alpha = this.alpha
    )
}
