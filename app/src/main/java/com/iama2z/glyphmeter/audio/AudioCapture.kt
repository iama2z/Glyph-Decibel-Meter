package com.iama2z.glyphmeter.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive

/**
 * Wraps [AudioRecord] and exposes a [Flow] of dB levels.
 *
 * Usage:
 * ```
 * val capture = AudioCapture()
 * capture.dbLevelFlow().collect { db -> /* update UI */ }
 * // When done:
 * capture.release()
 * ```
 *
 * Note: The RECORD_AUDIO permission must be granted before calling [dbLevelFlow].
 */
class AudioCapture {

    // Audio configuration — 44.1 kHz mono, 16-bit PCM is universally supported.
    private val sampleRate = 44_100 // Hz
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    // Minimum buffer size required by the hardware driver.
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        .coerceAtLeast(4096) // ensure at least 4 KB so each read covers ~46 ms of audio

    // Keep a reference so we can stop/release it later.
    private var audioRecord: AudioRecord? = null

    /**
     * Returns a [Flow] that emits the current dB level roughly 20 times per second.
     *
     * The flow runs on the IO dispatcher so it never blocks the main thread.
     * It is cold — recording only starts when collection begins and stops when
     * the coroutine is cancelled.
     */
    fun dbLevelFlow(): Flow<Float> = flow {
        // Create and start the recorder.
        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )
        audioRecord = recorder
        recorder.startRecording()

        // Read audio frames in a loop until the coroutine is cancelled.
        val buffer = ShortArray(bufferSize / 2) // ShortArray — each sample is 2 bytes
        try {
            while (currentCoroutineContext().isActive) {
                val read = recorder.read(buffer, 0, buffer.size)
                if (read > 0) {
                    val db = DbCalculator.rmsToDb(buffer, read)
                    emit(db)
                }
            }
        } finally {
            // Always clean up, even if an exception is thrown.
            recorder.stop()
            recorder.release()
            audioRecord = null
        }
    }.flowOn(Dispatchers.IO) // Do audio I/O on the IO thread pool

    /**
     * Stops recording and releases the [AudioRecord] resource.
     * Safe to call multiple times; subsequent calls are no-ops.
     */
    fun release() {
        audioRecord?.let {
            if (it.state == AudioRecord.STATE_INITIALIZED) {
                it.stop()
            }
            it.release()
            audioRecord = null
        }
    }
}
