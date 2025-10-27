package de.westnordost.streetmeasure

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetmeasure.databinding.ActivitySavedSummariesBinding

class SavedSummariesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavedSummariesBinding
    private lateinit var adapter: SummariesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedSummariesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize repositories
        ProjectRepository.init(this)
        TileSampleRepository.init(this)
        ProjectSummaryRepository.init(this)

        // Setup grid background
        val gridBackground = binding.root.findViewById<GridBackgroundView>(R.id.gridBackground)
        gridBackground?.setEnabledState(GridBackgroundView.isEnabled(this), saveToPreferences = false)

        setupHeader()
        setupRecyclerView()
        setupEmptyState()
    }

    private fun setupHeader() {
        binding.appHeader.setTitle("Saved Jobs")
        binding.appHeader.setSubtitle(null)
        binding.appHeader.setModeBack { finish() }
    }

    private fun setupRecyclerView() {
        adapter = SummariesAdapter { summary ->
            openSummaryDetail(summary)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupEmptyState() {
        binding.emptyStateButton.setOnClickListener {
            val intent = Intent(this, TileCalculatorActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadSummaries() {
        val summaries = ProjectSummaryRepository.getAllSummaries()
        android.util.Log.d("TileVision", "Loaded ${summaries.size} summaries")
        adapter.submitList(summaries)
        
        // Show/hide empty state
        if (summaries.isEmpty()) {
            binding.recyclerView.visibility = View.GONE
            binding.emptyStateLayout.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.emptyStateLayout.visibility = View.GONE
        }
    }

    private fun openSummaryDetail(summary: ProjectSummary) {
        val intent = Intent(this, ProjectSummaryDetailActivity::class.java).apply {
            putExtra("summary_id", summary.id)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadSummaries()
    }

    private inner class SummariesAdapter(
        private val onSummaryClick: (ProjectSummary) -> Unit
    ) : RecyclerView.Adapter<SummariesAdapter.SummaryViewHolder>() {
        private var summaries = listOf<ProjectSummary>()

        fun submitList(newSummaries: List<ProjectSummary>) {
            summaries = newSummaries
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_summary_card, parent, false)
            return SummaryViewHolder(view)
        }

        override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {
            holder.bind(summaries[position])
        }

        override fun getItemCount(): Int = summaries.size

        inner class SummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val titleText: TextView = itemView.findViewById(R.id.titleText)
            private val areaText: TextView = itemView.findViewById(R.id.areaText)
            private val tileText: TextView = itemView.findViewById(R.id.tileText)
            private val timestampText: TextView = itemView.findViewById(R.id.timestampText)

            fun bind(summary: ProjectSummary) {
                titleText.text = summary.displayName
                areaText.text = "${String.format("%.2f", summary.areaSqFt)} ft² • ${summary.totalTilesNeededFinal} tiles"
                tileText.text = "${String.format("%.1f", summary.tileWidthIn)} × ${String.format("%.1f", summary.tileHeightIn)} in"
                timestampText.text = MeasurementUtils.formatTimestamp(summary.timestamp)

                itemView.setOnClickListener {
                    onSummaryClick(summary)
                }

                itemView.setOnLongClickListener {
                    showDeleteDialog(summary)
                    true
                }
            }

            private fun showDeleteDialog(summary: ProjectSummary) {
                AlertDialog.Builder(this@SavedSummariesActivity)
                    .setTitle("Delete this job?")
                    .setPositiveButton("Delete") { _, _ ->
                        itemView.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                        val deleted = ProjectSummaryRepository.deleteSummary(summary.id)
                        if (deleted) {
                            Toast.makeText(this@SavedSummariesActivity, "Job deleted", Toast.LENGTH_SHORT).show()
                            loadSummaries()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
}
