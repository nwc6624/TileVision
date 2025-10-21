package com.tilevision.shared.ar

actual class ArFrame {
    actual val cameraImage: ArCameraImage? = null // TODO: Implement ARKit camera image
    
    actual val cameraPose: ArPose? = null // TODO: Implement ARKit camera pose
    
    actual val timestamp: Long = 0L // TODO: Implement ARKit frame timestamp
    
    actual fun getTrackedPlanes(): List<ArPlane> {
        // TODO: Implement ARKit plane tracking
        return emptyList()
    }
    
    actual fun getTrackedPoints(): List<ArPoint> {
        // TODO: Implement ARKit point tracking
        return emptyList()
    }
}

actual class ArCameraImage {
    actual val width: Int = 0 // TODO: Implement ARKit image width
    actual val height: Int = 0 // TODO: Implement ARKit image height
    actual val format: ArImageFormat = ArImageFormat.RGBA_8888 // TODO: Implement ARKit image format
}
