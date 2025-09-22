package it.cammino.risuscito.ui.composable.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ViewCarousel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.fragment.compose.AndroidFragment
import androidx.fragment.compose.rememberFragmentState
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
    val icon: ImageVector
) {
    object GeneralIndex : NavigationScreen(
        "navigation_indexes",
        R.string.title_activity_general_index,
        Icons.AutoMirrored.Filled.FormatListBulleted
    )

    object CustomLists : NavigationScreen(
        "navigation_lists",
        R.string.title_activity_custom_lists,
        Icons.Outlined.ViewCarousel
    )

    object Favorites : NavigationScreen(
        "navigation_favorites",
        R.string.action_favourites,
        Icons.Outlined.Bookmarks
    )

    object Consegnati : NavigationScreen(
        "navigation_consegnati",
        R.string.title_activity_consegnati,
        Icons.AutoMirrored.Outlined.FactCheck
    )

    object History : NavigationScreen(
        "navigation_history",
        R.string.title_activity_history,
        Icons.Outlined.History
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
    onNavigate: (String) -> Unit
) {
    NavigationBar { // Sostituisce BottomNavigationView
        bottomNavItems.forEach { screen ->
            val label = stringResource(screen.labelRes)
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = label) },
                label = { Text(label) },
                selected = currentRoute == screen.route,
                onClick = { onNavigate(screen.route) },
                alwaysShowLabel = false
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavigationScreen.GeneralIndex.route,

    ) {
        composable(NavigationScreen.GeneralIndex.route) {
            val fragmentState = rememberFragmentState()
            AndroidFragment<GeneralIndexFragment>(fragmentState = fragmentState)
        }
        composable(NavigationScreen.CustomLists.route) {
            val fragmentState = rememberFragmentState()
            AndroidFragment<CustomListsFragment>(fragmentState = fragmentState)
        }
        composable(NavigationScreen.Favorites.route) {
            val fragmentState = rememberFragmentState()
            AndroidFragment<FavoritesFragment>(fragmentState = fragmentState)
        }
        composable(NavigationScreen.Consegnati.route) {
            val fragmentState = rememberFragmentState()
            AndroidFragment<ConsegnatiFragment>(fragmentState = fragmentState)
        }
        composable(NavigationScreen.History.route) {
            val fragmentState = rememberFragmentState()
            AndroidFragment<HistoryFragment>(fragmentState = fragmentState)
        }
    }
}