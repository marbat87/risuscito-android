package it.cammino.risuscito.ui.composable.dialogs

import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.michaelflisar.composechangelog.Changelog
import com.michaelflisar.composechangelog.ChangelogDefaults
import com.michaelflisar.composechangelog.classes.rememberChangelogState
import com.michaelflisar.composechangelog.getAppVersionName
import com.michaelflisar.composechangelog.setup
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.RisuscitoApplication
import it.cammino.risuscito.ui.activity.ChangelogActivity.Companion.CHANGELOG_FORMATTER
import it.cammino.risuscito.ui.composable.BottomSheetItem
import it.cammino.risuscito.ui.composable.BottomSheetTitle
import it.cammino.risuscito.viewmodels.SharedBottomSheetViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RisuscitoBottomSheet(
    bottomSheetViewModel: SharedBottomSheetViewModel,
    onItemClick: (ResolveInfo) -> Unit,
    pm: PackageManager,
) {

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    if (bottomSheetViewModel.showBottomSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { bottomSheetViewModel.showBottomSheet.value = false },
            sheetState = sheetState
        ) {
            val margin = 24.dp
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(start = margin, end = margin, bottom = margin)
            ) {
                if (bottomSheetViewModel.titleTextRes.intValue > 0) {
                    BottomSheetTitle(stringResource(bottomSheetViewModel.titleTextRes.intValue))
                    Spacer(modifier = Modifier.padding(8.dp))
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.padding(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    items(bottomSheetViewModel.appList) { app ->
                        BottomSheetItem(
                            app,
                            pm,
                            onItemClick = { selectedProduct ->
                                onItemClick(selectedProduct)
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        bottomSheetViewModel.showBottomSheet.value = false
                                    }
                                }
                            })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogBottomSheet() {

    val context = LocalContext.current

    // needed - you can also provide your own implementation instead of this simple one
    // (which simply saves the last shown version inside a preference file)
//    val changelogStateSaver = remember { ChangelogStateSaverPreferences.create(context) }
    val application = context.applicationContext as RisuscitoApplication // Ottieni l'istanza dell'applicazione
    // Usa l'istanza singleton invece di crearne una nuova
    val changelogStateSaver = application.changelogStateSaver

    // optional - here you can apply some customisations like changelog resource id, localized texts, styles, filter, sorter, ...
    val setup = ChangelogDefaults.setup(
        context = context,
        versionFormatter = CHANGELOG_FORMATTER
    )

    // Changelog - this will show the changelog once only if the changelog was not shown for the current app version yet
    val versionName = Changelog.getAppVersionName(context)

    val changelogState = rememberChangelogState()
    // initially we check if we need to show the changelog
    // this is optional of course...
    LaunchedEffect(Unit) {
        changelogState.checkShouldShowChangelogOnStart(
            changelogStateSaver,
            versionName,
            CHANGELOG_FORMATTER
        )
    }

    if (changelogState.visible) {
        ModalBottomSheet(
            onDismissRequest = {
                changelogState.hide()
            }
        ) {
            val margin = 24.dp
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(start = margin, end = margin, bottom = margin)
            ) {
                BottomSheetTitle(stringResource(R.string.dialog_change_title))
                Spacer(modifier = Modifier.padding(8.dp))
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Changelog(
                    state = changelogState, setup = setup,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }

}