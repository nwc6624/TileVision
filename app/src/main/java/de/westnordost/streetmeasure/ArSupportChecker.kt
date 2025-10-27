package de.westnordost.streetmeasure

import android.app.Activity
import android.content.pm.PackageManager
import com.google.ar.core.ArCoreApk
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException

object ArSupportChecker {

    /**
     * Returns true if this device SHOULD be able to run AR.
     * We do not block just because tracking isn't ready yet.
     */
    fun isArSupported(activity: Activity): Boolean {
        // 1. Camera permission is required. If we don't have it yet, don't say "not supported",
        //    we just say "need permission".
        val hasCamera = activity.packageManager
            .hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        if (!hasCamera) return false

        // 2. Check ARCore availability via ArCoreApk
        //    On Pixel 6a this should come back SUPPORTED_INSTALLED or SUPPORTED_APK_TOO_OLD etc.
        return try {
            val availability = ArCoreApk.getInstance().checkAvailability(activity)
            availability.isSupported
        } catch (e: UnavailableDeviceNotCompatibleException) {
            false
        } catch (e: Exception) {
            // Fallback: be optimistic unless we KNOW it's unsupported
            true
        }
    }
}
