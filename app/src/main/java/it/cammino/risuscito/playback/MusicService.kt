package it.cammino.risuscito.playback

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import it.cammino.risuscito.ui.activity.CantoHostActivity
import it.cammino.risuscito.ui.activity.MainActivity
import it.cammino.risuscito.utils.extension.isOnPhone
import java.util.concurrent.TimeUnit

class MusicService : MediaLibraryService() {

    private lateinit var player: Player
    private lateinit var mediaLibrarySession: MediaLibrarySession
    private var mMusicProvider: MusicProvider? = null

    // Indicates whether the service was started.
    private var mServiceStarted: Boolean = false

    private val mDelayedStopHandler = Handler(Looper.getMainLooper()) { msg ->
        if (msg.what == STOP_CMD && !player.isPlaying) {
            Log.d(TAG, "Stopping service")
            stopSelf()
            mServiceStarted = false
        }
        false
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")

        mMusicProvider = MusicProvider(applicationContext)
        sendMusicProviderStatusBroadcast(false)
        mMusicProvider?.retrieveMediaAsync(object : MusicProvider.Callback {
            override fun onMusicCatalogReady(success: Boolean) {
                sendMusicProviderStatusBroadcast(success)
            }
        })

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .build()

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    mDelayedStopHandler.removeCallbacksAndMessages(null)
                    if (!mServiceStarted) {
                        mServiceStarted = true
                    }
                } else {
                    mDelayedStopHandler.removeCallbacksAndMessages(null)
                    mDelayedStopHandler.sendEmptyMessageDelayed(STOP_CMD, STOP_DELAY)
                }
            }
        })

        val intent = Intent(
            this,
            if (applicationContext.isOnPhone) CantoHostActivity::class.java else MainActivity::class.java
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, REQUEST_CODE, intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mediaLibrarySession = MediaLibrarySession.Builder(this, player, LibraryCallback())
            .setSessionActivity(pendingIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return mediaLibrarySession
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        mDelayedStopHandler.removeCallbacksAndMessages(null)
        player.release()
        mediaLibrarySession.release()
    }

    private inner class LibraryCallback : MediaLibrarySession.Callback {
        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val root = MediaItem.Builder()
                .setMediaId(MusicProvider.MEDIA_ID_EMPTY_ROOT)
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setIsBrowsable(true)
                        .setIsPlayable(false)
                        .build()
                )
                .build()
            return Futures.immediateFuture(LibraryResult.ofItem(root, params))
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val items = when (parentId) {
                MusicProvider.MEDIA_ID_ROOT -> ImmutableList.copyOf(mMusicProvider?.allMusics ?: emptyList())
                else -> ImmutableList.of()
            }
            return Futures.immediateFuture(LibraryResult.ofItemList(items, params))
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            if (customCommand.customAction == ACTION_REFRESH) {
                mMusicProvider = MusicProvider(applicationContext)
                sendMusicProviderStatusBroadcast(false)
                mMusicProvider?.retrieveMediaAsync(object : MusicProvider.Callback {
                    override fun onMusicCatalogReady(success: Boolean) {
                        sendMusicProviderStatusBroadcast(success)
                    }
                })
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }

        @OptIn(UnstableApi::class)
        override fun onSetMediaItems(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
            startIndex: Int,
            startPositionMs: Long
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            // We want to make sure the URIs are resolved if they are just IDs
            val resolvedItems = mediaItems.map { item ->
                if (item.localConfiguration == null) {
                    mMusicProvider?.getMusic(item.mediaId) ?: item
                } else {
                    item
                }
            }
            return Futures.immediateFuture(MediaSession.MediaItemsWithStartPosition(resolvedItems, startIndex, startPositionMs))
        }
    }

    private fun sendMusicProviderStatusBroadcast(done: Boolean) {
        val intentBroadcast = Intent(BROADCAST_RETRIEVE_ASYNC)
        intentBroadcast.putExtra(MSG_RETRIEVE_DONE, done)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
    }

    companion object {
        private val TAG = MusicService::class.java.simpleName
        private const val REQUEST_CODE = 99
        private val STOP_DELAY = TimeUnit.MINUTES.toMillis(10)
        private const val STOP_CMD = 0x7c48
        const val ACTION_REFRESH = "itcr_media_action_refresh"
        const val BROADCAST_RETRIEVE_ASYNC = "itcr_media_broadcast_retrieve_async"
        const val MSG_RETRIEVE_DONE = "itcr_media_retrieve_done"
    }
}
