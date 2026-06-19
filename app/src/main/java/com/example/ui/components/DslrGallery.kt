package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MatteGraphite
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.TactileOrange

data class GalleryItem(
    val id: String,
    val title: String,
    val mode: String,
    val sensor: String = "Sony IMX896 (1/1.56\")",
    val iso: String,
    val shutter: String,
    val fStop: String = "f/1.8 (Fixed)",
    val focalLen: String = "24mm eq.",
    val format: String,
    val resolution: String,
    val baseGradientColors: List<Color>
)

@Composable
fun DslrGallery(
    photos: List<GalleryItem>,
    onDeletePhoto: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPhoto by remember { mutableStateOf<GalleryItem?>(null) }

    Box(
        modifier = modifier
            .testTag("dslr_gallery_panel")
            .fillMaxSize()
            .background(Color(0xFF0C0D0E))
    ) {
        if (selectedPhoto == null) {
            // I. Thumbnail Grid Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = TactileOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GTCAM HARDWARE BUFFER STORAGE",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }

                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2125))
                    ) {
                        Text(text = "BACK TO VIEWPORT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = Color(0xFF232529))
                Spacer(modifier = Modifier.height(10.dp))

                if (photos.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "RAW CAMERA BUFFER VACANT",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Use top shutter release keys in P, M, HR or Tv mode to capture high fidelity frames.",
                                color = Color.White.copy(alpha = 0.2f),
                                fontSize = 10.sp
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(photos) { photo ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1.5f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFF22252B), RoundedCornerShape(6.dp))
                                    .clickable { selectedPhoto = photo }
                            ) {
                                // Draw dynamic render background representations of captured things
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val grad = Brush.verticalGradient(
                                        colors = photo.baseGradientColors,
                                        startY = 0f,
                                        endY = size.height
                                    )
                                    drawRect(brush = grad)
                                }

                                // Format badge bottom tag
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = photo.format,
                                        color = TactileOrange,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Title text top tag
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(4.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = photo.title,
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        maxLines = 1,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // II. Full Photo Mode with Metadata EXIF Panel (DSLR style inspect view!)
            val curPhoto = selectedPhoto!!
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .animateContentSize()
            ) {
                // Left 65% - Photo full bleed renderer
                Box(
                    modifier = Modifier
                        .weight(0.65f)
                        .fillMaxHeight()
                        .background(Color.Black)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val grad = Brush.verticalGradient(
                            colors = curPhoto.baseGradientColors,
                            startY = 0f,
                            endY = size.height
                        )
                        drawRect(brush = grad)

                        // Draw neat mountain vectors inside full inspect view
                        drawRect(
                            color = Color(0xD0111115),
                            topLeft = Offset(0f, size.height * 0.75f),
                            size = Size(size.width, size.height * 0.25f)
                        )
                        drawLine(
                            color = TactileOrange.copy(alpha = 0.3f),
                            start = Offset(0f, size.height * 0.75f),
                            end = Offset(size.width, size.height * 0.75f),
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    // Back button to list selector
                    Button(
                        onClick = { selectedPhoto = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "BACK TO STORAGE MATRIX", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // Right 35% - Full EXIF Hardware specifications board
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(280.dp)
                        .background(Color(0xFF0F1113))
                        .border(width = 1.dp, color = Color(0xFF262A30))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RAW METADATA SPEC",
                                color = TactileOrange,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = curPhoto.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                        Text(text = "Captured via: ${curPhoto.mode}", color = SlateTextSecondary, fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color(0xFF202327))
                        Spacer(modifier = Modifier.height(12.dp))

                        // EXIF Data Lines
                        MetadataLine(label = "Primary Sensor", value = curPhoto.sensor)
                        MetadataLine(label = "Focal Equivalent", value = curPhoto.focalLen)
                        MetadataLine(label = "Hardware Aperture", value = curPhoto.fStop)
                        MetadataLine(label = "Configured Speed", value = curPhoto.shutter)
                        MetadataLine(label = "Configured Gain (ISO)", value = curPhoto.iso)
                        MetadataLine(label = "Output Matrix", value = curPhoto.resolution)
                        MetadataLine(label = "File Format", value = curPhoto.format)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onDeletePhoto(curPhoto.id)
                                selectedPhoto = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "DELETE FILER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetadataLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = SlateTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        Text(text = value, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
