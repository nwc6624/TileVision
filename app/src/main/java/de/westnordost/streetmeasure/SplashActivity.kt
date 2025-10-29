package de.westnordost.streetmeasure

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tilevision.prefs.StartupPrefs

class SplashActivity : AppCompatActivity() {

    private lateinit var gridBackground: GridBackgroundView
    private lateinit var logoImage: ImageView
    private lateinit var ripplePulse: View
    private lateinit var appTitle: TextView
    private lateinit var tagline: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("SplashActivity", "onCreate")
        
        // Apply theme before setting content view
        ThemeManager.applyTheme(ThemeManager.load(this))
        
        setContentView(R.layout.activity_splash)
        
        // Initialize views
        gridBackground = findViewById(R.id.gridBackground)
        logoImage = findViewById(R.id.logoImage)
        ripplePulse = findViewById(R.id.ripplePulse)
        appTitle = findViewById(R.id.appTitle)
        tagline = findViewById(R.id.tagline)
        
        // Start the animated background
        gridBackground.applyInitialEnabledState(this)
        gridBackground.setGridEnabled(this, true)
        
        // Start the animation sequence
        startAnimationSequence()
    }

    private fun startAnimationSequence() {
        // 1. Ripple burst (starts immediately)
        ripplePulse.animate()
            .alpha(1f)
            .scaleX(1.3f)
            .scaleY(1.3f)
            .setDuration(600)
            .withStartAction {
                ripplePulse.alpha = 0f
                ripplePulse.scaleX = 0.2f
                ripplePulse.scaleY = 0.2f
            }
            .withEndAction {
                // Fade ripple back out so it doesn't just sit there
                ripplePulse.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .start()
            }
            .start()

        // 2. Logo pop-in
        logoImage.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(800)
            .setInterpolator(OvershootInterpolator(1.2f))
            .withStartAction {
                logoImage.alpha = 0f
                logoImage.scaleX = 0.85f
                logoImage.scaleY = 0.85f
            }
            .start()

        // 3. Title fade-in after 800ms
        appTitle.postDelayed({
            appTitle.animate()
                .alpha(1f)
                .setDuration(600)
                .start()
        }, 800)

        // 4. Tagline fade-in after 1400ms
        tagline.postDelayed({
            tagline.animate()
                .alpha(1f)
                .setDuration(600)
                .start()
        }, 1400)

        // 5. Launch next activity after ~2500ms
        tagline.postDelayed({
            routeAfterSplash()
        }, 2500)
    }

    private fun routeAfterSplash() {
        val shouldShowDisclaimer = StartupPrefs.shouldShowDisclaimer(this)
        
        if (shouldShowDisclaimer) {
            startActivity(Intent(this, DisclaimerActivity::class.java))
        } else {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        // VERY IMPORTANT: kill Splash so we don't come back to it on Back
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Apply orientation policy
        com.tilevision.ui.ScreenOrientationHelper.applyOrientationPolicy(this)
    }

    override fun onPause() {
        super.onPause()
        Log.d("SplashActivity", "onPause")
        // Stop animations to prevent memory leaks
        gridBackground.stopAnimators()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("SplashActivity", "onDestroy")
    }
}
