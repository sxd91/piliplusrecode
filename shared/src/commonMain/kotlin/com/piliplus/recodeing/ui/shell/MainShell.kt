package com.piliplus.recodeing.ui.shell

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
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
import com.piliplus.recodeing.core.design.LocalLiquidGlassEnabled
import com.piliplus.recodeing.core.design.PiliGlassDefaults
import com.piliplus.recodeing.core.design.platformSupportsLiquidGlass
import com.piliplus.recodeing.ui.dynamics.DynamicsScreen
import com.piliplus.recodeing.ui.home.HomeScreen
import com.piliplus.recodeing.ui.profile.ProfileScreen
import com.piliplus.recodeing.ui.search.SearchScreen
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
    var secondaryStack by remember { mutableStateOf<List<SecondaryDestination>>(emptyList()) }
    val secondary = secondaryStack.lastOrNull()
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
            secondary = secondary,
            onDestinationSelected = {
                current = it
                secondaryStack = emptyList()
            },
            onSecondarySelected = { destination ->
                secondaryStack = when {
                    destination is SecondaryDestination.Video && secondaryStack.lastOrNull() is SecondaryDestination.Video -> {
                        secondaryStack.dropLast(1) + destination
                    }
                    else -> secondaryStack + destination
                }
            },
            onSecondaryBack = { secondaryStack = secondaryStack.dropLast(1) },
            accountRepository = accountRepository,
            settingsStateHolder = settingsStateHolder,
            glassEnabled = settings.glassEnabled && platformSupportsLiquidGlass(),
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
    secondary: SecondaryDestination?,
    onDestinationSelected: (AppDestination) -> Unit,
    onSecondarySelected: (SecondaryDestination) -> Unit,
    onSecondaryBack: () -> Unit,
    accountRepository: AccountRepository,
    settingsStateHolder: SettingsStateHolder,
    glassEnabled: Boolean,
    floatingBottomBar: Boolean,
) {
    val backgroundColor = MiuixTheme.colorScheme.background
    val backdrop = rememberLayerBackdrop(
        onDraw = {
            drawRect(backgroundColor)
            drawContent()
        },
    )

    CompositionLocalProvider(LocalLiquidGlassEnabled provides glassEnabled) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .then(if (glassEnabled) Modifier.layerBackdrop(backdrop) else Modifier),
        ) {
            when (navigationMode) {
                WindowNavigationMode.CompactBottomBar -> CompactShell(
                    current = current,
                    secondary = secondary,
                    onDestinationSelected = onDestinationSelected,
                    onSecondarySelected = onSecondarySelected,
                    onSecondaryBack = onSecondaryBack,
                    accountRepository = accountRepository,
                    settingsStateHolder = settingsStateHolder,
                    backdrop = backdrop,
                    glassEnabled = glassEnabled,
                    floatingBottomBar = floatingBottomBar,
                )
                WindowNavigationMode.SideRail -> RailShell(
                    current = current,
                    secondary = secondary,
                    onDestinationSelected = onDestinationSelected,
                    onSecondarySelected = onSecondarySelected,
                    onSecondaryBack = onSecondaryBack,
                    accountRepository = accountRepository,
                    settingsStateHolder = settingsStateHolder,
                    backdrop = backdrop,
                    glassEnabled = glassEnabled,
                )
            }
        }
    }
}

@Composable
private fun CompactShell(
    current: AppDestination,
    secondary: SecondaryDestination?,
    onDestinationSelected: (AppDestination) -> Unit,
    onSecondarySelected: (SecondaryDestination) -> Unit,
    onSecondaryBack: () -> Unit,
    accountRepository: AccountRepository,
    settingsStateHolder: SettingsStateHolder,
    backdrop: Backdrop,
    glassEnabled: Boolean,
    floatingBottomBar: Boolean,
) {
    if (secondary != null) {
        SecondaryContent(
            destination = secondary,
            onBack = onSecondaryBack,
            onSecondarySelected = onSecondarySelected,
            accountRepository = accountRepository,
            settingsStateHolder = settingsStateHolder,
            backdrop = backdrop,
        )
        return
    }
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { ShellTopBar(current, scrollBehavior) },
        bottomBar = {
            GlassBottomNavigation(
                selected = current,
                onSelected = onDestinationSelected,
                backdrop = backdrop,
                glassEnabled = glassEnabled,
                modifier = Modifier.padding(horizontal = if (floatingBottomBar) 16.dp else 0.dp),
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(bottom = paddingValues.calculateBottomPadding()),
        ) {
            RootDestination(
                current = current,
                onSecondarySelected = onSecondarySelected,
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
    secondary: SecondaryDestination?,
    onDestinationSelected: (AppDestination) -> Unit,
    onSecondarySelected: (SecondaryDestination) -> Unit,
    onSecondaryBack: () -> Unit,
    accountRepository: AccountRepository,
    settingsStateHolder: SettingsStateHolder,
    backdrop: Backdrop,
    glassEnabled: Boolean,
) {
    if (secondary != null) {
        SecondaryContent(
            destination = secondary,
            onBack = onSecondaryBack,
            onSecondarySelected = onSecondarySelected,
            accountRepository = accountRepository,
            settingsStateHolder = settingsStateHolder,
            backdrop = backdrop,
        )
        return
    }
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { ShellTopBar(current, scrollBehavior) },
    ) { paddingValues ->
        Row(Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())) {
            NavigationRail(
                modifier = Modifier.widthIn(max = 96.dp),
                color = if (glassEnabled) Color.Transparent else MiuixTheme.colorScheme.surface,
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
                RootDestination(
                    current = current,
                    onSecondarySelected = onSecondarySelected,
                    accountRepository = accountRepository,
                    settingsStateHolder = settingsStateHolder,
                    backdrop = backdrop,
                )
            }
        }
    }
}

@Composable
private fun ShellTopBar(current: AppDestination, scrollBehavior: ScrollBehavior) {
    TopAppBar(
        title = current.title,
        largeTitle = when (current) {
            AppDestination.Home -> "reliqliquid"
            AppDestination.Dynamics -> "动态"
            AppDestination.Profile -> "个人中心"
            AppDestination.Settings -> "设置"
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun RootDestination(
    current: AppDestination,
    onSecondarySelected: (SecondaryDestination) -> Unit,
    accountRepository: AccountRepository,
    settingsStateHolder: SettingsStateHolder,
    backdrop: Backdrop,
) {
    AnimatedContent(
        targetState = current,
        transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.985f)).togetherWith(fadeOut() + scaleOut(targetScale = 0.985f)) },
    ) { destination ->
        when (destination) {
            AppDestination.Home -> HomeScreen(
                backdrop = backdrop,
                feedColumns = settingsStateHolder.state.homeFeedColumns,
                onSearch = { onSecondarySelected(SecondaryDestination.Search) },
                onVideoSelected = { onSecondarySelected(SecondaryDestination.Video(it)) },
            )
            AppDestination.Dynamics -> DynamicsScreen(
                accountRepository = accountRepository,
                backdrop = backdrop,
                onVideoSelected = { onSecondarySelected(SecondaryDestination.Video(it)) },
            )
            AppDestination.Profile -> ProfileScreen(accountRepository, backdrop)
            AppDestination.Settings -> SettingsPage(backdrop, settingsStateHolder)
        }
    }
}

@Composable
private fun SecondaryContent(
    destination: SecondaryDestination,
    onBack: () -> Unit,
    onSecondarySelected: (SecondaryDestination) -> Unit,
    accountRepository: AccountRepository,
    settingsStateHolder: SettingsStateHolder,
    backdrop: Backdrop,
) {
    AnimatedContent(
        targetState = destination,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.985f)).togetherWith(fadeOut() + scaleOut(targetScale = 0.985f)) },
    ) { target ->
        when (target) {
            SecondaryDestination.Search -> SearchScreen(
                backdrop = backdrop,
                onBack = onBack,
                onVideoSelected = { onSecondarySelected(SecondaryDestination.Video(it)) },
            )
            is SecondaryDestination.Video -> VideoDetailScreen(
                bvid = target.bvid,
                backdrop = backdrop,
                accountRepository = accountRepository,
                settings = settingsStateHolder.state,
                onBack = onBack,
                onPlay = { },
                onVideoSelected = { onSecondarySelected(SecondaryDestination.Video(it)) },
                onAuthorSelected = { },
            )
        }
    }
}

@Composable
private fun GlassBottomNavigation(
    selected: AppDestination,
    onSelected: (AppDestination) -> Unit,
    backdrop: Backdrop,
    glassEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val glassColor = MiuixTheme.colorScheme.surface.copy(alpha = PiliGlassDefaults.SurfaceAlpha)
    FloatingNavigationBar(
        modifier = modifier.then(
            if (glassEnabled) {
                Modifier.drawBackdrop(
                    backdrop = backdrop,
                    shape = { RoundedCornerShape(PiliGlassDefaults.NavigationCornerRadius) },
                    effects = {
                        vibrancy()
                        blur(8.dp.toPx())
                        lens(24.dp.toPx(), 24.dp.toPx(), depthEffect = true)
                    },
                    onDrawSurface = { drawRect(glassColor) },
                )
            } else {
                Modifier.background(MiuixTheme.colorScheme.surface)
            },
        ),
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
