package com.piliplus.recodeing

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() {
    DebugLog.install()
    DebugLog.info("Desktop main entered")
    application {
        Window(
            onCloseRequest = ::exitApplication,
            state = WindowState(width = 1080.dp, height = 720.dp),
            title = "reliqliquid-debug",
            undecorated = false,
            transparent = false,
        ) {
            window.background = java.awt.Color(247, 247, 247)
            LaunchedEffect(window) {
                window.minimumSize = java.awt.Dimension(720, 540)
                DebugLog.info("Desktop window shown; size=${window.width}x${window.height}")
            }
            App()
        }
    }
}
