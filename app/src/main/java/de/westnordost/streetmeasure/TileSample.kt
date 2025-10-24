package de.westnordost.streetmeasure

data class TileSample(
    val id: String,
    val displayName: String,
    val width: Float,
    val height: Float,
    val areaFt2: Float,
    val units: String,
    val timestamp: Long,
    val previewImageUri: String? = null
)
