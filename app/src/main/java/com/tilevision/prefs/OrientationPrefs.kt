package com.tilevision.prefs

import android.content.Context
import android.content.SharedPreferences

object OrientationPrefs {
    private const val PREFS_NAME = "tilevision_prefs"
    private const val KEY_LOCK_PORTRAIT = "lock_portrait"

    private fun prefs(ctx: Context): SharedPreferences {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isPortraitLocked(ctx: Context): Boolean {
        return prefs(ctx).getBoolean(KEY_LOCK_PORTRAIT, true) // default true if you want
    }

    fun setPortraitLocked(ctx: Context, locked: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_LOCK_PORTRAIT, locked).apply()
    }
}


