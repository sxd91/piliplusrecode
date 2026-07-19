package com.piliplus.recodeing.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.backdrop.Backdrop
import com.piliplus.recodeing.core.auth.AccountRepository
import com.piliplus.recodeing.core.auth.TvQrLoginStatus
import com.piliplus.recodeing.core.auth.TvQrLoginViewModel
import com.piliplus.recodeing.core.design.GlassBackButton
import com.piliplus.recodeing.core.design.GlassSurface
import com.piliplus.recodeing.core.design.LiquidButton
import com.piliplus.recodeing.core.design.QrCodeImage
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

private enum class LoginMode(val title: String) {
    QrCode("扫码登录"),
    Cookie("Cookie"),
}

@Composable
fun LoginScreen(
    accountRepository: AccountRepository,
    backdrop: Backdrop,
    onBack: () -> Unit,
    viewModel: TvQrLoginViewModel = viewModel(key = "tv-qr-${accountRepository.hashCode()}") {
        TvQrLoginViewModel(accountRepository)
    },
) {
    val qrState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var mode by remember { mutableStateOf(LoginMode.QrCode) }
    var cookieInput by remember { mutableStateOf("") }
    var cookieMessage by remember { mutableStateOf<String?>(null) }
    var cookieImporting by remember { mutableStateOf(false) }

    DisposableEffect(viewModel) {
        onDispose(viewModel::cancel)
    }
    LaunchedEffect(mode) {
        if (mode == LoginMode.QrCode && qrState.status is TvQrLoginStatus.Idle) viewModel.start()
    }
    LaunchedEffect(qrState.status) {
        if (qrState.status is TvQrLoginStatus.Success) onBack()
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { GlassBackButton(onClick = onBack, backdrop = backdrop) }
            item { SmallTitle("登录 reliqliquid") }
            item {
                TabRow(
                    tabs = LoginMode.entries.map(LoginMode::title),
                    selectedTabIndex = mode.ordinal,
                    onTabSelected = { index -> LoginMode.entries.getOrNull(index)?.let { mode = it } },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            when (mode) {
                LoginMode.QrCode -> item {
                    QrLoginCard(
                        backdrop = backdrop,
                        qrUrl = qrState.qrUrl,
                        status = qrState.status,
                        onStart = viewModel::start,
                    )
                }
                LoginMode.Cookie -> item {
                    Card(Modifier.fillMaxWidth(), insideMargin = PaddingValues(20.dp)) {
                        Text("导入 Cookie", style = MiuixTheme.textStyles.title3)
                        Text(
                            "从已登录的 Bilibili 网页复制 Cookie。必须包含 SESSDATA，导入后会通过账号接口验证。",
                            modifier = Modifier.padding(top = 8.dp),
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                        InputField(
                            query = cookieInput,
                            onQueryChange = {
                                cookieInput = it
                                cookieMessage = null
                            },
                            onSearch = { },
                            expanded = false,
                            onExpandedChange = { },
                            label = "SESSDATA=...; bili_jct=...",
                            modifier = Modifier.padding(top = 16.dp),
                        )
                        cookieMessage?.let {
                            Text(
                                it,
                                modifier = Modifier.padding(top = 8.dp),
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                        LiquidButton(
                            onClick = {
                                if (cookieImporting) return@LiquidButton
                                cookieImporting = true
                                coroutineScope.launch {
                                    accountRepository.importAndValidateCookies(cookieInput).fold(
                                        onSuccess = {
                                            cookieMessage = "登录成功"
                                            onBack()
                                        },
                                        onFailure = { cookieMessage = it.message ?: "Cookie 导入失败" },
                                    )
                                    cookieImporting = false
                                }
                            },
                            backdrop = backdrop,
                            enabled = !cookieImporting && cookieInput.isNotBlank(),
                            modifier = Modifier.padding(top = 16.dp),
                            tint = MiuixTheme.colorScheme.primary.copy(alpha = 0.18f),
                        ) { Text(if (cookieImporting) "验证中" else "导入并验证") }
                    }
                }
            }
        }
    }
}

@Composable
private fun QrLoginCard(
    backdrop: Backdrop,
    qrUrl: String?,
    status: TvQrLoginStatus,
    onStart: () -> Unit,
) {
    GlassSurface(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        insideMargin = PaddingValues(20.dp),
    ) {
        Text("使用哔哩哔哩客户端扫码", style = MiuixTheme.textStyles.title3)
        Text(
            "二维码由 Bilibili TV 登录接口生成；确认后才会保存会话。",
            modifier = Modifier.padding(top = 8.dp),
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        if (qrUrl != null) {
            QrCodeImage(
                content = qrUrl,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(240.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(16.dp)),
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (status is TvQrLoginStatus.Creating || status is TvQrLoginStatus.Completing) {
                CircularProgressIndicator()
            }
            Text(
                text = qrStatusText(status),
                modifier = Modifier.padding(top = 8.dp),
                color = if (status is TvQrLoginStatus.Error) {
                    MiuixTheme.colorScheme.error
                } else {
                    MiuixTheme.colorScheme.onSurfaceVariantSummary
                },
            )
            if (status is TvQrLoginStatus.Idle || status is TvQrLoginStatus.Expired || status is TvQrLoginStatus.Error) {
                FlowRow(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    LiquidButton(
                        onClick = onStart,
                        backdrop = backdrop,
                        tint = MiuixTheme.colorScheme.primary.copy(alpha = 0.18f),
                    ) { Text(if (status is TvQrLoginStatus.Idle) "生成二维码" else "刷新二维码") }
                }
            }
        }
    }
}

private fun qrStatusText(status: TvQrLoginStatus): String = when (status) {
    TvQrLoginStatus.Idle -> "点击生成登录二维码"
    TvQrLoginStatus.Creating -> "正在生成二维码"
    TvQrLoginStatus.WaitingForScan -> "等待扫码"
    TvQrLoginStatus.WaitingForConfirmation -> "已扫码，请在手机上确认"
    TvQrLoginStatus.Completing -> "正在验证账号会话"
    TvQrLoginStatus.Success -> "登录成功"
    TvQrLoginStatus.Expired -> "二维码已过期"
    is TvQrLoginStatus.Error -> status.message
}
