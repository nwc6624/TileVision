package com.tilevision.prefs

import android.content.Context
import android.content.SharedPreferences

object UnitsPrefs {
    private const val PREFS_NAME = "tilevision_prefs"
    private const val KEY_UNITS = "units_system" // "imperial" or "metric"

    private fun prefs(ctx: Context): SharedPreferences {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getUnits(ctx: Context): String {
        return prefs(ctx).getString(KEY_UNITS, "imperial") ?: "imperial"
    }

    fun setUnits(ctx: Context, units: String) {
        // accepted values: "imperial", "metric"
        prefs(ctx).edit().putString(KEY_UNITS, units).apply()
    }

    fun isImperial(ctx: Context): Boolean = getUnits(ctx) == "imperial"
    fun isMetric(ctx: Context): Boolean = getUnits(ctx) == "metric"
}


