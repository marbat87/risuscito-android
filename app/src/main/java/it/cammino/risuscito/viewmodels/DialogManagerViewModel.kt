package it.cammino.risuscito.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import it.cammino.risuscito.ui.composable.dialogs.SimpleDialogTag

open class DialogManagerViewModel(application: Application) : AndroidViewModel(application) {

    val showAlertDialog = MutableLiveData(false)

    val dialogTitle = MutableLiveData<String>()

    val content = MutableLiveData<String>()

    val iconRes = MutableLiveData<Int>()

    val positiveButton = MutableLiveData<String>()

    val negativeButton = MutableLiveData<String>()

    var dialogTag: SimpleDialogTag = SimpleDialogTag.DEFAULT

    var dialogPrefill = -1



}
