package com.tilevision.shared.settings

/**
 * Factory for creating SettingsDataSource instances
 */
expect object SettingsDataSourceFactory {
    fun create(): SettingsDataSource
}
