package com.piliplus.recodeing.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import com.piliplus.recodeing.core.auth.rememberUrlOpener
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ProfileScreen(
    accountRepository: AccountRepository,
) {
    val urlOpener = rememberUrlOpener()
    val authState by accountRepository.authState.collectAsState()

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
                                "登录与安全验证将由 Bilibili 官方页面完成。",
                                modifier = Modifier.padding(top = 8.dp),
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                            FlowRow(
                                modifier = Modifier.padding(top = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Button(
                                    onClick = { urlOpener.open(accountRepository.officialLoginUrl) },
                                    colors = ButtonDefaults.buttonColorsPrimary(),
                                ) {
                                    Text("官方登录")
                                }
                                TextButton(
                                    text = "注册或验证",
                                    onClick = { urlOpener.open(accountRepository.officialRegisterUrl) },
                                )
                            }
                        }
                        is AuthState.LoggedIn -> {
                            Text(state.name, style = MiuixTheme.textStyles.title3)
                            Text(
                                "UID ${state.mid}",
                                modifier = Modifier.padding(top = 8.dp),
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                            TextButton(
                                text = "退出当前账号",
                                onClick = accountRepository::clearSession,
                                modifier = Modifier.padding(top = 16.dp),
                            )
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
                    Text(
                        "后续将在安全存储接入后支持账号隔离、切换和会话刷新。",
                        modifier = Modifier.padding(top = 8.dp),
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }
    }
}
