package com.tilevision.shared.permissions

import kotlinx.coroutines.flow.StateFlow

/**
 * Manages app permissions across platforms
 */
expect class PermissionCoordinator {
    /**
     * Permission state flow
     */
    val permissionState: StateFlow<PermissionState>
    
    /**
     * Request camera permission
     * @return Result indicating success or failure
     */
    suspend fun requestCameraPermission(): Result<Boolean>
    
    /**
     * Check if camera permission is granted
     */
    fun isCameraPermissionGranted(): Boolean
    
    /**
     * Request storage permission
     * @return Result indicating success or failure
     */
    suspend fun requestStoragePermission(): Result<Boolean>
    
    /**
     * Check if storage permission is granted
     */
    fun isStoragePermissionGranted(): Boolean
    
    /**
     * Open app settings for manual permission management
     */
    suspend fun openAppSettings(): Result<Unit>
    
    /**
     * Check if permission can be requested (not permanently denied)
     */
    fun canRequestPermission(permission: Permission): Boolean
}

/**
 * Permission types
 */
enum class Permission {
    CAMERA,
    STORAGE,
    LOCATION
}

/**
 * Permission states
 */
data class PermissionState(
    val camera: PermissionStatus,
    val storage: PermissionStatus,
    val location: PermissionStatus
)

/**
 * Individual permission status
 */
enum class PermissionStatus {
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED,
    NOT_REQUESTED
}
