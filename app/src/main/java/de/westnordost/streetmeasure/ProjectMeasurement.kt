package de.westnordost.streetmeasure

import com.google.ar.sceneform.math.Vector3

data class ProjectMeasurement(
    val id: String,
    val displayName: String,
    val areaFt2: Float,
    val timestamp: Long,
    val previewImageUri: String? = null,
    val polygonPoints: List<Vector3> = emptyList()
)
