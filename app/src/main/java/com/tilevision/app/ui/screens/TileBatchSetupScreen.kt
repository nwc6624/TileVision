@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.tilevision.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tilevision.app.ui.navigation.NavRoutes

@Composable
fun TileBatchSetupScreen(navController: NavController) {
    var tileLength by remember { mutableStateOf("") }
    var tileWidth by remember { mutableStateOf("") }
    var tileQuantity by remember { mutableStateOf("") }
    var groutWidth by remember { mutableStateOf("1/8") }
    var wastePercentage by remember { mutableStateOf("10") }
    var selectedOption by remember { mutableStateOf("manual") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tile Batch Setup") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📐",
                    fontSize = 48.sp
                )
                Text(
                    text = "Let's log your tile batch",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // Input Method Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "How would you like to input tile dimensions?",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InputOption(
                            icon = Icons.Default.Camera,
                            title = "Scan Tile",
                            subtitle = "Use camera to auto-detect",
                            isSelected = selectedOption == "scan",
                            onClick = { selectedOption = "scan" },
                            modifier = Modifier.weight(1f)
                        )
                        
                        InputOption(
                            icon = Icons.Default.Edit,
                            title = "Manual Entry",
                            subtitle = "Enter dimensions manually",
                            isSelected = selectedOption == "manual",
                            onClick = { selectedOption = "manual" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Tile Dimensions Input
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Tile Dimensions",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = tileLength,
                            onValueChange = { tileLength = it },
                            label = { Text("Length (inches)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        
                        OutlinedTextField(
                            value = tileWidth,
                            onValueChange = { tileWidth = it },
                            label = { Text("Width (inches)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    OutlinedTextField(
                        value = tileQuantity,
                        onValueChange = { tileQuantity = it },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Grout and Waste Settings
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Project Settings",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                    
                    OutlinedTextField(
                        value = groutWidth,
                        onValueChange = { groutWidth = it },
                        label = { Text("Grout Line Width") },
                        supportingText = { Text("Common: 1/16\", 1/8\", 3/16\"") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = wastePercentage,
                        onValueChange = { wastePercentage = it },
                        label = { Text("Waste Percentage") },
                        supportingText = { Text("Recommended: 5-15%") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Preview Section
            if (tileLength.isNotEmpty() && tileWidth.isNotEmpty() && tileQuantity.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📊 Coverage Preview",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        
                        val length = tileLength.toFloatOrNull() ?: 0f
                        val width = tileWidth.toFloatOrNull() ?: 0f
                        val quantity = tileQuantity.toIntOrNull() ?: 0
                        val totalArea = length * width * quantity
                        
                        Text("Total Tile Area: ${String.format("%.2f", totalArea)} sq in")
                        Text("Total Tile Area: ${String.format("%.2f", totalArea / 144)} sq ft")
                    }
                }
            }

            // Save Button
            Button(
                onClick = { navController.navigate(NavRoutes.ProjectAreaSetup.route) },
                modifier = Modifier.fillMaxWidth(),
                enabled = tileLength.isNotEmpty() && tileWidth.isNotEmpty() && tileQuantity.isNotEmpty(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Save Tile Batch",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun InputOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer 
                       else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
} 