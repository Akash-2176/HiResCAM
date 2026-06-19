package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.ui.*
import com.example.ui.components.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TactileOrange
import com.example.ui.theme.VolcanicBlack
import com.example.ui.theme.FluorescentGreen
import com.example.ui.theme.SlateTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Define GTCam State Management
        val cameraState = CameraState()
        
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0.dp) // Edge to edge full screen
                ) { innerPadding ->
                    GTCamAppScreen(
                        state = cameraState,
                        onCaptureProgressSubmit = { updateProgress -> 
                            // Update capture metrics
                        },
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun GTCamAppScreen(
    state: CameraState,
    onCaptureProgressSubmit: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSettingsOpen by remember { mutableStateOf(false) }
    var isCompSuiteOpen by remember { mutableStateOf(false) }
    var isGalleryOpen by remember { mutableStateOf(false) }
    
    // Virtual photo storage buffer (EXIF matched)
    val photoGallery = remember {
        mutableStateListOf(
            GalleryItem(
                id = "photo_1",
                title = "GTCAM_SUNSET_STACK_7T.DNG",
                mode = "P (Program AE)",
                iso = "ISO 100",
                shutter = "1/1000 sec",
                format = "RAW + JPEG",
                resolution = "50.0 MP (8160 x 6120 Pixels)",
                baseGradientColors = listOf(Color(0xFF0F1524), Color(0xFFD32F2F), Color(0xFFFBC02D))
            ),
            GalleryItem(
                id = "photo_2",
                title = "GTCAM_MACRO_FLOWER_MF.JPG",
                mode = "M (Manual Mode)",
                iso = "ISO 200",
                shutter = "1/250 sec",
                format = "JPEG Only",
                resolution = "12.2 MP (4032 x 3024 Pixels)",
                baseGradientColors = listOf(Color(0xFF0F2C18), Color(0xFF388E3C), Color(0xFF81C784))
            ),
            GalleryItem(
                id = "photo_3",
                title = "GTCAM_NIGHT_CATHEDRAL_L.DNG",
                mode = "Super Night Stacking (12F)",
                iso = "ISO 1600",
                shutter = "5.0 sec (Stack)",
                format = "DNG RAW",
                resolution = "12.2 MP (4032 x 3024 Pixels)",
                baseGradientColors = listOf(Color(0xFF020410), Color(0xFF111E39), Color(0xFF37474F))
            )
        )
    }

    var successNoticeString by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Quick system clock indicator to feel like a modern camera HUD
    var currentTime by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val formatter = SimpleDateFormat("HH:mm:ss", Locale.US)
            currentTime = formatter.format(Date())
            delay(1000)
        }
    }

    Box(
        modifier = modifier
            .testTag("app_root_screen")
            .background(VolcanicBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // 1. TOP HEADER STATUS BAR (DSLR Viewfinder Metadata style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(Color(0xFF0B0C0E))
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Device Brand & OIS / Core Status indicators
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(TactileOrange)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(text = " GTCAM PRO ", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Text(
                        text = "REALME GT 7T • COMP_CORE v1.5",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Connected lens state
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .border(1.dp, Color(0xFF00E676), RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "SONY IMX896 CORE: ACTIVE",
                            color = Color(0xFF00E676),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Camera active values (File Format, Battery, Clock)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "AWB ON", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(text = if (state.rawMode == RAWMode.JPEG) "JPEG" else "RAW (DNG)", color = TactileOrange, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(text = "BAT 98%", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(text = currentTime, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                }
            }

            // 2. MAIN GRID (Left Viewport + Extras, Right Controls)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // LEFT COLUMN (70% WIDTH) - Viewfinder + Exposure bar + MF sliders
                Column(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxHeight()
                        .padding(start = 10.dp, top = 6.dp, bottom = 6.dp, end = 6.dp)
                ) {
                    // Viewfinder Screen
                    CameraViewfinder(
                        state = state,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // DSLR style live exposure balance notches meter
                    CameraExposureMeter(state = state)

                    // Manual focus Vernier distance scales (Appear dynamically only in MF mode)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick Switch for Focus Mode (AF-C to Manual Focus Slider)
                        Button(
                            onClick = { state.isManualFocus = !state.isManualFocus },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.isManualFocus) TactileOrange else Color(0xFF1E2125)
                            ),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .padding(end = 6.dp)
                        ) {
                            Text(
                                text = if (state.isManualFocus) "MF ACTIVE" else "AF-C AUTO",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.isManualFocus) Color.Black else Color.White
                            )
                        }

                        if (state.isManualFocus) {
                            ManualFocusScaleSlider(
                                state = state,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .background(Color(0xFF131518), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color(0xFF1D2125), RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "TAP 'AF-C AUTO' TO ENGAGE MANUAL FOCUS SCALE SLIDER (0.1m - INFINITY)",
                                    color = Color.White.copy(alpha = 0.35f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                // RIGHT COLUMN (30% WIDTH) - Tactile Controls and Mode Wheels
                Column(
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF0F1012))
                        .border(width = 1.dp, color = Color(0xFF1E2125))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    
                    // A. Live Histogram Widget Overlay (Compact size)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "LIVE HISTOGRAM (RGB)", color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF263238))
                                    .clickable { state.showHistogram = !state.showHistogram }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (state.showHistogram) "HIDE OVERLAY" else "SHOW OVERLAY",
                                    color = TactileOrange,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (state.showHistogram) {
                            CameraHistogram(state = state, modifier = Modifier.fillMaxWidth())
                        }
                    }

                    // B. Settings + Compare Suite Quick Launcher Options
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Hardware Assists setup trigger
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1F2228))
                                .border(1.dp, Color(0xFF2E3239), RoundedCornerShape(4.dp))
                                .clickable { 
                                    isSettingsOpen = true
                                    isCompSuiteOpen = false
                                    isGalleryOpen = false
                                }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(imageVector = Icons.Default.SettingsInputComponent, contentDescription = null, tint = TactileOrange, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "METRICS DRAWER", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Computational Compare trigger
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1F2228))
                                .border(1.dp, Color(0xFF2E3239), RoundedCornerShape(4.dp))
                                .clickable { 
                                    isCompSuiteOpen = true
                                    isSettingsOpen = false
                                    isGalleryOpen = false
                                }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(imageVector = Icons.Default.Flip, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "COMPARE PANEL", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // C. Large Shutter Release Button + Storage Quick access
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick Browser trigger
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1C1D21))
                                .border(1.dp, Color(0xFF292C33), RoundedCornerShape(6.dp))
                                .clickable {
                                    isGalleryOpen = true
                                    isSettingsOpen = false
                                    isCompSuiteOpen = false
                                }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = Color.White, modifier = Modifier.size(18.dp))
                                Text(text = "[${photoGallery.size} ITEMS]", color = SlateTextSecondary, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // MAIN PHYSIC GTCAM SHUTTER RELEASE
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    androidx.compose.ui.graphics.Brush.radialGradient(
                                        colors = listOf(Color(0xFFFF9E80), TactileOrange)
                                    )
                                )
                                .border(4.dp, Color.White, CircleShape)
                                .clickable(enabled = state.captureProgress < 0f) {
                                    // Trigger advanced computational stacking
                                    scope.launch {
                                        state.captureProgress = 0f
                                        state.captureStageString = "Locking Realme OIS matrix..."
                                        delay(700)
                                        state.captureProgress = 0.25f
                                        state.captureStageString = "Bracketing exposure frames (-2EV, 0EV, +2EV)..."
                                        delay(800)
                                        state.captureProgress = 0.5f
                                        state.captureStageString = "Aligning sub-pixels (Sony IMX896 alignment)..."
                                        delay(800)
                                        state.captureProgress = 0.75f
                                        state.captureStageString = "Blending HDR saturation RAW curves..."
                                        delay(700)
                                        state.captureProgress = 1.0f
                                        state.captureStageString = "DNG file package completed!"
                                        delay(400)

                                        // Instantiate beautiful photo
                                        val randomId = "photo_" + System.currentTimeMillis()
                                        val modeLabel = state.currentMode.label
                                        val shutterText = state.shutterSpeed.label
                                        val isoVal = "ISO ${state.iso}"
                                        
                                        val formatTag = if (state.rawMode == RAWMode.RAW_DNG) "DNG RAW" else if (state.rawMode == RAWMode.RAW_JPEG) "RAW + JPEG" else "JPEG Only"
                                        val titleText = "GTCAM_" + state.currentMode.label + "_" + (100..999).random().toString() + "." + (if (state.rawMode == RAWMode.RAW_DNG) "DNG" else "JPG")
                                        
                                        val colorsSample = when (state.wbMode) {
                                            WBMode.SUNNY -> listOf(Color(0xFF0F1E24), Color(0xFFF57C00), Color(0xFFFFD54F))
                                            WBMode.CLOUDY -> listOf(Color(0xFF1B2A34), Color(0xFF78909C), Color(0xFFCFD8DC))
                                            else -> listOf(Color(0xFF140F24), Color(0xFF4A148C), Color(0xFFFF4081))
                                        }

                                        val newPhotoItem = GalleryItem(
                                            id = randomId,
                                            title = titleText,
                                            mode = "$modeLabel (Computational Blend)",
                                            iso = isoVal,
                                            shutter = shutterText,
                                            format = formatTag,
                                            resolution = if (state.currentMode == CameraMode.HR) "50.0 MP (8160 x 6120 Pixels)" else "12.2 MP (4032 x 3024 Pixels)",
                                            baseGradientColors = colorsSample
                                        )

                                        photoGallery.add(0, newPhotoItem)
                                        state.captureProgress = -1f
                                        
                                        // Trigger happy success card HUD fade in
                                        successNoticeString = "SUCCESSFULLY ADDED TO RAW STORE: $titleText [$formatTag format written]"
                                        delay(2600)
                                        successNoticeString = null
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "RELEASE", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                        }

                        // Help / Specs label
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1C1D21))
                                .border(1.dp, Color(0xFF292C33), RoundedCornerShape(6.dp))
                                .clickable {
                                    // Direct link to open comparative panel
                                    isCompSuiteOpen = true
                                    isSettingsOpen = false
                                }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = "Specs", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                                Text(text = "EXPLAIN", color = SlateTextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Divider(color = Color(0xFF1F2228), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(4.dp))

                    // D. Visual Rotating DSLR Mode Dial Selector Wheel
                    DslrModeDial(
                        selectedMode = state.currentMode,
                        onModeChange = { clickedMode ->
                            state.currentMode = clickedMode
                            
                            // Automated preset adjustments for specific locks
                            if (clickedMode == CameraMode.AUTO) {
                                state.wbMode = WBMode.AUTO
                                state.isManualFocus = false
                                state.rawMode = RAWMode.JPEG
                                state.bracketingMode = BracketingMode.OFF
                            } else if (clickedMode == CameraMode.HR) {
                                state.rawMode = RAWMode.RAW_JPEG // High resolution outputs
                            } else if (clickedMode == CameraMode.PORTRAIT) {
                                state.isManualFocus = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                }
            }

            // 3. DSLR QUICK-SETTING LOWER BAR
            DslrControlStrip(state = state)
        }

        // III. PROFESSIONAL ASSISTS OVERLAY SLIDERS / DRAWER SHEETS
        
        // Settings Drawer Left side
        DslrSettingsDrawer(
            state = state,
            isOpen = isSettingsOpen,
            onClose = { isSettingsOpen = false }
        )

        // Computational Photography Compare side-by-side Panel
        AnimatedVisibility(
            visible = isCompSuiteOpen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                ComputationalPhotographySuite()
                
                // Overlay close button top corner
                Button(
                    onClick = { isCompSuiteOpen = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.85f)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "CLOSE RECOVERY ANALYZER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Vault Gallery Layer Modal
        AnimatedVisibility(
            visible = isGalleryOpen,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            DslrGallery(
                photos = photoGallery,
                onDeletePhoto = { targetId -> photoGallery.removeAll { it.id == targetId } },
                onClose = { isGalleryOpen = false }
            )
        }

        // IV. HUD FLOATING SUCCESS CARRIER POPUP
        AnimatedVisibility(
            visible = successNoticeString != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -150 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -150 }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 45.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .widthIn(max = 550.dp)
                    .border(2.dp, Color(0xFF00E676), RoundedCornerShape(8.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Success",
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = successNoticeString ?: "",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
