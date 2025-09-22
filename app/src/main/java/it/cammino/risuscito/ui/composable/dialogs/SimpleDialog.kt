package it.cammino.risuscito.ui.composable.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import it.cammino.risuscito.utils.extension.capitalize

@Composable
fun SimpleAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: Painter,
    confirmButtonText: CharSequence,
    dismissButtonText: CharSequence
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Dialog Icon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            if (confirmButtonText.isNotEmpty()) {
                TextButton(
                    onClick = {
                        onConfirmation()
                    }
                ) {
                    Text(confirmButtonText.capitalize(LocalContext.current))
                }
            }
        },
        dismissButton = {
            if (dismissButtonText.isNotEmpty()) {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text(dismissButtonText.capitalize(LocalContext.current))
                }
            }
        }
    )
}