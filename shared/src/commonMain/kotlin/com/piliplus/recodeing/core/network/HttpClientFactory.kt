package com.piliplus.recodeing.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect fun platformHttpClientEngine(): HttpClientEngineFactory<*>

fun createBiliHttpClient(): HttpClient = HttpClient(platformHttpClientEngine()) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            },
        )
    }
    install(ContentEncoding) {
        gzip()
        deflate()
    }
    install(HttpTimeout) {
        connectTimeoutMillis = 15_000
        requestTimeoutMillis = 30_000
        socketTimeoutMillis = 30_000
    }
    install(UserAgent) {
        agent = "Mozilla/5.0 liquidreode/0.1 ComposeMultiplatform"
    }
    install(Logging) {
        level = LogLevel.INFO
        // 注意：后续添加账号后需继续保持敏感 Header/Cookie 脱敏。
        sanitizeHeader { header ->
            header.equals(HttpHeaders.Authorization, ignoreCase = true) ||
                header.equals(HttpHeaders.Cookie, ignoreCase = true)
        }
    }
    defaultRequest {
        url {
            protocol = URLProtocol.HTTPS
            host = "api.bilibili.com"
        }
        contentType(ContentType.Application.Json)
        accept(ContentType.Application.Json)
        header(HttpHeaders.AcceptLanguage, "zh-CN,zh;q=0.9")
        header(HttpHeaders.Referrer, BiliApiConstants.WEB_BASE_URL)
    }
}
