package com.piliplus.recodeing.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.backdrop.Backdrop
import com.piliplus.recodeing.core.design.BiliAsyncImage
import com.piliplus.recodeing.core.design.GlassBackButton
import com.piliplus.recodeing.core.design.GlassSurface
import com.piliplus.recodeing.core.design.LiquidButton
import com.piliplus.recodeing.core.model.SearchResultItem
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun SearchScreen(
    backdrop: Backdrop,
    onBack: () -> Unit,
    onVideoSelected: (String) -> Unit,
    viewModel: SearchViewModel = viewModel { SearchViewModel() },
) {
    val state by viewModel.state.collectAsState()
    val filteredResults = state.results.filter { item ->
        state.minimumDurationMinutes == 0 || item.duration.toDurationSeconds() >= state.minimumDurationMinutes * 60
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier.widthIn(max = 840.dp).fillMaxSize().padding(horizontal = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlassBackButton(onClick = onBack, backdrop = backdrop)
                GlassSurface(
                    backdrop = backdrop,
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                ) {
                    InputField(
                        query = state.query,
                        onQueryChange = viewModel::updateQuery,
                        onSearch = viewModel::submit,
                        expanded = true,
                        onExpandedChange = { },
                        label = state.defaultKeyword,
                        color = Color.Transparent,
                    )
                }
            }

            AnimatedVisibility(
                visible = state.query.isBlank() || state.suggestions.isNotEmpty(),
                enter = fadeIn() + slideInVertically { -it / 4 },
                exit = fadeOut() + slideOutVertically { -it / 4 },
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    insideMargin = PaddingValues(vertical = 4.dp),
                ) {
                    if (state.query.isBlank()) {
                        if (state.history.isEmpty()) {
                            BasicComponent(
                                title = "搜索视频、番剧、直播间与用户",
                                summary = "输入关键词开始搜索",
                            )
                        } else {
                            state.history.forEach { keyword ->
                                BasicComponent(
                                    title = keyword,
                                    summary = "搜索历史",
                                    onClick = { viewModel.submit(keyword) },
                                )
                            }
                            BasicComponent(title = "清空搜索历史", onClick = viewModel::clearHistory)
                        }
                    } else {
                        state.suggestions.take(8).forEach { suggestion ->
                            val keyword = suggestion.value.ifBlank { suggestion.term ?: suggestion.name.orEmpty() }
                            if (keyword.isNotBlank()) {
                                BasicComponent(title = keyword, onClick = { viewModel.submit(keyword) })
                            }
                        }
                    }
                }
            }

            if (state.results.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(0 to "全部时长", 5 to "5 分钟+", 20 to "20 分钟+").forEach { (minutes, label) ->
                        LiquidButton(
                            onClick = { viewModel.setDurationFilter(minutes) },
                            backdrop = backdrop,
                            tint = if (state.minimumDurationMinutes == minutes) {
                                MiuixTheme.colorScheme.primary.copy(alpha = 0.18f)
                            } else {
                                Color.Unspecified
                            },
                        ) { Text(label) }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
            ) {
                state.error?.let { message ->
                    item {
                        Card(Modifier.fillMaxWidth(), insideMargin = PaddingValues(18.dp)) {
                            Text(message, color = MiuixTheme.colorScheme.error)
                        }
                    }
                }
                if (state.isSearching) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                if (filteredResults.isNotEmpty()) item { SmallTitle("搜索结果") }
                items(filteredResults, key = { it.bvid ?: it.aid ?: it.mid ?: it.title }) { item ->
                    SearchResultCard(item = item, onVideoSelected = onVideoSelected)
                }
                if (state.hasMore) {
                    item {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            LiquidButton(
                                onClick = viewModel::loadMore,
                                backdrop = backdrop,
                                enabled = !state.isLoadingMore,
                            ) {
                                Text(if (state.isLoadingMore) "加载中" else "加载更多")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    item: SearchResultItem,
    onVideoSelected: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(16.dp),
        pressFeedbackType = PressFeedbackType.Sink,
        showIndication = true,
        onClick = { item.bvid?.let(onVideoSelected) },
    ) {
        BiliAsyncImage(
            url = item.pic ?: item.cover,
            contentDescription = item.title.cleanSearchText(),
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(16.dp)),
        )
        Text(
            text = item.title.cleanSearchText().ifBlank { "未命名结果" },
            modifier = Modifier.padding(top = 12.dp),
            style = MiuixTheme.textStyles.title3,
        )
        Text(
            text = item.author ?: item.uname ?: item.type ?: "Bilibili",
            modifier = Modifier.padding(top = 6.dp),
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

private fun String?.toDurationSeconds(): Int {
    if (this.isNullOrBlank()) return 0
    return split(':').mapNotNull(String::toIntOrNull).fold(0) { total, part -> total * 60 + part }
}

private fun String.cleanSearchText(): String = replace(Regex("<[^>]+>"), "")
