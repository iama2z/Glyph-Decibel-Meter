package com.iama2z.glyphmeter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iama2z.glyphmeter.audio.DbCalculator
import com.iama2z.glyphmeter.ui.theme.GlyphDecibelMeterTheme

/**
 * The main screen of the app.
 *
 * Shows:
 *  - A title and the current dB reading as text.
 *  - A vertical fill bar that grows from bottom to top as loudness increases.
 *  - A sensitivity slider to adjust the gain.
 *  - A permission prompt if microphone access hasn't been granted yet.
 *
 * @param currentDb          The latest dB level (range: −60 to 0).
 * @param sensitivity        Current sensitivity offset in dB.
 * @param onSensitivityChange Callback when the slider is moved.
 * @param hasPermission      Whether RECORD_AUDIO has been granted.
 * @param onRequestPermission Callback to trigger the system permission dialog.
 */
@Composable
fun MeterScreen(
    currentDb: Float,
    sensitivity: Float,
    onSensitivityChange: (Float) -> Unit,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    // Apply sensitivity to the current dB reading for visualization.
    val adjustedDb = (currentDb + sensitivity).coerceIn(DbCalculator.DB_MIN, DbCalculator.DB_MAX)

    // Normalise adjusted dB to 0.0–1.0 for the fill fraction.
    val fillFraction = ((adjustedDb - DbCalculator.DB_MIN) /
            (DbCalculator.DB_MAX - DbCalculator.DB_MIN))
        .coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ---- Title ----
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Glyph",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 6.sp
            )
            Text(
                text = "Decibel Meter",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        // ---- Vertical fill bar ----
        if (hasPermission) {
            VerticalMeterBar(fillFraction = fillFraction)
        } else {
            // Show a permission prompt instead of the bar.
            PermissionPrompt(onRequestPermission = onRequestPermission)
        }

        // ---- dB reading ----
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (hasPermission) "%.1f dBFS".format(adjustedDb) else "-- dBFS",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "current level",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Light
            )

            if (hasPermission) {
                Spacer(modifier = Modifier.height(32.dp))
                SensitivitySlider(
                    sensitivity = sensitivity,
                    onSensitivityChange = onSensitivityChange
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * A slider to adjust the sensitivity (gain) of the meter.
 */
@Composable
private fun SensitivitySlider(
    sensitivity: Float,
    onSensitivityChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(0.8f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sensitivity: %+.1f dB".format(sensitivity),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Slider(
            value = sensitivity,
            onValueChange = onSensitivityChange,
            valueRange = -20f..20f,
            steps = 40
        )
    }
}

/**
 * A vertical bar that fills from bottom to top according to [fillFraction].
 *
 * The bar uses a gradient from green (quiet) through yellow to red (loud),
 * matching the classic VU meter aesthetic.
 */
@Composable
private fun VerticalMeterBar(fillFraction: Float) {
    // The track is the grey background; the fill sits on top inside it.
    Box(
        modifier = Modifier
            .width(80.dp)
            .fillMaxHeight(0.65f)   // occupy 65 % of screen height
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E)), // dark grey track
        contentAlignment = Alignment.BottomCenter
    ) {
        // The coloured fill grows from the bottom up.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fillFraction)
                .background(
                    brush = Brush.verticalGradient(
                        // Gradient: top = red (loud), bottom = green (quiet)
                        colors = listOf(
                            Color(0xFFFF3B30), // red   — top / loud
                            Color(0xFFFFCC00), // yellow — middle
                            Color(0xFF34C759)  // green  — bottom / quiet
                        )
                    )
                )
        )
    }
}

/**
 * A simple card shown when the app doesn't yet have microphone permission.
 */
@Composable
private fun PermissionPrompt(onRequestPermission: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxHeight(0.65f)
    ) {
        Text(
            text = "🎙️",
            fontSize = 56.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Microphone access needed",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap below to grant permission\nand start measuring.",
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant Microphone Access")
        }
    }
}

// ---- Previews ----

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun MeterScreenActivePreview() {
    GlyphDecibelMeterTheme {
        MeterScreen(
            currentDb = -20f,
            sensitivity = 0f,
            onSensitivityChange = {},
            hasPermission = true,
            onRequestPermission = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun MeterScreenPermissionPreview() {
    GlyphDecibelMeterTheme {
        MeterScreen(
            currentDb = -60f,
            sensitivity = 0f,
            onSensitivityChange = {},
            hasPermission = false,
            onRequestPermission = {}
        )
    }
}
