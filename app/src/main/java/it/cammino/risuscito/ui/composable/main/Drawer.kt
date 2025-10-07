package it.cammino.risuscito.ui.composable.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.color.MaterialColors
import it.cammino.risuscito.R

enum class Drawer(
    val route: String,
    val label: Int,
    val icon: ImageVector? = null
) {
    SETTINGS("navigation_settings", R.string.title_activity_settings, Icons.Outlined.Settings),
    INFO("navigation_changelog", R.string.title_activity_about, Icons.Outlined.Info)
}

val drawerNavItems =
    listOf(Drawer.SETTINGS, Drawer.INFO)

@Preview
@Composable
fun AppDrawerContent(
    onItemClick: (String) -> Unit = {},
    onCloseDrawer: () -> Unit = {},
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)
) {
    ModalDrawerSheet(drawerState = drawerState) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp, 0.dp),
                color = Color(
                    MaterialColors.harmonize(
                        colorResource(R.color.ic_launcher_background).toArgb(),
                        MaterialTheme.colorScheme.primary.toArgb()
                    )
                )
            )

            Spacer(Modifier.height(20.dp))
            HorizontalDivider()
            drawerNavItems.forEach { screen ->
                val title = stringResource(screen.label)
                NavigationDrawerItem(
                    icon = {
                        screen.icon?.let {
                            Icon(
                                screen.icon,
                                contentDescription = title
                            )
                        }
                    },
                    label = { Text(title) },
                    selected = false,
                    onClick = {
                        onItemClick(screen.route)
                    }
                )
            }
        }
    }
}