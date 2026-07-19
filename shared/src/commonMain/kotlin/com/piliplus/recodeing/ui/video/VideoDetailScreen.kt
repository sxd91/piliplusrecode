package com.piliplus.recodeing.ui.video

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.backdrop.Backdrop
import com.piliplus.recodeing.core.auth.AccountRepository
import com.piliplus.recodeing.core.design.BiliAsyncImage
import com.piliplus.recodeing.core.design.GlassBackButton
import com.piliplus.recodeing.core.design.GlassSurface
import com.piliplus.recodeing.core.design.LiquidButton
import com.piliplus.recodeing.core.model.RecommendItem
import com.piliplus.recodeing.core.model.VideoDetail
import com.piliplus.recodeing.core.model.VideoPlayUrl
import com.piliplus.recodeing.core.model.VideoRelation
import com.piliplus.recodeing.core.network.BiliApiService
import com.piliplus.recodeing.core.repository.CommentRepository
import com.piliplus.recodeing.core.repository.VideoRepository
import com.piliplus.recodeing.ui.settings.SettingsUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun VideoDetailScreen(
    bvid: String,
    backdrop: Backdrop,
    accountRepository: AccountRepository,
    settings: SettingsUiState,
    onBack: () -> Unit,
    onPlay: (VideoPlayUrl) -> Unit,
    onVideoSelected: (String) -> Unit,
    onAuthorSelected: (Long) -> Unit = { },
    viewModel: VideoDetailViewModel = run {
        val service = BiliApiService(cookieHeaderProvider = accountRepository::cookieHeader)
        val accountKey = accountRepository.authState.value.let { state ->
            if (state is com.piliplus.recodeing.core.auth.AuthState.LoggedIn) state.mid else 0L
        }
        viewModel(
            key = "video-$bvid-$accountKey-${settings.selectedCdnHost}-${settings.audioIgnoreCdn}",
        ) {
            VideoDetailViewModel(
                bvid = bvid,
                repository = VideoRepository(service = service),
                commentRepository = CommentRepository(service = service),
                cdnEndpoint = settings.selectedCdnHost,
                rewriteAudioCdn = !settings.audioIgnoreCdn,
            )
        }
    },
) {
    val state by viewModel.uiState.collectAsState()
    var activePlayUrl by remember(bvid, state.selectedCid) { mutableStateOf<VideoPlayUrl?>(null) }
    var selectedSection by remember(bvid) { mutableStateOf(VideoDetailSection.Description) }
    val sections = remember(settings.showVideoComments, settings.showRelatedVideos) {
        buildList {
            add(VideoDetailSection.Description)
            if (settings.showVideoComments) add(VideoDetailSection.Comments)
            if (settings.showRelatedVideos) add(VideoDetailSection.Related)
        }
    }
    LaunchedEffect(sections) {
        if (selectedSection !in sections) selectedSection = VideoDetailSection.Description
    }
    val authState by accountRepository.authState.collectAsState()
    val csrf = remember(authState, accountRepository) {
        accountRepository.storedCookies().firstOrNull { it.name == "bili_jct" }?.value.orEmpty()
    }
    VideoDetailContent(
        state = state,
        activePlayUrl = activePlayUrl,
        backdrop = backdrop,
        settings = settings,
        sections = sections,
        selectedSection = selectedSection,
        isLoggedIn = csrf.isNotBlank(),
        onBack = {
            if (activePlayUrl != null) activePlayUrl = null else onBack()
        },
        onPlay = {
            activePlayUrl = it
            onPlay(it)
        },
        onVideoSelected = onVideoSelected,
        onAuthorSelected = onAuthorSelected,
        onSelectPage = viewModel::selectPage,
        onSectionSelected = { selectedSection = it },
        commentRepository = viewModel.commentRepository,
        csrf = csrf,
        onLike = { viewModel.toggleLike(csrf) },
        onCoin = { viewModel.coin(csrf) },
        onRetry = viewModel::reload,
    )
}

@Composable
private fun VideoDetailContent(
    state: VideoDetailUiState,
    activePlayUrl: VideoPlayUrl?,
    backdrop: Backdrop,
    settings: SettingsUiState,
    sections: List<VideoDetailSection>,
    selectedSection: VideoDetailSection,
    isLoggedIn: Boolean,
    onBack: () -> Unit,
    onPlay: (VideoPlayUrl) -> Unit,
    onVideoSelected: (String) -> Unit,
    onAuthorSelected: (Long) -> Unit,
    onSelectPage: (Long) -> Unit,
    onSectionSelected: (VideoDetailSection) -> Unit,
    commentRepository: CommentRepository,
    csrf: String,
    onLike: () -> Unit,
    onCoin: () -> Unit,
    onRetry: () -> Unit,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 900.dp).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { GlassBackButton(onClick = onBack, backdrop = backdrop) }
            activePlayUrl?.let { playUrl ->
                item {
                    GlassSurface(
                        backdrop = backdrop,
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
                    ) {
                        PlatformVideoPlayer(
                            playUrl = playUrl,
                            modifier = Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(20.dp)),
                        )
                    }
                }
            }
            state.error?.let { errorMessage ->
                item {
                    Card(Modifier.fillMaxWidth(), insideMargin = PaddingValues(18.dp)) {
                        Text(errorMessage, color = MiuixTheme.colorScheme.error)
                        LiquidButton(
                            onClick = onRetry,
                            backdrop = backdrop,
                            modifier = Modifier.padding(top = 10.dp),
                        ) { Text("重试") }
                    }
                }
            }
            if (state.isLoading && state.detail == null) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            state.detail?.let { detail ->
                item {
                    DetailHeader(
                        detail = detail,
                        playUrl = state.playUrl,
                        backdrop = backdrop,
                        actionInProgress = state.actionInProgress,
                        actionMessage = state.actionMessage,
                        relation = state.relation,
                        isLoggedIn = isLoggedIn,
                        onPlay = onPlay,
                        onAuthorSelected = onAuthorSelected,
                        onLike = onLike,
                        onCoin = onCoin,
                    )
                }
                if (detail.pages.size > 1) {
                    item { SmallTitle("分P与合集") }
                    items(detail.pages, key = { it.cid }) { page ->
                        BasicComponent(
                            title = page.part.ifBlank { "第 ${page.page} P" },
                            summary = formatDuration(page.duration),
                            onClick = { onSelectPage(page.cid) },
                        )
                    }
                }
                item {
                    TabRow(
                        tabs = sections.map(VideoDetailSection::title),
                        selectedTabIndex = sections.indexOf(selectedSection).coerceAtLeast(0),
                        onTabSelected = { index -> sections.getOrNull(index)?.let(onSectionSelected) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                when (selectedSection) {
                    VideoDetailSection.Description -> item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            insideMargin = PaddingValues(18.dp),
                        ) {
                            Text(detail.desc.ifBlank { "暂无简介" })
                        }
                    }
                    VideoDetailSection.Comments -> if (settings.showVideoComments) {
                        videoComments(
                            aid = detail.aid,
                            csrf = csrf,
                            backdrop = backdrop,
                            repository = commentRepository,
                        )
                    }
                    VideoDetailSection.Related -> if (settings.showRelatedVideos) {
                        if (state.related.isEmpty()) {
                            item {
                                Card(Modifier.fillMaxWidth(), insideMargin = PaddingValues(18.dp)) {
                                    Text("暂无相关推荐")
                                }
                            }
                        } else {
                            items(state.related, key = { it.bvid ?: it.aid ?: it.title }) { item ->
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
        }
    }
}

@Composable
private fun DetailHeader(
    detail: VideoDetail,
    playUrl: VideoPlayUrl?,
    backdrop: Backdrop,
    actionInProgress: Boolean,
    actionMessage: String?,
    relation: VideoRelation?,
    isLoggedIn: Boolean,
    onPlay: (VideoPlayUrl) -> Unit,
    onAuthorSelected: (Long) -> Unit,
    onLike: () -> Unit,
    onCoin: () -> Unit,
) {
    GlassSurface(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        insideMargin = PaddingValues(14.dp),
    ) {
        BiliAsyncImage(
            url = detail.pic,
            contentDescription = detail.title,
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(18.dp)),
        )
        Text(
            detail.title,
            modifier = Modifier.padding(top = 14.dp),
            style = MiuixTheme.textStyles.title2,
        )
        Row(
            modifier = Modifier
                .padding(top = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { detail.owner?.mid?.let(onAuthorSelected) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BiliAsyncImage(
                url = detail.owner?.face,
                contentDescription = detail.owner?.name,
                modifier = Modifier.size(44.dp).clip(CircleShape),
            )
            Column {
                Text(detail.owner?.name ?: "未知 UP 主", style = MiuixTheme.textStyles.title3)
                Text(
                    text = "播放 ${detail.stat?.view ?: 0} · 弹幕 ${detail.stat?.danmaku ?: 0}",
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LiquidButton(
                onClick = { playUrl?.let(onPlay) },
                backdrop = backdrop,
                enabled = playUrl != null,
                tint = MiuixTheme.colorScheme.primary.copy(alpha = 0.18f),
            ) { Text(if (playUrl == null) "加载播放地址" else "播放") }
            LiquidButton(
                onClick = onLike,
                backdrop = backdrop,
                enabled = isLoggedIn && relation != null && !actionInProgress,
                tint = if (relation?.like == true) MiuixTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Unspecified,
            ) {
                Text(if (relation?.like == true) "已点赞" else "点赞")
            }
            LiquidButton(
                onClick = onCoin,
                backdrop = backdrop,
                enabled = isLoggedIn && relation != null && !actionInProgress,
            ) {
                Text(if ((relation?.coin ?: 0) > 0) "已投 ${relation?.coin} 币" else "投币")
            }
            LiquidButton(onClick = { }, backdrop = backdrop, enabled = false) {
                Text(if (relation?.favorite == true) "已收藏" else "收藏夹接入中")
            }
        }
        AnimatedVisibility(visible = actionMessage != null, enter = fadeIn(), exit = fadeOut()) {
            Text(
                text = actionMessage.orEmpty(),
                modifier = Modifier.padding(top = 10.dp),
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
        if (!isLoggedIn) {
            Text(
                "登录后可点赞、投币和收藏",
                modifier = Modifier.padding(top = 8.dp),
                color = Color.Unspecified,
            )
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    val remaining = seconds % 60
    return "%02d:%02d".format(minutes, remaining)
}

private enum class VideoDetailSection(val title: String) {
    Description("简介"),
    Comments("评论"),
    Related("相关推荐"),
}

data class VideoDetailUiState(
    val isLoading: Boolean = true,
    val detail: VideoDetail? = null,
    val relation: VideoRelation? = null,
    val related: List<RecommendItem> = emptyList(),
    val playUrl: VideoPlayUrl? = null,
    val selectedCid: Long = 0,
    val actionInProgress: Boolean = false,
    val actionMessage: String? = null,
    val error: String? = null,
)

class VideoDetailViewModel(
    private val bvid: String,
    private val repository: VideoRepository = VideoRepository(),
    val commentRepository: CommentRepository = CommentRepository(BiliApiService()),
    private val cdnEndpoint: String = "auto",
    private val rewriteAudioCdn: Boolean = true,
) : ViewModel() {
    private val _uiState = MutableStateFlow(VideoDetailUiState())
    val uiState: StateFlow<VideoDetailUiState> = _uiState.asStateFlow()
    private var detailJob: Job? = null
    private var relationJob: Job? = null
    private var relatedJob: Job? = null
    private var playUrlJob: Job? = null

    init {
        load()
    }

    fun reload() {
        detailJob?.cancel()
        relationJob?.cancel()
        relatedJob?.cancel()
        playUrlJob?.cancel()
        _uiState.update {
            it.copy(
                isLoading = true,
                relation = null,
                related = emptyList(),
                playUrl = null,
                actionMessage = null,
                error = null,
            )
        }
        load()
    }

    fun selectPage(cid: Long) {
        playUrlJob?.cancel()
        _uiState.update { it.copy(selectedCid = cid, playUrl = null, error = null) }
        loadPlayUrl(cid)
    }

    fun toggleLike(csrf: String) {
        val detail = _uiState.value.detail ?: return
        val targetLike = _uiState.value.relation?.like != true
        runAction(
            name = if (targetLike) "点赞" else "取消点赞",
            request = { repository.like(detail.aid, like = targetLike, csrf = csrf) },
            onSuccess = {
                _uiState.update { state ->
                    state.copy(relation = (state.relation ?: VideoRelation()).copy(like = targetLike))
                }
            },
        )
    }

    fun coin(csrf: String) {
        val detail = _uiState.value.detail ?: return
        runAction(
            "投币",
            request = { repository.coin(detail.aid, count = 1, alsoLike = false, csrf = csrf) },
            onSuccess = {
                loadRelation(detail.aid)
            },
        )
    }

    private fun runAction(
        name: String,
        request: suspend () -> Result<Unit>,
        onSuccess: () -> Unit = {},
    ) {
        if (_uiState.value.actionInProgress) return
        viewModelScope.launch {
            _uiState.update { it.copy(actionInProgress = true, actionMessage = null) }
            request().fold(
                onSuccess = {
                    onSuccess()
                    _uiState.update { it.copy(actionInProgress = false, actionMessage = "$name 成功") }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(actionInProgress = false, actionMessage = error.message ?: "$name 失败")
                    }
                },
            )
        }
    }

    private fun load() {
        detailJob = viewModelScope.launch {
            repository.detail(bvid).fold(
                onSuccess = { detail ->
                    _uiState.update { it.copy(isLoading = false, detail = detail, selectedCid = detail.cid, error = null) }
                    loadRelation(detail.aid)
                    loadRelated()
                    loadPlayUrl(detail.cid)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message ?: "视频详情加载失败") }
                },
            )
        }
    }

    private fun loadRelation(aid: Long) {
        relationJob?.cancel()
        relationJob = viewModelScope.launch {
            repository.relation(aid).onSuccess { relation ->
                _uiState.update { it.copy(relation = relation) }
            }
        }
    }

    private fun loadRelated() {
        relatedJob?.cancel()
        relatedJob = viewModelScope.launch {
            repository.related(bvid).onSuccess { items -> _uiState.update { it.copy(related = items) } }
        }
    }

    private fun loadPlayUrl(cid: Long) {
        playUrlJob?.cancel()
        playUrlJob = viewModelScope.launch {
            repository.playUrl(
                bvid = bvid,
                cid = cid,
                cdnEndpoint = cdnEndpoint,
                rewriteAudioCdn = rewriteAudioCdn,
            ).fold(
                onSuccess = { url ->
                    _uiState.update { state -> if (state.selectedCid == cid) state.copy(playUrl = url) else state }
                },
                onFailure = { error ->
                    _uiState.update { state ->
                        if (state.selectedCid == cid) state.copy(error = error.message ?: "播放地址加载失败") else state
                    }
                },
            )
        }
    }
}
