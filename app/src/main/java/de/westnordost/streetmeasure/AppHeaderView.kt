package de.westnordost.streetmeasure

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class AppHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val titleText: TextView
    private val subtitleText: TextView
    private val actionButton: MaterialButton
    private val statusBarSpacer: View

    init {
        orientation = VERTICAL
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.view_app_header, this, true)

        titleText = findViewById(R.id.headerTitle)
        subtitleText = findViewById(R.id.headerSubtitle)
        actionButton = findViewById(R.id.headerActionButton)
        statusBarSpacer = findViewById(R.id.statusBarSpacer)

        // Handle system window insets to avoid status bar collision
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = statusBarSpacer.layoutParams as LayoutParams
            params.height = systemBars.top
            statusBarSpacer.layoutParams = params
            insets
        }
    }

    fun setTitle(text: String) {
        titleText.text = text
    }

    fun setSubtitle(text: String?) {
        if (!text.isNullOrBlank()) {
            subtitleText.text = text
            subtitleText.visibility = View.VISIBLE
        } else {
            subtitleText.visibility = View.GONE
        }
    }

    fun setModeHome(onSettingsClick: () -> Unit) {
        actionButton.setIconResource(R.drawable.ic_settings_24)
        actionButton.setOnClickListener { onSettingsClick() }
    }

    fun setModeBack(onBackClick: () -> Unit) {
        actionButton.setIconResource(R.drawable.ic_arrow_back_24)
        actionButton.setOnClickListener { onBackClick() }
    }
}
