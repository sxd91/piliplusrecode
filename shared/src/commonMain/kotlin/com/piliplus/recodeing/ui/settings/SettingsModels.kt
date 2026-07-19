package com.piliplus.recodeing.ui.settings

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable

enum class SettingsCategory(
    val title: String,
    val summary: String,
) {
    Privacy("隐私与账号", "黑名单、账号模式和安全选项"),
    Recommend("推荐流", "首页推荐和内容过滤"),
    AudioVideo("音视频", "画质、音质、解码和缓冲"),
    Player("播放器", "弹幕、手势、全屏和后台播放"),
    Appearance("外观", "主题、布局、导航和图片显示"),
    Other("其他", "搜索、网络、评论、缓存和实验功能"),
    WebDav("WebDAV", "备份与恢复设置"),
    About("关于", "版本、日志、开源许可和更新"),
}

@Immutable
@Serializable
data class SettingsUiState(
    val glassEnabled: Boolean = true,
    val appRecommend: Boolean = true,
    val retainRecommendation: Boolean = true,
    val recommendationPositionHint: Boolean = true,
    val exemptFollowedCreators: Boolean = true,
    val filterRelatedVideos: Boolean = true,
    val hardwareDecode: Boolean = true,
    val allow1080pAnonymous: Boolean = true,
    val cdnSpeedTest: Boolean = true,
    val audioIgnoreCdn: Boolean = false,
    val danmakuEnabled: Boolean = true,
    val tapDanmaku: Boolean = true,
    val autoPlay: Boolean = false,
    val fullscreenLock: Boolean = true,
    val fullscreenScreenshot: Boolean = true,
    val doubleTapSeek: Boolean = true,
    val gestureBrightnessVolume: Boolean = true,
    val backgroundPlayback: Boolean = false,
    val pictureInPicture: Boolean = false,
    val showFullscreenActions: Boolean = true,
    val horizontalLayout: Boolean = false,
    val useNavigationRail: Boolean = false,
    val tabletNavigationOptimization: Boolean = true,
    val floatingBottomBar: Boolean = true,
    val pureBlackTheme: Boolean = false,
    val dynamicUnreadBadge: Boolean = true,
    val messageUnreadBadge: Boolean = true,
    val checkDynamicUnread: Boolean = true,
    val showVideoChapters: Boolean = true,
    val showRelatedVideos: Boolean = true,
    val showVideoComments: Boolean = true,
    val showBangumiComments: Boolean = true,
    val expandVideoDescription: Boolean = false,
    val openLinksExternally: Boolean = false,
    val searchSuggestions: Boolean = true,
    val recordSearchHistory: Boolean = true,
    val showDecorations: Boolean = true,
    val showFanMedals: Boolean = true,
    val hapticFeedback: Boolean = false,
    val aiSummary: Boolean = false,
    val commentAntiFraud: Boolean = false,
    val blockCommercialDynamics: Boolean = false,
    val blockCommercialComments: Boolean = false,
    val http2Enabled: Boolean = false,
    val updateCheck: Boolean = true,
    val sponsorBlockEnabled: Boolean = false,
    val webDavConfigured: Boolean = false,
    val defaultVideoQuality: String = "8K",
    val cellularVideoQuality: String = "1080P",
    val defaultAudioQuality: String = "Hi-Res",
    val defaultLiveQuality: String = "原画",
    val bufferSize: String = "4 MB",
    val bufferDuration: String = "16 秒",
    val seekDuration: String = "10 秒",
    val themeMode: String = "跟随系统",
    val defaultStartupTab: String = "首页",
    val imageQuality: String = "10%",
    val fullImageQuality: String = "100%",
    val commentSort: String = "热门",
    val dynamicTab: String = "全部",
    val maxCacheSize: String = "1 GiB",
    val homeFeedColumns: Int = 0,
    val selectedCdnHost: String = "auto",
)

class SettingsStateHolder(
    private val repository: SettingsRepository,
) {
    var state by mutableStateOf(repository.load())
        private set

    fun update(transform: (SettingsUiState) -> SettingsUiState) {
        state = transform(state)
        repository.save(state)
    }
}
