package com.iama2z.glyphmeter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.iama2z.glyphmeter.audio.AudioCapture
import com.iama2z.glyphmeter.glyph.GlyphMatrixMapper
import com.iama2z.glyphmeter.glyph.StubGlyphController
import com.iama2z.glyphmeter.ui.MeterScreen
import com.iama2z.glyphmeter.ui.theme.GlyphDecibelMeterTheme

/**
 * The single Activity for the app.
 *
 * Responsibilities:
 *  1. Request the RECORD_AUDIO permission at runtime.
 *  2. Start/stop audio capture based on permission state.
 *  3. Pass the live dB level to the Compose UI and to the Glyph controller.
 */
class MainActivity : ComponentActivity() {

    // We lazily create AudioCapture only once; it is lifecycle-aware via coroutines.
    private val audioCapture by lazy { AudioCapture() }

    // Stub glyph controller — swap for the real Nothing SDK implementation later.
    private val glyphController = StubGlyphController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GlyphDecibelMeterTheme {
                // Track whether we have microphone permission.
                var hasPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }

                // Track the current dB reading to display on screen.
                var currentDb by remember { mutableFloatStateOf(-60f) }

                // System permission dialog launcher.
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { granted ->
                    hasPermission = granted
                }

                // When permission is granted, start collecting dB levels from the mic.
                LaunchedEffect(hasPermission) {
                    if (hasPermission) {
                        audioCapture.dbLevelFlow().collect { db ->
                            currentDb = db

                            // Map dB to Glyph zone states and send to the controller.
                            val zones = GlyphMatrixMapper.map(db)
                            glyphController.update(zones)
                        }
                    }
                }

                // Show the meter UI, requesting permission if needed.
                MeterScreen(
                    currentDb = currentDb,
                    hasPermission = hasPermission,
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Release the AudioRecord resource when the app goes to the background.
        audioCapture.release()
        // Turn off all Glyph LEDs and release the controller so lights don't
        // get stuck on after the app exits.
        glyphController.release()
    }
}
