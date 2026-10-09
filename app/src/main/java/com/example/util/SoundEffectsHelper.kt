package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.*
import java.util.Random

/**
 * Sound and audio generator for Focus Timer, Alarms, and Celebrations.
 * Features:
 * - Victory Melodic Chime (tones synthesized or system ringtone)
 * - Real-time synthesized ambient sounds: Rain, White Noise, Campfire, Deep Cosmos
 * - Haptic feedback integration
 */
class SoundEffectsHelper(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var ambientTrack: AudioTrack? = null
    private var ambientJob: Job? = null
    private val audioScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * Plays an uplifting victory chime when a timer or quest finishes.
     */
    fun playVictoryChime() {
        try {
            audioScope.launch {
                // Try system notification sound first
                val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(context, notificationUri)
                ringtone?.play()
            }
        } catch (e: Exception) {
            Log.e("SoundEffectsHelper", "Error playing system ringtone: ${e.message}")
            // Fallback to ToneGenerator melody
            playToneSequence()
        }
    }

    private fun playToneSequence() {
        audioScope.launch {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                delay(180)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 200)
                delay(220)
                toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 300)
                delay(350)
                toneGen.release()
            } catch (e: Exception) {
                Log.e("SoundEffectsHelper", "ToneGenerator fallback error: ${e.message}")
            }
        }
    }

    /**
     * Mischievous demonic laughter sequence using synthesized audio tones.
     */
    fun playDemonLaugh() {
        audioScope.launch {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
                // Chuckling rhythmic tones descending into hellish grin
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 100)
                delay(120)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 100)
                delay(120)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 140)
                delay(160)
                toneGen.startTone(ToneGenerator.TONE_PROP_PROMPT, 220)
                delay(240)
                toneGen.release()
                triggerVibration("DEMON")
            } catch (e: Exception) {
                Log.e("SoundEffectsHelper", "Demon laugh sound error: ${e.message}")
            }
        }
    }

    fun playHellBurst() {
        audioScope.launch {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
                toneGen.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 120)
                delay(140)
                toneGen.startTone(ToneGenerator.TONE_CDMA_MED_L, 180)
                delay(200)
                toneGen.release()
                triggerVibration("MEDIUM")
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun playPactWon() {
        audioScope.launch {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 95)
                toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 160)
                delay(180)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 220)
                delay(240)
                toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 350)
                delay(380)
                toneGen.release()
                triggerVibration("VICTORY")
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun playPactLost() {
        audioScope.launch {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
                toneGen.startTone(ToneGenerator.TONE_PROP_NACK, 350)
                delay(380)
                toneGen.release()
                triggerVibration("ALARM")
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private var activeAlarmRingtone: android.media.Ringtone? = null

    /**
     * Starts playing a continuous alarm ringtone and repeating vibration until stopped.
     */
    fun startContinuousAlarmRingtone() {
        stopAlarmRingtone()
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, alarmUri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone?.isLooping = true
            }
            ringtone?.play()
            activeAlarmRingtone = ringtone
            triggerVibration("ALARM")
        } catch (e: Exception) {
            playToneSequence()
        }
    }

    /**
     * Stops any currently playing alarm ringtone.
     */
    fun stopAlarmRingtone() {
        try {
            activeAlarmRingtone?.stop()
        } catch (e: Exception) {
            // Ignore
        }
        activeAlarmRingtone = null
    }

    /**
     * Plays a distinct alarm bell / chime when a scheduled task alarm fires.
     */
    fun playAlarmAlert() {
        try {
            audioScope.launch {
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(context, alarmUri)
                ringtone?.play()
            }
        } catch (e: Exception) {
            playToneSequence()
        }
    }

    /**
     * Haptic feedback patterns.
     */
    fun triggerVibration(pattern: String = "VICTORY") {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (pattern) {
                    "LIGHT" -> VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                    "MEDIUM" -> VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE)
                    "ALARM" -> VibrationEffect.createWaveform(longArrayOf(0, 250, 150, 250, 150, 400), -1)
                    "DEMON" -> VibrationEffect.createWaveform(longArrayOf(0, 100, 60, 100, 60, 220), -1)
                    else -> VibrationEffect.createWaveform(longArrayOf(0, 80, 60, 140, 80, 220), -1)
                }
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(150)
            }
        } catch (e: Exception) {
            // Ignore vibration errors
        }
    }

    /**
     * Starts playing real synthesized ambient focus audio: Rain, White Noise, Campfire, Cosmos.
     */
    fun startAmbientAudio(soundType: String) {
        stopAmbientAudio()
        if (soundType == "Тишина 🤫" || soundType.isBlank()) return

        ambientJob = audioScope.launch {
            val sampleRate = 22050
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            ambientTrack = track

            try {
                track.play()
                val buffer = ShortArray(bufferSize / 2)
                val random = Random()
                var filterState = 0.0
                var phase = 0.0

                while (isActive) {
                    when {
                        soundType.contains("дождя") -> {
                            // Soft low-passed pinkish noise with occasional droplet impulses
                            for (i in buffer.indices) {
                                val white = (random.nextDouble() * 2.0 - 1.0)
                                filterState = 0.94 * filterState + 0.06 * white
                                val droplet = if (random.nextInt(1200) == 0) (random.nextDouble() * 0.4) else 0.0
                                val sample = (filterState * 0.25 + droplet).coerceIn(-1.0, 1.0)
                                buffer[i] = (sample * 16000).toInt().toShort()
                            }
                        }
                        soundType.contains("Белый") -> {
                            // Gentle broadband soothing noise
                            for (i in buffer.indices) {
                                val white = (random.nextDouble() * 2.0 - 1.0)
                                filterState = 0.85 * filterState + 0.15 * white
                                buffer[i] = (filterState * 7000).toInt().toShort()
                            }
                        }
                        soundType.contains("Костер") -> {
                            // Low hum + crackle sparks
                            for (i in buffer.indices) {
                                val white = (random.nextDouble() * 2.0 - 1.0)
                                filterState = 0.96 * filterState + 0.04 * white
                                val crackle = if (random.nextInt(350) == 0) (random.nextDouble() * 0.8 - 0.4) else 0.0
                                val sample = (filterState * 0.2 + crackle).coerceIn(-1.0, 1.0)
                                buffer[i] = (sample * 15000).toInt().toShort()
                            }
                        }
                        soundType.contains("Космос") -> {
                            // Soft 432 Hz warm binaural drone
                            val freq = 108.0 // deep soothing octave
                            for (i in buffer.indices) {
                                val sine = Math.sin(phase)
                                phase += 2.0 * Math.PI * freq / sampleRate
                                if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI
                                val noise = (random.nextDouble() * 2.0 - 1.0) * 0.02
                                val sample = (sine * 0.18 + noise).coerceIn(-1.0, 1.0)
                                buffer[i] = (sample * 18000).toInt().toShort()
                            }
                        }
                        else -> {
                            // Default soft whisper
                            for (i in buffer.indices) {
                                val white = (random.nextDouble() * 2.0 - 1.0)
                                filterState = 0.90 * filterState + 0.10 * white
                                buffer[i] = (filterState * 5000).toInt().toShort()
                            }
                        }
                    }
                    track.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                Log.e("SoundEffectsHelper", "Ambient audio track error: ${e.message}")
            } finally {
                try {
                    track.stop()
                    track.release()
                } catch (e: Exception) {
                    // Ignore release errors
                }
            }
        }
    }

    /**
     * Stops ambient audio playback.
     */
    fun stopAmbientAudio() {
        ambientJob?.cancel()
        ambientJob = null
        try {
            ambientTrack?.stop()
            ambientTrack?.release()
        } catch (e: Exception) {
            // Ignore
        }
        ambientTrack = null
    }
}
