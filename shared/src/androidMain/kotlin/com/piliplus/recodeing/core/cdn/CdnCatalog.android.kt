package com.piliplus.recodeing.core.cdn

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.time.TimeSource

actual suspend fun probeCdnEndpoint(endpoint: CdnEndpoint): CdnProbeResult = withContext(Dispatchers.IO) {
    val host = endpoint.host ?: return@withContext CdnProbeResult(endpoint, latencyMillis = null)
    runCatching {
        val mark = TimeSource.Monotonic.markNow()
        Socket().use { socket -> socket.connect(InetSocketAddress(host, 443), 4_000) }
        CdnProbeResult(endpoint, mark.elapsedNow().inWholeMilliseconds)
    }.getOrElse { throwable ->
        CdnProbeResult(endpoint, latencyMillis = null, error = throwable.message ?: "连接失败")
    }
}
