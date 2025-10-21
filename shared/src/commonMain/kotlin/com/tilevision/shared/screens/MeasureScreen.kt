package com.tilevision.shared.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tilevision.shared.measurement.MeasureViewModel
import com.tilevision.shared.measurement.MeasureUiState

@Composable
fun MeasureScreen(
    onNavigateBack: () -> Unit,
    viewModel: MeasureViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // AR View Container (Underlay)
        ArViewContainer(
            modifier = Modifier.fillMaxSize(),
            onTap = { x, y -> viewModel.onAddPoint(x, y) }
        )
        
        // Overlay UI
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar with measurements
            TopMeasurementBar(
                areaFt2 = uiState.areaFt2,
                perimeterFt = uiState.perimeterFt,
                onNavigateBack = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Center Reticle
            CenterReticle(
                trackingStable = uiState.trackingStable,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Bottom Action Row
            BottomActionRow(
                canFinish = uiState.canFinish,
                onAddPoint = { x, y -> viewModel.onAddPoint(x, y) },
                onUndo = viewModel::onUndo,
                onReset = viewModel::onReset,
                onFinish = viewModel::onFinish,
                onToggleSnap = viewModel::onToggleSnap,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Tip Text Overlay
        if (uiState.tipText.isNotEmpty()) {
            TipTextOverlay(
                text = uiState.tipText,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun ArViewContainer(
    modifier: Modifier = Modifier,
    onTap: (Float, Float) -> Unit
) {
    // This would be replaced with actual AR view implementation
    Box(
        modifier = modifier
            .background(Color.Black)
            .pointerInput(Unit) {
                // Handle tap events for AR raycast
                detectTapGestures { offset ->
                    onTap(offset.x, offset.y)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "AR View\n(Tap to add points)",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun TopMeasurementBar(
    areaFt2: Double,
    perimeterFt: Double,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                MeasurementDisplay(
                    label = "Area",
                    value = formatDouble(areaFt2),
                    unit = "ft²"
                )
                
                MeasurementDisplay(
                    label = "Perimeter",
                    value = formatDouble(perimeterFt),
                    unit = "ft"
                )
            }
        }
    }
}

@Composable
private fun MeasurementDisplay(
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CenterReticle(
    trackingStable: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Outer ring
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    if (trackingStable) 
                        Color.Green.copy(alpha = 0.3f) 
                    else 
                        Color.Red.copy(alpha = 0.3f)
                )
        )
        
        // Inner crosshair
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Reticle",
            tint = if (trackingStable) Color.Green else Color.Red,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun BottomActionRow(
    canFinish: Boolean,
    onAddPoint: (Float, Float) -> Unit,
    onUndo: () -> Unit,
    onReset: () -> Unit,
    onFinish: () -> Unit,
    onToggleSnap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionButton(
                icon = Icons.Filled.Add,
                label = "Add",
                onClick = { onAddPoint(0f, 0f) } // Center tap
            )
            
            ActionButton(
                icon = Icons.Filled.ArrowBack,
                label = "Undo",
                onClick = onUndo
            )
            
            ActionButton(
                icon = Icons.Filled.Refresh,
                label = "Reset",
                onClick = onReset
            )
            
            ActionButton(
                icon = Icons.Filled.Settings,
                label = "Snap",
                onClick = onToggleSnap
            )
            
            ActionButton(
                icon = Icons.Filled.Check,
                label = "Finish",
                onClick = onFinish,
                enabled = canFinish
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled) 
                MaterialTheme.colorScheme.onSurface 
            else 
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}

@Composable
private fun TipTextOverlay(
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.9f)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.inverseOnSurface,
            modifier = Modifier.padding(12.dp)
        )
    }
}

private fun formatDouble(value: Double): String {
    val rounded = (value * 10).toInt().toDouble() / 10.0
    return rounded.toString()
}