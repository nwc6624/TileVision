package de.westnordost.streetmeasure

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class GridBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    
    private data class Bolt(
        var isHorizontal: Boolean,
        var lineIndex: Int,      // which grid row/column
        var headPos: Float,      // px along travel axis
        var speedPxPerSec: Float,
        var lengthPx: Float
    )
    
    companion object {
        private const val MAX_BOLTS = 5
        private const val TWO_PI = 2f * PI.toFloat()
        
        fun isEnabled(ctx: Context): Boolean {
            val prefs = ctx.getSharedPreferences("tilevision_prefs", Context.MODE_PRIVATE)
            return prefs.getBoolean("grid_enabled", true)
        }
    }
    
    // Fixed-size bolt pool
    private val bolts = MutableList(MAX_BOLTS) { Bolt(false, 0, 0f, 0f, 0f) }
    private var boltsInitialized = false
    
    // Animation state
    private var lineAlpha = 0.14f
    private var pulsePhase = 0f
    private var monochromeMode: Boolean = false
    
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val boltPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val nodePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var baseLineColor = 0
    private var boltColor = 0
    private var nodeColor = 0
    
    private var spacingPx = 0f
    private var gridEnabled = false
    private var frameAnimator: ValueAnimator? = null
    private var lastFrameTimeNs: Long = 0L
    private var density = 0f
    private var isAttached = false

    init {
        setWillNotDraw(false)
        density = resources.displayMetrics.density
        spacingPx = 32f * density
        resolveThemeColors()
        setupPaints()
    }

    private fun resolveThemeColors() {
        val isNight = (resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

        if (isNight) {
            // Dark mode: bright teal energy
            baseLineColor = ContextCompat.getColor(context, R.color.gridLineDarkMode)
            boltColor = ContextCompat.getColor(context, R.color.accent_teal)
            // Node: whitish-teal glow
            val teal = Color.rgb(0, 184, 178)
            val white = Color.rgb(255, 255, 255)
            nodeColor = Color.rgb(
                (Color.red(teal) + Color.red(white)) / 2,
                (Color.green(teal) + Color.green(white)) / 2,
                (Color.blue(teal) + Color.blue(white)) / 2
            )
        } else {
            // Light mode: darker teal/slate
            baseLineColor = ContextCompat.getColor(context, R.color.gridLineLightMode)
            // Darker teal for visibility on light bg
            boltColor = Color.rgb(0, 100, 100)
            // Node: slightly lighter
            nodeColor = Color.rgb(0, 120, 120)
        }
    }
    
    private fun setupPaints() {
        // Base grid lines
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = density * 1f
        
        // Bold bolts
        boltPaint.style = Paint.Style.STROKE
        boltPaint.strokeWidth = density * 2f
        boltPaint.strokeCap = Paint.Cap.ROUND
        boltPaint.alpha = (0.8f * 255).toInt()
        
        // Node glow
        nodePaint.style = Paint.Style.FILL
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Reset bolts initialization when size changes
        boltsInitialized = false
    }

    private fun dpToPx(dp: Int): Float = dp * resources.displayMetrics.density
    
    private fun initBoltsIfNeeded() {
        if (boltsInitialized) return
        boltsInitialized = true
        
        for (bolt in bolts) {
            resetBolt(bolt)
        }
    }
    
    private fun resetBolt(bolt: Bolt) {
        bolt.isHorizontal = Random.nextBoolean()
        val travel = if (bolt.isHorizontal) height.toFloat() else width.toFloat()
        
        if (travel <= 0f) return // No space yet
        
        val maxLines = (travel / spacingPx).toInt().coerceAtLeast(1)
        bolt.lineIndex = Random.nextInt(1, maxLines).coerceAtLeast(0)
        bolt.lengthPx = travel * 0.25f
        bolt.speedPxPerSec = dpToPx(Random.nextInt(200, 400))
        bolt.headPos = -bolt.lengthPx
    }

    private fun ensureFrameAnimator() {
        if (frameAnimator != null) return

        frameAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2000L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                val now = System.nanoTime()
                if (lastFrameTimeNs == 0L) {
                    lastFrameTimeNs = now
                }
                val dtSec = (now - lastFrameTimeNs) / 1_000_000_000f
                lastFrameTimeNs = now

                // update bolts + alpha pulse
                updateBolts(dtSec)
                updatePulse(dtSec)

                // request redraw
                invalidate()
            }
        }
    }

    private fun updateBolts(dtSec: Float) {
        if (width <= 0 || height <= 0) return
        
        for (bolt in bolts) {
            bolt.headPos += bolt.speedPxPerSec * dtSec
            val limit = if (bolt.isHorizontal) width.toFloat() else height.toFloat()
            if (bolt.headPos - bolt.lengthPx > limit) {
                resetBolt(bolt)
            }
        }
    }

    private fun updatePulse(dtSec: Float) {
        // 3s cycle
        pulsePhase += dtSec
        val cycle = 3f
        val t = (pulsePhase % cycle) / cycle // 0..1
        // sin wave 0→1→0 style
        val wave = kotlin.math.sin(t * (PI * 2)).toFloat() * 0.5f + 0.5f // 0..1
        val base = 0.12f
        val amp = 0.08f
        lineAlpha = (base + wave * amp).coerceIn(0.08f, 0.22f)
    }

    private fun startAnimators() {
        if (!isAttached || !gridEnabled) return
        if (width <= 0 || height <= 0) return
        if (frameAnimator?.isRunning == true) return
        
        initBoltsIfNeeded()
        ensureFrameAnimator()
        lastFrameTimeNs = 0L
        frameAnimator?.start()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isAttached = true
        resolveThemeColors()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isAttached = false
        stopAnimators()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility != VISIBLE) {
            stopAnimators()
        } else {
            if (gridEnabled && width > 0 && height > 0) {
                startAnimators()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!gridEnabled || width == 0 || height == 0) return

        if (monochromeMode) {
            // Monochrome mode: simple wireframe grid lines only
            val lineA = (lineAlpha * 255).toInt().coerceIn(0, 255)
            linePaint.color = (Color.parseColor("#3355FFFF") and 0x00FFFFFF) or (lineA shl 24)

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
        } else {
            // Normal mode: animated teal pulses and glow nodes
            // 1. Draw base grid lines
            val lineA = (lineAlpha * 255).toInt().coerceIn(0, 255)
            linePaint.color = (baseLineColor and 0x00FFFFFF) or (lineA shl 24)

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

            // 2. Draw bolts with try-catch safety
            try {
                boltPaint.color = boltColor
                for (bolt in bolts) {
                    if (bolt.isHorizontal) {
                        val yPos = (bolt.lineIndex * spacingPx).coerceIn(0f, height.toFloat())
                        val startX = bolt.headPos - bolt.lengthPx
                        val endX = bolt.headPos
                        val clampedStart = max(0f, startX).coerceAtMost(width.toFloat())
                        val clampedEnd = max(0f, endX).coerceAtMost(width.toFloat())
                        
                        if (clampedStart < clampedEnd) {
                            canvas.drawLine(clampedStart, yPos, clampedEnd, yPos, boltPaint)
                            drawNodeGlow(canvas, endX, yPos)
                        }
                    } else {
                        val xPos = (bolt.lineIndex * spacingPx).coerceIn(0f, width.toFloat())
                        val startY = bolt.headPos - bolt.lengthPx
                        val endY = bolt.headPos
                        val clampedStart = max(0f, startY).coerceAtMost(height.toFloat())
                        val clampedEnd = max(0f, endY).coerceAtMost(height.toFloat())
                        
                        if (clampedStart < clampedEnd) {
                            canvas.drawLine(xPos, clampedStart, xPos, clampedEnd, boltPaint)
                            drawNodeGlow(canvas, xPos, endY)
                        }
                    }
                }
            } catch (e: Exception) {
                // Swallow to avoid crash during transitions
            }
        }
    }
    
    private fun drawNodeGlow(canvas: Canvas, cx: Float, cy: Float) {
        if (cx < 0f || cy < 0f || cx > width || cy > height) return
        
        try {
            // Core dot
            nodePaint.alpha = (0.6f * 255).toInt()
            nodePaint.color = nodeColor
            canvas.drawCircle(cx, cy, 4f * density, nodePaint)
            
            // Halo
            nodePaint.alpha = (0.18f * 255).toInt()
            canvas.drawCircle(cx, cy, 10f * density, nodePaint)
        } catch (e: Exception) {
            // Swallow drawing errors
        }
    }

    // -------- public API --------

    fun applyInitialEnabledState(ctx: Context) {
        gridEnabled = getPref(ctx, "grid_enabled", true)
        if (gridEnabled) {
            alpha = 1f
            visibility = VISIBLE
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
    
    fun setMonochrome(enabled: Boolean) {
        monochromeMode = enabled
        invalidate()
    }
    
    // Public method for Activities to stop animators
    fun stopAnimators() {
        frameAnimator?.cancel()
        frameAnimator = null
        lastFrameTimeNs = 0L
    }
    
    // Private helper functions for this instance
    private fun savePref(ctx: Context, key: String, value: Boolean) {
        val prefs = ctx.getSharedPreferences("tilevision_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean(key, value).apply()
    }

    private fun getPref(ctx: Context, key: String, def: Boolean): Boolean {
        val prefs = ctx.getSharedPreferences("tilevision_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean(key, def)
    }
}
