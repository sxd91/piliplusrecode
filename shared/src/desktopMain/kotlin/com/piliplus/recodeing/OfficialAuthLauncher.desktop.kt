package com.piliplus.recodeing.core.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Desktop
import java.net.URI

@Composable
actual fun rememberUrlOpener(): UrlOpener = remember {
    UrlOpener { url ->
        runCatching {
            if (!Desktop.isDesktopSupported()) return@UrlOpener false
            val desktop = Desktop.getDesktop()
            if (!desktop.isSupported(Desktop.Action.BROWSE)) return@UrlOpener false
            desktop.browse(URI(url))
        }.isSuccess
    }
}
