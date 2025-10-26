package de.westnordost.streetmeasure

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

object TileSampleRepository {
    private var tileSamples = mutableListOf<TileSample>()
    private var prefs: SharedPreferences? = null
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences("tilevision", Context.MODE_PRIVATE)
        loadTileSamples()
    }

    private fun loadTileSamples() {
        val json = prefs?.getString("tile_samples_json", null)
        if (json != null) {
            val type = object : TypeToken<List<TileSample>>() {}.type
            tileSamples = gson.fromJson(json, type) ?: mutableListOf()
        }
    }
    
    private fun saveTileSamples() {
        val json = gson.toJson(tileSamples)
        prefs?.edit()?.putString("tile_samples_json", json)?.apply()
    }
    
    fun addTileSample(tileSample: TileSample): TileSample {
        tileSamples.add(tileSample)
        saveTileSamples()
        return tileSample
    }
    
    fun getAllTileSamples(): List<TileSample> {
        return tileSamples.toList()
    }
    
    fun getTileSampleById(id: String): TileSample? {
        return tileSamples.find { it.id == id }
    }
    
    fun deleteTileSample(id: String): Boolean {
        val removed = tileSamples.removeAll { it.id == id }
        if (removed) {
            saveTileSamples()
        }
        return removed
    }
    
    fun updateTileName(id: String, newName: String): Boolean {
        val index = tileSamples.indexOfFirst { it.id == id }
        if (index != -1) {
            val currentTile = tileSamples[index]
            val updatedTile = currentTile.copy(displayName = newName)
            tileSamples[index] = updatedTile
            saveTileSamples()
            return true
        }
        return false
    }
    
    fun clear() {
        tileSamples.clear()
        saveTileSamples()
    }
}
