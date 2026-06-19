package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.*
import com.example.ui.theme.MatteGraphite
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.TactileOrange

@Composable
fun DslrControlStrip(
    state: CameraState,
    modifier: Modifier = Modifier
) {
    var activeConfigTab by remember { mutableStateOf<String?>("ISO") } // "ISO", "SHUTTER", "WB", "METER", "BRACKET"

    // DSLR Mechanical Locks rules
    val canControlIso = state.currentMode == CameraMode.M
    val canControlShutter = state.currentMode == CameraMode.M || state.currentMode == CameraMode.TV
    val canControlWb = state.currentMode != CameraMode.AUTO
    val canControlEv = state.currentMode == CameraMode.P || state.currentMode == CameraMode.TV

    Column(
        modifier = modifier
            .testTag("dslr_control_strip")
            .fillMaxWidth()
            .background(Color(0xFF121417))
            .border(width = 1.dp, color = Color(0xFF1D2125))
            .padding(6.dp)
    ) {
        // I. Top Row: Quick parameters status indicator bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ISO Badge Status
                StatusPill(
                    label = "ISO",
                    value = if (state.currentMode == CameraMode.AUTO) "AUTO" else state.iso.toString(),
                    isLocked = !canControlIso,
                    isSelected = activeConfigTab == "ISO" && canControlIso,
                    onClick = { if (canControlIso) activeConfigTab = "ISO" }
                )

                // Shutter Speed Badge Status
                StatusPill(
                    label = "SHUTTER",
                    value = if (state.currentMode == CameraMode.AUTO || state.currentMode == CameraMode.P) "AUTO" else state.shutterSpeed.label,
                    isLocked = !canControlShutter,
                    isSelected = activeConfigTab == "SHUTTER" && canControlShutter,
                    onClick = { if (canControlShutter) activeConfigTab = "SHUTTER" }
                )

                // White Balance Badge Status
                StatusPill(
                    label = "WB",
                    value = if (state.currentMode == CameraMode.AUTO) "AWB" else state.wbMode.label,
                    isLocked = !canControlWb,
                    isSelected = activeConfigTab == "WB" && canControlWb,
                    onClick = { if (canControlWb) activeConfigTab = "WB" }
                )

                // Exposure Compensation Status
                StatusPill(
                    label = "EV",
                    value = if (canControlEv) "${if (state.exposureCompensation >= 0) "+" else ""}${String.format("%.1f", state.exposureCompensation)}" else "SECURED",
                    isLocked = !canControlEv,
                    isSelected = activeConfigTab == "EV" && canControlEv,
                    onClick = { if (canControlEv) activeConfigTab = "EV" }
                )

                // Metering Mode Badge Status
                StatusPill(
                    label = "METERING",
                    value = state.meteringMode.label.substringBefore(" "),
                    isLocked = state.currentMode == CameraMode.AUTO,
                    isSelected = activeConfigTab == "METER" && state.currentMode != CameraMode.AUTO,
                    onClick = { if (state.currentMode != CameraMode.AUTO) activeConfigTab = "METER" }
                )
            }

            // Lens Swapping Selector Indicator
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF24272D))
                    .padding(2.dp)
            ) {
                val lenses = LensType.values()
                lenses.forEach { lens ->
                    val isSelected = state.currentLens == lens
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) TactileOrange else Color.Transparent)
                            .clickable { state.currentLens = lens }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (lens == LensType.MAIN) "MAIN (IMX896)" else "W_ANGLE (8MP)",
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // II. Bottom Row: Interactive Slider / Selector matching the active CONFIG tab
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F1012), RoundedCornerShape(4.dp))
                .border(1.dp, Color(0xFF1C1E22), RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            when {
                // 1. ISO Configurations
                activeConfigTab == "ISO" && canControlIso -> {
                    Column {
                        Text(
                            text = "MANUAL ISO (Sony IMX896 Analog Gains):",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val isoValues = listOf(50, 100, 200, 400, 800, 1600, 3200, 6400, 12800)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(isoValues) { valIso ->
                                val isActive = state.iso == valIso
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (isActive) TactileOrange else Color(0xFF1A1C20))
                                        .clickable { state.iso = valIso }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = valIso.toString(),
                                        color = if (isActive) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Shutter Speed Configurations
                activeConfigTab == "SHUTTER" && canControlShutter -> {
                    Column {
                        Text(
                            text = "SHUTTER TIME PRESETS (OIS active tracking):",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(ShutterSpeed.values()) { valShutter ->
                                val isActive = state.shutterSpeed == valShutter
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (isActive) TactileOrange else Color(0xFF1A1C20))
                                        .clickable { state.shutterSpeed = valShutter }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = valShutter.label,
                                        color = if (isActive) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. White Balance Configurations
                activeConfigTab == "WB" && canControlWb -> {
                    Column {
                        Text(
                            text = "COLOR TEMPERATURE CALIBRATIONS:",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(WBMode.values()) { modeWb ->
                                val isActive = state.wbMode == modeWb
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (isActive) TactileOrange else Color(0xFF1A1C20))
                                        .clickable { state.wbMode = modeWb }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = modeWb.label,
                                        color = if (isActive) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Exposure Compensation Configurations
                activeConfigTab == "EV" && canControlEv -> {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "EXPOSURE COMPENSATION SLIDER:",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${if (state.exposureCompensation >= 0) "+" else ""}${String.format("%.1f EV", state.exposureCompensation)}",
                                color = TactileOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Slider(
                            value = state.exposureCompensation.toFloat(),
                            onValueChange = { state.exposureCompensation = (Math.round(it * 3f) / 3f).toDouble() },
                            valueRange = -3.0f..3.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = TactileOrange,
                                activeTrackColor = TactileOrange,
                                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }
                }

                // 5. Metering Mode Configurations
                activeConfigTab == "METER" && state.currentMode != CameraMode.AUTO -> {
                    Column {
                        Text(
                            text = "PHOTOMETRIC METERING MATRIX PATTERNS:",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MeteringMode.values().forEach { mode ->
                                val isActive = state.meteringMode == mode
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (isActive) TactileOrange else Color(0xFF1A1C20))
                                        .clickable { state.meteringMode = mode }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = mode.label,
                                        color = if (isActive) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {
                    // Locked fallback notice
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Exposure parameters are currently managed by Program Auto-AE. Tap mode: 'M' or 'Tv' to unlock.",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusPill(
    label: String,
    value: String,
    isLocked: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(
                when {
                    isSelected -> TactileOrange
                    isLocked -> Color(0xFF1C1D21).copy(alpha = 0.6f)
                    else -> Color(0xFF1F2228)
                }
            )
            .clickable(enabled = !isLocked, onClick = onClick)
            .border(
                1.dp,
                if (isSelected) Color.Transparent else Color(0xFF2E323A),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Column {
                Text(
                    text = label,
                    color = when {
                        isSelected -> Color.Black.copy(alpha = 0.6f)
                        isLocked -> Color.White.copy(alpha = 0.25f)
                        else -> Color.White.copy(alpha = 0.4f)
                    } ,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = value,
                    color = when {
                        isSelected -> Color.Black
                        isLocked -> Color.White.copy(alpha = 0.35f)
                        else -> Color.White
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}
