package com.tilevision.shared.settings

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

class AndroidSettingsDataSource(private val context: Context) : SettingsDataSource {
    
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    private val settingsFile: File by lazy {
        File(context.filesDir, "user_prefs.json")
    }
    
    private val backupFile: File by lazy {
        File(context.filesDir, "user_prefs_backup.json")
    }
    
    override suspend fun getUserPrefs(): Result<UserPrefs> = withContext(Dispatchers.IO) {
        try {
            if (!settingsFile.exists()) {
                return@withContext Result.success(DefaultUserPrefs.IMPERIAL)
            }
            
            val jsonString = settingsFile.readText()
            if (jsonString.isBlank()) {
                return@withContext Result.success(DefaultUserPrefs.IMPERIAL)
            }
            
            val prefs = json.decodeFromString<UserPrefs>(jsonString)
            Result.success(prefs)
        } catch (e: Exception) {
            Result.failure(IOException("Failed to read user preferences: ${e.message}", e))
        }
    }
    
    override suspend fun saveUserPrefs(prefs: UserPrefs): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val jsonString = json.encodeToString(prefs)
            settingsFile.writeText(jsonString)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IOException("Failed to save user preferences: ${e.message}", e))
        }
    }
    
    override suspend fun backupUserPrefs(prefs: UserPrefs): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonString = json.encodeToString(prefs)
            backupFile.writeText(jsonString)
            Result.success(backupFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(IOException("Failed to backup user preferences: ${e.message}", e))
        }
    }
    
    override suspend fun restoreUserPrefs(): Result<UserPrefs> = withContext(Dispatchers.IO) {
        try {
            if (!backupFile.exists()) {
                return@withContext Result.failure(IOException("No backup file found"))
            }
            
            val jsonString = backupFile.readText()
            if (jsonString.isBlank()) {
                return@withContext Result.failure(IOException("Backup file is empty"))
            }
            
            val prefs = json.decodeFromString<UserPrefs>(jsonString)
            
            // Save the restored preferences to the main settings file
            saveUserPrefs(prefs)
            
            Result.success(prefs)
        } catch (e: Exception) {
            Result.failure(IOException("Failed to restore user preferences: ${e.message}", e))
        }
    }
    
    override suspend fun clearUserPrefs(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (settingsFile.exists()) {
                settingsFile.delete()
            }
            if (backupFile.exists()) {
                backupFile.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IOException("Failed to clear user preferences: ${e.message}", e))
        }
    }
}
