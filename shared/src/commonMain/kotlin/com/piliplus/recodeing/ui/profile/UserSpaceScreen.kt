package com.piliplus.recodeing.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.backdrop.Backdrop
import com.piliplus.recodeing.core.design.BiliAsyncImage
import com.piliplus.recodeing.core.design.GlassBackButton
import com.piliplus.recodeing.core.design.GlassSurface
import com.piliplus.recodeing.core.design.LiquidButton
import com.piliplus.recodeing.core.model.UserRelationStat
import com.piliplus.recodeing.core.model.UserSpaceProfile
import com.piliplus.recodeing.core.model.UserVideoItem
import com.piliplus.recodeing.core.repository.UserSpaceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

data class UserSpaceUiState(
    val isLoading: Boolean = true,
    val profile: UserSpaceProfile? = null,
    val relation: UserRelationStat? = null,
    val videos: List<UserVideoItem> = emptyList(),
    val error: String? = null,
)

class UserSpaceViewModel(
    private val mid: Long,
    private val repository: UserSpaceRepository = UserSpaceRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserSpaceUiState())
    val uiState: StateFlow<UserSpaceUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        load()
    }

    fun reload() {
        loadJob?.cancel()
        _uiState.update { it.copy(isLoading = true, error = null) }
        load()
    }

    private fun load() {
        loadJob = viewModelScope.launch {
            val profile = async { repository.profile(mid) }
            val relation = async { repository.relation(mid) }
            val videos = async { repository.videos(mid) }
            val profileResult = profile.await()
            val relationResult = relation.await()
            val videosResult = videos.await()
            if (profileResult.isFailure) {
                _uiState.update {
                    it.copy(isLoading = false, error = profileResult.exceptionOrNull()?.message ?: "用户资料加载失败")
                }
                return@launch
            }
            _uiState.value = UserSpaceUiState(
                isLoading = false,
                profile = profileResult.getOrNull(),
                relation = relationResult.getOrNull(),
                videos = videosResult.getOrDefault(emptyList()),
                error = relationResult.exceptionOrNull()?.message ?: videosResult.exceptionOrNull()?.message,
            )
        }
    }
}

@Composable
fun UserSpaceScreen(
    mid: Long,
    backdrop: Backdrop,
    onBack: () -> Unit,
    onVideoSelected: (String) -> Unit,
    viewModel: UserSpaceViewModel = viewModel(key = "user-$mid") { UserSpaceViewModel(mid) },
) {
    val state by viewModel.uiState.collectAsState()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 900.dp).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { GlassBackButton(onClick = onBack, backdrop = backdrop) }
            if (state.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            state.error?.let { message ->
                item {
                    Card(Modifier.fillMaxWidth(), insideMargin = PaddingValues(18.dp)) {
                        Text(message, color = MiuixTheme.colorScheme.error)
                        LiquidButton(
                            onClick = viewModel::reload,
                            backdrop = backdrop,
                            modifier = Modifier.padding(top = 12.dp),
                        ) { Text("重试") }
                    }
                }
            }
            state.profile?.let { profile ->
                item {
                    GlassSurface(
                        backdrop = backdrop,
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp,
                        insideMargin = PaddingValues(18.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            BiliAsyncImage(
                                url = profile.face,
                                contentDescription = profile.name,
                                modifier = Modifier.size(76.dp).clip(CircleShape),
                            )
                            Column {
                                Text(profile.name.ifBlank { "未知用户" }, style = MiuixTheme.textStyles.title2)
                                Text(
                                    "UID ${profile.mid}",
                                    modifier = Modifier.padding(top = 4.dp),
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                                state.relation?.let { relation ->
                                    Text(
                                        "关注 ${relation.following} · 粉丝 ${relation.follower}",
                                        modifier = Modifier.padding(top = 4.dp),
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    )
                                }
                            }
                        }
                        Text(
                            profile.sign.ifBlank { "这个用户还没有填写签名" },
                            modifier = Modifier.padding(top = 14.dp),
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
                item { SmallTitle("投稿视频") }
                if (state.videos.isEmpty()) {
                    item {
                        Card(Modifier.fillMaxWidth(), insideMargin = PaddingValues(18.dp)) {
                            Text("暂无公开投稿")
                        }
                    }
                } else {
                    items(state.videos, key = { it.bvid.ifBlank { it.title } }) { video ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            insideMargin = PaddingValues(12.dp),
                            onClick = { if (video.bvid.isNotBlank()) onVideoSelected(video.bvid) },
                        ) {
                            BiliAsyncImage(
                                url = video.pic,
                                contentDescription = video.title,
                                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(16.dp)),
                            )
                            Text(
                                video.title.ifBlank { "未命名视频" },
                                modifier = Modifier.padding(top = 10.dp),
                                style = MiuixTheme.textStyles.title3,
                            )
                            Text(
                                "播放 ${video.play} · 评论 ${video.comment} · ${video.length}",
                                modifier = Modifier.padding(top = 6.dp),
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    }
                }
            }
        }
    }
}
