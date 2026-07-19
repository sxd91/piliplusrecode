package com.piliplus.recodeing.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piliplus.recodeing.core.model.SearchResultItem
import com.piliplus.recodeing.core.model.SearchSuggestItem
import com.piliplus.recodeing.core.repository.SearchRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val SuggestionDebounceMillis = 300L

data class SearchUiState(
    val query: String = "",
    val defaultKeyword: String = "搜索 Bilibili",
    val history: List<String> = emptyList(),
    val suggestions: List<SearchSuggestItem> = emptyList(),
    val results: List<SearchResultItem> = emptyList(),
    val page: Int = 0,
    val isSearching: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val minimumDurationMinutes: Int = 0,
    val error: String? = null,
)

class SearchViewModel(
    private val repository: SearchRepository = SearchRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()
    private var suggestionJob: Job? = null
    private var searchJob: Job? = null
    private var searchGeneration = 0L

    init {
        viewModelScope.launch {
            repository.loadDefaultKeyword().onSuccess { data ->
                _state.update { current ->
                    current.copy(defaultKeyword = data.showName ?: data.name ?: current.defaultKeyword)
                }
            }
        }
    }

    fun updateQuery(query: String) {
        suggestionJob?.cancel()
        val keyword = query.trim()
        _state.update {
            it.copy(
                query = query,
                suggestions = if (keyword.isBlank()) emptyList() else it.suggestions,
                error = null,
            )
        }
        if (keyword.isBlank()) return
        suggestionJob = viewModelScope.launch {
            delay(SuggestionDebounceMillis)
            repository.suggest(keyword).onSuccess { suggestions ->
                _state.update { current ->
                    if (current.query.trim() == keyword) current.copy(suggestions = suggestions) else current
                }
            }
        }
    }

    fun submit(query: String = _state.value.query) {
        val keyword = query.ifBlank { _state.value.defaultKeyword }.trim()
        if (keyword.isBlank()) return
        searchJob?.cancel()
        val generation = ++searchGeneration
        _state.update {
            it.copy(
                query = keyword,
                history = (listOf(keyword) + it.history.filterNot { item -> item == keyword }).take(10),
                results = emptyList(),
                suggestions = emptyList(),
                page = 0,
                isSearching = true,
                isLoadingMore = false,
                error = null,
            )
        }
        searchJob = viewModelScope.launch { loadPage(keyword, page = 1, generation = generation, append = false) }
    }

    fun loadMore() {
        val current = _state.value
        if (current.query.isBlank() || current.isSearching || current.isLoadingMore || !current.hasMore) return
        val generation = searchGeneration
        viewModelScope.launch {
            loadPage(current.query.trim(), current.page + 1, generation, append = true)
        }
    }

    fun setDurationFilter(minutes: Int) {
        _state.update { it.copy(minimumDurationMinutes = minutes.coerceAtLeast(0)) }
    }

    fun clearHistory() {
        _state.update { it.copy(history = emptyList()) }
    }

    private suspend fun loadPage(keyword: String, page: Int, generation: Long, append: Boolean) {
        _state.update {
            if (append) it.copy(isLoadingMore = true, error = null) else it
        }
        repository.search(keyword, page).fold(
            onSuccess = { items ->
                if (generation != searchGeneration) return@fold
                _state.update { current ->
                    val merged = if (append) current.results + items else items
                    current.copy(
                        results = merged.distinctBy { it.bvid ?: it.aid ?: it.mid ?: it.title },
                        page = page,
                        isSearching = false,
                        isLoadingMore = false,
                        hasMore = items.size >= 20,
                    )
                }
            },
            onFailure = { throwable ->
                if (generation != searchGeneration) return@fold
                _state.update {
                    it.copy(
                        isSearching = false,
                        isLoadingMore = false,
                        error = throwable.message ?: "搜索失败",
                    )
                }
            },
        )
    }
}
