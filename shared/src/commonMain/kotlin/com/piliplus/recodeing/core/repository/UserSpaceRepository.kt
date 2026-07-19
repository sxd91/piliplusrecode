package com.piliplus.recodeing.core.repository

import com.piliplus.recodeing.core.model.UserRelationStat
import com.piliplus.recodeing.core.model.UserSpaceProfile
import com.piliplus.recodeing.core.model.UserVideoItem
import com.piliplus.recodeing.core.network.BiliApiService

class UserSpaceRepository(
    private val service: BiliApiService = BiliApiService(),
) {
    suspend fun profile(mid: Long): Result<UserSpaceProfile> = runCatching {
        val response = service.userSpaceProfile(mid)
        require(response.code == 0) { response.message.ifBlank { "用户资料加载失败" } }
        requireNotNull(response.data) { "用户资料为空" }
    }

    suspend fun relation(mid: Long): Result<UserRelationStat> = runCatching {
        val response = service.userRelationStat(mid)
        require(response.code == 0) { response.message.ifBlank { "关注数据加载失败" } }
        requireNotNull(response.data) { "关注数据为空" }
    }

    suspend fun videos(mid: Long, page: Int = 1): Result<List<UserVideoItem>> = runCatching {
        val response = service.userSpaceVideos(mid = mid, page = page)
        require(response.code == 0) { response.message.ifBlank { "投稿视频加载失败" } }
        response.data?.list?.vlist.orEmpty()
    }
}
