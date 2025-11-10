package it.cammino.risuscito.ui.composable.main

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import it.cammino.risuscito.ui.interfaces.SnackBarFragment

@Composable
fun RisuscitoSnackBar(
    showSnackBar: MutableState<Boolean> = mutableStateOf(false),
    snackbarHostState: SnackbarHostState,
    callBack: SnackBarFragment?,
    message: String,
    actionLabel: String?
) {

    val updatedMessage by rememberUpdatedState(message)
    val updatedActionLabel by rememberUpdatedState(actionLabel)

    LaunchedEffect(showSnackBar.value) {
        if (showSnackBar.value) {
            val result = snackbarHostState
                .showSnackbar(
                    message = updatedMessage,
                    actionLabel = if (updatedActionLabel?.isBlank() == true) null else updatedActionLabel,
                    duration = SnackbarDuration.Short,
                    withDismissAction = true
                )
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    callBack?.onActionPerformed()
                    showSnackBar.value = false
                }

                SnackbarResult.Dismissed -> {
                    callBack?.onDismissed()
                    showSnackBar.value = false
                }
            }
        }
    }
}