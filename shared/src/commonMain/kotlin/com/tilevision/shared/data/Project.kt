package com.tilevision.shared.data

import com.tilevision.shared.measurement.Vec2
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * Represents a complete tile project
 */
@Serializable
data class Project(
    val id: String,
    val name: String,
    val description: String,
    val surfaces: List<Surface>,
    val createdTimestamp: Long,
    val modifiedTimestamp: Long,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Represents a surface within a project (e.g., floor, wall)
 */
@Serializable
data class Surface(
    val id: String,
    val name: String,
    val description: String,
    val polygon: List<@Contextual Vec2>,
    val areaSqFt: Double,
    val tileCount: Int,
    val boxCount: Int,
    val tileWidth: Double,
    val tileHeight: Double,
    val layoutType: String,
    val groutWidth: Double,
    val wastePercentage: Double,
    val costBreakdown: CostBreakdown?,
    val createdTimestamp: Long,
    val modifiedTimestamp: Long
)

/**
 * Cost breakdown for a surface
 */
@Serializable
data class CostBreakdown(
    val priceMode: String, // "PER_TILE" or "PER_BOX"
    val price: Double,
    val taxPercentage: Double,
    val extras: List<CostExtra>,
    val baseCost: Double,
    val extrasCost: Double,
    val subtotal: Double,
    val taxAmount: Double,
    val total: Double
)

/**
 * Additional cost items
 */
@Serializable
data class CostExtra(
    val description: String,
    val quantity: Int,
    val unitPrice: Double
)
