package com.tilevision.shared.platform

import android.content.Context
import com.tilevision.shared.ar.ArSessionManager
import com.tilevision.shared.data.LocalDataSource
import com.tilevision.shared.data.LocalDataSourceFactory
import com.tilevision.shared.haptics.Haptics
import com.tilevision.shared.io.FileExporter
import com.tilevision.shared.settings.SettingsDataSource
import com.tilevision.shared.settings.SettingsDataSourceFactory

actual object PlatformServices {
    // In a real implementation, you'd get the context from the application
    private var context: Context? = null
    
    fun initialize(context: Context) {
        this.context = context
        LocalDataSourceFactory.initialize(context)
        SettingsDataSourceFactory.initialize(context)
    }
    
    actual fun createArSessionManager(): ArSessionManager {
        return ArSessionManager(context ?: throw IllegalStateException("Context not initialized"))
    }
    
    actual fun createHaptics(): Haptics {
        return Haptics(context ?: throw IllegalStateException("Context not initialized"))
    }
    
    actual fun createFileExporter(): FileExporter {
        return FileExporter(context ?: throw IllegalStateException("Context not initialized"))
    }
    
    actual fun createLocalDataSource(): LocalDataSource {
        return LocalDataSourceFactory.create()
    }
    
    actual fun createSettingsDataSource(): SettingsDataSource {
        return SettingsDataSourceFactory.create()
    }
}
