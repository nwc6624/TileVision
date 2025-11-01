package com.tilevision.export

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import de.westnordost.streetmeasure.ProjectSummary
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfExporter {

    // Generate a PDF for a single ProjectSummary and return a sharable Uri.
    // We'll store it under cacheDir so it's temporary and doesn't need special storage perms.
    fun createSummaryPdf(context: Context, summary: ProjectSummary): Uri? {
        try {
            // 1. Prep
            val pageWidth = 595  // A4-ish in points (72 dpi) ~8.27in
            val pageHeight = 842 // ~11.69in
            val pdfDoc = PdfDocument()

            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDoc.startPage(pageInfo)
            val canvas = page.canvas

            // Colors & Paint styles
            val bgPaint = Paint().apply {
                color = Color.BLACK
                style = Paint.Style.FILL
            }

            val titlePaint = Paint().apply {
                color = Color.WHITE
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                isAntiAlias = true
            }

            val headerPaint = Paint().apply {
                color = Color.parseColor("#18FFC4") // teal accent
                textSize = 14f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                isAntiAlias = true
            }

            val bodyPaint = Paint().apply {
                color = Color.WHITE
                textSize = 14f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                isAntiAlias = true
            }

            val smallPaint = Paint().apply {
                color = Color.LTGRAY
                textSize = 11f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
                isAntiAlias = true
            }

            // black background
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

            // 2. Header badge / logo circle
            // We'll draw a glowing teal ring with a purple tile grid vibe instead of embedding bitmap for now
            val cx = 60f
            val cy = 60f
            val outerRingPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 8f
                isAntiAlias = true
                color = Color.parseColor("#18FFC4")
                maskFilter = BlurMaskFilter(12f, BlurMaskFilter.Blur.SOLID)
            }
            canvas.drawCircle(cx, cy, 28f, outerRingPaint)

            // 3. Title text ("TileVision AR")
            var y = 40f
            val xTitle = 110f
            canvas.drawText("TileVision AR", xTitle, y, titlePaint)

            // Subtitle tagline
            y += 20f
            canvas.drawText("Measure. Visualize. Estimate.", xTitle, y, bodyPaint)

            // Timestamp
            val formatter = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault())
            val tsString = formatter.format(Date(summary.timestamp))
            y += 20f
            canvas.drawText(tsString, xTitle, y, smallPaint)

            // 4. Section: Project
            y += 40f
            canvas.drawText("PROJECT", 40f, y, headerPaint)
            y += 20f
            canvas.drawText("Name: ${summary.displayName}", 40f, y, bodyPaint)
            y += 20f
            canvas.drawText("Units: ${summary.unitsSystem}", 40f, y, bodyPaint)

            // 5. Section: Area + tile info
            y += 30f
            canvas.drawText("MEASUREMENT", 40f, y, headerPaint)

            y += 20f
            canvas.drawText(
                "Area: ${summary.areaValue} ${summary.areaUnit}",
                40f,
                y,
                bodyPaint
            )

            y += 20f
            canvas.drawText(
                "Tile Size: ${summary.tileWidth} × ${summary.tileHeight} ${summary.tileSizeUnit}",
                40f,
                y,
                bodyPaint
            )

            y += 20f
            canvas.drawText(
                "Grout Gap: ${summary.groutGap} ${summary.groutUnit}",
                40f,
                y,
                bodyPaint
            )

            // 6. Section: Results
            y += 30f
            canvas.drawText("ESTIMATE", 40f, y, headerPaint)

            y += 20f
            canvas.drawText(
                "Waste Allowance: ${summary.wastePercent}%",
                40f,
                y,
                bodyPaint
            )

            y += 20f
            canvas.drawText(
                "Tiles Needed: ${summary.tilesNeeded}",
                40f,
                y,
                bodyPaint
            )

            y += 20f
            canvas.drawText(
                "Boxes Needed: ${summary.boxesNeeded}",
                40f,
                y,
                bodyPaint
            )

            y += 20f
            canvas.drawText(
                "Box Coverage Unit: ${summary.boxCoverageUnit}",
                40f,
                y,
                bodyPaint
            )

            // 7. Notes
            if (!summary.notes.isNullOrBlank()) {
                y += 30f
                canvas.drawText("NOTES", 40f, y, headerPaint)

                val noteLines = wrapText(summary.notes!!, bodyPaint, pageWidth - 80f)
                for (line in noteLines) {
                    y += 20f
                    canvas.drawText(line, 40f, y, bodyPaint)
                }
            }

            // 8. Disclaimer
            y += 40f
            val disclaimerLines = wrapText(
                "TileVision AR provides estimates only. Always confirm with a physical tape or ruler before cutting materials or purchasing product.",
                smallPaint,
                pageWidth - 80f
            )
            for (line in disclaimerLines) {
                y += 16f
                canvas.drawText(line, 40f, y, smallPaint)
            }

            pdfDoc.finishPage(page)

            // 9. Write file into cache/
            val outDir = File(context.cacheDir, "exports")
            if (!outDir.exists()) outDir.mkdirs()

            val fileName = "TileVisionAR_${summary.id}.pdf"
            val outFile = File(outDir, fileName)

            FileOutputStream(outFile).use { fos ->
                pdfDoc.writeTo(fos)
            }
            pdfDoc.close()

            // 10. Build a FileProvider Uri so we can share it
            // Make sure manifest has a <provider> with FileProvider and proper authorities.
            val uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                outFile
            )
            return uri
        } catch (t: Throwable) {
            t.printStackTrace()
            return null
        }
    }

    // helper to wrap flowing text inside PDF width
    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = StringBuilder()

        for (word in words) {
            val trial = if (current.isEmpty()) word else current.toString() + " " + word
            if (paint.measureText(trial) > maxWidth) {
                lines.add(current.toString())
                current = StringBuilder(word)
            } else {
                current = StringBuilder(trial)
            }
        }
        if (current.isNotEmpty()) {
            lines.add(current.toString())
        }
        return lines
    }
    
    // Lightweight PDF export with callbacks for simple measurement sharing
    fun export(context: Context, summary: ProjectSummary, areaText: String, bitmapPreview: Bitmap?, onReady: (Uri) -> Unit, onError: (Throwable) -> Unit) {
        try {
            // Create PDF document
            val doc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(1024, 1448, 1).create()
            val page = doc.startPage(pageInfo)
            val c = page.canvas
            
            // White background
            c.drawColor(Color.WHITE)
            
            // Title
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { 
                textSize = 40f
                color = Color.BLACK
                typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            }
            c.drawText("TileVision Measurement", 64f, 96f, p)
            
            // Area
            p.textSize = 28f
            p.typeface = Typeface.DEFAULT
            c.drawText("Area: $areaText", 64f, 160f, p)
            
            // Units
            c.drawText("Units: ${summary.unitsSystem}", 64f, 210f, p)
            
            // Optional bitmap preview
            bitmapPreview?.let { bitmap ->
                c.drawBitmap(bitmap, 64f, 260f, null)
            }
            
            doc.finishPage(page)
            
            // Save to cache directory
            val file = File(context.cacheDir, "tilevision_${System.currentTimeMillis()}.pdf")
            file.outputStream().use { doc.writeTo(it) }
            doc.close()
            
            // Get FileProvider URI
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            onReady(uri)
        } catch (t: Throwable) {
            onError(t)
        }
    }
}
