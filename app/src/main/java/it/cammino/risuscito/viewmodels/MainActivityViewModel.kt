package it.cammino.risuscito.viewmodels

import android.app.Application
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData

class MainActivityViewModel(application: Application) : DialogManagerViewModel(application) {

    var backupRestoreState = MutableLiveData(BakupRestoreState.NONE)
    var httpRequestState = MutableLiveData(ClientState.STARTED)
    var loginState = MutableLiveData(LOGIN_STATE_STARTED)
    var profileAction = ProfileAction.NONE

    var isTabletWithFixedDrawer: Boolean = false
    var isTabletWithNoFixedDrawer: Boolean = false
    var catalogRefreshReady = MutableLiveData(true)
    var lastPlaybackState = MutableLiveData<PlaybackStateCompat>()
    var medatadaCompat = MutableLiveData<MediaMetadataCompat>()
    var playerConnected = MutableLiveData(false)

    enum class BakupRestoreState {
        NONE,
        BACKUP_STARTED,
        BACKUP_STEP_2,
        BACKUP_COMPLETED,
        RESTORE_STARTED,
        RESTORE_STEP_2,
        RESTORE_COMPLETED

    }

    enum class ProfileAction {
        BACKUP,
        RESTORE,
        NONE
    }

    enum class ClientState {
        STARTED,
        COMPLETED
    }

    var sub: String = ""

    companion object {
        const val LOGIN_STATE_STARTED = "LoginStateStarted"
        const val LOGIN_STATE_OK = "LoginStateOk"
        const val LOGIN_STATE_OK_SILENT = "LoginStateOkSilent"
    }

}
