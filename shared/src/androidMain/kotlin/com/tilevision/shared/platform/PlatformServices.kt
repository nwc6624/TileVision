package com.tilevision.shared.platform

import android.content.Context
import com.tilevision.shared.ar.ArSessionManager
import com.tilevision.shared.haptics.Haptics

actual object PlatformServices {
    // In a real implementation, you'd get the context from the application
    private var context: Context? = null
    
    fun initialize(context: Context) {
        this.context = context
    }
    
    actual fun createArSessionManager(): ArSessionManager {
        return ArSessionManager(context ?: throw IllegalStateException("Context not initialized"))
    }
    
    actual fun createHaptics(): Haptics {
        return Haptics(context ?: throw IllegalStateException("Context not initialized"))
    }
}
