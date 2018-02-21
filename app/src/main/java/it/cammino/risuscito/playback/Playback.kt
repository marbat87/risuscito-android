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
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.*
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import android.util.Log
import it.cammino.risuscito.LUtils
import java.io.FileInputStream
import java.io.IOException
import android.media.AudioFocusRequest
import android.os.Handler


/** A class that implements local media playback using [MediaPlayer]  */
class Playback internal constructor(private val mService: MusicService, //    private final MusicProvider mMusicProvider;
                                    private var mMusicProvider: MusicProvider?) : AudioManager.OnAudioFocusChangeListener, OnCompletionListener, OnErrorListener, OnPreparedListener, OnSeekCompleteListener {
    private val mWifiLock: WifiManager.WifiLock
    var state = PlaybackStateCompat.STATE_NONE
        private set
    private var mPlayOnFocusGain: Boolean = false
    private var mCallback: Callback? = null
    @Volatile private var mCurrentPosition: Int = 0
    @Volatile private var mCurrentMediaId: String? = null
    // Type of audio focus we have:
    private var mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK
    private val mAudioManager: AudioManager
    private var mMediaPlayer: MediaPlayer? = null

    private val mPlaybackAttributes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
    }
    else {null}
    private val mFocusRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(mPlaybackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this, Handler())
                .build()
    }
    else {null}

    internal val isConnected: Boolean
        get() = true

    internal val isPlaying: Boolean
        get() = mPlayOnFocusGain || mMediaPlayer != null && mMediaPlayer!!.isPlaying

    internal val currentStreamPosition: Int
        get() = if (mMediaPlayer != null) mMediaPlayer!!.currentPosition else mCurrentPosition

    val duration: Long
        get() = if (mMediaPlayer != null && state == PlaybackStateCompat.STATE_PLAYING) {
            mMediaPlayer!!.duration.toLong()
        } else
            0

    init {
        val context = mService.applicationContext
        this.mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Create the Wifi lock (this does not acquire the lock, this just creates it).

        this.mWifiLock = (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "sample_lock")
    }

    internal fun stop() {
        state = PlaybackStateCompat.STATE_STOPPED
        if (mCallback != null) {
            mCallback!!.onPlaybackStatusChanged(state)
        }
        // prova perchè al completamento della registrazione, il successivo play ripartiva dalla fine
        //        mCurrentPosition = getCurrentStreamPosition();
        mCurrentPosition = 0
        // Give up Audio focus
        giveUpAudioFocus()
        // Relax all resources
        relaxResources(true)
        if (mWifiLock.isHeld) mWifiLock.release()
    }

    internal fun setmMusicProvider(mMusicProvider: MusicProvider) {
        this.mMusicProvider = mMusicProvider
    }

    fun play(item: MediaSessionCompat.QueueItem) {
        mPlayOnFocusGain = true
        tryToGetAudioFocus()
        val mediaId = item.description.mediaId
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
            val track = mMusicProvider!!.getMusic(item.description.mediaId!!)

            val source = track!!.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)
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

                Log.d(TAG, "play: " + source)

                if (source.startsWith("http"))
                    mMediaPlayer!!.setDataSource(source)
                else {
                    val fileInputStream = FileInputStream(source)
                    mMediaPlayer!!.setDataSource(fileInputStream.fd)
                    fileInputStream.close()
                }

                //                Log.d(TAG, "play: " + source);
                //                mMediaPlayer.setDataSource(source);

                // Starts preparing the media player in the background. When
                // it's done, it will call our OnPreparedListener (that is,
                // the onPrepared() method on this class, since we set the
                // listener to 'this'). Until the media player is prepared,
                // we *cannot* call start() on it!
                mMediaPlayer!!.prepareAsync()

                // If we are streaming from the internet, we want to hold a
                // Wifi lock, which prevents the Wifi radio from going to
                // sleep while the song is playing.
                if (source.startsWith("http"))
                    mWifiLock.acquire()
                else {
                    if (mWifiLock.isHeld) mWifiLock.release()
                }

                if (mCallback != null) {
                    mCallback!!.onPlaybackStatusChanged(state)
                }

            } catch (ioException: IOException) {
                Log.e(TAG, "Exception playing song", ioException)
                if (mCallback != null) {
                    mCallback!!.onError(ioException.message!!)
                }
            }

        }
    }

    internal fun pause() {
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            // Pause media player and cancel the 'foreground service' state.
            if (mMediaPlayer != null && mMediaPlayer!!.isPlaying) {
                mMediaPlayer!!.pause()
                mCurrentPosition = mMediaPlayer!!.currentPosition
            }
            // while paused, retain the MediaPlayer but give up audio focus
            relaxResources(false)
        }
        state = PlaybackStateCompat.STATE_PAUSED
        if (mCallback != null) {
            mCallback!!.onPlaybackStatusChanged(state)
        }
    }

    internal fun seekTo(position: Int) {
        Log.d(TAG, "seekTo called with " + position)

        if (mMediaPlayer == null) {
            // If we do not have a current media player, simply update the current position.
            mCurrentPosition = position
        } else {
            if (mMediaPlayer!!.isPlaying) {
                state = PlaybackStateCompat.STATE_BUFFERING
            }
            mMediaPlayer!!.seekTo(position)
            if (mCallback != null) {
                mCallback!!.onPlaybackStatusChanged(state)
            }
        }
    }

    fun setCallback(callback: Callback) {
        this.mCallback = callback
    }

    /** Try to get the system audio focus.  */
    private fun tryToGetAudioFocus() {
        Log.d(TAG, "tryToGetAudioFocus")
//        val result = mAudioManager.requestAudioFocus(
//                this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        val result = requestAudioFocus()
        mAudioFocus = if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            AUDIO_FOCUSED
        else
            AUDIO_NO_FOCUS_NO_DUCK
    }

    /** Give up the audio focus.  */
    private fun giveUpAudioFocus() {
        Log.d(TAG, "giveUpAudioFocus")
//        if (mAudioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
        if (abandonAudioFocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK
        }
    }

    /**
     * Reconfigures MediaPlayer according to audio focus settings and starts/restarts it. This method
     * starts/restarts the MediaPlayer respecting the current audio focus state. So if we have focus,
     * it will play normally; if we don't have focus, it will either leave the MediaPlayer paused or
     * set it to a low volume, depending on what is allowed by the current focus settings. This method
     * assumes mPlayer != null, so if you are calling it, you have to do so from a context where you
     * are sure this is the case.
     */
    private fun configMediaPlayerState() {
        Log.d(TAG, "configMediaPlayerState. mAudioFocus=" + mAudioFocus)
        if (mAudioFocus == AUDIO_NO_FOCUS_NO_DUCK) {
            // If we don't have audio focus and can't duck, we have to pause,
            if (state == PlaybackStateCompat.STATE_PLAYING) {
                pause()
            }
        } else { // we have audio focus:
            if (mAudioFocus == AUDIO_NO_FOCUS_CAN_DUCK) {
                mMediaPlayer!!.setVolume(VOLUME_DUCK, VOLUME_DUCK) // we'll be relatively quiet
            } else {
                if (mMediaPlayer != null) {
                    mMediaPlayer!!.setVolume(VOLUME_NORMAL, VOLUME_NORMAL) // we can be loud again
                } // else do something for remote client.
            }
            // If we were playing when we lost focus, we need to resume playing.
            if (mPlayOnFocusGain) {
                if (mMediaPlayer != null && !mMediaPlayer!!.isPlaying) {
                    Log.d(TAG, "configMediaPlayerState startMediaPlayer. seeking to " + mCurrentPosition)
                    state = if (mCurrentPosition == mMediaPlayer!!.currentPosition) {
                        mMediaPlayer!!.start()
                        PlaybackStateCompat.STATE_PLAYING
                    } else {
                        mMediaPlayer!!.seekTo(mCurrentPosition)
                        PlaybackStateCompat.STATE_BUFFERING
                    }
                }
                mPlayOnFocusGain = false
            }
        }
        if (mCallback != null) {
            mCallback!!.onPlaybackStatusChanged(state)
        }
    }

    /**
     * Called by AudioManager on audio focus changes. Implementation of [ ].
     */
    override fun onAudioFocusChange(focusChange: Int) {
        Log.d(TAG, "onAudioFocusChange. focusChange=" + focusChange)
        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
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
            Log.e(TAG, "onAudioFocusChange: Ignoring unsupported focusChange: " + focusChange)
        }
        configMediaPlayerState()
    }

    /**
     * Called when MediaPlayer has completed a seek.
     *
     * @see OnSeekCompleteListener
     */
    override fun onSeekComplete(player: MediaPlayer) {
        Log.d(TAG, "onSeekComplete from MediaPlayer:" + player.currentPosition)
        mCurrentPosition = player.currentPosition
        if (state == PlaybackStateCompat.STATE_BUFFERING) {
            mMediaPlayer!!.start()
            state = PlaybackStateCompat.STATE_PLAYING
        }
        if (mCallback != null) {
            mCallback!!.onPlaybackStatusChanged(state)
        }
    }

    /**
     * Called when media player is done playing current song.
     *
     * @see OnCompletionListener
     */
    override fun onCompletion(player: MediaPlayer) {
        Log.d(TAG, "onCompletion from MediaPlayer")
        // The media player finished playing the current song, so we go ahead
        // and start the next.
        if (mCallback != null) {
            mCallback!!.onCompletion()
        }
    }

    /**
     * Called when media player is done preparing.
     *
     * @see OnPreparedListener
     */
    override fun onPrepared(player: MediaPlayer) {
        Log.d(TAG, "onPrepared from MediaPlayer")
        // The media player is done preparing. That means we can start playing if we
        // have audio focus.
        configMediaPlayerState()
    }

    /**
     * Called when there's an error playing media. When this happens, the media player goes to the
     * Error state. We warn the user about the error and reset the media player.
     *
     * @see OnErrorListener
     */
    override fun onError(player: MediaPlayer, what: Int, extra: Int): Boolean {
        Log.e(TAG, "Media player error: what=$what, extra=$extra")
        if (mCallback != null) {
            mCallback!!.onError("MediaPlayer error $what ($extra)")
        }
        return true // true indicates we handled the error
    }

    /**
     * Makes sure the media player exists and has been reset. This will create the media player if
     * needed, or reset the existing media player if one already exists.
     */
    private fun createMediaPlayerIfNeeded() {
        Log.d(TAG, "createMediaPlayerIfNeeded. needed? " + (mMediaPlayer == null))
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer()

            // Make sure the media player will acquire a wake-lock while
            // playing. If we don't do that, the CPU might go to sleep while the
            // song is playing, causing playback to stop.
            mMediaPlayer!!.setWakeMode(mService.applicationContext, PowerManager.PARTIAL_WAKE_LOCK)

            // we want the media player to notify us when it's ready preparing,
            // and when it's done playing:
            mMediaPlayer!!.setOnPreparedListener(this)
            mMediaPlayer!!.setOnCompletionListener(this)
            mMediaPlayer!!.setOnErrorListener(this)
            mMediaPlayer!!.setOnSeekCompleteListener(this)
        } else {
            mMediaPlayer!!.reset()
        }
    }

    /**
     * Releases resources used by the service for playback. This includes the "foreground service"
     * status, the wake locks and possibly the MediaPlayer.
     *
     * @param releaseMediaPlayer Indicates whether the Media Player should also be released or not.
     */
    private fun relaxResources(releaseMediaPlayer: Boolean) {
        Log.d(TAG, "relaxResources. releaseMediaPlayer=" + releaseMediaPlayer)

        mService.stopForeground(true)

        // stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mMediaPlayer != null) {
            mMediaPlayer!!.reset()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }

        // we can also release the Wifi lock, if we're holding it
        if (mWifiLock.isHeld) mWifiLock.release()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setStreamTypeLollipop() {
//        val mPlaybackAttributes = AudioAttributes.Builder()
//                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
//                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
//                .build()
        mMediaPlayer!!.setAudioAttributes(mPlaybackAttributes)
    }

    @Suppress("DEPRECATION")
    private fun setStreamTypeLegacy() {
        mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
    }

    private fun setStreamType() {
        if (LUtils.hasL())
            setStreamTypeLollipop()
        else
            setStreamTypeLegacy()
    }

    private fun requestAudioFocus() : Int {
        return if (LUtils.hasO())
            requestAudioFocusO()
        else
            requestAudioFocusLegacy()
    }

    @Suppress("DEPRECATION")
    private fun  requestAudioFocusLegacy(): Int {
        return mAudioManager.requestAudioFocus(
                this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun requestAudioFocusO(): Int {
        return mAudioManager.requestAudioFocus(mFocusRequest)
    }

    private fun abandonAudioFocus(): Int {
        return if (LUtils.hasO())
            abandonAudioFocusO()
        else
            abandonAudioFocusLegacy()
    }

    @Suppress("DEPRECATION")
    private fun abandonAudioFocusLegacy(): Int {
        return mAudioManager.abandonAudioFocus(this)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun abandonAudioFocusO(): Int {
        return mAudioManager.abandonAudioFocusRequest(mFocusRequest)
    }

    /* package */  interface Callback {
        /** On current music completed.  */
        fun onCompletion()

        /**
         * on Playback status changed Implementations can use this callback to update playback state on
         * the media sessions.
         */
        fun onPlaybackStatusChanged(state: Int)

        /** @param error to be added to the PlaybackState
         */
        fun onError(error: String)
    }

    companion object {

        private val TAG = Playback::class.java.simpleName
        // The volume we set the media player to when we lose audio focus, but are
        // allowed to reduce the volume instead of stopping playback.
        private val VOLUME_DUCK = 0.2f
        // The volume we set the media player when we have audio focus.
        private val VOLUME_NORMAL = 1.0f
        // we don't have audio focus, and can't duck (play at a low volume)
        private val AUDIO_NO_FOCUS_NO_DUCK = 0
        // we don't have focus, but can duck (play at a low volume)
        private val AUDIO_NO_FOCUS_CAN_DUCK = 1
        // we have full audio focus
        private val AUDIO_FOCUSED = 2
    }
}
