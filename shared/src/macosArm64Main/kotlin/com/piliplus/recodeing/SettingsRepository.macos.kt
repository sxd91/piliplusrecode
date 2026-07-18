package com.piliplus.recodeing.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.russhwolf.settings.NSUserDefaultsSettings
import platform.Foundation.NSUserDefaults

@Composable
actual fun rememberSettingsRepository(): SettingsRepository = remember {
    SettingsRepository(
        NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults),
    )
}
