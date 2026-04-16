package com.iama2z.glyphmeter.glyph

import com.iama2z.glyphmeter.audio.DbCalculator
import kotlin.math.floor

/**
 * Maps a decibel level to a list of [GlyphZoneState] objects, creating a
 * bottom-to-top "filling" effect — like a VU meter on the phone's back.
 *
 * ## Zone layout
 * The Nothing Phone 4a Pro Glyph Matrix is treated as 12 horizontal rows,
 * ordered from the bottom of the phone (index 0) to the top (index 11).
 * Adjust [ZONE_IDS] to match the real zone identifiers in the Nothing SDK.
 *
 * ## Fill algorithm
 * 1. The dB value is normalised to 0.0–1.0 within [DB_MIN, DB_MAX].
 * 2. The normalised value is multiplied by the total zone count → `fillLevel`.
 * 3. All zones below `floor(fillLevel)` are fully lit (intensity = 1.0).
 * 4. The zone at `floor(fillLevel)` gets a fractional intensity for smoothness.
 * 5. All zones above that are off (intensity = 0.0).
 */
object GlyphMatrixMapper {

    /** dB floor — matches [DbCalculator.DB_MIN]. */
    private const val DB_MIN = DbCalculator.DB_MIN

    /** dB ceiling — matches [DbCalculator.DB_MAX]. */
    private const val DB_MAX = DbCalculator.DB_MAX

    /**
     * Zone identifiers ordered from **bottom** (index 0) to **top** (last index).
     *
     * Replace these string values with the actual zone IDs from the Nothing
     * Glyph SDK when you integrate it.
     */
    private val ZONE_IDS = listOf(
        "matrix_row_0",  // bottom
        "matrix_row_1",
        "matrix_row_2",
        "matrix_row_3",
        "matrix_row_4",
        "matrix_row_5",
        "matrix_row_6",
        "matrix_row_7",
        "matrix_row_8",
        "matrix_row_9",
        "matrix_row_10",
        "matrix_row_11"  // top
    )

    /**
     * Converts a [db] reading into a list of [GlyphZoneState] objects.
     *
     * @param db  Current audio level in dBFS. Values outside [DB_MIN, DB_MAX] are clamped.
     * @return    One [GlyphZoneState] per zone, ordered bottom-to-top.
     */
    fun map(db: Float): List<GlyphZoneState> {
        val totalZones = ZONE_IDS.size

        // Clamp the dB value to our expected range.
        val clamped = db.coerceIn(DB_MIN, DB_MAX)

        // Normalise to 0.0–1.0.
        val normalised = (clamped - DB_MIN) / (DB_MAX - DB_MIN)

        // Continuous fill level (e.g. 7.4 means 7 full zones + one at 40 %).
        val fillLevel = normalised * totalZones
        val floorFillLevel = floor(fillLevel).toInt()
        val fractionalFill = fillLevel - floorFillLevel

        return ZONE_IDS.mapIndexed { index, zoneId ->
            when {
                index < floorFillLevel -> {
                    // Fully below the fill line — 100 % brightness.
                    GlyphZoneState(zoneId, active = true, intensity = 1f)
                }
                index.toFloat() < fillLevel -> {
                    // Transitional zone — partial brightness for smooth animation.
                    GlyphZoneState(zoneId, active = true, intensity = fractionalFill)
                }
                else -> {
                    // Above the fill line — off.
                    GlyphZoneState(zoneId, active = false, intensity = 0f)
                }
            }
        }
    }
}
