package com.piliplus.recodeing.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.piliplus.recodeing.core.design.GlassSurface
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SettingsPage(backdrop: Backdrop) {
    var glassEnabled by remember { mutableStateOf(true) }
    var autoPlay by remember { mutableStateOf(false) }
    var dynamicBadge by remember { mutableStateOf(true) }

    Column(modifier = Modifier.padding(20.dp)) {
        Text("设置", style = MiuixTheme.textStyles.title1)
        Text(
            "阶段 1 先迁移样式、播放和动态入口，后续按 Flutter 设置树继续补齐。",
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )

        GlassSurface(
            backdrop = backdrop,
            enabled = glassEnabled,
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                SwitchPreference(
                    title = "液态玻璃样式",
                    summary = "开关容器使用 Backdrop vibrancy + blur + lens",
                    checked = glassEnabled,
                    onCheckedChange = { glassEnabled = it },
                )
            }
        }

        Card(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            insideMargin = PaddingValues(vertical = 4.dp),
        ) {
            SwitchPreference(
                title = "自动播放视频",
                summary = "对应原播放设置中的自动播放入口",
                checked = autoPlay,
                onCheckedChange = { autoPlay = it },
            )
            SwitchPreference(
                title = "动态未读提醒",
                summary = "后续接入动态红点和消息未读接口",
                checked = dynamicBadge,
                onCheckedChange = { dynamicBadge = it },
            )
        }
    }
}
