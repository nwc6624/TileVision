package de.westnordost.streetmeasure

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import com.tilevision.prefs.StartupPrefs

class DisclaimerActivity : BaseFramedActivity() {
    
    override fun getContentLayoutResId(): Int = R.layout.activity_disclaimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("DisclaimerActivity", "onCreate")
        
        val checkbox = findViewById<CheckBox>(R.id.checkboxShowEachTime)
        val continueBtn = findViewById<View>(R.id.buttonContinue)
        val learnMoreBtn = findViewById<View>(R.id.buttonLearnMore)

        // Initialize checkbox to current pref value
        // If StartupPrefs.shouldShowDisclaimer() returns true,
        // that means we ARE currently showing it on launch.
        // So default the checkbox to "checked".
        checkbox.isChecked = StartupPrefs.shouldShowDisclaimer(this)

        continueBtn.setOnClickListener {
            // Save user's preference based on checkbox state:
            StartupPrefs.setShowDisclaimer(this, checkbox.isChecked)

            startActivity(Intent(this, HomeActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        learnMoreBtn.setOnClickListener {
            // launch SettingsActivity, scrolled/highlighted to Legal & Support/Terms section
            val intent = Intent(this, SettingsActivity::class.java)
            // optionally putExtra("highlightLegal", true)
            startActivity(intent)
        }
        
        // Animate the card in
        val card = findViewById<View>(R.id.disclaimerCard)
        card.scaleX = 0.95f
        card.scaleY = 0.95f
        card.alpha = 0f
        card.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(400)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }
}
