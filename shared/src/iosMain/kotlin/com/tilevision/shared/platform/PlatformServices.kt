package com.tilevision.shared.platform

import com.tilevision.shared.ar.ArSessionManager
import com.tilevision.shared.haptics.Haptics

actual object PlatformServices {
    actual fun createArSessionManager(): ArSessionManager {
        return ArSessionManager()
    }
    
    actual fun createHaptics(): Haptics {
        return Haptics()
    }
}
