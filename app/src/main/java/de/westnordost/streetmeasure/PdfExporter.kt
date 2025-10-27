package de.westnordost.streetmeasure

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfExporter {

    fun exportProjectSummaryToPdf(context: Context, summary: ProjectSummary): File {
        // Create a new document
        val document = PdfDocument()
        
        // Page size: 8.5 x 11 inches at 72 DPI = 612 x 792 pixels
        val pageInfo = PdfDocument.PageInfo.Builder(612, 792, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        
        var yPos = 72 // Start 1 inch from top
        val margin = 72 // 1 inch margins
        
        // Title Paint (bold, 20sp equivalent)
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 20f
            isAntiAlias = true
        }
        
        // Label Paint (semi-bold, 14sp)
        val labelPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 14f
            isAntiAlias = true
        }
        
        // Value Paint (regular, 14sp)
        val valuePaint = Paint().apply {
            textSize = 14f
            isAntiAlias = true
        }
        
        // Footer Paint (small, gray)
        val footerPaint = Paint().apply {
            textSize = 10f
            color = android.graphics.Color.GRAY
            isAntiAlias = true
        }
        
        // Header
        canvas.drawText("TileVision AR", margin.toFloat(), yPos.toFloat(), titlePaint)
        yPos += 28
        
        canvas.drawText(summary.displayName, margin.toFloat(), yPos.toFloat(), labelPaint)
        yPos += 20
        
        val dateFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.US)
        val dateStr = dateFormat.format(Date(summary.timestamp))
        canvas.drawText(dateStr, margin.toFloat(), yPos.toFloat(), valuePaint)
        yPos += 30
        
        // Divider
        canvas.drawLine(margin.toFloat(), yPos.toFloat(), 612f - margin, yPos.toFloat(), Paint().apply {
            color = android.graphics.Color.LTGRAY
            strokeWidth = 2f
        })
        yPos += 24
        
        // Project Area
        yPos = drawLabelValue(canvas, "Project Area", "${String.format("%.1f", summary.areaSqFt)} ft²", 
            yPos, margin, labelPaint, valuePaint)
        
        // Layout Style
        yPos = drawLabelValue(canvas, "Layout Style", summary.layoutStyle, 
            yPos, margin, labelPaint, valuePaint)
        
        // Grout Gap
        val groutStr = formatGroutGap(summary.groutGapInches)
        yPos = drawLabelValue(canvas, "Grout Gap", groutStr, 
            yPos, margin, labelPaint, valuePaint)
        
        yPos += 12
        
        // Tile Size
        yPos = drawLabelValue(canvas, "Tile Size", 
            "${String.format("%.1f", summary.tileWidthIn)} in × ${String.format("%.1f", summary.tileHeightIn)} in", 
            yPos, margin, labelPaint, valuePaint)
        
        // Tile Coverage
        yPos = drawLabelValue(canvas, "Tile Coverage", "${String.format("%.2f", summary.tileAreaSqFt)} ft² per tile", 
            yPos, margin, labelPaint, valuePaint)
        
        yPos += 12
        
        // Raw Tiles
        yPos = drawLabelValue(canvas, "Raw Tiles", 
            "${String.format("%.1f", summary.totalTilesNeededRaw)} tiles (before waste)", 
            yPos, margin, labelPaint, valuePaint)
        
        // Waste
        yPos = drawLabelValue(canvas, "Waste", "${String.format("%.0f", summary.wastePercent)}%", 
            yPos, margin, labelPaint, valuePaint)
        
        // Total Tiles
        val tilesPaint = Paint().apply {
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        yPos = drawLabelValue(canvas, "Total Tiles", "${summary.totalTilesNeededFinal} tiles (rounded)", 
            yPos, margin, labelPaint, tilesPaint)
        
        // Boxes Needed (if present)
        if (summary.boxesNeeded != null) {
            yPos = drawLabelValue(canvas, "Boxes Needed", String.format("%.1f boxes", summary.boxesNeeded), 
                yPos, margin, labelPaint, valuePaint)
        }
        
        yPos += 12
        
        // Notes
        val notes = summary.notes ?: "—"
        canvas.drawText("Notes", margin.toFloat(), yPos.toFloat(), labelPaint)
        yPos += 18
        
        // Wrap long notes
        val notesLines = wrapText(notes, valuePaint, 612 - (2 * margin))
        for (line in notesLines) {
            canvas.drawText(line, margin.toFloat(), yPos.toFloat(), valuePaint)
            yPos += 18
        }
        
        // Footer
        yPos = 792 - 36 // 0.5 inch from bottom
        canvas.drawText("Generated by TileVision AR", margin.toFloat(), yPos.toFloat(), footerPaint)
        
        // Finish the page
        document.finishPage(page)
        
        // Write to file
        val fileName = "TileVision_${sanitizeFileName(summary.displayName)}_${summary.timestamp}.pdf"
        val file = File(context.cacheDir, fileName)
        document.writeTo(FileOutputStream(file))
        document.close()
        
        return file
    }
    
    private fun drawLabelValue(canvas: android.graphics.Canvas, label: String, value: String, 
                               yPos: Int, margin: Int, labelPaint: Paint, valuePaint: Paint): Int {
        canvas.drawText(label + ":", margin.toFloat(), yPos.toFloat(), labelPaint)
        canvas.drawText(value, margin.toFloat() + 140, yPos.toFloat(), valuePaint)
        return yPos + 20
    }
    
    private fun formatGroutGap(inches: Float): String {
        return when (inches) {
            0.0625f -> "1/16 in"
            0.125f -> "1/8 in"
            0.1875f -> "3/16 in"
            0.25f -> "1/4 in"
            else -> String.format("%.3f", inches)
        }
    }
    
    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9-]"), "_")
    }
    
    private fun wrapText(text: String, paint: Paint, maxWidth: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                }
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        
        return lines
    }
}
