package de.westnordost.streetmeasure

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import de.westnordost.streetmeasure.R

abstract class BaseFramedActivity : AppCompatActivity() {

    protected var gridBackground: GridBackgroundView? = null

    // Child screens MUST say which layout they want injected into the frame
    abstract fun getContentLayoutResId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("BaseFramedActivity", "onCreate ${this::class.simpleName}")

        // 1. Always inflate the shared shell that draws the teal border + animated background
        setContentView(R.layout.layout_page_shell)

        // 2. Get the shell views
        val pageContainer = findViewById<FrameLayout>(R.id.pageContentContainer)
        gridBackground = findViewById(R.id.gridBackground)

        // 3. Inflate the screen-specific content into the container INSIDE the border frame
        val innerLayout = getContentLayoutResId()
        try {
            layoutInflater.inflate(innerLayout, pageContainer, true)
            Log.d("BaseFramedActivity", "Inflated content layout $innerLayout")
        } catch (e: Exception) {
            Log.e("BaseFramedActivity", "Failed to inflate content layout $innerLayout", e)
        }

        // AFTER this call, subclasses can safely call findViewById()
        // on their normal views in their own onCreate().
    }

    override fun onResume() {
        super.onResume()
        
        // Apply orientation policy first
        com.tilevision.ui.ScreenOrientationHelper.applyOrientationPolicy(this)
        
        // Start / sync animated background
        val gb = gridBackground
        if (gb != null) {
            gb.applyInitialEnabledState(this)

            val enabled = GridBackgroundView.isEnabled(this)
            gb.setGridEnabled(this, enabled)
        }
    }

    override fun onPause() {
        super.onPause()
        // Stop animation to avoid crashes when backgrounded
        gridBackground?.stopAnimators()
    }
}
