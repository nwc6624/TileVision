package com.tilevision.shared.haptics

/**
 * Provides haptic feedback functionality
 */
expect class Haptics {
    /**
     * Perform light haptic feedback
     */
    fun light()
    
    /**
     * Perform medium haptic feedback
     */
    fun medium()
    
    /**
     * Perform heavy haptic feedback
     */
    fun heavy()
    
    /**
     * Perform selection haptic feedback
     */
    fun selection()
    
    /**
     * Perform impact haptic feedback
     * @param style Impact style
     */
    fun impact(style: ImpactStyle)
    
    /**
     * Perform notification haptic feedback
     * @param type Notification type
     */
    fun notification(type: NotificationType)
    
    /**
     * Check if haptics are supported on this device
     */
    fun isSupported(): Boolean
    
    /**
     * Check if haptics are enabled in system settings
     */
    fun isEnabled(): Boolean
}

/**
 * Impact styles for haptic feedback
 */
enum class ImpactStyle {
    LIGHT,
    MEDIUM,
    HEAVY,
    RIGID,
    SOFT
}

/**
 * Notification types for haptic feedback
 */
enum class NotificationType {
    SUCCESS,
    WARNING,
    ERROR
}
