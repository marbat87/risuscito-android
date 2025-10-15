package it.cammino.risuscito.ui.composable.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import it.cammino.risuscito.viewmodels.SharedScrollViewModel
import it.cammino.risuscito.viewmodels.SharedSearchViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    sharedScrollViewModel: SharedScrollViewModel,
    navController: NavHostController = rememberNavController(),
    onDrawerItemClick: (String) -> Unit = {},
    isActionMode: Boolean = false,
    actionModeMenu: List<ActionModeItem> = emptyList(),
    hideNavigation: Boolean = false,
    onActionModeClick: (String) -> Unit = {},
    contextualTitle: String = "",
    drawerState: DrawerState = DrawerState(initialValue = DrawerValue.Closed),
    searchBarState: SearchBarState = rememberSearchBarState(),
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
    fabIcon: ImageVector,
    onFabClick: (FabActionItem) -> Unit = {},
    loggedIn: Boolean = false,
    profilePhotoUrl: String = "",
    onProfileClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    // Per ottenere la route corrente per evidenziare l'item nel drawer/bottomNav
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    // Imposta lo scrollBehavior nel ViewModel condiviso
    LaunchedEffect(
        scrollBehavior,
        sharedScrollViewModel
    ) {
        sharedScrollViewModel.setScrollBehavior(scrollBehavior)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            AppDrawerContent(
                onItemClick = { route ->
                    scope.launch { drawerState.close() }
                    onDrawerItemClick(route)
                },
                onCloseDrawer = { scope.launch { drawerState.close() } },
                drawerState = drawerState
            )
        }
    ) {
        Scaffold(
            // 2. Collega il scrollBehavior allo Scaffold tramite il Modifier.nestedScroll
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBarWithSearch(
                    searchBarState = searchBarState,
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
                // Se hai anche i TabLayout, la loro gestione andrà qui,
                // probabilmente sotto la SearchBar o come parte di essa se integrati.
                // Se i TabLayout sono condizionali, mostra/nascondi qui.
            },
            bottomBar = {
                RisuscitoBottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        scrollBehavior.state.heightOffset = 0F
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
//                            restoreState = true
                        }
                    },
                    resetTab = resetTab
                )
            },
            floatingActionButton = {
                if (showFab) {
                    RisuscitoFab(
                        actions = fabActions,
                        expanded = fabExpanded.value,
                        onExpandedChange = { fabExpanded.value = it },
                        onFabActionClick = onFabClick,
                        mainIcon = fabIcon,
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
        }
    }
}