package com.tilevision.shared.data

/**
 * Factory for creating LocalDataSource instances
 */
expect object LocalDataSourceFactory {
    fun create(): LocalDataSource
}
