package com.tilevision.shared.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tilevision.shared.settings.MeasurementUnits
import com.tilevision.shared.settings.SettingsRepository
import com.tilevision.shared.settings.UserPrefs
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    settingsRepository: SettingsRepository
) {
    val userPrefs by settingsRepository.userPrefs.collectAsState()
    val isLoading by settingsRepository.isLoading.collectAsState()
    val error by settingsRepository.error.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Load settings when screen is first displayed
    LaunchedEffect(Unit) {
        settingsRepository.loadUserPrefs()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                            text = "Error loading settings",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { settingsRepository.loadUserPrefs() }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            userPrefs != null -> {
                SettingsContent(
                    userPrefs = userPrefs!!,
                    onPrefsChange = { updatedPrefs ->
                        coroutineScope.launch {
                            settingsRepository.saveUserPrefs(updatedPrefs)
                        }
                    },
                    onBackup = {
                        coroutineScope.launch {
                            settingsRepository.backupUserPrefs()
                        }
                    },
                    onRestore = {
                        coroutineScope.launch {
                            settingsRepository.restoreUserPrefs()
                        }
                    },
                    onClearProjects = {
                        coroutineScope.launch {
                            settingsRepository.clearUserPrefs()
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun SettingsContent(
    userPrefs: UserPrefs,
    onPrefsChange: (UserPrefs) -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onClearProjects: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Units Section
        SettingsSection(
            title = "Units",
            content = {
                UnitToggle(
                    currentUnits = userPrefs.units,
                    onUnitsChange = { newUnits ->
                        onPrefsChange(userPrefs.copy(units = newUnits))
                    }
                )
            }
        )
        
        // Default Tile Settings
        SettingsSection(
            title = "Default Tile Settings",
            content = {
                DefaultTileSettings(
                    userPrefs = userPrefs,
                    onPrefsChange = onPrefsChange
                )
            }
        )
        
        // Currency Settings
        SettingsSection(
            title = "Currency",
            content = {
                CurrencySettings(
                    currencySymbol = userPrefs.currencySymbol,
                    onCurrencyChange = { newSymbol ->
                        onPrefsChange(userPrefs.copy(currencySymbol = newSymbol))
                    }
                )
            }
        )
        
        // Data Management
        SettingsSection(
            title = "Data Management",
            content = {
                DataManagementButtons(
                    onBackup = onBackup,
                    onRestore = onRestore,
                    onClearProjects = onClearProjects
                )
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun UnitToggle(
    currentUnits: MeasurementUnits,
    onUnitsChange: (MeasurementUnits) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Measurement Units",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Row {
            MeasurementUnits.entries.forEach { units ->
                FilterChip(
                    selected = currentUnits == units,
                    onClick = { onUnitsChange(units) },
                    label = { Text(units.name) }
                )
                if (units != MeasurementUnits.entries.last()) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DefaultTileSettings(
    userPrefs: UserPrefs,
    onPrefsChange: (UserPrefs) -> Unit
) {
    val unitSuffix = if (userPrefs.units == MeasurementUnits.IMPERIAL) "in" else "cm"
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = userPrefs.defaultTileWidth.toString(),
            onValueChange = { value ->
                value.toDoubleOrNull()?.let { width ->
                    onPrefsChange(userPrefs.copy(defaultTileWidth = width))
                }
            },
            label = { Text("Default Tile Width ($unitSuffix)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = userPrefs.defaultTileHeight.toString(),
            onValueChange = { value ->
                value.toDoubleOrNull()?.let { height ->
                    onPrefsChange(userPrefs.copy(defaultTileHeight = height))
                }
            },
            label = { Text("Default Tile Height ($unitSuffix)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = userPrefs.defaultGroutWidth.toString(),
            onValueChange = { value ->
                value.toDoubleOrNull()?.let { grout ->
                    onPrefsChange(userPrefs.copy(defaultGroutWidth = grout))
                }
            },
            label = { Text("Default Grout Width ($unitSuffix)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = userPrefs.defaultWastePercentage.toString(),
            onValueChange = { value ->
                value.toDoubleOrNull()?.let { waste ->
                    onPrefsChange(userPrefs.copy(defaultWastePercentage = waste))
                }
            },
            label = { Text("Default Waste Percentage (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = userPrefs.defaultTilesPerBox.toString(),
            onValueChange = { value ->
                value.toIntOrNull()?.let { tiles ->
                    onPrefsChange(userPrefs.copy(defaultTilesPerBox = tiles))
                }
            },
            label = { Text("Default Tiles per Box") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CurrencySettings(
    currencySymbol: String,
    onCurrencyChange: (String) -> Unit
) {
    OutlinedTextField(
        value = currencySymbol,
        onValueChange = onCurrencyChange,
        label = { Text("Currency Symbol") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
private fun DataManagementButtons(
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onClearProjects: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onBackup,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Backup Data")
        }
        
        Button(
            onClick = onRestore,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Restore Data")
        }
        
        OutlinedButton(
            onClick = onClearProjects,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear Projects")
        }
    }
}