package it.cammino.risuscito.viewmodels

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.cammino.risuscito.R

class MainActivityViewModel : ViewModel() {

    var showSnackbar = true
    var backupRestoreState = MutableLiveData(BakupRestoreState.NONE)

    var isTabletWithFixedDrawer: Boolean = false
    var isTabletWithNoFixedDrawer: Boolean = false
    var selectedMenuItemId: Int = R.id.navigation_indexes
    var catalogRefreshReady = MutableLiveData(true)
    var lastPlaybackState = MutableLiveData<PlaybackStateCompat>()
    var medatadaCompat = MutableLiveData<MediaMetadataCompat>()
    var playerConnected = MutableLiveData(false)
//    var searchBarExpanded = false

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
