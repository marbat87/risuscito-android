package it.cammino.risuscito.viewmodels

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import it.cammino.risuscito.R

class MainActivityViewModel : ViewModel() {

    var showSnackbar = true
    var backupRestoreState = MutableLiveData(BakupRestoreState.NONE)
    var httpRequestState = MutableLiveData(ClientState.STARTED)
    var loginState = MutableLiveData(LOGIN_STATE_STARTED)
//    var retrieveLastAccount = false

    var isTabletWithFixedDrawer: Boolean = false
    var isTabletWithNoFixedDrawer: Boolean = false
    var selectedMenuItemId: Int = R.id.navigation_indexes
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

    enum class ClientState {
        STARTED,
        COMPLETED
    }

    var acct: GoogleIdTokenCredential? = null
    var sub: String = ""

    companion object {
        const val LOGIN_STATE_STARTED = "LoginStateStarted"
        const val LOGIN_STATE_OK = "LoginStateOk"
    }

}
