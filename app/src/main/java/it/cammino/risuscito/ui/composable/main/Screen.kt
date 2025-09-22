package it.cammino.risuscito.ui.composable.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    selectedTabIndex: MutableIntState = mutableIntStateOf(0),
    tabsList: List<Destination>? = emptyList(),
    pagerState: PagerState,
    snackbarHostState: SnackbarHostState,
    showFab: Boolean = false,
    sharedSearchViewModel: SharedSearchViewModel
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
                onCloseDrawer = { scope.launch { drawerState.close() } }
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
                    sharedSearchViewModel = sharedSearchViewModel
                )
                // Se hai anche i TabLayout, la loro gestione andrà qui,
                // probabilmente sotto la SearchBar o come parte di essa se integrati.
                // Se i TabLayout sono condizionali, mostra/nascondi qui.
            },
            bottomBar = {
                RisuscitoBottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            },
            floatingActionButton = {
                // Sostituisci con la tua implementazione di SpeedDialView
                // Potrebbe essere necessario un Composable personalizzato o una libreria Compose.
                // Per ora, un FAB standard come placeholder.
                if (showFab) {
                    ExtendedFloatingActionButton(
                        text = { Text("Azione") },
                        icon = { Icon(Icons.Filled.Add, contentDescription = "Azione") },
                        onClick = { /* Logica FAB principale */ }
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showTabs) {
                    RisuscitoTabs(
                        selectedTabIndex = selectedTabIndex,
                        tabsList = tabsList,
                        pagerState = pagerState
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
        }
    }
}