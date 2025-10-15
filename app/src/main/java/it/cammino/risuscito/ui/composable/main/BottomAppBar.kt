package it.cammino.risuscito.ui.composable.main

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.compose.AndroidFragment
import androidx.fragment.compose.rememberFragmentState
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.fragment.ConsegnatiFragment
import it.cammino.risuscito.ui.fragment.CustomListsFragment
import it.cammino.risuscito.ui.fragment.FavoritesFragment
import it.cammino.risuscito.ui.fragment.GeneralIndexFragment
import it.cammino.risuscito.ui.fragment.HistoryFragment

sealed class NavigationScreen(
    val route: String,
    val labelRes: Int,
    val iconRes: Int,
    val selectediconRes: Int
) {
    object GeneralIndex : NavigationScreen(
        "navigation_indexes",
        R.string.title_activity_general_index,
        R.drawable.format_list_bulleted_24px,
        R.drawable.format_list_bulleted_24px
    )

    object CustomLists : NavigationScreen(
        "navigation_lists",
        R.string.title_activity_custom_lists,
        R.drawable.view_carousel_24px,
        R.drawable.view_carousel_filled_24px
    )

    object Favorites : NavigationScreen(
        "navigation_favorites",
        R.string.action_favourites,
        R.drawable.bookmarks_24px,
        R.drawable.bookmarks_filled_24px
    )

    object Consegnati : NavigationScreen(
        "navigation_consegnati",
        R.string.title_activity_consegnati,
        R.drawable.fact_check_24px,
        R.drawable.fact_check_filled_24px
    )

    object History : NavigationScreen(
        "navigation_history",
        R.string.title_activity_history,
        R.drawable.history_24px,
        R.drawable.history_24px
    )

}

val bottomNavItems =
    listOf(
        NavigationScreen.GeneralIndex,
        NavigationScreen.CustomLists,
        NavigationScreen.Favorites,
        NavigationScreen.Consegnati,
        NavigationScreen.History
    )

@Composable
fun RisuscitoBottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    resetTab: MutableState<Boolean>
) {
    NavigationBar { // Sostituisce BottomNavigationView
        bottomNavItems.forEach { screen ->
            val label = stringResource(screen.labelRes)
            NavigationBarItem(
                icon = {
                    Icon(
                        painterResource(if (currentRoute == screen.route) screen.selectediconRes else screen.iconRes),
                        contentDescription = label
                    )
                },
                label = { Text(label) },
                selected = currentRoute == screen.route,
                onClick = {
                    resetTab.value = true
                    onNavigate(screen.route) },
                alwaysShowLabel = false
            )
        }
    }
}

// Definisce l'ordine degli item della BottomBar per determinare la direzione dello slide
private val bottomBarOrder = listOf(
    NavigationScreen.GeneralIndex.route,
    NavigationScreen.CustomLists.route,
    NavigationScreen.Favorites.route,
    NavigationScreen.Consegnati.route,
    NavigationScreen.History.route
)

// Funzioni helper per le transizioni
fun AnimatedContentTransitionScope<NavBackStackEntry>.sharedAxisEnter(
    targetRoute: String,
    durationMillis: Int = 300
): EnterTransition {
    val initialRoute = initialState.destination.route
    val initialIndex = initialRoute?.let { bottomBarOrder.indexOf(it) } ?: -1
    val targetIndex = bottomBarOrder.indexOf(targetRoute)

    return if (initialIndex < targetIndex) { // Navigazione "in avanti" (es. da Indice a Liste)
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            tween(durationMillis)
        ) +
                fadeIn(tween(durationMillis))
    } else { // Navigazione "all'indietro" (es. da Liste a Indice)
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            tween(durationMillis)
        ) +
                fadeIn(tween(durationMillis))
    }
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.sharedAxisExit(
    initialRoute: String, // Route della schermata che sta uscendo
    durationMillis: Int = 300
): ExitTransition {
    val targetRoute = targetState.destination.route
    val initialIndex = bottomBarOrder.indexOf(initialRoute)
    val targetIndex = targetRoute?.let { bottomBarOrder.indexOf(it) } ?: -1

    return if (initialIndex < targetIndex) { // Navigazione "in avanti"
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            tween(durationMillis)
        ) +
                fadeOut(tween(durationMillis))
    } else { // Navigazione "all'indietro"
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            tween(durationMillis)
        ) +
                fadeOut(tween(durationMillis))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavigationScreen.GeneralIndex.route,

        ) {

        val animationDuration = 300

        composable(
            NavigationScreen.GeneralIndex.route,
            enterTransition = {
                sharedAxisEnter(
                    NavigationScreen.GeneralIndex.route,
                    animationDuration
                )
            },
            exitTransition = {
                sharedAxisExit(
                    NavigationScreen.GeneralIndex.route,
                    animationDuration
                )
            },
            popEnterTransition = { // Di solito popEnter è l'inverso di exit della destinazione da cui si proviene
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(animationDuration)
                ) + fadeIn(tween(animationDuration))
            },
            popExitTransition = { // Di solito popExit è l'inverso di enter della destinazione a cui si andava
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(animationDuration)
                ) + fadeOut(tween(animationDuration))
            }) {
            val fragmentState = rememberFragmentState()
            AndroidFragment<GeneralIndexFragment>(fragmentState = fragmentState)
        }
        composable(
            NavigationScreen.CustomLists.route,
            enterTransition = {
                sharedAxisEnter(
                    NavigationScreen.GeneralIndex.route,
                    animationDuration
                )
            },
            exitTransition = {
                sharedAxisExit(
                    NavigationScreen.GeneralIndex.route,
                    animationDuration
                )
            },
            popEnterTransition = { // Di solito popEnter è l'inverso di exit della destinazione da cui si proviene
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(animationDuration)
                ) + fadeIn(tween(animationDuration))
            },
            popExitTransition = { // Di solito popExit è l'inverso di enter della destinazione a cui si andava
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(animationDuration)
                ) + fadeOut(tween(animationDuration))
            }) {
            val fragmentState = rememberFragmentState()
            AndroidFragment<CustomListsFragment>(fragmentState = fragmentState)
        }
        composable(
            NavigationScreen.Favorites.route,
            enterTransition = {
                sharedAxisEnter(
                    NavigationScreen.GeneralIndex.route,
                    animationDuration
                )
            },
            exitTransition = {
                sharedAxisExit(
                    NavigationScreen.GeneralIndex.route,
                    animationDuration
                )
            },
            popEnterTransition = { // Di solito popEnter è l'inverso di exit della destinazione da cui si proviene
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(animationDuration)
                ) + fadeIn(tween(animationDuration))
            },
            popExitTransition = { // Di solito popExit è l'inverso di enter della destinazione a cui si andava
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(animationDuration)
                ) + fadeOut(tween(animationDuration))
            }) {
            val fragmentState = rememberFragmentState()
            AndroidFragment<FavoritesFragment>(fragmentState = fragmentState)
        }
        composable(
            NavigationScreen.Consegnati.route,
            enterTransition = {
                sharedAxisEnter(
                    NavigationScreen.GeneralIndex.route,
                    animationDuration
                )
            },
            exitTransition = {
                sharedAxisExit(
                    NavigationScreen.GeneralIndex.route,
                    animationDuration
                )
            },
            popEnterTransition = { // Di solito popEnter è l'inverso di exit della destinazione da cui si proviene
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(animationDuration)
                ) + fadeIn(tween(animationDuration))
            },
            popExitTransition = { // Di solito popExit è l'inverso di enter della destinazione a cui si andava
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(animationDuration)
                ) + fadeOut(tween(animationDuration))
            }) {
            val fragmentState = rememberFragmentState()
            AndroidFragment<ConsegnatiFragment>(fragmentState = fragmentState)
        }
        composable(
            NavigationScreen.History.route,
            enterTransition = {
                sharedAxisEnter(
                    NavigationScreen.GeneralIndex.route,
                    animationDuration
                )
            },
            exitTransition = {
                sharedAxisExit(
                    NavigationScreen.GeneralIndex.route,
                    animationDuration
                )
            },
            popEnterTransition = { // Di solito popEnter è l'inverso di exit della destinazione da cui si proviene
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(animationDuration)
                ) + fadeIn(tween(animationDuration))
            },
            popExitTransition = { // Di solito popExit è l'inverso di enter della destinazione a cui si andava
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(animationDuration)
                ) + fadeOut(tween(animationDuration))
            }) {
            val fragmentState = rememberFragmentState()
            AndroidFragment<HistoryFragment>(fragmentState = fragmentState)
        }
    }
}