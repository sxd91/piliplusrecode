package com.piliplus.recodeing.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DynamicFeedData(
    val items: List<DynamicItem> = emptyList(),
    @SerialName("has_more") val hasMore: Boolean = false,
    val offset: String = "",
)

@Serializable
data class DynamicItem(
    val idStr: String = "",
    val type: String = "",
    val modules: DynamicModules? = null,
)

@Serializable
data class DynamicModules(
    @SerialName("module_author") val author: DynamicAuthor? = null,
    @SerialName("module_dynamic") val dynamic: DynamicContent? = null,
)

@Serializable
data class DynamicAuthor(
    val name: String = "",
    val face: String = "",
    val mid: Long = 0,
    @SerialName("pub_time") val publishTime: String = "",
)

@Serializable
data class DynamicContent(
    val desc: DynamicDescription? = null,
    val major: DynamicMajor? = null,
)

@Serializable
data class DynamicDescription(
    val text: String = "",
)

@Serializable
data class DynamicMajor(
    val archive: DynamicArchive? = null,
)

@Serializable
data class DynamicArchive(
    val bvid: String = "",
    val title: String = "",
    val desc: String = "",
    val cover: String = "",
)
