package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ActiveYellow
import com.example.ui.theme.FocusRed
import com.example.ui.theme.FluorescentGreen

enum class CameraMode(val label: String, val shortDesc: String, val description: String) {
    AUTO("AUTO", "Full Auto", "Fully automated exposure, focus, and computational color balancing."),
    P("P", "Program AE", "Camera manages ISO and shutter speed; user controls EV, WB, focus, and metering."),
    TV("Tv", "Shutter Priority", "Specify shutter speed to capture action; camera automatically balances ISO."),
    M("M", "Manual", "Full creative command. Absolute control over ISO, Shutter, White Balance, and Focus."),
    PORTRAIT("PORTRAIT", "Depth Portrait", "Segment hair and skin using advanced segmentation algorithms with adjustable bokeh."),
    HR("HR", "High Resolution", "50MP Multi-frame micro-shift merge. Combines sub-pixel inputs for supreme clarity.")
}

enum class ShutterSpeed(val label: String, val seconds: Double) {
    S_4000("1/4000", 1.0 / 4000),
    S_2000("1/2000", 1.0 / 2000),
    S_1000("1/1000", 1.0 / 1000),
    S_500("1/500", 1.0 / 500),
    S_250("1/250", 1.0 / 250),
    S_125("1/125", 1.0 / 125),
    S_60("1/60", 1.0 / 60),
    S_1_30("1/30", 1.0 / 30),
    S_1("1 sec", 1.0),
    S_5("5 sec", 5.0),
    S_30("30 sec", 30.0)
}

enum class WBMode(val label: String) {
    AUTO("AWB"),
    SUNNY("Sunny (5200K)"),
    CLOUDY("Cloudy (6000K)"),
    INCANDESCENT("Tungsten (3200K)"),
    FLUORESCENT("Fluorescent (4000K)"),
    SHADE("Shade (7000K)")
}

enum class MeteringMode(val label: String) {
    MATRIX("Matrix (Multi)"),
    CENTER_WEIGHTED("Center-Weighted"),
    SPOT("Spot Meter")
}

enum class GridType(val label: String) {
    NONE("No Grid"),
    RULE_OF_THIRDS("Rule of Thirds"),
    GOLDEN_RATIO("Golden Ratio"),
    SQUARE("1:1 Framing Grid"),
    CENTER_CROSS("Center Crosshair")
}

enum class PeakingColor(val label: String, val color: Color) {
    RED("Red Peaking", FocusRed),
    GREEN("Green Peaking", FluorescentGreen),
    YELLOW("Yellow Peaking", ActiveYellow)
}

enum class PeakingSensitivity(val label: String) {
    LOW("Low"),
    MED("Medium"),
    HIGH("High")
}

enum class ZebraThreshold(val label: String, val percentage: Int) {
    T_95("95% (Near Clipping)", 95),
    T_98("98% (Overexposed)", 98),
    T_100("100% (Highlight Clipped)", 100)
}

enum class RAWMode(val label: String) {
    JPEG("JPEG Only"),
    RAW_DNG("DNG RAW Only"),
    RAW_JPEG("RAW + JPEG")
}

enum class BracketingMode(val label: String, val shots: Int) {
    OFF("Single Shot", 1),
    THREE_SHOT("3-Shot AEB", 3),
    FIVE_SHOT("5-Shot AEB", 5),
    SEVEN_SHOT("7-Shot AEB", 7)
}

enum class LensType(val label: String, val aperture: String, val focal: String) {
    MAIN("SONY IMX896 Main Lens (50MP)", "f/1.8 (OIS)", "24mm eq."),
    ULTRAWIDE("Ultrawide Secondary (8MP)", "f/2.2", "112° eq.")
}

class CameraState {
    var currentMode by mutableStateOf(CameraMode.AUTO)
    var currentLens by mutableStateOf(LensType.MAIN)
    
    // Core Manual Parameters
    var iso by mutableStateOf(400) // Ranges 100 - 12800
    var shutterSpeed by mutableStateOf(ShutterSpeed.S_250)
    var wbMode by mutableStateOf(WBMode.AUTO)
    var meteringMode by mutableStateOf(MeteringMode.MATRIX)
    var exposureCompensation by mutableStateOf(0.0) // -3.0 to +3.0
    
    // Focus Settings
    var isManualFocus by mutableStateOf(false)
    var manualFocusDistance by mutableStateOf(1.0f) // Slider from 0.1m to Infinity (represented as 0f to 10f slider)
    
    // Assistant Guides
    var showHistogram by mutableStateOf(true)
    var showFocusPeaking by mutableStateOf(false)
    var peakingColor by mutableStateOf(PeakingColor.GREEN)
    var peakingSensitivity by mutableStateOf(PeakingSensitivity.MED)
    
    var showZebraStripes by mutableStateOf(false)
    var zebraThreshold by mutableStateOf(ZebraThreshold.T_98)
    
    var gridType by mutableStateOf(GridType.NONE)
    var rawMode by mutableStateOf(RAWMode.JPEG)
    
    // Bracketing Settings
    var bracketingMode by mutableStateOf(BracketingMode.OFF)
    var bracketingInterval by mutableStateOf(1.0) // ±0.3, ±0.7, ±1.0, ±2.0 EV
    
    // Computational Capturing Flow
    var captureProgress by mutableStateOf(-1f) // -1 means inactive. 0.0 to 1.0 means active merge progress
    var captureStageString by mutableStateOf("")
    var isCapturingFinished by mutableStateOf(false)
    var capturedImageUri by mutableStateOf<String?>(null)
    
    // Exposure Meter calculation based on settings
    val calculatedExposureDeviation: Float
        get() {
            if (currentMode == CameraMode.AUTO) return 0f
            if (currentMode == CameraMode.P) return exposureCompensation.toFloat()
            
            // M/Tv calculations: compare speed, ISO to baseline exposure of 0.
            // Say baseline EV is for ISO 400 & Shutter 1/250 at EV=0.
            val shutterFactor = Math.log(shutterSpeed.seconds / (1.0 / 250.0)) / Math.log(2.0)
            val isoFactor = Math.log(iso.toDouble() / 400.0) / Math.log(2.0)
            val compensation = if (currentMode == CameraMode.TV) exposureCompensation else 0.0
            
            return (isoFactor + shutterFactor + compensation).toFloat().coerceIn(-3f, 3f)
        }
}
