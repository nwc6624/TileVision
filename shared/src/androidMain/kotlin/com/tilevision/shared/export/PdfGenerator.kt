package com.tilevision.shared.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.webkit.WebView
import com.tilevision.shared.domain.ProjectSummary
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class PdfGenerator(private val context: Context) {
    
    actual suspend fun generatePdf(summary: ProjectSummary): Result<ByteArray> = suspendCancellableCoroutine { continuation ->
        try {
            // For Android, we'll use WebView to render HTML to PDF
            val webView = WebView(context)
            webView.settings.javaScriptEnabled = true
            
            val htmlContent = generateHtmlTemplate(summary)
            
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            
            webView.webViewClient = object : android.webkit.WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    
                    try {
                        val pdfBytes = generatePdfFromWebView(webView, summary)
                        continuation.resume(Result.success(pdfBytes))
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }
                
                override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    continuation.resumeWithException(Exception("WebView error: $description"))
                }
            }
            
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
    
    private fun generatePdfFromWebView(webView: WebView, summary: ProjectSummary): ByteArray {
        // Create a simple PDF using Android's PdfDocument
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        // Draw content
        drawPdfContent(canvas, summary)
        
        pdfDocument.finishPage(page)
        
        // Convert to byte array
        val outputStream = java.io.ByteArrayOutputStream()
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
        
        return outputStream.toByteArray()
    }
    
    private fun drawPdfContent(canvas: Canvas, summary: ProjectSummary) {
        val paint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }
        
        val titlePaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 18f
            isAntiAlias = true
            isFakeBoldText = true
        }
        
        val headerPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 14f
            isAntiAlias = true
            isFakeBoldText = true
        }
        
        var y = 50f
        val leftMargin = 50f
        val rightMargin = 545f
        
        // Title
        canvas.drawText("Tile Project Summary", leftMargin, y, titlePaint)
        y += 30f
        
        // Project Information
        canvas.drawText("Project Information", leftMargin, y, headerPaint)
        y += 25f
        
        canvas.drawText("Area: ${String.format("%.1f", summary.projectInfo.areaSqFt)} ft²", leftMargin, y, paint)
        y += 20f
        canvas.drawText("Tiles: ${summary.projectInfo.tileCount} tiles", leftMargin, y, paint)
        y += 20f
        canvas.drawText("Boxes: ${summary.projectInfo.boxCount} boxes", leftMargin, y, paint)
        y += 20f
        canvas.drawText("Tile Size: ${summary.projectInfo.tileSize}", leftMargin, y, paint)
        y += 20f
        canvas.drawText("Layout: ${summary.projectInfo.layoutType}", leftMargin, y, paint)
        y += 30f
        
        // Cost Breakdown
        canvas.drawText("Cost Breakdown", leftMargin, y, headerPaint)
        y += 25f
        
        // Line items
        summary.costBreakdown.lineItems.forEach { item ->
            val description = item.description
            val total = "$${String.format("%.2f", item.total)}"
            
            canvas.drawText(description, leftMargin, y, paint)
            canvas.drawText(total, rightMargin - paint.measureText(total), y, paint)
            y += 20f
        }
        
        y += 10f
        
        // Totals
        canvas.drawText("Subtotal: $${String.format("%.2f", summary.costBreakdown.subtotal)}", leftMargin, y, paint)
        y += 20f
        canvas.drawText("Tax: $${String.format("%.2f", summary.costBreakdown.taxAmount)}", leftMargin, y, paint)
        y += 20f
        
        val totalPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 14f
            isAntiAlias = true
            isFakeBoldText = true
        }
        
        canvas.drawText("Total: $${String.format("%.2f", summary.costBreakdown.total)}", leftMargin, y, totalPaint)
    }
}
