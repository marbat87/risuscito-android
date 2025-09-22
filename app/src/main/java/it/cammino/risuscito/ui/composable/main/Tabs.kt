package it.cammino.risuscito.ui.composable.main

import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import it.cammino.risuscito.R

open class Destination(
    val route: String,
    val label: Int = 0,
    val labelString: String = ""
) {

    object CantiParola : Destination("canti_parola", R.string.title_activity_canti_parola)
    object CantiEucarestia :
        Destination("canti_eucarestia", R.string.title_activity_canti_eucarestia)

    object LetterOrderIndex : Destination("letter_order_index", R.string.letter_order_text)
    object PageOrderIndex : Destination("page_order_index", R.string.page_order_text)
    object IndiceLiturgicoIndex :
        Destination("indice_liturgico_index", R.string.indice_liturgico_index)

    object SalmiMusicaIndex : Destination("salmi_musica_index", R.string.salmi_musica_index)
}

val generalIndexesList = listOf(
    Destination.LetterOrderIndex,
    Destination.PageOrderIndex,
//    Destination.IndiceLiturgicoIndex,
    Destination.SalmiMusicaIndex
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RisuscitoTabs(
    selectedTabIndex: MutableIntState = mutableIntStateOf(0),
    tabsList: List<Destination>? = generalIndexesList,
    pagerState: PagerState
) {

    SecondaryScrollableTabRow(
        selectedTabIndex = selectedTabIndex.intValue
    ) {
        tabsList?.forEachIndexed { index, destination ->
            Tab(
                selected = selectedTabIndex.intValue == index,
                onClick = {
                    selectedTabIndex.intValue = index
                },
                text = {
                    Text(
                        text = if (destination.label > 0) stringResource(destination.label) else destination.labelString,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }

    LaunchedEffect(selectedTabIndex.intValue) {
        if (pagerState.currentPage != selectedTabIndex.intValue)
            pagerState.animateScrollToPage(selectedTabIndex.intValue)
    }
}