package com.piliplus.recodeing.core.design

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

@Composable
fun GlassBackButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
) {
    val layoutDirection = LocalLayoutDirection.current
    LiquidButton(
        onClick = onClick,
        backdrop = backdrop,
        modifier = modifier.size(44.dp),
        compact = true,
        tint = Color.White.copy(alpha = 0.10f),
    ) {
        Icon(
            imageVector = MiuixIcons.Back,
            contentDescription = "返回",
            modifier = Modifier.graphicsLayer {
                if (layoutDirection == LayoutDirection.Rtl) scaleX = -1f
            },
        )
    }
}
