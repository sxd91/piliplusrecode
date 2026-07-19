package com.piliplus.recodeing.ui.video

import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.piliplus.recodeing.core.model.DashTrack
import com.piliplus.recodeing.core.model.VideoPlayUrl

@OptIn(UnstableApi::class)
@Composable
actual fun PlatformVideoPlayer(
    playUrl: VideoPlayUrl,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val directUrl = playUrl.durl.firstOrNull()?.url
    val videoTrack = playUrl.dash?.video?.maxByOrNull(DashTrack::bandwidth)
    val audioTrack = playUrl.dash?.audio?.maxByOrNull(DashTrack::bandwidth)
    val playerKey = directUrl ?: videoTrack?.baseUrl ?: return
    val player = remember(playerKey, audioTrack?.baseUrl) {
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 liquidreode")
            .setDefaultRequestProperties(mapOf("Referer" to "https://www.bilibili.com/"))
        ExoPlayer.Builder(context).build().apply {
            if (directUrl != null) {
                setMediaItem(MediaItem.fromUri(directUrl))
            } else if (videoTrack != null) {
                val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(videoTrack.toMediaItem(MimeTypes.VIDEO_MP4))
                val source = if (audioTrack != null) {
                    val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(audioTrack.toMediaItem(MimeTypes.AUDIO_MP4))
                    MergingMediaSource(videoSource, audioSource)
                } else {
                    videoSource
                }
                setMediaSource(source)
            }
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(player, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> player.play()
                Lifecycle.Event.ON_STOP -> player.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.release()
        }
    }
    AndroidView(
        factory = { viewContext ->
            PlayerView(viewContext).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                useController = true
                this.player = player
            }
        },
        update = { it.player = player },
        modifier = modifier,
    )
}

private fun DashTrack.toMediaItem(mimeType: String): MediaItem = MediaItem.Builder()
    .setUri(baseUrl)
    .setMimeType(mimeType)
    .build()
