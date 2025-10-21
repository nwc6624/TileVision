package com.tilevision.shared.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class PermissionCoordinator(private val context: Context) {
    private val _permissionState = MutableStateFlow(
        PermissionState(
            camera = getCameraPermissionStatus(),
            storage = getStoragePermissionStatus(),
            location = PermissionStatus.NOT_REQUESTED
        )
    )
    actual val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()
    
    actual suspend fun requestCameraPermission(): Result<Boolean> = suspendCancellableCoroutine { continuation ->
        // Note: In a real implementation, this would need to be called from an Activity
        // and use ActivityResultContracts.RequestPermission()
        val granted = isCameraPermissionGranted()
        updatePermissionState()
        continuation.resume(Result.success(granted))
    }
    
    actual fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    actual suspend fun requestStoragePermission(): Result<Boolean> = suspendCancellableCoroutine { continuation ->
        // Note: In a real implementation, this would need to be called from an Activity
        // and use ActivityResultContracts.RequestMultiplePermissions()
        val granted = isStoragePermissionGranted()
        updatePermissionState()
        continuation.resume(Result.success(granted))
    }
    
    actual fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+, use specific media permissions
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // For older versions, use external storage permissions
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    actual suspend fun openAppSettings(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            continuation.resume(Result.success(Unit))
        } catch (e: Exception) {
            continuation.resume(Result.failure(e))
        }
    }
    
    actual fun canRequestPermission(permission: Permission): Boolean {
        return when (permission) {
            Permission.CAMERA -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    !context.packageManager.isPermissionRevokedByPolicy(
                        Manifest.permission.CAMERA,
                        context.packageName
                    )
                } else {
                    true
                }
            }
            Permission.STORAGE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        !context.packageManager.isPermissionRevokedByPolicy(
                            Manifest.permission.READ_MEDIA_IMAGES,
                            context.packageName
                        )
                    } else {
                        !context.packageManager.isPermissionRevokedByPolicy(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            context.packageName
                        )
                    }
                } else {
                    true
                }
            }
            Permission.LOCATION -> true // TODO: Implement location permission check
        }
    }
    
    private fun getCameraPermissionStatus(): PermissionStatus {
        return when {
            isCameraPermissionGranted() -> PermissionStatus.GRANTED
            canRequestPermission(Permission.CAMERA) -> PermissionStatus.DENIED
            else -> PermissionStatus.PERMANENTLY_DENIED
        }
    }
    
    private fun getStoragePermissionStatus(): PermissionStatus {
        return when {
            isStoragePermissionGranted() -> PermissionStatus.GRANTED
            canRequestPermission(Permission.STORAGE) -> PermissionStatus.DENIED
            else -> PermissionStatus.PERMANENTLY_DENIED
        }
    }
    
    private fun updatePermissionState() {
        _permissionState.value = PermissionState(
            camera = getCameraPermissionStatus(),
            storage = getStoragePermissionStatus(),
            location = PermissionStatus.NOT_REQUESTED
        )
    }
}
