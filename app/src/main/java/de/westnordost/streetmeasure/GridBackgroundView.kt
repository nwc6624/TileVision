package de.westnordost.streetmeasure

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat

class GridBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    
    private var lineAlpha = 0.15f           // animated 0.10 → 0.18 → 0.10
    private var sweepOffset = 0f

    private var pulseAnimator: ValueAnimator? = null
    private var sweepAnimator: ValueAnimator? = null

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sweepPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var lineColorBase = 0
    private var sweepColorBase = 0

    private var spacingPx = 0f
    private var gridEnabled = false

    init {
        setWillNotDraw(false)

        // convert 32dp to px once
        spacingPx = (32f * resources.displayMetrics.density)

        resolveThemeColors()
    }

    private fun resolveThemeColors() {
        // theme-aware colors
        val isNight = (resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

        if (isNight) {
            lineColorBase = ContextCompat.getColor(context, R.color.gridLineDarkMode)
            sweepColorBase = ContextCompat.getColor(context, R.color.gridLineDarkMode)
        } else {
            lineColorBase = ContextCompat.getColor(context, R.color.gridLineLightMode)
            sweepColorBase = ContextCompat.getColor(context, R.color.gridLineLightMode)
        }

        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = resources.displayMetrics.density * 1f // 1dp-ish

        sweepPaint.style = Paint.Style.FILL
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // whenever size changes (first layout, rotation, etc), rebuild animators
        rebuildAnimators()
        if (gridEnabled) {
            startAnimators()
        }
    }

    private fun rebuildAnimators() {
        pulseAnimator?.cancel()
        sweepAnimator?.cancel()

        // pulseAnimator: lineAlpha breathing
        pulseAnimator = ValueAnimator.ofFloat(0.10f, 0.18f, 0.10f).apply {
            duration = 3000L
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { va ->
                lineAlpha = (va.animatedValue as Float).coerceIn(0.05f, 0.25f)
                invalidate()
            }
        }

        // sweepAnimator: diagonal shimmer motion
        // if width is 0 somehow, guard to something > 0 so animator still runs
        val travel = (width.takeIf { it > 0 } ?: 1) * 2f
        sweepAnimator = ValueAnimator.ofFloat(0f, travel).apply {
            duration = 6000L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { va ->
                sweepOffset = va.animatedValue as Float
                invalidate()
            }
        }
    }

    private fun startAnimators() {
        // start only if not already running
        if (pulseAnimator?.isRunning != true) pulseAnimator?.start()
        if (sweepAnimator?.isRunning != true) sweepAnimator?.start()
    }

    private fun stopAnimators() {
        pulseAnimator?.cancel()
        sweepAnimator?.cancel()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        resolveThemeColors()
        if (gridEnabled) {
            // after attach we may still not know size, but onSizeChanged will fire soon
            // so just ensure visibility state is correct
            alpha = 1f
        } else {
            alpha = 0f
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimators()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!gridEnabled) return
        if (width == 0 || height == 0) return

        // draw grid lines
        val lineA = (lineAlpha * 255).toInt().coerceIn(0, 255)
        linePaint.color = (lineColorBase and 0x00FFFFFF) or (lineA shl 24)

        var x = 0f
        while (x <= width.toFloat()) {
            canvas.drawLine(x, 0f, x, height.toFloat(), linePaint)
            x += spacingPx
        }

        var y = 0f
        while (y <= height.toFloat()) {
            canvas.drawLine(0f, y, width.toFloat(), y, linePaint)
            y += spacingPx
        }

        // draw diagonal sweep
        val sweepAlpha = (0.08f * 255).toInt().coerceIn(0, 255)
        val sweepColor = (sweepColorBase and 0x00FFFFFF) or (sweepAlpha shl 24)

        val shader = LinearGradient(
            -width.toFloat() + sweepOffset,
            0f,
            sweepOffset,
            height.toFloat(),
            intArrayOf(Color.TRANSPARENT, sweepColor, Color.TRANSPARENT),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        sweepPaint.shader = shader

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), sweepPaint)
    }

    // -------- public API we call from Activities / header / settings --------

    fun applyInitialEnabledState(ctx: Context) {
        gridEnabled = getPref(ctx, "grid_enabled", true)
        if (gridEnabled) {
            // instantly visible (no flicker at Activity start)
            alpha = 1f
            visibility = VISIBLE
            if (width > 0 && height > 0) {
                startAnimators()
            }
        } else {
            alpha = 0f
            visibility = GONE
            stopAnimators()
        }
    }

    fun toggleGrid(ctx: Context) {
        val newState = !gridEnabled
        setGridEnabled(ctx, newState)
    }

    fun setGridEnabled(ctx: Context, enabled: Boolean) {
        gridEnabled = enabled
        savePref(ctx, "grid_enabled", enabled)

        if (enabled) {
            // fade in, then start animators
            visibility = VISIBLE
            animate()
                .alpha(1f)
                .setDuration(300L)
                .withEndAction {
                    if (width > 0 && height > 0) {
                        startAnimators()
                    }
                }
                .start()
        } else {
            // fade out, then stop animators + hide
            animate()
                .alpha(0f)
                .setDuration(300L)
                .withEndAction {
                    stopAnimators()
                    visibility = GONE
                }
                .start()
        }
    }

    companion object {
        private fun prefs(ctx: Context) =
            ctx.getSharedPreferences("tilevision_prefs", Context.MODE_PRIVATE)

        private fun savePref(ctx: Context, key: String, value: Boolean) {
            prefs(ctx).edit().putBoolean(key, value).apply()
        }

        private fun getPref(ctx: Context, key: String, def: Boolean): Boolean {
            return prefs(ctx).getBoolean(key, def)
        }

        fun isEnabled(ctx: Context): Boolean {
            return getPref(ctx, "grid_enabled", true)
        }
    }
}
