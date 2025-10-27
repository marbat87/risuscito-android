package it.cammino.risuscito.ui.composable

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import it.cammino.risuscito.R
import it.cammino.risuscito.items.ExpandableItemType
import it.cammino.risuscito.items.ListaPersonalizzataRisuscitoListItem
import it.cammino.risuscito.items.RisuscitoListItem
import it.cammino.risuscito.items.SwipeableRisuscitoListItem
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.spannedFromHtml
import it.cammino.risuscito.utils.extension.systemLocale
import java.lang.Long
import java.sql.Date
import java.text.DateFormat
import java.text.SimpleDateFormat
import kotlin.Boolean
import kotlin.Int
import kotlin.OptIn
import kotlin.String
import kotlin.Unit
import kotlin.let
import kotlin.takeIf

@Composable
fun BottomSheetItem(infoItem: ResolveInfo, pm: PackageManager, onItemClick: (ResolveInfo) -> Unit) {
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

@Composable
fun SimpleListItem(
    ctx: Context,
    simpleItem: RisuscitoListItem,
    onItemClick: (RisuscitoListItem) -> Unit,
    onItemLongClick: (RisuscitoListItem) -> Unit,
    selected: Boolean,
    modifier: Modifier,
    isInsert: Boolean = false,
    onIconClick: (RisuscitoListItem) -> Unit = {},
) {

    val title = remember(simpleItem.titleRes, simpleItem.filter) {
        val baseTitle =
            ctx.getString(simpleItem.titleRes) // Usa ctx.getString se stringResource è problematico in remember
        simpleItem.filter?.takeIf { it.isNotEmpty() }?.let { filterValue ->
            val normalizedTitle = Utility.removeAccents(baseTitle)
            val mPosition = normalizedTitle.lowercase(ctx.systemLocale)
                .indexOf(filterValue.lowercase(ctx.systemLocale)) // Normalizza anche il filtro
            if (mPosition >= 0) {
                val highlighted = StringBuilder(
                    if (mPosition > 0) (baseTitle.substring(0, mPosition)) else ""
                )
                    .append("<i>")
                    .append(baseTitle.substring(mPosition, mPosition + filterValue.length))
                    .append("</i>")
                    .append(baseTitle.substring(mPosition + filterValue.length))
                highlighted.toString().spannedFromHtml.toString()
            } else {
                baseTitle
            }
        } ?: baseTitle
    }

    val animatedColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.secondaryContainer else ListItemDefaults.containerColor,
        label = "background color"
    )

    ListItem(
        leadingContent = {
            AnimatedRisuscitoListItemPage(
                selected
            ) { state ->
                when (state) {
                    true -> {
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
                    }

                    else -> PageText(stringResource(simpleItem.pageRes), simpleItem.rawColor)
                }
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
            containerColor = animatedColor
        ),
        trailingContent = {
            if (isInsert) {
                FilledTonalIconButton(onClick = { onIconClick(simpleItem) }) {
                    Icon(
                        painter = painterResource(R.drawable.visibility_24px),
                        contentDescription = "Notation"
                    )
                }
            }
        }
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
                Date(Long.parseLong(simpleItem.timestamp))
            tempTimestamp = if (df is SimpleDateFormat) {
                val pattern = df.toPattern().replace("y+".toRegex(), "yyyy")
                df.applyPattern(pattern)
                df.format(dateTimestamp)
            } else
                df.format(dateTimestamp)
            tempTimestamp
        } else
            ""

    val animatedColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.secondaryContainer else ListItemDefaults.containerColor,
        label = "background color"
    )

    ListItem(
        leadingContent = {
            AnimatedRisuscitoListItemPage(
                selected
            ) { state ->
                when (state) {
                    true -> {
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
                    }

                    else -> PageText(stringResource(simpleItem.pageRes), simpleItem.rawColor)
                }
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
            containerColor = animatedColor
        )
    )
}

@Composable
fun ExpandableListItem(
    ctx: Context,
    item: RisuscitoListItem,
    onHeaderClicked: (RisuscitoListItem) -> Unit,
    onItemClick: (RisuscitoListItem) -> Unit,
    onItemLongClick: (RisuscitoListItem) -> Unit,
    modifier: Modifier,
    isExpanded: Boolean,
) {
    when (item.itemType) {
        ExpandableItemType.EXPANDABLE -> ListExpandableTitle(
            item,
            isExpanded,
            onHeaderClicked,
            modifier
        )

        ExpandableItemType.SUBITEM ->
            AnimatedVisibility(
                visible = isExpanded, // Sempre visibile quando il gruppo è espanso
                enter = fadeIn(
                    animationSpec = tween(
                        150,
                        delayMillis = 100
                    )
                ) + expandVertically(
                    animationSpec = tween(300)
                ),
                exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(
                    animationSpec = tween(
                        300
                    )
                ),
                label = "SubItemAnimation-${item.id}"
            ) {
                SimpleListItem(
                    ctx,
                    item,
                    onItemClick,
                    onItemLongClick,
                    false,
                    modifier
                )
            }
    }

}

@Composable
fun ListTitleItem(titleRes: Int) {
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    )
}

@Composable
fun ListExpandableTitle(
    item: RisuscitoListItem,
    isExpanded: Boolean,
    onHeaderClicked: (RisuscitoListItem) -> Unit,
    modifier: Modifier
) {
    // Animatable per la rotazione. Inizializza a 0f (non ruotato) o 180f se inizia espanso.
    // Lo stato iniziale dell'icona (freccia in giù) corrisponde a 0 gradi di rotazione.
    // Quando è espanso, la freccia dovrebbe puntare in su, che otteniamo ruotando la freccia in giù di 180 gradi.
    val rotationAngle = remember { Animatable(if (isExpanded) 180f else 0f) }

    // Questo LaunchedEffect reagisce ai cambiamenti di isExpanded
    // e anima la rotazione all'angolo appropriato.
    LaunchedEffect(isExpanded) {
        rotationAngle.animateTo(
            targetValue = if (isExpanded) 180f else 0f,
            animationSpec = tween(durationMillis = 300) // Puoi personalizzare la durata e il tipo di animazione
        )
    }

    ListItem(
        headlineContent = {
            Text(
                text = stringResource(item.titleRes) + " (${item.subCantiCounter})",
                color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        },
        modifier = modifier
            .combinedClickable(
                enabled = true,
                onClick = { onHeaderClicked(item) }
            ),
        trailingContent = {
            Icon(
                painter = painterResource(R.drawable.keyboard_arrow_down_24px),
                contentDescription = "Expand",
                tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = modifier.rotate(rotationAngle.value)
            )
        }
    )

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PassageListItem(
    simpleItem: RisuscitoListItem,
    onItemClick: (RisuscitoListItem) -> Unit,
    onIconClick: (RisuscitoListItem) -> Unit,
    modifier: Modifier
) {
    ListItem(
        leadingContent = { PageText(stringResource(simpleItem.pageRes), simpleItem.rawColor) },
        headlineContent = { Text(stringResource(simpleItem.titleRes)) },
        modifier = modifier.clickable(
            enabled = true,
            onClick = { onItemClick(simpleItem) }
        ),
        trailingContent = {
            FilledTonalIconToggleButton(
                checked = simpleItem.numPassaggio != -1,
                onCheckedChange = { onIconClick(simpleItem) },
                shapes = IconButtonDefaults.toggleableShapes()
            ) {
                if (simpleItem.numPassaggio != -1) {
                    Icon(
                        painter = painterResource(R.drawable.sell_filled_24px),
                        contentDescription = "Notation"
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.sell_24px),
                        contentDescription = "Notation"
                    )
                }
            }
        }
    )
}

@Composable
fun CheckableListItem(
    simpleItem: RisuscitoListItem,
    modifier: Modifier,
    onSelect: (Boolean) -> Unit,
    selected: Boolean = false
) {

    val animatedColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.secondaryContainer else ListItemDefaults.containerColor,
        label = "background color"
    )

    ListItem(
        leadingContent = { PageText(stringResource(simpleItem.pageRes), simpleItem.rawColor) },
        headlineContent = { Text(stringResource(simpleItem.titleRes)) },
        modifier = modifier.clickable(
            enabled = true,
            onClick = {
                onSelect(!selected)
            }
        ),
        trailingContent = {
            Checkbox(
                checked = selected,
                onCheckedChange = {
                    onSelect(it)
                }
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = animatedColor
        )
    )
}

@Composable
fun RadioListItem(
    titleItem: String,
    onSelect: (Int) -> Unit,
    itemId: Int,
    selectedOption: Int,
) {

    ListItem(
        headlineContent = { Text(titleItem) },
        modifier = Modifier
            .selectable(
                selected = itemId == selectedOption,
                onClick = { onSelect(itemId) },
                role = Role.RadioButton
            ),
        trailingContent = {
            RadioButton(
                selected = itemId == selectedOption,
                onClick = null // null recommended for accessibility with screen readers
            )
        },
        colors = ListItemDefaults.colors()
            .copy(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NotableListItem(
    item: ListaPersonalizzataRisuscitoListItem,
    onItemClick: (ListaPersonalizzataRisuscitoListItem) -> Unit,
    onItemLongClick: (ListaPersonalizzataRisuscitoListItem) -> Unit,
    onNoteClick: (ListaPersonalizzataRisuscitoListItem) -> Unit,
    selected: Boolean
) {
    ListItem(
        leadingContent = {
            AnimatedRisuscitoListItemPage(
                selected
            ) { state ->
                when (state) {
                    true -> {
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
                    }

                    else -> PageText(stringResource(item.pageRes), item.rawColor)
                }
            }
        },
        headlineContent = { Text(stringResource(item.titleRes)) },
        modifier = Modifier
            .combinedClickable(
                enabled = true,
                onClick = { onItemClick(item) },
                onLongClick = {
                    onItemLongClick(item)
                }
            ),
        colors = ListItemDefaults.colors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.secondaryContainer // Colore per selezione
            } else {
                Color.Transparent
            }
        ),
        trailingContent = {
            if (!selected) {
                FilledTonalIconToggleButton(
                    checked = item.nota.isNotEmpty(),
                    onCheckedChange = { onNoteClick(item) },
                    shapes = IconButtonDefaults.toggleableShapes(),
                ) {
                    if (item.nota.isNotEmpty()) {
                        Icon(
                            painter = painterResource(R.drawable.sticky_note_2_filled_24px),
                            contentDescription = "Notation"
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.sticky_note_2_24px),
                            contentDescription = "Notation"
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun PosizioneListItem(
    titoloPosizione: String,
    idPosizione: Int,
    isMultiple: Boolean,
    posizioni: List<ListaPersonalizzataRisuscitoListItem>,
    addClickListener: (Int) -> Unit,
    cantoClickListener: (ListaPersonalizzataRisuscitoListItem) -> Unit,
    cantoLongClickListener: (ListaPersonalizzataRisuscitoListItem) -> Unit,
    noteClickListener: (ListaPersonalizzataRisuscitoListItem) -> Unit,
) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column {
            Text(
                text = titoloPosizione,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp)
            )
            posizioni.forEach {
                NotableListItem(
                    item = it,
                    onItemClick = cantoClickListener,
                    onItemLongClick = cantoLongClickListener,
                    onNoteClick = noteClickListener,
                    selected = it.selected
                )
            }
            if (posizioni.isEmpty() || isMultiple) {
                TextButton(
                    onClick = { addClickListener(idPosizione) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.add_circle_24px),
                        contentDescription = "Add canto",
                    )
                    Text(stringResource(R.string.select_canto))
                }
            }
        }
    }
}

@Composable
fun DraggableDismissableListItem(
    modifier: Modifier,
    dragModifier: Modifier,
    interactionSource: MutableInteractionSource,
    swipeToDismissBoxState: SwipeToDismissBoxState,
    index: Int,
    item: SwipeableRisuscitoListItem,
    onItemLongClick: (Int, SwipeableRisuscitoListItem) -> Unit
) {
    SwipeToDismissBox(
        state = swipeToDismissBoxState,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        backgroundContent = {
            SwipeToDismissBackground(swipeToDismissBoxState)
        }
    ) {

        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp,
                draggedElevation = 16.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            onClick = {},
            interactionSource = interactionSource
        ) {
            ListItem(
                headlineContent = { Text(item.title) },
                modifier = Modifier
                    .combinedClickable(
                        enabled = true,
                        onClick = { },
                        onLongClick = {
                            onItemLongClick(
                                index,
                                item
                            )
                        }
                    ),
                trailingContent = {
                    IconButton(
                        modifier = dragModifier,
                        onClick = {},
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.drag_handle_24px),
                            contentDescription = "Reorder"
                        )
                    }
                }
            )
        }
    }
}

const val risuscitoListItemPageAnimation = 300

@Composable
fun <S> AnimatedRisuscitoListItemPage(
    targetState: S,
    content: @Composable() AnimatedContentScope.(targetState: S) -> Unit
) {
    AnimatedContent(
        targetState,
        transitionSpec = {
            scaleIn(
                animationSpec = tween(risuscitoListItemPageAnimation)
            ) togetherWith scaleOut(animationSpec = tween(risuscitoListItemPageAnimation))
        },
        label = "Animated Content"
    ) {
        content(it)
    }
}