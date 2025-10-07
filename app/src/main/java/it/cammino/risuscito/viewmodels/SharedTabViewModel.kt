package it.cammino.risuscito.viewmodels

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class SharedTabViewModel : ViewModel() {

    var resetTab = mutableStateOf(true)

    val tabsSelectedIndex = mutableIntStateOf(0)

}