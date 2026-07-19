package com.piliplus.recodeing.ui.dynamics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.backdrop.Backdrop
import com.piliplus.recodeing.core.auth.AccountRepository
import com.piliplus.recodeing.core.auth.AuthState
import com.piliplus.recodeing.core.design.BiliAsyncImage
import com.piliplus.recodeing.core.design.LiquidButton
import com.piliplus.recodeing.core.model.DynamicItem
import com.piliplus.recodeing.core.network.BiliApiService
import com.piliplus.recodeing.core.repository.DynamicsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun DynamicsScreen(
    accountRepository: AccountRepository,
    backdrop: Backdrop,
    onVideoSelected: (String) -> Unit,
) {
    val authState by accountRepository.authState.collectAsState()
    if (authState !is AuthState.LoggedIn) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("登录后可查看动态时间线")
        }
        return
    }
    val account = authState as AuthState.LoggedIn
    val viewModel = viewModel(key = "dynamics-${account.mid}") {
        DynamicsViewModel(DynamicsRepository(BiliApiService(cookieHeaderProvider = accountRepository::cookieHeader)))
    }
    val state by viewModel.state.collectAsState()
    LaunchedEffect(account.mid) { viewModel.refresh() }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SmallTitle("动态时间线")
                LiquidButton(onClick = viewModel::refresh, backdrop = backdrop) { Text("刷新") }
            }
            if (state.loading) item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                }
            }
            state.error?.let { message -> item { Text(message, color = MiuixTheme.colorScheme.error) } }
            items(state.items, key = { it.idStr }) { item ->
                DynamicCard(item, onVideoSelected)
            }
            if (state.hasMore && !state.loading) item {
                LiquidButton(onClick = viewModel::loadMore, backdrop = backdrop) { Text("加载更多") }
            }
        }
    }
}

@Composable
private fun DynamicCard(item: DynamicItem, onVideoSelected: (String) -> Unit) {
    val archive = item.modules?.dynamic?.major?.archive
    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(18.dp),
        onClick = { archive?.bvid?.takeIf(String::isNotBlank)?.let(onVideoSelected) },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BiliAsyncImage(
                url = item.modules?.author?.face,
                contentDescription = item.modules?.author?.name,
                modifier = Modifier.size(42.dp).clip(CircleShape),
            )
            Column {
                Text(
                    item.modules?.author?.name.orEmpty().ifBlank { "Bilibili 用户" },
                    style = MiuixTheme.textStyles.title3,
                )
                item.modules?.author?.publishTime?.takeIf(String::isNotBlank)?.let {
                    Text(it, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                }
            }
        }
        archive?.cover?.takeIf(String::isNotBlank)?.let { cover ->
            BiliAsyncImage(
                url = cover,
                contentDescription = archive.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(14.dp)),
            )
        }
        Text(
            (archive?.title ?: item.modules?.dynamic?.desc?.text.orEmpty()).ifBlank { "动态内容" },
            modifier = Modifier.padding(top = 10.dp),
        )
        archive?.desc?.takeIf(String::isNotBlank)?.let {
            Text(it, modifier = Modifier.padding(top = 6.dp), color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        }
    }
}

data class DynamicsUiState(
    val loading: Boolean = false,
    val items: List<DynamicItem> = emptyList(),
    val offset: String = "",
    val hasMore: Boolean = false,
    val error: String? = null,
)

class DynamicsViewModel(private val repository: DynamicsRepository) : ViewModel() {
    private val _state = MutableStateFlow(DynamicsUiState())
    val state = _state.asStateFlow()

    fun refresh() = load(reset = true)
    fun loadMore() = load(reset = false)

    private fun load(reset: Boolean) {
        if (_state.value.loading) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            repository.load(if (reset) "" else _state.value.offset).fold(
                onSuccess = { data ->
                    _state.update {
                        it.copy(
                            loading = false,
                            items = (if (reset) data.items else it.items + data.items)
                                .distinctBy(DynamicItem::idStr)
                                .takeLast(300),
                            offset = data.offset,
                            hasMore = data.hasMore && data.offset.isNotBlank() && data.offset != it.offset,
                        )
                    }
                },
                onFailure = { error ->
                    _state.update { it.copy(loading = false, error = error.message ?: "动态加载失败") }
                },
            )
        }
    }
}
