package com.tilevision.shared.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.tilevision.shared.measurement.MeasureViewModelProvider
import com.tilevision.shared.measurement.Vec2
import com.tilevision.shared.platform.PlatformServices
import com.tilevision.shared.polygon.PolygonReviewViewModel
import com.tilevision.shared.screens.HomeScreen
import com.tilevision.shared.screens.MeasureScreen
import com.tilevision.shared.screens.OnboardingScreen
import com.tilevision.shared.screens.PolygonReviewScreen
import com.tilevision.shared.screens.ProjectDetailScreen
import com.tilevision.shared.screens.SettingsScreen
import com.tilevision.shared.screens.TilePlannerScreen
import com.tilevision.shared.tile.TilePlannerViewModel
import kotlinx.coroutines.Dispatchers

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf("onboarding") }
    var projectId by remember { mutableStateOf("") }
    var polygonPoints by remember { mutableStateOf<List<Vec2>>(emptyList()) }
    var polygonArea by remember { mutableStateOf(0.0) }
    
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
            val coroutineScope = rememberCoroutineScope()
            val measureViewModel = remember {
                MeasureViewModelProvider.create(
                    arSessionManager = PlatformServices.createArSessionManager(),
                    haptics = PlatformServices.createHaptics(),
                    coroutineScope = coroutineScope
                )
            }
            
            MeasureScreen(
                onNavigateBack = { currentScreen = "home" },
                onNavigateToPolygonReview = { points ->
                    polygonPoints = points
                    currentScreen = "polygon_review"
                },
                viewModel = measureViewModel
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
        
        "polygon_review" -> {
            val coroutineScope = rememberCoroutineScope()
            val polygonReviewViewModel = remember {
                PolygonReviewViewModel(
                    initialPoints = polygonPoints,
                    coroutineScope = coroutineScope
                )
            }
            
            PolygonReviewScreen(
                onNavigateBack = { currentScreen = "measure" },
                onNavigateToTilePlanner = { points ->
                    polygonPoints = points
                    polygonArea = calculateArea(points)
                    currentScreen = "tile_planner"
                },
                viewModel = polygonReviewViewModel
            )
        }
        
        "tile_planner" -> {
            val coroutineScope = rememberCoroutineScope()
            val tilePlannerViewModel = remember {
                TilePlannerViewModel(
                    polygon = polygonPoints,
                    areaSqFt = polygonArea,
                    coroutineScope = coroutineScope
                )
            }
            
            TilePlannerScreen(
                polygon = polygonPoints,
                onNavigateBack = { currentScreen = "polygon_review" },
                viewModel = tilePlannerViewModel
            )
        }
    }
}

private fun calculateArea(points: List<Vec2>): Double {
    if (points.size < 3) return 0.0
    
    // Shoelace formula for polygon area
    var area = 0.0
    val n = points.size
    for (i in 0 until n) {
        val j = (i + 1) % n
        area += points[i].x * points[j].y
        area -= points[j].x * points[i].y
    }
    area = kotlin.math.abs(area) / 2.0
    
    // Convert from square meters to square feet
    return area * 10.764
}
