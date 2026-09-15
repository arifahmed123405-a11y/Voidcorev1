package com.example.engine.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

enum class AudioCue(val displayName: String, val frequencyHz: Float, val durationMs: Int) {
    WAKE("Wake Arrival", 880f, 140),
    LISTENING("Listening Ready", 660f, 90),
    THINKING("Thinking Hum", 440f, 180),
    ACTING("Action Pulse", 587f, 120),
    CONFIRMATION("Confirmation Required", 740f, 160),
    SUCCESS("Success Resolution", 1046f, 220),
    WARNING("Security Warning", 370f, 200),
    ERROR("Error Recovery Tone", 290f, 250),
    NOTIFICATION("Notification Chime", 920f, 150),
    DISMISSAL("Dismissal Fade", 520f, 180)
}

class AudioIdentityEngine(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var isSoundEnabled: Boolean = true
    var isHapticsEnabled: Boolean = true

    fun playCue(cue: AudioCue) {
        if (isSoundEnabled) {
            scope.launch {
                synthesizeAndPlayTone(cue)
            }
        }
        if (isHapticsEnabled) {
            triggerHaptic(cue)
        }
    }

    private fun synthesizeAndPlayTone(cue: AudioCue) {
        try {
            val sampleRate = 44100
            val numSamples = (sampleRate * (cue.durationMs / 1000f)).toInt()
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / sampleRate
                // Harmonic synthesis with gentle attack & exponential decay envelope
                val envelope = sin((i.toFloat() / numSamples) * Math.PI.toFloat())
                val fundamental = sin(2.0 * Math.PI * cue.frequencyHz * t)
                val harmonic2 = 0.3 * sin(2.0 * Math.PI * (cue.frequencyHz * 1.5) * t)
                val harmonic3 = 0.15 * sin(2.0 * Math.PI * (cue.frequencyHz * 2.0) * t)
                
                val sampleValue = ((fundamental + harmonic2 + harmonic3) * envelope * 24000).toInt()
                buffer[i] = sampleValue.coerceIn(-32768, 32767).toShort()
            }

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            
            Thread.sleep(cue.durationMs.toLong() + 50)
            audioTrack.release()
        } catch (e: Exception) {
            // Audio synthesis safe fallback
        }
    }

    private fun triggerHaptic(cue: AudioCue) {
        if (vibrator == null || !vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (cue) {
                AudioCue.WAKE -> VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                AudioCue.SUCCESS -> VibrationEffect.createWaveform(longArrayOf(0, 30, 40, 50), -1)
                AudioCue.WARNING, AudioCue.ERROR -> VibrationEffect.createWaveform(longArrayOf(0, 60, 60, 80), -1)
                AudioCue.CONFIRMATION -> VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE)
                else -> VibrationEffect.createOneShot(25, 120)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AudioIdentityEngine? = null

        fun getInstance(context: Context): AudioIdentityEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AudioIdentityEngine(context).also { INSTANCE = it }
            }
        }
    }
}
