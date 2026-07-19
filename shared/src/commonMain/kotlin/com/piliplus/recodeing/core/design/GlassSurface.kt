package com.piliplus.recodeing.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun GlassSurface(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cornerRadius: Dp = PiliGlassDefaults.CornerRadius,
    insideMargin: PaddingValues = PiliGlassDefaults.InsideMargin,
    surfaceColor: Color = MiuixTheme.colorScheme.surface.copy(alpha = PiliGlassDefaults.SurfaceAlpha),
    contentColor: Color = MiuixTheme.colorScheme.onSurface,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val fallbackColor = MiuixTheme.colorScheme.surfaceContainer
    val glassModifier = if (enabled && platformSupportsLiquidGlass()) {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                vibrancy()
                blur(PiliGlassDefaults.BlurRadius.toPx())
                lens(
                    PiliGlassDefaults.RefractionHeight.toPx(),
                    PiliGlassDefaults.RefractionAmount.toPx(),
                )
            },
            onDrawSurface = { drawRect(surfaceColor) },
        )
    } else {
        Modifier.background(fallbackColor)
    }

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Column(
            modifier = modifier
                .semantics { isTraversalGroup = true }
                .clip(shape)
                .then(glassModifier)
                .padding(insideMargin),
            content = content,
        )
    }
}

object PiliGlassDefaults {
    val CornerRadius = 20.dp
    val NavigationCornerRadius = 28.dp
    val InsideMargin = PaddingValues(0.dp)
    val BlurRadius = 4.dp
    val RefractionHeight = 16.dp
    val RefractionAmount = 32.dp
    const val SurfaceAlpha = 0.46f
    const val LuminanceSampleIntervalMillis = 750L
}
