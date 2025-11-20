package it.cammino.risuscito.ui.composable.main

import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import it.cammino.risuscito.ui.composable.dialogs.ChangelogBottomSheet
import it.cammino.risuscito.ui.composable.dialogs.RisuscitoBottomSheet
import it.cammino.risuscito.ui.composable.hasDrawer
import it.cammino.risuscito.viewmodels.SharedBottomSheetViewModel
import it.cammino.risuscito.viewmodels.SharedScrollViewModel
import it.cammino.risuscito.viewmodels.SharedSearchViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    sharedScrollViewModel: SharedScrollViewModel,
    navController: NavHostController = rememberNavController(),
    onDrawerItemClick: (DrawerItem) -> Unit = {},
    isActionMode: Boolean = false,
    actionModeMenu: List<ActionModeItem> = emptyList(),
    hideNavigation: Boolean = false,
    onActionModeClick: (ActionModeItem) -> Unit = {},
    contextualTitle: String = "",
    drawerState: DrawerState = DrawerState(initialValue = DrawerValue.Closed),
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
    onProfileClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    bottomSheetViewModel: SharedBottomSheetViewModel,
    bottomSheetOnItemClick: (ResolveInfo) -> Unit,
    pm: PackageManager
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

    Log.d("MainScreen", "hasDrawer: ${hasDrawer()}")

    val mainScaffold =
        @Composable {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    TopAppBarWithSearch(
                        onMenuClick = { scope.launch { drawerState.open() } },
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
                        onProfileClick = onProfileClick,
                        onLoginClick = onLoginClick
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
                        Box {
                            AppNavigationHost(navController = navController)

                            // LinearProgressIndicator sovrapposto
                            if (showLoadingBar) {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.TopCenter)
                                )
                            }
                        }
                    }

//                if (fabExpanded.value) {
//                    Surface(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .clickable(
//                                enabled = true,
//                                onClickLabel = stringResource(R.string.material_drawer_close),
//                                onClick = { fabExpanded.value = false }
//                            ),
//                        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)
//                    ) {}
//                }

                }

                RisuscitoBottomSheet(
                    bottomSheetViewModel = bottomSheetViewModel,
                    onItemClick = bottomSheetOnItemClick,
                    pm = pm
                )

                ChangelogBottomSheet()

            }
        }

    val mainScaffoldWithDrawer =
        @Composable {
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = drawerState.isOpen,
                drawerContent = {
                    AppDrawerContent(
                        onItemClick = { route ->
                            onDrawerItemClick(route)
                            scope.launch { drawerState.close() }
                        }
                    )
                },
                content = mainScaffold
            )
        }

    val itemsList = if (hasDrawer()) bottomNavItems else navitagionRailItems

    // 2. USA LA NUOVA API DI NavigationSuiteScaffold
    NavigationSuiteScaffold(
        // Fornisci le destinazioni qui
        navigationSuiteItems = {
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
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                )
            }
        },
        content = if (hasDrawer()) mainScaffoldWithDrawer else mainScaffold
    )

}