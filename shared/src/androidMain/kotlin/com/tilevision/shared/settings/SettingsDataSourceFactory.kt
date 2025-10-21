package com.tilevision.shared.settings

import android.content.Context

actual object SettingsDataSourceFactory {
    private var context: Context? = null
    
    fun initialize(context: Context) {
        this.context = context
    }
    
    actual fun create(): SettingsDataSource {
        return AndroidSettingsDataSource(
            context ?: throw IllegalStateException("Context not initialized")
        )
    }
}
