package com.tilevision.shared.io

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.tilevision.shared.export.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

actual class FileExporter(private val context: Context) {
    private val _exportState = MutableStateFlow(ExportState.IDLE)
    actual val exportState: StateFlow<ExportState> = _exportState.asStateFlow()
    
    private val json = Json { prettyPrint = true }
    private val pdfGenerator = PdfGenerator(context)
    
    actual suspend fun exportMeasurement(
        data: MeasurementData,
        fileName: String,
        format: ExportFormat
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            _exportState.value = ExportState.EXPORTING
            
            val content = when (format) {
                ExportFormat.JSON -> json.encodeToString(MeasurementData.serializer(), data)
                ExportFormat.CSV -> convertToCsv(data)
                ExportFormat.PDF -> "PDF export not implemented yet" // TODO: Implement PDF export
                ExportFormat.DXF -> "DXF export not implemented yet" // TODO: Implement DXF export
            }
            
            val filePath = saveToFile(content, fileName, format)
            _exportState.value = ExportState.SUCCESS
            
            Result.success(filePath)
        } catch (e: Exception) {
            _exportState.value = ExportState.ERROR
            Result.failure(e)
        }
    }
    
    actual suspend fun exportProject(
        data: ProjectData,
        fileName: String,
        format: ExportFormat
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            _exportState.value = ExportState.EXPORTING
            
            val result = when (format) {
                ExportFormat.JSON -> {
                    val content = json.encodeToString(ProjectData.serializer(), data)
                    Result.success(saveToFile(content, fileName, format))
                }
                ExportFormat.CSV -> {
                    val content = convertProjectToCsv(data)
                    Result.success(saveToFile(content, fileName, format))
                }
                ExportFormat.PDF -> {
                    // Convert ProjectData to ProjectSummary for PDF generation
                    val summary = convertToProjectSummary(data)
                    val pdfBytes = pdfGenerator.generatePdf(summary).getOrThrow()
                    saveBinaryToFile(pdfBytes, fileName, format)
                }
                ExportFormat.PNG -> {
                    // Generate PNG from project data
                    val pngBytes = generatePngFromProject(data)
                    saveBinaryToFile(pngBytes, fileName, format)
                }
                ExportFormat.DXF -> {
                    val content = "DXF export not implemented yet"
                    Result.success(saveToFile(content, fileName, format))
                }
            }
            
            _exportState.value = ExportState.SUCCESS
            result
        } catch (e: Exception) {
            _exportState.value = ExportState.ERROR
            Result.failure(e)
        }
    }
    
    actual fun getAvailableFormats(): List<ExportFormat> {
        return listOf(ExportFormat.JSON, ExportFormat.CSV, ExportFormat.PDF, ExportFormat.PNG)
    }
    
    actual fun isExportSupported(): Boolean {
        return true
    }
    
    private suspend fun saveToFile(content: String, fileName: String, format: ExportFormat): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val finalFileName = "${fileName}_$timestamp${format.extension}"
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore for Android 10+
            saveToMediaStore(content, finalFileName, format)
        } else {
            // Use app files directory for older versions
            saveToAppFiles(content, finalFileName)
        }
    }
    
    private suspend fun saveToMediaStore(content: String, fileName: String, format: ExportFormat): String {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, format.mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/TileVision")
        }
        
        val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            ?: throw Exception("Failed to create file in MediaStore")
        
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(content.toByteArray())
        }
        
        return uri.toString()
    }
    
    private suspend fun saveToAppFiles(content: String, fileName: String): String {
        val documentsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "TileVision")
        if (!documentsDir.exists()) {
            documentsDir.mkdirs()
        }
        
        val file = File(documentsDir, fileName)
        FileOutputStream(file).use { outputStream ->
            outputStream.write(content.toByteArray())
        }
        
        return file.absolutePath
    }
    
    private fun convertToCsv(data: MeasurementData): String {
        val header = "ID,Name,StartX,StartY,StartZ,EndX,EndY,EndZ,Distance,Unit,Timestamp"
        val rows = data.measurements.joinToString("\n") { measurement ->
            "${measurement.id},${measurement.name}," +
            "${measurement.startPoint.position.x},${measurement.startPoint.position.y},${measurement.startPoint.position.z}," +
            "${measurement.endPoint.position.x},${measurement.endPoint.position.y},${measurement.endPoint.position.z}," +
            "${measurement.distance},${measurement.unit.symbol},${measurement.timestamp}"
        }
        return "$header\n$rows"
    }
    
    private fun convertProjectToCsv(data: ProjectData): String {
        val header = "ProjectID,ProjectName,MeasurementID,MeasurementName,StartX,StartY,StartZ,EndX,EndY,EndZ,Distance,Unit,Timestamp"
        val rows = data.measurements.flatMap { measurementData ->
            measurementData.measurements.map { measurement ->
                "${data.id},${data.name},${measurement.id},${measurement.name}," +
                "${measurement.startPoint.position.x},${measurement.startPoint.position.y},${measurement.startPoint.position.z}," +
                "${measurement.endPoint.position.x},${measurement.endPoint.position.y},${measurement.endPoint.position.z}," +
                "${measurement.distance},${measurement.unit.symbol},${measurement.timestamp}"
            }
        }.joinToString("\n")
        return "$header\n$rows"
    }
    
    private fun convertToProjectSummary(data: ProjectData): com.tilevision.shared.domain.ProjectSummary {
        // Convert ProjectData to ProjectSummary for PDF generation
        // This is a simplified conversion - in a real app you'd have more detailed mapping
        return com.tilevision.shared.domain.ProjectSummary(
            projectInfo = com.tilevision.shared.domain.ProjectInfo(
                areaSqFt = 0.0, // TODO: Calculate from measurements
                tileCount = 0, // TODO: Get from tile planner
                boxCount = 0, // TODO: Get from tile planner
                tileSize = "12\" x 12\"", // TODO: Get from tile planner
                layoutType = "Grid" // TODO: Get from tile planner
            ),
            costBreakdown = com.tilevision.shared.domain.CostCalculationResult(
                baseCost = 0.0,
                extrasCost = 0.0,
                subtotal = 0.0,
                taxAmount = 0.0,
                total = 0.0,
                lineItems = emptyList()
            ),
            generatedAt = System.currentTimeMillis()
        )
    }
    
    private fun generatePngFromProject(data: ProjectData): ByteArray {
        // Generate a simple PNG representation of the project
        val bitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Fill background
        canvas.drawColor(android.graphics.Color.WHITE)
        
        // Draw title
        val paint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 24f
            isAntiAlias = true
        }
        canvas.drawText("Tile Project Summary", 50f, 50f, paint)
        
        // Draw project info
        paint.textSize = 16f
        canvas.drawText("Project: ${data.name}", 50f, 100f, paint)
        canvas.drawText("Description: ${data.description}", 50f, 130f, paint)
        canvas.drawText("Created: ${java.util.Date(data.createdTimestamp)}", 50f, 160f, paint)
        
        // Convert bitmap to byte array
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }
    
    private fun saveBinaryToFile(data: ByteArray, fileName: String, format: ExportFormat): Result<String> {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val finalFileName = "${fileName}_$timestamp${format.extension}"
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore for Android 10+
            saveBinaryToMediaStore(data, finalFileName, format)
        } else {
            // Use app files directory for older versions
            saveBinaryToAppFiles(data, finalFileName)
        }
    }
    
    private fun saveBinaryToMediaStore(data: ByteArray, fileName: String, format: ExportFormat): Result<String> {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, format.mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/TileVision")
        }
        
        val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            ?: return Result.failure(Exception("Failed to create file in MediaStore"))
        
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(data)
        }
        
        return Result.success(uri.toString())
    }
    
    private fun saveBinaryToAppFiles(data: ByteArray, fileName: String): Result<String> {
        val documentsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "TileVision")
        if (!documentsDir.exists()) {
            documentsDir.mkdirs()
        }
        
        val file = File(documentsDir, fileName)
        FileOutputStream(file).use { outputStream ->
            outputStream.write(data)
        }
        
        return Result.success(file.absolutePath)
    }
}
