package com.tilevision.shared.settings

actual object SettingsDataSourceFactory {
    actual fun create(): SettingsDataSource {
        return IosSettingsDataSource()
    }
}
