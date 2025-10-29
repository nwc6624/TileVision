package com.tilevision.prefs

import android.content.Context
import android.content.SharedPreferences

object StartupPrefs {
    private const val PREFS_NAME = "tilevision_prefs"
    private const val KEY_SHOW_DISCLAIMER = "show_disclaimer_on_launch"

    private fun prefs(ctx: Context): SharedPreferences {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun shouldShowDisclaimer(ctx: Context): Boolean {
        // default true for first run
        return prefs(ctx).getBoolean(KEY_SHOW_DISCLAIMER, true)
    }

    fun setShouldShowDisclaimer(ctx: Context, show: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_SHOW_DISCLAIMER, show).apply()
    }
}
