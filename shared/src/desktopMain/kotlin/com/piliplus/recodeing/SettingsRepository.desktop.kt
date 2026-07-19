package com.piliplus.recodeing.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.russhwolf.settings.PreferencesSettings
import java.util.prefs.Preferences

@Composable
actual fun rememberSettingsRepository(): SettingsRepository = remember {
    SettingsRepository(
        PreferencesSettings(
            Preferences.userRoot().node("com/sxd/reliqliquid"),
        ),
    )
}
