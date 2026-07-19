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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.piliplus.recodeing.core.auth.AccountRepository
import com.piliplus.recodeing.core.auth.AuthState
import com.kyant.backdrop.Backdrop
import com.piliplus.recodeing.core.design.BiliAsyncImage
import com.piliplus.recodeing.core.design.LiquidButton
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ProfileScreen(
    accountRepository: AccountRepository,
    backdrop: Backdrop,
    onLogin: () -> Unit,
) {
    val authState by accountRepository.authState.collectAsState()
    val accounts by accountRepository.accounts.collectAsState()
    var accountMessage by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }

    LaunchedEffect(accountRepository) {
        if (accountRepository.storedCookies().isNotEmpty()) {
            accountRepository.refreshSession()
        }
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
                                "支持 Bilibili 客户端扫码登录，也可导入浏览器 Cookie。密码和短信登录仍需接入官方风控流程。",
                                modifier = Modifier.padding(top = 8.dp),
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                            LiquidButton(
                                onClick = onLogin,
                                backdrop = backdrop,
                                modifier = Modifier.padding(top = 16.dp),
                                tint = MiuixTheme.colorScheme.primary.copy(alpha = 0.18f),
                            ) {
                                Text("扫码或 Cookie 登录")
                            }
                            accountMessage?.let { message ->
                                Text(
                                    message,
                                    modifier = Modifier.padding(top = 8.dp),
                                    color = MiuixTheme.colorScheme.error,
                                )
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
                                            accountMessage = it.message ?: "账号切换失败"
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
