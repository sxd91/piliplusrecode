package com.piliplus.recodeing.ui.shell

import androidx.compose.ui.graphics.vector.ImageVector
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Community
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Settings

sealed class AppDestination(
    val title: String,
    val icon: ImageVector,
) {
    data object Home : AppDestination("首页", MiuixIcons.Light.Home)
    data object Dynamics : AppDestination("动态", MiuixIcons.Light.Community)
    data object Profile : AppDestination("我的", MiuixIcons.Light.Contacts)
    data object Settings : AppDestination("设置", MiuixIcons.Light.Settings)
}

val mainDestinations = listOf(
    AppDestination.Home,
    AppDestination.Dynamics,
    AppDestination.Profile,
    AppDestination.Settings,
)
