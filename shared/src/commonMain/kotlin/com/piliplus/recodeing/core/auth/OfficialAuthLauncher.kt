package com.piliplus.recodeing.core.auth

import androidx.compose.runtime.Composable

fun interface UrlOpener {
    fun open(url: String): Boolean
}

@Composable
expect fun rememberUrlOpener(): UrlOpener
