package it.cammino.risuscito.ui.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.composable.theme.RisuscitoTheme

@Composable
fun Hint(
    hintText: String,
    onDismiss: () -> Unit = {}
) {
    val swipeToDismissBoxState =
        rememberSwipeToDismissBoxState(
            SwipeToDismissBoxValue.Settled,
            SwipeToDismissBoxDefaults.positionalThreshold
        )

    SwipeToDismissBox(
        state = swipeToDismissBoxState,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        backgroundContent = {
            SwipeToDismissBackground(swipeToDismissBoxState)
        },
        onDismiss = {
            onDismiss()
        }
    ) {
        ListItem(
            modifier = Modifier.height(88.dp),
            supportingContent = {
                Text(
                    text = hintText,
                    style = MaterialTheme.typography.bodySmall
                )
            },
            headlineContent = {},
            leadingContent = {
                Box(
                    contentAlignment = Alignment.TopCenter
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.info_24px),
                        contentDescription = null, // Come da android:contentDescription="@null"
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary), // Simula app:tint="?colorControlActivated"
                        modifier = Modifier
                            .zIndex(1f) // Per stare sopra la linea
                            .size(34.dp)
                            .clip(CircleShape) // Per lo sfondo ovale/circolare
                            .background(MaterialTheme.colorScheme.surface) // Simula @drawable/oval_bg_hint
                    )
                    VerticalDivider()
                }
            }
        )
    }
}

@Composable
fun SwipeToDismissBackground(
    swipeToDismissBoxState: SwipeToDismissBoxState
) {
    when (swipeToDismissBoxState.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd,
        SwipeToDismissBoxValue.EndToStart -> {
            Icon(
                painter = painterResource(R.drawable.delete_sweep_24px),
                contentDescription = "Remove item",
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.error)
                    .wrapContentSize(if (swipeToDismissBoxState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd)
                    .padding(12.dp),
                tint = MaterialTheme.colorScheme.onError
            )
        }

        SwipeToDismissBoxValue.Settled -> {}
    }
}

@Preview
@Composable
fun MainHintLayoutPreview() {
    RisuscitoTheme {
        Hint(
            hintText = stringResource(id = R.string.showcase_rename_desc) + System.lineSeparator() + stringResource(
                R.string.showcase_delete_desc
            )
        )
    }
}

@Composable
fun EmptyListView(iconRes: Int, textRes: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(), // Occupa solo l'altezza necessaria
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = stringResource(id = textRes),
            modifier = Modifier
                .size(120.dp)
        )
        Spacer(modifier = Modifier.height(16.dp)) // Spazio tra immagine e testo
        Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, // Colore secondario del testo
            fontFamily = risuscito_medium_font,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth() // Per centrare il testo se è multiriga
        )
    }
}

@Preview
@Composable
fun EmptyListViewPreview() {
    EmptyListView(
        iconRes = R.drawable.ic_sunglassed_star,
        textRes = R.string.no_favourites_short
    )
}