package com.tilevision.ios

import androidx.compose.ui.window.ComposeUIViewController
import com.tilevision.shared.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
