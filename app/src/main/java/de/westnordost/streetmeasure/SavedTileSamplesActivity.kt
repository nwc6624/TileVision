package de.westnordost.streetmeasure

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetmeasure.databinding.ActivitySavedTileSamplesBinding

class SavedTileSamplesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavedTileSamplesBinding
    private lateinit var adapter: TileSamplesAdapter
    private lateinit var gridBackground: GridBackgroundView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        android.util.Log.d("TileVisionLifecycle", "onCreate SavedTileSamplesActivity starting")
        
        // Inflate the shared page shell
        setContentView(R.layout.layout_page_shell)
        
        // Get references to shell elements
        val pageContentContainer = findViewById<android.widget.FrameLayout>(R.id.pageContentContainer)
        gridBackground = findViewById(R.id.gridBackground)
        
        // Inflate the activity's own content layout into the shell's container
        try {
            layoutInflater.inflate(R.layout.activity_saved_tile_samples, pageContentContainer, true)
            android.util.Log.d("TileVisionLifecycle", "SavedTileSamplesActivity shell + content inflated ok")
        } catch (e: Exception) {
            android.util.Log.e("TileVisionLifecycle", "inflate failed in SavedTileSamplesActivity", e)
        }
        
        // Now set up binding on the inflated content
        binding = ActivitySavedTileSamplesBinding.bind(pageContentContainer)

        // Initialize repositories
        ProjectRepository.init(this)
        TileSampleRepository.init(this)

        setupToolbar()
        setupRecyclerView()
        setupCalculateTilesButton()
    }

    private fun setupToolbar() {
        binding.appHeader.apply {
            setTitle("Saved Tile Samples")
            setModeBack { finish() }
        }
    }

    private fun setupRecyclerView() {
        adapter = TileSamplesAdapter { tileSample ->
            openTileDetail(tileSample)
        }
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = adapter

        // Load tile samples
        loadTileSamples()
    }

    private fun setupCalculateTilesButton() {
        binding.buttonCalculateTiles.setOnClickListener {
            val intent = Intent(this, TileCalculatorActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadTileSamples() {
        val tileSamples = TileSampleRepository.getAllTileSamples()
        adapter.submitList(tileSamples)
    }

    private fun openTileDetail(tileSample: TileSample) {
        val intent = Intent(this, TileSampleDetailActivity::class.java).apply {
            putExtra("tile_id", tileSample.id)
        }
        startActivityForResult(intent, REQUEST_CODE_TILE_DETAIL)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_CODE_TILE_DETAIL) {
            // Refresh the list in case a tile was deleted
            loadTileSamples()
        }
    }

    private inner class TileSamplesAdapter(
        private val onTileClick: (TileSample) -> Unit
    ) : RecyclerView.Adapter<TileSamplesAdapter.TileSampleViewHolder>() {
        private var tileSamples = listOf<TileSample>()

        fun submitList(newTileSamples: List<TileSample>) {
            tileSamples = newTileSamples
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TileSampleViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_tile_sample_card, parent, false)
            return TileSampleViewHolder(view)
        }

        override fun onBindViewHolder(holder: TileSampleViewHolder, position: Int) {
            holder.bind(tileSamples[position])
        }

        override fun getItemCount(): Int = tileSamples.size

        inner class TileSampleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val rectanglePreview: RectanglePreviewView = itemView.findViewById(R.id.rectanglePreview)
            private val titleText: TextView = itemView.findViewById(R.id.titleText)
            private val subtitleText: TextView = itemView.findViewById(R.id.subtitleText)
            private val timestampText: TextView = itemView.findViewById(R.id.timestampText)

                    fun bind(tileSample: TileSample) {
                        titleText.text = tileSample.displayName
                        subtitleText.text = "${String.format("%.1f", tileSample.widthInInches)} in x ${String.format("%.1f", tileSample.heightInInches)} in"
                        timestampText.text = MeasurementUtils.formatTimestamp(tileSample.timestamp)

                        // Set rectangle preview data
                        rectanglePreview.setTileData(tileSample.widthInInches, tileSample.heightInInches, tileSample.areaSqFt)

                        itemView.setOnClickListener {
                            onTileClick(tileSample)
                        }

                        itemView.setOnLongClickListener {
                            showDeleteDialog(tileSample)
                            true
                        }
                    }
            
            private fun showDeleteDialog(tileSample: TileSample) {
                AlertDialog.Builder(this@SavedTileSamplesActivity)
                    .setTitle("Delete Tile Sample")
                    .setMessage("Are you sure you want to delete this tile sample?")
                    .setPositiveButton("Delete") { _, _ ->
                        val deleted = TileSampleRepository.deleteTileSample(tileSample.id)
                        if (deleted) {
                            Toast.makeText(this@SavedTileSamplesActivity, "Tile sample deleted", Toast.LENGTH_SHORT).show()
                            loadTileSamples()
                        }
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
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

    companion object {
        private const val REQUEST_CODE_TILE_DETAIL = 1002
    }
}
