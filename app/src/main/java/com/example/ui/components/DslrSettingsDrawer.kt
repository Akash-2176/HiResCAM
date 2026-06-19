package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
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
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.TactileOrange
import androidx.compose.ui.draw.scale

@Composable
fun DslrSettingsDrawer(
    state: CameraState,
    isOpen: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
        modifier = modifier
            .testTag("settings_drawer")
            .width(260.dp)
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F1114))
                .border(width = 1.dp, color = Color(0xFF1E2125))
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = TactileOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HARDWARE ASSISTS",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFF1F2228), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // 1. RAW / DNG Format
                item {
                    Column {
                        Text("RAW SENSOR CONFIGURE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RAWMode.values().forEach { mode ->
                                val active = state.rawMode == mode
                                TextCard(
                                    text = mode.label.substringBefore(" "),
                                    isActive = active,
                                    onClick = { state.rawMode = mode }
                                )
                            }
                        }
                    }
                }

                // 2. Grids
                item {
                    Column {
                        Text("COMPOSITIVE GRIDS", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(GridType.values()) { type ->
                                val active = state.gridType == type
                                TextCard(
                                    text = type.label.substringBefore(" "),
                                    isActive = active,
                                    onClick = { state.gridType = type }
                                )
                            }
                        }
                    }
                }

                // 3. Focus Peaking Toggle & Color
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("FOCUS PEAKING (MF EDGE)", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Switch(
                                checked = state.showFocusPeaking,
                                onCheckedChange = { state.showFocusPeaking = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TactileOrange,
                                    checkedTrackColor = TactileOrange.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.scale(0.65f)
                            )
                        }
                        
                        if (state.showFocusPeaking) {
                            Spacer(modifier = Modifier.height(4.dp))
                            // Select colors
                            Text("Peaking Edge Tint:", color = SlateTextSecondary, fontSize = 8.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                PeakingColor.values().forEach { col ->
                                    val active = state.peakingColor == col
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (active) col.color.copy(alpha = 0.3f) else Color(0xFF171A1E))
                                            .border(width = 1.dp, color = if (active) col.color else Color.Transparent, shape = RoundedCornerShape(4.dp))
                                            .clickable { state.peakingColor = col }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = col.label.substringBefore(" "), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Zebra Stripes Toggle & Threshold
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ZEBRA STRIPES (CLIPPING)", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Switch(
                                checked = state.showZebraStripes,
                                onCheckedChange = { state.showZebraStripes = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TactileOrange,
                                    checkedTrackColor = TactileOrange.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.scale(0.65f)
                            )
                        }

                        if (state.showZebraStripes) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Clipping Sensitivity:", color = SlateTextSecondary, fontSize = 8.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ZebraThreshold.values().forEach { thresh ->
                                    val active = state.zebraThreshold == thresh
                                    TextCard(
                                        text = thresh.label.substringBefore(" "),
                                        isActive = active,
                                        onClick = { state.zebraThreshold = thresh }
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Bracketing
                item {
                    Column {
                        Text("AE BRACKETING (HDR BURST)", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            BracketingMode.values().forEach { mode ->
                                val active = state.bracketingMode == mode
                                TextCard(
                                    text = mode.label.substringBefore(" "),
                                    isActive = active,
                                    onClick = { state.bracketingMode = mode }
                                )
                            }
                        }

                        if (state.bracketingMode != BracketingMode.OFF) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Bracketing Interval Split EV:", color = SlateTextSecondary, fontSize = 8.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(0.3, 0.7, 1.0, 2.0).forEach { ev ->
                                    val active = state.bracketingInterval == ev
                                    TextCard(
                                        text = "±${ev}",
                                        isActive = active,
                                        onClick = { state.bracketingInterval = ev }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TextCard(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isActive) TactileOrange else Color(0xFF171A1E))
            .border(1.dp, if (isActive) Color.Transparent else Color(0xFF29303A), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            color = if (isActive) Color.Black else Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
