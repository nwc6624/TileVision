package com.tilevision.app.ui.ar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import android.view.SurfaceView
import android.view.ViewGroup

class ARPreviewActivity : ComponentActivity() {
    
    private var session: Session? = null
    private var surfaceView: SurfaceView? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ARPreviewScreen(
                        onBackPressed = { finish() }
                    )
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Resume AR session if needed
    }
    
    override fun onPause() {
        super.onPause()
        // Pause AR session if needed
    }
    
    override fun onDestroy() {
        super.onDestroy()
        session?.close()
    }
}

@Composable
fun ARPreviewScreen(
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // AR Preview Surface
        AndroidView(
            factory = { ctx ->
                SurfaceView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    // Configure AR surface view
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        
        // AR Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBackPressed,
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Back")
            }
            
            Button(
                onClick = { /* AR capture functionality */ },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Capture AR")
            }
        }
    }
} 