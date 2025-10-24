package de.westnordost.streetmeasure

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.westnordost.streetmeasure.databinding.ActivityMeasurementSavedBinding
import java.io.File

class MeasurementSavedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMeasurementSavedBinding
    private var projectId: String? = null
    private var project: ProjectMeasurement? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeasurementSavedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        // Get the project ID from intent
        projectId = intent.getStringExtra("project_id")
        projectId?.let {
            project = ProjectRepository.getProjectById(it)
            project?.let { p ->
                displayMeasurementDetails(p)
                setupClickListeners(p)
            } ?: run {
                Toast.makeText(this, "Project not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        } ?: run {
            Toast.makeText(this, "Invalid project ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Measurement Saved"
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun displayMeasurementDetails(project: ProjectMeasurement) {
        binding.measurementTitle.text = project.displayName
        binding.measurementArea.text = "${String.format("%.2f", project.areaFt2)} ft²"
        binding.measurementTimestamp.text = MeasurementUtils.formatTimestamp(project.timestamp)

        // Load preview image if available
        project.previewImageUri?.let { uri ->
            val imgFile = File(uri)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                binding.measurementImage.setImageBitmap(bitmap)
            } else {
                binding.measurementImage.setImageResource(R.drawable.ic_launcher_foreground)
            }
        } ?: binding.measurementImage.setImageResource(R.drawable.ic_launcher_foreground)
    }

    private fun setupClickListeners(project: ProjectMeasurement) {
        binding.buttonCalculateTiles.setOnClickListener {
            // Launch TileCalculatorActivity with the project area
            val intent = Intent(this, TileCalculatorActivity::class.java).apply {
                putExtra("areaSqFeet", project.areaFt2)
            }
            startActivity(intent)
            finish()
        }

        binding.buttonDone.setOnClickListener {
            // Go back to home screen
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        binding.buttonViewAllProjects.setOnClickListener {
            // Go to saved projects page
            val intent = Intent(this, SavedProjectsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
