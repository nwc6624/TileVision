package com.tilevision.shared.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tilevision.shared.data.Project
import com.tilevision.shared.data.ProjectRepository
import com.tilevision.shared.data.Surface
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    projectRepository: ProjectRepository,
    onExportProject: (Project) -> Unit
) {
    var project by remember { mutableStateOf<Project?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    // Load project when screen is first displayed
    LaunchedEffect(projectId) {
        isLoading = true
        error = null
        
        projectRepository.getProject(projectId).fold(
            onSuccess = { loadedProject ->
                project = loadedProject
                isLoading = false
            },
            onFailure = { exception ->
                error = exception.message
                isLoading = false
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { project?.let { onExportProject(it) } }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Export")
                    }
                    IconButton(onClick = { /* TODO: Edit project */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                projectRepository.deleteProject(projectId)
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error loading project",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Go Back")
                        }
                    }
                }
            }
            project == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Project not found")
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ProjectInfoCard(project = project!!)
                    }
                    
                    item {
                        Text(
                            text = "Surfaces (${project!!.surfaces.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    
                    if (project!!.surfaces.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No surfaces in this project yet",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(project!!.surfaces) { surface ->
                            SurfaceCard(surface = surface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectInfoCard(project: Project) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = project.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = project.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Created: ${formatTimestamp(project.createdTimestamp)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Modified: ${formatTimestamp(project.modifiedTimestamp)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SurfaceCard(surface: Surface) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = surface.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${formatArea(surface.areaSqFt)} ft²",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = surface.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${surface.tileCount} tiles",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "${surface.tileWidth}\" x ${surface.tileHeight}\"",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                surface.costBreakdown?.let { cost ->
                    Text(
                        text = "$${formatPrice(cost.total)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    // Simple timestamp formatting - in a real app you'd use proper date formatting
    val now = getCurrentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        diff < 604800000 -> "${diff / 86400000}d ago"
        else -> "${diff / 604800000}w ago"
    }
}

private fun formatArea(area: Double): String {
    return formatDouble(area, 1)
}

private fun formatPrice(price: Double): String {
    return formatDouble(price, 2)
}

private fun formatDouble(value: Double, decimals: Int): String {
    val multiplier = 10.0 * decimals // Simple approximation
    val rounded = kotlin.math.round(value * multiplier) / multiplier
    return rounded.toString()
}

private fun getCurrentTimeMillis(): Long {
    // Simple timestamp - in a real app you'd use proper date/time library
    return 1700000000000L
}
