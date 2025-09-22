package it.cammino.risuscito.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

open class DialogManagerViewModel(application: Application) : AndroidViewModel(application) {

    val showAlertDialog = MutableLiveData(false)

}
