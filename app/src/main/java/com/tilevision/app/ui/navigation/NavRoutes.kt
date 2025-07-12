package com.tilevision.app.ui.navigation

sealed class NavRoutes(val route: String) {
    object Home : NavRoutes("home")
    object TileBatchSetup : NavRoutes("tile_batch_setup")
    object ProjectAreaSetup : NavRoutes("project_area_setup")
    object CoverageMatch : NavRoutes("coverage_match")
    object LayoutExplorer : NavRoutes("layout_explorer")
    object ARPreview : NavRoutes("ar_preview")
    object ProjectSummary : NavRoutes("project_summary")
    object Settings : NavRoutes("settings")
    object SavedProjects : NavRoutes("saved_projects")
    object Camera : NavRoutes("camera")
} 