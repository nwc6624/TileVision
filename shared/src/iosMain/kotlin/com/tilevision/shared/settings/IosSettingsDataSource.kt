package com.tilevision.shared.settings

class IosSettingsDataSource : SettingsDataSource {
    
    override suspend fun getUserPrefs(): Result<UserPrefs> {
        // TODO: Implement iOS file storage using FileManager
        return Result.success(DefaultUserPrefs.IMPERIAL)
    }
    
    override suspend fun saveUserPrefs(prefs: UserPrefs): Result<Unit> {
        // TODO: Implement iOS file storage using FileManager
        return Result.success(Unit)
    }
    
    override suspend fun backupUserPrefs(prefs: UserPrefs): Result<String> {
        // TODO: Implement iOS backup to Documents directory
        return Result.success("/Documents/user_prefs_backup.json")
    }
    
    override suspend fun restoreUserPrefs(): Result<UserPrefs> {
        // TODO: Implement iOS restore from Documents directory
        return Result.success(DefaultUserPrefs.IMPERIAL)
    }
    
    override suspend fun clearUserPrefs(): Result<Unit> {
        // TODO: Implement iOS file deletion
        return Result.success(Unit)
    }
}
