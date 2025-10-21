package com.tilevision.shared.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.tilevision.shared.screens.HomeScreen
import com.tilevision.shared.screens.MeasureScreen
import com.tilevision.shared.screens.OnboardingScreen
import com.tilevision.shared.screens.ProjectDetailScreen
import com.tilevision.shared.screens.SettingsScreen

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf("onboarding") }
    var projectId by remember { mutableStateOf("") }
    
    when (currentScreen) {
        "onboarding" -> {
            OnboardingScreen(
                onNavigateToHome = {
                    currentScreen = "home"
                }
            )
        }
        
        "home" -> {
            HomeScreen(
                onNavigateToMeasure = { currentScreen = "measure" },
                onNavigateToProjectDetail = { id ->
                    projectId = id
                    currentScreen = "project_detail"
                },
                onNavigateToSettings = { currentScreen = "settings" }
            )
        }
        
        "measure" -> {
            MeasureScreen(
                onNavigateBack = { currentScreen = "home" }
            )
        }
        
        "project_detail" -> {
            ProjectDetailScreen(
                projectId = projectId,
                onNavigateBack = { currentScreen = "home" }
            )
        }
        
        "settings" -> {
            SettingsScreen(
                onNavigateBack = { currentScreen = "home" }
            )
        }
    }
}
