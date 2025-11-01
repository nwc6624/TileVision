package de.westnordost.streetmeasure

data class ProjectSummary(
    val id: String,
    val timestamp: Long,
    val displayName: String,            // e.g. "Kitchen Floor - Oct 26, 2025"
    val projectMeasurementId: String?,  // FK -> ProjectMeasurement.id (nullable)
    val tileSampleId: String?,          // FK -> TileSample.id (nullable if using unsaved tile)
    
    // Units
    val unitsSystem: String,            // "imperial" or "metric"
    
    // Surface / area
    val areaValue: Double,              // numeric area at time of save
    val areaUnit: String,               // "ft²" or "m²"
    
    // Tile geometry (canonical MM)
    val tileWidth: Double,
    val tileHeight: Double,
    val tileSizeUnit: String,           // "in" or "cm"
    val tileWidthMm: Float,
    val tileHeightMm: Float,
    
    // Grout gap (canonical MM)
    val groutGap: Double,
    val groutUnit: String,              // "in" or "mm"
    val groutGapMm: Float,
    
    // Waste & box
    val wastePercent: Double,
    val tilesNeeded: Int,
    val boxesNeeded: Int,
    val boxCoverageUnit: String,        // "ft²/box" or "m²/box" etc.
    
    // Optional user notes
    val notes: String? = null,
    
    // Legacy fields for backward compatibility
    val areaSqFt: Float = 0f,           // Will be calculated
    val tileWidthIn: Float = 0f,        // Will be calculated
    val tileHeightIn: Float = 0f,       // Will be calculated
    val tileAreaSqFt: Float = 0f,       // Will be calculated
    val totalTilesNeededRaw: Float = 0f, // Will be calculated
    val totalTilesNeededFinal: Int = 0,  // Will be calculated
    val layoutStyle: String = "Straight",
    val groutGapInches: Float = 0f      // Will be calculated
)
