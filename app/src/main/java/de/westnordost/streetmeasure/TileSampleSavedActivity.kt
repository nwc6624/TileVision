package de.westnordost.streetmeasure

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.westnordost.streetmeasure.databinding.ActivityTileSampleSavedBinding

class TileSampleSavedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTileSampleSavedBinding
    private var tileId: String? = null
    private var tileSample: TileSample? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTileSampleSavedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        // Get the tile ID from intent
        tileId = intent.getStringExtra("tile_id")
        tileId?.let {
            tileSample = TileSampleRepository.getTileSampleById(it)
            tileSample?.let { ts ->
                displayTileDetails(ts)
                setupClickListeners(ts)
            } ?: run {
                Toast.makeText(this, "Tile sample not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        } ?: run {
            Toast.makeText(this, "Invalid tile ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Tile Sample Saved"
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun displayTileDetails(tileSample: TileSample) {
        binding.tileTitle.text = tileSample.displayName
        binding.tileDimensions.text = "${String.format("%.1f", tileSample.widthInInches)} in x ${String.format("%.1f", tileSample.heightInInches)} in"
        binding.tileArea.text = "${String.format("%.2f", tileSample.areaSqFt)} ft²"
        binding.tileTimestamp.text = MeasurementUtils.formatTimestamp(tileSample.timestamp)

        // Show placeholder image since we're not storing screenshots yet
        binding.tileImage.setImageResource(R.drawable.ic_launcher_foreground)
    }

    private fun setupClickListeners(tileSample: TileSample) {
        binding.buttonCalculateTiles.setOnClickListener {
            // Launch TileCalculatorActivity with the tile dimensions
            val intent = Intent(this, TileCalculatorActivity::class.java).apply {
                putExtra(TileSampleMeasureActivity.EXTRA_TILE_WIDTH, tileSample.widthInInches)
                putExtra(TileSampleMeasureActivity.EXTRA_TILE_HEIGHT, tileSample.heightInInches)
                putExtra(TileSampleMeasureActivity.EXTRA_TILE_AREA, tileSample.areaSqFt)
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

        binding.buttonViewAllTiles.setOnClickListener {
            // Go to saved tile samples page
            val intent = Intent(this, SavedTileSamplesActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
