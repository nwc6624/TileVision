package com.tilevision.shared.domain

import kotlinx.serialization.Serializable

/**
 * Domain functions for cost calculation and export
 */
object CostCalculator {
    
    /**
     * Calculate total project cost
     */
    fun calculateTotalCost(
        tileCount: Int,
        boxCount: Int,
        priceMode: PriceMode,
        price: Double,
        taxPercentage: Double,
        extras: List<CostExtra>
    ): CostCalculationResult {
        val baseCost = when (priceMode) {
            PriceMode.PER_TILE -> tileCount * price
            PriceMode.PER_BOX -> boxCount * price
        }
        
        val extrasCost = extras.sumOf { it.cost }
        val subtotal = baseCost + extrasCost
        val taxAmount = subtotal * (taxPercentage / 100.0)
        val total = subtotal + taxAmount
        
        return CostCalculationResult(
            baseCost = baseCost,
            extrasCost = extrasCost,
            subtotal = subtotal,
            taxAmount = taxAmount,
            total = total,
            lineItems = generateLineItems(tileCount, boxCount, priceMode, price, extras, taxPercentage)
        )
    }
    
    private fun generateLineItems(
        tileCount: Int,
        boxCount: Int,
        priceMode: PriceMode,
        price: Double,
        extras: List<CostExtra>,
        taxPercentage: Double
    ): List<CostLineItem> {
        val items = mutableListOf<CostLineItem>()
        
        // Base cost line item
        val baseDescription = when (priceMode) {
            PriceMode.PER_TILE -> "$tileCount tiles @ $${formatPrice(price)} each"
            PriceMode.PER_BOX -> "$boxCount boxes @ $${formatPrice(price)} each"
        }
        items.add(CostLineItem(
            description = baseDescription,
            quantity = if (priceMode == PriceMode.PER_TILE) tileCount else boxCount,
            unitPrice = price,
            total = when (priceMode) {
                PriceMode.PER_TILE -> tileCount * price
                PriceMode.PER_BOX -> boxCount * price
            }
        ))
        
        // Extras line items
        extras.forEach { extra ->
            items.add(CostLineItem(
                description = extra.description,
                quantity = extra.quantity,
                unitPrice = extra.unitPrice,
                total = extra.cost
            ))
        }
        
        // Tax line item
        val subtotal = items.sumOf { it.total }
        items.add(CostLineItem(
            description = "Tax (${formatPercentage(taxPercentage)}%)",
            quantity = 1,
            unitPrice = subtotal * (taxPercentage / 100.0),
            total = subtotal * (taxPercentage / 100.0)
        ))
        
        return items
    }
    
    /**
     * Generate project summary for export
     */
    fun generateProjectSummary(
        tileCount: Int,
        boxCount: Int,
        areaSqFt: Double,
        tileWidth: Double,
        tileHeight: Double,
        layoutType: TileLayoutType,
        costResult: CostCalculationResult
    ): ProjectSummary {
        return ProjectSummary(
            projectInfo = ProjectInfo(
                areaSqFt = areaSqFt,
                tileCount = tileCount,
                boxCount = boxCount,
                tileSize = "${tileWidth}\" x ${tileHeight}\"",
                layoutType = layoutType.name
            ),
            costBreakdown = costResult,
            generatedAt = getCurrentTimeMillis()
        )
    }
    
    private fun formatPrice(price: Double): String {
        return formatDouble(price, 2)
    }
    
    private fun formatPercentage(percentage: Double): String {
        return formatDouble(percentage, 1)
    }
    
    private fun formatDouble(value: Double, decimals: Int): String {
        val multiplier = 10.0 * decimals // Simple approximation
        val rounded = kotlin.math.round(value * multiplier) / multiplier
        return rounded.toString()
    }
    
    private fun getCurrentTimeMillis(): Long {
        // Simple timestamp - in a real app you'd use proper date/time library
        return 1700000000000L // Placeholder timestamp
    }
}

/**
 * Price calculation modes
 */
enum class PriceMode {
    PER_TILE,
    PER_BOX
}

/**
 * Cost extra items
 */
@Serializable
data class CostExtra(
    val description: String,
    val quantity: Int,
    val unitPrice: Double
) {
    val cost: Double get() = quantity * unitPrice
}

/**
 * Cost calculation result
 */
@Serializable
data class CostCalculationResult(
    val baseCost: Double,
    val extrasCost: Double,
    val subtotal: Double,
    val taxAmount: Double,
    val total: Double,
    val lineItems: List<CostLineItem>
)

/**
 * Individual cost line item
 */
@Serializable
data class CostLineItem(
    val description: String,
    val quantity: Int,
    val unitPrice: Double,
    val total: Double
)

/**
 * Project summary for export
 */
@Serializable
data class ProjectSummary(
    val projectInfo: ProjectInfo,
    val costBreakdown: CostCalculationResult,
    val generatedAt: Long
)

/**
 * Project information
 */
@Serializable
data class ProjectInfo(
    val areaSqFt: Double,
    val tileCount: Int,
    val boxCount: Int,
    val tileSize: String,
    val layoutType: String
)
