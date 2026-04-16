package com.iama2z.glyphmeter.audio

import kotlin.math.log10
import kotlin.math.sqrt

/**
 * Utility object for converting raw PCM audio samples into a decibel level.
 *
 * The process has two steps:
 *  1. Calculate the **RMS** (Root Mean Square) of a buffer of audio samples.
 *     RMS represents the "average energy" of the signal.
 *  2. Convert RMS to **dBFS** (decibels relative to full scale):
 *     `dB = 20 * log10(rms / maxAmplitude)`
 *
 * The result is clamped to the range [DB_MIN, DB_MAX]:
 *  - 0 dBFS = maximum amplitude (all 16-bit samples at ±32767)
 *  - −60 dBFS ≈ near silence (treated as the floor)
 */
object DbCalculator {

    /** Floor dB value — anything quieter than this is treated as silence. */
    const val DB_MIN = -60f

    /** Ceiling dB value — 0 dBFS means the signal is at maximum amplitude. */
    const val DB_MAX = 0f

    // Maximum amplitude for a 16-bit signed PCM sample.
    private const val MAX_AMPLITUDE = 32767.0

    /**
     * Calculates the dB level from an array of 16-bit PCM samples.
     *
     * @param samples  Array of raw PCM short values from [android.media.AudioRecord].
     * @param count    Number of valid samples in [samples] (may be less than array size).
     * @return         dB level in the range [DB_MIN, DB_MAX].
     */
    fun rmsToDb(samples: ShortArray, count: Int): Float {
        if (count <= 0) return DB_MIN

        // Step 1: Compute the mean of the squares of all samples.
        var sumOfSquares = 0.0
        for (i in 0 until count) {
            val sample = samples[i].toDouble()
            sumOfSquares += sample * sample
        }
        val meanSquare = sumOfSquares / count

        // Step 2: Take the square root to get RMS.
        val rms = sqrt(meanSquare)

        // Avoid log10(0) which would produce -Infinity.
        if (rms == 0.0) return DB_MIN

        // Step 3: Convert to dBFS.
        val db = (20.0 * log10(rms / MAX_AMPLITUDE)).toFloat()

        // Clamp to our display range.
        return db.coerceIn(DB_MIN, DB_MAX)
    }
}
