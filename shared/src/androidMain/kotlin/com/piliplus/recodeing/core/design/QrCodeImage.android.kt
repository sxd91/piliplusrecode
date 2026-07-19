package com.piliplus.recodeing.core.design

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

@Composable
actual fun QrCodeImage(
    content: String,
    modifier: Modifier,
) {
    val bitmap = remember(content) { createQrCodeBitmap(content) }
    Image(bitmap = bitmap.asImageBitmap(), contentDescription = "登录二维码", modifier = modifier)
}

private fun createQrCodeBitmap(content: String): Bitmap {
    val matrix = QRCodeWriter().encode(
        content,
        BarcodeFormat.QR_CODE,
        QrSize,
        QrSize,
        mapOf(EncodeHintType.MARGIN to 1),
    )
    val pixels = IntArray(QrSize * QrSize) { index ->
        val x = index % QrSize
        val y = index / QrSize
        if (matrix[x, y]) Color.BLACK else Color.WHITE
    }
    return Bitmap.createBitmap(QrSize, QrSize, Bitmap.Config.ARGB_8888).apply {
        setPixels(pixels, 0, QrSize, 0, 0, QrSize, QrSize)
    }
}

private const val QrSize = 512
