package de.westnordost.streetmeasure

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class GridBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    
    private val linePaint = Paint().apply {
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }
    
    private var spacingPx = 32f * resources.displayMetrics.density
    private var pulseAlpha = 1.0f
    private var pulseAnimator: ValueAnimator? = null
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Determine grid color based on current theme
        val baseColor = if (androidx.core.content.ContextCompat.getColor(context, R.color.bg_primary) == 0xFF0E1117.toInt()) {
            // Dark mode
            androidx.core.content.ContextCompat.getColor(context, R.color.gridLineDarkMode)
        } else {
            // Light mode
            androidx.core.content.ContextCompat.getColor(context, R.color.gridLineLightMode)
        }
        
        // Apply pulse alpha to create energy effect
        val alpha = (android.graphics.Color.alpha(baseColor) * pulseAlpha).toInt()
        linePaint.color = android.graphics.Color.argb(
            alpha,
            android.graphics.Color.red(baseColor),
            android.graphics.Color.green(baseColor),
            android.graphics.Color.blue(baseColor)
        )
        
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()
        
        // Draw vertical lines
        var x = 0f
        while (x <= width) {
            canvas.drawLine(x, 0f, x, height, linePaint)
            x += spacingPx
        }
        
        // Draw horizontal lines
        var y = 0f
        while (y <= height) {
            canvas.drawLine(0f, y, width, y, linePaint)
            y += spacingPx
        }
    }
    
    fun fadeIn() {
        alpha = 0f
        visibility = VISIBLE
        animate()
            .alpha(1f)
            .setDuration(300)
            .withEndAction {
                startPulseAnimation()
            }
            .start()
    }
    
    fun fadeOut() {
        stopPulseAnimation()
        animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction { visibility = GONE }
            .start()
    }
    
    private fun startPulseAnimation() {
        stopPulseAnimation()
        
        // More dramatic pulse: from 15% to 80% opacity
        pulseAnimator = ValueAnimator.ofFloat(0.15f, 0.80f).apply {
            duration = 1500L  // Faster pulse for more energy
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                pulseAlpha = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }
    
    private fun stopPulseAnimation() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        pulseAlpha = 1.0f
    }
    
    fun setEnabledState(enabled: Boolean, saveToPreferences: Boolean = true) {
        if (enabled) {
            fadeIn()
        } else {
            stopPulseAnimation()
            fadeOut()
        }
        if (saveToPreferences) {
            savePreference(enabled)
        }
    }
    
    private fun savePreference(enabled: Boolean) {
        val prefs = context.getSharedPreferences("tilevision", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("grid_enabled", enabled).apply()
    }
    
    companion object {
        fun isEnabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences("tilevision", Context.MODE_PRIVATE)
            return prefs.getBoolean("grid_enabled", false)
        }
        
        fun setEnabledState(context: Context, enabled: Boolean) {
            val prefs = context.getSharedPreferences("tilevision", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("grid_enabled", enabled).apply()
        }
    }
}
