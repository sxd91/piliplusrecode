package com.piliplus.recodeing.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawRect
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.piliplus.recodeing.core.auth.AccountRepository
import com.piliplus.recodeing.ui.dynamics.DynamicsScreen
import com.piliplus.recodeing.ui.home.HomeScreen
import com.piliplus.recodeing.ui.profile.ProfileScreen
import com.piliplus.recodeing.ui.settings.SettingsPage
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val WideNavigationWidth = 760.dp

@Composable
fun MainShell(
    accountRepository: AccountRepository = remember { AccountRepository() },
) {
    var current by remember { mutableStateOf<AppDestination>(AppDestination.Home) }
    val backdrop = rememberLayerBackdrop {
        drawRect(MiuixTheme.colorScheme.background)
        drawContent()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.background)
            .layerBackdrop(backdrop),
    ) {
        val useRail = maxWidth >= WideNavigationWidth

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = current.title,
                    largeTitle = when (current) {
                        AppDestination.Home -> "PiliPlus"
                        AppDestination.Dynamics -> "动态"
                        AppDestination.Profile -> "个人中心"
                        AppDestination.Settings -> "设置"
                    },
                    subtitle = "Compose Multiplatform · Miuix Glass",
                )
            },
            bottomBar = {
                if (!useRail) {
                    GlassBottomNavigation(
                        selected = current,
                        onSelected = { current = it },
                        modifier = Modifier
                            .padding(horizontal = 18.dp)
                            .drawBackdrop(
                                backdrop = backdrop,
                                shape = { RoundedCornerShape(32.dp) },
                                effects = {
                                    vibrancy()
                                    blur(4.dp.toPx())
                                    lens(16.dp.toPx(), 32.dp.toPx())
                                },
                                onDrawSurface = {
                                    drawRect(MiuixTheme.colorScheme.surface.copy(alpha = 0.46f))
                                },
                            ),
                    )
                }
            },
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
            ) {
                if (useRail) {
                    NavigationRail(
                        modifier = Modifier.widthIn(max = 96.dp),
                        color = MiuixTheme.colorScheme.surface.copy(alpha = 0.78f),
                    ) {
                        mainDestinations.forEach { destination ->
                            NavigationRailItem(
                                selected = current == destination,
                                onClick = { current = destination },
                                icon = destination.icon,
                                label = destination.title,
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (useRail) 0.dp else paddingValues.calculateBottomPadding()),
                ) {
                    CurrentDestination(
                        current = current,
                        accountRepository = accountRepository,
                        backdrop = backdrop,
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentDestination(
    current: AppDestination,
    accountRepository: AccountRepository,
    backdrop: Backdrop,
) {
    when (current) {
        AppDestination.Home -> HomeScreen()
        AppDestination.Dynamics -> DynamicsScreen()
        AppDestination.Profile -> ProfileScreen(accountRepository)
        AppDestination.Settings -> SettingsPage(backdrop)
    }
}

@Composable
private fun GlassBottomNavigation(
    selected: AppDestination,
    onSelected: (AppDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingNavigationBar(
        modifier = modifier,
        color = Color.Transparent,
        showDivider = false,
    ) {
        mainDestinations.forEach { destination ->
            FloatingNavigationBarItem(
                selected = selected == destination,
                onClick = { onSelected(destination) },
                icon = destination.icon,
                label = destination.title,
            )
        }
    }
}
