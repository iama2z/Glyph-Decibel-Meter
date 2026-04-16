package com.iama2z.glyphmeter.glyph

/**
 * Represents the state of a single Glyph Matrix zone.
 *
 * @param zoneId    A string identifier for the zone (e.g. "matrix_row_0").
 *                  These IDs should match whatever the real Nothing Glyph SDK uses.
 * @param active    Whether this zone should be illuminated.
 * @param intensity Brightness level from 0.0 (off) to 1.0 (full brightness).
 *                  The topmost active zone uses a fractional value for smooth transitions.
 */
data class GlyphZoneState(
    val zoneId: String,
    val active: Boolean,
    val intensity: Float
)

/**
 * Interface for controlling the Nothing Phone Glyph LEDs.
 *
 * Implement this interface with the real Nothing Glyph SDK when it becomes
 * available. For now, [StubGlyphController] provides a no-op placeholder.
 *
 * Typical usage:
 * ```
 * val controller: GlyphController = RealNothingGlyphController(context)
 * controller.update(GlyphMatrixMapper.map(currentDb))
 * ```
 */
interface GlyphController {

    /**
     * Apply the given zone states to the physical Glyph LEDs.
     *
     * @param zones List of zone states produced by [GlyphMatrixMapper.map].
     */
    fun update(zones: List<GlyphZoneState>)

    /**
     * Release any resources held by this controller (e.g. SDK session).
     * Call this when the app is stopping.
     */
    fun release()
}
