package com.tilevision.shared.polygon

import com.tilevision.shared.measurement.Vec2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * ViewModel for the polygon review screen
 */
class PolygonReviewViewModel(
    initialPoints: List<Vec2>,
    private val coroutineScope: CoroutineScope
) {
    private val _uiState = MutableStateFlow(
        PolygonReviewUiState(
            points = initialPoints,
            areaFt2 = calculateArea(initialPoints),
            perimeterFt = calculatePerimeter(initialPoints)
        )
    )
    val uiState: StateFlow<PolygonReviewUiState> = _uiState.asStateFlow()
    
    fun onPointDrag(index: Int, newPosition: Vec2) {
        val currentPoints = _uiState.value.points.toMutableList()
        if (index in currentPoints.indices) {
            currentPoints[index] = newPosition
            updateMeasurements(currentPoints)
        }
    }
    
    private fun updateMeasurements(points: List<Vec2>) {
        val area = calculateArea(points)
        val perimeter = calculatePerimeter(points)
        
        _uiState.value = _uiState.value.copy(
            points = points,
            areaFt2 = area,
            perimeterFt = perimeter
        )
    }
    
    private fun calculateArea(points: List<Vec2>): Double {
        if (points.size < 3) return 0.0
        
        // Shoelace formula for polygon area
        var area = 0.0
        val n = points.size
        for (i in 0 until n) {
            val j = (i + 1) % n
            area += points[i].x * points[j].y
            area -= points[j].x * points[i].y
        }
        area = abs(area) / 2.0
        
        // Convert from square meters to square feet
        return area * 10.764
    }
    
    private fun calculatePerimeter(points: List<Vec2>): Double {
        if (points.size < 2) return 0.0
        
        var perimeter = 0.0
        for (i in points.indices) {
            val current = points[i]
            val next = points[(i + 1) % points.size]
            val distance = sqrt((next.x - current.x).pow(2) + (next.y - current.y).pow(2))
            perimeter += distance
        }
        
        // Convert from meters to feet
        return perimeter * 3.281
    }
}

/**
 * UI State for the polygon review screen
 */
data class PolygonReviewUiState(
    val points: List<Vec2>,
    val areaFt2: Double,
    val perimeterFt: Double
)
