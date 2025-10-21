package com.tilevision.shared.platform

import com.tilevision.shared.ar.ArSessionManager
import com.tilevision.shared.data.LocalDataSource
import com.tilevision.shared.data.LocalDataSourceFactory
import com.tilevision.shared.haptics.Haptics
import com.tilevision.shared.io.FileExporter
import com.tilevision.shared.settings.SettingsDataSource
import com.tilevision.shared.settings.SettingsDataSourceFactory

actual object PlatformServices {
    actual fun createArSessionManager(): ArSessionManager {
        return ArSessionManager()
    }
    
    actual fun createHaptics(): Haptics {
        return Haptics()
    }
    
    actual fun createFileExporter(): FileExporter {
        return FileExporter()
    }
    
    actual fun createLocalDataSource(): LocalDataSource {
        return LocalDataSourceFactory.create()
    }
    
    actual fun createSettingsDataSource(): SettingsDataSource {
        return SettingsDataSourceFactory.create()
    }
}
