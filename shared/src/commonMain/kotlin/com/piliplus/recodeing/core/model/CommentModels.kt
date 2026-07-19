package com.piliplus.recodeing.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoRelation(
    val attention: Boolean = false,
    val favorite: Boolean = false,
    @SerialName("season_fav") val seasonFavorite: Boolean = false,
    val like: Boolean = false,
    val dislike: Boolean = false,
    val coin: Int = 0,
)

enum class CommentSort(val title: String) {
    Hot("热门"),
    Newest("最新"),
    MostLiked("最多赞"),
    MostReplies("最多回复"),
}

@Serializable
data class CommentData(
    val cursor: CommentCursor? = null,
    val page: CommentPage? = null,
    val replies: List<CommentItem>? = null,
    val hots: List<CommentItem>? = null,
    @SerialName("top_replies") val topReplies: List<CommentItem>? = null,
    val top: CommentTop? = null,
    val upper: CommentUpper? = null,
    val root: CommentItem? = null,
)

@Serializable
data class CommentCursor(
    @SerialName("all_count") val allCount: Int = 0,
    @SerialName("is_end") val isEnd: Boolean = false,
    val next: Long = 0,
)

@Serializable
data class CommentPage(
    val num: Int = 0,
    val size: Int = 0,
    val count: Int = 0,
    val acount: Int = 0,
)

@Serializable
data class CommentTop(
    val upper: CommentItem? = null,
    val admin: CommentItem? = null,
    val vote: CommentItem? = null,
)

@Serializable
data class CommentUpper(
    val top: CommentItem? = null,
)

@Serializable
data class CommentItem(
    val rpid: Long = 0,
    val root: Long = 0,
    val parent: Long = 0,
    val dialog: Long = 0,
    val oid: Long = 0,
    val mid: Long = 0,
    val count: Int = 0,
    val rcount: Int = 0,
    val like: Int = 0,
    val ctime: Long = 0,
    val action: Int = 0,
    val member: CommentMember? = null,
    val content: CommentContent = CommentContent(),
    val replies: List<CommentItem>? = null,
    @SerialName("reply_control") val replyControl: CommentReplyControl? = null,
)

@Serializable
data class CommentMember(
    val mid: String = "",
    val uname: String = "",
    val avatar: String? = null,
)

@Serializable
data class CommentContent(
    val message: String = "",
    val pictures: List<CommentPicture>? = null,
)

@Serializable
data class CommentPicture(
    @SerialName("img_src") val imageUrl: String? = null,
    @SerialName("img_width") val width: Double = 0.0,
    @SerialName("img_height") val height: Double = 0.0,
)

@Serializable
data class CommentReplyControl(
    val location: String? = null,
)

@Serializable
data class AddCommentData(
    val rpid: Long = 0,
    @SerialName("rpid_str") val rpidString: String = "",
    val dialog: Long = 0,
    val root: Long = 0,
    val parent: Long = 0,
    val reply: CommentItem? = null,
)
