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
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
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
    
    private val bolts = mutableListOf<Bolt>()
    
    // Animation state
    private var lineAlpha = 0.14f
    private var pulsePhase = 0f
    
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
        bolts.clear()
        repeat(TARGET_NUM_BOLTS) { spawnBolt() }
        rebuildAnimator()
        if (gridEnabled) {
            startAnimators()
        }
    }

    private fun spawnBolt() {
        val isHorizontal = Random.nextBoolean()
        val numLines = if (isHorizontal) {
            (height / spacingPx).toInt()
        } else {
            (width / spacingPx).toInt()
        }
        
        if (numLines < 2) return  // Not enough space
        
        val lineIndex = Random.nextInt(1, numLines - 1)
        val travelDistance = if (isHorizontal) width.toFloat() else height.toFloat()
        val lengthPx = travelDistance * 0.25f
        val speedPxPerSec = Random.nextFloat() * (400f - 200f) * density + 200f * density
        
        bolts.add(
            Bolt(
                isHorizontal = isHorizontal,
                lineIndex = lineIndex,
                headPos = -lengthPx,
                speedPxPerSec = speedPxPerSec,
                lengthPx = lengthPx
            )
        )
    }

    private fun rebuildAnimator() {
        frameAnimator?.cancel()
        frameAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2000L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                val now = System.nanoTime()
                if (lastFrameTimeNs == 0L) lastFrameTimeNs = now
                val dtSec = (now - lastFrameTimeNs) / 1_000_000_000f
                lastFrameTimeNs = now

                updateBolts(dtSec)
                updatePulseAlpha(dtSec)
                invalidate()
            }
        }
    }

    private fun updateBolts(dtSec: Float) {
        val iter = bolts.iterator()
        while (iter.hasNext()) {
            val bolt = iter.next()
            bolt.headPos += bolt.speedPxPerSec * dtSec
            
            val bounds = if (bolt.isHorizontal) width.toFloat() else height.toFloat()
            if (bolt.headPos - bolt.lengthPx > bounds) {
                // Respawn this bolt
                iter.remove()
                spawnBolt()
            }
        }
        
        // Maintain target count
        while (bolts.size < TARGET_NUM_BOLTS) {
            spawnBolt()
        }
    }

    private fun updatePulseAlpha(dtSec: Float) {
        pulsePhase += dtSec
        // 3 second cycle
        val phase = (pulsePhase / 3f) * TWO_PI
        lineAlpha = (0.14f + sin(phase) * 0.04f).coerceIn(0.08f, 0.22f)
    }

    private fun startAnimators() {
        if (frameAnimator?.isRunning != true) {
            lastFrameTimeNs = 0L
            frameAnimator?.start()
        }
    }

    private fun stopAnimators() {
        frameAnimator?.cancel()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        resolveThemeColors()
        setupPaints()
        if (gridEnabled) {
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

        if (!gridEnabled || width == 0 || height == 0) return

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

        // 2. Draw bolts
        boltPaint.color = boltColor
        for (bolt in bolts) {
            if (bolt.isHorizontal) {
                val y = bolt.lineIndex * spacingPx
                val startX = bolt.headPos - bolt.lengthPx
                val endX = bolt.headPos
                val clampedStart = max(0f, startX)
                val clampedEnd = min(width.toFloat(), endX)
                
                if (clampedStart < clampedEnd) {
                    canvas.drawLine(clampedStart, y, clampedEnd, y, boltPaint)
                    drawNodeGlow(canvas, endX, y)
                }
            } else {
                val x = bolt.lineIndex * spacingPx
                val startY = bolt.headPos - bolt.lengthPx
                val endY = bolt.headPos
                val clampedStart = max(0f, startY)
                val clampedEnd = min(height.toFloat(), endY)
                
                if (clampedStart < clampedEnd) {
                    canvas.drawLine(x, clampedStart, x, clampedEnd, boltPaint)
                    drawNodeGlow(canvas, x, endY)
                }
            }
        }
    }
    
    private fun drawNodeGlow(canvas: Canvas, cx: Float, cy: Float) {
        if (cx < 0f || cy < 0f || cx > width || cy > height) return
        
        // Core dot
        nodePaint.alpha = (0.6f * 255).toInt()
        nodePaint.color = nodeColor
        canvas.drawCircle(cx, cy, 4f * density, nodePaint)
        
        // Halo
        nodePaint.alpha = (0.18f * 255).toInt()
        canvas.drawCircle(cx, cy, 10f * density, nodePaint)
    }

    // -------- public API --------

    fun applyInitialEnabledState(ctx: Context) {
        gridEnabled = getPref(ctx, "grid_enabled", true)
        if (gridEnabled) {
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

    // -------- companion object for static methods --------
    companion object {
        private const val TWO_PI = 2f * PI.toFloat()
        private const val TARGET_NUM_BOLTS = 5
        
        fun isEnabled(ctx: Context): Boolean {
            val prefs = ctx.getSharedPreferences("tilevision_prefs", Context.MODE_PRIVATE)
            return prefs.getBoolean("grid_enabled", true)
        }
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
