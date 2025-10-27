package de.westnordost.streetmeasure

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import de.westnordost.streetmeasure.databinding.ActivityProjectSummaryDetailBinding
import java.text.SimpleDateFormat
import java.util.*

class ProjectSummaryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectSummaryDetailBinding
    private var summary: ProjectSummary? = null
    private lateinit var gridBackground: GridBackgroundView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        android.util.Log.d("TileVisionLifecycle", "onCreate ProjectSummaryDetailActivity starting")
        
        // Inflate the shared page shell
        setContentView(R.layout.layout_page_shell)
        
        // Get references to shell elements
        val pageContentContainer = findViewById<android.widget.FrameLayout>(R.id.pageContentContainer)
        gridBackground = findViewById(R.id.gridBackground)
        
        // Inflate the activity's own content layout into the shell's container
        try {
            layoutInflater.inflate(R.layout.activity_project_summary_detail, pageContentContainer, true)
            android.util.Log.d("TileVisionLifecycle", "ProjectSummaryDetailActivity shell + content inflated ok")
        } catch (e: Exception) {
            android.util.Log.e("TileVisionLifecycle", "inflate failed in ProjectSummaryDetailActivity", e)
        }
        
        // Now set up binding on the inflated content (bind to the child, not the container)
        val inflatedContent = pageContentContainer.getChildAt(0) as android.widget.LinearLayout
        binding = ActivityProjectSummaryDetailBinding.bind(inflatedContent)

        // Initialize repositories
        ProjectRepository.init(this)
        TileSampleRepository.init(this)
        ProjectSummaryRepository.init(this)

        setupHeader()
        loadSummary()
        setupClickListeners()
    }

    private fun setupHeader() {
        binding.appHeader.setModeBack { finish() }
    }

    private fun loadSummary() {
        val summaryId = intent.getStringExtra("summary_id")
        if (summaryId != null) {
            summary = ProjectSummaryRepository.getSummaryById(summaryId)
            if (summary != null) {
                displaySummary(summary!!)
            } else {
                Toast.makeText(this, "Job not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displaySummary(s: ProjectSummary) {
        // Header
        binding.appHeader.setTitle(s.displayName)
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
        val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
        val date = Date(s.timestamp)
        binding.appHeader.setSubtitle("${dateFormat.format(date)} • ${timeFormat.format(date)}")

        // Project section
        binding.areaValueText.text = "${String.format("%.1f", s.areaSqFt)} ft²"
        binding.layoutValueText.text = s.layoutStyle
        binding.groutValueText.text = formatGroutGap(s.groutGapInches)

        // Tile section
        binding.tileSizeValueText.text = "${String.format("%.1f", s.tileWidthIn)} in × ${String.format("%.1f", s.tileHeightIn)} in"
        binding.tileCoverageValueText.text = "${String.format("%.2f", s.tileAreaSqFt)} ft² per tile"

        // Calculation section
        binding.rawTilesValueText.text = "${String.format("%.1f", s.totalTilesNeededRaw)} tiles (before waste)"
        binding.wasteValueText.text = "${String.format("%.0f", s.wastePercent)}%"
        binding.totalTilesValueText.text = "${s.totalTilesNeededFinal} tiles (rounded)"

        // Boxes needed (if present)
        if (s.boxesNeeded != null) {
            binding.boxesNeededContainer.visibility = View.VISIBLE
            binding.boxesNeededValueText.text = String.format("%.1f boxes", s.boxesNeeded)
        } else {
            binding.boxesNeededContainer.visibility = View.GONE
        }

        // Notes
        binding.notesValueText.text = s.notes ?: "—"
    }

    private fun formatGroutGap(inches: Float): String {
        return when (inches) {
            0.0625f -> "1/16 in"
            0.125f -> "1/8 in"
            0.1875f -> "3/16 in"
            0.25f -> "1/4 in"
            else -> String.format("%.3f", inches)
        }
    }

    private fun setupClickListeners() {
        binding.buttonShareText.setOnClickListener {
            summary?.let { s ->
                binding.buttonShareText.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
                shareText(s)
            }
        }

        binding.buttonExportPdf.setOnClickListener {
            summary?.let { s ->
                binding.buttonExportPdf.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
                sharePdf(s)
            }
        }

        binding.buttonDelete.setOnClickListener {
            showDeleteDialog()
        }

        binding.buttonOpenCalculator.setOnClickListener {
            openInCalculator()
        }
    }

    private fun shareText(summary: ProjectSummary) {
        val shareText = buildString {
            appendLine("Job: ${summary.displayName}")
            appendLine("Area: ${String.format("%.1f", summary.areaSqFt)} sq ft")
            appendLine("Tile: ${String.format("%.1f", summary.tileWidthIn)} x ${String.format("%.1f", summary.tileHeightIn)} in")
            appendLine("Layout: ${summary.layoutStyle}")
            appendLine("Grout Gap: ${formatGroutGap(summary.groutGapInches)}")
            appendLine("Waste: ${String.format("%.0f", summary.wastePercent)}%")
            appendLine("Tiles Needed: ${summary.totalTilesNeededFinal}")
            if (summary.boxesNeeded != null) {
                appendLine("Boxes Needed: ${String.format("%.1f", summary.boxesNeeded)}")
            }
            appendLine("Notes: ${summary.notes ?: "—"}")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "TileVision AR estimate - ${summary.displayName}")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Share job summary"))
    }

    private fun sharePdf(summary: ProjectSummary) {
        try {
            val pdfFile = PdfExporter.exportProjectSummaryToPdf(this, summary)
            
            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                pdfFile
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            startActivity(Intent.createChooser(intent, "Share Job PDF"))
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to export PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteDialog() {
        val s = summary ?: return
        
        AlertDialog.Builder(this)
            .setTitle("Delete this job?")
            .setPositiveButton("Delete") { _, _ ->
                binding.buttonDelete.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                val deleted = ProjectSummaryRepository.deleteSummary(s.id)
                if (deleted) {
                    Toast.makeText(this, "Job deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openInCalculator() {
        val s = summary ?: return
        
        val intent = Intent(this, TileCalculatorActivity::class.java).apply {
            putExtra("areaSqFeet", s.areaSqFt)
            putExtra("tileWidthIn", s.tileWidthIn)
            putExtra("tileHeightIn", s.tileHeightIn)
            putExtra("wastePercent", s.wastePercent)
        }
        startActivity(intent)
    }
    
    override fun onResume() {
        super.onResume()
        if (::gridBackground.isInitialized && gridBackground != null) {
            gridBackground.applyInitialEnabledState(this)
            if (GridBackgroundView.isEnabled(this)) {
                gridBackground.setGridEnabled(this, true)
            } else {
                gridBackground.setGridEnabled(this, false)
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        if (::gridBackground.isInitialized && gridBackground != null) {
            gridBackground.stopAnimators()
        }
    }
}
