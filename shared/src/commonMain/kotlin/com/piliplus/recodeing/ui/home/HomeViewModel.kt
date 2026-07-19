package com.piliplus.recodeing.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piliplus.recodeing.core.model.RecommendItem
import com.piliplus.recodeing.core.repository.FeedRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val selectedTab: HomeTab = HomeTab.Recommend,
    val recommendItems: List<RecommendItem> = emptyList(),
    val popularItems: List<RecommendItem> = emptyList(),
    val recommendFreshIndex: Int = 0,
    val popularPage: Int = 0,
    val recommendHasMore: Boolean = true,
    val popularHasMore: Boolean = true,
    val minimumDurationMinutes: Int = 0,
    val publishedWithinDays: Int = 0,
    val error: String? = null,
)

enum class HomeTab(val title: String) {
    Recommend("推荐"),
    Popular("热门"),
}

class HomeViewModel(
    private val feedRepository: FeedRepository = FeedRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var feedJob: Job? = null
    private var feedGeneration = 0L

    init {
        refresh()
    }

    fun selectTab(tab: HomeTab) {
        _uiState.update { it.copy(selectedTab = tab, error = null) }
        val current = _uiState.value
        if (tab == HomeTab.Popular && current.popularItems.isEmpty()) refresh()
    }

    fun setDurationFilter(minutes: Int) {
        _uiState.update { it.copy(minimumDurationMinutes = minutes.coerceAtLeast(0)) }
    }

    fun setPublishedFilter(days: Int) {
        _uiState.update { it.copy(publishedWithinDays = days.coerceAtLeast(0)) }
    }

    fun refresh() {
        feedJob?.cancel()
        val tab = _uiState.value.selectedTab
        val generation = ++feedGeneration
        feedJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isLoadingMore = false, error = null) }
            loadFeed(tab = tab, reset = true, generation = generation)
        }
    }

    fun loadMore() {
        val current = _uiState.value
        if (current.isLoading || current.isLoadingMore) return
        val hasMore = when (current.selectedTab) {
            HomeTab.Recommend -> current.recommendHasMore
            HomeTab.Popular -> current.popularHasMore
        }
        if (!hasMore) return
        val generation = feedGeneration
        feedJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, error = null) }
            loadFeed(tab = current.selectedTab, reset = false, generation = generation)
        }
    }

    private suspend fun loadFeed(tab: HomeTab, reset: Boolean, generation: Long) {
        val snapshot = _uiState.value
        val requestIndex = when (tab) {
            HomeTab.Recommend -> if (reset) 0 else snapshot.recommendFreshIndex + 1
            HomeTab.Popular -> if (reset) 1 else snapshot.popularPage + 1
        }
        val result = when (tab) {
            HomeTab.Recommend -> feedRepository.loadRecommendations(freshIndex = requestIndex)
            HomeTab.Popular -> feedRepository.loadPopular(page = requestIndex)
        }
        if (generation != feedGeneration) return
        result.fold(
            onSuccess = { items ->
                _uiState.update { state ->
                    when (tab) {
                        HomeTab.Recommend -> state.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            recommendItems = mergeFeed(if (reset) emptyList() else state.recommendItems, items),
                            recommendFreshIndex = requestIndex,
                            recommendHasMore = items.isNotEmpty(),
                        )
                        HomeTab.Popular -> state.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            popularItems = mergeFeed(if (reset) emptyList() else state.popularItems, items),
                            popularPage = requestIndex,
                            popularHasMore = items.isNotEmpty(),
                        )
                    }
                }
            },
            onFailure = { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = throwable.message ?: "加载失败",
                    )
                }
            },
        )
    }

    private fun mergeFeed(current: List<RecommendItem>, incoming: List<RecommendItem>): List<RecommendItem> {
        return (current + incoming).distinctBy { item ->
            item.bvid ?: item.aid ?: item.id ?: "${item.title}-${item.owner?.mid}"
        }.takeLast(500)
    }
}
