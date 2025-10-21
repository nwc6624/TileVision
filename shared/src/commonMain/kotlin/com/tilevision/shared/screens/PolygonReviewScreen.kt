package com.tilevision.shared.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tilevision.shared.measurement.Vec2
import com.tilevision.shared.polygon.PolygonReviewViewModel
import com.tilevision.shared.polygon.PolygonReviewUiState
import kotlin.math.*

@Composable
fun PolygonReviewScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTilePlanner: (List<Vec2>) -> Unit,
    viewModel: PolygonReviewViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Review Polygon") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                TextButton(
                    onClick = { onNavigateToTilePlanner(uiState.points) }
                ) {
                    Text("Continue")
                }
            }
        )
        
        // Measurement Display
        MeasurementCard(
            areaFt2 = uiState.areaFt2,
            perimeterFt = uiState.perimeterFt,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        
        // Polygon Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            PolygonCanvas(
                points = uiState.points,
                onPointDrag = { index, newPosition ->
                    viewModel.onPointDrag(index, newPosition)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun MeasurementCard(
    areaFt2: Double,
    perimeterFt: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MeasurementItem(
                label = "Area",
                value = formatDouble(areaFt2),
                unit = "ft²"
            )
            
            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
            )
            
            MeasurementItem(
                label = "Perimeter",
                value = formatDouble(perimeterFt),
                unit = "ft"
            )
        }
    }
}

@Composable
private fun MeasurementItem(
    label: String,
    value: String,
    unit: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
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

@Composable
private fun PolygonCanvas(
    points: List<Vec2>,
    onPointDrag: (Int, Vec2) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(12.dp)
            )
    ) {
        if (points.isNotEmpty()) {
            drawPolygon(points)
            drawHandles(points, onPointDrag)
        } else {
            drawEmptyState()
        }
    }
}

private fun DrawScope.drawPolygon(points: List<Vec2>) {
    if (points.size < 2) return
    
    val path = Path()
    val scaledPoints = scalePointsToCanvas(points)
    
    path.moveTo(scaledPoints[0].x, scaledPoints[0].y)
    
    for (i in 1 until scaledPoints.size) {
        path.lineTo(scaledPoints[i].x, scaledPoints[i].y)
    }
    
    if (points.size >= 3) {
        path.close()
    }
    
    // Draw polygon fill
    drawPath(
        path = path,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    )
    
    // Draw polygon outline
    drawPath(
        path = path,
        color = MaterialTheme.colorScheme.primary,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
    )
}

private fun DrawScope.drawHandles(
    points: List<Vec2>,
    onPointDrag: (Int, Vec2) -> Unit
) {
    val scaledPoints = scalePointsToCanvas(points)
    val handleRadius = 12.dp.toPx()
    
    scaledPoints.forEachIndexed { index, point ->
        // Draw handle
        drawCircle(
            color = MaterialTheme.colorScheme.primary,
            radius = handleRadius,
            center = Offset(point.x, point.y)
        )
        
        // Draw handle center
        drawCircle(
            color = Color.White,
            radius = handleRadius * 0.6f,
            center = Offset(point.x, point.y)
        )
        
        // Draw point number
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 24f
                textAlign = android.graphics.Paint.Align.CENTER
            }
            drawText(
                (index + 1).toString(),
                point.x,
                point.y + 8f,
                paint
            )
        }
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
            "No polygon to display",
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

private fun formatDouble(value: Double): String {
    val rounded = (value * 10).toInt().toDouble() / 10.0
    return rounded.toString()
}
