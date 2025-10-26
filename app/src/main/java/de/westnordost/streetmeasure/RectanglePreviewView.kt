package de.westnordost.streetmeasure

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class RectanglePreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var widthInInches: Float = 0f
    private var heightInInches: Float = 0f
    private var areaSqFt: Float = 0f

    // Colors matching the AR overlay
    private val fillColor = Color.parseColor("#40FFA500") // Semi-transparent orange
    private val strokeColor = Color.parseColor("#FFA500") // Orange stroke
    private val textColor = Color.parseColor("#2D5A4A") // Dark green text
    private val backgroundColor = Color.parseColor("#F5F5F5") // Light gray background

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fillColor
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = strokeColor
        style = Paint.Style.STROKE
        strokeWidth = 4f * resources.displayMetrics.density // Density-aware stroke width
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = 12f * resources.displayMetrics.density // Density-aware text size
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val textBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }

    fun setTileData(width: Float, height: Float, area: Float) {
        widthInInches = width
        heightInInches = height
        areaSqFt = area
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw background
        canvas.drawColor(backgroundColor)

        if (widthInInches <= 0 || heightInInches <= 0) {
            drawPlaceholder(canvas)
            return
        }

        // Calculate scale to fit the rectangle in the view
        val paddingPx = 16f * resources.displayMetrics.density
        val availW = width - paddingPx * 2
        val availH = height - paddingPx * 2

        val scaleX = availW / widthInInches
        val scaleY = availH / heightInInches
        val scale = min(scaleX, scaleY)

        val scaledWidth = widthInInches * scale
        val scaledHeight = heightInInches * scale

        // Center the rectangle
        val left = (width - scaledWidth) / 2f
        val top = (height - scaledHeight) / 2f
        val right = left + scaledWidth
        val bottom = top + scaledHeight

        val rect = RectF(left, top, right, bottom)

        // Draw rectangle fill
        canvas.drawRect(rect, fillPaint)

        // Draw rectangle stroke
        canvas.drawRect(rect, strokePaint)

        // Draw dimensions text
        val centerX = rect.centerX()
        val centerY = rect.centerY()
        val dimensionsText = "${String.format("%.1f", widthInInches)}\" × ${String.format("%.1f", heightInInches)}\""
        val areaText = "${String.format("%.2f", areaSqFt)} ft²"

        drawTextWithBubble(canvas, dimensionsText, centerX, centerY - 10f)
        drawTextWithBubble(canvas, areaText, centerX, centerY + 10f)
    }

    private fun drawTextWithBubble(canvas: Canvas, text: String, centerX: Float, centerY: Float) {
        val textWidth = textPaint.measureText(text)
        val textHeight = textPaint.descent() - textPaint.ascent()

        val hPadding = 6f * resources.displayMetrics.density
        val vPadding = 3f * resources.displayMetrics.density
        val cornerRadius = 6f * resources.displayMetrics.density

        val rectLeft = centerX - textWidth / 2f - hPadding
        val rectTop = centerY + textPaint.ascent() - vPadding
        val rectRight = centerX + textWidth / 2f + hPadding
        val rectBottom = centerY + textPaint.descent() + vPadding

        canvas.drawRoundRect(rectLeft, rectTop, rectRight, rectBottom, cornerRadius, cornerRadius, textBackgroundPaint)
        canvas.drawText(text, centerX, centerY, textPaint)
    }

    private fun drawPlaceholder(canvas: Canvas) {
        val centerX = width / 2f
        val centerY = height / 2f

        textPaint.color = textColor
        textPaint.textSize = 12f * resources.displayMetrics.density
        canvas.drawText("No preview", centerX, centerY, textPaint)
    }
}
