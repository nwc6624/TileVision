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
import com.google.android.material.slider.Slider

class TileCalculatorActivity : AppCompatActivity() {
    
    companion object {
        private const val REQUEST_CODE_SELECT_TILE = 1001
        private const val REQUEST_CODE_MEASURE_TILE = 1002
        private const val REQUEST_CODE_SELECT_PROJECT = 1003
    }
    
    // Views
    private lateinit var manualAreaInput: android.widget.EditText
    private lateinit var tileWidthInput: android.widget.EditText
    private lateinit var tileHeightInput: android.widget.EditText
    private lateinit var wasteSlider: Slider
    private lateinit var wastePercentBadge: TextView
    private lateinit var tilesNeededText: TextView
    private lateinit var tilesCalculationDetailsText: TextView
    private lateinit var notesInput: android.widget.EditText
    
    private var incomingArea: Float = 0f
    private var projectMeasurementId: String? = null
    private var tileSampleId: String? = null
    private var currentAreaSqFt: Float = 0f  // Track current area value
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tile_calculator)
        
        // Initialize AppPrefs and Repositories
        AppPrefs.init(this)
        ProjectRepository.init(this)
        TileSampleRepository.init(this)
        ProjectSummaryRepository.init(this)
        
        // Grab views
        manualAreaInput = findViewById(R.id.manualAreaInput)
        tileWidthInput = findViewById(R.id.tileWidthInput)
        tileHeightInput = findViewById(R.id.tileHeightInput)
        wasteSlider = findViewById(R.id.wasteSlider)
        wastePercentBadge = findViewById(R.id.wastePercentBadge)
        tilesNeededText = findViewById(R.id.tilesNeededText)
        tilesCalculationDetailsText = findViewById(R.id.tilesCalculationDetailsText)
        notesInput = findViewById(R.id.notesInput)
        
        // Read incoming data
        incomingArea = intent.getFloatExtra("areaSqFeet", 0f)
        if (incomingArea == 0f) {
            incomingArea = intent.getFloatExtra("EXTRA_AREA_FT2", 0f)
        }
        projectMeasurementId = intent.getStringExtra("projectMeasurementId")
        tileSampleId = intent.getStringExtra("tileSampleId")
        android.util.Log.d("TileCalculatorActivity", "Received incoming area: $incomingArea ft², projectId: $projectMeasurementId, tileId: $tileSampleId")
        
        // Setup header
        setupHeader()
        
        // Setup grid background
        val gridBackground = findViewById<GridBackgroundView>(R.id.gridBackground)
        gridBackground?.setEnabledState(GridBackgroundView.isEnabled(this), saveToPreferences = false)
        
        // Set up area display
        if (incomingArea > 0) {
            currentAreaSqFt = incomingArea
            updateMeasuredAreaChip(incomingArea)
            android.util.Log.d("TileCalculatorActivity", "Set measured area to: $incomingArea")
        }
        
        // Load default tile sizes from preferences
        loadDefaultTileSizes()
        
        // Setup input change listeners to trigger calculation
        manualAreaInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                calculateTiles()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        
        tileWidthInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                calculateTiles()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        
        tileHeightInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                calculateTiles()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        
        // Setup waste slider listener
        wasteSlider.addOnChangeListener { _, value, _ ->
            val percent = value.toInt()
            wastePercentBadge.text = "$percent%"
            calculateTiles()
        }
        
        // Initial calculation
        calculateTiles()
        
        // Set click listeners
        findViewById<android.widget.Button>(R.id.buttonSaveJobSummary)?.setOnClickListener {
            saveJobSummary()
        }
        
        findViewById<android.widget.Button>(R.id.buttonBackToHome)?.setOnClickListener {
            finish()
        }
        
        findViewById<android.widget.Button>(R.id.buttonMeasureTile)?.setOnClickListener {
            val intent = Intent(this, TileSampleMeasureActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_MEASURE_TILE)
        }
        
        // New button listeners
        findViewById<android.widget.ImageButton>(R.id.buttonMeasureArea)?.setOnClickListener {
            val intent = Intent(this, MeasureActivity::class.java)
            startActivity(intent)
        }
        
        findViewById<android.widget.ImageButton>(R.id.buttonPickSavedArea)?.setOnClickListener {
            val intent = Intent(this, SavedProjectsActivity::class.java)
            intent.putExtra("picker_mode", true)
            startActivityForResult(intent, REQUEST_CODE_SELECT_PROJECT)
        }
        
        findViewById<android.widget.Button>(R.id.buttonPickSavedTile)?.setOnClickListener {
            val intent = Intent(this, SavedTileSamplesActivity::class.java)
            intent.putExtra("picker_mode", true)
            startActivityForResult(intent, REQUEST_CODE_SELECT_TILE)
        }
        }
        
    private fun updateMeasuredAreaChip(area: Float) {
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.measuredAreaChip)?.apply {
            visibility = android.view.View.VISIBLE
            findViewById<TextView>(R.id.measuredAreaText)?.text = "${String.format("%.2f", area)} ft²"
        }
    }
    
    private fun saveJobSummary() {
        showSaveJobSummaryDialog()
    }
    
    private fun calculateTiles() {
        // 1. Determine areaFt2 - priority: currentAreaSqFt > incomingArea > manual input
        val areaFt2 = when {
            currentAreaSqFt > 0 -> currentAreaSqFt
            incomingArea > 0 -> incomingArea
            else -> {
                try {
                    val manualArea = manualAreaInput.text.toString().toFloat()
                    if (manualArea <= 0) {
                        tilesNeededText.text = "0 tiles"
                        return
                    }
                    manualArea
                } catch (e: NumberFormatException) {
                    tilesNeededText.text = "0 tiles"
                    return
                }
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
            // Just show "0 tiles" if no tile dimensions entered yet
            tilesNeededText.text = "0 tiles"
            return
        }
        
        // 3. Read waste percent from slider
        val wastePercent = wasteSlider.value
        
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
        
        // 6. Update UI
        tilesNeededText.text = "$tilesNeededRoundedUp tiles"
        tilesCalculationDetailsText.text = String.format(
            "%.1f calculated • %.0f%% waste • rounded up",
            rawTileCount,
            wastePercent
        )
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
                tilesNeededText.text = "-- tiles"
                tilesCalculationDetailsText.text = ""
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
        
        // Create bottom sheet dialog view
        val dialogView = layoutInflater.inflate(R.layout.dialog_save_summary, null)
        val jobNameText = dialogView.findViewById<EditText>(R.id.jobNameText)
        val notesText = dialogView.findViewById<EditText>(R.id.notesText)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
        val buttonSave = dialogView.findViewById<Button>(R.id.buttonSave)
        
        // Pre-fill with default values
        val defaultName = MeasurementUtils.formatDisplayName("Job", System.currentTimeMillis())
        jobNameText.setText(defaultName)
        
        // Create bottom sheet dialog
        val bottomSheet = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        bottomSheet.setContentView(dialogView)
        bottomSheet.behavior.peekHeight = com.google.android.material.bottomsheet.BottomSheetBehavior.PEEK_HEIGHT_AUTO
        
        buttonCancel.setOnClickListener {
            bottomSheet.dismiss()
        }
        
        buttonSave.setOnClickListener {
            val jobNameValue = jobNameText.text.toString().trim()
            val notesValue = notesText.text.toString().trim()
            android.util.Log.d("TileVision", "Notes captured from dialog: '$notesValue'")
            saveJobSummary(
                if (jobNameValue.isBlank()) defaultName else jobNameValue,
                notesValue,
                wasteSlider.value
            )
            bottomSheet.dismiss()
        }
        
        bottomSheet.show()
    }
    
    private fun saveJobSummary(displayName: String, notes: String, wastePercent: Float) {
        android.util.Log.d("TileVision", "saveJobSummary called with notes='$notes'")
        
        // Get current values
        val currentArea = if (incomingArea > 0) incomingArea else manualAreaInput.text.toString().toFloat()
        val tileWidth = tileWidthInput.text.toString().toFloat()
        val tileHeight = tileHeightInput.text.toString().toFloat()
        val tileAreaSqFt = (tileWidth / 12f) * (tileHeight / 12f)
        
        // Get layout style and grout gap from dropdowns
        val layoutStyleLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.layoutStyleLayout)
        val layoutStyleDropdown = layoutStyleLayout?.editText?.text?.toString() ?: "Straight"
        val groutGapLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.groutGapLayout)
        val groutGapText = groutGapLayout?.editText?.text?.toString() ?: "1/8"
        
        // Parse grout gap to inches
        val groutGapInches = when (groutGapText) {
            "1/16" -> 0.0625f
            "1/8" -> 0.125f
            "3/16" -> 0.1875f
            "1/4" -> 0.25f
            else -> 0.125f
        }
        
        // Calculate tiles needed
        val rawTileCount = currentArea / tileAreaSqFt
        val withWasteTileCount = rawTileCount * (1f + wastePercent / 100f)
        val finalTileCount = kotlin.math.round(withWasteTileCount).toInt()
        
        // Determine final notes value
        val finalNotes = notes.ifEmpty { null }
        android.util.Log.d("TileVision", "Final notes value: '${finalNotes ?: "(null)"}'")
        
        // Create ProjectSummary
        val summary = ProjectSummary(
            id = java.util.UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            displayName = displayName,
            projectMeasurementId = projectMeasurementId,
            tileSampleId = tileSampleId,
            areaSqFt = currentArea,
            tileWidthIn = tileWidth,
            tileHeightIn = tileHeight,
            tileAreaSqFt = tileAreaSqFt,
            layoutStyle = layoutStyleDropdown,
            groutGapInches = groutGapInches,
            wastePercent = wastePercent,
            totalTilesNeededRaw = rawTileCount,
            totalTilesNeededFinal = finalTileCount,
            notes = finalNotes
        )
        
        // Save to repository
        android.util.Log.d("TileVision", "Saving job summary to repository...")
        ProjectSummaryRepository.addSummary(summary)
        android.util.Log.d("TileVision", "Summary saved: ${summary.displayName}")
        
        // Haptic feedback
        window.decorView.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
        
        // Show confirmation snackbar with "View" action
        val snackbar = com.google.android.material.snackbar.Snackbar.make(
            findViewById(android.R.id.content),
            "Saved to Jobs",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        )
        snackbar.setAction("View") {
            val intent = Intent(this, SavedSummariesActivity::class.java)
            startActivity(intent)
        }
        snackbar.show()
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
                        calculateTiles() // Recalculate after loading tile dimensions
                        val message = if (requestCode == REQUEST_CODE_MEASURE_TILE) {
                            "Tile dimensions loaded from measurement"
                        } else {
                            "Tile dimensions loaded from saved sample"
                        }
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                }
                REQUEST_CODE_SELECT_PROJECT -> {
                    val projectArea = data.getFloatExtra("EXTRA_AREA_FT2", 0f)
                    // Also try the old key for compatibility
                    val projectAreaAlt = if (projectArea == 0f) data.getFloatExtra("project_area", 0f) else projectArea
                    
                    if (projectAreaAlt > 0) {
                        currentAreaSqFt = projectAreaAlt
                        incomingArea = projectAreaAlt
                        // Update UI with the measured area chip
                        updateMeasuredAreaChip(projectAreaAlt)
                        manualAreaInput.setText("") // Clear manual input since we're using AR measurement
                        calculateTiles() // Recalculate after loading project area
                        Toast.makeText(this, "Area loaded: ${String.format("%.2f", projectAreaAlt)} ft²", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    private fun setupHeader() {
        val headerView = findViewById<de.westnordost.streetmeasure.AppHeaderView>(R.id.appHeader)
        headerView?.apply {
            setTitle("Tile Calculator")
            setSubtitle("Estimate tiles and waste")
            setModeBack { finish() }
        }
    }
}
