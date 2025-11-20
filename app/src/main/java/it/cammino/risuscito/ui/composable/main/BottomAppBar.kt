package it.cammino.risuscito.ui.composable.main

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.compose.AndroidFragment
import androidx.fragment.compose.rememberFragmentState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.fragment.AboutFragment
import it.cammino.risuscito.ui.fragment.ConsegnatiFragment
import it.cammino.risuscito.ui.fragment.CustomListsFragment
import it.cammino.risuscito.ui.fragment.FavoritesFragment
import it.cammino.risuscito.ui.fragment.GeneralIndexFragment
import it.cammino.risuscito.ui.fragment.HistoryFragment
import it.cammino.risuscito.ui.fragment.SettingsFragment

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

    object Settings : NavigationScreen(
        "navigation_settings",
        R.string.title_activity_settings,
        R.drawable.settings_24px,
        R.drawable.settings_filled_24px
    )

    object Info : NavigationScreen(
        "navigation_info",
        R.string.title_activity_about,
        R.drawable.info_24px,
        R.drawable.info_filled_24px
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

val navitagionRailItems =
    listOf(
        NavigationScreen.GeneralIndex,
        NavigationScreen.CustomLists,
        NavigationScreen.Favorites,
        NavigationScreen.Consegnati,
        NavigationScreen.History,
        NavigationScreen.Settings,
        NavigationScreen.Info
    )

const val animationDuration = 300

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavigationScreen.GeneralIndex.route,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Up,
                tween(animationDuration, easing = EaseIn)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Down,
                tween(animationDuration, easing = EaseOut)
            )
        },
        popEnterTransition = { // Di solito popEnter è l'inverso di exit della destinazione da cui si proviene
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Up,
                tween(animationDuration, easing = EaseIn)
            )
        },
        popExitTransition = { // Di solito popExit è l'inverso di enter della destinazione a cui si andava
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Down,
                tween(animationDuration, easing = EaseOut)
            )
        }
    ) {

        composable(
            NavigationScreen.GeneralIndex.route
        ) {
            val fragmentState = rememberFragmentState()
            AndroidFragment<GeneralIndexFragment>(fragmentState = fragmentState)
        }
        composable(
            NavigationScreen.CustomLists.route
        ) {
            val fragmentState = rememberFragmentState()
            AndroidFragment<CustomListsFragment>(fragmentState = fragmentState)
        }
        composable(
            NavigationScreen.Favorites.route
        ) {
            val fragmentState = rememberFragmentState()
            AndroidFragment<FavoritesFragment>(fragmentState = fragmentState)
        }
        composable(
            NavigationScreen.Consegnati.route
        ) {
            val fragmentState = rememberFragmentState()
            AndroidFragment<ConsegnatiFragment>(fragmentState = fragmentState)
        }
        composable(
            NavigationScreen.History.route
        ) {
            val fragmentState = rememberFragmentState()
            AndroidFragment<HistoryFragment>(fragmentState = fragmentState)
        }
        composable(
            NavigationScreen.Settings.route
        ) {
            val fragmentState = rememberFragmentState()
            AndroidFragment<SettingsFragment>(fragmentState = fragmentState)
        }
        composable(
            NavigationScreen.Info.route
        ) {
            val fragmentState = rememberFragmentState()
            AndroidFragment<AboutFragment>(fragmentState = fragmentState, modifier = Modifier.fillMaxSize())
        }
    }
}