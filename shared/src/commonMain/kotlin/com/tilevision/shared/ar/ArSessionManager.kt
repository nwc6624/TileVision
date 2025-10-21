package com.tilevision.shared.ar

import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

/**
 * Manages AR session lifecycle and provides AR functionality
 */
expect class ArSessionManager {
    /**
     * Current session state
     */
    val sessionState: StateFlow<ArSessionState>
    
    /**
     * Start AR session
     */
    suspend fun startSession(): Result<Unit>
    
    /**
     * Stop AR session
     */
    suspend fun stopSession(): Result<Unit>
    
    /**
     * Pause AR session
     */
    suspend fun pauseSession(): Result<Unit>
    
    /**
     * Resume AR session
     */
    suspend fun resumeSession(): Result<Unit>
    
    /**
     * Perform raycast from screen coordinates
     * @param x Screen X coordinate
     * @param y Screen Y coordinate
     * @return Raycast result with hit information
     */
    suspend fun raycast(x: Float, y: Float): Result<ArRaycastResult?>
    
    /**
     * Check if AR is supported on this device
     */
    fun isArSupported(): Boolean
}

/**
 * AR Session states
 */
enum class ArSessionState {
    UNINITIALIZED,
    INITIALIZING,
    RUNNING,
    PAUSED,
    STOPPED,
    ERROR
}

/**
 * AR Raycast result
 */
@Serializable
data class ArRaycastResult(
    val hitPose: ArPose,
    val distance: Float,
    val trackable: ArTrackable
)

/**
 * AR Pose representing position and orientation
 */
@Serializable
data class ArPose(
    val position: ArVector3,
    val rotation: ArQuaternion
)

/**
 * 3D Vector
 */
@Serializable
data class ArVector3(
    val x: Float,
    val y: Float,
    val z: Float
)

/**
 * Quaternion for rotation
 */
@Serializable
data class ArQuaternion(
    val x: Float,
    val y: Float,
    val z: Float,
    val w: Float
)

/**
 * AR Trackable types
 */
enum class ArTrackable {
    PLANE,
    POINT,
    DEPTH_POINT
}
