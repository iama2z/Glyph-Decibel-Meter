package com.iama2z.glyphmeter.glyph

import android.util.Log

/**
 * A placeholder (stub) implementation of [GlyphController].
 *
 * Instead of driving real hardware LEDs it simply logs the zone states to
 * Android Logcat. This lets the rest of the app compile and run on any device
 * while you wait for the real Nothing Glyph SDK.
 *
 * ## How to replace this with the real SDK
 * 1. Add the Nothing Glyph SDK dependency to `app/build.gradle.kts`.
 * 2. Create `RealNothingGlyphController` that implements [GlyphController].
 * 3. In `MainActivity`, replace `StubGlyphController()` with your new class.
 */
class StubGlyphController : GlyphController {

    companion object {
        private const val TAG = "GlyphController"
    }

    override fun update(zones: List<GlyphZoneState>) {
        // Build a compact one-line summary, e.g.:
        //   row_0=100% row_1=100% row_2=47% row_3=off ...
        val summary = zones.joinToString(" ") { zone ->
            if (zone.active) {
                "${zone.zoneId}=${(zone.intensity * 100).toInt()}%"
            } else {
                "${zone.zoneId}=off"
            }
        }
        Log.d(TAG, "Glyph update → $summary")
    }

    override fun release() {
        Log.d(TAG, "Stub GlyphController released — all LEDs turned off (no-op on stub)")
    }
}
