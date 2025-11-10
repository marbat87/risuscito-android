package it.cammino.risuscito.viewmodels

import android.content.Intent
import android.content.pm.ResolveInfo
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class SharedBottomSheetViewModel : ViewModel() {

    val showBottomSheet = mutableStateOf(false)
    val titleTextRes = mutableIntStateOf(0)
    var appList = mutableListOf<ResolveInfo>()
    val intent = mutableStateOf<Intent?>(null)

}