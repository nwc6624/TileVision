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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectAreaSetupScreen(navController: NavController) {
    var areaLength by remember { mutableStateOf("") }
    var areaWidth by remember { mutableStateOf("") }
    var areaName by remember { mutableStateOf("") }
    var areaNotes by remember { mutableStateOf("") }
    var selectedOption by remember { mutableStateOf("manual") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Area Setup") },
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
                    text = "📏",
                    fontSize = 48.sp
                )
                Text(
                    text = "Where are these tiles going?",
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
                        text = "How would you like to measure the area?",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InputOption(
                            icon = Icons.Default.Camera,
                            title = "AR Scan",
                            subtitle = "Use AR to trace corners",
                            isSelected = selectedOption == "ar",
                            onClick = { selectedOption = "ar" },
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

            // Area Details
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Area Details",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                    
                    OutlinedTextField(
                        value = areaName,
                        onValueChange = { areaName = it },
                        label = { Text("Area Name") },
                        supportingText = { Text("e.g., Kitchen Floor, Bathroom Wall") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = areaLength,
                            onValueChange = { areaLength = it },
                            label = { Text("Length (feet)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        
                        OutlinedTextField(
                            value = areaWidth,
                            onValueChange = { areaWidth = it },
                            label = { Text("Width (feet)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    OutlinedTextField(
                        value = areaNotes,
                        onValueChange = { areaNotes = it },
                        label = { Text("Notes (Optional)") },
                        supportingText = { Text("e.g., Skip toilet zone, account for vanity") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Preview Section
            if (areaLength.isNotEmpty() && areaWidth.isNotEmpty()) {
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
                            text = "📊 Area Preview",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        
                        val length = areaLength.toFloatOrNull() ?: 0f
                        val width = areaWidth.toFloatOrNull() ?: 0f
                        val totalArea = length * width
                        
                        Text("Total Area: ${String.format("%.2f", totalArea)} sq ft")
                        Text("Total Area: ${String.format("%.2f", totalArea * 144)} sq in")
                    }
                }
            }

            // Save Button
            Button(
                onClick = { navController.navigate(NavRoutes.CoverageMatch.route) },
                modifier = Modifier.fillMaxWidth(),
                enabled = areaName.isNotEmpty() && areaLength.isNotEmpty() && areaWidth.isNotEmpty(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Save Area",
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