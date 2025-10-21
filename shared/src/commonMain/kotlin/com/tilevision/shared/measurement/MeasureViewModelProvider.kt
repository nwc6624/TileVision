package com.tilevision.shared.measurement

import com.tilevision.shared.ar.ArSessionManager
import com.tilevision.shared.haptics.Haptics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Provider for creating MeasureViewModel instances
 * In a real app, this would be handled by a DI framework like Koin or Hilt
 */
expect object MeasureViewModelProvider {
    fun create(
        arSessionManager: ArSessionManager,
        haptics: Haptics,
        coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
    ): MeasureViewModel
}
