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

        // Initialize repositories
        TileSampleRepository.init(this)

        setupToolbar()
        loadTileSample()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadTileSample() {
        val tileId = intent.getStringExtra("tile_id")
        if (tileId != null) {
            tileSample = TileSampleRepository.getTileSampleById(tileId)
            if (tileSample != null) {
                displayTileSample(tileSample!!)
            } else {
                Toast.makeText(this, "Tile sample not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayTileSample(tileSample: TileSample) {
        binding.tileNameText.text = tileSample.displayName
        binding.dimensionsText.text = "${String.format("%.1f", tileSample.widthInInches)} in x ${String.format("%.1f", tileSample.heightInInches)} in"
        binding.areaText.text = "${String.format("%.2f", tileSample.areaSqFt)} ft²"
        binding.timestampText.text = MeasurementUtils.formatTimestamp(tileSample.timestamp)

        // Set rectangle preview data
        binding.rectanglePreview.setTileData(tileSample.widthInInches, tileSample.heightInInches, tileSample.areaSqFt)
    }

    private fun setupClickListeners() {
        binding.useTileButton.setOnClickListener {
            useTile()
        }

        binding.renameButton.setOnClickListener {
            showRenameDialog()
        }

        binding.deleteButton.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun useTile() {
        val tile = tileSample ?: return

        val resultIntent = Intent().apply {
            putExtra(TileSampleMeasureActivity.EXTRA_TILE_WIDTH, tile.widthInInches)
            putExtra(TileSampleMeasureActivity.EXTRA_TILE_HEIGHT, tile.heightInInches)
            putExtra(TileSampleMeasureActivity.EXTRA_TILE_AREA, tile.areaSqFt)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun showRenameDialog() {
        val tile = tileSample ?: return

        val input = android.widget.EditText(this)
        input.setText(tile.displayName)
        input.selectAll()

        AlertDialog.Builder(this)
            .setTitle("Rename Tile")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    val updated = TileSampleRepository.updateTileName(tile.id, newName)
                    if (updated) {
                        // Refresh the tile sample from repository
                        tileSample = TileSampleRepository.getTileSampleById(tile.id)
                        tileSample?.let { displayTileSample(it) }
                        
                        Toast.makeText(this, "Tile renamed", Toast.LENGTH_SHORT).show()
                        
                        // Set result to indicate a change occurred
                        setResult(RESULT_OK)
                    }
                } else {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Tile Sample")
            .setMessage("Are you sure you want to delete this tile sample?")
            .setPositiveButton("Delete") { _, _ ->
                val tile = tileSample ?: return@setPositiveButton
                val deleted = TileSampleRepository.deleteTileSample(tile.id)
                if (deleted) {
                    Toast.makeText(this, "Tile sample deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}