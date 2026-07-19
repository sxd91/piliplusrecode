package com.piliplus.recodeing.core.design

actual fun platformSupportsLiquidGlass(): Boolean {
    val sdk = android.os.Build.VERSION.SDK_INT
    val xiaomiAndroid16 = sdk >= 36 && android.os.Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)
    return sdk >= 31 && !xiaomiAndroid16
}
