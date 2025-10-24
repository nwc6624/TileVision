package de.westnordost.streetmeasure

import android.content.Context
import android.graphics.Bitmap
import android.text.format.DateFormat
import com.google.ar.sceneform.ArSceneView
import java.io.File
import java.io.FileOutputStream
import java.util.Date

object MeasurementUtils {
    
    fun formatDisplayName(prefix: String, timestamp: Long): String {
        val date = Date(timestamp)
        val dateFormat = DateFormat.format("MMM dd, yyyy HH:mm", date)
        return "$prefix - $dateFormat"
    }
    
    fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        val dateFormat = DateFormat.format("MMM dd, yyyy h:mm a", date)
        return dateFormat.toString()
    }
    
    fun captureArScreenshot(arSceneView: ArSceneView): Bitmap? {
        return try {
            // TODO: Implement proper AR screenshot capture
            // For now, return null as a placeholder
            null
        } catch (e: Exception) {
            null
        }
    }
    
    fun saveScreenshot(context: Context, bitmap: Bitmap, filename: String): String? {
        return try {
            val file = File(context.filesDir, "screenshots")
            if (!file.exists()) {
                file.mkdirs()
            }
            
            val screenshotFile = File(file, "$filename.jpg")
            val outputStream = FileOutputStream(screenshotFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()
            
            screenshotFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
