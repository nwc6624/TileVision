package com.tilevision.shared.measurement

import com.tilevision.shared.ar.ArSessionManager
import com.tilevision.shared.haptics.Haptics
import kotlinx.coroutines.CoroutineScope

actual object MeasureViewModelProvider {
    actual fun create(
        arSessionManager: ArSessionManager,
        haptics: Haptics,
        coroutineScope: CoroutineScope
    ): MeasureViewModel {
        return MeasureViewModel(arSessionManager, haptics, coroutineScope)
    }
}
