package com.tilevision.shared.tile

import com.tilevision.shared.domain.TileCalculator
import com.tilevision.shared.domain.TileLayoutType
import com.tilevision.shared.domain.TileLayoutPreview
import com.tilevision.shared.measurement.Vec2
import com.tilevision.shared.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the tile planner screen
 */
class TilePlannerViewModel(
    private val polygon: List<Vec2>,
    private val areaSqFt: Double,
    private val coroutineScope: CoroutineScope,
    private val settingsRepository: SettingsRepository
) {
    private val _uiState = MutableStateFlow(
        TilePlannerUiState(
            tileWidth = settingsRepository.getCurrentUserPrefs().defaultTileWidth,
            tileHeight = settingsRepository.getCurrentUserPrefs().defaultTileHeight,
            groutWidth = settingsRepository.getCurrentUserPrefs().defaultGroutWidth,
            layoutType = TileLayoutType.GRID,
            wastePercentage = settingsRepository.getCurrentUserPrefs().defaultWastePercentage,
            tilesPerBox = settingsRepository.getCurrentUserPrefs().defaultTilesPerBox,
            tileCount = 0,
            boxCount = 0,
            totalAreaSqFt = 0.0,
            wasteAreaSqFt = 0.0,
            tileLayout = TileLayoutPreview(emptyList(), com.tilevision.shared.domain.PolygonBounds(0.0, 0.0, 0.0, 0.0))
        )
    )
    val uiState: StateFlow<TilePlannerUiState> = _uiState.asStateFlow()
    
    init {
        // Calculate initial values
        updateCalculations()
        
        // Listen for settings changes and update defaults
        coroutineScope.launch {
            settingsRepository.userPrefs.collect { userPrefs ->
                if (userPrefs != null) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            tileWidth = userPrefs.defaultTileWidth,
                            tileHeight = userPrefs.defaultTileHeight,
                            groutWidth = userPrefs.defaultGroutWidth,
                            wastePercentage = userPrefs.defaultWastePercentage,
                            tilesPerBox = userPrefs.defaultTilesPerBox
                        )
                    }
                    updateCalculations()
                }
            }
        }
    }
    
    fun onTileWidthChange(width: Double) {
        _uiState.value = _uiState.value.copy(tileWidth = width)
        updateCalculations()
    }
    
    fun onTileHeightChange(height: Double) {
        _uiState.value = _uiState.value.copy(tileHeight = height)
        updateCalculations()
    }
    
    fun onGroutWidthChange(width: Double) {
        _uiState.value = _uiState.value.copy(groutWidth = width)
        updateCalculations()
    }
    
    fun onLayoutTypeChange(layoutType: TileLayoutType) {
        _uiState.value = _uiState.value.copy(layoutType = layoutType)
        updateCalculations()
    }
    
    fun onWastePercentageChange(percentage: Double) {
        _uiState.value = _uiState.value.copy(wastePercentage = percentage)
        updateCalculations()
    }
    
    fun onTilesPerBoxChange(count: Int) {
        _uiState.value = _uiState.value.copy(tilesPerBox = count)
        updateCalculations()
    }
    
    private fun updateCalculations() {
        coroutineScope.launch(Dispatchers.Default) {
            val currentState = _uiState.value
            
            // Calculate tile count and related metrics
            val calculationResult = TileCalculator.calculateTileCount(
                areaSqFt = areaSqFt,
                tileWidth = currentState.tileWidth,
                tileHeight = currentState.tileHeight,
                groutWidth = currentState.groutWidth,
                wastePercentage = currentState.wastePercentage
            )
            
            // Calculate box count
            val boxCount = TileCalculator.calculateBoxCount(
                tileCount = calculationResult.tileCount,
                tilesPerBox = currentState.tilesPerBox
            )
            
            // Generate tile layout preview
            val tileLayout = TileCalculator.generateTileLayout(
                polygon = polygon,
                tileWidth = currentState.tileWidth,
                tileHeight = currentState.tileHeight,
                layoutType = currentState.layoutType
            )
            
            // Update state
            _uiState.value = currentState.copy(
                tileCount = calculationResult.tileCount,
                boxCount = boxCount,
                totalAreaSqFt = calculationResult.totalAreaSqFt,
                wasteAreaSqFt = calculationResult.wasteAreaSqFt,
                tileLayout = tileLayout
            )
        }
    }
}

/**
 * UI State for the tile planner screen
 */
data class TilePlannerUiState(
    val tileWidth: Double,
    val tileHeight: Double,
    val groutWidth: Double,
    val layoutType: TileLayoutType,
    val wastePercentage: Double,
    val tilesPerBox: Int,
    val tileCount: Int,
    val boxCount: Int,
    val totalAreaSqFt: Double,
    val wasteAreaSqFt: Double,
    val tileLayout: TileLayoutPreview
)
