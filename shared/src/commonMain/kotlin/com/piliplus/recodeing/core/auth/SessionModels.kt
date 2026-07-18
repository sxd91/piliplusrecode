package com.piliplus.recodeing.core.auth

import com.piliplus.recodeing.core.network.BiliApiConstants
import com.piliplus.recodeing.core.network.BiliApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface AuthState {
    data object Anonymous : AuthState
    data class LoggedIn(
        val mid: Long,
        val name: String,
        val avatarUrl: String?,
    ) : AuthState
}

class AccountRepository(
    private val apiService: BiliApiService = BiliApiService(),
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
            AuthState.LoggedIn(
                mid = data.mid,
                name = data.uname,
                avatarUrl = data.face,
            )
        } else {
            AuthState.Anonymous
        }
        _authState.value = authState
        authState
    }

    fun clearSession() {
        _authState.value = AuthState.Anonymous
    }

    fun importCookiePlaceholder() {
        // 后续接入安全存储和 CookieJar；此处避免保存敏感信息到普通内存外的位置。
        _authState.value = AuthState.Anonymous
    }
}
