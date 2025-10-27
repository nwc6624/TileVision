package de.westnordost.streetmeasure

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator

class GridBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    
    // State
    private var lineAlpha = 0.15f
    private var sweepOffset = 0f
    
    // Animators
    private val pulseAnimator = ValueAnimator.ofFloat(0.10f, 0.18f, 0.10f).apply {
        duration = 3000L
        repeatCount = ValueAnimator.INFINITE
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener {
            lineAlpha = it.animatedValue as Float
            invalidate()
        }
    }
    
    private val sweepAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 6000L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            sweepOffset = it.animatedValue as Float
            invalidate()
        }
    }
    
    // Paints
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }
    
    private val sweepPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    // Colors (resolved from theme)
    private var lineColor: Int = Color.argb(51, 51, 51, 51) // Default gray fallback
    private var sweepColor: Int = Color.argb(20, 226, 184, 26) // Default teal fallback
    
    private var spacingPx = 32f * resources.displayMetrics.density
    
    init {
        resolveColorsForTheme()
    }
    
    private fun resolveColorsForTheme() {
        // Determine if dark mode by checking bg_primary color
        val isDarkMode = androidx.core.content.ContextCompat.getColor(context, R.color.bg_primary) == 0xFF0E1117.toInt()
        
        if (isDarkMode) {
            // Dark mode: teal-ish lines
            val baseColor = androidx.core.content.ContextCompat.getColor(context, R.color.gridLineDarkMode)
            lineColor = baseColor
            // Sweep: same color but with lower alpha
            val alpha = (android.graphics.Color.alpha(baseColor) * 0.3f).toInt()
            sweepColor = android.graphics.Color.argb(
                alpha,
                android.graphics.Color.red(baseColor),
                android.graphics.Color.green(baseColor),
                android.graphics.Color.blue(baseColor)
            )
        } else {
            // Light mode: darker gray/teal lines
            val baseColor = androidx.core.content.ContextCompat.getColor(context, R.color.gridLineLightMode)
            lineColor = baseColor
            // Sweep: same color but with lower alpha
            val alpha = (android.graphics.Color.alpha(baseColor) * 0.3f).toInt()
            sweepColor = android.graphics.Color.argb(
                alpha,
                android.graphics.Color.red(baseColor),
                android.graphics.Color.green(baseColor),
                android.graphics.Color.blue(baseColor)
            )
        }
    }
    
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Resolve colors again in case theme changed
        resolveColorsForTheme()
        // Start animations if visible
        if (visibility == VISIBLE) {
            startAnimIfVisible()
        }
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnim()
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Restart sweep animator with new range
        if (sweepAnimator.isRunning) {
            sweepAnimator.cancel()
            sweepAnimator.setFloatValues(0f, w.toFloat() * 2f)
            sweepAnimator.start()
        }
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()
        
        // Draw grid lines with current pulse alpha
        val alpha = (android.graphics.Color.alpha(lineColor) * lineAlpha).toInt()
        linePaint.color = android.graphics.Color.argb(
            alpha,
            android.graphics.Color.red(lineColor),
            android.graphics.Color.green(lineColor),
            android.graphics.Color.blue(lineColor)
        )
        
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
        
        // Draw diagonal sweep
        val currentSweepOffset = sweepOffset * width * 2f
        val shader = LinearGradient(
            -width + currentSweepOffset,
            0f,
            currentSweepOffset,
            height,
            intArrayOf(Color.TRANSPARENT, sweepColor, Color.TRANSPARENT),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        sweepPaint.shader = shader
        sweepPaint.alpha = (0.08f * 255).toInt()
        
        canvas.drawRect(0f, 0f, width, height, sweepPaint)
    }
    
    override fun setVisibility(visibility: Int) {
        when (visibility) {
            VISIBLE -> {
                alpha = 0f
                super.setVisibility(VISIBLE)
                animate()
                    .alpha(1f)
                    .setDuration(300)
                    .withEndAction { startAnimIfVisible() }
                    .start()
            }
            GONE, INVISIBLE -> {
                stopAnim()
                animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction { super.setVisibility(GONE) }
                    .start()
            }
            else -> super.setVisibility(visibility)
        }
    }
    
    fun startAnimIfVisible() {
        if (visibility == VISIBLE && width > 0 && height > 0) {
            // Update sweep animator range to current width
            sweepAnimator.setFloatValues(0f, width.toFloat() * 2f)
            pulseAnimator.start()
            sweepAnimator.start()
        }
    }
    
    fun stopAnim() {
        pulseAnimator.cancel()
        sweepAnimator.cancel()
    }
    
    fun setGridEnabled(ctx: Context, enabled: Boolean) {
        savePref(ctx, "grid_enabled", enabled)
        if (enabled) {
            if (visibility != VISIBLE) {
                visibility = VISIBLE
            } else {
                startAnimIfVisible()
            }
        } else {
            // Triggers fade out and stopAnim via setVisibility(GONE)
            visibility = GONE
        }
    }
    
    private fun savePref(context: Context, key: String, value: Boolean) {
        val prefs = context.getSharedPreferences("tilevision", Context.MODE_PRIVATE)
        prefs.edit().putBoolean(key, value).apply()
    }
    
    // Legacy methods for compatibility
    fun fadeIn() {
        visibility = VISIBLE
    }
    
    fun fadeOut() {
        visibility = GONE
    }
    
    fun setEnabledState(enabled: Boolean, saveToPreferences: Boolean = true) {
        if (saveToPreferences) {
            savePref(context, "grid_enabled", enabled)
        }
        setGridEnabled(context, enabled)
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
