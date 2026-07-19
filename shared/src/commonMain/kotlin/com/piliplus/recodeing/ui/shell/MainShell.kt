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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.piliplus.recodeing.core.auth.AccountRepository
import com.piliplus.recodeing.core.auth.rememberAccountRepository
import com.piliplus.recodeing.core.design.PiliGlassDefaults
import com.piliplus.recodeing.ui.dynamics.DynamicsScreen
import com.piliplus.recodeing.ui.home.HomeScreen
import com.piliplus.recodeing.ui.profile.ProfileScreen
import com.piliplus.recodeing.ui.settings.SettingsPage
import com.piliplus.recodeing.ui.settings.SettingsStateHolder
import com.piliplus.recodeing.ui.settings.rememberSettingsRepository
import com.piliplus.recodeing.ui.video.VideoDetailScreen
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarItem
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val CompactWidth = 600.dp
private val ExpandedWidth = 840.dp
private val MediumLandscapeMinHeight = 520.dp

private enum class WindowNavigationMode { CompactBottomBar, SideRail }

@Composable
fun MainShell(
    accountRepository: AccountRepository = rememberAccountRepository(),
) {
    var current by remember { mutableStateOf<AppDestination>(AppDestination.Home) }
    var selectedVideoId by remember { mutableStateOf<String?>(null) }
    val settingsRepository = rememberSettingsRepository()
    val settingsStateHolder = remember(settingsRepository) { SettingsStateHolder(settingsRepository) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val settings = settingsStateHolder.state
        val navigationMode = remember(maxWidth, maxHeight, settings.useNavigationRail, settings.tabletNavigationOptimization) {
            resolveNavigationMode(
                width = maxWidth,
                height = maxHeight,
                preferRail = settings.useNavigationRail,
                optimizeTabletNavigation = settings.tabletNavigationOptimization,
            )
        }
        ShellWithBackdrop(
            navigationMode = navigationMode,
            current = current,
            onDestinationSelected = { current = it },
            selectedVideoId = selectedVideoId,
            onVideoSelected = { selectedVideoId = it },
            onVideoBack = { selectedVideoId = null },
            accountRepository = accountRepository,
            settingsStateHolder = settingsStateHolder,
            glassEnabled = settings.glassEnabled,
            floatingBottomBar = settings.floatingBottomBar,
        )
    }
}

private fun resolveNavigationMode(
    width: Dp,
    height: Dp,
    preferRail: Boolean,
    optimizeTabletNavigation: Boolean,
): WindowNavigationMode = when {
    width < CompactWidth -> WindowNavigationMode.CompactBottomBar
    preferRail -> WindowNavigationMode.SideRail
    optimizeTabletNavigation && width >= ExpandedWidth -> WindowNavigationMode.SideRail
    optimizeTabletNavigation && width > height && height >= MediumLandscapeMinHeight -> WindowNavigationMode.SideRail
    else -> WindowNavigationMode.CompactBottomBar
}

@Composable
private fun ShellWithBackdrop(
    navigationMode: WindowNavigationMode,
    current: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit,
    selectedVideoId: String?,
    onVideoSelected: (String) -> Unit,
    onVideoBack: () -> Unit,
    accountRepository: AccountRepository,
    settingsStateHolder: SettingsStateHolder,
    glassEnabled: Boolean,
    floatingBottomBar: Boolean,
) {
    val backgroundColor = MiuixTheme.colorScheme.background
    val backdrop = key(navigationMode) {
        rememberLayerBackdrop(
            onDraw = {
                drawRect(backgroundColor)
                drawContent()
            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .layerBackdrop(backdrop),
    ) {
        when (navigationMode) {
            WindowNavigationMode.CompactBottomBar -> CompactShell(
                current = current,
                onDestinationSelected = onDestinationSelected,
                accountRepository = accountRepository,
                settingsStateHolder = settingsStateHolder,
                backdrop = backdrop,
                selectedVideoId = selectedVideoId,
                onVideoSelected = onVideoSelected,
                onVideoBack = onVideoBack,
                glassEnabled = glassEnabled,
                floatingBottomBar = floatingBottomBar,
            )

            WindowNavigationMode.SideRail -> RailShell(
                current = current,
                onDestinationSelected = onDestinationSelected,
                selectedVideoId = selectedVideoId,
                onVideoSelected = onVideoSelected,
                onVideoBack = onVideoBack,
                accountRepository = accountRepository,
                settingsStateHolder = settingsStateHolder,
                backdrop = backdrop,
            )
        }
    }
}

@Composable
private fun CompactShell(
    current: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit,
    selectedVideoId: String?,
    onVideoSelected: (String) -> Unit,
    onVideoBack: () -> Unit,
    accountRepository: AccountRepository,
    settingsStateHolder: SettingsStateHolder,
    backdrop: Backdrop,
    glassEnabled: Boolean,
    floatingBottomBar: Boolean,
) {
    val scrollBehavior = key(current) { MiuixScrollBehavior() }
    val glassNavigationColor = MiuixTheme.colorScheme.surface.copy(alpha = PiliGlassDefaults.SurfaceAlpha)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { ShellTopBar(current, scrollBehavior) },
        bottomBar = {
            GlassBottomNavigation(
                selected = current,
                onSelected = onDestinationSelected,
                modifier = Modifier
                    .padding(horizontal = if (floatingBottomBar) 16.dp else 0.dp)
                    .then(
                        if (glassEnabled) {
                            Modifier.drawBackdrop(
                                backdrop = backdrop,
                                shape = {
                                    RoundedCornerShape(
                                        if (floatingBottomBar) PiliGlassDefaults.NavigationCornerRadius else 0.dp,
                                    )
                                },
                                effects = {
                                    vibrancy()
                                    blur(PiliGlassDefaults.BlurRadius.toPx())
                                    lens(
                                        PiliGlassDefaults.RefractionHeight.toPx(),
                                        PiliGlassDefaults.RefractionAmount.toPx(),
                                    )
                                },
                                onDrawSurface = { drawRect(glassNavigationColor) },
                            )
                        } else {
                            Modifier.background(MiuixTheme.colorScheme.surface)
                        },
                    ),
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(bottom = paddingValues.calculateBottomPadding()),
        ) {
            CurrentDestination(
                current = current,
                selectedVideoId = selectedVideoId,
                onVideoSelected = onVideoSelected,
                onVideoBack = onVideoBack,
                accountRepository = accountRepository,
                settingsStateHolder = settingsStateHolder,
                backdrop = backdrop,
            )
        }
    }
}

@Composable
private fun RailShell(
    current: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit,
    selectedVideoId: String?,
    onVideoSelected: (String) -> Unit,
    onVideoBack: () -> Unit,
    accountRepository: AccountRepository,
    settingsStateHolder: SettingsStateHolder,
    backdrop: Backdrop,
) {
    val scrollBehavior = key(current) { MiuixScrollBehavior() }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { ShellTopBar(current, scrollBehavior) },
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
        ) {
            NavigationRail(
                modifier = Modifier.widthIn(max = 96.dp),
                color = MiuixTheme.colorScheme.surface,
            ) {
                mainDestinations.forEach { destination ->
                    NavigationRailItem(
                        selected = current == destination,
                        onClick = { onDestinationSelected(destination) },
                        icon = destination.icon,
                        label = destination.title,
                    )
                }
            }
            Box(Modifier.fillMaxSize()) {
                CurrentDestination(
                current = current,
                selectedVideoId = selectedVideoId,
                onVideoSelected = onVideoSelected,
                onVideoBack = onVideoBack,
                accountRepository = accountRepository,
                settingsStateHolder = settingsStateHolder,
                backdrop = backdrop,
            )
            }
        }
    }
}

@Composable
private fun ShellTopBar(
    current: AppDestination,
    scrollBehavior: ScrollBehavior,
) {
    TopAppBar(
        title = current.title,
        largeTitle = when (current) {
            AppDestination.Home -> "liquidreode"
            AppDestination.Dynamics -> "动态"
            AppDestination.Profile -> "个人中心"
            AppDestination.Settings -> "设置"
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun CurrentDestination(
    current: AppDestination,
    selectedVideoId: String?,
    onVideoSelected: (String) -> Unit,
    onVideoBack: () -> Unit,
    accountRepository: AccountRepository,
    settingsStateHolder: SettingsStateHolder,
    backdrop: Backdrop,
) {
    if (selectedVideoId != null) {
        VideoDetailScreen(
            bvid = selectedVideoId,
            onBack = onVideoBack,
            onPlay = { },
            onVideoSelected = onVideoSelected,
        )
        return
    }
    when (current) {
        AppDestination.Home -> HomeScreen(
            backdrop = backdrop,
            onVideoSelected = onVideoSelected,
        )
        AppDestination.Dynamics -> DynamicsScreen(
            accountRepository = accountRepository,
            backdrop = backdrop,
            onVideoSelected = onVideoSelected,
        )
        AppDestination.Profile -> ProfileScreen(accountRepository, backdrop)
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
