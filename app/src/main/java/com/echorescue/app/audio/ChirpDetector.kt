package com.echorescue.app.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.SystemClock
import java.lang.IllegalStateException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.sqrt

data class ChirpDetection(
    val timestampNanos: Long,
    val signalPower: Float,
    val noiseFloor: Float,
    val noiseFloorDb: Float
)

class ChirpDetector(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val sampleRate: Int = 48_000,
    private val blockSize: Int = 1024,
    private val threshold: Float = 0.15f
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val goertzel = GoertzelDetector(sampleRate, blockSize)
    private var recorder: AudioRecord? = null
    private var job: Job? = null
    private var lastDetectionNanos: Long = 0L

    fun start(onDetection: (ChirpDetection) -> Unit) {
        if (job != null) return

        val minBuffer = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = maxOf(minBuffer, blockSize * Short.SIZE_BYTES * 2)
        val audioRecord = buildRecorder(bufferSize)

        recorder = audioRecord
        job = scope.launch {
            val raw = ShortArray(blockSize)
            val floats = FloatArray(blockSize)

            audioRecord.startRecording()
            while (isActive) {
                val read = audioRecord.read(raw, 0, raw.size)
                if (read <= 0) continue

                var energy = 0.0
                for (i in 0 until read) {
                    val sample = raw[i] / Short.MAX_VALUE.toFloat()
                    floats[i] = sample
                    energy += sample * sample
                }
                for (i in read until blockSize) {
                    floats[i] = 0f
                }

                val power20 = goertzel.analyze(floats, 20_000)
                val power21 = goertzel.analyze(floats, 21_000)
                val power22 = goertzel.analyze(floats, 22_000)
                val combinedPower = (power20 + power21 + power22) / 3f
                val rms = sqrt((energy / read.toDouble()).coerceAtLeast(1e-9)).toFloat()
                val noiseFloorDb = (20f * log10(rms + 1e-6f) + 100f).coerceIn(0f, 100f)
                val now = SystemClock.elapsedRealtimeNanos()

                if (combinedPower >= threshold && now - lastDetectionNanos > 250_000_000L) {
                    lastDetectionNanos = now
                    onDetection(
                        ChirpDetection(
                            timestampNanos = now,
                            signalPower = combinedPower,
                            noiseFloor = rms,
                            noiseFloorDb = noiseFloorDb
                        )
                    )
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        runCatching { recorder?.stop() }
        recorder?.release()
        recorder = null
    }

    fun release() {
        stop()
        scope.cancel()
    }

    private fun buildRecorder(bufferSize: Int): AudioRecord {
        val preferred = AudioRecord(
            MediaRecorder.AudioSource.UNPROCESSED,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        if (preferred.state == AudioRecord.STATE_INITIALIZED) {
            return preferred
        }

        preferred.release()
        val fallback = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        if (fallback.state != AudioRecord.STATE_INITIALIZED) {
            fallback.release()
            throw IllegalStateException("AudioRecord initialization failed for both UNPROCESSED and MIC sources.")
        }
        return fallback
    }
}
