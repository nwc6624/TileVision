package de.westnordost.streetmeasure

import android.content.Context
import android.util.AttributeSet
import android.graphics.*
import android.view.View
import kotlin.math.*

class PolygonPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var polygonPoints: List<ProjectPoint2D> = emptyList()
    private var areaFt2: Float = 0f
    
    // Colors matching the AR overlay
    private val fillColor = Color.parseColor("#40FFA500") // Semi-transparent orange
    private val strokeColor = Color.parseColor("#FFA500") // Orange stroke
    private val textColor = Color.parseColor("#2D5A4A") // Dark green text
    private val backgroundColor = Color.parseColor("#F5F5F5") // Light gray background
    private val bubbleColor = Color.WHITE // White bubble background
    
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fillColor
        style = Paint.Style.FILL
    }
    
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = strokeColor
        style = Paint.Style.STROKE
        strokeWidth = 4f * resources.displayMetrics.density
    }
    
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = 16f * resources.displayMetrics.density
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    
    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = bubbleColor
        style = Paint.Style.FILL
    }
    
    fun setPolygonData(points: List<ProjectPoint2D>, area: Float) {
        polygonPoints = points
        areaFt2 = area
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw background
        canvas.drawColor(backgroundColor)
        
        // Early exit for invalid data
        if (polygonPoints.isEmpty() || polygonPoints.size < 3) {
            drawPlaceholder(canvas)
            return
        }
        
        // Project 3D points to 2D (using x,z coordinates, ignoring y)
        val pts2d = polygonPoints.map { p -> PointF(p.x, p.z) }
        
        // Compute bounds of those points
        val minX = pts2d.minOf { it.x }
        val maxX = pts2d.maxOf { it.x }
        val minY = pts2d.minOf { it.y }
        val maxY = pts2d.maxOf { it.y }
        
        val widthWorld = maxX - minX
        val heightWorld = maxY - minY
        
        // Handle edge cases where widthWorld or heightWorld == 0
        if (widthWorld <= 0 || heightWorld <= 0) {
            drawPlaceholder(canvas)
            return
        }
        
        // Normalize them to start at (0,0)
        val normPts = pts2d.map { PointF(it.x - minX, it.y - minY) }
        
        // Compute scale to fit view
        val paddingPx = 24f * resources.displayMetrics.density
        val availW = width - paddingPx * 2
        val availH = height - paddingPx * 2
        
        val scaleX = availW / widthWorld
        val scaleY = availH / heightWorld
        val scale = minOf(scaleX, scaleY)
        
        // Scale the normalized points
        val scaledPts = normPts.map { PointF(it.x * scale, it.y * scale) }
        
        // Compute polygon size after scaling
        val scaledMinX = scaledPts.minOf { it.x }
        val scaledMaxX = scaledPts.maxOf { it.x }
        val scaledMinY = scaledPts.minOf { it.y }
        val scaledMaxY = scaledPts.maxOf { it.y }
        
        val scaledWidth = scaledMaxX - scaledMinX
        val scaledHeight = scaledMaxY - scaledMinY
        
        // Compute offset to center in the view
        val offsetX = (width - scaledWidth) / 2f
        val offsetY = (height - scaledHeight) / 2f
        
        // Final screen points
        val screenPts = scaledPts.map { PointF(it.x + offsetX, it.y + offsetY) }
        
        // Create path for polygon
        val path = Path()
        path.moveTo(screenPts[0].x, screenPts[0].y)
        for (i in 1 until screenPts.size) {
            path.lineTo(screenPts[i].x, screenPts[i].y)
        }
        path.close()
        
        // Draw polygon fill
        canvas.drawPath(path, fillPaint)
        
        // Draw polygon stroke
        canvas.drawPath(path, strokePaint)
        
        // Draw area text at centroid with bubble background
        val centroid = calculateCentroid(screenPts)
        val areaText = "${String.format("%.2f", areaFt2)} ft²"
        drawTextWithBubble(canvas, areaText, centroid.x, centroid.y)
    }
    
    private fun calculateCentroid(points: List<PointF>): PointF {
        if (points.isEmpty()) return PointF(width / 2f, height / 2f)
        
        var sumX = 0f
        var sumY = 0f
        points.forEach { point ->
            sumX += point.x
            sumY += point.y
        }
        
        return PointF(sumX / points.size, sumY / points.size)
    }
    
    private fun drawTextWithBubble(canvas: Canvas, text: String, centerX: Float, centerY: Float) {
        // Measure text dimensions
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        
        // Add padding to bubble
        val paddingH = 8f * resources.displayMetrics.density
        val paddingV = 4f * resources.displayMetrics.density
        val bubbleWidth = textBounds.width() + paddingH * 2
        val bubbleHeight = textBounds.height() + paddingV * 2
        
        // Calculate bubble position (centered on text)
        val bubbleLeft = centerX - bubbleWidth / 2f
        val bubbleTop = centerY - bubbleHeight / 2f
        val bubbleRight = centerX + bubbleWidth / 2f
        val bubbleBottom = centerY + bubbleHeight / 2f
        
        // Draw rounded rectangle bubble
        val radius = 8f * resources.displayMetrics.density
        val bubbleRect = RectF(bubbleLeft, bubbleTop, bubbleRight, bubbleBottom)
        canvas.drawRoundRect(bubbleRect, radius, radius, bubblePaint)
        
        // Draw text centered in bubble
        val textY = centerY + textBounds.height() / 2f
        canvas.drawText(text, centerX, textY, textPaint)
    }
    
    private fun drawPlaceholder(canvas: Canvas) {
        // Draw a simple placeholder when no polygon data is available
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = minOf(width, height) / 4f
        
        // Draw circle outline
        canvas.drawCircle(centerX, centerY, radius, strokePaint)
        
        // Draw placeholder text
        val placeholderText = "No preview"
        drawTextWithBubble(canvas, placeholderText, centerX, centerY)
    }
}