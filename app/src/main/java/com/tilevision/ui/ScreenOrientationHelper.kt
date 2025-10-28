package com.tilevision.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import com.tilevision.prefs.OrientationPrefs

object ScreenOrientationHelper {
    fun applyOrientationPolicy(activity: Activity) {
        if (OrientationPrefs.isPortraitLocked(activity)) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
}
