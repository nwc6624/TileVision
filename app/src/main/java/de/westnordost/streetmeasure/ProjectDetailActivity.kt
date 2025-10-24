package de.westnordost.streetmeasure

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetmeasure.databinding.ActivityProjectDetailBinding

class ProjectDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectDetailBinding
    private var project: ProjectMeasurement? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadProject()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Project Details"
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadProject() {
        val projectId = intent.getStringExtra("project_id")
        if (projectId != null) {
            project = ProjectRepository.getProjectById(projectId)
            project?.let { displayProject(it) }
        } else {
            Toast.makeText(this, "Project not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun displayProject(project: ProjectMeasurement) {
        binding.projectNameText.text = project.displayName
        binding.areaText.text = "${String.format("%.2f", project.areaFt2)} ft²"
        binding.timestampText.text = MeasurementUtils.formatTimestamp(project.timestamp)
        
        // TODO: Load preview image when screenshot capture is implemented
        binding.previewImage.setImageResource(R.drawable.ic_launcher_foreground)
    }

    private fun setupClickListeners() {
        binding.useProjectButton.setOnClickListener {
            useProject()
        }
        
        binding.deleteButton.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun useProject() {
        project?.let { project ->
            // Launch TileCalculatorActivity with the project area
            val intent = Intent(this, TileCalculatorActivity::class.java).apply {
                putExtra("areaSqFeet", project.areaFt2)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Project")
            .setMessage("Are you sure you want to delete this project? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteProject()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteProject() {
        project?.let { project ->
            val deleted = ProjectRepository.deleteProject(project.id)
            if (deleted) {
                Toast.makeText(this, "Project deleted", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to delete project", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
