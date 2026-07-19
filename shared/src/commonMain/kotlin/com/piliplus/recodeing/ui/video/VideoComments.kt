package com.piliplus.recodeing.ui.video

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
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
import com.piliplus.recodeing.core.design.LiquidButton
import com.piliplus.recodeing.core.model.CommentItem
import com.piliplus.recodeing.core.model.CommentSort
import com.piliplus.recodeing.core.repository.CommentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

data class VideoCommentsUiState(
    val sort: CommentSort = CommentSort.Hot,
    val comments: List<CommentItem> = emptyList(),
    val page: Int = 0,
    val nextCursor: Long? = null,
    val totalCount: Int = 0,
    val endReached: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
    val selectedRoot: CommentItem? = null,
    val replies: List<CommentItem> = emptyList(),
    val replyPage: Int = 0,
    val replyTotalCount: Int = 0,
    val repliesEndReached: Boolean = false,
    val repliesLoading: Boolean = false,
    val composerText: String = "",
    val actionMessage: String? = null,
    val submitting: Boolean = false,
    val reactingCommentIds: Set<Long> = emptySet(),
)

class VideoCommentsViewModel(
    private val aid: Long,
    private val repository: CommentRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(VideoCommentsUiState())
    val state: StateFlow<VideoCommentsUiState> = _state.asStateFlow()
    private var commentsJob: Job? = null
    private var repliesJob: Job? = null

    init {
        loadComments(reset = true)
    }

    fun selectSort(sort: CommentSort) {
        if (_state.value.sort == sort) return
        commentsJob?.cancel()
        _state.value = VideoCommentsUiState(sort = sort)
        loadComments(reset = true)
    }

    fun loadMoreComments() {
        val state = _state.value
        if (state.loading || state.endReached) return
        loadComments(reset = false)
    }

    fun openThread(comment: CommentItem) {
        repliesJob?.cancel()
        _state.update {
            it.copy(
                selectedRoot = comment,
                replies = emptyList(),
                replyPage = 0,
                replyTotalCount = comment.rcount.coerceAtLeast(comment.count),
                repliesEndReached = false,
                error = null,
                composerText = "",
            )
        }
        loadReplies(reset = true)
    }

    fun closeThread() {
        repliesJob?.cancel()
        _state.update {
            it.copy(
                selectedRoot = null,
                replies = emptyList(),
                replyPage = 0,
                repliesEndReached = false,
                composerText = "",
                actionMessage = null,
            )
        }
    }

    fun loadMoreReplies() {
        val state = _state.value
        if (state.repliesLoading || state.repliesEndReached || state.selectedRoot == null) return
        loadReplies(reset = false)
    }

    fun updateComposer(value: String) {
        _state.update { it.copy(composerText = value, actionMessage = null) }
    }

    fun like(comment: CommentItem, csrf: String) {
        if (comment.rpid in _state.value.reactingCommentIds) return
        val targetLike = comment.action != 1
        viewModelScope.launch {
            _state.update { it.copy(reactingCommentIds = it.reactingCommentIds + comment.rpid) }
            repository.like(aid, comment.rpid, targetLike, csrf).fold(
                onSuccess = {
                    updateComment(comment.rpid) {
                        it.copy(
                            action = if (targetLike) 1 else 0,
                            like = (it.like + if (targetLike) 1 else -1).coerceAtLeast(0),
                        )
                    }
                    _state.update {
                        it.copy(
                            reactingCommentIds = it.reactingCommentIds - comment.rpid,
                            actionMessage = if (targetLike) "已点赞评论" else "已取消点赞",
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            reactingCommentIds = it.reactingCommentIds - comment.rpid,
                            actionMessage = error.message ?: "评论点赞失败",
                        )
                    }
                },
            )
        }
    }

    fun submit(csrf: String, parent: CommentItem? = null) {
        val state = _state.value
        if (state.submitting || state.composerText.isBlank()) return
        val root = state.selectedRoot
        viewModelScope.launch {
            _state.update { it.copy(submitting = true, actionMessage = null) }
            repository.add(
                aid = aid,
                message = state.composerText,
                csrf = csrf,
                root = root?.rpid,
                parent = parent?.rpid ?: root?.rpid,
            ).fold(
                onSuccess = { created ->
                    _state.update {
                        it.copy(
                            composerText = "",
                            submitting = false,
                            actionMessage = "评论发布成功",
                            comments = if (root == null && created != null) listOf(created) + it.comments else it.comments,
                            replies = if (root != null && created != null) it.replies + created else it.replies,
                        )
                    }
                    if (created == null) {
                        if (root == null) loadComments(reset = true) else loadReplies(reset = true)
                    }
                },
                onFailure = { error ->
                    _state.update { it.copy(submitting = false, actionMessage = error.message ?: "评论发布失败") }
                },
            )
        }
    }

    private fun loadComments(reset: Boolean) {
        commentsJob?.cancel()
        val nextPage = if (reset) 1 else _state.value.page + 1
        commentsJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            repository.comments(
                aid = aid,
                sort = _state.value.sort,
                page = nextPage,
                cursor = if (reset) null else _state.value.nextCursor,
            ).fold(
                onSuccess = { result ->
                    _state.update {
                        it.copy(
                            comments = (if (reset) result.items else it.comments + result.items)
                                .distinctBy(CommentItem::rpid),
                            page = nextPage,
                            nextCursor = result.nextCursor,
                            totalCount = result.totalCount,
                            endReached = result.endReached || (!reset && result.nextCursor == it.nextCursor),
                            loading = false,
                        )
                    }
                },
                onFailure = { error ->
                    _state.update { it.copy(loading = false, error = error.message ?: "评论加载失败") }
                },
            )
        }
    }

    private fun loadReplies(reset: Boolean) {
        val root = _state.value.selectedRoot ?: return
        repliesJob?.cancel()
        val nextPage = if (reset) 1 else _state.value.replyPage + 1
        repliesJob = viewModelScope.launch {
            _state.update { it.copy(repliesLoading = true, error = null) }
            repository.replies(aid, root.rpid, nextPage).fold(
                onSuccess = { result ->
                    _state.update {
                        it.copy(
                            selectedRoot = result.root ?: it.selectedRoot,
                            replies = (if (reset) result.replies else it.replies + result.replies)
                                .distinctBy(CommentItem::rpid),
                            replyPage = nextPage,
                            replyTotalCount = result.totalCount,
                            repliesEndReached = result.endReached,
                            repliesLoading = false,
                        )
                    }
                },
                onFailure = { error ->
                    _state.update { it.copy(repliesLoading = false, error = error.message ?: "回复加载失败") }
                },
            )
        }
    }

    private fun updateComment(rpid: Long, transform: (CommentItem) -> CommentItem) {
        _state.update {
            it.copy(
                comments = it.comments.map { item -> if (item.rpid == rpid) transform(item) else item },
                replies = it.replies.map { item -> if (item.rpid == rpid) transform(item) else item },
                selectedRoot = it.selectedRoot?.let { item -> if (item.rpid == rpid) transform(item) else item },
            )
        }
    }
}

fun LazyListScope.videoComments(
    aid: Long,
    csrf: String,
    backdrop: Backdrop,
    repository: CommentRepository,
) {
    item(key = "comments-$aid") {
        val viewModel: VideoCommentsViewModel = viewModel(key = "comments-$aid-${repository.hashCode()}") {
            VideoCommentsViewModel(aid, repository)
        }
        val state by viewModel.state.collectAsState()
        VideoCommentsContent(
            state = state,
            csrf = csrf,
            backdrop = backdrop,
            onSort = viewModel::selectSort,
            onLoadMore = viewModel::loadMoreComments,
            onOpenThread = viewModel::openThread,
            onCloseThread = viewModel::closeThread,
            onLoadMoreReplies = viewModel::loadMoreReplies,
            onComposerChange = viewModel::updateComposer,
            onSubmit = { viewModel.submit(csrf) },
            onLike = { viewModel.like(it, csrf) },
        )
    }
}

@Composable
private fun VideoCommentsContent(
    state: VideoCommentsUiState,
    csrf: String,
    backdrop: Backdrop,
    onSort: (CommentSort) -> Unit,
    onLoadMore: () -> Unit,
    onOpenThread: (CommentItem) -> Unit,
    onCloseThread: () -> Unit,
    onLoadMoreReplies: () -> Unit,
    onComposerChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onLike: (CommentItem) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (state.selectedRoot == null) {
            TabRow(
                tabs = CommentSort.entries.map(CommentSort::title),
                selectedTabIndex = state.sort.ordinal,
                onTabSelected = { index -> CommentSort.entries.getOrNull(index)?.let(onSort) },
                modifier = Modifier.fillMaxWidth(),
            )
            CommentComposer(state, csrf, backdrop, onComposerChange, onSubmit)
            state.comments.forEach { comment ->
                CommentCard(
                    comment = comment,
                    csrf = csrf,
                    backdrop = backdrop,
                    reacting = comment.rpid in state.reactingCommentIds,
                    onLike = onLike,
                    onOpenThread = onOpenThread,
                )
            }
            CommentLoadFooter(state.loading, state.endReached, state.error, backdrop, onLoadMore)
        } else {
            LiquidButton(onClick = onCloseThread, backdrop = backdrop) { Text("返回主评论") }
            Text("楼中楼 · ${state.replyTotalCount} 条回复", style = MiuixTheme.textStyles.title3)
            CommentCard(
                comment = state.selectedRoot,
                csrf = csrf,
                backdrop = backdrop,
                reacting = state.selectedRoot.rpid in state.reactingCommentIds,
                onLike = onLike,
                onOpenThread = null,
            )
            CommentComposer(state, csrf, backdrop, onComposerChange, onSubmit)
            state.replies.forEach { reply ->
                CommentCard(
                    comment = reply,
                    csrf = csrf,
                    backdrop = backdrop,
                    reacting = reply.rpid in state.reactingCommentIds,
                    onLike = onLike,
                    onOpenThread = null,
                )
            }
            CommentLoadFooter(
                state.repliesLoading,
                state.repliesEndReached,
                state.error,
                backdrop,
                onLoadMoreReplies,
            )
        }
        state.actionMessage?.let {
            Text(it, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        }
    }
}

@Composable
private fun CommentComposer(
    state: VideoCommentsUiState,
    csrf: String,
    backdrop: Backdrop,
    onChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    InputField(
        query = state.composerText,
        onQueryChange = onChange,
        onSearch = { onSubmit() },
        expanded = false,
        onExpandedChange = { },
        label = if (csrf.isBlank()) "登录后可发表评论" else "写下你的评论",
        enabled = csrf.isNotBlank() && !state.submitting,
    )
    LiquidButton(
        onClick = onSubmit,
        backdrop = backdrop,
        enabled = csrf.isNotBlank() && state.composerText.isNotBlank() && !state.submitting,
    ) { Text(if (state.submitting) "发布中" else "发布评论") }
}

@Composable
private fun CommentCard(
    comment: CommentItem,
    csrf: String,
    backdrop: Backdrop,
    reacting: Boolean,
    onLike: (CommentItem) -> Unit,
    onOpenThread: ((CommentItem) -> Unit)?,
) {
    Card(
        modifier = Modifier.fillMaxWidth().then(
            if (onOpenThread != null && comment.rcount.coerceAtLeast(comment.count) > 0) {
                Modifier.clickable { onOpenThread(comment) }
            } else {
                Modifier
            },
        ),
        insideMargin = PaddingValues(14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BiliAsyncImage(
                url = comment.member?.avatar,
                contentDescription = comment.member?.uname,
                modifier = Modifier.size(38.dp).clip(CircleShape),
            )
            Column {
                Text(comment.member?.uname?.ifBlank { "未知用户" } ?: "未知用户")
                Text(
                    listOfNotNull(comment.replyControl?.location, formatCommentTime(comment.ctime)).joinToString(" · "),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
        Text(comment.content.message.ifBlank { "[该评论没有文本内容]" }, modifier = Modifier.padding(top = 10.dp))
        comment.content.pictures.orEmpty().firstOrNull()?.let { picture ->
            BiliAsyncImage(
                url = picture.imageUrl,
                contentDescription = "评论图片",
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            )
        }
        Row(
            modifier = Modifier.padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LiquidButton(
                onClick = { onLike(comment) },
                backdrop = backdrop,
                compact = true,
                enabled = csrf.isNotBlank() && !reacting,
                tint = if (comment.action == 1) MiuixTheme.colorScheme.primary.copy(alpha = 0.18f) else androidx.compose.ui.graphics.Color.Unspecified,
            ) { Text("赞 ${comment.like}") }
            val replies = comment.rcount.coerceAtLeast(comment.count)
            if (onOpenThread != null && replies > 0) {
                LiquidButton(onClick = { onOpenThread(comment) }, backdrop = backdrop, compact = true) {
                    Text("回复 $replies")
                }
            }
        }
    }
}

@Composable
private fun CommentLoadFooter(
    loading: Boolean,
    endReached: Boolean,
    error: String?,
    backdrop: Backdrop,
    onLoadMore: () -> Unit,
) {
    if (loading) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { CircularProgressIndicator() }
    } else if (!endReached) {
        error?.let { Text(it, color = MiuixTheme.colorScheme.error) }
        LiquidButton(onClick = onLoadMore, backdrop = backdrop, modifier = Modifier.fillMaxWidth()) {
            Text(if (error == null) "加载更多" else "重试加载")
        }
    } else if (error != null) {
        Text(error, color = MiuixTheme.colorScheme.error)
    } else {
        Text("没有更多评论", color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
    }
}

private fun formatCommentTime(epochSeconds: Long): String {
    if (epochSeconds <= 0) return ""
    val days = ((kotlin.time.Clock.System.now().epochSeconds - epochSeconds).coerceAtLeast(0) / 86_400)
    return when {
        days == 0L -> "今天"
        days == 1L -> "昨天"
        days < 30L -> "$days 天前"
        days < 365L -> "${days / 30} 个月前"
        else -> "${days / 365} 年前"
    }
}
