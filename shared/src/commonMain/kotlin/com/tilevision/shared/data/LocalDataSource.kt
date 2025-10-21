package com.tilevision.shared.data

/**
 * Local data source interface for project storage
 */
interface LocalDataSource {
    suspend fun listProjects(): Result<List<Project>>
    suspend fun getProject(id: String): Result<Project?>
    suspend fun saveProject(project: Project): Result<Unit>
    suspend fun deleteProject(id: String): Result<Unit>
    suspend fun clearAllProjects(): Result<Unit>
}
