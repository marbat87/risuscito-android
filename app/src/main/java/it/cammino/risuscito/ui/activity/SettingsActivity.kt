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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.compose.AndroidFragment
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.composable.dialogs.ProgressDialog
import it.cammino.risuscito.ui.composable.main.StatusBarProtection
import it.cammino.risuscito.ui.composable.theme.RisuscitoTheme
import it.cammino.risuscito.ui.fragment.SettingsFragment
import it.cammino.risuscito.utils.extension.slideOutRight

class SettingsActivity : ThemeableActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
//        setEnterTransition()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            RisuscitoTheme {
                val scrollBehavior =
                    TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

                val snackbarHostState = remember { SnackbarHostState() }

                val showProgressDialog by progressDialogViewModel.showProgressDialog.observeAsState()

                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(stringResource(R.string.title_activity_settings))
                            },
                            navigationIcon = {
                                IconButton(onClick = { onBackPressedAction() }) {
                                    Icon(
                                        painter = painterResource(R.drawable.arrow_back_24px),
                                        contentDescription = stringResource(R.string.material_drawer_close)
                                    )
                                }
                            },
                            scrollBehavior = scrollBehavior,
                            colors = TopAppBarDefaults.topAppBarColors().copy(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        )
                    },
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    },
                ) { innerPadding ->
                    AndroidFragment<SettingsFragment>(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    )
                }

                LaunchedEffect(sharedSnackBarViewModel.showSnackBar.value) {
                    if (sharedSnackBarViewModel.showSnackBar.value) {
                        val result = snackbarHostState
                            .showSnackbar(
                                message = sharedSnackBarViewModel.snackbarMessage.value,
                                actionLabel = sharedSnackBarViewModel.actionLabel.value.ifBlank { null },
                                duration = SnackbarDuration.Short,
                                withDismissAction = true
                            )
                        when (result) {
                            SnackbarResult.ActionPerformed -> {
                                sharedSnackBarViewModel.showSnackBar.value = false
                            }

                            SnackbarResult.Dismissed -> {
                                sharedSnackBarViewModel.showSnackBar.value = false
                            }
                        }
                    }
                }

                if (showProgressDialog == true) {
                    ProgressDialog(
                        dialogTitleRes = progressDialogViewModel.dialogTitleRes,
                        messageRes = progressDialogViewModel.messageRes.value ?: 0,
                        onDismissRequest = {
                            progressDialogViewModel.showProgressDialog.value = false
                        },
                        buttonTextRes = progressDialogViewModel.buttonTextRes,
                        indeterminate = progressDialogViewModel.indeterminate
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
