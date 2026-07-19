package com.piliplus.recodeing.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.russhwolf.settings.SharedPreferencesSettings

@Composable
actual fun rememberSettingsRepository(): SettingsRepository {
    val context = LocalContext.current
    return remember(context) {
        SettingsRepository(
            SharedPreferencesSettings(
                context.getSharedPreferences("reliqliquid_settings", 0),
            ),
        )
    }
}
