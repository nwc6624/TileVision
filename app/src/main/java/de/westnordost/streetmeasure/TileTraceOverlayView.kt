package de.westnordost.streetmeasure

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class TileTraceOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val strokePointsPx = mutableListOf<PointF>()
    var onStrokeComplete: ((List<PointF>) -> Unit)? = null
    var onStrokeStart: ((PointF) -> Unit)? = null
    
    private val paintTrace = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f * resources.displayMetrics.density
        color = Color.MAGENTA  // temp bright color to confirm it's drawing
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private var isDrawingStroke = false

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // start a new stroke
                strokePointsPx.clear()
                val startPoint = PointF(ev.x, ev.y)
                strokePointsPx.add(startPoint)
                isDrawingStroke = true
                invalidate()
                
                // Notify activity that stroke started
                onStrokeStart?.invoke(startPoint)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDrawingStroke) {
                    strokePointsPx.add(PointF(ev.x, ev.y))
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                if (isDrawingStroke) {
                    strokePointsPx.add(PointF(ev.x, ev.y))
                    isDrawingStroke = false
                    invalidate()

                    // Make a defensive copy BEFORE any clear
                    val finishedStroke = strokePointsPx.toList()

                    // Call callback so Activity can process
                    onStrokeComplete?.invoke(finishedStroke)
                }
                return true
            }
        }
        return super.onTouchEvent(ev)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (strokePointsPx.size < 2) return

        val path = Path()
        path.moveTo(strokePointsPx[0].x, strokePointsPx[0].y)
        for (i in 1 until strokePointsPx.size) {
            path.lineTo(strokePointsPx[i].x, strokePointsPx[i].y)
        }
        canvas.drawPath(path, paintTrace)
    }

    fun clearStroke() {
        strokePointsPx.clear()
        isDrawingStroke = false
        invalidate()
    }
    
    fun lockStrokeDisplay() {
        // keeps the current stroke visible (do nothing right now,
        // but exists so Activity can "freeze" UI if needed)
    }
    
    fun clearStrokeFromActivity() {
        strokePointsPx.clear()
        invalidate()
        isDrawingStroke = false
    }
}
