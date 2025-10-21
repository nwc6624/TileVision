package com.tilevision.shared.measurement

import com.tilevision.shared.ar.ArPose
import com.tilevision.shared.ar.ArSessionManager
import com.tilevision.shared.ar.ArVector3
import com.tilevision.shared.haptics.Haptics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * ViewModel for the measurement screen
 */
class MeasureViewModel(
    private val arSessionManager: ArSessionManager,
    private val haptics: Haptics,
    private val coroutineScope: CoroutineScope
) {
    private val _uiState = MutableStateFlow(MeasureUiState())
    val uiState: StateFlow<MeasureUiState> = _uiState.asStateFlow()
    
    private var snapToGrid = false
    private val gridSize = 0.1f // 10cm grid
    
    fun onAddPoint(screenX: Float, screenY: Float) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                val raycastResult = arSessionManager.raycast(screenX, screenY)
                raycastResult.fold(
                    onSuccess = { hit ->
                        if (hit != null) {
                            val worldPoint = hit.hitPose.position
                            val localPoint = worldToLocalPlane(worldPoint)
                            
                            val finalPoint = if (snapToGrid) {
                                snapToGrid(localPoint)
                            } else {
                                localPoint
                            }
                            
                            val newPoints = _uiState.value.points + finalPoint
                            _uiState.value = _uiState.value.copy(
                                points = newPoints,
                                trackingStable = true,
                                tipText = "Point added successfully"
                            )
                            
                            haptics.selection()
                            updateMeasurements(newPoints)
                        } else {
                            _uiState.value = _uiState.value.copy(
                                trackingStable = false,
                                tipText = "No surface detected. Try pointing at a flat surface."
                            )
                            haptics.notification(com.tilevision.shared.haptics.NotificationType.WARNING)
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            trackingStable = false,
                            tipText = "Failed to detect surface: ${error.message}"
                        )
                        haptics.notification(com.tilevision.shared.haptics.NotificationType.ERROR)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    trackingStable = false,
                    tipText = "Error: ${e.message}"
                )
                haptics.notification(com.tilevision.shared.haptics.NotificationType.ERROR)
            }
        }
    }
    
    fun onUndo() {
        val currentPoints = _uiState.value.points
        if (currentPoints.isNotEmpty()) {
            val newPoints = currentPoints.dropLast(1)
            _uiState.value = _uiState.value.copy(
                points = newPoints,
                tipText = "Point removed"
            )
            haptics.light()
            updateMeasurements(newPoints)
        }
    }
    
    fun onReset() {
        _uiState.value = _uiState.value.copy(
            points = emptyList(),
            areaFt2 = 0.0,
            perimeterFt = 0.0,
            canFinish = false,
            tipText = "Measurement reset"
        )
        haptics.medium()
    }
    
    fun onFinish() {
        if (_uiState.value.canFinish) {
            _uiState.value = _uiState.value.copy(
                tipText = "Measurement completed!"
            )
            haptics.notification(com.tilevision.shared.haptics.NotificationType.SUCCESS)
        }
    }
    
    fun onToggleSnap() {
        snapToGrid = !snapToGrid
        _uiState.value = _uiState.value.copy(
            tipText = if (snapToGrid) "Grid snap enabled" else "Grid snap disabled"
        )
        haptics.selection()
    }
    
    private fun worldToLocalPlane(worldPoint: ArVector3): Vec2 {
        // For simplicity, we'll use the world coordinates directly
        // In a real implementation, you'd transform to a local plane coordinate system
        return Vec2(worldPoint.x, worldPoint.z)
    }
    
    private fun snapToGrid(point: Vec2): Vec2 {
        val snappedX = (point.x / gridSize).roundToInt() * gridSize
        val snappedY = (point.y / gridSize).roundToInt() * gridSize
        return Vec2(snappedX, snappedY)
    }
    
    private fun updateMeasurements(points: List<Vec2>) {
        val area = calculateArea(points)
        val perimeter = calculatePerimeter(points)
        val canFinish = points.size >= 3 // Need at least 3 points for a valid area
        
        _uiState.value = _uiState.value.copy(
            areaFt2 = area,
            perimeterFt = perimeter,
            canFinish = canFinish
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
 * UI State for the measurement screen
 */
data class MeasureUiState(
    val points: List<Vec2> = emptyList(),
    val areaFt2: Double = 0.0,
    val perimeterFt: Double = 0.0,
    val canFinish: Boolean = false,
    val tipText: String = "Tap to add measurement points",
    val trackingStable: Boolean = false
)

/**
 * 2D Vector for local plane coordinates
 */
data class Vec2(
    val x: Float,
    val y: Float
)
