package com.tilevision.shared.data

import android.content.Context

actual object LocalDataSourceFactory {
    private var context: Context? = null
    
    fun initialize(context: Context) {
        this.context = context
    }
    
    actual fun create(): LocalDataSource {
        return AndroidLocalDataSource(
            context ?: throw IllegalStateException("Context not initialized")
        )
    }
}
