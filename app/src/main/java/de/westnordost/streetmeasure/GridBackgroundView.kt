package de.westnordost.streetmeasure

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class GridBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    
    private val linePaint = Paint().apply {
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }
    
    private var spacingPx = 32f * resources.displayMetrics.density
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Determine grid color based on current theme
        val gridColor = if (androidx.core.content.ContextCompat.getColor(context, R.color.bg_primary) == 0xFF0E1117.toInt()) {
            // Dark mode
            androidx.core.content.ContextCompat.getColor(context, R.color.gridLineDarkMode)
        } else {
            // Light mode
            androidx.core.content.ContextCompat.getColor(context, R.color.gridLineLightMode)
        }
        linePaint.color = gridColor
        
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
            .start()
    }
    
    fun fadeOut() {
        animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction { visibility = GONE }
            .start()
    }
    
    fun setEnabledState(enabled: Boolean) {
        if (enabled) {
            fadeIn()
        } else {
            fadeOut()
        }
        savePreference(enabled)
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
    }
}
