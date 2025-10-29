package com.tilevision.prefs

import android.content.Context
import android.content.SharedPreferences

object AppearancePrefs {
    private const val PREFS_NAME = "tilevision_prefs"
    private const val KEY_MONOCHROME = "appearance_monochrome"

    private fun prefs(ctx: Context): SharedPreferences {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isMonochrome(ctx: Context): Boolean {
        return prefs(ctx).getBoolean(KEY_MONOCHROME, false)
    }

    fun setMonochrome(ctx: Context, enabled: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_MONOCHROME, enabled).apply()
    }
}
