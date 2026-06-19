package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MatteGraphite
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.TactileOrange

data class CompTech(
    val title: String,
    val description: String,
    val camera2Implementation: String,
    val feasibilityScore: String,
    val beforeDesc: String,
    val afterDesc: String,
    val beforeColor: Color,
    val afterColor: Color,
    val beforeSky: Color,
    val afterSky: Color
)

@Composable
fun ComputationalPhotographySuite(
    modifier: Modifier = Modifier
) {
    val technologies = remember {
        listOf(
            CompTech(
                "1. Smart HDR Mode",
                "High Dynamic Range reconstruction capturing rapid exposure brackets.",
                "Realized via Camera2 CaptureSession bursting multiple requests with CameraMetadata.CONTROL_AE_MODE_OFF. Shutter times are changed incrementally (e.g. 1/1000s, 1/250s, 1/60s). Stretched frames are aligned on GPU with pyramidal optical flow and merged.",
                "Fully Feasible in Android 16 via customized camera extension sessions.",
                "Harsh contrast: Silhouetted background hills, absolute blown sky highlights.",
                "Rich recovery: Delicate warm sunset cloud detail, illuminated dark mountain textures.",
                beforeColor = Color(0xFF0C1014), afterColor = Color(0xFF3E4F5B),
                beforeSky = Color(0xFFF9A825), afterSky = Color(0xFFF4511E)
            ),
            CompTech(
                "2. Multi-Frame Night Sight",
                "Temporal noise stacking to gather optimal light in low environments.",
                "Configures Camera2 RAW_SENSOR / YUV_420_888 streams. Stacks 8-15 frames. Temporal median pixel filtering is applied to cancel static sensor grain without losing high sharpness. Leverages Realme IMX896 OIS stability.",
                "Feasible. Hard limitations exist on static handshake motions.",
                "Noisy void: Grainy purple chromatic sensor noise, muddy pixel contours.",
                "Silent clarity: Squeaky clean, noise-reduced moon surface, high detail shadows.",
                beforeColor = Color(0xFF030712), afterColor = Color(0xFF1E293B),
                beforeSky = Color(0xFF1F1A24), afterSky = Color(0xFF020617)
            ),
            CompTech(
                "3. Super Resolution (HR)",
                "Merges sub-pixel shifts from minute hand tremor into sharp outputs.",
                "Uses natural hand-tremor or micro-OIS lens shifts. Captures a fast burst of 10 primary 12MP shots. Aligned at sub-pixel levels. Interstitials are Lanczos-interpolated, merging to synthesize a sharp 50MP equivalent detail matrix.",
                "Feasible. Demands intensive GPU/NPU multi-threading.",
                "Soft blocky zoom: Blurry leaf edges, stair-stepped jagged stems.",
                "Razor sharpness: Single fine leaf veins, perfectly crisp petal margins.",
                beforeColor = Color(0xFF1B5E20), afterColor = Color(0xFF2E7D32),
                beforeSky = Color(0xFF00B0FF), afterSky = Color(0xFF00E5FF)
            ),
            CompTech(
                "4. Portrait Depth Segmenter",
                "Generates premium aesthetic physical bokeh via deep segmentation.",
                "Utilizes dual stream arrays or ML models to separate subject depth values. Hair is segregated into transparent edge alpha-masks. Background layers are convolved using varying circle-of-confusion Gaussian kernel blurs.",
                "Highly Feasible using Android ML frameworks & Depth APIs.",
                "Flat profile: Foreground flower, distant mountain mountains are equally sharp.",
                "True DOF: Razor-edged subject pop-out, gorgeous buttery background bokeh circles.",
                beforeColor = Color(0xFF4E342E), afterColor = Color(0xFF5D4037),
                beforeSky = Color(0xFFEF9A9A), afterSky = Color(0xFFFFEB3B)
            ),
            CompTech(
                "5. Motion Blur Compensator",
                "Calculates motion vectors to recover crisp details from shaky actions.",
                "Monitors real-time IMX896 OIS gyroscope metrics alongside optical flow algorithms. Calculates directional blur kernels and applies Fast Fourier Deconvolution filters on GPU to restore sharp edges.",
                "Moderately Feasible. Highly memory and compute-demanding.",
                "Shaky smear: Double boundaries on mountain lines, blurry horizon curves.",
                "Frozen moment: Instantly locked horizon peaks, high-definition stone edges.",
                beforeColor = Color(0xFF37474F), afterColor = Color(0xFF455A64),
                beforeSky = Color(0xFF90CAF9), afterSky = Color(0xFFE3F2FD)
            ),
            CompTech(
                "6. Intelligent Shadow Recovery",
                "Selectively amplifies dark regions without inflating noise curves.",
                "Maps local tone-curves using 2D luminance grids. Amplifies gains dynamically in underexposed spatial blocks. Compiles a recursive noise filter specifically tailored to the Sony IMX896 1.0μm pixel size parameters.",
                "Highly Feasible. Fully achievable on real-time preview.",
                "Crushed black: Mountains are completely blacked out, no texture visible.",
                "Decoded detail: Rich stone ridges and moss textures visible under soft sunlight.",
                beforeColor = Color(0xFF0E0E10), afterColor = Color(0xFF2C3E50),
                beforeSky = Color(0xFFFFB74D), afterSky = Color(0xFFFFCC80)
            ),
            CompTech(
                "7. Highlight recovery",
                "Restores native highlights from RAW saturation limits.",
                "Reads RAW_SENSOR white-point saturation thresholds before JPEG compression clip. Reconstructs missing color channels (Red/Blue clipping) using adjacent non-saturated Green values.",
                "Feasible with RAW workflow. Overexposed areas are restored seamlessly.",
                "Burned out: Blown white sky void surrounding the sun orb.",
                "Dynamic sky: Smooth glowing yellow gradients and blue atmospheric details.",
                beforeColor = Color(0xFF5D4037), afterColor = Color(0xFF8D6E63),
                beforeSky = Color(0xFFFFFFFF), afterSky = Color(0xFFFFE082)
            ),
            CompTech(
                "8. Noise reduction leveler",
                "Balanced spatial noise reduction preserving micro textures.",
                "Uses bilateral filter passes. Segregates high-frequency edges from flat homogeneous zones. Performs adaptive chroma denoising while protecting fine texture detail.",
                "Highly Feasible. Configured via Camera2 capture flags.",
                "Grainy static: Sandy digital noise speckles distorting clean surfaces.",
                "Clean textures: Solid smooth blue sky transitions and sharp mountain ridges.",
                beforeColor = Color(0xFF212121), afterColor = Color(0xFF424242),
                beforeSky = Color(0xFFCFD8DC), afterSky = Color(0xFFECEFF1)
            )
        )
    }

    var selectedTech by remember { mutableStateOf(technologies[0]) }

    Row(
        modifier = modifier
            .testTag("computational_photography_panel")
            .fillMaxSize()
            .background(Color(0xFF0B0C0E))
    ) {
        // Left column - Scrollable technologies list
        LazyColumn(
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight()
                .border(width = 1.dp, color = Color(0xFF1E2125))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "COMPUTATIONAL PIPELINE",
                    color = TactileOrange,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }

            items(technologies) { tech ->
                val isSelected = selectedTech.title == tech.title
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF1C1D21) else Color(0xFF111215)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedTech = tech }
                        .border(
                            1.dp,
                            if (isSelected) TactileOrange else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tech.title,
                                color = if (isSelected) TactileOrange else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tech.description,
                                color = SlateTextSecondary,
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = if (isSelected) TactileOrange else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Right column - Detail and split-screen compare slider preview!
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(12.dp)
        ) {
            Text(
                text = selectedTech.title.uppercase(),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = selectedTech.description,
                color = SlateTextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Real-time camera2 architectural details
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF14161A), RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = "SONY IMX896 CAMERA2 PIPELINE ARCHITECTURE:",
                        color = TactileOrange,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = selectedTech.camera2Implementation,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Android 16 API Feasibility: ${selectedTech.feasibilityScore}",
                        color = Color(0xFF00E676),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Split screen visual slider comparison
            Text(
                text = "INTERACTIVE BEFORE vs AFTER PROCESSOR MODELLING",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF2C3E50), RoundedCornerShape(6.dp))
            ) {
                var sliderRatio by remember { mutableStateOf(0.5f) }

                // Canvas scene representing Before and After states side by side
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                sliderRatio = (sliderRatio + dragAmount.x / size.width).coerceIn(0.05f, 0.95f)
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val splitX = width * sliderRatio

                    // DRAW BEFORE SIDE (Left)
                    // Sky gradient BEFORE
                    val skyBefore = Brush.verticalGradient(
                        colors = listOf(Color(0xFF011627), selectedTech.beforeSky),
                        startY = 0f,
                        endY = height * 0.6f
                    )
                    drawRect(brush = skyBefore, size = Size(splitX, height))

                    // Peak mountain BEFORE
                    val peakBefore = Path().apply {
                        moveTo(0f, height * 0.8f)
                        lineTo(width * 0.35f, height * 0.4f)
                        lineTo(splitX, height * 0.72f)
                        lineTo(splitX, height)
                        lineTo(0f, height)
                        close()
                    }
                    drawPath(path = peakBefore, color = selectedTech.beforeColor)

                    // DRAW AFTER SIDE (Right)
                    // Sky gradient AFTER
                    val skyAfter = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0B1428), selectedTech.afterSky),
                        startY = 0f,
                        endY = height * 0.6f
                    )
                    drawRect(
                        brush = skyAfter,
                        topLeft = Offset(splitX, 0f),
                        size = Size(width - splitX, height)
                    )

                    // Peak mountain AFTER
                    val peakAfter = Path().apply {
                        moveTo(splitX, height * 0.72f)
                        lineTo(width * 0.35f, height * 0.4f)
                        lineTo(width * 0.7f, height * 0.85f)
                        lineTo(width, height * 0.55f)
                        lineTo(width, height)
                        lineTo(splitX, height)
                        close()
                    }
                    drawPath(path = peakAfter, color = selectedTech.afterColor)

                    // Draw separator handle line
                    drawLine(
                        color = TactileOrange,
                        start = Offset(splitX, 0f),
                        end = Offset(splitX, height),
                        strokeWidth = 2.dp.toPx()
                    )
                    
                    // Small circular slider knob grip
                    drawCircle(
                        color = Color.White,
                        radius = 8.dp.toPx(),
                        center = Offset(splitX, height / 2f)
                    )
                    drawCircle(
                        color = TactileOrange,
                        radius = 4.dp.toPx(),
                        center = Offset(splitX, height / 2f)
                    )
                }

                // Header Info Badges
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(text = "RAW SENSOR (UNPROCESSED)", color = Color.Red, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(text = "GTCAM STACKED PIPELINE (ACTIVE)", color = Color(0xFF00E676), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }

                // Interactive info text
                Text(
                    text = "Drag center slider left/right",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(8.dp)
                )
            }
        }
    }
}
