package com.piliplus.recodeing.ui.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.piliplus.recodeing.core.model.VideoPlayUrl
import top.yukonga.miuix.kmp.basic.Text

@Composable
actual fun PlatformVideoPlayer(
    playUrl: VideoPlayUrl,
    modifier: Modifier,
) {
    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Desktop 播放器需要系统安装 mpv；播放地址已在应用内解析。",
            color = Color.White,
            modifier = Modifier.padding(24.dp),
        )
    }
}
