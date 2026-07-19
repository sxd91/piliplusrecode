package com.piliplus.recodeing.core.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.piliplus.recodeing.core.network.BiliApiConstants
import com.piliplus.recodeing.core.network.BiliApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SessionCookie(
    val name: String,
    val value: String,
)

expect class SessionStore {
    fun loadCookies(): List<SessionCookie>
    fun saveCookies(cookies: List<SessionCookie>)
    fun clear()
}

@Composable
expect fun rememberSessionStore(): SessionStore

sealed interface AuthState {
    data object Anonymous : AuthState
    data class LoggedIn(
        val mid: Long,
        val name: String,
        val avatarUrl: String?,
    ) : AuthState
}

class AccountRepository(
    private val sessionStore: SessionStore,
    private val apiService: BiliApiService = BiliApiService {
        sessionStore.loadCookies().joinToString("; ") { "${it.name}=${it.value}" }
    },
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Anonymous)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val officialLoginUrl: String = BiliApiConstants.OFFICIAL_LOGIN_URL
    val officialRegisterUrl: String = BiliApiConstants.OFFICIAL_REGISTER_URL

    suspend fun refreshSession(): Result<AuthState> = runCatching {
        val response = apiService.navUserInfo()
        require(response.code == 0) { response.message.ifBlank { "账号状态获取失败" } }
        val data = response.data
        val authState = if (data?.isLogin == true && data.mid != null && data.uname != null) {
            AuthState.LoggedIn(data.mid, data.uname, data.face)
        } else {
            AuthState.Anonymous
        }
        _authState.value = authState
        authState
    }

    fun importCookies(rawCookieHeader: String): Result<Unit> = runCatching {
        val cookies = rawCookieHeader.split(';').mapNotNull { part ->
            val separator = part.indexOf('=')
            if (separator <= 0) return@mapNotNull null
            val name = part.substring(0, separator).trim()
            val value = part.substring(separator + 1).trim()
            if (name.isBlank() || value.isBlank()) null else SessionCookie(name, value)
        }
        require(cookies.isNotEmpty()) { "Cookie 内容为空" }
        sessionStore.saveCookies(cookies)
    }

    fun storedCookies(): List<SessionCookie> = sessionStore.loadCookies()

    fun clearSession() {
        sessionStore.clear()
        _authState.value = AuthState.Anonymous
    }

    fun cookieHeader(): String = sessionStore.loadCookies()
        .joinToString("; ") { "${it.name}=${it.value}" }
}

@Composable
fun rememberAccountRepository(): AccountRepository {
    val sessionStore = rememberSessionStore()
    return androidx.compose.runtime.remember(sessionStore) { AccountRepository(sessionStore = sessionStore) }
}
