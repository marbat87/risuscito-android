/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.cammino.risuscito.playback

import android.annotation.TargetApi
import android.content.ContentUris
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.*
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import android.util.Log
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.LUtils.Companion.hasQ
import it.cammino.risuscito.Utility.getExternalMediaIdByName
import it.cammino.risuscito.Utility.isDefaultLocationPublic
import it.cammino.risuscito.Utility.isExternalStorageReadable
import java.io.FileInputStream
import java.io.IOException

class Playback internal constructor(private val mService: MusicService,
                                    private var mMusicProvider: MusicProvider?) : AudioManager.OnAudioFocusChangeListener, OnCompletionListener, OnErrorListener, OnPreparedListener, OnSeekCompleteListener {
    private val mWifiLock: WifiManager.WifiLock
    var state = PlaybackStateCompat.STATE_NONE
        private set
    private var mPlayOnFocusGain: Boolean = false
    private var mCallback: Callback? = null
    @Volatile
    private var mCurrentPosition: Int = 0
    @Volatile
    private var mCurrentMediaId: String? = null
    private var mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK
    private var mAudioManager: AudioManager?
    private var mMediaPlayer: MediaPlayer? = null

    private val mPlaybackAttributes = AudioAttributesCompat.Builder()
            .setUsage(AudioAttributesCompat.USAGE_MEDIA)
            .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
            .build()
    private val mFocusRequest = AudioFocusRequestCompat.Builder(AudioManagerCompat.AUDIOFOCUS_GAIN)
            .setAudioAttributes(mPlaybackAttributes)
            .setOnAudioFocusChangeListener(this@Playback, Handler(Looper.getMainLooper()))
            .build()

    internal val isConnected: Boolean
        get() = true

    internal val isPlaying: Boolean
        get() = mPlayOnFocusGain || (mMediaPlayer?.isPlaying == true)

    internal val currentStreamPosition: Int
        get() = mMediaPlayer?.currentPosition ?: mCurrentPosition

    val duration: Long
        get() = if (state == PlaybackStateCompat.STATE_PLAYING) {
            mMediaPlayer?.duration?.toLong() ?: 0
        } else
            0

    init {
        val context = mService.applicationContext
        this.mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

        // Create the Wifi lock (this does not acquire the lock, this just creates it).

        this.mWifiLock = (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
                .createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "sample_lock")
    }

    internal fun stop() {
        state = PlaybackStateCompat.STATE_STOPPED
        mCallback?.onPlaybackStatusChanged(state)
        mCurrentPosition = 0
        // Give up Audio focus
        giveUpAudioFocus()
        // Relax all resources
        relaxResources(true)
        if (mWifiLock.isHeld) mWifiLock.release()
    }

    internal fun setmMusicProvider(mMusicProvider: MusicProvider?) {
        this.mMusicProvider = mMusicProvider
    }

    fun play(item: MediaSessionCompat.QueueItem?) {
        mPlayOnFocusGain = true
        tryToGetAudioFocus()
        val mediaId = item?.description?.mediaId
        val mediaHasChanged = !TextUtils.equals(mediaId, mCurrentMediaId)
        if (mediaHasChanged) {
            mCurrentPosition = 0
            mCurrentMediaId = mediaId
        }

        if (state == PlaybackStateCompat.STATE_PAUSED && !mediaHasChanged && mMediaPlayer != null) {
            configMediaPlayerState()
        } else {
            state = PlaybackStateCompat.STATE_STOPPED
            relaxResources(false) // release everything except MediaPlayer
            val track = mMusicProvider?.getMusic(item?.description?.mediaId)

            val source = track?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)
            try {
                createMediaPlayerIfNeeded()

                state = PlaybackStateCompat.STATE_BUFFERING

                // Android "O" makes much greater use of AudioAttributes, especially
                // with regards to AudioFocus. All of UAMP's tracks are music, but
                // if your content includes spoken word such as audiobooks or podcasts
                // then the content type should be set to CONTENT_TYPE_SPEECH for those
                // tracks.
                //                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                setStreamType()

                Log.d(TAG, "play: $source")

                if (source?.startsWith("http") == true)
                    mMediaPlayer?.setDataSource(source)
                else {
                    if (hasQ() && isExternalStorageReadable && isDefaultLocationPublic(mService.applicationContext))
                        source?.let { mSource ->
                            val mUri = ContentUris.withAppendedId(MediaStore.Audio.Media
                                    .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), getExternalMediaIdByName(mService.applicationContext, mSource))
                            mMediaPlayer?.setDataSource(mService.applicationContext, mUri)
                        } ?: run {
                            mCallback?.onError("NULL AUDIO LINK")
                        }
                    else {
                        source?.let {
                            val fileInputStream = FileInputStream(it)
                            mMediaPlayer?.setDataSource(fileInputStream.fd)
                            fileInputStream.close()
                        } ?: run {
                            mCallback?.onError("NULL AUDIO LINK")
                        }
                    }
                }

                // Starts preparing the media player in the background. When
                // it's done, it will call our OnPreparedListener (that is,
                // the onPrepared() method on this class, since we set the
                // listener to 'this'). Until the media player is prepared,
                // we *cannot* call start() on it!
                mMediaPlayer?.prepareAsync()

                // If we are streaming from the internet, we want to hold a
                // Wifi lock, which prevents the Wifi radio from going to
                // sleep while the song is playing.
                if (source?.startsWith("http") == true)
                    mWifiLock.acquire()
                else {
                    if (mWifiLock.isHeld) mWifiLock.release()
                }

                mCallback?.onPlaybackStatusChanged(state)

            } catch (ioException: IOException) {
                Log.e(TAG, "Exception playing song", ioException)
                mCallback?.onError(ioException.message)
            }

        }
    }

    internal fun pause() {
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            // Pause media player and cancel the 'foreground service' state.
            if (mMediaPlayer?.isPlaying == true) {
                mMediaPlayer?.pause()
                mCurrentPosition = mMediaPlayer?.currentPosition ?: 0
            }
            // while paused, retain the MediaPlayer but give up audio focus
            relaxResources(false)
        }
        state = PlaybackStateCompat.STATE_PAUSED
        mCallback?.onPlaybackStatusChanged(state)
    }

    internal fun seekTo(position: Int) {
        Log.d(TAG, "seekTo called with $position")

        if (mMediaPlayer == null) {
            // If we do not have a current media player, simply update the current position.
            mCurrentPosition = position
        } else {
            if (mMediaPlayer?.isPlaying == true) {
                state = PlaybackStateCompat.STATE_BUFFERING
            }
            mMediaPlayer?.seekTo(position)
            mCallback?.onPlaybackStatusChanged(state)
        }
    }

    fun setCallback(callback: Callback) {
        this.mCallback = callback
    }

    private fun tryToGetAudioFocus() {
        Log.d(TAG, "tryToGetAudioFocus")
        mAudioManager?.let {
            val result = AudioManagerCompat.requestAudioFocus(it, mFocusRequest)
            mAudioFocus = if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                AUDIO_FOCUSED
            else
                AUDIO_NO_FOCUS_NO_DUCK
        }
    }

    private fun giveUpAudioFocus() {
        Log.d(TAG, "giveUpAudioFocus")
        mAudioManager?.let {
            if (AudioManagerCompat.abandonAudioFocusRequest(it, mFocusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK
        }
    }

    private fun configMediaPlayerState() {
        Log.d(TAG, "configMediaPlayerState. mAudioFocus=$mAudioFocus")
        if (mAudioFocus == AUDIO_NO_FOCUS_NO_DUCK) {
            // If we don't have audio focus and can't duck, we have to pause,
            if (state == PlaybackStateCompat.STATE_PLAYING) {
                pause()
            }
        } else { // we have audio focus:
            if (mAudioFocus == AUDIO_NO_FOCUS_CAN_DUCK) {
                mMediaPlayer?.setVolume(VOLUME_DUCK, VOLUME_DUCK) // we'll be relatively quiet
            } else
                mMediaPlayer?.setVolume(VOLUME_NORMAL, VOLUME_NORMAL) // we can be loud again
            // If we were playing when we lost focus, we need to resume playing.
            if (mPlayOnFocusGain) {
                if (mMediaPlayer != null && mMediaPlayer?.isPlaying != true) {
                    Log.d(TAG, "configMediaPlayerState startMediaPlayer. seeking to $mCurrentPosition")
                    state = if (mCurrentPosition == mMediaPlayer?.currentPosition) {
                        mMediaPlayer?.start()
                        PlaybackStateCompat.STATE_PLAYING
                    } else {
                        mMediaPlayer?.seekTo(mCurrentPosition)
                        PlaybackStateCompat.STATE_BUFFERING
                    }
                }
                mPlayOnFocusGain = false
            }
        }
        mCallback?.onPlaybackStatusChanged(state)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        Log.d(TAG, "onAudioFocusChange. focusChange= $focusChange")
        if (focusChange == AudioManagerCompat.AUDIOFOCUS_GAIN) {
            // We have gained focus:
            mAudioFocus = AUDIO_FOCUSED

        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS
                || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            // We have lost focus. If we can duck (low playback volume), we can keep playing.
            // Otherwise, we need to pause the playback.
            val canDuck = focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
            mAudioFocus = if (canDuck) AUDIO_NO_FOCUS_CAN_DUCK else AUDIO_NO_FOCUS_NO_DUCK

            // If we are playing, we need to reset media player by calling configMediaPlayerState
            // with mAudioFocus properly set.
            if (state == PlaybackStateCompat.STATE_PLAYING && !canDuck) {
                // If we don't have audio focus and can't duck, we save the information that
                // we were playing, so that we can resume playback once we get the focus back.
                mPlayOnFocusGain = true
            }
        } else {
            Log.e(TAG, "onAudioFocusChange: Ignoring unsupported focusChange: $focusChange")
        }
        configMediaPlayerState()
    }

    override fun onSeekComplete(player: MediaPlayer) {
        Log.d(TAG, "onSeekComplete from MediaPlayer:" + player.currentPosition)
        mCurrentPosition = player.currentPosition
        if (state == PlaybackStateCompat.STATE_BUFFERING) {
            mMediaPlayer?.start()
            state = PlaybackStateCompat.STATE_PLAYING
        }
        mCallback?.onPlaybackStatusChanged(state)
    }

    override fun onCompletion(player: MediaPlayer) {
        Log.d(TAG, "onCompletion from MediaPlayer")
        // The media player finished playing the current song, so we go ahead
        // and start the next.
        mCallback?.onCompletion()
    }

    override fun onPrepared(player: MediaPlayer) {
        Log.d(TAG, "onPrepared from MediaPlayer")
        // The media player is done preparing. That means we can start playing if we
        // have audio focus.
        configMediaPlayerState()
    }

    override fun onError(player: MediaPlayer, what: Int, extra: Int): Boolean {
        Log.e(TAG, "Media player error: what=$what, extra=$extra")
        mCallback?.onError("MediaPlayer error $what ($extra)")
        return true // true indicates we handled the error
    }

    private fun createMediaPlayerIfNeeded() {
        Log.d(TAG, "createMediaPlayerIfNeeded. needed? " + (mMediaPlayer == null))
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer()

            // Make sure the media player will acquire a wake-lock while
            // playing. If we don't do that, the CPU might go to sleep while the
            // song is playing, causing playback to stop.
            mMediaPlayer?.setWakeMode(mService.applicationContext, PowerManager.PARTIAL_WAKE_LOCK)

            // we want the media player to notify us when it's ready preparing,
            // and when it's done playing:
            mMediaPlayer?.setOnPreparedListener(this)
            mMediaPlayer?.setOnCompletionListener(this)
            mMediaPlayer?.setOnErrorListener(this)
            mMediaPlayer?.setOnSeekCompleteListener(this)
        } else {
            mMediaPlayer?.reset()
        }
    }

    private fun relaxResources(releaseMediaPlayer: Boolean) {
        Log.d(TAG, "relaxResources. releaseMediaPlayer= $releaseMediaPlayer")

        mService.stopForeground(true)

        // stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mMediaPlayer != null) {
            mMediaPlayer?.reset()
            mMediaPlayer?.release()
            mMediaPlayer = null
        }

        // we can also release the Wifi lock, if we're holding it
        if (mWifiLock.isHeld) mWifiLock.release()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setStreamTypeLollipop() {
        (mPlaybackAttributes.unwrap() as? AudioAttributes)?.let { mMediaPlayer?.setAudioAttributes(it) }
    }

    @Suppress("DEPRECATION")
    private fun setStreamTypeLegacy() {
        mMediaPlayer?.setAudioStreamType(mPlaybackAttributes.legacyStreamType)
    }

    private fun setStreamType() {
        if (LUtils.hasL())
            setStreamTypeLollipop()
        else
            setStreamTypeLegacy()
    }

    interface Callback {
        fun onCompletion()
        fun onPlaybackStatusChanged(state: Int)
        fun onError(error: String?)
    }

    companion object {

        private val TAG = Playback::class.java.simpleName
        // The volume we set the media player to when we lose audio focus, but are
        // allowed to reduce the volume instead of stopping playback.
        private const val VOLUME_DUCK = 0.2f
        // The volume we set the media player when we have audio focus.
        private const val VOLUME_NORMAL = 1.0f
        // we don't have audio focus, and can't duck (play at a low volume)
        private const val AUDIO_NO_FOCUS_NO_DUCK = 0
        // we don't have focus, but can duck (play at a low volume)
        private const val AUDIO_NO_FOCUS_CAN_DUCK = 1
        // we have full audio focus
        private const val AUDIO_FOCUSED = 2
    }
}
