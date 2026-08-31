package it.cammino.risuscito.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import it.cammino.risuscito.ui.composable.dialogs.InputDialogTag
import it.cammino.risuscito.utils.StringUtils

open class InputDialogManagerViewModel(application: Application) : AndroidViewModel(application) {

    val showAlertDialog = MutableLiveData(false)

    var dialogTag = InputDialogTag.DEFAULT

    var dialogPrefill = StringUtils.EMPTY

    var outputItemId = 0

    var outputCantoId = 0

    var dialogTitleRes = 0

    var confirmationLabelRes = 0

}
