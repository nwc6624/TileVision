package com.tilevision.shared.ar

import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.Point

actual class ArFrame(private val frame: Frame) {
    actual val cameraImage: ArCameraImage? = frame.acquireCameraImage()?.let { image ->
        ArCameraImage(image)
    }
    
    actual val cameraPose: ArPose? = frame.camera?.pose?.let { pose ->
        ArPose(
            position = ArVector3(pose.tx(), pose.ty(), pose.tz()),
            rotation = ArQuaternion(pose.qx(), pose.qy(), pose.qz(), pose.qw())
        )
    }
    
    actual val timestamp: Long = frame.timestamp
    
    actual fun getTrackedPlanes(): List<ArPlane> {
        return frame.getUpdatedTrackables(Plane::class.java)
            .filter { it.trackingState == com.google.ar.core.TrackingState.TRACKING }
            .map { plane ->
                ArPlane(
                    id = plane.toString(),
                    center = ArPose(
                        position = ArVector3(plane.centerPose.tx(), plane.centerPose.ty(), plane.centerPose.tz()),
                        rotation = ArQuaternion(plane.centerPose.qx(), plane.centerPose.qy(), plane.centerPose.qz(), plane.centerPose.qw())
                    ),
                    extentX = plane.extentX,
                    extentZ = plane.extentZ,
                    type = when (plane.type) {
                        com.google.ar.core.Plane.Type.HORIZONTAL_UPWARD_FACING -> ArPlaneType.HORIZONTAL_UPWARD_FACING
                        com.google.ar.core.Plane.Type.HORIZONTAL_DOWNWARD_FACING -> ArPlaneType.HORIZONTAL_DOWNWARD_FACING
                        com.google.ar.core.Plane.Type.VERTICAL -> ArPlaneType.VERTICAL_FACING
                        else -> ArPlaneType.HORIZONTAL_UPWARD_FACING
                    },
                    isSubsumedBy = plane.subsumedBy?.toString()
                )
            }
    }
    
    actual fun getTrackedPoints(): List<ArPoint> {
        return frame.getUpdatedTrackables(Point::class.java)
            .filter { it.trackingState == com.google.ar.core.TrackingState.TRACKING }
            .map { point ->
                ArPoint(
                    id = point.toString(),
                    position = ArPose(
                        position = ArVector3(point.pose.tx(), point.pose.ty(), point.pose.tz()),
                        rotation = ArQuaternion(point.pose.qx(), point.pose.qy(), point.pose.qz(), point.pose.qw())
                    ),
                    orientationMode = when (point.orientationMode) {
                        com.google.ar.core.Point.OrientationMode.INITIALIZED_TO_IDENTITY -> ArPointOrientationMode.INITIALIZED_TO_IDENTITY
                        com.google.ar.core.Point.OrientationMode.ESTIMATED_SURFACE_NORMAL -> ArPointOrientationMode.ESTIMATED_SURFACE_NORMAL
                        else -> ArPointOrientationMode.INITIALIZED_TO_IDENTITY
                    }
                )
            }
    }
}

actual class ArCameraImage(private val image: com.google.ar.core.CameraImage) {
    actual val width: Int = image.width
    actual val height: Int = image.height
    actual val format: ArImageFormat = when (image.format) {
        android.graphics.ImageFormat.YUV_420_888 -> ArImageFormat.YUV_420_888
        android.graphics.ImageFormat.RGB_888 -> ArImageFormat.RGB_888
        android.graphics.ImageFormat.RGBA_8888 -> ArImageFormat.RGBA_8888
        else -> ArImageFormat.YUV_420_888
    }
}
