package it.cammino.risuscito.ui.composable.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import it.cammino.risuscito.utils.extension.capitalize

@Composable
fun SimpleAlertDialog(
    onDismissRequest: (String?) -> Unit,
    onConfirmation: (String?) -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
    confirmButtonText: CharSequence,
    dismissButtonText: CharSequence,
    dialogTag: String = ""
) {
    AlertDialog(
        icon = {
            Icon(imageVector = icon, contentDescription = "Dialog Icon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest(dialogTag)
        },
        confirmButton = {
            if (confirmButtonText.isNotEmpty()) {
                TextButton(
                    onClick = {
                        onConfirmation(dialogTag)
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
                        onDismissRequest(dialogTag)
                    }
                ) {
                    Text(dismissButtonText.capitalize(LocalContext.current))
                }
            }
        }
    )
}

enum class SimpleDialogTag {
    DEFAULT,
    RESET_LIST,
    DELETE_LIST
}