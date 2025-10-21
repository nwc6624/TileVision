package com.tilevision.shared.io

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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
            
            val content = when (format) {
                ExportFormat.JSON -> json.encodeToString(ProjectData.serializer(), data)
                ExportFormat.CSV -> convertProjectToCsv(data)
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
    
    actual fun getAvailableFormats(): List<ExportFormat> {
        return listOf(ExportFormat.JSON, ExportFormat.CSV)
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
}
