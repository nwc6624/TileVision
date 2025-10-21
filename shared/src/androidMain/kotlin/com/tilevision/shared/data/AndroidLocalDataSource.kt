package com.tilevision.shared.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

class AndroidLocalDataSource(private val context: Context) : LocalDataSource {
    
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    private val projectsFile: File by lazy {
        File(context.filesDir, "projects.json")
    }
    
    override suspend fun listProjects(): Result<List<Project>> = withContext(Dispatchers.IO) {
        try {
            if (!projectsFile.exists()) {
                return@withContext Result.success(emptyList())
            }
            
            val jsonString = projectsFile.readText()
            if (jsonString.isBlank()) {
                return@withContext Result.success(emptyList())
            }
            
            val projects = json.decodeFromString<List<Project>>(jsonString)
            Result.success(projects)
        } catch (e: Exception) {
            Result.failure(IOException("Failed to read projects: ${e.message}", e))
        }
    }
    
    override suspend fun getProject(id: String): Result<Project?> = withContext(Dispatchers.IO) {
        try {
            val projectsResult = listProjects()
            if (projectsResult.isFailure) {
                return@withContext projectsResult.map { null }
            }
            
            val projects = projectsResult.getOrThrow()
            val project = projects.find { it.id == id }
            Result.success(project)
        } catch (e: Exception) {
            Result.failure(IOException("Failed to get project: ${e.message}", e))
        }
    }
    
    override suspend fun saveProject(project: Project): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val projectsResult = listProjects()
            val existingProjects = if (projectsResult.isSuccess) {
                projectsResult.getOrThrow().toMutableList()
            } else {
                mutableListOf()
            }
            
            // Remove existing project with same ID if it exists
            existingProjects.removeAll { it.id == project.id }
            
            // Add the new/updated project
            existingProjects.add(project)
            
            // Sort by modified timestamp (newest first)
            existingProjects.sortByDescending { it.modifiedTimestamp }
            
            val jsonString = json.encodeToString(existingProjects)
            projectsFile.writeText(jsonString)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IOException("Failed to save project: ${e.message}", e))
        }
    }
    
    override suspend fun deleteProject(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val projectsResult = listProjects()
            if (projectsResult.isFailure) {
                return@withContext Result.failure(IOException("Failed to read projects for deletion"))
            }
            
            val projects = projectsResult.getOrThrow()
            val updatedProjects = projects.filter { it.id != id }
            
            val jsonString = json.encodeToString(updatedProjects)
            projectsFile.writeText(jsonString)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IOException("Failed to delete project: ${e.message}", e))
        }
    }
    
    override suspend fun clearAllProjects(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (projectsFile.exists()) {
                projectsFile.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IOException("Failed to clear projects: ${e.message}", e))
        }
    }
}
