package it.cammino.risuscito.ui.composable.dialogs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import it.cammino.risuscito.ui.composable.DialogTitle
import it.cammino.risuscito.utils.extension.capitalize

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProgressDialog(
    dialogTag: ProgressDialogTag = ProgressDialogTag.DEFAULT,
    dialogTitleRes: Int = 0,
    iconRes: Int = 0,
    messageRes: Int = 0,
    onDismissRequest: (ProgressDialogTag) -> Unit,
    buttonTextRes: Int = 0,
    indeterminate: Boolean = true,
    progress: Float = 0.1f,
) {

    Dialog(onDismissRequest = { onDismissRequest(dialogTag) }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(vertical = 24.dp)
            ) {
                if (iconRes > 0) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = "progress dialog",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                if (dialogTitleRes > 0) DialogTitle(title = stringResource(dialogTitleRes))
                if (iconRes > 0 || dialogTitleRes > 0) Spacer(modifier = Modifier.height(16.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    if (indeterminate) {
                        LoadingIndicator()
                    } else {
                        val animatedProgress by
                        animateFloatAsState(
                            targetValue = progress,
                            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                        )
                        LinearWavyProgressIndicator(progress = { animatedProgress })
                    }
                    if (messageRes > 0) {
                        Text(
                            text = stringResource(messageRes),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                if (buttonTextRes > 0) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .align(Alignment.End)
                    ) {
                        TextButton(
                            onClick = {
                                onDismissRequest(dialogTag)
                            }
                        ) {
                            Text(stringResource(buttonTextRes).capitalize(LocalContext.current))
                        }
                    }
                }
            }
        }
    }
}

enum class ProgressDialogTag {
    DEFAULT
}