package com.piliplus.recodeing.core.repository

import com.piliplus.recodeing.core.model.FavoriteFolder
import com.piliplus.recodeing.core.network.BiliApiService
import kotlinx.coroutines.CancellationException

class FavoriteRepository(
    private val service: BiliApiService,
) {
    suspend fun folders(mid: Long, aid: Long): Result<List<FavoriteFolder>> = suspendResult {
        require(mid > 0) { "账号 UID 无效" }
        require(aid > 0) { "视频 AV 号无效" }
        val response = service.favoriteFolders(mid = mid, aid = aid)
        require(response.code == 0) { response.message.ifBlank { "收藏夹加载失败" } }
        response.data?.list.orEmpty().filter { it.mediaId > 0 }
    }

    suspend fun update(
        aid: Long,
        originalFolderIds: Set<Long>,
        selectedFolderIds: Set<Long>,
        csrf: String,
    ): Result<Unit> = suspendResult {
        require(csrf.isNotBlank()) { "登录会话缺少 bili_jct" }
        val addIds = selectedFolderIds - originalFolderIds
        val removeIds = originalFolderIds - selectedFolderIds
        if (addIds.isEmpty() && removeIds.isEmpty()) return@suspendResult
        val response = service.updateFavoriteFolders(
            aid = aid,
            addFolderIds = addIds,
            removeFolderIds = removeIds,
            csrf = csrf,
        )
        require(response.code == 0) { response.message.ifBlank { "收藏夹更新失败" } }
    }

    suspend fun create(
        title: String,
        introduction: String,
        isPrivate: Boolean,
        csrf: String,
    ): Result<Unit> = suspendResult {
        require(title.isNotBlank()) { "收藏夹名称不能为空" }
        require(csrf.isNotBlank()) { "登录会话缺少 bili_jct" }
        val response = service.createFavoriteFolder(
            title = title.trim(),
            introduction = introduction.trim(),
            isPrivate = isPrivate,
            csrf = csrf,
        )
        require(response.code == 0) { response.message.ifBlank { "收藏夹创建失败" } }
    }
}

private suspend inline fun <T> suspendResult(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (error: CancellationException) {
    throw error
} catch (error: Throwable) {
    Result.failure(error)
}
