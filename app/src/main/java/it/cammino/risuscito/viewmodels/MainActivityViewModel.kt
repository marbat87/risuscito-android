package it.cammino.risuscito.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.cammino.risuscito.R

class MainActivityViewModel : ViewModel() {

    var showSnackbar = true
    var signedIn = MutableLiveData<Boolean>()
    var backupRestoreState = MutableLiveData(BakupRestoreState.NONE)

    var isTabletWithFixedDrawer: Boolean = false
    var isTabletWithNoFixedDrawer: Boolean = false
    var selectedMenuItemId: Int = R.id.navigation_home

    enum class BakupRestoreState {
        NONE,
        BACKUP_STARTED,
        BACKUP_STEP_2,
        BACKUP_COMPLETED,
        RESTORE_STARTED,
        RESTORE_STEP_2,
        RESTORE_COMPLETED

    }

}
