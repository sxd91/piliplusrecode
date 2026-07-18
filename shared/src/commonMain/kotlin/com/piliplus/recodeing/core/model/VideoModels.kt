package com.piliplus.recodeing.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BiliResponse<T>(
    val code: Int = -1,
    val message: String = "",
    val ttl: Int = 0,
    val data: T? = null,
)

@Serializable
data class RecommendFeedData(
    val item: List<RecommendItem> = emptyList(),
)

@Serializable
data class PopularFeedData(
    val list: List<RecommendItem> = emptyList(),
    @SerialName("no_more") val noMore: Boolean = false,
)

@Serializable
data class RecommendItem(
    val id: Long? = null,
    val aid: Long? = null,
    val bvid: String? = null,
    val cid: Long? = null,
    val goto: String? = null,
    val uri: String? = null,
    val pic: String? = null,
    val title: String = "",
    val duration: Long = 0,
    val pubdate: Long? = null,
    val desc: String? = null,
    val owner: BiliOwner? = null,
    val stat: BiliStat? = null,
    val tname: String? = null,
    @SerialName("rcmd_reason") val recommendReason: RecommendReason? = null,
)

@Serializable
data class RecommendReason(
    val content: String? = null,
)

@Serializable
data class BiliOwner(
    val mid: Long? = null,
    val name: String? = null,
    val face: String? = null,
)

@Serializable
data class BiliStat(
    val view: Long? = null,
    val like: Long? = null,
    val danmaku: Long? = null,
    val reply: Long? = null,
    val favorite: Long? = null,
    val coin: Long? = null,
    val share: Long? = null,
)

@Serializable
data class NavUserInfo(
    @SerialName("isLogin") val isLogin: Boolean = false,
    val mid: Long? = null,
    val uname: String? = null,
    val face: String? = null,
    val money: Double? = null,
    val levelInfo: LevelInfo? = null,
)

@Serializable
data class LevelInfo(
    @SerialName("current_level") val currentLevel: Int = 0,
)
