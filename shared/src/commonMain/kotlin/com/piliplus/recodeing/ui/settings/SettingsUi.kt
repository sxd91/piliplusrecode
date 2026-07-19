package com.piliplus.recodeing.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.piliplus.recodeing.core.cdn.CdnProbeResult
import com.piliplus.recodeing.core.cdn.builtInCdnEndpoints
import com.piliplus.recodeing.core.cdn.probeCdnEndpoint
import com.piliplus.recodeing.core.cdn.resolveCdnEndpoint
import com.piliplus.recodeing.core.design.GlassBackButton
import com.piliplus.recodeing.core.design.GlassSurface
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference

@Composable
fun SettingsPage(
    backdrop: Backdrop,
    stateHolder: SettingsStateHolder,
) {
    var selectedCategory by remember { mutableStateOf<SettingsCategory?>(null) }
    var query by remember { mutableStateOf("") }
    var showCdnDialog by remember { mutableStateOf(false) }
    val state = stateHolder.state
    val rootListState = remember { androidx.compose.foundation.lazy.LazyListState() }
    val categoryListStates = remember { mutableStateMapOf<SettingsCategory, androidx.compose.foundation.lazy.LazyListState>() }
    val activeListState = selectedCategory?.let { category ->
        categoryListStates.getOrPut(category) { androidx.compose.foundation.lazy.LazyListState() }
    } ?: rootListState
    val matchingCategories = remember(query) {
        if (query.isBlank()) SettingsCategory.entries else SettingsCategory.entries.filter { category ->
            category.title.contains(query, ignoreCase = true) || category.summary.contains(query, ignoreCase = true) ||
                settingsSearchTerms.getValue(category).any { it.contains(query, ignoreCase = true) }
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            state = activeListState,
            modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (selectedCategory != null) {
                        GlassBackButton(onClick = { selectedCategory = null }, backdrop = backdrop)
                    }
                    GlassSurface(
                        backdrop = backdrop,
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp,
                    ) {
                        InputField(
                            query = query,
                            onQueryChange = { query = it },
                            onSearch = { },
                            expanded = true,
                            onExpandedChange = { },
                            label = "搜索设置",
                        )
                    }
                }
            }
            if (selectedCategory == null) {
                item { SmallTitle(if (query.isBlank()) "设置分类" else "搜索结果") }
                items(matchingCategories, key = SettingsCategory::name) { category ->
                    Card(Modifier.fillMaxWidth(), insideMargin = PaddingValues(vertical = 4.dp)) {
                        BasicComponent(
                            title = category.title,
                            summary = category.summary,
                            onClick = {
                                selectedCategory = category
                                query = ""
                            },
                        )
                    }
                }
                if (matchingCategories.isEmpty()) item {
                    Card(Modifier.fillMaxWidth(), insideMargin = PaddingValues(18.dp)) {
                        Text("没有找到相关设置")
                    }
                }
                settingsGroup("常用") {
                    settingSwitch("液态玻璃样式", "导航、搜索、按钮和重点容器", state.glassEnabled) {
                        stateHolder.update { current -> current.copy(glassEnabled = it) }
                    }
                }
            } else {
                categoryContent(
                    category = selectedCategory!!,
                    state = state,
                    update = stateHolder::update,
                    onCdnConfig = { showCdnDialog = true },
                )
            }
        }
    }

    CdnConfigurationDialog(
        show = showCdnDialog,
        selectedHost = state.selectedCdnHost,
        onSelectedHost = { selected -> stateHolder.update { it.copy(selectedCdnHost = selected) } },
        onDismiss = { showCdnDialog = false },
    )
}

private fun LazyListScope.categoryContent(
    category: SettingsCategory,
    state: SettingsUiState,
    update: ((SettingsUiState) -> SettingsUiState) -> Unit,
    onCdnConfig: () -> Unit,
) {
    when (category) {
        SettingsCategory.Privacy -> settingsGroup("账号与隐私") {
            ArrowPreference("黑名单管理", summary = "管理已屏蔽用户", onClick = {})
            ArrowPreference("账号模式说明", summary = "Cookie、扫码、短信、密码和多账号", onClick = {})
        }
        SettingsCategory.Recommend -> {
            settingsGroup("首页推荐") {
                settingSwitch("使用 App 推荐流", null, state.appRecommend) { update { s -> s.copy(appRecommend = it) } }
                settingSwitch("保留上一次推荐", null, state.retainRecommendation) { update { s -> s.copy(retainRecommendation = it) } }
                settingSwitch("显示上次浏览位置", null, state.recommendationPositionHint) { update { s -> s.copy(recommendationPositionHint = it) } }
                OverlayDropdownPreference(
                    title = "瀑布流列数",
                    summary = feedColumnLabel(state.homeFeedColumns),
                    items = listOf("自动", "1 列", "2 列", "3 列", "4 列", "5 列", "6 列"),
                    selectedIndex = state.homeFeedColumns.coerceIn(0, 6),
                    onSelectedIndexChange = { index -> update { it.copy(homeFeedColumns = index) } },
                )
            }
            settingsGroup("推荐过滤") {
                ArrowPreference("最低点赞比例", summary = "0%，不过滤", onClick = {})
                ArrowPreference("标题关键词", summary = "文本或正则表达式", onClick = {})
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
                ArrowPreference(
                    "CDN 配置",
                    summary = resolveCdnEndpoint(state.selectedCdnHost).name,
                    onClick = onCdnConfig,
                )
                settingSwitch("CDN 测速", "手动检测内置线路延迟", state.cdnSpeedTest) { update { s -> s.copy(cdnSpeedTest = it) } }
                settingSwitch("音频不跟随 CDN", null, state.audioIgnoreCdn) { update { s -> s.copy(audioIgnoreCdn = it) } }
            }
            settingsGroup("默认质量") {
                selectionPreference("视频画质", state.defaultVideoQuality, videoQualityOptions) { update { s -> s.copy(defaultVideoQuality = it) } }
                selectionPreference("移动网络视频画质", state.cellularVideoQuality, videoQualityOptions) { update { s -> s.copy(cellularVideoQuality = it) } }
                selectionPreference("音频质量", state.defaultAudioQuality, audioQualityOptions) { update { s -> s.copy(defaultAudioQuality = it) } }
                selectionPreference("直播画质", state.defaultLiveQuality, liveQualityOptions) { update { s -> s.copy(defaultLiveQuality = it) } }
            }
            settingsGroup("缓冲与同步") {
                selectionPreference("缓冲大小", state.bufferSize, listOf("2 MB", "4 MB", "8 MB", "16 MB")) { update { s -> s.copy(bufferSize = it) } }
                selectionPreference("缓冲时长", state.bufferDuration, listOf("8 秒", "16 秒", "30 秒", "60 秒")) { update { s -> s.copy(bufferDuration = it) } }
            }
        }
        SettingsCategory.Player -> {
            settingsGroup("弹幕与播放") {
                settingSwitch("显示弹幕", null, state.danmakuEnabled) { update { s -> s.copy(danmakuEnabled = it) } }
                settingSwitch("点击弹幕", null, state.tapDanmaku) { update { s -> s.copy(tapDanmaku = it) } }
                settingSwitch("自动播放", null, state.autoPlay) { update { s -> s.copy(autoPlay = it) } }
            }
            settingsGroup("全屏与手势") {
                settingSwitch("双击快进与快退", state.seekDuration, state.doubleTapSeek) { update { s -> s.copy(doubleTapSeek = it) } }
                settingSwitch("亮度与音量手势", null, state.gestureBrightnessVolume) { update { s -> s.copy(gestureBrightnessVolume = it) } }
                settingSwitch("后台播放", null, state.backgroundPlayback) { update { s -> s.copy(backgroundPlayback = it) } }
                settingSwitch("后台画中画", null, state.pictureInPicture) { update { s -> s.copy(pictureInPicture = it) } }
                settingSwitch("全屏操作按钮", "点赞、投币和收藏", state.showFullscreenActions) { update { s -> s.copy(showFullscreenActions = it) } }
            }
        }
        SettingsCategory.Appearance -> {
            settingsGroup("布局与导航") {
                settingSwitch("横屏布局适配", null, state.horizontalLayout) { update { s -> s.copy(horizontalLayout = it) } }
                settingSwitch("使用侧边栏导航", "立即生效", state.useNavigationRail) { update { s -> s.copy(useNavigationRail = it) } }
                settingSwitch("优化平板导航", "立即生效", state.tabletNavigationOptimization) { update { s -> s.copy(tabletNavigationOptimization = it) } }
                settingSwitch("浮动底栏", "立即生效", state.floatingBottomBar) { update { s -> s.copy(floatingBottomBar = it) } }
                settingSwitch("液态玻璃全覆盖", "底栏、搜索、按钮、返回键与重点面板", state.glassEnabled) { update { s -> s.copy(glassEnabled = it) } }
            }
            settingsGroup("主题与图像") {
                selectionPreference("主题模式", state.themeMode, listOf("跟随系统", "浅色", "深色")) { update { s -> s.copy(themeMode = it) } }
                settingSwitch("纯黑主题", null, state.pureBlackTheme) { update { s -> s.copy(pureBlackTheme = it) } }
                selectionPreference("图片质量", state.imageQuality, listOf("10%", "30%", "50%", "75%", "100%")) { update { s -> s.copy(imageQuality = it) } }
            }
        }
        SettingsCategory.Other -> {
            settingsGroup("视频与评论") {
                settingSwitch("显示视频章节", null, state.showVideoChapters) { update { s -> s.copy(showVideoChapters = it) } }
                settingSwitch("显示相关视频", null, state.showRelatedVideos) { update { s -> s.copy(showRelatedVideos = it) } }
                settingSwitch("显示视频评论", null, state.showVideoComments) { update { s -> s.copy(showVideoComments = it) } }
                settingSwitch("默认展开视频简介", null, state.expandVideoDescription) { update { s -> s.copy(expandVideoDescription = it) } }
            }
            settingsGroup("搜索与显示") {
                settingSwitch("搜索建议", null, state.searchSuggestions) { update { s -> s.copy(searchSuggestions = it) } }
                settingSwitch("记录搜索历史", null, state.recordSearchHistory) { update { s -> s.copy(recordSearchHistory = it) } }
                settingSwitch("触觉反馈", null, state.hapticFeedback) { update { s -> s.copy(hapticFeedback = it) } }
                settingSwitch("AI 总结", null, state.aiSummary) { update { s -> s.copy(aiSummary = it) } }
            }
            settingsGroup("网络与缓存") {
                selectionPreference("评论排序", state.commentSort, listOf("热门", "最新")) { update { s -> s.copy(commentSort = it) } }
                selectionPreference("最大缓存大小", state.maxCacheSize, listOf("512 MiB", "1 GiB", "2 GiB", "5 GiB", "不限")) { update { s -> s.copy(maxCacheSize = it) } }
                settingSwitch("启动时检查更新", null, state.updateCheck) { update { s -> s.copy(updateCheck = it) } }
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
        SettingsCategory.About -> settingsGroup("关于 reliqliquid") {
            BasicComponent(title = "版本", summary = "0.1.0")
            BasicComponent(title = "开发者", summary = "SXD")
            BasicComponent(title = "项目仓库", summary = "github.com/sxd91/piliplusrecode")
            BasicComponent(title = "必需 UI 库", summary = "Miuix · AndroidLiquidGlass-kmp")
            ArrowPreference("日志", summary = "查看、复制、清除与报告问题", onClick = {})
            ArrowPreference("开源许可", summary = "Miuix、Backdrop、Coil、Ktor 与 Media3", onClick = {})
        }
    }
}

@Composable
private fun CdnConfigurationDialog(
    show: Boolean,
    selectedHost: String,
    onSelectedHost: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var probing by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<CdnProbeResult>>(emptyList()) }
    LaunchedEffect(show) {
        if (!show) results = emptyList()
    }
    OverlayDialog(
        show = show,
        title = "CDN 配置",
        summary = "选择内置 Bilibili CDN，并手动检测 TCP 443 连接延迟。媒体地址会保留原始候选，播放器自动重试仍在完善。",
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 620.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            builtInCdnEndpoints.forEach { endpoint ->
                val result = results.firstOrNull { it.endpoint == endpoint }
                BasicComponent(
                    title = endpoint.name,
                    summary = buildString {
                        append(endpoint.host ?: "由 Bilibili 自动选择")
                        result?.latencyMillis?.let { append(" · ").append(it).append(" ms") }
                        result?.error?.let { append(" · ").append(it) }
                        if (resolveCdnEndpoint(selectedHost).id == endpoint.id) append(" · 当前")
                    },
                    onClick = { onSelectedHost(endpoint.id) },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (probing) CircularProgressIndicator()
                top.yukonga.miuix.kmp.basic.TextButton(text = "关闭", onClick = onDismiss)
                top.yukonga.miuix.kmp.basic.TextButton(
                    text = if (probing) "检测中" else "检测延迟",
                    enabled = !probing,
                    onClick = {
                        probing = true
                        scope.launch {
                            results = builtInCdnEndpoints.filter { it.host != null }
                                .map { endpoint -> probeCdnEndpoint(endpoint) }
                                .sortedBy { it.latencyMillis ?: Long.MAX_VALUE }
                            probing = false
                        }
                    },
                )
            }
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
private fun selectionPreference(
    title: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
) {
    OverlayDropdownPreference(
        title = title,
        summary = value,
        items = options,
        selectedIndex = options.indexOf(value).coerceAtLeast(0),
        onSelectedIndexChange = { index -> options.getOrNull(index)?.let(onValueChange) },
    )
}

@Composable
private fun settingSwitch(
    title: String,
    summary: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SwitchPreference(title = title, summary = summary, checked = checked, onCheckedChange = onCheckedChange)
}

private fun feedColumnLabel(columns: Int): String = if (columns == 0) "自动适配" else "$columns 列"

private val videoQualityOptions = listOf("自动", "360P", "480P", "720P", "1080P", "1080P 高码率", "4K", "8K")
private val audioQualityOptions = listOf("自动", "64K", "132K", "192K", "杜比全景声", "Hi-Res")
private val liveQualityOptions = listOf("自动", "流畅", "高清", "蓝光", "原画")

private val settingsSearchTerms = mapOf(
    SettingsCategory.Privacy to listOf("登录", "Cookie", "扫码", "短信", "密码", "黑名单", "多账号"),
    SettingsCategory.Recommend to listOf("首页", "瀑布流", "列数", "推荐", "热门", "过滤"),
    SettingsCategory.AudioVideo to listOf("CDN", "测速", "画质", "音质", "解码", "缓冲"),
    SettingsCategory.Player to listOf("弹幕", "手势", "倍速", "画中画", "后台播放", "全屏"),
    SettingsCategory.Appearance to listOf("液态玻璃", "主题", "导航", "侧栏", "图片", "动画"),
    SettingsCategory.Other to listOf("评论", "搜索", "缓存", "SponsorBlock", "AI"),
    SettingsCategory.WebDav to listOf("备份", "恢复", "服务器"),
    SettingsCategory.About to listOf("版本", "仓库", "SXD", "日志", "开源许可"),
)
