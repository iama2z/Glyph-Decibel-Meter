package com.iama2z.glyphmeter.glyph

import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.nothing.ketchum.Glyph
import com.nothing.ketchum.GlyphMatrixManager

/**
 * Real implementation of [GlyphController] using the Nothing Glyph Matrix SDK v2.0.
 *
 * This implementation targets a 13x13 matrix (e.g., Phone (4a) Pro).
 */
class NothingGlyphController(context: Context) : GlyphController {

    private val glyphManager = GlyphMatrixManager.getInstance(context)
    private var isConnected = false

    private val callback = object : GlyphMatrixManager.Callback {
        override fun onServiceConnected(name: ComponentName) {
            Log.d("GlyphController", "Service Connected: $name")
            try {
                // Register for Phone (4a) Pro (DEVICE_25111p)
                glyphManager.register(Glyph.DEVICE_25111p)
                isConnected = true
            } catch (e: Exception) {
                Log.e("GlyphController", "Failed to register device", e)
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d("GlyphController", "Service Disconnected: $name")
            isConnected = false
        }
    }

    init {
        try {
            glyphManager.init(callback)
        } catch (e: Exception) {
            Log.e("GlyphController", "Failed to init GlyphManager", e)
        }
    }

    override fun update(zones: List<GlyphZoneState>) {
        if (!isConnected) return

        // 13x13 matrix = 169 pixels
        val matrixSize = 13
        val colors = IntArray(matrixSize * matrixSize) { 0 }

        // Map our rows to the 13x13 grid.
        zones.forEachIndexed { rowIdx, zone ->
            if (rowIdx < matrixSize) {
                // Brightness 0-255
                val brightness = (zone.intensity * 255).toInt().coerceIn(0, 255)
                
                // Flip so row 0 is at the bottom of the phone
                val matrixRow = (matrixSize - 1) - rowIdx 
                
                for (col in 0 until matrixSize) {
                    colors[matrixRow * matrixSize + col] = brightness
                }
            }
        }

        try {
            glyphManager.setAppMatrixFrame(colors)
        } catch (e: Exception) {
            Log.e("GlyphController", "Error updating matrix", e)
        }
    }

    override fun release() {
        try {
            glyphManager.closeAppMatrix()
            glyphManager.unInit()
        } catch (e: Exception) {
            Log.e("GlyphController", "Error releasing controller", e)
        }
    }
}
