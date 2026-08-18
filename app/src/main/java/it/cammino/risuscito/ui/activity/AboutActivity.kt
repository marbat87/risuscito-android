package it.cammino.risuscito.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.composable.ClassicBackNavigationButton
import it.cammino.risuscito.ui.composable.dialogs.WebViewDialog
import it.cammino.risuscito.ui.composable.main.StatusBarProtection
import it.cammino.risuscito.ui.composable.theme.RisuscitoTheme
import it.cammino.risuscito.ui.composable.views.AppInfoScreen
import it.cammino.risuscito.utils.OSUtils
import it.cammino.risuscito.utils.extension.enableEdgeToEdgeWrapper
import it.cammino.risuscito.utils.extension.slideOutRight
import it.cammino.risuscito.utils.extension.startActivityWithTransition

class AboutActivity : ThemeableActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdgeWrapper()

        setContent {

            val context = LocalContext.current
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = packageInfo.versionName ?: "1.0.0"
            val versionCode = OSUtils.getVersionCode(context)


            val showWebViewDialog by webViewDialogManagerViewModel.showWebViewDialog.observeAsState()

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
                                ClassicBackNavigationButton(
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

                    val listModifier = Modifier
                        .fillMaxSize()
                        .then(
                            Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                        )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        AppInfoScreen(
                            versionName = versionName,
                            versionCode = versionCode,
                            modifier = listModifier,
                            onChangelogClick = {
                                startActivityWithTransition(
                                    Intent(
                                        this@AboutActivity, ChangelogActivity::class.java
                                    )
                                )
                            },
                            onPrivacyClick = { webViewDialogManagerViewModel.showWebViewDialog.value = true }
                        )
                    }
                }

                if (showWebViewDialog == true) {
                    WebViewDialog(
                        dialogTitleRes = R.string.privacy,
                        iconRes = R.drawable.policy_24px,
                        htmlString = "https://marbat87.altervista.org/privacy_policy.html",
                        onDismissRequest = { webViewDialogManagerViewModel.showWebViewDialog.value = false },
                        buttonTextRes = R.string.mal_close
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
        finish()
        slideOutRight()
    }
}
