package de.westnordost.streetmeasure.permissions

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object CameraPermissionHelper {
    const val REQ_CAMERA = 42

    fun hasCameraPermission(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(
            activity, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestCameraPermission(activity: Activity) {
        if (!hasCameraPermission(activity)) {
            // show rationale if needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
                AlertDialog.Builder(activity)
                    .setTitle("Camera access needed")
                    .setMessage("We use the camera to measure surfaces and tile samples in AR.")
                    .setPositiveButton("Continue") { _, _ ->
                        ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(Manifest.permission.CAMERA),
                            REQ_CAMERA
                        )
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.CAMERA),
                    REQ_CAMERA
                )
            }
        }
    }

    fun handlePermissionResult(
        activity: Activity,
        requestCode: Int,
        grantResults: IntArray,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        if (requestCode == REQ_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onGranted()
            } else {
                onDenied()
            }
        }
    }
}
