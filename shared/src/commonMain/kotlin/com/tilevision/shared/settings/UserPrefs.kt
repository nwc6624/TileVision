package com.tilevision.shared.settings

import kotlinx.serialization.Serializable

/**
 * User preferences and settings
 */
@Serializable
data class UserPrefs(
    val units: MeasurementUnits = MeasurementUnits.IMPERIAL,
    val defaultTileWidth: Double = 12.0,
    val defaultTileHeight: Double = 12.0,
    val defaultGroutWidth: Double = 0.125,
    val defaultWastePercentage: Double = 10.0,
    val defaultTilesPerBox: Int = 20,
    val currencySymbol: String = "$",
    val lastBackupTimestamp: Long = 0L,
    val version: Int = 1
)

/**
 * Measurement unit system
 */
@Serializable
enum class MeasurementUnits {
    IMPERIAL, // feet, inches
    METRIC    // meters, centimeters
}

/**
 * Default values for user preferences
 */
object DefaultUserPrefs {
    val IMPERIAL = UserPrefs(
        units = MeasurementUnits.IMPERIAL,
        defaultTileWidth = 12.0, // inches
        defaultTileHeight = 12.0, // inches
        defaultGroutWidth = 0.125, // inches
        defaultWastePercentage = 10.0,
        defaultTilesPerBox = 20,
        currencySymbol = "$"
    )
    
    val METRIC = UserPrefs(
        units = MeasurementUnits.METRIC,
        defaultTileWidth = 30.0, // centimeters
        defaultTileHeight = 30.0, // centimeters
        defaultGroutWidth = 0.3, // centimeters
        defaultWastePercentage = 10.0,
        defaultTilesPerBox = 20,
        currencySymbol = "€"
    )
}
