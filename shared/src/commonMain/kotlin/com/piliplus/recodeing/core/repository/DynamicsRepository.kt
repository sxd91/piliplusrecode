package com.piliplus.recodeing.core.repository

import com.piliplus.recodeing.core.model.DynamicFeedData
import com.piliplus.recodeing.core.network.BiliApiService

class DynamicsRepository(
    private val service: BiliApiService,
) {
    suspend fun load(offset: String = ""): Result<DynamicFeedData> = runCatching {
        val response = service.dynamics(offset)
        require(response.code == 0) { response.message.ifBlank { "动态加载失败" } }
        requireNotNull(response.data) { "动态响应为空" }
    }
}
