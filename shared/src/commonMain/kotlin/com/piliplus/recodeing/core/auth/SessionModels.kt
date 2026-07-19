package com.piliplus.recodeing.core.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.piliplus.recodeing.core.model.TvQrLoginData
import com.piliplus.recodeing.core.network.BiliApiConstants
import com.piliplus.recodeing.core.network.BiliApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

@Serializable
data class SessionCookie(
    val name: String,
    val value: String,
)

@Serializable
data class StoredAccount(
    val id: String,
    val name: String,
    val mid: Long? = null,
    val avatarUrl: String? = null,
    val cookies: List<SessionCookie>,
)

expect class SessionStore {
    fun loadCookies(): List<SessionCookie>
    fun saveCookies(cookies: List<SessionCookie>)
    fun loadAccounts(): List<StoredAccount>
    fun saveAccounts(accounts: List<StoredAccount>)
    fun currentAccountId(): String?
    fun setCurrentAccountId(id: String?)
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
    private val apiService: BiliApiService = BiliApiService(
        cookieHeaderProvider = {
            sessionStore.loadCookies().joinToString("; ") { "${it.name}=${it.value}" }
        },
    ),
) {
    private val _accounts = MutableStateFlow(sessionStore.loadAccounts())
    val accounts: StateFlow<List<StoredAccount>> = _accounts.asStateFlow()
    private val initialAccount = _accounts.value.firstOrNull { it.id == sessionStore.currentAccountId() }
    private val _authState = MutableStateFlow<AuthState>(
        initialAccount?.mid?.let { AuthState.LoggedIn(it, initialAccount.name, initialAccount.avatarUrl) }
            ?: AuthState.Anonymous,
    )
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
        if (authState is AuthState.LoggedIn) {
            val account = StoredAccount(
                id = authState.mid.toString(),
                name = authState.name,
                mid = authState.mid,
                avatarUrl = authState.avatarUrl,
                cookies = sessionStore.loadCookies(),
            )
            val updated = _accounts.value.filterNot { it.id == account.id } + account
            _accounts.value = updated
            sessionStore.saveAccounts(updated)
            sessionStore.setCurrentAccountId(account.id)
        }
        authState
    }

    suspend fun importAndValidateCookies(rawCookieHeader: String): Result<AuthState> = runCatching {
        val cookies = rawCookieHeader.split(';').mapNotNull { part ->
            val separator = part.indexOf('=')
            if (separator <= 0) return@mapNotNull null
            val name = part.substring(0, separator).trim()
            val value = part.substring(separator + 1).trim()
            if (name.isBlank() || value.isBlank()) null else SessionCookie(name, value)
        }
        require(cookies.any { it.name == "SESSDATA" }) { "Cookie 缺少 SESSDATA" }
        replaceCookiesAndValidate(cookies)
    }

    suspend fun completeTvQrLogin(data: TvQrLoginData): Result<AuthState> = runCatching {
        val cookies = data.cookieInfo?.cookies.orEmpty()
            .filter { it.name.isNotBlank() && it.value.isNotBlank() }
            .map { SessionCookie(name = it.name, value = it.value) }
        require(cookies.any { it.name == "SESSDATA" }) { "扫码结果缺少 SESSDATA" }
        replaceCookiesAndValidate(cookies)
    }

    private suspend fun replaceCookiesAndValidate(cookies: List<SessionCookie>): AuthState {
        val previousCookies = sessionStore.loadCookies()
        sessionStore.saveCookies(cookies)
        return try {
            refreshSession().getOrThrow().also { state ->
                require(state is AuthState.LoggedIn) { "账号会话验证失败" }
            }
        } catch (error: Throwable) {
            sessionStore.saveCookies(previousCookies)
            throw error
        }
    }

    fun storedCookies(): List<SessionCookie> = sessionStore.loadCookies()

    fun switchAccount(id: String): Result<Unit> = runCatching {
        val account = _accounts.value.firstOrNull { it.id == id } ?: error("账号不存在")
        sessionStore.saveCookies(account.cookies)
        sessionStore.setCurrentAccountId(account.id)
        _authState.value = account.mid?.let {
            AuthState.LoggedIn(it, account.name, account.avatarUrl)
        } ?: AuthState.Anonymous
    }

    fun removeAccount(id: String) {
        val updated = _accounts.value.filterNot { it.id == id }
        _accounts.value = updated
        sessionStore.saveAccounts(updated)
        if (sessionStore.currentAccountId() == id) clearSession()
    }

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
