package com.piliplus.recodeing.core.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AppKit.NSWorkspace
import platform.Foundation.NSURL

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberUrlOpener(): UrlOpener = remember {
    UrlOpener { url ->
        val nsUrl = NSURL.URLWithString(url) ?: return@UrlOpener false
        NSWorkspace.sharedWorkspace.openURL(nsUrl)
    }
}
