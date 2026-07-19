package com.piliplus.recodeing.core.network

import com.piliplus.recodeing.core.model.BiliResponse
import com.piliplus.recodeing.core.model.NavUserInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock

class WbiSigner(
    private val client: HttpClient,
) {
    private val keyMutex = Mutex()
    private var cachedMixinKey: String? = null
    private var cacheExpiresAtSeconds: Long = 0L

    suspend fun sign(params: Map<String, Any?>): Map<String, String> {
        val mixinKey = getMixinKey()
        val timestamp = Clock.System.now().epochSeconds
        val sanitized = buildMap {
            params.forEach { (key, value) ->
                if (value != null) put(key, value.toString().filterNot(::isFilteredCharacter))
            }
            put("wts", timestamp.toString())
        }
        val query = sanitized.toSortedMap().entries.joinToString("&") { (key, value) ->
            "${percentEncode(key)}=${percentEncode(value)}"
        }
        return sanitized + ("w_rid" to md5Hex(query + mixinKey))
    }

    suspend fun invalidate() {
        keyMutex.withLock {
            cachedMixinKey = null
            cacheExpiresAtSeconds = 0L
        }
    }

    private suspend fun getMixinKey(): String {
        val now = Clock.System.now().epochSeconds
        cachedMixinKey?.takeIf { now < cacheExpiresAtSeconds }?.let { return it }
        return keyMutex.withLock {
            val lockedNow = Clock.System.now().epochSeconds
            cachedMixinKey?.takeIf { lockedNow < cacheExpiresAtSeconds }?.let { return@withLock it }
            val navResponse: BiliResponse<NavUserInfo> = client.get(BiliApiConstants.USER_INFO).body()
            val wbiImage = requireNotNull(navResponse.data?.wbiImage) { "Bilibili nav response did not include WBI keys" }
            val originalKey = extractKey(wbiImage.imageUrl) + extractKey(wbiImage.subUrl)
            val mixinKey = MixinKeyPermutation.asSequence()
                .mapNotNull { index -> originalKey.getOrNull(index) }
                .joinToString("")
                .take(32)
            require(mixinKey.length == 32) { "Bilibili returned invalid WBI keys" }
            cachedMixinKey = mixinKey
            cacheExpiresAtSeconds = lockedNow + WbiCacheDurationSeconds
            mixinKey
        }
    }

    private fun extractKey(url: String): String = url.substringAfterLast('/').substringBefore('.')

    private fun isFilteredCharacter(character: Char): Boolean = character in "!'()*"

    private fun percentEncode(value: String): String = buildString {
        value.encodeToByteArray().forEach { byte ->
            val unsigned = byte.toInt() and 0xFF
            val character = unsigned.toChar()
            if (
                character in 'a'..'z' || character in 'A'..'Z' || character in '0'..'9' ||
                character == '-' || character == '_' || character == '.' || character == '~'
            ) {
                append(character)
            } else {
                append('%')
                append(HexDigits[unsigned ushr 4])
                append(HexDigits[unsigned and 0x0F])
            }
        }
    }

    private companion object {
        const val WbiCacheDurationSeconds = 24L * 60L * 60L
        const val HexDigits = "0123456789ABCDEF"
        val MixinKeyPermutation = intArrayOf(
            46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35,
            27, 43, 5, 49, 33, 9, 42, 19, 29, 28, 14, 39, 12, 38, 41, 13,
            37, 48, 7, 16, 24, 55, 40, 61, 26, 17, 0, 1, 60, 51, 30, 4,
            22, 25, 54, 21, 56, 59, 6, 63, 57, 62, 11, 36, 20, 34, 44, 52,
        )
    }
}
