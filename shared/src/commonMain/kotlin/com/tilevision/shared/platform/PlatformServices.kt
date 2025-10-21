package com.tilevision.shared.platform

import com.tilevision.shared.ar.ArSessionManager
import com.tilevision.shared.haptics.Haptics

/**
 * Platform-specific service factory
 * In a real app, this would be handled by a DI framework
 */
expect object PlatformServices {
    fun createArSessionManager(): ArSessionManager
    fun createHaptics(): Haptics
}
