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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(ThemeManager.load(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        setupToolbar()
        
        // Setup grid background
        val gridBackground = findViewById<GridBackgroundView>(R.id.gridBackground)
        gridBackground?.setEnabledState(GridBackgroundView.isEnabled(this), saveToPreferences = false)
        
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
            GridBackgroundView.setEnabledState(this, isChecked)
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
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
