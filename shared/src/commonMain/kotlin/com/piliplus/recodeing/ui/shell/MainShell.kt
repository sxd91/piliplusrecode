package com.piliplus.recodeing.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.piliplus.recodeing.core.auth.AccountRepository
import com.piliplus.recodeing.core.design.PiliGlassDefaults
import com.piliplus.recodeing.ui.dynamics.DynamicsScreen
import com.piliplus.recodeing.ui.home.HomeScreen
import com.piliplus.recodeing.ui.profile.ProfileScreen
import com.piliplus.recodeing.ui.settings.SettingsPage
import com.piliplus.recodeing.ui.settings.SettingsStateHolder
import com.piliplus.recodeing.ui.settings.rememberSettingsRepository
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarItem
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
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
    val settingsRepository = rememberSettingsRepository()
    val settingsStateHolder = remember(settingsRepository) { SettingsStateHolder(settingsRepository) }
    val backgroundColor = MiuixTheme.colorScheme.background
    val glassNavigationColor = MiuixTheme.colorScheme.surface.copy(alpha = PiliGlassDefaults.SurfaceAlpha)
    val backdrop = rememberLayerBackdrop(
        onDraw = {
            drawRect(backgroundColor)
            drawContent()
        },
    )
    val scrollBehavior = key(current) { MiuixScrollBehavior() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .layerBackdrop(backdrop),
    ) {
        val useRail = maxWidth >= WideNavigationWidth

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = current.title,
                    largeTitle = when (current) {
                        AppDestination.Home -> "PiliPlus"
                        AppDestination.Dynamics -> "动态"
                        AppDestination.Profile -> "个人中心"
                        AppDestination.Settings -> "设置"
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
            bottomBar = {
                if (!useRail) {
                    GlassBottomNavigation(
                        selected = current,
                        onSelected = { current = it },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .drawBackdrop(
                                backdrop = backdrop,
                                shape = { RoundedCornerShape(PiliGlassDefaults.NavigationCornerRadius) },
                                effects = {
                                    vibrancy()
                                    blur(PiliGlassDefaults.BlurRadius.toPx())
                                    lens(
                                        PiliGlassDefaults.RefractionHeight.toPx(),
                                        PiliGlassDefaults.RefractionAmount.toPx(),
                                    )
                                },
                                onDrawSurface = { drawRect(glassNavigationColor) },
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
                        color = MiuixTheme.colorScheme.surface,
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
                        settingsStateHolder = settingsStateHolder,
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
    settingsStateHolder: SettingsStateHolder,
    backdrop: Backdrop,
) {
    when (current) {
        AppDestination.Home -> HomeScreen()
        AppDestination.Dynamics -> DynamicsScreen()
        AppDestination.Profile -> ProfileScreen(accountRepository)
        AppDestination.Settings -> SettingsPage(backdrop, settingsStateHolder)
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
