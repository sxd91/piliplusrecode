package com.piliplus.recodeing.core.design

import androidx.compose.animation.Animatable as ColorAnimatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.tanh

@Composable
fun LiquidButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    adaptiveLuminance: Boolean = false,
    tint: Color = Color.Unspecified,
    content: @Composable RowScope.() -> Unit,
) {
    val scope = rememberCoroutineScope()
    val pressProgress = remember { Animatable(0f) }
    val pointerOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val sampledLayer = rememberGraphicsLayer()
    val luminance = remember { Animatable(0.5f) }
    val contentColor = remember { ColorAnimatable(Color.White) }

    LaunchedEffect(sampledLayer, adaptiveLuminance) {
        if (!adaptiveLuminance) return@LaunchedEffect
        val pixels = IntArray(25)
        while (isActive) {
            val bitmap = runCatching { sampledLayer.toImageBitmap() }.getOrNull()
            if (bitmap != null && bitmap.width > 0 && bitmap.height > 0) {
                val thumbnail = bitmap.scaledTo(5, 5)
                thumbnail.readPixels(pixels)
                val average = pixels.sumOf { argb ->
                    val red = (argb shr 16 and 0xFF) / 255.0
                    val green = (argb shr 8 and 0xFF) / 255.0
                    val blue = (argb and 0xFF) / 255.0
                    0.2126 * red + 0.7152 * green + 0.0722 * blue
                }.toFloat() / pixels.size
                if (abs(average - luminance.targetValue) >= 0.03f) {
                    luminance.animateTo(average, spring(stiffness = 120f))
                }
                val targetContentColor = if (average > 0.55f) Color.Black else Color.White
                if (targetContentColor != contentColor.targetValue) {
                    contentColor.animateTo(targetContentColor, spring(stiffness = 180f))
                }
            }
            delay(PiliGlassDefaults.LuminanceSampleIntervalMillis)
        }
    }

    Row(
        modifier = modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(percent = 50) },
                effects = {
                    val adjustedLuminance = (luminance.value * 2f - 1f).let { sign(it) * it * it }
                    if (adaptiveLuminance) {
                        colorControls(
                            brightness = if (adjustedLuminance > 0f) {
                                lerp(0.08f, 0.38f, adjustedLuminance)
                            } else {
                                lerp(0.08f, -0.16f, -adjustedLuminance)
                            },
                            contrast = if (adjustedLuminance > 0f) lerp(1f, 0.35f, adjustedLuminance) else 1f,
                            saturation = 1.45f,
                        )
                    }
                    blur(4.dp.toPx())
                    lens(12.dp.toPx(), 24.dp.toPx(), depthEffect = true)
                },
                highlight = { Highlight.Default.copy(alpha = 0.65f + pressProgress.value * 0.35f) },
                layerBlock = {
                    val progress = pressProgress.value
                    val width = size.width.coerceAtLeast(1f)
                    val height = size.height.coerceAtLeast(1f)
                    val offset = pointerOffset.value
                    val maxOffset = size.minDimension.coerceAtLeast(1f)
                    translationX = maxOffset * tanh(0.05f * offset.x / maxOffset)
                    translationY = maxOffset * tanh(0.05f * offset.y / maxOffset)
                    val angle = atan2(offset.y, offset.x)
                    val maxDimension = size.maxDimension.coerceAtLeast(1f)
                    val baseScale = lerp(1f, 1f + 4.dp.toPx() / height, progress)
                    val dragScale = 4.dp.toPx() / height
                    scaleX = baseScale + dragScale * abs(cos(angle) * offset.x / maxDimension) *
                        (width / height).fastCoerceAtMost(1f)
                    scaleY = baseScale + dragScale * abs(sin(angle) * offset.y / maxDimension) *
                        (height / width).fastCoerceAtMost(1f)
                },
                onDrawBackdrop = { drawBackdrop ->
                    drawBackdrop()
                    if (adaptiveLuminance) sampledLayer.record { drawBackdrop() }
                },
                onDrawSurface = {
                    if (tint != Color.Unspecified) {
                        drawRect(tint, blendMode = BlendMode.Hue)
                        drawRect(tint.copy(alpha = 0.64f))
                    } else {
                        drawRect(Color.White.copy(alpha = 0.08f))
                    }
                },
            )
            .clickable(
                enabled = enabled,
                interactionSource = null,
                indication = if (enabled) null else LocalIndication.current,
                role = Role.Button,
                onClick = onClick,
            )
            .pointerInput(enabled, scope) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    val origin = down.position
                    var pointerUpdateJob: Job? = null
                    scope.launch { pressProgress.animateTo(1f, spring(0.5f, 300f)) }
                    scope.launch { pointerOffset.snapTo(Offset.Zero) }
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id }
                        change?.let { pointerChange ->
                            pointerUpdateJob?.cancel()
                            pointerUpdateJob = scope.launch {
                                pointerOffset.snapTo(pointerChange.position - origin)
                            }
                        }
                    } while (change?.pressed == true)
                    pointerUpdateJob?.cancel()
                    scope.launch { pressProgress.animateTo(0f, spring(0.5f, 300f)) }
                    scope.launch { pointerOffset.animateTo(Offset.Zero, spring(0.5f, 300f)) }
                }
            }
            .height(48.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            top.yukonga.miuix.kmp.theme.LocalContentColor provides contentColor.value,
        ) {
            content()
        }
    }
}

private fun androidx.compose.ui.graphics.ImageBitmap.scaledTo(width: Int, height: Int): androidx.compose.ui.graphics.ImageBitmap {
    val target = androidx.compose.ui.graphics.ImageBitmap(width, height)
    val canvas = androidx.compose.ui.graphics.Canvas(target)
    canvas.drawImageRect(
        image = this,
        srcOffset = androidx.compose.ui.unit.IntOffset.Zero,
        srcSize = androidx.compose.ui.unit.IntSize(this.width, this.height),
        dstOffset = androidx.compose.ui.unit.IntOffset.Zero,
        dstSize = androidx.compose.ui.unit.IntSize(width, height),
        paint = androidx.compose.ui.graphics.Paint(),
    )
    return target
}
