package com.tilevision.shared.data

class IosLocalDataSource : LocalDataSource {
    
    override suspend fun listProjects(): Result<List<Project>> {
        // TODO: Implement iOS file storage using FileManager
        return Result.success(emptyList())
    }
    
    override suspend fun getProject(id: String): Result<Project?> {
        // TODO: Implement iOS project retrieval
        return Result.success(null)
    }
    
    override suspend fun saveProject(project: Project): Result<Unit> {
        // TODO: Implement iOS project saving
        return Result.success(Unit)
    }
    
    override suspend fun deleteProject(id: String): Result<Unit> {
        // TODO: Implement iOS project deletion
        return Result.success(Unit)
    }
    
    override suspend fun clearAllProjects(): Result<Unit> {
        // TODO: Implement iOS project clearing
        return Result.success(Unit)
    }
}
