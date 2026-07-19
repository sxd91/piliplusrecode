package com.piliplus.recodeing.core.cdn

data class CdnEndpoint(
    val name: String,
    val host: String?,
)

val builtInCdnEndpoints = listOf(
    CdnEndpoint("自动选择", null),
    CdnEndpoint("上海电信", "cn-sh-ct-01-23.bilivideo.com"),
    CdnEndpoint("北京联通", "cn-bj-cc-03-17.bilivideo.com"),
    CdnEndpoint("广州移动", "cn-gdgz-cm-01-10.bilivideo.com"),
    CdnEndpoint("深圳腾讯", "upos-sz-mirrorcosdisp.bilivideo.com"),
    CdnEndpoint("杭州移动", "cn-zjhz-cm-01-17.bilivideo.com"),
    CdnEndpoint("成都电信", "cn-sccd-ct-01-24.bilivideo.com"),
    CdnEndpoint("海外 Akamai", "upos-hz-mirrorakam.akamaized.net"),
)

data class CdnProbeResult(
    val endpoint: CdnEndpoint,
    val latencyMillis: Long?,
    val error: String? = null,
)

expect suspend fun probeCdnEndpoint(endpoint: CdnEndpoint): CdnProbeResult
