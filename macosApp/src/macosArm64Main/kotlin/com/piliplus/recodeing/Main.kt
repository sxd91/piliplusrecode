package com.piliplus.recodeing

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AppKit.NSApplication
import platform.AppKit.NSApplicationActivationPolicy
import platform.AppKit.NSApplicationDelegateProtocol
import platform.CoreGraphics.CGSizeMake
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
fun main() {
    val application = NSApplication.sharedApplication()
    application.setActivationPolicy(
        NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular,
    )
    application.delegate =
        object : NSObject(), NSApplicationDelegateProtocol {
            override fun applicationShouldTerminateAfterLastWindowClosed(
                sender: NSApplication,
            ): Boolean = true
        }

    Window(
        title = "PiliPlus Recodeing",
        size = DpSize(1080.dp, 720.dp),
    ) {
        window.minSize = CGSizeMake(480.0, 640.0)
        App()
    }
    application.run()
}
