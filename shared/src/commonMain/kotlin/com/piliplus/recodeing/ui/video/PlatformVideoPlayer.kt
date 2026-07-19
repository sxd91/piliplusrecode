package com.piliplus.recodeing.ui.video

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.piliplus.recodeing.core.model.VideoPlayUrl

@Composable
expect fun PlatformVideoPlayer(
    playUrl: VideoPlayUrl,
    modifier: Modifier = Modifier,
)
