package com.piliplus.recodeing.core.network

import com.piliplus.recodeing.core.model.BiliResponse
import com.piliplus.recodeing.core.model.DynamicFeedData
import com.piliplus.recodeing.core.model.NavUserInfo
import com.piliplus.recodeing.core.model.PopularFeedData
import com.piliplus.recodeing.core.model.RecommendFeedData
import com.piliplus.recodeing.core.model.RecommendItem
import com.piliplus.recodeing.core.model.SearchAllData
import com.piliplus.recodeing.core.model.SearchDefaultData
import com.piliplus.recodeing.core.model.SearchSuggestEnvelope
import com.piliplus.recodeing.core.model.TvQrCodeData
import com.piliplus.recodeing.core.model.TvQrLoginData
import com.piliplus.recodeing.core.model.UserRelationStat
import com.piliplus.recodeing.core.model.UserSpaceProfile
import com.piliplus.recodeing.core.model.UserVideoSearchData
import com.piliplus.recodeing.core.model.VideoDetail
import com.piliplus.recodeing.core.model.VideoPlayUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters

class BiliApiService(
    cookieHeaderProvider: () -> String = { "" },
    private val client: HttpClient = createBiliHttpClient(cookieHeaderProvider),
    private val wbiSigner: WbiSigner = WbiSigner(client),
) {
    suspend fun recommendations(pageSize: Int, freshIndex: Int): BiliResponse<RecommendFeedData> {
        val params = wbiSigner.sign(
            mapOf(
                "version" to 1,
                "feed_version" to "V8",
                "homepage_ver" to 1,
                "ps" to pageSize,
                "fresh_idx" to freshIndex,
                "brush" to freshIndex,
                "fresh_type" to 4,
            ),
        )
        return client.get(BiliApiConstants.RECOMMEND_LIST_WEB) {
            params.forEach { (key, value) -> parameter(key, value) }
        }.body()
    }

    suspend fun popular(page: Int, pageSize: Int): BiliResponse<PopularFeedData> {
        return client.get(BiliApiConstants.HOT_LIST) {
            parameter("pn", page)
            parameter("ps", pageSize)
        }.body()
    }

    suspend fun navUserInfo(): BiliResponse<NavUserInfo> {
        return client.get(BiliApiConstants.USER_INFO).body()
    }

    suspend fun createTvQrCode(): BiliResponse<TvQrCodeData> {
        return client.submitForm(
            url = BiliApiConstants.TV_QR_CREATE,
            formParameters = tvSignedParameters(),
        ) {
            url {
                protocol = io.ktor.http.URLProtocol.HTTPS
                host = "passport.bilibili.com"
            }
        }.body()
    }

    suspend fun pollTvQrCode(authCode: String): BiliResponse<TvQrLoginData> {
        return client.submitForm(
            url = BiliApiConstants.TV_QR_POLL,
            formParameters = tvSignedParameters(mapOf("auth_code" to authCode)),
        ) {
            url {
                protocol = io.ktor.http.URLProtocol.HTTPS
                host = "passport.bilibili.com"
            }
        }.body()
    }

    suspend fun dynamics(offset: String = ""): BiliResponse<DynamicFeedData> {
        return client.get(BiliApiConstants.DYNAMIC_FEED) {
            parameter("timezone_offset", -480)
            parameter("type", "all")
            if (offset.isNotBlank()) parameter("offset", offset)
        }.body()
    }

    suspend fun userSpaceProfile(mid: Long): BiliResponse<UserSpaceProfile> {
        val params = wbiSigner.sign(mapOf("mid" to mid))
        return client.get(BiliApiConstants.USER_SPACE_PROFILE) {
            params.forEach { (key, value) -> parameter(key, value) }
        }.body()
    }

    suspend fun userRelationStat(mid: Long): BiliResponse<UserRelationStat> {
        return client.get(BiliApiConstants.USER_RELATION_STAT) {
            parameter("vmid", mid)
        }.body()
    }

    suspend fun userSpaceVideos(mid: Long, page: Int = 1, pageSize: Int = 20): BiliResponse<UserVideoSearchData> {
        val params = wbiSigner.sign(
            mapOf(
                "mid" to mid,
                "pn" to page,
                "ps" to pageSize,
                "order" to "pubdate",
            ),
        )
        return client.get(BiliApiConstants.USER_SPACE_VIDEOS) {
            params.forEach { (key, value) -> parameter(key, value) }
        }.body()
    }

    suspend fun searchDefault(): BiliResponse<SearchDefaultData> {
        val params = wbiSigner.sign(
            mapOf("web_location" to 1430654),
        )
        return client.get(BiliApiConstants.SEARCH_DEFAULT) {
            params.forEach { (key, value) -> parameter(key, value) }
        }.body()
    }

    suspend fun searchAll(keyword: String, page: Int = 1): BiliResponse<SearchAllData> {
        val params = wbiSigner.sign(
            mapOf(
                "keyword" to keyword,
                "page" to page,
            ),
        )
        return client.get(BiliApiConstants.SEARCH_ALL) {
            params.forEach { (key, value) -> parameter(key, value) }
        }.body()
    }

    suspend fun searchSuggest(term: String): SearchSuggestEnvelope {
        return client.get(BiliApiConstants.SEARCH_SUGGEST_URL) {
            parameter("term", term)
            parameter("main_ver", "v1")
            parameter("highlight", term)
        }.body()
    }

    suspend fun videoDetail(bvid: String): BiliResponse<VideoDetail> {
        return client.get(BiliApiConstants.VIDEO_VIEW) {
            parameter("bvid", bvid)
        }.body()
    }

    suspend fun relatedVideos(bvid: String): BiliResponse<List<RecommendItem>> {
        return client.get(BiliApiConstants.VIDEO_RELATED) {
            parameter("bvid", bvid)
        }.body()
    }

    suspend fun videoPlayUrl(
        bvid: String,
        cid: Long,
        quality: Int = 80,
        tryLook: Boolean = true,
    ): BiliResponse<VideoPlayUrl> {
        val params = wbiSigner.sign(
            mapOf(
                "bvid" to bvid,
                "cid" to cid,
                "qn" to quality,
                "fnval" to 4048,
                "fnver" to 0,
                "fourk" to 1,
                "try_look" to if (tryLook) 1 else null,
                "web_location" to 1315873,
            ),
        )
        return client.get(BiliApiConstants.VIDEO_PLAY_URL) {
            params.forEach { (key, value) -> parameter(key, value) }
        }.body()
    }

    suspend fun likeVideo(aid: Long, like: Boolean, csrf: String): BiliResponse<kotlinx.serialization.json.JsonElement> {
        return client.submitForm(
            url = BiliApiConstants.VIDEO_LIKE,
            formParameters = Parameters.build {
                append("aid", aid.toString())
                append("like", if (like) "1" else "2")
                append("csrf", csrf)
            },
        ).body()
    }

    suspend fun coinVideo(
        aid: Long,
        count: Int,
        alsoLike: Boolean,
        csrf: String,
    ): BiliResponse<kotlinx.serialization.json.JsonElement> {
        return client.submitForm(
            url = BiliApiConstants.VIDEO_COIN,
            formParameters = Parameters.build {
                append("aid", aid.toString())
                append("multiply", count.coerceIn(1, 2).toString())
                append("select_like", if (alsoLike) "1" else "0")
                append("csrf", csrf)
            },
        ).body()
    }

    private fun tvSignedParameters(extra: Map<String, String> = emptyMap()): Parameters {
        val values = buildMap {
            put("appkey", TvAppKey)
            put("local_id", "0")
            put("ts", kotlin.time.Clock.System.now().epochSeconds.toString())
            putAll(extra)
        }
        val query = values.toSortedMap().entries.joinToString("&") { (key, value) -> "$key=$value" }
        return Parameters.build {
            values.forEach { (key, value) -> append(key, value) }
            append("sign", md5Hex(query + TvAppSecret))
        }
    }

    private companion object {
        const val TvAppKey = "4409e2ce8ffd12b8"
        const val TvAppSecret = "59b43e04ad6965f34319062b478f83dd"
    }
}
