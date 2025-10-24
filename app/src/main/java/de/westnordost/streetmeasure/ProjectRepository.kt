package de.westnordost.streetmeasure

import android.content.Context
import android.content.SharedPreferences
import com.google.ar.sceneform.math.Vector3
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

object ProjectRepository {
    private var projects = mutableListOf<ProjectMeasurement>()
    private var prefs: SharedPreferences? = null
    private val gson = Gson()
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences("project_measurements", Context.MODE_PRIVATE)
        loadProjects()
    }
    
    private fun loadProjects() {
        val json = prefs?.getString("projects", null)
        if (json != null) {
            val type = object : TypeToken<List<ProjectMeasurement>>() {}.type
            projects = gson.fromJson(json, type) ?: mutableListOf()
        }
    }
    
    private fun saveProjects() {
        val json = gson.toJson(projects)
        prefs?.edit()?.putString("projects", json)?.apply()
    }
    
    fun addProjectMeasurement(project: ProjectMeasurement): ProjectMeasurement {
        projects.add(project)
        saveProjects()
        android.util.Log.d("ProjectRepository", "Added project: ${project.displayName} (${projects.size} total)")
        return project
    }
    
    fun getAllProjects(): List<ProjectMeasurement> {
        android.util.Log.d("ProjectRepository", "Getting all projects: ${projects.size} found")
        return projects.toList()
    }
    
    fun getProjectById(id: String): ProjectMeasurement? {
        return projects.find { it.id == id }
    }
    
    fun deleteProject(id: String): Boolean {
        val removed = projects.removeAll { it.id == id }
        if (removed) {
            saveProjects()
        }
        return removed
    }
    
    fun clear() {
        projects.clear()
        saveProjects()
    }
}
