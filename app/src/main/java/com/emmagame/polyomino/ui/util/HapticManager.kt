package com.emmagame.polyomino.ui.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

class HapticManager(private val context: Context) {

    var enabled = true

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val mgr = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            mgr?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun confirmPlacement(view: View?) {
        if (!enabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    fun rejectPlacement(view: View?) {
        if (!enabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            view?.performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            // Buzz-pause-buzz pattern
            vibrator?.let { v ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 50, 80, 50), -1
                        )
                    )
                }
            }
        }
    }

    fun tick(view: View?) {
        if (!enabled) return
        view?.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    fun click(view: View?) {
        if (!enabled) return
        view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
}
