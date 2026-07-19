package com.piliplus.recodeing.core.design

import androidx.compose.runtime.staticCompositionLocalOf

expect fun platformSupportsLiquidGlass(): Boolean

val LocalLiquidGlassEnabled = staticCompositionLocalOf { false }
