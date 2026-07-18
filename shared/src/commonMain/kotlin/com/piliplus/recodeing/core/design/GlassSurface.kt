package com.piliplus.recodeing.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawRect
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun GlassSurface(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(28.dp),
    surfaceColor: Color = MiuixTheme.colorScheme.surface.copy(alpha = 0.42f),
    content: @Composable BoxScope.() -> Unit,
) {
    val glassModifier = if (enabled) {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                vibrancy()
                blur(4.dp.toPx())
                lens(16.dp.toPx(), 32.dp.toPx())
            },
            onDrawSurface = { drawRect(surfaceColor) },
        )
    } else {
        Modifier.background(MiuixTheme.colorScheme.surfaceContainer)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .then(glassModifier),
        content = content,
    )
}
