package com.piliplus.recodeing.core.cdn

import com.piliplus.recodeing.core.model.DashMedia
import com.piliplus.recodeing.core.model.DashTrack
import com.piliplus.recodeing.core.model.DirectMediaUrl
import com.piliplus.recodeing.core.model.DolbyTracks
import com.piliplus.recodeing.core.model.FlacTrack
import com.piliplus.recodeing.core.model.VideoPlayUrl

fun VideoPlayUrl.withCdn(
    endpointValue: String,
    rewriteAudio: Boolean,
): VideoPlayUrl {
    val host = resolveCdnEndpoint(endpointValue).host ?: return this
    return copy(
        dash = dash?.withCdn(host = host, rewriteAudio = rewriteAudio),
        durl = durl.map { it.withCdn(host) },
    )
}

private fun DashMedia.withCdn(host: String, rewriteAudio: Boolean): DashMedia = copy(
    video = video.map { it.withCdn(host) },
    audio = if (rewriteAudio) audio.map { it.withCdn(host) } else audio,
    dolby = if (rewriteAudio) dolby?.withCdn(host) else dolby,
    flac = if (rewriteAudio) flac?.withCdn(host) else flac,
)

private fun DolbyTracks.withCdn(host: String): DolbyTracks = copy(
    audio = audio.map { it.withCdn(host) },
)

private fun FlacTrack.withCdn(host: String): FlacTrack = copy(
    audio = audio?.withCdn(host),
)

private fun DashTrack.withCdn(host: String): DashTrack {
    val originalCandidates = listOf(baseUrl) + backupUrls
    return copy(
        baseUrl = baseUrl.replaceMediaHost(host),
        backupUrls = (originalCandidates.map { it.replaceMediaHost(host) } + originalCandidates)
            .filter(String::isNotBlank)
            .distinct()
            .filterNot { it == baseUrl.replaceMediaHost(host) },
    )
}

private fun DirectMediaUrl.withCdn(host: String): DirectMediaUrl {
    val originalCandidates = listOf(url) + backupUrls
    return copy(
        url = url.replaceMediaHost(host),
        backupUrls = (originalCandidates.map { it.replaceMediaHost(host) } + originalCandidates)
            .filter(String::isNotBlank)
            .distinct()
            .filterNot { it == url.replaceMediaHost(host) },
    )
}

private fun String.replaceMediaHost(host: String): String {
    val schemeEnd = indexOf("://")
    if (schemeEnd < 0) return this
    val authorityStart = schemeEnd + 3
    val suffixStart = listOf(
        indexOf('/', authorityStart),
        indexOf('?', authorityStart),
        indexOf('#', authorityStart),
    ).filter { it >= 0 }.minOrNull() ?: length
    return substring(0, authorityStart) + host + substring(suffixStart)
}
