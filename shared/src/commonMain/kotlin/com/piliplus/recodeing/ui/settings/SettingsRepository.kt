package com.piliplus.recodeing.ui.settings

import androidx.compose.runtime.Composable
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
        return runCatching { json.decodeFromString<SettingsUiState>(encoded) }
            .getOrDefault(SettingsUiState())
    }

    fun save(state: SettingsUiState) {
        settings.putString(SettingsStateKey, json.encodeToString(state))
    }
}

private const val SettingsStateKey = "settings_ui_state"

@Composable
expect fun rememberSettingsRepository(): SettingsRepository
