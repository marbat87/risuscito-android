package it.cammino.risuscito.ui.composable

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import it.cammino.risuscito.R
import it.cammino.risuscito.items.RisuscitoListItem
import it.cammino.risuscito.ui.composable.theme.RisuscitoTheme
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.spannedFromHtml
import it.cammino.risuscito.utils.extension.systemLocale
import java.sql.Date
import java.text.DateFormat
import java.text.SimpleDateFormat

@Composable
fun BottomSheetItem(infoItem: ResolveInfo, pm: PackageManager, onItemClick: (ResolveInfo) -> Unit) {
    RisuscitoTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable { onItemClick(infoItem) }
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 16.dp)
        ) {
            Image(
                painter = rememberDrawablePainter(drawable = infoItem.loadIcon(pm)),
                contentDescription = "",
                modifier = Modifier
                    .width(48.dp)
                    .height(48.dp)
                    .padding(8.dp),
            )
            GridItemTitle(infoItem.loadLabel(pm).toString())
        }
    }
}

@Composable
fun SimpleListItem(
    ctx: Context,
    simpleItem: RisuscitoListItem,
    onItemClick: (RisuscitoListItem) -> Unit,
    onItemLongClick: (RisuscitoListItem) -> Unit,
    selected: Boolean,
    modifier: Modifier
) {

    val title = simpleItem.filter?.let {
        if (it.isNotEmpty()) {
            val normalizedTitle = Utility.removeAccents(
                stringResource(simpleItem.titleRes)
            )
            val mPosition =
                normalizedTitle.lowercase(ctx.systemLocale).indexOf(it)
            if (mPosition >= 0) {
                val stringTitle = stringResource(simpleItem.titleRes)
                val highlighted = StringBuilder(
                    if (mPosition > 0) (stringTitle.substring(0, mPosition)) else StringUtils.EMPTY
                )
                    .append("<b>")
                    .append(stringTitle.substring(mPosition, mPosition + it.length))
                    .append("</b>")
                    .append(stringTitle.substring(mPosition + it.length))
                highlighted.toString().spannedFromHtml.toString()
            } else
                stringResource(simpleItem.titleRes)
        } else
            stringResource(simpleItem.titleRes)
    } ?: stringResource(simpleItem.titleRes)

    ListItem(
        leadingContent = {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.check_24px),
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            } else {
                PageText(stringResource(simpleItem.pageRes), simpleItem.rawColor)
            }
        },
        headlineContent = { Text(title) },
        modifier = modifier
            .combinedClickable(
                enabled = true,
                onClick = { onItemClick(simpleItem) },
                onLongClick = {
                    onItemLongClick(simpleItem)
                }
            ),
        colors = ListItemDefaults.colors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.secondaryContainer // Colore per selezione
            } else {
                Color.Transparent
            }
        )
    )
}

@Composable
fun HistoryListItem(
    ctx: Context,
    simpleItem: RisuscitoListItem,
    onItemClick: (RisuscitoListItem) -> Unit,
    onItemLongClick: (RisuscitoListItem) -> Unit,
    selected: Boolean,
    modifier: Modifier
) {

    val textTimestamp =
        if (simpleItem.timestamp.isNotEmpty()) {
            // FORMATTO LA DATA IN BASE ALLA LOCALIZZAZIONE
            val df = DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.MEDIUM, ctx.systemLocale
            )
            val tempTimestamp: String

            val dateTimestamp =
                Date(java.lang.Long.parseLong(simpleItem.timestamp))
            tempTimestamp = if (df is SimpleDateFormat) {
                val pattern = df.toPattern().replace("y+".toRegex(), "yyyy")
                df.applyPattern(pattern)
                df.format(dateTimestamp)
            } else
                df.format(dateTimestamp)
            tempTimestamp
        } else
            ""

    ListItem(
        leadingContent = {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.check_24px),
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            } else {
                PageText(stringResource(simpleItem.pageRes), simpleItem.rawColor)
            }
        },
        headlineContent = { Text(stringResource(simpleItem.titleRes)) },
        supportingContent = { Text(textTimestamp) },
        modifier = modifier
            .combinedClickable(
                enabled = true,
                onClick = { onItemClick(simpleItem) },
                onLongClick = {
                    onItemLongClick(simpleItem)
                }
            ),
        colors = ListItemDefaults.colors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.secondaryContainer // Colore per selezione
            } else {
                ListItemDefaults.containerColor // Colore di default (o Color.Transparent)
            }
        )
    )
}