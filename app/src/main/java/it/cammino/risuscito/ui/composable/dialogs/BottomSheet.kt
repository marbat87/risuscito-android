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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
            Column(Modifier.fillMaxSize()) {
                if (bottomSheetViewModel.titleTextRes.intValue > 0) {
                    BottomSheetTitle(stringResource(bottomSheetViewModel.titleTextRes.intValue))
                    Spacer(modifier = Modifier.padding(8.dp))
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp, 0.dp, 24.dp, 24.dp),
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