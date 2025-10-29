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

class SettingsActivity : BaseFramedActivity() {
    
    override fun getContentLayoutResId(): Int = R.layout.activity_settings
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Theme must be applied before setContentView, but BaseFramedActivity already did that
        // So we need to recreate if theme changed
        val savedTheme = ThemeManager.load(this)
        ThemeManager.applyTheme(savedTheme)
        
        android.util.Log.d("TileVisionLifecycle", "onCreate SettingsActivity starting")
        
        // BaseFramedActivity already inflated layout_page_shell and activity_settings
        
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
        findViewById<android.view.View>(R.id.themeRow)?.setOnClickListener {
            showThemeDialog()
        }
        
        // Grid Background Toggle Switch
        val gridSwitch = findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.gridBackgroundSwitch)
        gridSwitch?.isChecked = GridBackgroundView.isEnabled(this)
        gridSwitch?.setOnCheckedChangeListener { _, isChecked ->
            gridBackground?.setGridEnabled(this@SettingsActivity, isChecked)
        }
        
        // Disclaimer Toggle Switch
        val disclaimerToggle = findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchShowDisclaimer)
        disclaimerToggle?.isChecked = com.tilevision.prefs.StartupPrefs.shouldShowDisclaimer(this)
        disclaimerToggle?.setOnCheckedChangeListener { _, isChecked ->
            com.tilevision.prefs.StartupPrefs.setShouldShowDisclaimer(this, isChecked)
        }
        
        // Units System row
        findViewById<android.view.View>(R.id.unitsRow)?.setOnClickListener {
            showUnitsDialog()
        }
        
        // Portrait Lock Toggle Switch
        val portraitSwitch = findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchPortraitLock)
        portraitSwitch?.isChecked = com.tilevision.prefs.OrientationPrefs.isPortraitLocked(this)
        portraitSwitch?.setOnCheckedChangeListener { _, isChecked ->
            com.tilevision.prefs.OrientationPrefs.setPortraitLocked(this, isChecked)
            // Immediately apply to SettingsActivity itself:
            com.tilevision.ui.ScreenOrientationHelper.applyOrientationPolicy(this)
        }
        
        // Monochrome Mode Toggle Switch
        val monochromeSwitch = findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchMonochromeMode)
        monochromeSwitch?.isChecked = com.tilevision.prefs.AppearancePrefs.isMonochrome(this)
        monochromeSwitch?.setOnCheckedChangeListener { _, isChecked ->
            com.tilevision.prefs.AppearancePrefs.setMonochrome(this, isChecked)
            // Trigger UI refresh so user sees it immediately
            recreate()
        }
        
        // Privacy Policy row
        findViewById<android.view.View>(R.id.rowPrivacyPolicy)?.setOnClickListener {
            showPrivacyPolicyDialog()
        }
        
        // Terms of Use row
        findViewById<android.view.View>(R.id.rowTerms)?.setOnClickListener {
            showTermsOfUseDialog()
        }
        
        // Contact Support row
        findViewById<android.view.View>(R.id.rowSupport)?.setOnClickListener {
            openContactSupport()
        }
        
        // Delete All Data row
        findViewById<android.view.View>(R.id.rowDeleteData)?.setOnClickListener {
            showDeleteAllDataDialog()
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
    
    private fun showUnitsDialog() {
        val units = arrayOf("Imperial (ft² / in)", "Metric (m² / cm)")
        val currentUnits = com.tilevision.prefs.UnitsPrefs.getUnits(this)
        val currentIndex = if (currentUnits == "imperial") 0 else 1
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Choose Units")
            .setSingleChoiceItems(units, currentIndex) { dialog, which ->
                val newUnits = if (which == 0) "imperial" else "metric"
                com.tilevision.prefs.UnitsPrefs.setUnits(this, newUnits)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showPrivacyPolicyDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Privacy Policy")
            .setMessage("We use your camera only to measure surfaces locally on your device. Measurements are stored locally on this phone. No cloud upload.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showTermsOfUseDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Terms of Use")
            .setMessage("This app provides estimates only. Always verify measurements before purchasing materials.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showDeleteAllDataDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete all saved jobs and tile samples?")
            .setMessage("This will remove all saved measurements from this device. This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteAllData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteAllData() {
        ProjectSummaryRepository.clear()
        TileSampleRepository.clear()
        ProjectRepository.clear()
        MeasurementStore.clear()
        
        Toast.makeText(this, "All saved data deleted", Toast.LENGTH_SHORT).show()
    }
    
    private fun openContactSupport() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@tilevision.example"))
            putExtra(Intent.EXTRA_SUBJECT, "TileVision AR Support")
        }
        startActivity(Intent.createChooser(intent, "Contact Support"))
    }
    
    override fun onResume() {
        super.onResume()
        // Sync switch state
        val gridSwitch = findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.gridBackgroundSwitch)
        gridSwitch?.isChecked = GridBackgroundView.isEnabled(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
