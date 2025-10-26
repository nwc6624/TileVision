package de.westnordost.streetmeasure

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetmeasure.databinding.ActivityProjectSummaryDetailBinding

class ProjectSummaryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectSummaryDetailBinding
    private var summary: ProjectSummary? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectSummaryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize repositories
        ProjectSummaryRepository.init(this)

        setupToolbar()
        loadSummary()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadSummary() {
        val summaryId = intent.getStringExtra("summary_id")
        if (summaryId != null) {
            summary = ProjectSummaryRepository.getSummaryById(summaryId)
            if (summary != null) {
                displaySummary(summary!!)
            } else {
                Toast.makeText(this, "Job summary not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displaySummary(summary: ProjectSummary) {
        binding.summaryNameText.text = summary.displayName
        binding.projectAreaText.text = "${String.format("%.2f", summary.areaSqFt)} sq ft"
        binding.tileSizeText.text = "${String.format("%.1f", summary.tileWidthIn)} in x ${String.format("%.1f", summary.tileHeightIn)} in"
        binding.tileCoverageText.text = "${String.format("%.2f", summary.tileAreaSqFt)} ft² per tile"
        binding.wasteText.text = "Waste: ${String.format("%.0f", summary.wastePercent)}%"
        binding.tilesNeededText.text = "Required Tiles: ${summary.totalTilesNeededFinal}"
        binding.notesText.text = summary.notes ?: "No notes"
        binding.timestampText.text = MeasurementUtils.formatTimestamp(summary.timestamp)
    }

    private fun setupClickListeners() {
        binding.buttonShare.setOnClickListener {
            Toast.makeText(this, "PDF export coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.deleteButton.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun showDeleteDialog() {
        val s = summary ?: return
        
        AlertDialog.Builder(this)
            .setTitle("Delete Job Summary")
            .setMessage("Are you sure you want to delete this job summary?")
            .setPositiveButton("Delete") { _, _ ->
                val deleted = ProjectSummaryRepository.deleteSummary(s.id)
                if (deleted) {
                    Toast.makeText(this, "Job summary deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
