package com.piliplus.recodeing.core.design

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

@Composable
actual fun QrCodeImage(
    content: String,
    modifier: Modifier,
) {
    val matrix = remember(content) {
        QRCodeWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            QrModules,
            QrModules,
            mapOf(EncodeHintType.MARGIN to 1),
        )
    }
    Canvas(modifier) {
        drawRect(Color.White)
        val cellWidth = size.width / matrix.width
        val cellHeight = size.height / matrix.height
        repeat(matrix.height) { y ->
            repeat(matrix.width) { x ->
                if (matrix[x, y]) drawRect(
                    color = Color.Black,
                    topLeft = Offset(x * cellWidth, y * cellHeight),
                    size = Size(cellWidth, cellHeight),
                )
            }
        }
    }
}

private const val QrModules = 256
