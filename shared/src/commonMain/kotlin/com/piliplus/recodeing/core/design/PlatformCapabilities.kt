package com.piliplus.recodeing.core.design

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap

expect fun platformSupportsLiquidGlass(): Boolean

expect fun sampleImageLuminance(image: ImageBitmap): Float?

val LocalLiquidGlassEnabled = staticCompositionLocalOf { false }
