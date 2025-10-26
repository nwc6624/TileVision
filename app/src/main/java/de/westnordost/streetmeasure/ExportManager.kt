package de.westnordost.streetmeasure

/**
 * Manages export functionality for project measurements
 */
object ExportManager {
    
    /**
     * Builds export data for a project measurement
     * Returns the data needed for PDF generation
     */
    fun buildProjectExportData(projectId: String): ProjectExportData? {
        val project = ProjectRepository.getProjectById(projectId) ?: return null
        
        return ProjectExportData(
            displayName = project.displayName,
            areaFt2 = project.areaFt2,
            timestamp = project.timestamp,
            polygonPoints = project.polygonPoints
        )
    }
}

/**
 * Data class containing project information for export
 */
data class ProjectExportData(
    val displayName: String,
    val areaFt2: Float,
    val timestamp: Long,
    val polygonPoints: List<ProjectPoint2D>
)
