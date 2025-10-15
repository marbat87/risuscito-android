package it.cammino.risuscito.ui.composable.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.composable.DialogTitle
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.extension.capitalize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputDialog(
    dialogTitleRes: Int,
    onDismissRequest: () -> Unit,
    onConfirmation: (String) -> Unit,
    confirmationTextRes: Int,
    prefill: String = StringUtils.EMPTY,
    multiline: Boolean = false
) {

    val inputState = rememberTextFieldState(prefill)

    val keyboardOptions = KeyboardOptions(
        imeAction = ImeAction.Done,
        capitalization = KeyboardCapitalization.Sentences
    )

    BasicAlertDialog(onDismissRequest = { onDismissRequest() }) {
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
                DialogTitle(title = stringResource(dialogTitleRes))
                Spacer(modifier = Modifier.height(16.dp))
                if (multiline) {
                    OutlinedTextField(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        state = inputState,
                        keyboardOptions = keyboardOptions,
                        trailingIcon = { ClearText(inputState) },
                    )
                } else {
                    OutlinedTextField(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        state = inputState, lineLimits = TextFieldLineLimits.SingleLine,
                        keyboardOptions = keyboardOptions,
                        trailingIcon = { ClearText(inputState) },
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .align(Alignment.End)
                ) {
                    TextButton(
                        onClick = {
                            onDismissRequest()
                        }
                    ) {
                        Text(stringResource(R.string.cancel).capitalize(LocalContext.current))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            onConfirmation(inputState.text.toString())
                        }
                    ) {
                        Text(stringResource(confirmationTextRes).capitalize(LocalContext.current))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClearText(inputState: TextFieldState) {
    if (inputState.text.isNotEmpty()) {
        TooltipBox(
            positionProvider =
                TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.Above
                ),
            tooltip = { PlainTooltip { Text(stringResource(R.string.clear_confirm)) } },
            state = rememberTooltipState(),
        ) {
            IconButton(onClick = { inputState.clearText() }) {
                Icon(
                    painter = painterResource(R.drawable.cancel_24px),
                    contentDescription = stringResource(R.string.clear_confirm)
                )
            }
        }
    }
}