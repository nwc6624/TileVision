package com.tilevision.shared.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tilevision.shared.cost.CostExportViewModel
import com.tilevision.shared.cost.CostExportUiState
import com.tilevision.shared.domain.PriceMode
import com.tilevision.shared.domain.CostExtra

@Composable
fun CostExportScreen(
    onNavigateBack: () -> Unit,
    onSaveProject: (String, String) -> Unit,
    viewModel: CostExportViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Cost & Export") },
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
                onPriceModeChange = viewModel::onPriceModeChange,
                onPriceChange = viewModel::onPriceChange,
                onTaxPercentageChange = viewModel::onTaxPercentageChange,
                onAddExtra = viewModel::onAddExtra,
                onRemoveExtra = viewModel::onRemoveExtra,
                onExtraDescriptionChange = viewModel::onExtraDescriptionChange,
                onExtraQuantityChange = viewModel::onExtraQuantityChange,
                onExtraUnitPriceChange = viewModel::onExtraUnitPriceChange,
                modifier = Modifier
                    .width(350.dp)
                    .fillMaxHeight()
            )
            
            // Summary and Export Panel
            SummaryPanel(
                uiState = uiState,
                onExportPNG = viewModel::onExportPNG,
                onExportPDF = viewModel::onExportPDF,
                onExportJSON = viewModel::onExportJSON,
                onSaveProject = onSaveProject,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun InputPanel(
    uiState: CostExportUiState,
    onPriceModeChange: (PriceMode) -> Unit,
    onPriceChange: (Double) -> Unit,
    onTaxPercentageChange: (Double) -> Unit,
    onAddExtra: () -> Unit,
    onRemoveExtra: (Int) -> Unit,
    onExtraDescriptionChange: (Int, String) -> Unit,
    onExtraQuantityChange: (Int, Int) -> Unit,
    onExtraUnitPriceChange: (Int, Double) -> Unit,
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
                text = "Cost Configuration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            // Price Mode
            Text(
                text = "Price Mode",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { onPriceModeChange(PriceMode.PER_TILE) },
                    label = { Text("Per Tile") },
                    selected = uiState.priceMode == PriceMode.PER_TILE,
                    modifier = Modifier.weight(1f)
                )
                
                FilterChip(
                    onClick = { onPriceModeChange(PriceMode.PER_BOX) },
                    label = { Text("Per Box") },
                    selected = uiState.priceMode == PriceMode.PER_BOX,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Price
            OutlinedTextField(
                value = uiState.price.toString(),
                onValueChange = { onPriceChange(it.toDoubleOrNull() ?: 0.0) },
                label = { Text("Price ($$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            
            // Tax Percentage
            OutlinedTextField(
                value = uiState.taxPercentage.toString(),
                onValueChange = { onTaxPercentageChange(it.toDoubleOrNull() ?: 0.0) },
                label = { Text("Tax %") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            
            Divider()
            
            // Extras Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Additional Items",
                    style = MaterialTheme.typography.titleMedium
                )
                
                IconButton(onClick = onAddExtra) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Extra")
                }
            }
            
            // Extras List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.extras.indices.toList()) { index ->
                    ExtraItem(
                        extra = uiState.extras[index],
                        onDescriptionChange = { onExtraDescriptionChange(index, it) },
                        onQuantityChange = { onExtraQuantityChange(index, it) },
                        onUnitPriceChange = { onExtraUnitPriceChange(index, it) },
                        onRemove = { onRemoveExtra(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExtraItem(
    extra: CostExtra,
    onDescriptionChange: (String) -> Unit,
    onQuantityChange: (Int) -> Unit,
    onUnitPriceChange: (Double) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Extra Item",
                    style = MaterialTheme.typography.labelMedium
                )
                
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove")
                }
            }
            
            OutlinedTextField(
                value = extra.description,
                onValueChange = onDescriptionChange,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = extra.quantity.toString(),
                    onValueChange = { onQuantityChange(it.toIntOrNull() ?: 1) },
                    label = { Text("Qty") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = extra.unitPrice.toString(),
                    onValueChange = { onUnitPriceChange(it.toDoubleOrNull() ?: 0.0) },
                    label = { Text("Unit Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Text(
                text = "Total: $${formatPrice(extra.cost)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SummaryPanel(
    uiState: CostExportUiState,
    onExportPNG: () -> Unit,
    onExportPDF: () -> Unit,
    onExportJSON: () -> Unit,
    onSaveProject: (String, String) -> Unit,
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
        ) {
            Text(
                text = "Cost Summary",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Project Info
            ProjectInfoCard(
                tileCount = uiState.tileCount,
                boxCount = uiState.boxCount,
                areaSqFt = uiState.areaSqFt,
                tileSize = uiState.tileSize,
                layoutType = uiState.layoutType
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Cost Breakdown
            CostBreakdownCard(
                costResult = uiState.costResult
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Export Actions
            ExportActionsCard(
                onExportPNG = onExportPNG,
                onExportPDF = onExportPDF,
                onExportJSON = onExportJSON,
                exportState = uiState.exportState
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Save Project Button
            SaveProjectCard(
                onSaveProject = onSaveProject,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ProjectInfoCard(
    tileCount: Int,
    boxCount: Int,
    areaSqFt: Double,
    tileSize: String,
    layoutType: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Project Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            InfoRow("Area", "${formatArea(areaSqFt)} ft²")
            InfoRow("Tiles", "$tileCount tiles")
            InfoRow("Boxes", "$boxCount boxes")
            InfoRow("Tile Size", tileSize)
            InfoRow("Layout", layoutType)
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun CostBreakdownCard(
    costResult: com.tilevision.shared.domain.CostCalculationResult
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Cost Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            // Line Items
            costResult.lineItems.forEach { item ->
                CostLineItemRow(item)
            }
            
            Divider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.3f))
            
            // Totals
            CostRow("Subtotal", costResult.subtotal)
            CostRow("Tax", costResult.taxAmount)
            CostRow("Total", costResult.total, isTotal = true)
        }
    }
}

@Composable
private fun CostLineItemRow(
    item: com.tilevision.shared.domain.CostLineItem
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "${item.quantity} @ $${formatPrice(item.unitPrice)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
        Text(
            text = "$${formatPrice(item.total)}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun CostRow(
    label: String,
    amount: Double,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = "$${formatPrice(amount)}",
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun ExportActionsCard(
    onExportPNG: () -> Unit,
    onExportPDF: () -> Unit,
    onExportJSON: () -> Unit,
    exportState: com.tilevision.shared.cost.ExportState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Export Options",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExportButton(
                    icon = Icons.Filled.Add,
                    label = "PNG",
                    onClick = onExportPNG,
                    enabled = exportState != com.tilevision.shared.cost.ExportState.EXPORTING,
                    modifier = Modifier.weight(1f)
                )
                
                ExportButton(
                    icon = Icons.Filled.Add,
                    label = "PDF",
                    onClick = onExportPDF,
                    enabled = exportState != com.tilevision.shared.cost.ExportState.EXPORTING,
                    modifier = Modifier.weight(1f)
                )
                
                ExportButton(
                    icon = Icons.Filled.Add,
                    label = "JSON",
                    onClick = onExportJSON,
                    enabled = exportState != com.tilevision.shared.cost.ExportState.EXPORTING,
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (exportState == com.tilevision.shared.cost.ExportState.EXPORTING) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Exporting...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun ExportButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun SaveProjectCard(
    onSaveProject: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var projectName by remember { mutableStateOf("") }
    var projectDescription by remember { mutableStateOf("") }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Save Project",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = "Save this surface as a project for future reference",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Project")
            }
        }
    }
    
    if (showDialog) {
        SaveProjectDialog(
            projectName = projectName,
            projectDescription = projectDescription,
            onProjectNameChange = { projectName = it },
            onProjectDescriptionChange = { projectDescription = it },
            onSave = {
                onSaveProject(projectName, projectDescription)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun SaveProjectDialog(
    projectName: String,
    projectDescription: String,
    onProjectNameChange: (String) -> Unit,
    onProjectDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Project") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = projectName,
                    onValueChange = onProjectNameChange,
                    label = { Text("Project Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = projectDescription,
                    onValueChange = onProjectDescriptionChange,
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = projectName.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatDouble(value: Double): String {
    val rounded = (value * 10).toInt().toDouble() / 10.0
    return rounded.toString()
}

private fun formatPrice(price: Double): String {
    return formatDouble(price, 2)
}

private fun formatArea(area: Double): String {
    return formatDouble(area, 1)
}

private fun formatDouble(value: Double, decimals: Int): String {
    val multiplier = 10.0 * decimals // Simple approximation
    val rounded = kotlin.math.round(value * multiplier) / multiplier
    return rounded.toString()
}
