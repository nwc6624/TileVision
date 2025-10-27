package de.westnordost.streetmeasure

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ProjectSummaryRepository {
    private var summaries = mutableListOf<ProjectSummary>()
    private var prefs: SharedPreferences? = null
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences("tilevision", Context.MODE_PRIVATE)
        loadSummaries()
    }

    private fun loadSummaries() {
        val json = prefs?.getString("project_summaries_json", null)
        if (json != null) {
            val type = object : TypeToken<List<ProjectSummary>>() {}.type
            summaries = gson.fromJson(json, type) ?: mutableListOf()
        }
        android.util.Log.d("TileVision", "Loaded ${summaries.size} summaries from storage")
    }

    private fun saveSummaries() {
        val json = gson.toJson(summaries)
        prefs?.edit()?.putString("project_summaries_json", json)?.apply()
    }

    fun addSummary(summary: ProjectSummary): ProjectSummary {
        summaries.add(summary)
        saveSummaries()
        android.util.Log.d("TileVision", "Saved summary: ${summary.displayName}")
        android.util.Log.d("TileVision", "  ID: ${summary.id}")
        android.util.Log.d("TileVision", "  Area: ${summary.areaSqFt} ft²")
        android.util.Log.d("TileVision", "  Tiles: ${summary.totalTilesNeededFinal}")
        android.util.Log.d("TileVision", "Total summaries now: ${summaries.size}")
        return summary
    }

    fun getAllSummaries(): List<ProjectSummary> {
        return summaries.toList()
    }

    fun getSummaryById(id: String): ProjectSummary? {
        return summaries.find { it.id == id }
    }

    fun deleteSummary(id: String): Boolean {
        val removed = summaries.removeAll { it.id == id }
        if (removed) {
            saveSummaries()
        }
        return removed
    }

    fun clear() {
        summaries.clear()
        saveSummaries()
    }
}
