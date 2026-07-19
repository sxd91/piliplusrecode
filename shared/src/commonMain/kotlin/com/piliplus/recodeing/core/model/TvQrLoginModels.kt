package com.piliplus.recodeing.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TvQrCodeData(
    val url: String = "",
    @SerialName("auth_code") val authCode: String = "",
)

@Serializable
data class TvQrLoginData(
    val mid: Long? = null,
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("cookie_info") val cookieInfo: TvQrCookieInfo? = null,
)

@Serializable
data class TvQrCookieInfo(
    val cookies: List<TvQrCookie> = emptyList(),
)

@Serializable
data class TvQrCookie(
    val name: String = "",
    val value: String = "",
)
