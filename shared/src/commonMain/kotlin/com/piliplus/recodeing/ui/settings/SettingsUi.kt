package com.piliplus.recodeing.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.piliplus.recodeing.core.design.LiquidButton
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference

@Composable
fun SettingsPage(
    backdrop: Backdrop,
    stateHolder: SettingsStateHolder,
) {
    var selectedCategory by remember { mutableStateOf<SettingsCategory?>(null) }
    val state = stateHolder.state
    val rootListState = rememberLazyListState()
    val categoryListStates = remember { mutableStateMapOf<SettingsCategory, androidx.compose.foundation.lazy.LazyListState>() }
    val activeListState = selectedCategory?.let { category ->
        categoryListStates.getOrPut(category) { androidx.compose.foundation.lazy.LazyListState() }
    } ?: rootListState

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            state = activeListState,
            modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (selectedCategory == null) {
                item {
                    SmallTitle("设置分类")
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SettingsCategory.entries.forEach { category ->
                            LiquidButton(
                                onClick = { selectedCategory = category },
                                backdrop = backdrop,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(Modifier.fillMaxWidth()) {
                                    Text(category.title)
                                    Text(category.summary)
                                }
                            }
                        }
                    }
                }
                settingsGroup("常用") {
                    settingSwitch("液态玻璃样式", "用于导航与重点容器", state.glassEnabled) {
                        stateHolder.update { state -> state.copy(glassEnabled = it) }
                    }
                    settingSwitch("自动播放", null, state.autoPlay) {
                        stateHolder.update { state -> state.copy(autoPlay = it) }
                    }
                    settingSwitch("动态未读提醒", null, state.dynamicUnreadBadge) {
                        stateHolder.update { state -> state.copy(dynamicUnreadBadge = it) }
                    }
                }
            } else {
                item {
                    LiquidButton(
                        onClick = { selectedCategory = null },
                        backdrop = backdrop,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("返回设置分类 · ${selectedCategory?.title.orEmpty()}")
                    }
                }
                categoryContent(selectedCategory!!, state, stateHolder::update)
            }
        }
    }
}

private fun LazyListScope.categoryContent(
    category: SettingsCategory,
    state: SettingsUiState,
    update: ((SettingsUiState) -> SettingsUiState) -> Unit,
) {
    when (category) {
        SettingsCategory.Privacy -> settingsGroup("账号与隐私") {
            ArrowPreference("黑名单管理", summary = "管理已屏蔽用户", onClick = {})
            ArrowPreference("账号模式说明", summary = "查看当前登录与 API 能力", onClick = {})
        }

        SettingsCategory.Recommend -> {
            settingsGroup("首页推荐") {
                settingSwitch("使用 App 推荐流", "重启后生效", state.appRecommend) { update { s -> s.copy(appRecommend = it) } }
                settingSwitch("保留上一次推荐", null, state.retainRecommendation) { update { s -> s.copy(retainRecommendation = it) } }
                settingSwitch("显示上次浏览位置", null, state.recommendationPositionHint) { update { s -> s.copy(recommendationPositionHint = it) } }
            }
            settingsGroup("推荐过滤") {
                ArrowPreference("最低点赞比例", summary = "0%，不过滤", onClick = {})
                ArrowPreference("标题关键词", summary = "文本或正则表达式", onClick = {})
                ArrowPreference("分区关键词", summary = "推荐、热门与排行榜", onClick = {})
                ArrowPreference("最低视频时长", summary = "0 秒", onClick = {})
                ArrowPreference("最低播放量", summary = "0", onClick = {})
                settingSwitch("关注 UP 主不受过滤", null, state.exemptFollowedCreators) { update { s -> s.copy(exemptFollowedCreators = it) } }
                settingSwitch("过滤相关视频", null, state.filterRelatedVideos) { update { s -> s.copy(filterRelatedVideos = it) } }
            }
        }

        SettingsCategory.AudioVideo -> {
            settingsGroup("解码与网络") {
                settingSwitch("硬件解码", null, state.hardwareDecode) { update { s -> s.copy(hardwareDecode = it) } }
                settingSwitch("未登录允许 1080P", null, state.allow1080pAnonymous) { update { s -> s.copy(allow1080pAnonymous = it) } }
                ArrowPreference("CDN 配置", summary = "备用 URL CDN", onClick = {})
                ArrowPreference("直播 CDN 主机", summary = "系统默认", onClick = {})
                settingSwitch("CDN 测速", null, state.cdnSpeedTest) { update { s -> s.copy(cdnSpeedTest = it) } }
                settingSwitch("音频不跟随 CDN", null, state.audioIgnoreCdn) { update { s -> s.copy(audioIgnoreCdn = it) } }
            }
            settingsGroup("默认质量") {
                ArrowPreference("视频画质", summary = state.defaultVideoQuality, onClick = {})
                ArrowPreference("移动网络视频画质", summary = state.cellularVideoQuality, onClick = {})
                ArrowPreference("音频质量", summary = state.defaultAudioQuality, onClick = {})
                ArrowPreference("直播画质", summary = state.defaultLiveQuality, onClick = {})
                ArrowPreference("偏好解码格式", summary = "第一个可用格式", onClick = {})
            }
            settingsGroup("缓冲与同步") {
                ArrowPreference("缓冲大小", summary = state.bufferSize, onClick = {})
                ArrowPreference("缓冲时长", summary = state.bufferDuration, onClick = {})
                ArrowPreference("自动同步", summary = "平台默认", onClick = {})
                ArrowPreference("视频同步模式", summary = "display-resample", onClick = {})
            }
        }

        SettingsCategory.Player -> {
            settingsGroup("弹幕与播放") {
                settingSwitch("显示弹幕", null, state.danmakuEnabled) { update { s -> s.copy(danmakuEnabled = it) } }
                settingSwitch("点击弹幕", null, state.tapDanmaku) { update { s -> s.copy(tapDanmaku = it) } }
                ArrowPreference("播放速度", summary = "速度列表、默认与长按速度", onClick = {})
                settingSwitch("自动播放", null, state.autoPlay) { update { s -> s.copy(autoPlay = it) } }
            }
            settingsGroup("全屏与手势") {
                settingSwitch("全屏锁定按钮", null, state.fullscreenLock) { update { s -> s.copy(fullscreenLock = it) } }
                settingSwitch("全屏截图按钮", null, state.fullscreenScreenshot) { update { s -> s.copy(fullscreenScreenshot = it) } }
                settingSwitch("双击快进与快退", state.seekDuration, state.doubleTapSeek) { update { s -> s.copy(doubleTapSeek = it) } }
                settingSwitch("亮度与音量手势", null, state.gestureBrightnessVolume) { update { s -> s.copy(gestureBrightnessVolume = it) } }
                settingSwitch("后台播放", null, state.backgroundPlayback) { update { s -> s.copy(backgroundPlayback = it) } }
                settingSwitch("后台画中画", null, state.pictureInPicture) { update { s -> s.copy(pictureInPicture = it) } }
                settingSwitch("全屏操作按钮", "点赞、投币和收藏", state.showFullscreenActions) { update { s -> s.copy(showFullscreenActions = it) } }
                ArrowPreference("默认全屏方向", summary = "自动", onClick = {})
                ArrowPreference("底部进度条", summary = "始终显示", onClick = {})
                ArrowPreference("播放顺序", summary = "播放结束后暂停", onClick = {})
            }
        }

        SettingsCategory.Appearance -> {
            settingsGroup("布局与导航") {
                settingSwitch("横屏布局适配", null, state.horizontalLayout) { update { s -> s.copy(horizontalLayout = it) } }
                settingSwitch("使用侧边栏导航", "重启后生效", state.useNavigationRail) { update { s -> s.copy(useNavigationRail = it) } }
                settingSwitch("优化平板导航", "重启后生效", state.tabletNavigationOptimization) { update { s -> s.copy(tabletNavigationOptimization = it) } }
                settingSwitch("浮动底栏", "重启后生效", state.floatingBottomBar) { update { s -> s.copy(floatingBottomBar = it) } }
                ArrowPreference("界面缩放", summary = "100%", onClick = {})
                ArrowPreference("页面转场", summary = "原生", onClick = {})
                ArrowPreference("首页标签管理", summary = "显示、隐藏和排序", onClick = {})
                ArrowPreference("导航栏管理", summary = "显示、隐藏和排序", onClick = {})
            }
            settingsGroup("主题与图像") {
                ArrowPreference("主题模式", summary = state.themeMode, onClick = {})
                settingSwitch("纯黑主题", null, state.pureBlackTheme) { update { s -> s.copy(pureBlackTheme = it) } }
                ArrowPreference("应用主题", summary = "动态色彩与调色板", onClick = {})
                ArrowPreference("字体大小", summary = "100%", onClick = {})
                ArrowPreference("图片质量", summary = state.imageQuality, onClick = {})
                ArrowPreference("大图预览质量", summary = state.fullImageQuality, onClick = {})
                ArrowPreference("默认启动标签", summary = state.defaultStartupTab, onClick = {})
                settingSwitch("动态未读角标", null, state.dynamicUnreadBadge) { update { s -> s.copy(dynamicUnreadBadge = it) } }
                settingSwitch("消息未读角标", null, state.messageUnreadBadge) { update { s -> s.copy(messageUnreadBadge = it) } }
            }
        }

        SettingsCategory.Other -> {
            settingsGroup("视频与评论") {
                settingSwitch("显示视频章节", null, state.showVideoChapters) { update { s -> s.copy(showVideoChapters = it) } }
                settingSwitch("显示相关视频", null, state.showRelatedVideos) { update { s -> s.copy(showRelatedVideos = it) } }
                settingSwitch("显示视频评论", null, state.showVideoComments) { update { s -> s.copy(showVideoComments = it) } }
                settingSwitch("显示番剧评论", null, state.showBangumiComments) { update { s -> s.copy(showBangumiComments = it) } }
                settingSwitch("默认展开视频简介", null, state.expandVideoDescription) { update { s -> s.copy(expandVideoDescription = it) } }
                settingSwitch("外部浏览器打开链接", null, state.openLinksExternally) { update { s -> s.copy(openLinksExternally = it) } }
                ArrowPreference("评论折叠行数", summary = "6 行", onClick = {})
                ArrowPreference("弹幕行高", summary = "1.6", onClick = {})
                ArrowPreference("评论关键词过滤", summary = "未设置", onClick = {})
                ArrowPreference("动态关键词过滤", summary = "未设置", onClick = {})
            }
            settingsGroup("搜索与显示") {
                settingSwitch("搜索建议", null, state.searchSuggestions) { update { s -> s.copy(searchSuggestions = it) } }
                settingSwitch("记录搜索历史", null, state.recordSearchHistory) { update { s -> s.copy(recordSearchHistory = it) } }
                settingSwitch("显示头像和动态装扮", null, state.showDecorations) { update { s -> s.copy(showDecorations = it) } }
                settingSwitch("显示粉丝勋章", null, state.showFanMedals) { update { s -> s.copy(showFanMedals = it) } }
                settingSwitch("触觉反馈", null, state.hapticFeedback) { update { s -> s.copy(hapticFeedback = it) } }
                settingSwitch("AI 总结", null, state.aiSummary) { update { s -> s.copy(aiSummary = it) } }
            }
            settingsGroup("网络与缓存") {
                settingSwitch("评论反诈检查", null, state.commentAntiFraud) { update { s -> s.copy(commentAntiFraud = it) } }
                settingSwitch("屏蔽商业动态", null, state.blockCommercialDynamics) { update { s -> s.copy(blockCommercialDynamics = it) } }
                settingSwitch("屏蔽商业评论", null, state.blockCommercialComments) { update { s -> s.copy(blockCommercialComments = it) } }
                settingSwitch("启用 HTTP/2", "重启后生效", state.http2Enabled) { update { s -> s.copy(http2Enabled = it) } }
                ArrowPreference("连接重试次数", summary = "2", onClick = {})
                ArrowPreference("连接重试间隔", summary = "500 ms", onClick = {})
                ArrowPreference("评论排序", summary = state.commentSort, onClick = {})
                ArrowPreference("动态标签", summary = state.dynamicTab, onClick = {})
                ArrowPreference("代理", summary = "未启用", onClick = {})
                ArrowPreference("最大缓存大小", summary = state.maxCacheSize, onClick = {})
                settingSwitch("启动时检查更新", null, state.updateCheck) { update { s -> s.copy(updateCheck = it) } }
            }
            settingsGroup("扩展功能") {
                settingSwitch("空降助手", "SponsorBlock 片段跳过", state.sponsorBlockEnabled) { update { s -> s.copy(sponsorBlockEnabled = it) } }
                ArrowPreference("空降助手设置", summary = "片段类型、服务器与统计", onClick = {})
            }
        }

        SettingsCategory.WebDav -> settingsGroup("WebDAV 配置") {
            ArrowPreference("服务器地址", summary = if (state.webDavConfigured) "已配置" else "未配置", onClick = {})
            ArrowPreference("用户名", summary = "未设置", onClick = {})
            ArrowPreference("密码", summary = "安全存储", onClick = {})
            ArrowPreference("远程目录", summary = "/", onClick = {})
            ArrowPreference("保存并测试配置", onClick = {})
            ArrowPreference("备份设置", summary = "上传设置与播放配置", onClick = {})
            ArrowPreference("恢复设置", summary = "替换本机设置，需要确认", onClick = {})
        }

        SettingsCategory.About -> settingsGroup("关于 liquidreode") {
            ArrowPreference("版本", summary = "0.1.0", onClick = {})
            ArrowPreference("开发者", summary = "SXD", onClick = {})
            ArrowPreference("日志", summary = "查看、复制、清除与报告问题", onClick = {})
            ArrowPreference("开源许可", summary = "Miuix、Backdrop 与其他依赖", onClick = {})
            ArrowPreference("检查更新", onClick = {})
        }
    }
}

private fun LazyListScope.settingsGroup(title: String, content: @Composable () -> Unit) {
    item {
        SmallTitle(title)
        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(vertical = 4.dp),
        ) { content() }
    }
}

@Composable
private fun settingSwitch(
    title: String,
    summary: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SwitchPreference(
        title = title,
        summary = summary,
        checked = checked,
        onCheckedChange = onCheckedChange,
    )
}
