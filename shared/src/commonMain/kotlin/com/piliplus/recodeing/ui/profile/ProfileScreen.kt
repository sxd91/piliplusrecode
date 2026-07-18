package com.piliplus.recodeing.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.piliplus.recodeing.core.auth.AccountRepository
import com.piliplus.recodeing.core.auth.AuthState
import com.piliplus.recodeing.core.auth.rememberUrlOpener
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(20.dp),
        ) {
            Text("我的", style = MiuixTheme.textStyles.title1)
            when (val state = authState) {
                AuthState.Anonymous -> Text(
                    "当前为匿名浏览。登录、注册和风控验证将通过 Bilibili 官方页面完成。",
                    modifier = Modifier.padding(top = 8.dp),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                is AuthState.LoggedIn -> Text(
                    "已登录：${state.name}（mid: ${state.mid}）",
                    modifier = Modifier.padding(top = 8.dp),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            FlowRow(
                modifier = Modifier.padding(top = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = { urlOpener.open(accountRepository.officialLoginUrl) },
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text("官方登录")
                }
                Button(onClick = { urlOpener.open(accountRepository.officialRegisterUrl) }) {
                    Text("注册/验证")
                }
                Button(onClick = { accountRepository.clearSession() }) {
                    Text("退出")
                }
            }
            Text(
                text = "登录地址：${accountRepository.officialLoginUrl}",
                modifier = Modifier.padding(top = 14.dp),
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}
