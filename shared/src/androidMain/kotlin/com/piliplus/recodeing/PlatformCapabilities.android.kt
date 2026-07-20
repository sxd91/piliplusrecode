package com.piliplus.recodeing.core.design

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap

actual fun platformSupportsLiquidGlass(): Boolean = android.os.Build.VERSION.SDK_INT >= 31

actual fun sampleImageLuminance(image: ImageBitmap): Float? {
    val bitmap = image.asAndroidBitmap()
    val softwareBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
        bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: return null
    } else {
        bitmap
    }
    return try {
        val width = softwareBitmap.width
        val height = softwareBitmap.height
        if (width <= 0 || height <= 0) return null
        var luminance = 0.0
        var samples = 0
        for (yIndex in 0 until 5) {
            val y = ((yIndex + 0.5f) * height / 5f).toInt().coerceIn(0, height - 1)
            for (xIndex in 0 until 5) {
                val x = ((xIndex + 0.5f) * width / 5f).toInt().coerceIn(0, width - 1)
                val color = softwareBitmap.getPixel(x, y)
                val red = (color shr 16 and 0xFF) / 255.0
                val green = (color shr 8 and 0xFF) / 255.0
                val blue = (color and 0xFF) / 255.0
                luminance += 0.2126 * red + 0.7152 * green + 0.0722 * blue
                samples++
            }
        }
        (luminance / samples).toFloat()
    } finally {
        if (softwareBitmap !== bitmap) softwareBitmap.recycle()
    }
}
