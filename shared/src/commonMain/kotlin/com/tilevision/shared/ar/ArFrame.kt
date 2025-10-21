package com.tilevision.shared.ar

/**
 * Represents a single AR frame with camera and tracking data
 */
expect class ArFrame {
    /**
     * Camera image data
     */
    val cameraImage: ArCameraImage?
    
    /**
     * Camera pose in world coordinates
     */
    val cameraPose: ArPose?
    
    /**
     * Frame timestamp in nanoseconds
     */
    val timestamp: Long
    
    /**
     * Get tracked planes in this frame
     */
    fun getTrackedPlanes(): List<ArPlane>
    
    /**
     * Get tracked points in this frame
     */
    fun getTrackedPoints(): List<ArPoint>
}

/**
 * AR Camera image data
 */
expect class ArCameraImage {
    /**
     * Image width in pixels
     */
    val width: Int
    
    /**
     * Image height in pixels
     */
    val height: Int
    
    /**
     * Image format
     */
    val format: ArImageFormat
}

/**
 * AR Image formats
 */
enum class ArImageFormat {
    YUV_420_888,
    RGB_888,
    RGBA_8888
}

/**
 * AR Plane representing detected surfaces
 */
data class ArPlane(
    val id: String,
    val center: ArPose,
    val extentX: Float,
    val extentZ: Float,
    val type: ArPlaneType,
    val isSubsumedBy: String? = null
)

/**
 * AR Plane types
 */
enum class ArPlaneType {
    HORIZONTAL_UPWARD_FACING,
    HORIZONTAL_DOWNWARD_FACING,
    VERTICAL_FACING
}

/**
 * AR Point representing tracked feature points
 */
data class ArPoint(
    val id: String,
    val position: ArPose,
    val orientationMode: ArPointOrientationMode
)

/**
 * AR Point orientation modes
 */
enum class ArPointOrientationMode {
    INITIALIZED_TO_IDENTITY,
    ESTIMATED_SURFACE_NORMAL
}
