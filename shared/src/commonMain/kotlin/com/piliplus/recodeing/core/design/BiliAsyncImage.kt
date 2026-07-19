package com.piliplus.recodeing.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

fun normalizeBiliImageUrl(url: String?): String? {
    val normalized = url?.trim().orEmpty()
    if (normalized.isBlank()) return null
    return when {
        normalized.startsWith("//") -> "https:$normalized"
        normalized.startsWith("http://") -> "https://${normalized.removePrefix("http://")}"
        normalized.startsWith("https://") -> normalized
        else -> normalized
    }
}

@Composable
fun BiliAsyncImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val model = normalizeBiliImageUrl(url)
    SubcomposeAsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier.background(MiuixTheme.colorScheme.surfaceContainer),
        contentScale = contentScale,
        loading = {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        },
        success = { SubcomposeAsyncImageContent() },
        error = {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("图片加载失败", color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            }
        },
    )
}
