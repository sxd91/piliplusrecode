package com.piliplus.recodeing.core.repository

import com.piliplus.recodeing.core.model.CommentData
import com.piliplus.recodeing.core.model.CommentItem
import com.piliplus.recodeing.core.model.CommentSort
import com.piliplus.recodeing.core.network.BiliApiService

class CommentRepository(
    private val service: BiliApiService,
) {
    suspend fun comments(
        aid: Long,
        sort: CommentSort,
        page: Int,
        cursor: Long? = null,
    ): Result<CommentPageResult> = runCatching {
        val response = service.comments(aid = aid, sort = sort, page = page, cursor = cursor)
        require(response.code == 0) { commentErrorMessage(response.code, response.message) }
        val data = response.data ?: CommentData()
        val items = buildList {
            if (page == 1) {
                add(data.top?.upper)
                add(data.top?.admin)
                add(data.top?.vote)
                add(data.upper?.top)
                addAll(data.topReplies.orEmpty())
                addAll(data.hots.orEmpty())
            }
            addAll(data.replies.orEmpty())
        }.filterNotNull().distinctBy(CommentItem::rpid)
        val endReached = data.cursor?.isEnd
            ?: data.page?.let { pageInfo -> pageInfo.num * pageInfo.size >= pageInfo.count }
            ?: items.isEmpty()
        CommentPageResult(
            items = items,
            totalCount = data.cursor?.allCount ?: data.page?.acount ?: data.page?.count ?: 0,
            endReached = endReached,
            nextCursor = data.cursor?.next,
        )
    }

    suspend fun replies(aid: Long, root: Long, page: Int): Result<CommentThreadPage> = runCatching {
        val response = service.commentReplies(aid = aid, root = root, page = page)
        require(response.code == 0) { commentErrorMessage(response.code, response.message) }
        val data = response.data ?: CommentData()
        val replies = data.replies.orEmpty().distinctBy(CommentItem::rpid)
        val pageInfo = data.page
        CommentThreadPage(
            root = data.root,
            replies = replies,
            totalCount = pageInfo?.count ?: data.root?.rcount ?: data.root?.count ?: replies.size,
            endReached = pageInfo?.let { it.num * it.size >= it.count } ?: replies.isEmpty(),
        )
    }

    suspend fun like(aid: Long, rpid: Long, like: Boolean, csrf: String): Result<Unit> = runCatching {
        require(csrf.isNotBlank()) { "登录会话缺少 bili_jct" }
        val response = service.reactToComment(aid = aid, rpid = rpid, like = like, csrf = csrf)
        require(response.code == 0) { commentErrorMessage(response.code, response.message) }
    }

    suspend fun hate(aid: Long, rpid: Long, hate: Boolean, csrf: String): Result<Unit> = runCatching {
        require(csrf.isNotBlank()) { "登录会话缺少 bili_jct" }
        val response = service.hateComment(aid = aid, rpid = rpid, hate = hate, csrf = csrf)
        require(response.code == 0) { commentErrorMessage(response.code, response.message) }
    }

    suspend fun add(
        aid: Long,
        message: String,
        csrf: String,
        root: Long? = null,
        parent: Long? = null,
    ): Result<CommentItem?> = runCatching {
        require(csrf.isNotBlank()) { "请先登录" }
        require(message.isNotBlank()) { "评论内容不能为空" }
        val response = service.addComment(
            aid = aid,
            message = message.trim(),
            csrf = csrf,
            root = root,
            parent = parent,
        )
        require(response.code == 0) { commentErrorMessage(response.code, response.message) }
        response.data?.reply
    }
}

data class CommentPageResult(
    val items: List<CommentItem>,
    val totalCount: Int,
    val endReached: Boolean,
    val nextCursor: Long?,
)

data class CommentThreadPage(
    val root: CommentItem?,
    val replies: List<CommentItem>,
    val totalCount: Int,
    val endReached: Boolean,
)

private fun commentErrorMessage(code: Int, message: String): String = when (code) {
    -101 -> "登录状态失效"
    -352, -509 -> "操作过于频繁，请稍后再试"
    -111 -> "请求签名验证失败"
    -400 -> "评论请求参数错误"
    -412 -> "评论请求被风控拦截"
    12002 -> "该视频已关闭评论"
    12009 -> "评论已被删除"
    12015 -> "评论需要完成验证码"
    12016 -> "评论包含敏感内容"
    12025 -> "评论内容过长"
    12035 -> "你已被 UP 主屏蔽"
    12051 -> "请勿重复发送相同评论"
    else -> message.ifBlank { "评论请求失败（$code）" }
}
