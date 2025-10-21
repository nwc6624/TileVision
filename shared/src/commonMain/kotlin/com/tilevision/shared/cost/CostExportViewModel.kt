package com.tilevision.shared.cost

import com.tilevision.shared.domain.CostCalculator
import com.tilevision.shared.domain.CostExtra
import com.tilevision.shared.domain.PriceMode
import com.tilevision.shared.io.FileExporter
import com.tilevision.shared.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the cost export screen
 */
class CostExportViewModel(
    private val tileCount: Int,
    private val boxCount: Int,
    private val areaSqFt: Double,
    private val tileSize: String,
    private val layoutType: String,
    private val fileExporter: FileExporter,
    private val coroutineScope: CoroutineScope,
    private val settingsRepository: SettingsRepository
) {
    private val _uiState = MutableStateFlow(
        CostExportUiState(
            tileCount = tileCount,
            boxCount = boxCount,
            areaSqFt = areaSqFt,
            tileSize = tileSize,
            layoutType = layoutType,
            priceMode = PriceMode.PER_TILE,
            price = 0.0,
            taxPercentage = 8.5,
            extras = emptyList(),
            costResult = CostCalculator.calculateTotalCost(
                tileCount = tileCount,
                boxCount = boxCount,
                priceMode = PriceMode.PER_TILE,
                price = 0.0,
                taxPercentage = 8.5,
                extras = emptyList()
            ),
            exportState = ExportState.IDLE
        )
    )
    val uiState: StateFlow<CostExportUiState> = _uiState.asStateFlow()
    
    fun onPriceModeChange(priceMode: PriceMode) {
        _uiState.value = _uiState.value.copy(priceMode = priceMode)
        updateCostCalculation()
    }
    
    fun onPriceChange(price: Double) {
        _uiState.value = _uiState.value.copy(price = price)
        updateCostCalculation()
    }
    
    fun onTaxPercentageChange(taxPercentage: Double) {
        _uiState.value = _uiState.value.copy(taxPercentage = taxPercentage)
        updateCostCalculation()
    }
    
    fun onAddExtra() {
        val newExtra = CostExtra(
            description = "Additional Item",
            quantity = 1,
            unitPrice = 0.0
        )
        val newExtras = _uiState.value.extras + newExtra
        _uiState.value = _uiState.value.copy(extras = newExtras)
        updateCostCalculation()
    }
    
    fun onRemoveExtra(index: Int) {
        val newExtras = _uiState.value.extras.toMutableList()
        if (index in newExtras.indices) {
            newExtras.removeAt(index)
            _uiState.value = _uiState.value.copy(extras = newExtras)
            updateCostCalculation()
        }
    }
    
    fun onExtraDescriptionChange(index: Int, description: String) {
        val newExtras = _uiState.value.extras.toMutableList()
        if (index in newExtras.indices) {
            newExtras[index] = newExtras[index].copy(description = description)
            _uiState.value = _uiState.value.copy(extras = newExtras)
            updateCostCalculation()
        }
    }
    
    fun onExtraQuantityChange(index: Int, quantity: Int) {
        val newExtras = _uiState.value.extras.toMutableList()
        if (index in newExtras.indices) {
            newExtras[index] = newExtras[index].copy(quantity = quantity)
            _uiState.value = _uiState.value.copy(extras = newExtras)
            updateCostCalculation()
        }
    }
    
    fun onExtraUnitPriceChange(index: Int, unitPrice: Double) {
        val newExtras = _uiState.value.extras.toMutableList()
        if (index in newExtras.indices) {
            newExtras[index] = newExtras[index].copy(unitPrice = unitPrice)
            _uiState.value = _uiState.value.copy(extras = newExtras)
            updateCostCalculation()
        }
    }
    
    fun onExportPNG() {
        coroutineScope.launch(Dispatchers.Main) {
            _uiState.value = _uiState.value.copy(exportState = ExportState.EXPORTING)
            
            try {
                val projectSummary = generateProjectSummary()
                val projectData = convertToProjectData(projectSummary)
                val result = fileExporter.exportProject(
                    data = projectData,
                    fileName = "tile_project_summary",
                    format = com.tilevision.shared.io.ExportFormat.PNG
                )
                
                result.fold(
                    onSuccess = { filePath ->
                        _uiState.value = _uiState.value.copy(exportState = ExportState.SUCCESS)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(exportState = ExportState.ERROR)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(exportState = ExportState.ERROR)
            }
        }
    }
    
    fun onExportPDF() {
        coroutineScope.launch(Dispatchers.Main) {
            _uiState.value = _uiState.value.copy(exportState = ExportState.EXPORTING)
            
            try {
                val projectSummary = generateProjectSummary()
                val projectData = convertToProjectData(projectSummary)
                val result = fileExporter.exportProject(
                    data = projectData,
                    fileName = "tile_project_summary",
                    format = com.tilevision.shared.io.ExportFormat.PDF
                )
                
                result.fold(
                    onSuccess = { filePath ->
                        _uiState.value = _uiState.value.copy(exportState = ExportState.SUCCESS)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(exportState = ExportState.ERROR)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(exportState = ExportState.ERROR)
            }
        }
    }
    
    fun onExportJSON() {
        coroutineScope.launch(Dispatchers.Main) {
            _uiState.value = _uiState.value.copy(exportState = ExportState.EXPORTING)
            
            try {
                val projectSummary = generateProjectSummary()
                val projectData = convertToProjectData(projectSummary)
                val result = fileExporter.exportProject(
                    data = projectData,
                    fileName = "tile_project_summary",
                    format = com.tilevision.shared.io.ExportFormat.JSON
                )
                
                result.fold(
                    onSuccess = { filePath ->
                        _uiState.value = _uiState.value.copy(exportState = ExportState.SUCCESS)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(exportState = ExportState.ERROR)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(exportState = ExportState.ERROR)
            }
        }
    }
    
    private fun updateCostCalculation() {
        val currentState = _uiState.value
        val costResult = CostCalculator.calculateTotalCost(
            tileCount = currentState.tileCount,
            boxCount = currentState.boxCount,
            priceMode = currentState.priceMode,
            price = currentState.price,
            taxPercentage = currentState.taxPercentage,
            extras = currentState.extras
        )
        
        _uiState.value = currentState.copy(costResult = costResult)
    }
    
    private fun generateProjectSummary(): com.tilevision.shared.domain.ProjectSummary {
        val currentState = _uiState.value
        return CostCalculator.generateProjectSummary(
            tileCount = currentState.tileCount,
            boxCount = currentState.boxCount,
            areaSqFt = currentState.areaSqFt,
            tileWidth = 12.0, // TODO: Get from tile planner
            tileHeight = 12.0, // TODO: Get from tile planner
            layoutType = com.tilevision.shared.domain.TileLayoutType.GRID, // TODO: Get from tile planner
            costResult = currentState.costResult
        )
    }
    
    private fun convertToProjectData(summary: com.tilevision.shared.domain.ProjectSummary): com.tilevision.shared.io.ProjectData {
        return com.tilevision.shared.io.ProjectData(
            id = "project_${getCurrentTimeMillis()}",
            name = "Tile Project",
            description = "Generated tile project with cost analysis",
            measurements = emptyList(), // TODO: Convert from summary if needed
            createdTimestamp = summary.generatedAt,
            modifiedTimestamp = summary.generatedAt
        )
    }
    
    private fun getCurrentTimeMillis(): Long {
        // Simple timestamp - in a real app you'd use proper date/time library
        return 1700000000000L // Placeholder timestamp
    }
    
    /**
     * Create a Surface from the current state
     */
    fun createSurface(
        surfaceName: String,
        surfaceDescription: String,
        polygon: List<com.tilevision.shared.measurement.Vec2>
    ): com.tilevision.shared.data.Surface {
        val currentState = _uiState.value
        return com.tilevision.shared.data.Surface(
            id = "surface_${getCurrentTimeMillis()}",
            name = surfaceName,
            description = surfaceDescription,
            polygon = polygon,
            areaSqFt = currentState.areaSqFt,
            tileCount = currentState.tileCount,
            boxCount = currentState.boxCount,
            tileWidth = 12.0, // TODO: Get from tile planner
            tileHeight = 12.0, // TODO: Get from tile planner
            layoutType = currentState.layoutType,
            groutWidth = 0.125, // TODO: Get from tile planner
            wastePercentage = 10.0, // TODO: Get from tile planner
            costBreakdown = com.tilevision.shared.data.CostBreakdown(
                priceMode = currentState.priceMode.name,
                price = currentState.price,
                taxPercentage = currentState.taxPercentage,
                extras = currentState.extras.map { extra ->
                    com.tilevision.shared.data.CostExtra(
                        description = extra.description,
                        quantity = extra.quantity,
                        unitPrice = extra.unitPrice
                    )
                },
                baseCost = currentState.costResult.baseCost,
                extrasCost = currentState.costResult.extrasCost,
                subtotal = currentState.costResult.subtotal,
                taxAmount = currentState.costResult.taxAmount,
                total = currentState.costResult.total
            ),
            createdTimestamp = getCurrentTimeMillis(),
            modifiedTimestamp = getCurrentTimeMillis()
        )
    }
}

/**
 * UI State for the cost export screen
 */
data class CostExportUiState(
    val tileCount: Int,
    val boxCount: Int,
    val areaSqFt: Double,
    val tileSize: String,
    val layoutType: String,
    val priceMode: PriceMode,
    val price: Double,
    val taxPercentage: Double,
    val extras: List<CostExtra>,
    val costResult: com.tilevision.shared.domain.CostCalculationResult,
    val exportState: ExportState
)

/**
 * Export states
 */
enum class ExportState {
    IDLE,
    EXPORTING,
    SUCCESS,
    ERROR
}
