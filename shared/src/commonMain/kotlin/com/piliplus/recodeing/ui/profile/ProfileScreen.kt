package com.piliplus.recodeing.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.piliplus.recodeing.core.auth.AccountRepository
import com.piliplus.recodeing.core.auth.AuthState
import com.kyant.backdrop.Backdrop
import com.piliplus.recodeing.core.design.BiliAsyncImage
import com.piliplus.recodeing.core.design.LiquidButton
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ProfileScreen(
    accountRepository: AccountRepository,
    backdrop: Backdrop,
) {
    val authState by accountRepository.authState.collectAsState()
    val accounts by accountRepository.accounts.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var cookieInput by remember { mutableStateOf("") }
    var cookieError by remember { mutableStateOf<String?>(null) }
    var cookieImporting by remember { mutableStateOf(false) }

    LaunchedEffect(accountRepository) {
        accountRepository.refreshSession()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SmallTitle(text = "账号")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    insideMargin = PaddingValues(20.dp),
                ) {
                    when (val state = authState) {
                        AuthState.Anonymous -> {
                            Text("未登录", style = MiuixTheme.textStyles.title3)
                            Text(
                                "可在应用内导入浏览器复制的 Bilibili Cookie；密码、短信和扫码登录仍需完成官方风控流程。",
                                modifier = Modifier.padding(top = 8.dp),
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                            InputField(
                                query = cookieInput,
                                onQueryChange = {
                                    cookieInput = it
                                    cookieError = null
                                },
                                onSearch = { },
                                expanded = false,
                                onExpandedChange = { },
                                label = "粘贴 Cookie，如 SESSDATA=...; bili_jct=...",
                                modifier = Modifier.padding(top = 16.dp),
                            )
                            cookieError?.let { message ->
                                Text(
                                    message,
                                    modifier = Modifier.padding(top = 8.dp),
                                    color = MiuixTheme.colorScheme.error,
                                )
                            }
                            FlowRow(
                                modifier = Modifier.padding(top = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                LiquidButton(
                                    onClick = {
                                        cookieImporting = true
                                        accountRepository.importCookies(cookieInput).fold(
                                            onSuccess = {
                                                cookieInput = ""
                                                cookieError = null
                                            },
                                            onFailure = { cookieError = it.message ?: "Cookie 导入失败" },
                                        )
                                        cookieImporting = false
                                    },
                                    backdrop = backdrop,
                                    tint = MiuixTheme.colorScheme.primary.copy(alpha = 0.18f),
                                ) {
                                    Text(if (cookieImporting) "导入中" else "导入 Cookie")
                                }
                                LiquidButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            accountRepository.refreshSession().onFailure {
                                                cookieError = it.message ?: "会话验证失败"
                                            }
                                        }
                                    },
                                    backdrop = backdrop,
                                ) {
                                    Text("验证会话")
                                }
                            }
                        }
                        is AuthState.LoggedIn -> {
                            BiliAsyncImage(
                                url = state.avatarUrl,
                                contentDescription = state.name,
                                modifier = Modifier.size(72.dp).clip(CircleShape),
                            )
                            Text(
                                state.name,
                                modifier = Modifier.padding(top = 12.dp),
                                style = MiuixTheme.textStyles.title3,
                            )
                            Text(
                                "UID ${state.mid}",
                                modifier = Modifier.padding(top = 8.dp),
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                            LiquidButton(
                                onClick = accountRepository::clearSession,
                                backdrop = backdrop,
                                modifier = Modifier.padding(top = 16.dp),
                            ) {
                                Text("退出当前账号")
                            }
                        }
                    }
                }
            }

            item {
                SmallTitle(text = "账号管理")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    insideMargin = PaddingValues(20.dp),
                ) {
                    Text("多账号", style = MiuixTheme.textStyles.title3)
                    if (accounts.isEmpty()) {
                        Text(
                            "登录后的账号会保存在加密会话存储中。",
                            modifier = Modifier.padding(top = 8.dp),
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    } else {
                        accounts.forEach { account ->
                            FlowRow(
                                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                LiquidButton(
                                    onClick = {
                                        accountRepository.switchAccount(account.id).onFailure {
                                            cookieError = it.message ?: "账号切换失败"
                                        }
                                    },
                                    backdrop = backdrop,
                                    tint = MiuixTheme.colorScheme.primary.copy(alpha = 0.12f),
                                ) {
                                    Text(account.name)
                                }
                                LiquidButton(
                                    onClick = { accountRepository.removeAccount(account.id) },
                                    backdrop = backdrop,
                                ) {
                                    Text("移除")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
