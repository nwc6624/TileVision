package com.tilevision.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tilevision.shared.App
import com.tilevision.shared.platform.PlatformServices

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize platform services
        PlatformServices.initialize(this)
        
        setContent {
            App()
        }
    }
}
