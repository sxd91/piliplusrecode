package com.piliplus.recodeing.core.repository

import com.piliplus.recodeing.core.cdn.withCdn
import com.piliplus.recodeing.core.model.RecommendItem
import com.piliplus.recodeing.core.model.VideoDetail
import com.piliplus.recodeing.core.model.VideoPlayUrl
import com.piliplus.recodeing.core.model.VideoRelation
import com.piliplus.recodeing.core.network.BiliApiService

class VideoRepository(
    private val service: BiliApiService = BiliApiService(),
) {
    suspend fun detail(bvid: String): Result<VideoDetail> = runCatching {
        val response = service.videoDetail(bvid)
        require(response.code == 0) { response.message.ifBlank { "视频详情加载失败" } }
        requireNotNull(response.data) { "视频详情为空" }
    }

    suspend fun relation(aid: Long): Result<VideoRelation> = runCatching {
        val response = service.videoRelation(aid)
        require(response.code == 0) { response.message.ifBlank { "互动状态加载失败" } }
        requireNotNull(response.data) { "互动状态为空" }
    }

    suspend fun related(bvid: String): Result<List<RecommendItem>> = runCatching {
        val response = service.relatedVideos(bvid)
        require(response.code == 0) { response.message.ifBlank { "相关推荐加载失败" } }
        response.data.orEmpty()
    }

    suspend fun playUrl(
        bvid: String,
        cid: Long,
        quality: Int = 80,
        cdnEndpoint: String = "auto",
        rewriteAudioCdn: Boolean = true,
    ): Result<VideoPlayUrl> = runCatching {
        val response = service.videoPlayUrl(bvid, cid, quality)
        require(response.code == 0) { response.message.ifBlank { "播放地址加载失败" } }
        requireNotNull(response.data) { "播放地址为空" }.withCdn(
            endpointValue = cdnEndpoint,
            rewriteAudio = rewriteAudioCdn,
        )
    }

    suspend fun like(aid: Long, like: Boolean, csrf: String): Result<Unit> = runCatching {
        require(csrf.isNotBlank()) { "登录会话缺少 bili_jct" }
        val response = service.likeVideo(aid = aid, like = like, csrf = csrf)
        require(response.code == 0) { response.message.ifBlank { "点赞失败" } }
    }

    suspend fun coin(aid: Long, count: Int, alsoLike: Boolean, csrf: String): Result<Unit> = runCatching {
        require(csrf.isNotBlank()) { "登录会话缺少 bili_jct" }
        val response = service.coinVideo(aid = aid, count = count, alsoLike = alsoLike, csrf = csrf)
        require(response.code == 0) { response.message.ifBlank { "投币失败" } }
    }
}
