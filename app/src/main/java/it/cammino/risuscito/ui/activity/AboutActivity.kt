package it.cammino.risuscito.ui.activity

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.fragment.compose.AndroidFragment
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.composable.ClassicBackNavitagionButton
import it.cammino.risuscito.ui.composable.main.StatusBarProtection
import it.cammino.risuscito.ui.composable.theme.RisuscitoTheme
import it.cammino.risuscito.ui.fragment.AboutFragment
import it.cammino.risuscito.utils.extension.slideOutRight

class AboutActivity : ThemeableActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            RisuscitoTheme {

                val scrollBehavior =
                    TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(stringResource(R.string.title_activity_about))
                            },
                            navigationIcon = {
                                ClassicBackNavitagionButton(
                                    onBackPressedAction = { onBackPressedAction() }
                                )
                            },
                            scrollBehavior = scrollBehavior,
                            colors = TopAppBarDefaults.topAppBarColors().copy(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        )
                    }
                ) { innerPadding ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        AndroidFragment<AboutFragment>(modifier = Modifier.fillMaxSize())
                    }
                }

                // After drawing main content, draw status bar protection
                StatusBarProtection()

                BackHandler {
                    onBackPressedAction()
                }
            }

        }
    }

    private fun onBackPressedAction() {
        finish()
        slideOutRight()
    }
}
