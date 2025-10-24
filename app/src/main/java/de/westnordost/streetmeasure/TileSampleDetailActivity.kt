package de.westnordost.streetmeasure

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetmeasure.databinding.ActivityTileSampleDetailBinding

class TileSampleDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTileSampleDetailBinding
    private var tileSample: TileSample? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTileSampleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadTileSample()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Tile Sample Details"
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadTileSample() {
        val tileId = intent.getStringExtra("tile_id")
        if (tileId != null) {
            tileSample = TileSampleRepository.getTileSampleById(tileId)
            tileSample?.let { displayTileSample(it) }
        } else {
            Toast.makeText(this, "Tile sample not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun displayTileSample(tileSample: TileSample) {
        binding.tileNameText.text = tileSample.displayName
        binding.dimensionsText.text = "${String.format("%.2f", tileSample.width)} ${tileSample.units} x ${String.format("%.2f", tileSample.height)} ${tileSample.units}"
        binding.areaText.text = "${String.format("%.2f", tileSample.areaFt2)} ft²"
        binding.timestampText.text = MeasurementUtils.formatTimestamp(tileSample.timestamp)
        
        // TODO: Load preview image when screenshot capture is implemented
        binding.previewImage.setImageResource(R.drawable.ic_launcher_foreground)
    }

    private fun setupClickListeners() {
        binding.useTileButton.setOnClickListener {
            useTile()
        }
        
        binding.deleteButton.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun useTile() {
        tileSample?.let { tile ->
            val resultIntent = Intent().apply {
                putExtra("tile_width", tile.width)
                putExtra("tile_height", tile.height)
                putExtra("tile_area", tile.areaFt2)
                putExtra("tile_units", tile.units)
                putExtra("tile_id", tile.id)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Tile Sample")
            .setMessage("Are you sure you want to delete this tile sample? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteTileSample()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteTileSample() {
        tileSample?.let { tile ->
            val deleted = TileSampleRepository.deleteTileSample(tile.id)
            if (deleted) {
                Toast.makeText(this, "Tile sample deleted", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to delete tile sample", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
