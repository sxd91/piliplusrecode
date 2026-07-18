package com.piliplus.recodeing.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoDetail(
    val aid: Long = 0,
    val bvid: String = "",
    val cid: Long = 0,
    val title: String = "",
    val desc: String = "",
    val pic: String = "",
    val duration: Long = 0,
    val pubdate: Long = 0,
    val owner: BiliOwner? = null,
    val stat: BiliStat? = null,
    val pages: List<VideoPage> = emptyList(),
)

@Serializable
data class VideoPage(
    val cid: Long = 0,
    val page: Int = 0,
    val from: String = "",
    val part: String = "",
    val duration: Long = 0,
    val vid: String = "",
)

@Serializable
data class VideoPlayUrl(
    val quality: Int = 0,
    val format: String = "",
    val timelength: Long = 0,
    @SerialName("accept_quality") val acceptQuality: List<Int> = emptyList(),
    @SerialName("accept_description") val acceptDescription: List<String> = emptyList(),
    val dash: DashMedia? = null,
    val durl: List<DirectMediaUrl> = emptyList(),
)

@Serializable
data class DashMedia(
    val duration: Double = 0.0,
    val video: List<DashTrack> = emptyList(),
    val audio: List<DashTrack> = emptyList(),
    @SerialName("dolby") val dolby: DolbyTracks? = null,
    @SerialName("flac") val flac: FlacTrack? = null,
)

@Serializable
data class DashTrack(
    val id: Int = 0,
    @SerialName("base_url") val baseUrl: String = "",
    @SerialName("backup_url") val backupUrls: List<String> = emptyList(),
    val bandwidth: Long = 0,
    @SerialName("mime_type") val mimeType: String = "",
    val codecs: String = "",
    val width: Int = 0,
    val height: Int = 0,
    @SerialName("frame_rate") val frameRate: String = "",
)

@Serializable
data class DolbyTracks(
    val type: Int = 0,
    val audio: List<DashTrack> = emptyList(),
)

@Serializable
data class FlacTrack(
    val display: Boolean = false,
    val audio: DashTrack? = null,
)

@Serializable
data class DirectMediaUrl(
    val order: Int = 0,
    val length: Long = 0,
    val size: Long = 0,
    val url: String = "",
    @SerialName("backup_url") val backupUrls: List<String> = emptyList(),
)
