package it.cammino.risuscito.ui.composable.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import it.cammino.risuscito.utils.extension.capitalize

@Composable
fun SimpleAlertDialog(
    onDismissRequest: (SimpleDialogTag) -> Unit,
    onConfirmation: (SimpleDialogTag) -> Unit,
    dialogTitle: String,
    dialogText: String,
    iconRes: Int,
    confirmButtonText: CharSequence,
    dismissButtonText: CharSequence,
    dialogTag: SimpleDialogTag = SimpleDialogTag.DEFAULT
) {
    AlertDialog(
        icon = {
            Icon(painter = painterResource(iconRes), contentDescription = "Dialog Icon")
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
    DELETE_LIST,
    LITURGICO_REPLACE,
    LITURGICO_REPLACE_2,
    SAVE_CONSEGNATI_DIALOG,
    ADD_PASSAGE_DIALOG,
    ALPHA_REPLACE,
    ALPHA_REPLACE_2,
    NUMERIC_REPLACE,
    NUMERIC_REPLACE_2,
    SALMI_REPLACE,
    SALMI_REPLACE_2,
    BACKUP_ASK,
    RESTORE_ASK,
    SIGNOUT,
    REVOKE,
    RESTORE_DONE,
    BACKUP_DONE,
    NOTIFICATION_DIALOG,
    SAVE_TAB,
    DELETE_MP3,
    DELETE_LINK,
    DOWNLINK_CHOOSE,
    ONLY_LINK

}