package com.tilevision.shared.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tilevision.shared.measurement.Vec2
import com.tilevision.shared.tile.TilePlannerViewModel
import com.tilevision.shared.tile.TilePlannerUiState
import com.tilevision.shared.domain.TileLayoutType

@Composable
fun TilePlannerScreen(
    polygon: List<Vec2>,
    onNavigateBack: () -> Unit,
    viewModel: TilePlannerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Tile Planner") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
        
        // Content
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Input Panel
            InputPanel(
                uiState = uiState,
                onTileWidthChange = viewModel::onTileWidthChange,
                onTileHeightChange = viewModel::onTileHeightChange,
                onGroutWidthChange = viewModel::onGroutWidthChange,
                onLayoutTypeChange = viewModel::onLayoutTypeChange,
                onWastePercentageChange = viewModel::onWastePercentageChange,
                onTilesPerBoxChange = viewModel::onTilesPerBoxChange,
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
            )
            
            // Preview Panel
            PreviewPanel(
                polygon = polygon,
                uiState = uiState,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun InputPanel(
    uiState: TilePlannerUiState,
    onTileWidthChange: (Double) -> Unit,
    onTileHeightChange: (Double) -> Unit,
    onGroutWidthChange: (Double) -> Unit,
    onLayoutTypeChange: (TileLayoutType) -> Unit,
    onWastePercentageChange: (Double) -> Unit,
    onTilesPerBoxChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Tile Configuration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            // Tile Size
            Text(
                text = "Tile Size (inches)",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.tileWidth.toString(),
                    onValueChange = { onTileWidthChange(it.toDoubleOrNull() ?: 0.0) },
                    label = { Text("Width") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = uiState.tileHeight.toString(),
                    onValueChange = { onTileHeightChange(it.toDoubleOrNull() ?: 0.0) },
                    label = { Text("Height") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Grout Width
            OutlinedTextField(
                value = uiState.groutWidth.toString(),
                onValueChange = { onGroutWidthChange(it.toDoubleOrNull() ?: 0.0) },
                label = { Text("Grout Width (inches)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            
            // Layout Type
            Text(
                text = "Layout Pattern",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { onLayoutTypeChange(TileLayoutType.GRID) },
                    label = { Text("Grid") },
                    selected = uiState.layoutType == TileLayoutType.GRID,
                    modifier = Modifier.weight(1f)
                )
                
                FilterChip(
                    onClick = { onLayoutTypeChange(TileLayoutType.BRICK) },
                    label = { Text("Brick") },
                    selected = uiState.layoutType == TileLayoutType.BRICK,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Waste Percentage
            OutlinedTextField(
                value = uiState.wastePercentage.toString(),
                onValueChange = { onWastePercentageChange(it.toDoubleOrNull() ?: 0.0) },
                label = { Text("Waste %") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            
            // Tiles Per Box
            OutlinedTextField(
                value = uiState.tilesPerBox.toString(),
                onValueChange = { onTilesPerBoxChange(it.toIntOrNull() ?: 1) },
                label = { Text("Tiles Per Box") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            Divider()
            
            // Results
            Text(
                text = "Calculation Results",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            ResultCard(
                title = "Tiles Needed",
                value = uiState.tileCount.toString(),
                unit = "tiles"
            )
            
            ResultCard(
                title = "Boxes Needed",
                value = uiState.boxCount.toString(),
                unit = "boxes"
            )
            
            ResultCard(
                title = "Total Area",
                value = formatDouble(uiState.totalAreaSqFt),
                unit = "ft²"
            )
            
            ResultCard(
                title = "Waste Area",
                value = formatDouble(uiState.wasteAreaSqFt),
                unit = "ft²"
            )
        }
    }
}

@Composable
private fun ResultCard(
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun PreviewPanel(
    polygon: List<Vec2>,
    uiState: TilePlannerUiState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Layout Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                TilePreviewCanvas(
                    polygon = polygon,
                    tileLayout = uiState.tileLayout,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun TilePreviewCanvas(
    polygon: List<Vec2>,
    tileLayout: com.tilevision.shared.domain.TileLayoutPreview,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(8.dp)
            )
    ) {
        if (polygon.isNotEmpty()) {
            drawPolygon(polygon)
            drawTileLayout(tileLayout)
        } else {
            drawEmptyState()
        }
    }
}

private fun DrawScope.drawPolygon(polygon: List<Vec2>) {
    if (polygon.size < 2) return
    
    val path = Path()
    val scaledPoints = scalePointsToCanvas(polygon)
    
    path.moveTo(scaledPoints[0].x, scaledPoints[0].y)
    
    for (i in 1 until scaledPoints.size) {
        path.lineTo(scaledPoints[i].x, scaledPoints[i].y)
    }
    
    if (polygon.size >= 3) {
        path.close()
    }
    
    // Draw polygon fill
    drawPath(
        path = path,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    )
    
    // Draw polygon outline
    drawPath(
        path = path,
        color = MaterialTheme.colorScheme.primary,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
    )
}

private fun DrawScope.drawTileLayout(tileLayout: com.tilevision.shared.domain.TileLayoutPreview) {
    val scaledTiles = scaleTilesToCanvas(tileLayout)
    
    scaledTiles.forEach { tile ->
        // Draw tile with hatch pattern
        drawRect(
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
            topLeft = Offset(tile.x, tile.y),
            size = androidx.compose.ui.geometry.Size(tile.width, tile.height)
        )
        
        // Draw tile border
        drawRect(
            color = MaterialTheme.colorScheme.secondary,
            topLeft = Offset(tile.x, tile.y),
            size = androidx.compose.ui.geometry.Size(tile.width, tile.height),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
        )
        
        // Draw hatch pattern
        drawHatchPattern(
            topLeft = Offset(tile.x, tile.y),
            size = androidx.compose.ui.geometry.Size(tile.width, tile.height)
        )
    }
}

private fun DrawScope.drawHatchPattern(
    topLeft: Offset,
    size: androidx.compose.ui.geometry.Size
) {
    val spacing = 4.dp.toPx()
    val color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.3f)
    
    // Draw diagonal lines
    var x = topLeft.x
    while (x < topLeft.x + size.width) {
        drawLine(
            color = color,
            start = Offset(x, topLeft.y),
            end = Offset(x + size.height, topLeft.y + size.height),
            strokeWidth = 1.dp.toPx()
        )
        x += spacing
    }
}

private fun DrawScope.drawEmptyState() {
    val centerX = size.width / 2
    val centerY = size.height / 2
    
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 48f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        drawText(
            "No preview available",
            centerX,
            centerY,
            paint
        )
    }
}

private fun DrawScope.scalePointsToCanvas(points: List<Vec2>): List<Offset> {
    if (points.isEmpty()) return emptyList()
    
    val bounds = calculateBounds(points)
    val padding = 50.dp.toPx()
    val availableWidth = size.width - (padding * 2)
    val availableHeight = size.height - (padding * 2)
    
    val scaleX = if (bounds.width > 0) availableWidth / bounds.width else 1f
    val scaleY = if (bounds.height > 0) availableHeight / bounds.height else 1f
    val scale = minOf(scaleX, scaleY)
    
    val offsetX = (size.width - bounds.width * scale) / 2
    val offsetY = (size.height - bounds.height * scale) / 2
    
    return points.map { point ->
        Offset(
            x = (point.x - bounds.minX) * scale + offsetX,
            y = (point.y - bounds.minY) * scale + offsetY
        )
    }
}

private fun DrawScope.scaleTilesToCanvas(tileLayout: com.tilevision.shared.domain.TileLayoutPreview): List<ScaledTile> {
    if (tileLayout.tiles.isEmpty()) return emptyList()
    
    val bounds = tileLayout.bounds
    val padding = 50.dp.toPx()
    val availableWidth = size.width - (padding * 2)
    val availableHeight = size.height - (padding * 2)
    
    val scaleX = if (bounds.maxX - bounds.minX > 0) availableWidth / (bounds.maxX - bounds.minX).toFloat() else 1f
    val scaleY = if (bounds.maxY - bounds.minY > 0) availableHeight / (bounds.maxY - bounds.minY).toFloat() else 1f
    val scale = minOf(scaleX, scaleY)
    
    val offsetX = (size.width - (bounds.maxX - bounds.minX) * scale) / 2
    val offsetY = (size.height - (bounds.maxY - bounds.minY) * scale) / 2
    
    return tileLayout.tiles.map { tile ->
        ScaledTile(
            x = (tile.x - bounds.minX) * scale + offsetX,
            y = (tile.y - bounds.minY) * scale + offsetY,
            width = tile.width * scale,
            height = tile.height * scale
        )
    }
}

private fun calculateBounds(points: List<Vec2>): Bounds {
    if (points.isEmpty()) {
        return Bounds(0f, 0f, 0f, 0f)
    }
    
    val minX = points.minOf { it.x }
    val maxX = points.maxOf { it.x }
    val minY = points.minOf { it.y }
    val maxY = points.maxOf { it.y }
    
    return Bounds(minX, maxX, minY, maxY)
}

private data class Bounds(
    val minX: Float,
    val maxX: Float,
    val minY: Float,
    val maxY: Float
) {
    val width: Float get() = maxX - minX
    val height: Float get() = maxY - minY
}

private data class ScaledTile(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

private fun formatDouble(value: Double): String {
    val rounded = (value * 10).toInt().toDouble() / 10.0
    return rounded.toString()
}
