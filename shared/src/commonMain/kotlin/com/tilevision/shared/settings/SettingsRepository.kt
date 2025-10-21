package com.tilevision.shared.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing user preferences and settings
 */
class SettingsRepository(private val settingsDataSource: SettingsDataSource) {
    
    private val _userPrefs = MutableStateFlow<UserPrefs?>(null)
    val userPrefs: StateFlow<UserPrefs?> = _userPrefs.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Load user preferences from storage
     */
    suspend fun loadUserPrefs() {
        _isLoading.value = true
        _error.value = null
        
        try {
            val result = settingsDataSource.getUserPrefs()
            result.fold(
                onSuccess = { prefs ->
                    _userPrefs.value = prefs
                },
                onFailure = { exception ->
                    _error.value = "Failed to load settings: ${exception.message}"
                    // Load default preferences if loading fails
                    _userPrefs.value = DefaultUserPrefs.IMPERIAL
                }
            )
        } catch (e: Exception) {
            _error.value = "Unexpected error loading settings: ${e.message}"
            _userPrefs.value = DefaultUserPrefs.IMPERIAL
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Save user preferences to storage
     */
    suspend fun saveUserPrefs(prefs: UserPrefs): Result<Unit> {
        _isLoading.value = true
        _error.value = null
        
        return try {
            val result = settingsDataSource.saveUserPrefs(prefs)
            result.fold(
                onSuccess = {
                    _userPrefs.value = prefs
                    Result.success(Unit)
                },
                onFailure = { exception ->
                    _error.value = "Failed to save settings: ${exception.message}"
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            _error.value = "Unexpected error saving settings: ${e.message}"
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Update a specific preference value
     */
    suspend fun updateUserPrefs(update: (UserPrefs) -> UserPrefs): Result<Unit> {
        val currentPrefs = _userPrefs.value ?: DefaultUserPrefs.IMPERIAL
        val updatedPrefs = update(currentPrefs)
        return saveUserPrefs(updatedPrefs)
    }
    
    /**
     * Get current user preferences or default if not loaded
     */
    fun getCurrentUserPrefs(): UserPrefs {
        return _userPrefs.value ?: DefaultUserPrefs.IMPERIAL
    }
    
    /**
     * Reset to default preferences
     */
    suspend fun resetToDefaults(): Result<Unit> {
        return saveUserPrefs(DefaultUserPrefs.IMPERIAL)
    }
    
    /**
     * Backup user preferences
     */
    suspend fun backupUserPrefs(): Result<String> {
        return try {
            val prefs = getCurrentUserPrefs()
            settingsDataSource.backupUserPrefs(prefs)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to backup settings: ${e.message}"))
        }
    }
    
    /**
     * Restore user preferences from backup
     */
    suspend fun restoreUserPrefs(): Result<Unit> {
        return try {
            val result = settingsDataSource.restoreUserPrefs()
            result.fold(
                onSuccess = { prefs ->
                    saveUserPrefs(prefs)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Failed to restore settings: ${e.message}"))
        }
    }
    
    /**
     * Clear all user preferences
     */
    suspend fun clearUserPrefs(): Result<Unit> {
        return try {
            val result = settingsDataSource.clearUserPrefs()
            result.fold(
                onSuccess = {
                    _userPrefs.value = DefaultUserPrefs.IMPERIAL
                    Result.success(Unit)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Failed to clear settings: ${e.message}"))
        }
    }
}
