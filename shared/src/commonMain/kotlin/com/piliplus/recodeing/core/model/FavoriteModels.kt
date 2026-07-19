package com.piliplus.recodeing.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteFolderList(
    val count: Int = 0,
    val list: List<FavoriteFolder>? = null,
)

@Serializable
data class FavoriteFolder(
    val id: Long = 0,
    val fid: Long = 0,
    val mid: Long = 0,
    val title: String = "",
    val cover: String? = null,
    @SerialName("media_count") val mediaCount: Int = 0,
    @SerialName("fav_state") val favoriteState: Int = 0,
    val type: Int = 0,
) {
    val mediaId: Long
        get() = id.takeIf { it > 0 } ?: fid.takeIf { it > 0 } ?: 0
}
