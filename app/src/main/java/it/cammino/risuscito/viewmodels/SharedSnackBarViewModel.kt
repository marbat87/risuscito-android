package it.cammino.risuscito.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import it.cammino.risuscito.utils.StringUtils

class SharedSnackBarViewModel : ViewModel() {

    val showSnackBar = mutableStateOf(false)
    var snackbarMessage = mutableStateOf(StringUtils.EMPTY)
    var actionLabel = mutableStateOf(StringUtils.EMPTY)
    var snackBarTag = SnackBarTag.DEFAULT

}

enum class SnackBarTag {
    DEFAULT,
    ELEMENT_REMOVED
}