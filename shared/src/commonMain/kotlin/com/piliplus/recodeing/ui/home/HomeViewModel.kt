package com.piliplus.recodeing.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piliplus.recodeing.core.model.RecommendItem
import com.piliplus.recodeing.core.model.SearchResultItem
import com.piliplus.recodeing.core.model.SearchSuggestItem
import com.piliplus.recodeing.core.repository.FeedRepository
import com.piliplus.recodeing.core.repository.SearchRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val selectedTab: HomeTab = HomeTab.Recommend,
    val recommendItems: List<RecommendItem> = emptyList(),
    val popularItems: List<RecommendItem> = emptyList(),
    val searchQuery: String = "",
    val searchDefault: String = "搜索 Bilibili",
    val searchSuggestions: List<SearchSuggestItem> = emptyList(),
    val searchResults: List<SearchResultItem> = emptyList(),
    val error: String? = null,
)

enum class HomeTab(val title: String) {
    Recommend("推荐"),
    Popular("热门"),
    Search("搜索"),
}

private const val SearchSuggestionDebounceMillis = 250L

class HomeViewModel(
    private val feedRepository: FeedRepository = FeedRepository(),
    private val searchRepository: SearchRepository = SearchRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var suggestJob: Job? = null

    init {
        refresh()
        loadSearchDefault()
    }

    fun selectTab(tab: HomeTab) {
        _uiState.update { it.copy(selectedTab = tab, error = null) }
        val state = _uiState.value
        if (tab == HomeTab.Popular && state.popularItems.isEmpty()) {
            refresh()
        }
    }

    fun updateSearchQuery(query: String) {
        suggestJob?.cancel()
        val keyword = query.trim()
        _uiState.update {
            it.copy(
                searchQuery = query,
                searchSuggestions = if (keyword.isEmpty()) emptyList() else it.searchSuggestions,
            )
        }
        if (keyword.isEmpty()) return

        suggestJob = viewModelScope.launch {
            delay(SearchSuggestionDebounceMillis)
            searchRepository.suggest(keyword).onSuccess { suggestions ->
                _uiState.update { state ->
                    if (state.searchQuery.trim() == keyword) {
                        state.copy(searchSuggestions = suggestions)
                    } else {
                        state
                    }
                }
            }
        }
    }

    fun submitSearch(query: String = _uiState.value.searchQuery) {
        val keyword = query.ifBlank { _uiState.value.searchDefault }.trim()
        if (keyword.isBlank()) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedTab = HomeTab.Search,
                    searchQuery = keyword,
                    isLoading = true,
                    error = null,
                    searchSuggestions = emptyList(),
                )
            }
            searchRepository.search(keyword).fold(
                onSuccess = { results ->
                    _uiState.update { it.copy(isLoading = false, searchResults = results) }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, error = throwable.message ?: "搜索失败")
                    }
                },
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val tab = _uiState.value.selectedTab) {
                HomeTab.Recommend,
                HomeTab.Popular,
                -> {
                    val result = when (tab) {
                        HomeTab.Recommend -> feedRepository.loadRecommendations()
                        HomeTab.Popular -> feedRepository.loadPopular()
                        HomeTab.Search -> error("unreachable")
                    }
                    result.fold(
                        onSuccess = { items ->
                            _uiState.update { state ->
                                when (tab) {
                                    HomeTab.Recommend -> state.copy(
                                        isLoading = false,
                                        recommendItems = items,
                                    )
                                    HomeTab.Popular -> state.copy(
                                        isLoading = false,
                                        popularItems = items,
                                    )
                                    HomeTab.Search -> state
                                }
                            }
                        },
                        onFailure = { throwable ->
                            _uiState.update {
                                it.copy(isLoading = false, error = throwable.message ?: "加载失败")
                            }
                        },
                    )
                }
                HomeTab.Search -> submitSearch()
            }
        }
    }

    private fun loadSearchDefault() {
        viewModelScope.launch {
            searchRepository.loadDefaultKeyword().onSuccess { data ->
                _uiState.update {
                    it.copy(searchDefault = data.showName ?: data.name ?: it.searchDefault)
                }
            }
        }
    }
}
