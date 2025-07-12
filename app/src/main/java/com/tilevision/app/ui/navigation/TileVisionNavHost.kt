package com.tilevision.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tilevision.app.ui.screens.HomeScreen
import com.tilevision.app.ui.screens.TileBatchSetupScreen
import com.tilevision.app.ui.screens.ProjectAreaSetupScreen
import com.tilevision.app.ui.screens.CoverageMatchScreen
import com.tilevision.app.ui.screens.LayoutExplorerScreen
import com.tilevision.app.ui.screens.ARPreviewScreen
import com.tilevision.app.ui.screens.ProjectSummaryScreen
import com.tilevision.app.ui.screens.SettingsScreen
import com.tilevision.app.ui.screens.SavedProjectsScreen

@Composable
fun TileVisionNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Home.route
    ) {
        composable(NavRoutes.Home.route) {
            HomeScreen(navController = navController)
        }
        
        composable(NavRoutes.TileBatchSetup.route) {
            TileBatchSetupScreen(navController = navController)
        }
        
        composable(NavRoutes.ProjectAreaSetup.route) {
            ProjectAreaSetupScreen(navController = navController)
        }
        
        composable(NavRoutes.CoverageMatch.route) {
            CoverageMatchScreen(navController = navController)
        }
        
        composable(NavRoutes.LayoutExplorer.route) {
            LayoutExplorerScreen(navController = navController)
        }
        
        composable(NavRoutes.ARPreview.route) {
            ARPreviewScreen(navController = navController)
        }
        
        composable(NavRoutes.ProjectSummary.route) {
            ProjectSummaryScreen(navController = navController)
        }
        
        composable(NavRoutes.Settings.route) {
            SettingsScreen(navController = navController)
        }
        
        composable(NavRoutes.SavedProjects.route) {
            SavedProjectsScreen(navController = navController)
        }
    }
} 