package de.westnordost.streetmeasure

import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import de.westnordost.streetmeasure.databinding.ActivityHomeBinding
import java.util.Date

class HomeActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHomeBinding
    private lateinit var gridBackground: GridBackgroundView

    // CHECKPOINT:
    // - Recent Measurements list should update because MeasurementStore was updated in MeasureActivity.
    // - Tapping a saved measurement should jump straight into TileCalculatorActivity with that area passed as "areaSqFeet" for cost planning.
    //
    // IMPORTANT: Do not reintroduce functionality that limits the app to 2 anchors, or that calculates only linear distance.
    // The ONLY AR workflow we support in TileVision is multi-point polygon surface area measurement for square footage, for tile planning.
    
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(ThemeManager.load(this))
        super.onCreate(savedInstanceState)
        
        // Inflate the shared page shell
        setContentView(R.layout.layout_page_shell)
        
        // Get references to shell elements
        gridBackground = findViewById(R.id.gridBackground)
        val pageContentContainer = findViewById<android.widget.FrameLayout>(R.id.pageContentContainer)
        
        // Inflate the activity's own content layout into the shell's container
        layoutInflater.inflate(R.layout.activity_home, pageContentContainer, true)
        
        // Now set up binding on the inflated content
        binding = ActivityHomeBinding.bind(pageContentContainer)
        
        // Initialize AppPrefs and repositories
        AppPrefs.init(this)
        ProjectRepository.init(this)
        TileSampleRepository.init(this)
        ProjectSummaryRepository.init(this)
        
        setupHeader()
        setupButtons()
        setupClickListeners()
    }
    
    private fun setupHeader() {
        binding.appHeader.setTitle("TileVision AR")
        binding.appHeader.setSubtitle("Measure. Visualize. Estimate.")
        binding.appHeader.setModeHome {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        
        // Set up grid toggle button
        binding.appHeader.setGridToggle {
            gridBackground.toggleGrid(this)
            
            // Update icon color based on state
            val newState = GridBackgroundView.isEnabled(this)
            binding.appHeader.updateGridToggleState(newState)
            
            // Haptic feedback
            window.decorView.performHapticFeedback(
                android.view.HapticFeedbackConstants.VIRTUAL_KEY
            )
        }
        
        // Set initial icon color based on current state
        binding.appHeader.updateGridToggleState(GridBackgroundView.isEnabled(this))
    }
    
    private fun setupButtons() {
        // Set text for library buttons that use include layout
        setButtonText(R.id.buttonViewSavedProjects, "View Saved Projects")
        setButtonText(R.id.buttonViewSavedTileSamples, "View Saved Tile Samples")
        setButtonText(R.id.buttonViewSavedJobs, "View Saved Jobs")
    }
    
    private fun setButtonText(buttonId: Int, text: String) {
        findViewById<android.view.View>(buttonId)?.apply {
            findViewById<android.widget.TextView>(R.id.buttonText)?.text = text
            findViewById<android.widget.TextView>(R.id.buttonIcon)?.visibility = android.view.View.VISIBLE
        }
    }
    
    override fun onResume() {
        super.onResume()
        populateRecentMeasurements()
        
        // Apply initial grid state and start/stop animators
        gridBackground.applyInitialEnabledState(this)
        if (GridBackgroundView.isEnabled(this)) {
            gridBackground.setGridEnabled(this, true)
        } else {
            gridBackground.setGridEnabled(this, false)
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Stop animators when leaving screen to prevent crash
        gridBackground.stopAnimators()
    }
    
    private fun setupClickListeners() {
        binding.btnStartMeasurement.setOnClickListener {
            startMeasurement()
        }
        
        findViewById<View>(R.id.buttonMeasureTileSample).setOnClickListener {
            val intent = Intent(this, TileSampleMeasureActivity::class.java)
            startActivity(intent)
        }
        
        findViewById<View>(R.id.openCalculatorButton).setOnClickListener {
            val intent = Intent(this, TileCalculatorActivity::class.java)
            // do NOT put area extra here, user will type manually
            startActivity(intent)
        }
        
        findViewById<View>(R.id.buttonViewSavedProjects).setOnClickListener {
            val intent = Intent(this, SavedProjectsActivity::class.java)
            startActivity(intent)
        }
        
        findViewById<View>(R.id.buttonViewSavedTileSamples).setOnClickListener {
            val intent = Intent(this, SavedTileSamplesActivity::class.java)
            startActivity(intent)
        }
        
        findViewById<View>(R.id.buttonViewSavedJobs).setOnClickListener {
            val intent = Intent(this, SavedSummariesActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun startMeasurement() {
        val intent = Intent(this, MeasureActivity::class.java)
        // MeasureActivity always runs in polygon surface area mode
        startActivity(intent)
    }
    
    private fun populateRecentMeasurements() {
        val recentContainer = binding.recentContainer
        recentContainer.removeAllViews()
        
        val measurements = MeasurementStore.getAll()
        
        if (measurements.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "No recent measurements"
                textSize = 16f
                setTextColor(resources.getColor(R.color.tv_text_secondary, null))
                gravity = android.view.Gravity.CENTER
                setPadding(0, 16, 0, 16)
            }
            recentContainer.addView(emptyText)
        } else {
            // Show most recent measurements first
            measurements.reversed().forEach { measurement ->
                val measurementText = TextView(this).apply {
                    text = formatMeasurement(measurement)
                    textSize = 16f
                    setTextColor(resources.getColor(R.color.tv_text_primary, null))
                    gravity = android.view.Gravity.CENTER
                    setPadding(0, 8, 0, 8)
                }
                recentContainer.addView(measurementText)
            }
        }
    }
    
    private fun formatMeasurement(measurement: MeasurementRecord): String {
        val date = Date(measurement.timestampMillis)
        val dateFormat = DateFormat.getDateFormat(this)
        val timeFormat = DateFormat.getTimeFormat(this)
        val formattedDate = dateFormat.format(date)
        val formattedTime = timeFormat.format(date)
        
        return "${measurement.areaSqFt} ft² • $formattedDate $formattedTime"
    }
}
