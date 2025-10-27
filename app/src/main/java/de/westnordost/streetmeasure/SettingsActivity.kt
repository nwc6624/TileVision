package de.westnordost.streetmeasure

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var gridBackground: GridBackgroundView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(ThemeManager.load(this))
        super.onCreate(savedInstanceState)
        
        android.util.Log.d("TileVisionLifecycle", "onCreate SettingsActivity starting")
        
        // Inflate the shared page shell
        setContentView(R.layout.layout_page_shell)
        
        // Get references to shell elements
        val pageContentContainer = findViewById<android.widget.FrameLayout>(R.id.pageContentContainer)
        gridBackground = findViewById(R.id.gridBackground)
        
        // Inflate the activity's own content layout into the shell's container
        try {
            layoutInflater.inflate(R.layout.activity_settings, pageContentContainer, true)
            android.util.Log.d("TileVisionLifecycle", "SettingsActivity shell + content inflated ok")
        } catch (e: Exception) {
            android.util.Log.e("TileVisionLifecycle", "inflate failed in SettingsActivity", e)
        }
        
        setupToolbar()
        
        setupSettingsRows()
        setupVersionText()
    }
    
    private fun setupToolbar() {
        val headerView = findViewById<AppHeaderView>(R.id.appHeader)
        headerView?.apply {
            setTitle("Settings")
            setModeBack { finish() }
        }
    }
    
    private fun setupSettingsRows() {
        // Theme row
        findViewById<android.view.View>(R.id.privacyPolicyRow)?.setOnClickListener {
            showThemeDialog()
        }
        
        // Grid Background Toggle Switch
        val gridSwitch = findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.gridBackgroundSwitch)
        gridSwitch?.isChecked = GridBackgroundView.isEnabled(this)
        gridSwitch?.setOnCheckedChangeListener { _, isChecked ->
            gridBackground.setGridEnabled(this@SettingsActivity, isChecked)
        }
        
        // Delete All Data row (using privacyPolicyRow ID for now, will update)
        findViewById<android.view.View>(R.id.deleteAllDataRow)?.setOnClickListener {
            showDeleteAllDataDialog()
        }
        
        // Contact Support row
        findViewById<android.view.View>(R.id.contactSupportRow)?.setOnClickListener {
            openContactSupport()
        }
    }
    
    private fun setupVersionText() {
        val versionText = findViewById<TextView>(R.id.versionText)
        val versionName = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            "1.0.0"
        }
        versionText?.text = "App Version: $versionName"
    }
    
    private fun showThemeDialog() {
        val themes = arrayOf("Light", "Dark", "Follow System")
        val currentTheme = ThemeManager.load(this)
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Choose Theme")
            .setSingleChoiceItems(themes, currentTheme.ordinal) { dialog, which ->
                val newTheme = AppTheme.values()[which]
                ThemeManager.setTheme(this, newTheme)
                ThemeManager.applyTheme(newTheme)
                dialog.dismiss()
                recreate()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun showDeleteAllDataDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete All Data")
            .setMessage("This will permanently delete all saved projects, tile samples, and job summaries. This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteAllData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteAllData() {
        ProjectRepository.clear()
        TileSampleRepository.clear()
        ProjectSummaryRepository.clear()
        
        Toast.makeText(this, "All saved data deleted", Toast.LENGTH_SHORT).show()
    }
    
    private fun openContactSupport() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:support@tilevision.example")
            putExtra(Intent.EXTRA_SUBJECT, "TileVision Support")
        }
        try {
            startActivity(emailIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Email client not available", Toast.LENGTH_SHORT).show()
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
        // Sync switch state
        val gridSwitch = findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.gridBackgroundSwitch)
        gridSwitch?.isChecked = GridBackgroundView.isEnabled(this)
    }
    
    override fun onPause() {
        super.onPause()
        if (::gridBackground.isInitialized && gridBackground != null) {
            gridBackground.stopAnimators()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
