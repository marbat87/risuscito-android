package it.cammino.risuscito.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import it.cammino.risuscito.utils.StringUtils

class SharedSnackBarViewModel : ViewModel() {

    val showSnackBar = mutableStateOf(false)
    var snackbarMessage = StringUtils.EMPTY
    var actionLabel = StringUtils.EMPTY
    var snackBarTag = SnackBarTag.DEFAULT

}

enum class SnackBarTag {
    DEFAULT,
    ELEMENT_REMOVED
}