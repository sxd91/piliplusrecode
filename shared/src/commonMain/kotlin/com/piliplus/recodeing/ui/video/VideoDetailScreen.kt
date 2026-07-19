package com.piliplus.recodeing.ui.video

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.piliplus.recodeing.core.model.RecommendItem
import com.piliplus.recodeing.core.model.VideoDetail
import com.piliplus.recodeing.core.model.VideoPlayUrl
import com.piliplus.recodeing.core.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun VideoDetailScreen(
    bvid: String,
    onBack: () -> Unit,
    onPlay: (VideoPlayUrl) -> Unit,
    viewModel: VideoDetailViewModel = viewModel(key = bvid) { VideoDetailViewModel(bvid) },
) {
    val state by viewModel.uiState.collectAsState()
    VideoDetailContent(
        state = state,
        onBack = onBack,
        onPlay = onPlay,
        onVideoSelected = onVideoSelected,
        onSelectPage = viewModel::selectPage,
    )
}

@Composable
private fun VideoDetailContent(
    state: VideoDetailUiState,
    onBack: () -> Unit,
    onPlay: (VideoPlayUrl) -> Unit,
    onVideoSelected: (String) -> Unit,
    onSelectPage: (Long) -> Unit,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                BasicComponent(title = "返回", onClick = onBack)
            }
            state.error?.let { errorMessage ->
                item {
                    Card(Modifier.fillMaxWidth(), insideMargin = PaddingValues(18.dp)) {
                        Text(errorMessage, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                    }
                }
            }
            if (state.isLoading && state.detail == null) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                    }
                }
            }
            state.detail?.let { detail ->
                item {
                    DetailHeader(
                        detail = detail,
                        playUrl = state.playUrl,
                        onPlay = onPlay,
                    )
                }
                if (detail.pages.size > 1) {
                    item { SmallTitle("分P与合集") }
                    items(detail.pages) { page ->
                        BasicComponent(
                            title = page.part.ifBlank { "第 ${page.page} P" },
                            summary = formatDuration(page.duration),
                            onClick = { onSelectPage(page.cid) },
                        )
                    }
                }
                item { SmallTitle("简介") }
                item {
                    Card(Modifier.fillMaxWidth(), insideMargin = PaddingValues(18.dp)) {
                        Text(detail.desc.ifBlank { "暂无简介" })
                    }
                }
            }
            if (state.related.isNotEmpty()) {
                item { SmallTitle("相关推荐") }
                items(state.related) { item ->
                    BasicComponent(
                        title = item.title,
                        summary = item.owner?.name ?: "",
                        onClick = { item.bvid?.let(onVideoSelected) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailHeader(
    detail: VideoDetail,
    playUrl: VideoPlayUrl?,
    onPlay: (VideoPlayUrl) -> Unit,
) {
    Card(Modifier.fillMaxWidth(), insideMargin = PaddingValues(20.dp)) {
        Text(detail.title, style = MiuixTheme.textStyles.title2)
        Text(
            text = buildString {
                append(detail.owner?.name ?: "未知 UP 主")
                detail.stat?.view?.let { append(" · 播放 ").append(it) }
                detail.stat?.danmaku?.let { append(" · 弹幕 ").append(it) }
            },
            modifier = Modifier.padding(top = 8.dp),
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Row(
            modifier = Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BasicComponent(
                title = "播放",
                summary = if (playUrl == null) "加载播放地址中" else "开始播放",
                onClick = { playUrl?.let(onPlay) },
            )
            BasicComponent(title = "点赞", onClick = { })
            BasicComponent(title = "收藏", onClick = { })
            BasicComponent(title = "分享", onClick = { })
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    val remaining = seconds % 60
    return "%02d:%02d".format(minutes, remaining)
}

data class VideoDetailUiState(
    val isLoading: Boolean = true,
    val detail: VideoDetail? = null,
    val related: List<RecommendItem> = emptyList(),
    val playUrl: VideoPlayUrl? = null,
    val selectedCid: Long = 0,
    val error: String? = null,
)

class VideoDetailViewModel(
    private val bvid: String,
    private val repository: VideoRepository = VideoRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(VideoDetailUiState())
    val uiState: StateFlow<VideoDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun selectPage(cid: Long) {
        _uiState.update { it.copy(selectedCid = cid, playUrl = null, error = null) }
        loadPlayUrl(cid)
    }

    private fun load() {
        viewModelScope.launch {
            repository.detail(bvid).fold(
                onSuccess = { detail ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            detail = detail,
                            selectedCid = detail.cid,
                        )
                    }
                    loadRelated()
                    loadPlayUrl(detail.cid)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message ?: "视频详情加载失败") }
                },
            )
        }
    }

    private fun loadRelated() {
        viewModelScope.launch {
            repository.related(bvid).onSuccess { items ->
                _uiState.update { it.copy(related = items) }
            }
        }
    }

    private fun loadPlayUrl(cid: Long) {
        viewModelScope.launch {
            repository.playUrl(bvid, cid).fold(
                onSuccess = { url -> _uiState.update { it.copy(playUrl = url) } },
                onFailure = { error -> _uiState.update { it.copy(error = error.message ?: "播放地址加载失败") } },
            )
        }
    }
}
