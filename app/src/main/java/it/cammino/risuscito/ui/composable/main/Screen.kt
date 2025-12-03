package it.cammino.risuscito.ui.composable.main

import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.util.Log
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarScrollBehavior
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.material.color.MaterialColors
import it.cammino.risuscito.R
import it.cammino.risuscito.items.CantoViewData
import it.cammino.risuscito.ui.composable.CantoView
import it.cammino.risuscito.ui.composable.EmptyListView
import it.cammino.risuscito.ui.composable.dialogs.ChangelogBottomSheet
import it.cammino.risuscito.ui.composable.dialogs.RisuscitoBottomSheet
import it.cammino.risuscito.ui.composable.hasNavigationBar
import it.cammino.risuscito.ui.composable.hasTwoPanes
import it.cammino.risuscito.viewmodels.SharedBottomSheetViewModel
import it.cammino.risuscito.viewmodels.SharedScrollViewModel
import it.cammino.risuscito.viewmodels.SharedSearchViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun MainScreen(
    sharedScrollViewModel: SharedScrollViewModel,
    navController: NavHostController = rememberNavController(),
    isActionMode: Boolean = false,
    actionModeMenu: List<ActionModeItem> = emptyList(),
    hideNavigation: Boolean = false,
    onActionModeClick: (ActionModeItem) -> Unit = {},
    contextualTitle: String = "",
    showLoadingBar: Boolean = false,
    showTabs: Boolean = false,
    selectedTabIndex: MutableIntState,
    tabsList: List<Destination>? = emptyList(),
    resetTab: MutableState<Boolean>,
    snackbarHostState: SnackbarHostState,
    showFab: Boolean,
    fabActions: List<FabActionItem>? = emptyList(),
    fabExpanded: MutableState<Boolean> = mutableStateOf(false),
    sharedSearchViewModel: SharedSearchViewModel,
    optionMenu: List<OptionMenuItem>? = emptyList(),
    onOptionMenuClick: (String) -> Unit = {},
    fabIconRes: Int,
    onFabClick: (String) -> Unit = {},
    loggedIn: Boolean = false,
    profilePhotoUrl: String = "",
    onProfileItemClick: (Boolean) -> Unit = {},
    searchBarState: SearchBarState,
    bottomSheetViewModel: SharedBottomSheetViewModel,
    bottomSheetOnItemClick: (ResolveInfo) -> Unit,
    pm: PackageManager,
    cantoData: CantoViewData,
    navigateBack: MutableState<Boolean> = mutableStateOf(false)
) {
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val scrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()

    LaunchedEffect(
        scrollBehavior,
        sharedScrollViewModel
    ) {
        sharedScrollViewModel.setScrollBehavior(scrollBehavior)
    }

    Log.d("MainScreen", "hasNavigationBar: ${hasNavigationBar()}")

    val mainScaffold =
        @Composable {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    TopAppBarWithSearch(
                        searchBarState = searchBarState,
                        scrollBehavior = scrollBehavior,
                        isActionMode = isActionMode,
                        actionModeMenu = actionModeMenu,
                        hideNavigation = hideNavigation,
                        onActionModeClick = onActionModeClick,
                        contextualTitle = contextualTitle,
                        sharedSearchViewModel = sharedSearchViewModel,
                        optionMenu = optionMenu,
                        onOptionMenuClick = onOptionMenuClick,
                        loggedIn = loggedIn,
                        profilePhotoUrl = profilePhotoUrl,
                        onProfileItemClick = onProfileItemClick,
                    )
                },
                floatingActionButton = {
                    if (showFab) {
                        RisuscitoFab(
                            actions = fabActions,
                            expanded = fabExpanded.value,
                            onExpandedChange = { fabExpanded.value = it },
                            onFabActionClick = onFabClick,
                            mainIconRes = fabIconRes,
                        )
                    }
                },
                floatingActionButtonPosition = FabPosition.End,
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState)
                },
            ) { innerPadding ->
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (showTabs) {
                            RisuscitoTabs(
                                selectedTabIndex = selectedTabIndex,
                                tabsList = tabsList
                            )
                        }
                        AppNavigationHost(navController = navController)
                    }

                }

                RisuscitoBottomSheet(
                    bottomSheetViewModel = bottomSheetViewModel,
                    onItemClick = bottomSheetOnItemClick,
                    pm = pm
                )

                ChangelogBottomSheet()

            }
        }


    val itemsList = if (hasNavigationBar()) bottomNavItems else navitagionRailItems

    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator<CantoViewData>()
    val backNavigationBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange

    LaunchedEffect(cantoData) {
        snapshotFlow { cantoData }
            .distinctUntilChanged()
            .collect {
                Log.d("MainScreen", "cantoData: ${it.idCanto}")
                // Navigate to the detail pane with the passed item
                if (it.idCanto > 0) {
                    scope.launch {
                        scaffoldNavigator.navigateTo(
                            ListDetailPaneScaffoldRole.Detail,
                            it
                        )
                    }
                }
            }
    }

    LaunchedEffect(navigateBack) {
        snapshotFlow { navigateBack.value }
            .distinctUntilChanged()
            .collect {
                Log.d("MainScreen", "navigateBack: $it")
                // Navigate to the detail pane with the passed item
                if (it) {
                    scope.launch {
                        scaffoldNavigator.navigateBack(backNavigationBehavior)
                    }
                    navigateBack.value = false
                }
            }
    }

    val showRailIcon = !hasNavigationBar()

    val iconHeaderColor = Color(
        MaterialColors.harmonize(
            colorResource(R.color.ic_launcher_background).toArgb(),
            MaterialTheme.colorScheme.primary.toArgb()
        )
    )

    val iconHeaderShape = MaterialShapes.Circle.toShape()

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasTwoPanes()) {
            NavigableListDetailPaneScaffold(
                navigator = scaffoldNavigator,
                listPane = {
                    Box(modifier = Modifier.preferredWidth(0.5f)) {
                        NavigationSuiteScaffold(
                            navigationSuiteItems = {
                                // Chiama la nuova funzione passando i parametri necessari
                                myNavigationSuiteItems(
                                    showRailIcon = showRailIcon,
                                    iconHeaderColor = iconHeaderColor,
                                    iconHeaderShape = iconHeaderShape,
                                    itemsList = itemsList,
                                    currentRoute = currentRoute,
                                    resetTab = resetTab,
                                    scrollBehavior = scrollBehavior, // Assicurati di passarlo
                                    navController = navController
                                )
                            },
                            content = mainScaffold
                        )
                    }
                },
                detailPane = {
                    AnimatedPane(
                        enterTransition =
                            slideInHorizontally(tween(1000, easing = EaseIn)),
                        exitTransition = slideOutHorizontally(tween(1000, easing = EaseIn))
                    ) {
                        // Show the detail pane content if selected item is available
                        scaffoldNavigator.currentDestination?.contentKey?.let { cantoData ->
                            key(cantoData.idCanto) {
                                CantoView(cantoData)
                            }
                        } ?: EmptyListView(
                            iconRes = R.drawable.lyrics_24px,
                            textRes = R.string.select_one_song
                        )
                    }
                }
            )
        }
        else {
            NavigationSuiteScaffold(
                navigationSuiteItems = {
                    // Chiama la nuova funzione passando i parametri necessari
                    myNavigationSuiteItems(
                        showRailIcon = showRailIcon,
                        iconHeaderColor = iconHeaderColor,
                        iconHeaderShape = iconHeaderShape,
                        itemsList = itemsList,
                        currentRoute = currentRoute,
                        resetTab = resetTab,
                        scrollBehavior = scrollBehavior, // Assicurati di passarlo
                        navController = navController
                    )
                },
                content = mainScaffold
            )
        }

        if (showLoadingBar) {
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
private fun NavigationSuiteScope.myNavigationSuiteItems(
    showRailIcon: Boolean,
    iconHeaderColor: Color,iconHeaderShape: Shape,
    itemsList: List<NavigationScreen>,
    currentRoute: String?,
    resetTab: MutableState<Boolean>,
    scrollBehavior: SearchBarScrollBehavior,
    navController: NavHostController
) {
    if (showRailIcon) {
        item(
            icon = {
                Icon(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = iconHeaderColor,
                            shape = iconHeaderShape
                        ),
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = stringResource(R.string.copertina),
                    tint = Color.White
                )
            },
            selected = false,
            onClick = {},
        )
    }
    itemsList.forEach { screen ->
        item(
            icon = {
                Icon(
                    painterResource(if (currentRoute == screen.route) screen.selectediconRes else screen.iconRes),
                    contentDescription = stringResource(screen.labelRes)
                )
            },
            label = { Text(stringResource(screen.labelRes)) },
            selected = currentRoute == screen.route,
            onClick = {
                resetTab.value = true
                scrollBehavior.scrollOffset = 0F
                scrollBehavior.contentOffset = 0f
                navController.navigate(screen.route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            },
        )
    }
}