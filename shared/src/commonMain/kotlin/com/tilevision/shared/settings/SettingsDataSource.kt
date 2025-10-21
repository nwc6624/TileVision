package com.tilevision.shared.settings

/**
 * Data source interface for user preferences storage
 */
interface SettingsDataSource {
    suspend fun getUserPrefs(): Result<UserPrefs>
    suspend fun saveUserPrefs(prefs: UserPrefs): Result<Unit>
    suspend fun backupUserPrefs(prefs: UserPrefs): Result<String>
    suspend fun restoreUserPrefs(): Result<UserPrefs>
    suspend fun clearUserPrefs(): Result<Unit>
}
