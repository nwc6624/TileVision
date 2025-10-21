package com.tilevision.shared.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

actual class Haptics(private val context: Context) {
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    actual fun light() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }
    
    actual fun medium() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(100, (VibrationEffect.DEFAULT_AMPLITUDE * 0.7).toInt())
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }
    
    actual fun heavy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }
    }
    
    actual fun selection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(10L)
        }
    }
    
    actual fun impact(style: ImpactStyle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (style) {
                ImpactStyle.LIGHT -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                ImpactStyle.MEDIUM -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                ImpactStyle.HEAVY -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                ImpactStyle.RIGID -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                ImpactStyle.SOFT -> VibrationEffect.createOneShot(30, (VibrationEffect.DEFAULT_AMPLITUDE * 0.5).toInt())
            }
            vibrator.vibrate(effect)
        } else {
            val duration = when (style) {
                ImpactStyle.LIGHT -> 20
                ImpactStyle.MEDIUM -> 50
                ImpactStyle.HEAVY -> 100
                ImpactStyle.RIGID -> 30
                ImpactStyle.SOFT -> 15
            }
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }
    
    actual fun notification(type: NotificationType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (type) {
                NotificationType.SUCCESS -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                NotificationType.WARNING -> VibrationEffect.createWaveform(
                    longArrayOf(0, 100, 50, 100),
                    intArrayOf(0, 100, 0, 100),
                    -1
                )
                NotificationType.ERROR -> VibrationEffect.createWaveform(
                    longArrayOf(0, 200, 100, 200),
                    intArrayOf(0, 255, 0, 255),
                    -1
                )
            }
            vibrator.vibrate(effect)
        } else {
            val pattern = when (type) {
                NotificationType.SUCCESS -> longArrayOf(0, 50)
                NotificationType.WARNING -> longArrayOf(0, 100, 50, 100)
                NotificationType.ERROR -> longArrayOf(0, 200, 100, 200)
            }
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
    
    actual fun isSupported(): Boolean {
        return vibrator.hasVibrator()
    }
    
    actual fun isEnabled(): Boolean {
        // Note: This requires checking system settings which may not be accessible
        // In a real implementation, you might need to use a different approach
        return isSupported()
    }
}
