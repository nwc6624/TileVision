package com.tilevision.shared.data

actual object LocalDataSourceFactory {
    actual fun create(): LocalDataSource {
        return IosLocalDataSource()
    }
}
