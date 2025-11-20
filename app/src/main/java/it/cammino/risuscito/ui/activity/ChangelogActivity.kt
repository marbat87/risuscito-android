package it.cammino.risuscito.ui.activity

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.michaelflisar.composechangelog.Changelog
import com.michaelflisar.composechangelog.ChangelogDefaults
import com.michaelflisar.composechangelog.DefaultVersionFormatter
import com.michaelflisar.composechangelog.classes.rememberChangelogState
import com.michaelflisar.composechangelog.setup
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.composable.ClassicBackNavitagionButton
import it.cammino.risuscito.ui.composable.layoutMargins
import it.cammino.risuscito.ui.composable.main.StatusBarProtection
import it.cammino.risuscito.ui.composable.theme.RisuscitoTheme
import it.cammino.risuscito.utils.extension.slideOutRight

class ChangelogActivity : AppCompatActivity() {


    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val state = rememberChangelogState()

            val context = LocalContext.current

            val setup = ChangelogDefaults.setup(
                context = context,
                versionFormatter = CHANGELOG_FORMATTER
            )

            RisuscitoTheme {
                val scrollBehavior =
                    TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(id = R.string.changelog)) },
                            navigationIcon = {
                                ClassicBackNavitagionButton(
                                    onBackPressedAction = { onBackPressedAction() })
                            },
                            scrollBehavior = scrollBehavior,
                            colors = TopAppBarDefaults.topAppBarColors().copy(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        )
                    }
                ) { innerPadding ->
                    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
                        Column(modifier = Modifier.padding(innerPadding)) {
                            Text(
                                text = stringResource(R.string.about_subtitle),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            )
                            Changelog(
                                state = state,
                                setup = setup,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = layoutMargins()),
                                loading = { LoadingIndicator() }
                            )
                        }
                    }
                }
            }

            StatusBarProtection()

            LaunchedEffect(Unit) {
                state.show()
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            onBackPressedAction()
        }
    }

    private fun onBackPressedAction() {
        finish()
        slideOutRight()
    }

    companion object {
        val CHANGELOG_FORMATTER =
            DefaultVersionFormatter(DefaultVersionFormatter.Format.MajorMinorPatchCandidate)
    }

}