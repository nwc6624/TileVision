package de.westnordost.streetmeasure

data class ProjectSummary(
    val id: String,
    val timestamp: Long,
    val displayName: String,            // e.g. "Kitchen Floor - Oct 26, 2025"
    val projectMeasurementId: String,   // FK -> ProjectMeasurement.id
    val tileSampleId: String?,          // FK -> TileSample.id (nullable if using unsaved tile)
    val areaSqFt: Float,                // total area for this job
    val tileWidthIn: Float,
    val tileHeightIn: Float,
    val tileAreaSqFt: Float,
    val wastePercent: Float,            // e.g. 10.0f
    val totalTilesNeededRaw: Float,     // before waste/round-up
    val totalTilesNeededFinal: Int,     // after waste/round-up
    val layoutStyle: String = "Straight", // e.g. "Straight", "Staggered", "Herringbone"
    val groutGapInches: Float = 0.125f,  // e.g. 1/8" = 0.125
    val notes: String? = null
)
