package com.piliplus.recodeing.core.cdn

data class CdnEndpoint(
    val id: String,
    val name: String,
    val host: String?,
)

val builtInCdnEndpoints = listOf(
    CdnEndpoint("auto", "自动选择", null),
    CdnEndpoint("sh-ct", "上海电信", "cn-sh-ct-01-23.bilivideo.com"),
    CdnEndpoint("bj-cu", "北京联通", "cn-bj-cc-03-17.bilivideo.com"),
    CdnEndpoint("gz-cm", "广州移动", "cn-gdgz-cm-01-10.bilivideo.com"),
    CdnEndpoint("sz-tencent", "深圳腾讯", "upos-sz-mirrorcosdisp.bilivideo.com"),
    CdnEndpoint("hz-cm", "杭州移动", "cn-zjhz-cm-01-17.bilivideo.com"),
    CdnEndpoint("cd-ct", "成都电信", "cn-sccd-ct-01-24.bilivideo.com"),
    CdnEndpoint("akamai", "海外 Akamai", "upos-hz-mirrorakam.akamaized.net"),
)

fun resolveCdnEndpoint(value: String): CdnEndpoint =
    builtInCdnEndpoints.firstOrNull { it.id == value || it.name == value } ?: builtInCdnEndpoints.first()

data class CdnProbeResult(
    val endpoint: CdnEndpoint,
    val latencyMillis: Long?,
    val error: String? = null,
)

expect suspend fun probeCdnEndpoint(endpoint: CdnEndpoint): CdnProbeResult
