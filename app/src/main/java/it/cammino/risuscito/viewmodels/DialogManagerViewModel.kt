package it.cammino.risuscito.viewmodels

import android.app.Application
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

open class DialogManagerViewModel(application: Application) : AndroidViewModel(application) {

    val showAlertDialog = MutableLiveData(false)

    val dialogTitle = MutableLiveData<String>()

    val content = MutableLiveData<String>()

    val icon = MutableLiveData<ImageVector>()

    val positiveButton = MutableLiveData<String>()

    val negativeButton = MutableLiveData<String>()

    var dialogTag = ""

    var dialogPrefill = -1



}
