package com.tilevision.shared

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tilevision.shared.navigation.AppNavigation
import com.tilevision.shared.theme.TileVisionTheme
@Composable
fun App() {
    TileVisionTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavigation()
        }
    }
}
