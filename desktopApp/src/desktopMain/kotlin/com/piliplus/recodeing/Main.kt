package com.piliplus.recodeing

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = WindowState(width = 1080.dp, height = 720.dp),
        title = "liquidreode",
    ) {
        LaunchedEffect(window) {
            window.minimumSize = java.awt.Dimension(720, 540)
        }
        App()
    }
}
