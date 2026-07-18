package com.piliplus.recodeing.core.repository

import com.piliplus.recodeing.core.model.SearchDefaultData
import com.piliplus.recodeing.core.model.SearchResultItem
import com.piliplus.recodeing.core.model.SearchSuggestItem
import com.piliplus.recodeing.core.network.BiliApiService

class SearchRepository(
    private val apiService: BiliApiService = BiliApiService(),
) {
    suspend fun loadDefaultKeyword(): Result<SearchDefaultData> = runCatching {
        val response = apiService.searchDefault()
        require(response.code == 0) { response.message.ifBlank { "搜索默认词加载失败" } }
        response.data ?: SearchDefaultData()
    }

    suspend fun search(keyword: String, page: Int = 1): Result<List<SearchResultItem>> = runCatching {
        val response = apiService.searchAll(keyword = keyword, page = page)
        require(response.code == 0) { response.message.ifBlank { "搜索失败" } }
        response.data
            ?.result
            .orEmpty()
            .flatMap { section -> section.data }
            .filter { it.title.isNotBlank() }
    }

    suspend fun suggest(term: String): Result<List<SearchSuggestItem>> = runCatching {
        if (term.isBlank()) return@runCatching emptyList()
        val response = apiService.searchSuggest(term)
        require(response.code == 0) { "搜索建议加载失败" }
        response.result?.tag.orEmpty()
    }
}
