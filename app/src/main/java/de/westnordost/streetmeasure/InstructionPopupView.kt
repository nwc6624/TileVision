package de.westnordost.streetmeasure

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView

class InstructionPopupView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var instructionText: TextView
    private var floatAnimator: ObjectAnimator? = null

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_instruction_popup, this, true)
        instructionText = view.findViewById(R.id.instructionText)
        
        // Make clickable for tap-to-dismiss
        isClickable = true
        isFocusable = true
        setOnClickListener {
            dismiss()
        }
    }

    fun setText(text: String) {
        instructionText.text = text
    }

    fun startFloatAnim() {
        stopFloatAnim()
        
        val density = resources.displayMetrics.density
        val offsetDp = 8f * density
        
        floatAnimator = ObjectAnimator.ofFloat(this, "translationY", -offsetDp, offsetDp).apply {
            duration = 3000L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }
        floatAnimator?.start()
    }

    fun stopFloatAnim() {
        floatAnimator?.cancel()
        floatAnimator = null
    }

    fun dismiss() {
        stopFloatAnim()
        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        animate()
            .alpha(0f)
            .setDuration(400)
            .withEndAction {
                visibility = View.GONE
            }
            .start()
    }

    fun fadeOut(callback: () -> Unit = {}) {
        stopFloatAnim()
        animate()
            .alpha(0f)
            .setDuration(500)
            .withEndAction {
                visibility = GONE
                callback()
            }
            .start()
    }
}
