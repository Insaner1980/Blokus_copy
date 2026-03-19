package com.emmagame.polyomino.ui.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator

class SoundManager(context: Context) {

    private val soundPool: SoundPool
    private var placementSoundId: Int = 0
    private var loaded = false
    var enabled = true

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attrs)
            .build()
        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) loaded = true
        }
        // Generate a short "tok" tone programmatically via ToneGenerator
        // Since SoundPool needs a file, we use ToneGenerator directly for playback
    }

    fun playPlacement() {
        if (!enabled) return
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
            toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 80)
            // Release after short delay
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                toneGen.release()
            }, 150)
        } catch (_: Exception) { }
    }

    fun playInvalid() {
        if (!enabled) return
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 40)
            toneGen.startTone(ToneGenerator.TONE_PROP_NACK, 120)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                toneGen.release()
            }, 200)
        } catch (_: Exception) { }
    }

    fun release() {
        soundPool.release()
    }
}
