package com.piliplus.recodeing.core.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piliplus.recodeing.core.network.BiliApiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface TvQrLoginStatus {
    data object Idle : TvQrLoginStatus
    data object Creating : TvQrLoginStatus
    data object WaitingForScan : TvQrLoginStatus
    data object WaitingForConfirmation : TvQrLoginStatus
    data object Completing : TvQrLoginStatus
    data object Success : TvQrLoginStatus
    data object Expired : TvQrLoginStatus
    data class Error(val message: String) : TvQrLoginStatus
}

data class TvQrLoginUiState(
    val qrUrl: String? = null,
    val status: TvQrLoginStatus = TvQrLoginStatus.Idle,
)

class TvQrLoginViewModel(
    private val accountRepository: AccountRepository,
    private val apiService: BiliApiService = BiliApiService(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(TvQrLoginUiState())
    val uiState: StateFlow<TvQrLoginUiState> = _uiState.asStateFlow()
    private var loginJob: Job? = null

    fun start() {
        loginJob?.cancel()
        loginJob = viewModelScope.launch {
            _uiState.value = TvQrLoginUiState(status = TvQrLoginStatus.Creating)
            try {
                val response = apiService.createTvQrCode()
                require(response.code == 0) { response.message.ifBlank { "二维码创建失败" } }
                val data = requireNotNull(response.data) { "二维码数据为空" }
                require(data.url.isNotBlank() && data.authCode.isNotBlank()) { "二维码数据不完整" }
                _uiState.value = TvQrLoginUiState(
                    qrUrl = data.url,
                    status = TvQrLoginStatus.WaitingForScan,
                )
                poll(data.authCode)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                _uiState.update { it.copy(status = TvQrLoginStatus.Error(error.message ?: "扫码登录失败")) }
            }
        }
    }

    fun cancel() {
        loginJob?.cancel()
        loginJob = null
        _uiState.value = TvQrLoginUiState()
    }

    private suspend fun poll(authCode: String) {
        while (kotlin.coroutines.coroutineContext.isActive) {
            delay(PollIntervalMillis)
            val response = apiService.pollTvQrCode(authCode)
            when (response.code) {
                0 -> {
                    val loginData = requireNotNull(response.data) { "登录结果为空" }
                    _uiState.update { it.copy(status = TvQrLoginStatus.Completing) }
                    accountRepository.completeTvQrLogin(loginData).getOrThrow()
                    _uiState.update { it.copy(status = TvQrLoginStatus.Success) }
                    return
                }
                WaitingCode -> _uiState.update { it.copy(status = TvQrLoginStatus.WaitingForScan) }
                ScannedCode -> _uiState.update { it.copy(status = TvQrLoginStatus.WaitingForConfirmation) }
                ExpiredCode -> {
                    _uiState.update { it.copy(status = TvQrLoginStatus.Expired) }
                    return
                }
                else -> error(response.message.ifBlank { "扫码状态获取失败（${response.code}）" })
            }
        }
    }

    override fun onCleared() {
        loginJob?.cancel()
    }

    private companion object {
        const val PollIntervalMillis = 2_000L
        const val WaitingCode = 86039
        const val ScannedCode = 86090
        const val ExpiredCode = 86038
    }
}
