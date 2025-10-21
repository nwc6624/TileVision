package com.tilevision.shared.io

import com.tilevision.shared.ar.ArPose
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

/**
 * Handles file export operations for measurements and project data
 */
expect class FileExporter {
    /**
     * Export state
     */
    val exportState: StateFlow<ExportState>
    
    /**
     * Export measurement data to file
     * @param data Measurement data to export
     * @param fileName Desired file name
     * @param format Export format
     * @return Result with file path or error
     */
    suspend fun exportMeasurement(
        data: MeasurementData,
        fileName: String,
        format: ExportFormat
    ): Result<String>
    
    /**
     * Export project data to file
     * @param data Project data to export
     * @param fileName Desired file name
     * @param format Export format
     * @return Result with file path or error
     */
    suspend fun exportProject(
        data: ProjectData,
        fileName: String,
        format: ExportFormat
    ): Result<String>
    
    /**
     * Get available export formats
     */
    fun getAvailableFormats(): List<ExportFormat>
    
    /**
     * Check if export is supported
     */
    fun isExportSupported(): Boolean
}

/**
 * Export states
 */
enum class ExportState {
    IDLE,
    EXPORTING,
    SUCCESS,
    ERROR
}

/**
 * Export formats
 */
enum class ExportFormat(val extension: String, val mimeType: String) {
    JSON(".json", "application/json"),
    CSV(".csv", "text/csv"),
    PDF(".pdf", "application/pdf"),
    DXF(".dxf", "application/dxf")
}

/**
 * Measurement data for export
 */
@Serializable
data class MeasurementData(
    val id: String,
    val name: String,
    val measurements: List<Measurement>,
    val timestamp: Long,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Project data for export
 */
@Serializable
data class ProjectData(
    val id: String,
    val name: String,
    val description: String,
    val measurements: List<MeasurementData>,
    val createdTimestamp: Long,
    val modifiedTimestamp: Long,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Individual measurement
 */
@Serializable
data class Measurement(
    val id: String,
    val name: String,
    val startPoint: ArPose,
    val endPoint: ArPose,
    val distance: Float,
    val unit: MeasurementUnit,
    val timestamp: Long
)

/**
 * Measurement units
 */
enum class MeasurementUnit(val symbol: String, val conversionFactor: Float) {
    METERS("m", 1.0f),
    CENTIMETERS("cm", 0.01f),
    MILLIMETERS("mm", 0.001f),
    FEET("ft", 0.3048f),
    INCHES("in", 0.0254f)
}

