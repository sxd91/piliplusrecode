package com.piliplus.recodeing.core.network

import com.piliplus.recodeing.core.model.BiliResponse
import com.piliplus.recodeing.core.model.NavUserInfo
import com.piliplus.recodeing.core.model.PopularFeedData
import com.piliplus.recodeing.core.model.RecommendFeedData
import com.piliplus.recodeing.core.model.SearchAllData
import com.piliplus.recodeing.core.model.SearchDefaultData
import com.piliplus.recodeing.core.model.SearchSuggestEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class BiliApiService(
    private val client: HttpClient = createBiliHttpClient(),
    private val wbiSigner: WbiSigner = WbiSigner(),
) {
    suspend fun recommendations(pageSize: Int, freshIndex: Int): BiliResponse<RecommendFeedData> {
        val params = wbiSigner.sign(
            mapOf(
                "version" to 1,
                "feed_version" to "V8",
                "homepage_ver" to 1,
                "ps" to pageSize,
                "fresh_idx" to freshIndex,
                "brush" to freshIndex,
                "fresh_type" to 4,
            ),
        )
        return client.get(BiliApiConstants.RECOMMEND_LIST_WEB) {
            params.forEach { (key, value) -> parameter(key, value) }
        }.body()
    }

    suspend fun popular(page: Int, pageSize: Int): BiliResponse<PopularFeedData> {
        return client.get(BiliApiConstants.HOT_LIST) {
            parameter("pn", page)
            parameter("ps", pageSize)
        }.body()
    }

    suspend fun navUserInfo(): BiliResponse<NavUserInfo> {
        return client.get(BiliApiConstants.USER_INFO).body()
    }

    suspend fun searchDefault(): BiliResponse<SearchDefaultData> {
        val params = wbiSigner.sign(
            mapOf("web_location" to 1430654),
        )
        return client.get(BiliApiConstants.SEARCH_DEFAULT) {
            params.forEach { (key, value) -> parameter(key, value) }
        }.body()
    }

    suspend fun searchAll(keyword: String, page: Int = 1): BiliResponse<SearchAllData> {
        val params = wbiSigner.sign(
            mapOf(
                "keyword" to keyword,
                "page" to page,
            ),
        )
        return client.get(BiliApiConstants.SEARCH_ALL) {
            params.forEach { (key, value) -> parameter(key, value) }
        }.body()
    }

    suspend fun searchSuggest(term: String): SearchSuggestEnvelope {
        return client.get(BiliApiConstants.SEARCH_SUGGEST_URL) {
            parameter("term", term)
            parameter("main_ver", "v1")
            parameter("highlight", term)
        }.body()
    }
}
