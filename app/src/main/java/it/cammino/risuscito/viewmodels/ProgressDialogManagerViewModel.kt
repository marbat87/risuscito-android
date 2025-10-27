package it.cammino.risuscito.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import it.cammino.risuscito.ui.composable.dialogs.ProgressDialogTag

open class ProgressDialogManagerViewModel(application: Application) :
    AndroidViewModel(application) {

    val showProgressDialog = MutableLiveData(false)

    var dialogTag = ProgressDialogTag.DEFAULT

    var dialogTitleRes = 0

    var dialogIconRes = 0

    val messageRes = MutableLiveData<Int>()

    var buttonTextRes = 0

    var indeterminate = true

    val progress = MutableLiveData<Float>()

}
