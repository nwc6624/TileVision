package com.tilevision.shared.domain

import com.tilevision.shared.measurement.Vec2
import kotlin.math.*

/**
 * Domain functions for tile calculation and planning
 */
object TileCalculator {
    
    /**
     * Calculate the number of tiles needed for a given area
     */
    fun calculateTileCount(
        areaSqFt: Double,
        tileWidth: Double,
        tileHeight: Double,
        groutWidth: Double,
        wastePercentage: Double
    ): TileCalculationResult {
        val tileAreaSqFt = (tileWidth * tileHeight) / 144.0 // Convert sq inches to sq feet
        val groutAreaSqFt = calculateGroutArea(areaSqFt, tileWidth, tileHeight, groutWidth)
        
        val totalAreaNeeded = areaSqFt + groutAreaSqFt
        val wasteArea = totalAreaNeeded * (wastePercentage / 100.0)
        val totalAreaWithWaste = totalAreaNeeded + wasteArea
        
        val tileCount = ceil(totalAreaWithWaste / tileAreaSqFt).toInt()
        
        return TileCalculationResult(
            tileCount = tileCount,
            tileAreaSqFt = tileAreaSqFt,
            groutAreaSqFt = groutAreaSqFt,
            wasteAreaSqFt = wasteArea,
            totalAreaSqFt = totalAreaWithWaste
        )
    }
    
    /**
     * Calculate grout area based on tile layout
     */
    private fun calculateGroutArea(
        areaSqFt: Double,
        tileWidth: Double,
        tileHeight: Double,
        groutWidth: Double
    ): Double {
        // Simplified calculation - in reality this would depend on the specific layout
        val tileAreaSqFt = (tileWidth * tileHeight) / 144.0
        val estimatedTileCount = areaSqFt / tileAreaSqFt
        
        // Estimate grout lines based on tile count
        val groutLines = sqrt(estimatedTileCount) * 2 // Rough estimate
        val groutLength = groutLines * max(tileWidth, tileHeight) / 12.0 // Convert to feet
        val groutArea = groutLength * (groutWidth / 12.0) // Convert grout width to feet
        
        return groutArea
    }
    
    /**
     * Calculate number of boxes needed based on tiles per box
     */
    fun calculateBoxCount(tileCount: Int, tilesPerBox: Int): Int {
        return ceil(tileCount.toDouble() / tilesPerBox).toInt()
    }
    
    /**
     * Generate tile layout preview data
     */
    fun generateTileLayout(
        polygon: List<Vec2>,
        tileWidth: Double,
        tileHeight: Double,
        layoutType: TileLayoutType
    ): TileLayoutPreview {
        val bounds = calculatePolygonBounds(polygon)
        val tiles = mutableListOf<TilePosition>()
        
        when (layoutType) {
            TileLayoutType.GRID -> {
                tiles.addAll(generateGridLayout(bounds, tileWidth, tileHeight))
            }
            TileLayoutType.BRICK -> {
                tiles.addAll(generateBrickLayout(bounds, tileWidth, tileHeight))
            }
        }
        
        return TileLayoutPreview(
            tiles = tiles,
            bounds = bounds
        )
    }
    
    private fun generateGridLayout(
        bounds: PolygonBounds,
        tileWidth: Double,
        tileHeight: Double
    ): List<TilePosition> {
        val tiles = mutableListOf<TilePosition>()
        val tileWidthFt = tileWidth / 12.0
        val tileHeightFt = tileHeight / 12.0
        
        var y = bounds.minY
        while (y < bounds.maxY) {
            var x = bounds.minX
            while (x < bounds.maxX) {
                tiles.add(TilePosition(x, y, tileWidthFt, tileHeightFt))
                x += tileWidthFt
            }
            y += tileHeightFt
        }
        
        return tiles
    }
    
    private fun generateBrickLayout(
        bounds: PolygonBounds,
        tileWidth: Double,
        tileHeight: Double
    ): List<TilePosition> {
        val tiles = mutableListOf<TilePosition>()
        val tileWidthFt = tileWidth / 12.0
        val tileHeightFt = tileHeight / 12.0
        val offset = tileWidthFt / 2.0
        
        var y = bounds.minY
        var isOffset = false
        
        while (y < bounds.maxY) {
            var x = bounds.minX
            if (isOffset) x += offset
            
            while (x < bounds.maxX) {
                tiles.add(TilePosition(x, y, tileWidthFt, tileHeightFt))
                x += tileWidthFt
            }
            
            y += tileHeightFt
            isOffset = !isOffset
        }
        
        return tiles
    }
    
    private fun calculatePolygonBounds(polygon: List<Vec2>): PolygonBounds {
        if (polygon.isEmpty()) {
            return PolygonBounds(0.0, 0.0, 0.0, 0.0)
        }
        
        val minX = polygon.minOf { it.x.toDouble() }
        val maxX = polygon.maxOf { it.x.toDouble() }
        val minY = polygon.minOf { it.y.toDouble() }
        val maxY = polygon.maxOf { it.y.toDouble() }
        
        return PolygonBounds(minX, maxX, minY, maxY)
    }
}

/**
 * Result of tile calculation
 */
data class TileCalculationResult(
    val tileCount: Int,
    val tileAreaSqFt: Double,
    val groutAreaSqFt: Double,
    val wasteAreaSqFt: Double,
    val totalAreaSqFt: Double
)

/**
 * Tile layout types
 */
enum class TileLayoutType {
    GRID,
    BRICK
}

/**
 * Tile position for preview
 */
data class TilePosition(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double
)

/**
 * Polygon bounds
 */
data class PolygonBounds(
    val minX: Double,
    val maxX: Double,
    val minY: Double,
    val maxY: Double
)

/**
 * Tile layout preview data
 */
data class TileLayoutPreview(
    val tiles: List<TilePosition>,
    val bounds: PolygonBounds
)
