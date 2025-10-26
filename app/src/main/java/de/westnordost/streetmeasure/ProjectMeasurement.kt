package de.westnordost.streetmeasure

data class ProjectMeasurement(
    val id: String,                  // UUID
    val displayName: String,         // e.g. "Project - Oct 24, 2025 14:32"
    val areaFt2: Float,              // total area in square feet at time of save
    val timestamp: Long,             // System.currentTimeMillis()
    val polygonPoints: List<ProjectPoint2D> // (x,z) projected for preview drawing
)

data class ProjectPoint2D(
    val x: Float,
    val z: Float
)
