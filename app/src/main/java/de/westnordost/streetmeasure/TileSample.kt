package de.westnordost.streetmeasure

data class TileSample(
    val id: String,
    val displayName: String,        // "Tile - Oct 24, 2025 18:41"
    val widthInInches: Float,
    val heightInInches: Float,
    val areaSqFt: Float,
    val timestamp: Long,
    val outlinePoints: List<TilePoint2D> // simplified polygon or fitted rectangle in plane space for preview
)

data class TilePoint2D(
    val x: Float,
    val z: Float
)
