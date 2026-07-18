package com.piliplus.recodeing.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SearchAllData(
    val result: List<SearchResultSection> = emptyList(),
)

@Serializable
data class SearchResultSection(
    @SerialName("result_type") val resultType: String = "",
    val data: List<SearchResultItem> = emptyList(),
)

@Serializable
data class SearchResultItem(
    val title: String = "",
    val bvid: String? = null,
    val aid: Long? = null,
    val mid: Long? = null,
    val author: String? = null,
    val uname: String? = null,
    val type: String? = null,
    val arcurl: String? = null,
    val pic: String? = null,
    val cover: String? = null,
    val duration: String? = null,
    val play: Long? = null,
    val danmaku: Long? = null,
    val favorites: Long? = null,
    val description: String? = null,
    @SerialName("pubdate") val pubDate: Long? = null,
)

@Serializable
data class SearchDefaultData(
    @SerialName("show_name") val showName: String? = null,
    val name: String? = null,
)

@Serializable
data class SearchSuggestEnvelope(
    val code: Int = -1,
    val result: SearchSuggestResult? = null,
)

@Serializable
data class SearchSuggestResult(
    val tag: List<SearchSuggestItem> = emptyList(),
)

@Serializable
data class SearchSuggestItem(
    val value: String = "",
    val term: String? = null,
    val name: String? = null,
    val ref: Int? = null,
    val spid: Int? = null,
    val extra: JsonElement? = null,
)
