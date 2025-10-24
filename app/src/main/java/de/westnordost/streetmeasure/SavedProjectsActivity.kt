package de.westnordost.streetmeasure

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetmeasure.databinding.ActivitySavedProjectsBinding

class SavedProjectsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavedProjectsBinding
    private lateinit var adapter: ProjectsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedProjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize repositories
        ProjectRepository.init(this)
        TileSampleRepository.init(this)

        android.util.Log.d("SavedProjectsActivity", "onCreate called")
        setupToolbar()
        setupRecyclerView()
        setupCalculateTilesButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Saved Projects"
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        android.util.Log.d("SavedProjectsActivity", "setupRecyclerView called")
        adapter = ProjectsAdapter { project ->
            openProjectDetail(project)
        }
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = adapter

        // Load projects
        loadProjects()
    }

    private fun setupCalculateTilesButton() {
        binding.buttonCalculateTiles.setOnClickListener {
            val intent = Intent(this, TileCalculatorActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadProjects() {
        val projects = ProjectRepository.getAllProjects()
        android.util.Log.d("SavedProjectsActivity", "Loaded ${projects.size} projects")
        projects.forEach { project ->
            android.util.Log.d("SavedProjectsActivity", "Project: ${project.displayName} - ${project.areaFt2} ft²")
        }
        
        if (projects.isEmpty()) {
            android.util.Log.d("SavedProjectsActivity", "No projects found - showing empty state")
        } else {
            android.util.Log.d("SavedProjectsActivity", "Submitting ${projects.size} projects to adapter")
        }
        
        adapter.submitList(projects)
    }

    private fun openProjectDetail(project: ProjectMeasurement) {
        // Check if this activity was launched from TileCalculatorActivity
        val isFromCalculator = intent.getBooleanExtra("from_calculator", false)
        
        if (isFromCalculator) {
            // Return the project area directly to the calculator
            val resultIntent = Intent().apply {
                putExtra("project_area", project.areaFt2)
                putExtra("project_id", project.id)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        } else {
            // Open the detail view
            val intent = Intent(this, ProjectDetailActivity::class.java).apply {
                putExtra("project_id", project.id)
            }
            startActivityForResult(intent, REQUEST_CODE_PROJECT_DETAIL)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_CODE_PROJECT_DETAIL) {
            // Refresh the list in case a project was deleted
            loadProjects()
        }
    }

    private inner class ProjectsAdapter(
        private val onProjectClick: (ProjectMeasurement) -> Unit
    ) : RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder>() {
        private var projects = listOf<ProjectMeasurement>()

        fun submitList(newProjects: List<ProjectMeasurement>) {
            android.util.Log.d("SavedProjectsActivity", "Adapter submitList called with ${newProjects.size} projects")
            projects = newProjects
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_project_card, parent, false)
            return ProjectViewHolder(view)
        }

        override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
            android.util.Log.d("SavedProjectsActivity", "Binding project at position $position: ${projects[position].displayName}")
            holder.bind(projects[position])
        }

        override fun getItemCount(): Int = projects.size

        inner class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val thumbnailImage: ImageView = itemView.findViewById(R.id.thumbnailImage)
            private val titleText: TextView = itemView.findViewById(R.id.titleText)
            private val subtitleText: TextView = itemView.findViewById(R.id.subtitleText)
            private val timestampText: TextView = itemView.findViewById(R.id.timestampText)

            fun bind(project: ProjectMeasurement) {
                android.util.Log.d("SavedProjectsActivity", "Binding project: ${project.displayName} with area: ${project.areaFt2} ft²")
                
                titleText.text = project.displayName
                        subtitleText.text = "${String.format("%.2f", project.areaFt2)} sq ft"
                timestampText.text = MeasurementUtils.formatTimestamp(project.timestamp)
                
                android.util.Log.d("SavedProjectsActivity", "Set title: ${titleText.text}, subtitle: ${subtitleText.text}")
                
                // TODO: Load actual preview image when screenshot capture is implemented
                thumbnailImage.setImageResource(R.drawable.ic_launcher_foreground)

                itemView.setOnClickListener {
                    onProjectClick(project)
                }
                
                itemView.setOnLongClickListener {
                    showDeleteDialog(project)
                    true
                }
            }
            
            private fun showDeleteDialog(project: ProjectMeasurement) {
                AlertDialog.Builder(this@SavedProjectsActivity)
                    .setTitle("Delete Project")
                    .setMessage("Are you sure you want to delete this project?")
                    .setPositiveButton("Delete") { _, _ ->
                        val deleted = ProjectRepository.deleteProject(project.id)
                        if (deleted) {
                            Toast.makeText(this@SavedProjectsActivity, "Project deleted", Toast.LENGTH_SHORT).show()
                            loadProjects()
                        }
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PROJECT_DETAIL = 1001
    }
}
