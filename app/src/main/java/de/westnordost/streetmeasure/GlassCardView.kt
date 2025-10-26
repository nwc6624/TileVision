package de.westnordost.streetmeasure

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat

class GlassCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var showGlowIcon: Boolean = false
    private var glowIconSize: Float = 0f
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        background = ContextCompat.getDrawable(context, R.drawable.glass_card_background)
        glowIconSize = 40f * resources.displayMetrics.density
    }

    fun setShowGlowIcon(show: Boolean) {
        showGlowIcon = show
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (showGlowIcon && canvas != null) {
            val iconX = glowIconSize / 2 + paddingStart
            val iconY = glowIconSize / 2 + paddingTop

            // Draw radial gradient glow
            val gradient = RadialGradient(
                iconX, iconY, glowIconSize / 2,
                intArrayOf(
                    ContextCompat.getColor(context, R.color.accent_teal),
                    ContextCompat.getColor(context, R.color.accent_teal_alpha_40),
                    android.graphics.Color.TRANSPARENT
                ),
                null,
                Shader.TileMode.CLAMP
            )
            glowPaint.shader = gradient
            canvas.drawCircle(iconX, iconY, glowIconSize / 2, glowPaint)
        }
    }
}
