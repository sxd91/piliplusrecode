package com.piliplus.recodeing.ui.settings

import androidx.compose.runtime.Composable
import com.piliplus.recodeing.core.cdn.resolveCdnEndpoint
import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SettingsRepository(
    private val settings: Settings,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun load(): SettingsUiState {
        val encoded = settings.getStringOrNull(SettingsStateKey) ?: return SettingsUiState()
        val decoded = runCatching { json.decodeFromString<SettingsUiState>(encoded) }
            .getOrDefault(SettingsUiState())
        val normalized = decoded.copy(
            selectedCdnHost = resolveCdnEndpoint(decoded.selectedCdnHost).id,
        )
        if (normalized != decoded) save(normalized)
        return normalized
    }

    fun save(state: SettingsUiState) {
        settings.putString(SettingsStateKey, json.encodeToString(state))
    }
}

private const val SettingsStateKey = "settings_ui_state"

@Composable
expect fun rememberSettingsRepository(): SettingsRepository
