package com.echorescue.app.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.SystemClock
import kotlin.math.PI
import kotlin.math.sin

class ChirpEmitter(
    private val sampleRate: Int = 48_000,
    private val durationMs: Int = 100,
    private val startFrequency: Double = 20_000.0,
    private val endFrequency: Double = 22_000.0
) {
    fun emit(): Long {
        val sampleCount = (sampleRate * durationMs) / 1000
        val samples = ShortArray(sampleCount)

        for (index in 0 until sampleCount) {
            val t = index.toDouble() / sampleRate.toDouble()
            val progress = index.toDouble() / sampleCount.toDouble()
            val frequency = startFrequency + (endFrequency - startFrequency) * progress
            val amplitude = 0.45 * Short.MAX_VALUE
            samples[index] = (amplitude * sin(2.0 * PI * frequency * t)).toInt().toShort()
        }

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(samples.size * Short.SIZE_BYTES)
            .build()

        audioTrack.write(samples, 0, samples.size)
        val emissionStartNanos = SystemClock.elapsedRealtimeNanos()
        audioTrack.play()
        SystemClock.sleep(durationMs.toLong() + 20L)
        audioTrack.stop()
        audioTrack.release()
        return emissionStartNanos
    }
}
