package it.cammino.risuscito.ui.composable

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import it.cammino.risuscito.R
import it.cammino.risuscito.items.ExpandableItemType
import it.cammino.risuscito.items.ListaPersonalizzataRisuscitoListItem
import it.cammino.risuscito.items.RisuscitoListItem
import it.cammino.risuscito.items.SwipeableRisuscitoListItem
import it.cammino.risuscito.ui.composable.animations.AnimatedScaleContent
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.systemLocale
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
    val title = remember { infoItem.loadLabel(pm).toString() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onItemClick(infoItem) }
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Image(
            painter = rememberDrawablePainter(drawable = infoItem.loadIcon(pm)),
            contentDescription = title,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.padding(8.dp))
        GridItemTitle(title)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SimpleListItem(
    ctx: Context,
    item: RisuscitoListItem,
    onItemClick: (RisuscitoListItem) -> Unit,
    onItemLongClick: (RisuscitoListItem) -> Unit,
    selected: Boolean = false,
    modifier: Modifier,
    isInsert: Boolean = false,
    onIconClick: (RisuscitoListItem) -> Unit = {},
    index: Int = 0,
    itemsCount: Int = 0,
    colors: ListItemColors? = null
) {

    val title = remember(item.titleRes, item.filter) {
        val baseTitle =
            ctx.getString(item.titleRes)
        item.filter?.takeIf { it.isNotEmpty() }?.let { filterValue ->
            val normalizedTitle = Utility.removeAccents(baseTitle)
            val mPosition = normalizedTitle.lowercase(ctx.systemLocale)
                .indexOf(filterValue.lowercase(ctx.systemLocale)) // Normalizza anche il filtro
            if (mPosition >= 0) {
                val highlighted = StringBuilder(
                    if (mPosition > 0) (baseTitle.take(mPosition)) else ""
                )
                    .append("<b>")
                    .append(baseTitle.substring(mPosition, mPosition + filterValue.length))
                    .append("</b>")
                    .append(baseTitle.substring(mPosition + filterValue.length))
                AnnotatedString.fromHtml(highlighted.toString())
            } else {
                AnnotatedString.fromHtml(baseTitle)
            }
        } ?: AnnotatedString.fromHtml(baseTitle)
    }

    SegmentedListItem(
        shapes = ListItemDefaults.segmentedShapes(index = index, count = itemsCount),
        onClick = { onItemClick(item) },
        leadingContent = {
            AnimatedScaleContent(
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
        modifier = modifier,
        selected = selected,
        trailingContent = {
            if (isInsert) {
                FilledTonalIconButton(
                    modifier = Modifier
                        .height(40.dp)
                        .width(35.dp),
                    onClick = { onIconClick(item) }) {
                    Icon(
                        modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize),
                        painter = painterResource(R.drawable.visibility_24px),
                        contentDescription = "Notation"
                    )
                }
            }
        },
        onLongClick = { onItemLongClick(item) },
        colors = colors ?: ListItemDefaults.segmentedColors()
    ) {
        Text(title)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HistoryListItem(
    ctx: Context,
    simpleItem: RisuscitoListItem,
    onItemClick: (RisuscitoListItem) -> Unit,
    onItemLongClick: (RisuscitoListItem) -> Unit,
    selected: Boolean,
    modifier: Modifier,
    index: Int = 0,
    itemsCount: Int = 0
) {

    val textTimestamp = remember(simpleItem.timestamp) {
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
    }

    SegmentedListItem(
        shapes = ListItemDefaults.segmentedShapes(index = index, count = itemsCount),
        modifier = modifier,
        selected = selected,
        leadingContent = {
            AnimatedScaleContent(
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
        supportingContent = { Text(textTimestamp) },
        onClick = { onItemClick(simpleItem) },
        onLongClick = { onItemLongClick(simpleItem) }
    ) {
        Text(stringResource(simpleItem.titleRes))
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpandableListItem(
    ctx: Context,
    item: RisuscitoListItem,
    onHeaderClicked: (RisuscitoListItem) -> Unit,
    onItemClick: (RisuscitoListItem) -> Unit,
    onItemLongClick: (RisuscitoListItem) -> Unit,
    modifier: Modifier,
    isExpanded: Boolean
) {

    val itemCount = 1 + if (isExpanded) item.subCantiCounter else 0

    val colors = if (isExpanded)
        ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    else ListItemDefaults.segmentedColors()

    when (item.itemType) {
        ExpandableItemType.EXPANDABLE -> ListExpandableTitle(
            item = item,
            isExpanded = isExpanded,
            onHeaderClicked = onHeaderClicked,
            modifier = modifier,
            itemCount = itemCount,
            colors = colors
        )

        ExpandableItemType.SUBITEM ->
            AnimatedVisibility(
                visible = isExpanded, // Sempre visibile quando il gruppo è espanso
                enter = expandVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
                exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec())
            ) {
                Box(modifier = Modifier.padding(top = ListItemDefaults.SegmentedGap)) {
                    SimpleListItem(
                        ctx = ctx,
                        item = item,
                        onItemClick = onItemClick,
                        onItemLongClick = onItemLongClick,
                        modifier = modifier,
                        index = item.subIndex + 1,
                        itemsCount = itemCount,
                        colors = colors
                    )
                }
            }
    }

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ListTitleItem(titleRes: Int) {
    ListItem(
        selected = false,
        onClick = {}
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ListExpandableTitle(
    item: RisuscitoListItem,
    isExpanded: Boolean,
    onHeaderClicked: (RisuscitoListItem) -> Unit,
    modifier: Modifier,
    itemCount: Int = 0,
    colors: ListItemColors
) {
    // Animatable per la rotazione. Inizializza a 0f (non ruotato) o 180f se inizia espanso.
    // Lo stato iniziale dell'icona (freccia in giù) corrisponde a 0 gradi di rotazione.
    // Quando è espanso, la freccia dovrebbe puntare in su, che otteniamo ruotando la freccia in giù di 180 gradi.
//    val rotationAngle = remember { Animatable(if (isExpanded) 180f else 0f) }

    // Questo LaunchedEffect reagisce ai cambiamenti di isExpanded
    // e anima la rotazione all'angolo appropriato.
//    LaunchedEffect(isExpanded) {
//        rotationAngle.animateTo(
//            targetValue = if (isExpanded) 180f else 0f,
//            animationSpec = tween(durationMillis = 300) // Puoi personalizzare la durata e il tipo di animazione
//        )
//    }

    SegmentedListItem(
        shapes = ListItemDefaults.segmentedShapes(index = 0, count = itemCount),
        modifier = modifier,
        selected = false,
        colors = colors,
        trailingContent = {
            Box(
                modifier = Modifier
                    .width(30.dp)
                    .height(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize),
                    painter = painterResource(if (isExpanded) R.drawable.keyboard_arrow_up_24px else R.drawable.keyboard_arrow_down_24px),
                    contentDescription = null
                )
            }
        },
        supportingContent = {
            Text(text = item.subCantiCounter.toString())
        },
        onClick = { onHeaderClicked(item) }
    ) {
        Text(text = stringResource(item.titleRes))
    }

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PassageListItem(
    item: RisuscitoListItem,
    onItemClick: (RisuscitoListItem) -> Unit,
    onIconClick: (RisuscitoListItem) -> Unit,
    modifier: Modifier,
    index: Int,
    itemsCount: Int
) {
    SegmentedListItem(
        shapes = ListItemDefaults.segmentedShapes(index = index, count = itemsCount),
        selected = false,
        leadingContent = { PageText(stringResource(item.pageRes), item.rawColor) },
        modifier = modifier,
        trailingContent = {
            FilledTonalIconToggleButton(
                modifier = Modifier
                    .height(40.dp)
                    .width(35.dp),
                checked = item.numPassaggio != -1,
                onCheckedChange = { onIconClick(item) },
                shapes = IconButtonDefaults.toggleableShapes()
            ) {
                Icon(
                    modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize),
                    painter = painterResource(if (item.numPassaggio != -1) R.drawable.sell_filled_24px else R.drawable.sell_24px),
                    contentDescription = "Notation"
                )
            }
        },
        onClick = { onItemClick(item) }
    ) {
        Text(stringResource(item.titleRes))
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CheckableListItem(
    item: RisuscitoListItem,
    modifier: Modifier,
    onSelect: (Boolean) -> Unit,
    selected: Boolean = false,
    index: Int,
    itemsCount: Int
) {

    SegmentedListItem(
        shapes = ListItemDefaults.segmentedShapes(index = index, count = itemsCount),
        modifier = modifier,
        checked = selected,
        leadingContent = { PageText(stringResource(item.pageRes), item.rawColor) },
        trailingContent = {
            Checkbox(
                checked = selected,
                onCheckedChange = null
            )
        },
        onCheckedChange = { onSelect(!selected) }
    ) {
        Text(
            stringResource(item.titleRes),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RadioListItem(
    titleItem: String,
    onSelect: (Int) -> Unit,
    itemId: Int,
    selectedOption: Int
) {

    ListItem(
        selected = itemId == selectedOption,
        trailingContent = {
            RadioButton(
                selected = itemId == selectedOption,
                onClick = null // null recommended for accessibility with screen readers
            )
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        onClick = { onSelect(itemId) }
    ) {
        Text(titleItem)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NotableListItem(
    item: ListaPersonalizzataRisuscitoListItem,
    onItemClick: (ListaPersonalizzataRisuscitoListItem) -> Unit,
    onItemLongClick: (ListaPersonalizzataRisuscitoListItem) -> Unit,
    onNoteClick: (ListaPersonalizzataRisuscitoListItem) -> Unit,
    selected: Boolean,
    index: Int,
    itemsCount: Int
) {
    SegmentedListItem(
        shapes = ListItemDefaults.segmentedShapes(index = index, count = itemsCount),
        selected = selected,
        leadingContent = {
            AnimatedScaleContent(
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
        trailingContent = {
            if (!selected) {
                FilledTonalIconToggleButton(
                    modifier = Modifier
                        .height(40.dp)
                        .width(35.dp),
                    checked = item.nota.isNotEmpty(),
                    onCheckedChange = { onNoteClick(item) },
                    shapes = IconButtonDefaults.toggleableShapes()
                ) {
                    Icon(
                        modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize),
                        painter = painterResource(if (item.nota.isNotEmpty()) R.drawable.sticky_note_2_filled_24px else R.drawable.sticky_note_2_24px),
                        contentDescription = "Notation"
                    )
                }
            }
        },
        onClick = { onItemClick(item) },
        onLongClick = { onItemLongClick(item) }
    ) {
        Text(stringResource(item.titleRes))
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
        Column(
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            modifier = Modifier.padding(horizontal = 5.dp)
        ) {
            Text(
                text = titoloPosizione,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp)
            )
            posizioni.forEachIndexed { index, item ->
                NotableListItem(
                    item = item,
                    onItemClick = cantoClickListener,
                    onItemLongClick = cantoLongClickListener,
                    onNoteClick = noteClickListener,
                    selected = item.selected,
                    index = index,
                    itemsCount = posizioni.size
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DraggableDismissableListItem(
    modifier: Modifier,
    dragModifier: Modifier,
    interactionSource: MutableInteractionSource,
    swipeToDismissBoxState: SwipeToDismissBoxState,
    index: Int,
    item: SwipeableRisuscitoListItem,
    onItemLongClick: (Int, SwipeableRisuscitoListItem) -> Unit,
    onDismiss: (SwipeToDismissBoxValue, Int, SwipeableRisuscitoListItem) -> Unit,
    itemsCount: Int
) {
    SwipeToDismissBox(
        state = swipeToDismissBoxState,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        backgroundContent = {
            SwipeToDismissBackground(swipeToDismissBoxState)
        },
        onDismiss = { onDismiss(it, index, item) }
    ) {

        SegmentedListItem(
            shapes = ListItemDefaults.segmentedShapes(index = index, count = itemsCount),
            selected = false,
            trailingContent = {
                IconButton(
                    modifier = dragModifier
                        .height(40.dp)
                        .width(30.dp),
                    onClick = {},
                ) {
                    Icon(
                        modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize),
                        painter = painterResource(R.drawable.drag_handle_24px),
                        contentDescription = "Reorder"
                    )
                }
            },
            onClick = {},
            onLongClick = {
                onItemLongClick(
                    index,
                    item
                )
            },
            interactionSource = interactionSource
        ) {
            Text(item.title)
        }

    }
}