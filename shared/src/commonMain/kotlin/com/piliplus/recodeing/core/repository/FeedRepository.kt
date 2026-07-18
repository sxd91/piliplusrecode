package com.piliplus.recodeing.core.repository

import com.piliplus.recodeing.core.model.RecommendItem
import com.piliplus.recodeing.core.network.BiliApiService

class FeedRepository(
    private val service: BiliApiService = BiliApiService(),
) {
    suspend fun loadRecommendations(pageSize: Int = 20, freshIndex: Int = 0): Result<List<RecommendItem>> {
        return runCatching {
            val response = service.recommendations(pageSize, freshIndex)
            if (response.code != 0) error(response.message.ifBlank { "推荐接口异常：${response.code}" })
            response.data?.item.orEmpty().filter { item -> item.goto == null || item.goto == "av" }
        }
    }

    suspend fun loadPopular(page: Int = 1, pageSize: Int = 20): Result<List<RecommendItem>> {
        return runCatching {
            val response = service.popular(page, pageSize)
            if (response.code != 0) error(response.message.ifBlank { "热门接口异常：${response.code}" })
            response.data?.list.orEmpty()
        }
    }
}
