package com.piliplus.recodeing.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.piliplus.recodeing.core.model.RecommendItem
import com.piliplus.recodeing.core.model.SearchResultItem
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel { HomeViewModel() },
) {
    val state by viewModel.uiState.collectAsState()
    val items = remember(state.selectedTab, state.recommendItems, state.popularItems) {
        when (state.selectedTab) {
            HomeTab.Recommend -> state.recommendItems
            HomeTab.Popular -> state.popularItems
            HomeTab.Search -> emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SearchBar(
            inputField = {
                InputField(
                    query = state.searchQuery,
                    onQueryChange = viewModel::updateSearchQuery,
                    onSearch = viewModel::submitSearch,
                    expanded = state.searchQuery.isNotBlank() && state.searchSuggestions.isNotEmpty(),
                    onExpandedChange = {},
                    label = state.searchDefault,
                )
            },
            onExpandedChange = {},
            expanded = state.searchQuery.isNotBlank() && state.searchSuggestions.isNotEmpty(),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                insideMargin = PaddingValues(vertical = 4.dp),
            ) {
                state.searchSuggestions.take(8).forEach { suggestion ->
                    val text = suggestion.value.ifBlank { suggestion.term ?: suggestion.name.orEmpty() }
                    Text(
                        text = text,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.submitSearch(text) }
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HomeTab.entries.forEach { tab ->
                Button(
                    onClick = { viewModel.selectTab(tab) },
                    colors = if (state.selectedTab == tab) {
                        ButtonDefaults.buttonColorsPrimary()
                    } else {
                        ButtonDefaults.buttonColors()
                    },
                ) {
                    Text(tab.title)
                }
            }
        }

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(24.dp))
        }

        state.error?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                insideMargin = PaddingValues(18.dp),
            ) {
                Text("加载失败", style = MiuixTheme.textStyles.title3)
                Text(message, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                Button(
                    onClick = viewModel::refresh,
                    modifier = Modifier.padding(top = 12.dp),
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text("重试")
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 112.dp),
        ) {
            when (state.selectedTab) {
                HomeTab.Recommend,
                HomeTab.Popular,
                -> items(items) { item ->
                    VideoCard(item)
                }
                HomeTab.Search -> items(state.searchResults) { item ->
                    SearchResultCard(item)
                }
            }
        }
    }
}

@Composable
private fun VideoCard(item: RecommendItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(18.dp),
    ) {
        Text(
            text = item.title.ifBlank { "未命名视频" },
            style = MiuixTheme.textStyles.title3,
        )
        Text(
            text = item.owner?.name ?: "未知 UP 主",
            modifier = Modifier.padding(top = 6.dp),
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Text(
            text = buildString {
                append("播放 ")
                append(item.stat?.view ?: 0)
                append(" · 弹幕 ")
                append(item.stat?.danmaku ?: 0)
                item.recommendReason?.content?.let { reason -> append(" · ").append(reason) }
            },
            modifier = Modifier.padding(top = 8.dp),
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun SearchResultCard(item: SearchResultItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(18.dp),
    ) {
        Text(
            text = item.title.cleanSearchText().ifBlank { "未命名结果" },
            style = MiuixTheme.textStyles.title3,
        )
        Text(
            text = item.author ?: item.uname ?: item.type ?: "搜索结果",
            modifier = Modifier.padding(top = 6.dp),
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Text(
            text = buildString {
                item.bvid?.let { append(it) }
                item.play?.let {
                    if (isNotEmpty()) append(" · ")
                    append("播放 ").append(it)
                }
                item.danmaku?.let {
                    if (isNotEmpty()) append(" · ")
                    append("弹幕 ").append(it)
                }
            }.ifBlank { item.description?.cleanSearchText().orEmpty() },
            modifier = Modifier.padding(top = 8.dp),
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

private fun String.cleanSearchText(): String = replace(Regex("<[^>]+>"), "")
