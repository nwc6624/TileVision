package de.westnordost.streetmeasure

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class TileCalculatorActivity : AppCompatActivity() {
    
    companion object {
        private const val REQUEST_CODE_SELECT_TILE = 1001
        private const val REQUEST_CODE_MEASURE_TILE = 1002
        private const val REQUEST_CODE_SELECT_PROJECT = 1003
    }
    
    // Views
    private lateinit var measuredAreaText: TextView
    private lateinit var manualAreaInput: EditText
    private lateinit var tileWidthInput: EditText
    private lateinit var tileHeightInput: EditText
    private lateinit var wasteInput: EditText
    private lateinit var boxCoverageInput: EditText
    private lateinit var calcButton: Button
    private lateinit var tilesNeededText: TextView
    private lateinit var boxesNeededText: TextView
    
    private var incomingArea: Float = 0f
    private var projectMeasurementId: String? = null
    private var tileSampleId: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tile_calculator)
        
        // Initialize AppPrefs and Repositories
        AppPrefs.init(this)
        ProjectRepository.init(this)
        TileSampleRepository.init(this)
        ProjectSummaryRepository.init(this)
        
        // Grab views
        measuredAreaText = findViewById(R.id.measuredAreaText)
        manualAreaInput = findViewById(R.id.manualAreaInput)
        tileWidthInput = findViewById(R.id.tileWidthInput)
        tileHeightInput = findViewById(R.id.tileHeightInput)
        wasteInput = findViewById(R.id.wasteInput)
        boxCoverageInput = findViewById(R.id.boxCoverageInput)
        calcButton = findViewById(R.id.calcButton)
        tilesNeededText = findViewById(R.id.tilesNeededText)
        boxesNeededText = findViewById(R.id.boxesNeededText)
        
        // Read incoming data
        incomingArea = intent.getFloatExtra("areaSqFeet", 0f)
        projectMeasurementId = intent.getStringExtra("projectMeasurementId")
        tileSampleId = intent.getStringExtra("tileSampleId")
        android.util.Log.d("TileCalculatorActivity", "Received incoming area: $incomingArea ft², projectId: $projectMeasurementId, tileId: $tileSampleId")
        
        // Set up area display
        if (incomingArea > 0) {
                measuredAreaText.text = String.format("%.2f ft²", incomingArea)
            manualAreaInput.setText("")  // leave manual empty, incoming wins
            android.util.Log.d("TileCalculatorActivity", "Set measured area text to: ${measuredAreaText.text}")
        } else {
            measuredAreaText.text = "—"
            manualAreaInput.setText("")  // user must type
            android.util.Log.d("TileCalculatorActivity", "No incoming area, showing '—'")
        }
        
        // Load default tile sizes from preferences
        loadDefaultTileSizes()
        
        // Set up highlighting for required fields
        setupFieldHighlighting()
        
        // Set click listeners
        calcButton.setOnClickListener {
            calculateTiles()
        }
        
        findViewById<Button>(R.id.buttonMeasureTileSample).setOnClickListener {
            val intent = Intent(this, TileSampleMeasureActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_MEASURE_TILE)
        }
        
        findViewById<Button>(R.id.buttonChooseFromSavedTiles).setOnClickListener {
            val intent = Intent(this, SavedTileSamplesActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_SELECT_TILE)
        }
        
        findViewById<Button>(R.id.buttonChooseFromSavedProjects).setOnClickListener {
            val intent = Intent(this, SavedProjectsActivity::class.java).apply {
                putExtra("from_calculator", true)
            }
            startActivityForResult(intent, REQUEST_CODE_SELECT_PROJECT)
        }
        
        findViewById<Button>(R.id.buttonSaveJobSummary).setOnClickListener {
            showSaveJobSummaryDialog()
        }
    }
    
    private fun calculateTiles() {
        // 1. Determine areaFt2
        val areaFt2 = if (incomingArea > 0) {
            incomingArea
        } else {
            try {
                val manualArea = manualAreaInput.text.toString().toFloat()
                if (manualArea <= 0) {
                    Toast.makeText(this, "Enter area in ft² first", Toast.LENGTH_SHORT).show()
                    highlightField(manualAreaInput)
                    return
                }
                manualArea
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Enter area in ft² first", Toast.LENGTH_SHORT).show()
                highlightField(manualAreaInput)
                return
            }
        }
        
        // 2. Read tile dimensions
        val tileWidthIn = try {
            tileWidthInput.text.toString().toFloat()
        } catch (e: NumberFormatException) {
            0f
        }
        
        val tileHeightIn = try {
            tileHeightInput.text.toString().toFloat()
        } catch (e: NumberFormatException) {
            0f
        }
        
        if (tileWidthIn <= 0 || tileHeightIn <= 0) {
            Toast.makeText(this, "Enter tile width/height", Toast.LENGTH_SHORT).show()
            if (tileWidthIn <= 0) highlightField(tileWidthInput)
            if (tileHeightIn <= 0) highlightField(tileHeightInput)
            return
        }
        
        // 3. Read waste percent
        val wastePercent = try {
            wasteInput.text.toString().toFloat()
        } catch (e: NumberFormatException) {
            10f
        }
        
        // 4. Read box coverage
        val boxCoverageFt2 = try {
            boxCoverageInput.text.toString().toFloat()
        } catch (e: NumberFormatException) {
            0f
        }
        
        // 5. Compute
        val tileAreaFt2 = (tileWidthIn / 12f) * (tileHeightIn / 12f)
        
        val rawTileCount = areaFt2 / tileAreaFt2
        
        val withWasteTileCount = rawTileCount * (1f + wastePercent / 100f)
        
        // Apply grout gap if enabled
        val finalTileCount = if (AppPrefs.getIncludeGrout()) {
            withWasteTileCount * 1.05f // Add 5% for grout gaps
        } else {
            withWasteTileCount
        }
        
        val tilesNeededRoundedUp = if (AppPrefs.getForceRoundUp()) {
            ceil(finalTileCount).toInt()
        } else {
            kotlin.math.round(finalTileCount).toInt()
        }
        
        val boxesNeededRoundedUp = if (boxCoverageFt2 > 0f) {
            val boxesRaw = (areaFt2 * (1f + wastePercent / 100f)) / boxCoverageFt2
            ceil(boxesRaw).toInt()
        } else {
            0
        }
        
        // 6. Update
        tilesNeededText.text = "Tiles Needed: $tilesNeededRoundedUp"
        boxesNeededText.text = "Boxes Needed: $boxesNeededRoundedUp"
        
        // 7. Show results popup
        showResultsDialog(tilesNeededRoundedUp, boxesNeededRoundedUp, areaFt2, tileWidthIn, tileHeightIn, wastePercent)
    }
    
    private fun ceil(x: Float): Float {
        return kotlin.math.ceil(x.toDouble()).toFloat()
    }
    
    private fun loadDefaultTileSizes() {
        // Load default tile sizes from preferences if they exist
        val defaultLength = AppPrefs.getDefaultTileLength()
        val defaultWidth = AppPrefs.getDefaultTileWidth()
        
        if (defaultLength != null && tileWidthInput.text.isNullOrBlank()) {
            tileWidthInput.setText(defaultLength.toString())
        }
        
        if (defaultWidth != null && tileHeightInput.text.isNullOrBlank()) {
            tileHeightInput.setText(defaultWidth.toString())
        }
    }
    
    private fun setupFieldHighlighting() {
        // Highlight required fields that need user input
        if (incomingArea <= 0) {
            highlightField(manualAreaInput)
        }
        
        // Always highlight tile dimensions as they're always required
        highlightField(tileWidthInput)
        highlightField(tileHeightInput)
        
        // Add text change listeners to clear highlighting when fields are filled
        manualAreaInput.addTextChangedListener(createTextWatcher(manualAreaInput))
        tileWidthInput.addTextChangedListener(createTextWatcher(tileWidthInput))
        tileHeightInput.addTextChangedListener(createTextWatcher(tileHeightInput))
    }
    
    private fun highlightField(editText: EditText) {
        editText.setBackgroundColor(Color.parseColor("#FFF3CD")) // Light yellow highlight
        editText.setHintTextColor(Color.parseColor("#856404")) // Darker yellow for hint
    }
    
    private fun clearHighlight(editText: EditText) {
        editText.setBackgroundColor(Color.WHITE)
        editText.setHintTextColor(Color.parseColor("#757575")) // Default gray
    }
    
    private fun createTextWatcher(editText: EditText): android.text.TextWatcher {
        return object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (!s.isNullOrBlank()) {
                    clearHighlight(editText)
                }
            }
        }
    }
    
    private fun showResultsDialog(tilesNeeded: Int, boxesNeeded: Int, areaFt2: Float, tileWidth: Float, tileHeight: Float, wastePercent: Float) {
        val message = buildString {
            append("📐 **Calculation Results**\n\n")
            append("**Area to Cover:** ${String.format("%.1f", areaFt2)} ft²\n")
            append("**Tile Size:** ${String.format("%.1f", tileWidth)}\" × ${String.format("%.1f", tileHeight)}\"\n")
            append("**Waste Allowance:** ${String.format("%.0f", wastePercent)}%\n\n")
            append("**📊 Results:**\n")
            append("• **Tiles Needed:** $tilesNeeded\n")
            if (boxesNeeded > 0) {
                append("• **Boxes Needed:** $boxesNeeded\n")
            } else {
                append("• **Boxes Needed:** Not calculated (no coverage per box specified)\n")
            }
            append("\n💡 **Tip:** Always buy a few extra tiles for cutting and future repairs!")
        }
        
        AlertDialog.Builder(this)
            .setTitle("🎯 Tile Calculation Complete")
            .setMessage(message)
            .setPositiveButton("Got it!") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Recalculate") { dialog, _ ->
                dialog.dismiss()
                // Clear the results to encourage recalculation
                tilesNeededText.text = "Tiles Needed: --"
                boxesNeededText.text = "Boxes Needed: --"
            }
            .setCancelable(true)
            .show()
    }
    
    private fun showSaveJobSummaryDialog() {
        // Validate that we have the minimum data
        val currentArea = if (incomingArea > 0) {
            incomingArea
        } else {
            try {
                manualAreaInput.text.toString().toFloat()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Please calculate first", Toast.LENGTH_SHORT).show()
                return
            }
        }
        
        val tileWidth = try {
            tileWidthInput.text.toString().toFloat()
        } catch (e: NumberFormatException) {
            0f
        }
        
        val tileHeight = try {
            tileHeightInput.text.toString().toFloat()
        } catch (e: NumberFormatException) {
            0f
        }
        
        if (tileWidth <= 0 || tileHeight <= 0) {
            Toast.makeText(this, "Please enter tile dimensions first", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Create dialog view
        val dialogView = layoutInflater.inflate(R.layout.dialog_save_job_summary, null)
        val editProjectName = dialogView.findViewById<EditText>(R.id.editProjectName)
        val editNotes = dialogView.findViewById<EditText>(R.id.editNotes)
        val editWastePercent = dialogView.findViewById<EditText>(R.id.editWastePercent)
        
        // Pre-fill with default values
        val defaultName = MeasurementUtils.formatDisplayName("Job", System.currentTimeMillis())
        editProjectName.setText(defaultName)
        editWastePercent.setText(wasteInput.text.toString())
        
        val dialog = AlertDialog.Builder(this)
            .setTitle("Save Project Summary")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                saveJobSummary(
                    editProjectName.text.toString().trim().ifEmpty { defaultName },
                    editNotes.text.toString(),
                    editWastePercent.text.toString().toFloatOrNull() ?: 10f
                )
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
    
    private fun saveJobSummary(displayName: String, notes: String, wastePercent: Float) {
        // Get current values
        val currentArea = if (incomingArea > 0) incomingArea else manualAreaInput.text.toString().toFloat()
        val tileWidth = tileWidthInput.text.toString().toFloat()
        val tileHeight = tileHeightInput.text.toString().toFloat()
        val tileAreaSqFt = (tileWidth / 12f) * (tileHeight / 12f)
        
        // Calculate tiles needed
        val rawTileCount = currentArea / tileAreaSqFt
        val withWasteTileCount = rawTileCount * (1f + wastePercent / 100f)
        val finalTileCount = kotlin.math.round(withWasteTileCount).toInt()
        
        // Create ProjectSummary
        val summary = ProjectSummary(
            id = java.util.UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            displayName = displayName,
            projectMeasurementId = projectMeasurementId ?: "",
            tileSampleId = tileSampleId,
            areaSqFt = currentArea,
            tileWidthIn = tileWidth,
            tileHeightIn = tileHeight,
            tileAreaSqFt = tileAreaSqFt,
            wastePercent = wastePercent,
            totalTilesNeededRaw = rawTileCount,
            totalTilesNeededFinal = finalTileCount,
            notes = notes.ifEmpty { null }
        )
        
        // Save to repository
        ProjectSummaryRepository.addSummary(summary)
        
        // Show confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Job Summary Saved")
            .setMessage("Your job summary has been saved successfully.")
            .setPositiveButton("View Saved Jobs") { _, _ ->
                val intent = Intent(this, SavedSummariesActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("Done") { _, _ ->
                // Just dismiss
            }
            .show()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_CODE_SELECT_TILE, REQUEST_CODE_MEASURE_TILE -> {
                    val tileWidth = data.getFloatExtra(TileSampleMeasureActivity.EXTRA_TILE_WIDTH, 0f)
                    val tileHeight = data.getFloatExtra(TileSampleMeasureActivity.EXTRA_TILE_HEIGHT, 0f)
                    
                    if (tileWidth > 0 && tileHeight > 0) {
                        tileWidthInput.setText(String.format("%.1f", tileWidth))
                        tileHeightInput.setText(String.format("%.1f", tileHeight))
                        val message = if (requestCode == REQUEST_CODE_MEASURE_TILE) {
                            "Tile dimensions loaded from measurement"
                        } else {
                            "Tile dimensions loaded from saved sample"
                        }
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                }
                REQUEST_CODE_SELECT_PROJECT -> {
                    val projectArea = data.getFloatExtra("project_area", 0f)
                    
                    if (projectArea > 0) {
                        // Set the measured area and clear manual input
                        measuredAreaText.text = String.format("%.2f ft²", projectArea)
                        manualAreaInput.setText("")
                        incomingArea = projectArea
                                Toast.makeText(this, "Project area loaded: ${String.format("%.2f", projectArea)} ft²", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
