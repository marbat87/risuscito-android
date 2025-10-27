package it.cammino.risuscito.ui.activity

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.compose.AndroidFragment
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.composable.main.StatusBarProtection
import it.cammino.risuscito.ui.composable.theme.RisuscitoTheme
import it.cammino.risuscito.ui.fragment.AboutFragment
import it.cammino.risuscito.utils.OSUtils
import it.cammino.risuscito.utils.extension.finishAfterTransitionWrapper
import it.cammino.risuscito.utils.extension.setEnterTransition
import it.cammino.risuscito.utils.extension.slideOutRight

class AboutActivity : ThemeableActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setEnterTransition()
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
                                IconButton(onClick = { onBackPressedAction() }) {
                                    Icon(
                                        painter = painterResource(R.drawable.arrow_back_24px),
                                        contentDescription = stringResource(R.string.material_drawer_close)
                                    )
                                }
                            },
                            scrollBehavior = scrollBehavior
                        )
                    }
                ) { innerPadding ->
                    AndroidFragment<AboutFragment>(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    )
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
        if (OSUtils.isObySamsung()) {
            finish()
            slideOutRight()
        } else
            finishAfterTransitionWrapper()
    }
}
