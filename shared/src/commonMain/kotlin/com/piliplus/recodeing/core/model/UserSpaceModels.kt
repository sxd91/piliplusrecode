package com.piliplus.recodeing.core.model

import kotlinx.serialization.Serializable

@Serializable
data class UserSpaceProfile(
    val mid: Long = 0,
    val name: String = "",
    val face: String? = null,
    val sign: String = "",
)

@Serializable
data class UserRelationStat(
    val following: Long = 0,
    val follower: Long = 0,
)

@Serializable
data class UserVideoSearchData(
    val page: UserVideoPage = UserVideoPage(),
    val list: UserVideoList = UserVideoList(),
)

@Serializable
data class UserVideoPage(
    val count: Int = 0,
)

@Serializable
data class UserVideoList(
    val vlist: List<UserVideoItem> = emptyList(),
)

@Serializable
data class UserVideoItem(
    val bvid: String = "",
    val title: String = "",
    val pic: String? = null,
    val description: String = "",
    val play: Long = 0,
    val comment: Long = 0,
    val length: String = "",
    val created: Long = 0,
    val typeName: String? = null,
)
