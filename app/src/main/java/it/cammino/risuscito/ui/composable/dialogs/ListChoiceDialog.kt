package it.cammino.risuscito.ui.composable.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.composable.DialogTitle
import it.cammino.risuscito.ui.composable.RadioListItem
import it.cammino.risuscito.utils.extension.capitalize

@Composable
fun ListChoiceAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (Int) -> Unit,
    nomiPassaggi: Array<String>,
    indiciPassaggi: IntArray,
    passaggioSelezionato: Int
) {

    var localSelezionato by remember { mutableIntStateOf(passaggioSelezionato) }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
        ) {
            val simpleListState = rememberLazyListState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(vertical = 24.dp)
            ) {
                DialogTitle(title = stringResource(R.string.passage_title))
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                LazyColumn(
                    state = simpleListState,
                    modifier = Modifier
                        .height(300.dp)
                        .padding(horizontal = 24.dp)
                ) {
                    itemsIndexed(nomiPassaggi.toList()) { index, item ->
                        RadioListItem(
                            item,
                            {
                                localSelezionato = it
                            },
                            indiciPassaggi[index],
                            localSelezionato
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
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
                            onConfirmation(localSelezionato)
                        }
                    ) {
                        Text(stringResource(R.string.action_salva).capitalize(LocalContext.current))
                    }
                }
            }
        }
    }
}