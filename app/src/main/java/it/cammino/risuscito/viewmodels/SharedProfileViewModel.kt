package it.cammino.risuscito.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import it.cammino.risuscito.utils.StringUtils

class SharedProfileViewModel : ViewModel() {

    val showProfileDialog = mutableStateOf(false)
    var profilePhotoUrl = StringUtils.EMPTY
    val profileNameStr = mutableStateOf(StringUtils.EMPTY)
    val profileEmailStr = mutableStateOf(StringUtils.EMPTY)

}