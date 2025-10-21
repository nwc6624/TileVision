package com.tilevision.shared.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing projects
 */
class ProjectRepository(private val localDataSource: LocalDataSource) {
    
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Load all projects from local storage
     */
    suspend fun loadProjects() {
        _isLoading.value = true
        _error.value = null
        
        try {
            val result = localDataSource.listProjects()
            result.fold(
                onSuccess = { projects ->
                    _projects.value = projects
                },
                onFailure = { exception ->
                    _error.value = "Failed to load projects: ${exception.message}"
                }
            )
        } catch (e: Exception) {
            _error.value = "Unexpected error loading projects: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Get a specific project by ID
     */
    suspend fun getProject(id: String): Result<Project?> {
        return try {
            localDataSource.getProject(id)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get project: ${e.message}"))
        }
    }
    
    /**
     * Save a project (create or update)
     */
    suspend fun saveProject(project: Project): Result<Unit> {
        _isLoading.value = true
        _error.value = null
        
        return try {
            val result = localDataSource.saveProject(project)
            result.fold(
                onSuccess = {
                    // Reload projects to update the list
                    loadProjects()
                    Result.success(Unit)
                },
                onFailure = { exception ->
                    _error.value = "Failed to save project: ${exception.message}"
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            _error.value = "Unexpected error saving project: ${e.message}"
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Delete a project by ID
     */
    suspend fun deleteProject(id: String): Result<Unit> {
        _isLoading.value = true
        _error.value = null
        
        return try {
            val result = localDataSource.deleteProject(id)
            result.fold(
                onSuccess = {
                    // Reload projects to update the list
                    loadProjects()
                    Result.success(Unit)
                },
                onFailure = { exception ->
                    _error.value = "Failed to delete project: ${exception.message}"
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            _error.value = "Unexpected error deleting project: ${e.message}"
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Clear all projects
     */
    suspend fun clearAllProjects(): Result<Unit> {
        _isLoading.value = true
        _error.value = null
        
        return try {
            val result = localDataSource.clearAllProjects()
            result.fold(
                onSuccess = {
                    _projects.value = emptyList()
                    Result.success(Unit)
                },
                onFailure = { exception ->
                    _error.value = "Failed to clear projects: ${exception.message}"
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            _error.value = "Unexpected error clearing projects: ${e.message}"
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Create a new project from surface data
     */
    suspend fun createProjectFromSurface(
        projectName: String,
        projectDescription: String,
        surface: Surface
    ): Result<Project> {
        val project = Project(
            id = generateProjectId(),
            name = projectName,
            description = projectDescription,
            surfaces = listOf(surface),
            createdTimestamp = getCurrentTimeMillis(),
            modifiedTimestamp = getCurrentTimeMillis()
        )
        
        return saveProject(project).map { project }
    }
    
    /**
     * Add a surface to an existing project
     */
    suspend fun addSurfaceToProject(projectId: String, surface: Surface): Result<Unit> {
        return try {
            val projectResult = getProject(projectId)
            if (projectResult.isFailure) {
                return Result.failure(Exception("Project not found"))
            }
            
            val project = projectResult.getOrThrow() ?: return Result.failure(Exception("Project not found"))
            
            val updatedProject = project.copy(
                surfaces = project.surfaces + surface,
                modifiedTimestamp = getCurrentTimeMillis()
            )
            
            saveProject(updatedProject)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to add surface: ${e.message}"))
        }
    }
    
    private fun generateProjectId(): String {
        return "project_${getCurrentTimeMillis()}"
    }
    
    private fun getCurrentTimeMillis(): Long {
        // Simple timestamp - in a real app you'd use proper date/time library
        return 1700000000000L
    }
}
