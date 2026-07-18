package com.piliplus.recodeing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.piliplus.recodeing.ui.shell.MainShell
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

@Composable
fun App() {
    val themeController = remember {
        ThemeController(colorSchemeMode = ColorSchemeMode.System)
    }

    MiuixTheme(controller = themeController) {
        MainShell()
    }
}
