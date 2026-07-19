package com.piliplus.recodeing.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.backdrop.Backdrop
import com.piliplus.recodeing.core.design.BiliAsyncImage
import com.piliplus.recodeing.core.design.GlassSurface
import com.piliplus.recodeing.core.design.LiquidButton
import com.piliplus.recodeing.core.model.RecommendItem
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun HomeScreen(
    backdrop: Backdrop,
    feedColumns: Int,
    onSearch: () -> Unit,
    onVideoSelected: (String) -> Unit,
    viewModel: HomeViewModel = viewModel { HomeViewModel() },
) {
    val state by viewModel.uiState.collectAsState()
    val gridState = rememberLazyGridState()
    val source = when (state.selectedTab) {
        HomeTab.Recommend -> state.recommendItems
        HomeTab.Popular -> state.popularItems
    }
    val nowSeconds = kotlin.time.Clock.System.now().epochSeconds
    val visibleItems = remember(source, state.minimumDurationMinutes, state.publishedWithinDays) {
        source.filter { item ->
            val durationMatches = state.minimumDurationMinutes == 0 ||
                item.duration >= state.minimumDurationMinutes * 60L
            val publishedMatches = state.publishedWithinDays == 0 || item.pubdate == null ||
                item.pubdate >= nowSeconds - state.publishedWithinDays * 86_400L
            durationMatches && publishedMatches
        }
    }

    LaunchedEffect(gridState, visibleItems.size) {
        snapshotFlow {
            val info = gridState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            val userHasScrolled = gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 0
            userHasScrolled && info.totalItemsCount > 0 && lastVisible >= info.totalItemsCount - 5
        }.distinctUntilChanged().filter { it }.collect { viewModel.loadMore() }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val resolvedColumns = remember(maxWidth, feedColumns) {
            when {
                feedColumns > 0 -> feedColumns.coerceIn(1, 6)
                maxWidth < 520.dp -> 1
                maxWidth < 900.dp -> 2
                maxWidth < 1280.dp -> 3
                else -> 4
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(resolvedColumns),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                GlassSurface(
                    backdrop = backdrop,
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                ) {
                    LiquidButton(
                        onClick = onSearch,
                        backdrop = backdrop,
                        modifier = Modifier.fillMaxWidth(),
                        adaptiveLuminance = true,
                        tint = MiuixTheme.colorScheme.primary.copy(alpha = 0.12f),
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Light.Search,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Text("搜索视频、UP 主、番剧与直播")
                    }
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                TabRow(
                    tabs = HomeTab.entries.map(HomeTab::title),
                    selectedTabIndex = state.selectedTab.ordinal,
                    onTabSelected = { viewModel.selectTab(HomeTab.entries[it]) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(0 to "全部", 5 to "5 分钟+", 20 to "20 分钟+").forEach { (minutes, label) ->
                        LiquidButton(
                            onClick = { viewModel.setDurationFilter(minutes) },
                            backdrop = backdrop,
                            tint = selectedTint(state.minimumDurationMinutes == minutes),
                        ) { Text(label) }
                    }
                    listOf(0 to "全部时间", 1 to "今天", 7 to "一周内").forEach { (days, label) ->
                        LiquidButton(
                            onClick = { viewModel.setPublishedFilter(days) },
                            backdrop = backdrop,
                            tint = selectedTint(state.publishedWithinDays == days),
                        ) { Text(label) }
                    }
                }
            }
            state.error?.let { message ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Card(Modifier.fillMaxWidth(), insideMargin = PaddingValues(18.dp)) {
                        Text("加载失败", style = MiuixTheme.textStyles.title3)
                        Text(message, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                        LiquidButton(
                            onClick = viewModel::refresh,
                            backdrop = backdrop,
                            modifier = Modifier.padding(top = 12.dp),
                        ) { Text("重试") }
                    }
                }
            }
            items(
                items = visibleItems,
                key = { item -> item.bvid ?: item.aid ?: item.id ?: item.title },
            ) { item ->
                VideoCard(item = item, onClick = { item.bvid?.let(onVideoSelected) })
            }
            if (state.isLoading || state.isLoadingMore) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoCard(
    item: RecommendItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(12.dp),
        pressFeedbackType = PressFeedbackType.Sink,
        showIndication = true,
        onClick = onClick,
    ) {
        BiliAsyncImage(
            url = item.pic,
            contentDescription = item.title,
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(16.dp)),
        )
        Text(
            text = item.title.ifBlank { "未命名视频" },
            modifier = Modifier.padding(top = 10.dp),
            style = MiuixTheme.textStyles.title3,
        )
        Text(
            text = item.owner?.name ?: "未知 UP 主",
            modifier = Modifier.padding(top = 6.dp),
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Text(
            text = buildString {
                append("播放 ").append(item.stat?.view ?: 0)
                append(" · 弹幕 ").append(item.stat?.danmaku ?: 0)
            },
            modifier = Modifier.padding(top = 4.dp),
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun selectedTint(selected: Boolean): Color = if (selected) {
    MiuixTheme.colorScheme.primary.copy(alpha = 0.18f)
} else {
    Color.Unspecified
}
