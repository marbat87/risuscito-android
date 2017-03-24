package it.cammino.risuscito.music;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import it.cammino.risuscito.LUtils;
import it.cammino.risuscito.PaginaRenderActivity;
import it.cammino.risuscito.R;
import it.cammino.risuscito.ui.ThemeableActivity;

public class MusicService extends Service implements OnCompletionListener, OnPreparedListener,
        OnErrorListener, MusicFocusable, PhoneStateHelper.PhoneListener {
    // The tag we put on debug messages
    final String TAG = getClass().getName();
    // These are the Intent actions that we are prepared to handle. Notice that the fact these
    // constants exist in our class is a mere convenience: what really defines the actions our
    // service can handle are the <action> tags in the <intent-filters> tag for our service in
    // AndroidManifest.xml.
    public static final String ACTION_TOGGLE_PLAYBACK = "it.cammino.risuscito.music.action.TOGGLE_PLAYBACK";
    public static final String ACTION_PLAY = "it.cammino.risuscito.music.action.PLAY";
    public static final String ACTION_PAUSE = "it.cammino.risuscito.music.action.PAUSE";
    public static final String ACTION_STOP = "it.cammino.risuscito.music.action.STOP";
    public static final String ACTION_SKIP = "it.cammino.risuscito.music.action.SKIP";
    public static final String ACTION_REWIND = "it.cammino.risuscito.music.action.REWIND";
    public static final String ACTION_URL = "it.cammino.risuscito.music.action.URL";
    public static final String ACTION_SEEK = "it.cammino.risuscito.music.action.SEEK";
    public static final String BROADCAST_PREPARING_COMPLETED = "preparing_completed";
    public static final String BROADCAST_PLAYBACK_COMPLETED = "playback_completed";
    public static final String BROADCAST_PLAYER_POSITION = "player_position";
    public static final String BROADCAST_PLAYBACK_PAUSED = "playback_paused";
    public static final String BROADCAST_PLAYER_STARTED = "playback_started";
    public static final String DATA_DURATION = "data_duration";
    public static final String DATA_POSITION = "data_position";
    public static final String DATA_COLOR = "data_color";
    public static final String DATA_TITLE = "data_title";
    public static final String DATA_LOCAL = "data_local";
    // The volume we set the media player to when we lose audio focus, but are allowed to reduce
    // the volume instead of stopping playback.
    public static final float DUCK_VOLUME = 0.1f;
    // our media player
    MediaPlayer mPlayer = null;
    // our AudioFocusHelper object, if it's available (it's available on SDK level >= 8)
    // If not available, this will be null. Always check for null before using!
    AudioFocusHelper mAudioFocusHelper = null;
    PhoneStateHelper mPhoneStateHelper = null;

    Item playingItem;

    // indicates the state our service:
    enum State {
//        Retrieving, // the MediaRetriever is retrieving music
        Stopped,    // media player is stopped and not prepared to play
        Preparing,  // media player is preparing...
        Playing,    // playback active (media player ready!). (but the media player may actually be
        // paused in this state if we don't have audio focus. But we stay in this state
        // so that we know we have to resume playback once we get focus back)
        Paused      // playback paused (media player ready!)
    }

    //    State mState = State.Retrieving;
    State mState = State.Stopped;
    // if in Retrieving mode, this flag indicates whether we should start playing immediately
    // when we are ready or not.
    boolean mStartPlayingAfterRetrieve = false;
    // if mStartPlayingAfterRetrieve is true, this variable indicates the URL that we should
    // start playing when we are ready. If null, we should play a random song from the device
    Uri mWhatToPlayAfterRetrieve = null;

    //    enum PauseReason {
//        UserRequest,  // paused by user request
//        FocusLoss,    // paused because of audio focus loss
//    }
    // why did we pause? (only relevant if mState == State.Paused)
//    PauseReason mPauseReason = PauseReason.UserRequest;
    // do we have audio focus?
    enum AudioFocus {
        NoFocusNoDuck,    // we don't have audio focus, and can't duck
        NoFocusCanDuck,   // we don't have focus, but can play at a low volume ("ducking")
        Focused           // we have full audio focus
    }

    AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;
    // title of the song we are currently playing
    String mSongTitle = "";
    boolean mLocalFile = false;
    // whether the song we are playing is streaming from the network
    boolean mIsStreaming = false;
    // Wifi lock that we hold when streaming files from the internet, in order to prevent the
    // device from shutting off the Wifi radio
    WifiLock mWifiLock;
    // The ID we use for the notification (the onscreen alert that appears at the notification
    // area at the top of the screen as an icon -- and as text as well if the user expands the
    // notification area).
    final int NOTIFICATION_ID = 1;
    // Our instance of our MusicRetriever, which handles scanning for media and
    // providing titles and URIs as we need.
//    MusicRetriever mRetriever;
    // our RemoteControlClient object, which will use remote control APIs available in
    // SDK level >= 14, if they're available.
//    RemoteControlClientCompat mRemoteControlClientCompat;
    // Dummy album art we will pass to the remote control (if the APIs are available).
    Bitmap mDummyAlbumArt;
    // The component name of MusicIntentReceiver, for use with media button and remote control
    // APIs
    //ComponentName mMediaButtonReceiverComponent;
    AudioManager mAudioManager;
    NotificationManagerCompat mNotificationManager;
    int mNotificationColor;
//    NotificationCompat.Builder mNotificationBuilder = null;

    private MediaSessionCompat mSession;
    private MediaControllerCompat.TransportControls mTransportController;

    private boolean mPositionBroadcasterRunning = false;
    private Handler mHandler = new Handler();
    final Runnable mPositionBroadcaster = new Runnable() {
        public void run() {
            if (mPlayer != null && mState == State.Playing) {
                mPositionBroadcasterRunning = true;
                Log.d(TAG, "Sending broadcast notification: " + BROADCAST_PLAYER_POSITION);
                Log.d(TAG, "POSITION SEND: " + mPlayer.getCurrentPosition());
                Intent intentBroadcast = new Intent(BROADCAST_PLAYER_POSITION);
                intentBroadcast.putExtra(DATA_POSITION, mPlayer.getCurrentPosition());
                sendBroadcast(intentBroadcast);
                mHandler.postDelayed(this, 1000);
            } else {
                mPositionBroadcasterRunning = false;
                Log.d(getClass().getName(), "mediaPlayer nullo o non avviato!");
            }
        }
    };

    /**
     * Makes sure the media player exists and has been reset. This will create the media player
     * if needed, or reset the existing media player if one already exists.
     */
    void createMediaPlayerIfNeeded() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            // Make sure the media player will acquire a wake-lock while playing. If we don't do
            // that, the CPU might go to sleep while the song is playing, causing playback to stop.
            //
            // Remember that to use this, we have to declare the android.permission.WAKE_LOCK
            // permission in AndroidManifest.xml.
            mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            // we want the media player to notify us when it's ready preparing, and when it's done
            // playing:
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
        } else
            mPlayer.reset();
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "debug: Creating service");
        // Create the Wifi lock (this does not acquire the lock, this just creates it)
        mWifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
//        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotificationManager = NotificationManagerCompat.from(this);
        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        mNotificationManager.cancelAll();

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        // Create the retriever and start an asynchronous task that will prepare it.
//        mRetriever = new MusicRetriever(getContentResolver());
//        (new PrepareMusicRetrieverTask(mRetriever, this)).execute();
        // create the Audio Focus Helper, if the Audio Focus feature is available (SDK 8 or above)
//        if (android.os.Build.VERSION.SDK_INT >= 8)
        mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);
        if (!LUtils.hasL())
            mPhoneStateHelper = new PhoneStateHelper(getApplicationContext(), this);
//        else
//            mAudioFocus = AudioFocus.Focused; // no focus feature, so we always "have" audio focus
        mDummyAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.main_cover);
        ComponentName mMediaButtonReceiverComponent = new ComponentName(this, MusicIntentReceiver.class);
//        ComponentName mRemoteControlResponder = new ComponentName(getPackageName(),
//                MediaButtonReceiver.class.getName());
//        final Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
//        mediaButtonIntent.setComponent(mRemoteControlResponder);
        // Try to handle the intent as a media button event wrapped by MediaButtonReceiver
//        MediaButtonReceiver.handleIntent(mSession, mediaButtonIntent);
        // Start a new MediaSession
        mSession = new MediaSessionCompat(this, "MusicService", mMediaButtonReceiverComponent, null);
//        mSession = new MediaSessionCompat(this, "MusicService");
//        setSessionToken(mSession.getSessionToken());
//        mSession.setCallback(mPlaybackManager.getMediaSessionCallback());
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mSession.setCallback(mMediaSessionCallback);
        mTransportController = mSession.getController().getTransportControls();
    }

    /**
     * Called when we receive an Intent. When we receive an intent sent to us via startService(),
     * this is the method that gets called. So here we react appropriately depending on the
     * Intent's action, which specifies what is being requested of us.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Log.d(TAG, "onStartCommand: " + action);
        switch (action) {
            case ACTION_TOGGLE_PLAYBACK:
                processTogglePlaybackRequest();
                break;
            case ACTION_PLAY:
                processPlayRequest();
                break;
            case ACTION_PAUSE:
                processPauseRequest();
                break;
            case ACTION_SKIP:
                processSkipRequest();
                break;
            case ACTION_STOP:
                processStopRequest();
                break;
            case ACTION_REWIND:
                processRewindRequest();
                break;
            case ACTION_URL:
                processAddRequest(intent);
                break;
            case ACTION_SEEK:
                processSeekRequest(intent);
                break;
        }
        return START_NOT_STICKY; // Means we started the service, but don't want it to
        // restart in case it's killed.
    }

    void processTogglePlaybackRequest() {
        if (mState != State.Stopped) {
//            if (mState == State.Paused || mState == State.Stopped) {
            if (mState == State.Paused )
                processPlayRequest();
            else
                processPauseRequest();
        }
    }

    void processPlayRequest() {
//        if (mState == State.Retrieving) {
//            // If we are still retrieving media, just set the flag to start playing when we're
//            // ready
//            mWhatToPlayAfterRetrieve = null; // play a random song
//            mStartPlayingAfterRetrieve = true;
//            return;
//        }
        tryToGetAudioFocus();
        // actually play the song
        if (mState == State.Stopped) {
            // If we're stopped, just go ahead to the next song and start playing
            playNextSong(null);
        } else if (mState == State.Paused) {
            // If we're paused, just continue playback and restore the 'foreground service' state.
            mState = State.Playing;
//            setUpAsForeground(mSongTitle + " (playing)");
//            updateNotification();
            Notification mNotification = createNotification("");
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
            configAndStartMediaPlayer();
        }
        // Tell any remote controls that our playback state is 'playing'.
//        if (mRemoteControlClientCompat != null) {
//            mRemoteControlClientCompat
//                    .setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
//        }
        if (mSession != null)
            mSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                    .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_REWIND)
//                    .setActions(PlaybackStateCompat.ACTION_REWIND)
                    .build());

        Log.d(TAG, "Sending broadcast notification: " + BROADCAST_PLAYER_STARTED);
//        Intent intentBroadcast = new Intent(BROADCAST_PLAYER_STARTED);
//        sendBroadcast(intentBroadcast);
        sendBroadcast(new Intent(BROADCAST_PLAYER_STARTED));
    }

    void processPauseRequest() {
//        if (mState == State.Retrieving) {
//            // If we are still retrieving media, clear the flag that indicates we should start
//            // playing when we're ready
//            mStartPlayingAfterRetrieve = false;
//            return;
//        }
        if (mState == State.Playing) {
            // Pause media player and cancel the 'foreground service' state.
            mState = State.Paused;
            mPlayer.pause();
//            updateNotification();
            Notification mNotification = createNotification("");
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
            relaxResources(false); // while paused, we always retain the MediaPlayer
            // do not give up audio focus
        }
        // Tell any remote controls that our playback state is 'paused'.
//        if (mRemoteControlClientCompat != null) {
//            mRemoteControlClientCompat
//                    .setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
//        }

        if (mSession != null)
            mSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0.0f)
                    .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_REWIND)
//                    .setActions(PlaybackStateCompat.ACTION_REWIND)
                    .build());

        Log.d(TAG, "Sending broadcast notification: " + BROADCAST_PLAYBACK_PAUSED);
//        Intent intentBroadcast = new Intent(BROADCAST_PLAYBACK_PAUSED);
//        sendBroadcast(intentBroadcast);
        sendBroadcast(new Intent(BROADCAST_PLAYBACK_PAUSED));
    }

    void processRewindRequest() {
        if (mState == State.Playing || mState == State.Paused)
            mPlayer.seekTo(0);
    }

    void processSkipRequest() {
        if (mState == State.Playing || mState == State.Paused) {
            tryToGetAudioFocus();
            playNextSong(null);
        }
    }

    void processSeekRequest(Intent intent) {
        if (mState == State.Playing || mState == State.Paused)
            mPlayer.seekTo(Integer.parseInt(intent.getData().toString()));
    }

    void processStopRequest() {
        processStopRequest(false);
    }

    void processStopRequest(boolean force) {
        Log.d(TAG, "processStopRequest: force" + force);
        Log.d(TAG, "processStopRequest: mState " + mState);
        if (mState == State.Playing || mState == State.Paused || mState == State.Preparing || force) {
            mState = State.Stopped;
            Log.d(TAG, "processStopRequest: ");
            // let go of all resources...
            relaxResources(true);
            giveUpAudioFocus();
            // Tell any remote controls that our playback state is 'paused'.
//            if (mRemoteControlClientCompat != null) {
//                mRemoteControlClientCompat
//                        .setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
//            }
            Log.d(TAG, "Sending broadcast notification: " + BROADCAST_PLAYBACK_COMPLETED);
//            Intent intentBroadcast = new Intent(BROADCAST_PLAYBACK_COMPLETED);
//            sendBroadcast(intentBroadcast);
            sendBroadcast(new Intent(BROADCAST_PLAYBACK_COMPLETED));
            // service is no longer necessary. Will be started again if needed.
            stopSelf();
        }
    }

    /**
     * Releases resources used by the service for playback. This includes the "foreground service"
     * status and notification, the wake locks and possibly the MediaPlayer.
     *
     * @param releaseMediaPlayer Indicates whether the Media Player should also be released or not
     */
    void relaxResources(boolean releaseMediaPlayer) {
        // stop being a foreground service
        // stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mPlayer != null) {
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }

        stopForeground(releaseMediaPlayer);

        if (releaseMediaPlayer) {
//            stopForeground(true);
            mSession.release();
        }
        // we can also release the Wifi lock, if we're holding it
        if (mWifiLock.isHeld()) mWifiLock.release();
    }

    void giveUpAudioFocus() {
        if (mAudioFocus == AudioFocus.Focused && mAudioFocusHelper != null
                && mAudioFocusHelper.abandonFocus())
            mAudioFocus = AudioFocus.NoFocusNoDuck;
        if (mPhoneStateHelper != null && !LUtils.hasL())
            mPhoneStateHelper.unregister();
    }

    /**
     * Reconfigures MediaPlayer according to audio focus settings and starts/restarts it. This
     * method starts/restarts the MediaPlayer respecting the current audio focus state. So if
     * we have focus, it will play normally; if we don't have focus, it will either leave the
     * MediaPlayer paused or set it to a low volume, depending on what is allowed by the
     * current focus settings. This method assumes mPlayer != null, so if you are calling it,
     * you have to do so from a context where you are sure this is the case.
     */
    void configAndStartMediaPlayer() {
        Log.d(TAG, "configAndStartMediaPlayer");
        if (mAudioFocus == AudioFocus.NoFocusNoDuck) {
            // If we don't have audio focus and can't duck, we have to pause, even if mState
            // is State.Playing. But we stay in the Playing state so that we know we have to resume
            // playback once we get the focus back.
//            if (mPlayer.isPlaying()) mPlayer.pause();
            if (mPlayer.isPlaying())
                processPauseRequest();
            return;
        } else if (mAudioFocus == AudioFocus.NoFocusCanDuck)
            mPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);  // we'll be relatively quiet
        else
            mPlayer.setVolume(1.0f, 1.0f); // we can be loud
        if (!mPlayer.isPlaying()) mPlayer.start();
        if (!mPositionBroadcasterRunning) mPositionBroadcaster.run();
    }

    void processAddRequest(Intent intent) {
        // user wants to play a song directly by URL or path. The URL or path comes in the "data"
        // part of the Intent. This Intent is sent by {@link MainActivity} after the user
        // specifies the URL/path via an alert box.
        mNotificationColor = intent.getIntExtra(DATA_COLOR, ContextCompat.getColor(this, R.color.theme_primary));
        mSongTitle = intent.getStringExtra(DATA_TITLE);
        mLocalFile = intent.getBooleanExtra(DATA_LOCAL, false);
//        if (mState == State.Retrieving) {
//            // we'll play the requested URL right after we finish retrieving
//            mWhatToPlayAfterRetrieve = intent.getData();
//            mStartPlayingAfterRetrieve = true;
//        } else
        if (mState == State.Playing || mState == State.Paused || mState == State.Stopped) {
            Log.i(TAG, "Playing from URL/path: " + intent.getData().toString());
            tryToGetAudioFocus();
            playNextSong(intent.getData().toString());
        }
    }

    void tryToGetAudioFocus() {
        if (mAudioFocus != AudioFocus.Focused && mAudioFocusHelper != null
                && mAudioFocusHelper.requestFocus())
            mAudioFocus = AudioFocus.Focused;
        if (mPhoneStateHelper != null && !LUtils.hasL())
            mPhoneStateHelper.listen();
    }

    /**
     * Starts playing the next song. If manualUrl is null, the next song will be randomly selected
     * from our Media Retriever (that is, it will be a random song in the user's device). If
     * manualUrl is non-null, then it specifies the URL or path to the song that will be played
     * next.
     */
    void playNextSong(String manualUrl) {
        mState = State.Stopped;
        relaxResources(false); // release everything except MediaPlayer
        try {
            playingItem = null;
            if (manualUrl != null) {
                // set the source of the media player to a manual URL or path
                createMediaPlayerIfNeeded();
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                if (mLocalFile) {
                    FileInputStream fileInputStream = new FileInputStream(manualUrl);
                    mPlayer.setDataSource(fileInputStream.getFD());
                    fileInputStream.close();
                } else
                    mPlayer.setDataSource(manualUrl);
//                mIsStreaming = manualUrl.startsWith("http:") || manualUrl.startsWith("https:");
                mIsStreaming = !mLocalFile;
                playingItem = new Item(0, null, mSongTitle, getString(R.string.risuscito_title), 0);
            } else {
                mIsStreaming = false; // playing a locally available song
//                playingItem = mRetriever.getRandomItem();
//                if (playingItem == null) {
////                    Toast.makeText(this,
////                            "No available music to play. Place some music on your external storage "
////                                    + "device (e.g. your SD card) and try again.",
////                            Toast.LENGTH_LONG).show();
//                    Log.d(TAG, "No available music to play. Place some music on your external storage "
//                            + "device (e.g. your SD card) and try again.");
//                    processStopRequest(true); // stop everything!
//                    return;
//                }
//                // set the source of the media player a a content URI
//                createMediaPlayerIfNeeded();
//                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                mPlayer.setDataSource(getApplicationContext(), playingItem.getURI());
                return;
            }
//            mSongTitle = playingItem.getTitle();
            mState = State.Preparing;
//            setUpAsForeground(mSongTitle + " (loading)");
            // Use the media button APIs (if available) to register ourselves for media button
            // events
//            MediaButtonHelper.registerMediaButtonEventReceiverCompat(
//                    mAudioManager, mMediaButtonReceiverComponent);
//            // Use the remote control APIs (if available) to set the playback state
//            if (mRemoteControlClientCompat == null) {
//                Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
//                intent.setComponent(mMediaButtonReceiverComponent);
//                mRemoteControlClientCompat = new RemoteControlClientCompat(
//                        PendingIntent.getBroadcast(this /*context*/,
//                                0 /*requestCode, ignored*/, intent /*intent*/, 0 /*flags*/));
//                RemoteControlHelper.registerRemoteControlClient(mAudioManager,
//                        mRemoteControlClientCompat);
//            }
//            mRemoteControlClientCompat.setPlaybackState(
//                    RemoteControlClient.PLAYSTATE_PLAYING);
//            mRemoteControlClientCompat.setTransportControlFlags(
//                    RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
//                            RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
//                            RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
//                            RemoteControlClient.FLAG_KEY_MEDIA_STOP);
//            // Update the remote controls
//            mRemoteControlClientCompat.editMetadata(true)
//                    .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, playingItem.getArtist())
//                    .putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, playingItem.getAlbum())
//                    .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, playingItem.getTitle())
//                    .putLong(MediaMetadataRetriever.METADATA_KEY_DURATION,
//                            playingItem.getDuration())
//                    .putBitmap(
//                            RemoteControlClientCompat.MetadataEditorCompat.METADATA_KEY_ARTWORK,
//                            mDummyAlbumArt)
//                    .apply();

            mSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0.0f)
//                    .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_REWIND)
//                    .setActions(PlaybackStateCompat.ACTION_REWIND)
                    .build());
            mSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, playingItem.getAlbum())
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, playingItem.getTitle())
//                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mSongTitle)
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                            BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .build());
            mSession.setActive(true);


            Notification mNotification = createNotification(" (loading)");
            startForeground(NOTIFICATION_ID, mNotification);
            // starts preparing the media player in the background. When it's done, it will call
            // our OnPreparedListener (that is, the onPrepared() method on this class, since we set
            // the listener to 'this').
            //
            // Until the media player is prepared, we *cannot* call start() on it!
            mPlayer.prepareAsync();
            // If we are streaming from the internet, we want to hold a Wifi lock, which prevents
            // the Wifi radio from going to sleep while the song is playing. If, on the other hand,
            // we are *not* streaming, we want to release the lock if we were holding it before.
            if (mIsStreaming) mWifiLock.acquire();
            else if (mWifiLock.isHeld()) mWifiLock.release();
        } catch (IOException ex) {
            Log.e("MusicService", "IOException playing next song: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Called when media player is done playing current song.
     */
    public void onCompletion(MediaPlayer player) {
        // The media player finished playing the current song, so we go ahead and start the next.
        Log.d(TAG, "OnCompleted");
        processStopRequest(true);
//        playNextSong(null);
    }

    /**
     * Called when media player is done preparing.
     */
    public void onPrepared(MediaPlayer player) {
        Log.d(TAG, "onPrepared");
        // The media player is done preparing. That means we can start playing!
        mState = State.Playing;

        mSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_REWIND)
//                .setActions(PlaybackStateCompat.ACTION_REWIND)
                .build());
        mSession.setMetadata(new MediaMetadataCompat.Builder()
//                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, playingItem.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, playingItem.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, playingItem.getTitle())
//                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mSongTitle)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mPlayer.getDuration())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                        BitmapFactory.decodeResource(getResources(), R.drawable.main_cover))
                .build());

//        mSession.setActive(true);

//        updateNotification();
        Notification mNotification = createNotification("");
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
//        startForeground(NOTIFICATION_ID, mNotification);

        Log.d(TAG, "Sending broadcast notification: " + BROADCAST_PREPARING_COMPLETED);
        Log.d(TAG, "DURATION SEND: " + mPlayer.getDuration());
        Intent intentBroadcast = new Intent(BROADCAST_PREPARING_COMPLETED);
        intentBroadcast.putExtra(DATA_DURATION, mPlayer.getDuration());
        sendBroadcast(intentBroadcast);
        configAndStartMediaPlayer();
    }
//    /** Updates the notification. */
//    void updateNotification() {
//
//        int icon;
//        String label;
////        Intent i = new Intent(getApplicationContext(),MusicService.class);
////        PendingIntent intent;
//        if (mState == State.Playing) {
//            icon = R.drawable.ic_pause_48dp;
//            label = "Pause";
////            intent = PendingIntent.getBroadcast(this, 9876,
////                    new Intent(ACTION_PAUSE).setPackage(getPackageName()), PendingIntent.FLAG_CANCEL_CURRENT);
////            intent = getActionIntent(this, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
////            i.setAction(MusicService.ACTION_PAUSE);
//        } else {
//            icon = R.drawable.ic_play_arrow_48dp;
//            label = "Play";
////            i.setAction(MusicService.ACTION_PLAY);
////            intent = PendingIntent.getBroadcast(this, 9876,
////                    new Intent(ACTION_PLAY).setPackage(getPackageName()), PendingIntent.FLAG_CANCEL_CURRENT);
////            intent = getActionIntent(this, KeyEvent.KEYCODE_MEDIA_PLAY);
//        }
//
//        int duration = mPlayer.getDuration();
//        int seconds = duration / 1000 % 60;
//        Log.d(getClass().getName(), "seconds: " + seconds);
//        int minutes = (duration / (1000 * 60));
//        Log.d(getClass().getName(), "minutes: " + minutes);
//        @SuppressLint("DefaultLocale")
//        String durationStr = String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
//
////        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
//
//        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(this);
//        mNotificationBuilder
//                .setStyle(new NotificationCompat.MediaStyle()
//                        .setShowActionsInCompactView(0)  // show only play/pause in compact view
//                        .setMediaSession(mSession.getSessionToken())
//                        .setShowCancelButton(true)
//                        .setCancelButtonIntent(getActionIntent(this, KeyEvent.KEYCODE_MEDIA_STOP)))
//                .setColor(mNotificationColor)
//                .setSmallIcon(mState == State.Playing ? R.drawable.ic_play_circle_outline_48dp : R.drawable.ic_pause_circle_outline_48dp)
//                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
////                .setUsesChronometer(true)
////                .setContentIntent(createContentIntent(description))
//                .setContentTitle(mSongTitle)
//                .setContentText(durationStr)
//                .addAction(new NotificationCompat.Action(icon, label, getActionIntent(this, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)))
//                .setLargeIcon(mDummyAlbumArt)
//                .setOngoing(mState == State.Playing);
//
//        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
//    }
//    /**
//     * Configures service as a foreground service. A foreground service is a service that's doing
//     * something the user is actively aware of (such as playing music), and must appear to the
//     * user as a notification. That's why we create the notification here.
//     */
//    void setUpAsForeground(String text) {
////        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
////                new Intent(getApplicationContext(), PaginaRenderActivity.class),
////                PendingIntent.FLAG_UPDATE_CURRENT);
//        // Build the notification object.
////        mNotificationBuilder = new NotificationCompat.Builder(getApplicationContext())
////                .setSmallIcon(R.drawable.ic_play_arrow_48dp)
////                .setTicker(text)
////                .setWhen(System.currentTimeMillis())
////                .setContentTitle("RandomMusicPlayer")
////                .setContentText(text)
////                .setContentIntent(pi)
////                .setOngoing(true);
//
//        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(this);
//        mNotificationBuilder
//                .setStyle(new NotificationCompat.MediaStyle()
////                        .setShowActionsInCompactView(0)  // show only play/pause in compact view
//                        .setMediaSession(mSession.getSessionToken())
//                        .setShowCancelButton(true)
//                        .setCancelButtonIntent(getActionIntent(this, KeyEvent.KEYCODE_MEDIA_STOP)))
//                .setColor(mNotificationColor)
//                .setSmallIcon(R.drawable.ic_pause_circle_outline_48dp)
//                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
////                .setUsesChronometer(true)
////                .setContentIntent(createContentIntent(description))
//                .setContentTitle(text)
//                .setContentText("00:00")
//                .setLargeIcon(mDummyAlbumArt)
//                .setOngoing(mState == State.Playing);
//
//        startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
//    }

    /**
     * Creates the notification.
     */
    private Notification createNotification(String text) {

        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(this);
        mNotificationBuilder
                .setColor(mNotificationColor)
//                .setSmallIcon(mState == State.Playing ? R.drawable.ic_play_circle_outline_white_24dp : R.drawable.ic_pause_circle_outline_white_24dp)
                .setSmallIcon(mState == State.Playing ? android.R.drawable.ic_media_play : android.R.drawable.ic_media_pause)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(playingItem.getTitle() + text)
                .setLargeIcon(mDummyAlbumArt)
                .setContentIntent(createContentIntent())
                .setShowWhen(false);

        addRestartAction(mNotificationBuilder);

        addPlayPauseAction(mNotificationBuilder);

        setNotificationPlaybackState(mNotificationBuilder);

        return mNotificationBuilder.build();
    }

    private void setNotificationPlaybackState(NotificationCompat.Builder builder) {
        switch (mState) {
            case Playing:
            case Paused:
                int duration = mPlayer.getDuration();
//                int seconds = duration / 1000 % 60;
//                Log.d(getClass().getName(), "seconds: " + seconds);
//                int minutes = (duration / (1000 * 60));
//                Log.d(getClass().getName(), "minutes: " + minutes);
//                String durationStr = String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
                String durationStr = String.format(ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration()), "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(duration),
                        TimeUnit.MILLISECONDS.toSeconds(duration) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
                );

                builder.setStyle(new NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0,1)  // show only play/pause in compact view
                        .setMediaSession(mSession.getSessionToken())
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(getActionIntent(this, KeyEvent.KEYCODE_MEDIA_STOP)))
                        .setContentText(durationStr);
                break;
            case Preparing:
                builder.setProgress(0, 0, true);
                break;
            default:
                builder.setStyle(new NotificationCompat.MediaStyle()
                        .setMediaSession(mSession.getSessionToken())
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(getActionIntent(this, KeyEvent.KEYCODE_MEDIA_STOP)));
                break;
        }
        // Make sure that the notification can be dismissed by the user when we are not playing:
        builder.setOngoing(mState == State.Playing);
    }

    private void addPlayPauseAction(NotificationCompat.Builder builder) {
        int icon;
        String label;
        switch (mState) {
            case Playing:
                icon = R.drawable.notification_pause;
                label = "Pause";
                builder.addAction(new NotificationCompat.Action(icon, label, getActionIntent(this, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)));
                break;
            case Paused:
                icon = R.drawable.notification_play;
                label = "Play";
                builder.addAction(new NotificationCompat.Action(icon, label, getActionIntent(this, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)));
                break;
            default:
                break;
        }
    }

    private void addRestartAction(NotificationCompat.Builder builder) {
        switch (mState) {
            case Playing:
            case Paused:
                builder.addAction(new NotificationCompat.Action(
                        R.drawable.notification_restart
                        , "Restart"
                        , getActionIntent(this, KeyEvent.KEYCODE_MEDIA_PREVIOUS)));
                break;
            default:
                break;
        }
    }

    /**
     * Called when there's an error playing media. When this happens, the media player goes to
     * the Error state. We warn the user about the error and reset the media player.
     */
    public boolean onError(MediaPlayer mp, int what, int extra) {
//        Toast.makeText(getApplicationContext(), "Media player error! Resetting.",
//                Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Media player error! Resetting.");
        Log.e(TAG, "Error: what=" + String.valueOf(what) + ", extra=" + String.valueOf(extra));
//        mState = State.Stopped;
//        relaxResources(true);
//        giveUpAudioFocus();
        processStopRequest(true);
        return true; // true indicates we handled the error
    }

    public void onGainedAudioFocus() {
//        Toast.makeText(getApplicationContext(), "gained audio focus.", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onGainedAudioFocus");
        mAudioFocus = AudioFocus.Focused;
        // restart media player with new focus settings
        if (mState == State.Playing)
            configAndStartMediaPlayer();
    }

    public void onLostAudioFocus(boolean canDuck) {
//        Toast.makeText(getApplicationContext(), "lost audio focus." + (canDuck ? "can duck" :
//                "no duck"), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "lost audio focus." + (canDuck ? "can duck" : "no duck"));
        mAudioFocus = canDuck ? AudioFocus.NoFocusCanDuck : AudioFocus.NoFocusNoDuck;
        // start/restart/pause media player with new focus settings
        if (mPlayer != null && mPlayer.isPlaying())
            configAndStartMediaPlayer();
    }

    @Override
    public void onPhoneRinging() {
        Log.d(TAG, "onPhoneRinging");
        if (mPlayer != null && mPlayer.isPlaying())
            processPauseRequest();
    }

    @Override
    public void onDialing() {
        Log.d(TAG, "onDialing");
        if (mPlayer != null && mPlayer.isPlaying())
            processPauseRequest();
    }

    public void onMusicRetrieverPrepared() {
        // Done retrieving!
        mState = State.Stopped;
        // If the flag indicates we should start playing after retrieving, let's do that now.
        if (mStartPlayingAfterRetrieve) {
            tryToGetAudioFocus();
            playNextSong(mWhatToPlayAfterRetrieve == null ?
                    null : mWhatToPlayAfterRetrieve.toString());
        }
    }

    @Override
    public void onDestroy() {
        // Service is being killed, so make sure we release our resources
        mState = State.Stopped;
        relaxResources(true);
        giveUpAudioFocus();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

//    private class MediaSessionCallback extends MediaSessionCompat.Callback {
//        @Override
//        public void onPlay() {
//            LogHelper.d(TAG, "play");
//            if (mQueueManager.getCurrentMusic() == null) {
//                mQueueManager.setRandomQueue();
//            }
//            handlePlayRequest();
//        }
//
//        @Override
//        public void onSkipToQueueItem(long queueId) {
//            LogHelper.d(TAG, "OnSkipToQueueItem:" + queueId);
//            mQueueManager.setCurrentQueueItem(queueId);
//            handlePlayRequest();
//            mQueueManager.updateMetadata();
//        }
//
//        @Override
//        public void onSeekTo(long position) {
//            LogHelper.d(TAG, "onSeekTo:", position);
//            mPlayback.seekTo((int) position);
//        }
//
//        @Override
//        public void onPlayFromMediaId(String mediaId, Bundle extras) {
//            LogHelper.d(TAG, "playFromMediaId mediaId:", mediaId, "  extras=", extras);
//            mQueueManager.setQueueFromMusic(mediaId);
//            handlePlayRequest();
//        }
//
//        @Override
//        public void onPause() {
//            LogHelper.d(TAG, "pause. current state=" + mPlayback.getState());
//            handlePauseRequest();
//        }
//
//        @Override
//        public void onStop() {
//            LogHelper.d(TAG, "stop. current state=" + mPlayback.getState());
//            handleStopRequest(null);
//        }
//
//        @Override
//        public void onSkipToNext() {
//            LogHelper.d(TAG, "skipToNext");
//            if (mQueueManager.skipQueuePosition(1)) {
//                handlePlayRequest();
//            } else {
//                handleStopRequest("Cannot skip");
//            }
//            mQueueManager.updateMetadata();
//        }
//
//        @Override
//        public void onSkipToPrevious() {
//            if (mQueueManager.skipQueuePosition(-1)) {
//                handlePlayRequest();
//            } else {
//                handleStopRequest("Cannot skip");
//            }
//            mQueueManager.updateMetadata();
//        }
//
//        @Override
//        public void onCustomAction(@NonNull String action, Bundle extras) {
//            if (CUSTOM_ACTION_THUMBS_UP.equals(action)) {
//                LogHelper.i(TAG, "onCustomAction: favorite for current track");
//                MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
//                if (currentMusic != null) {
//                    String mediaId = currentMusic.getDescription().getMediaId();
//                    if (mediaId != null) {
//                        String musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId);
//                        mMusicProvider.setFavorite(musicId, !mMusicProvider.isFavorite(musicId));
//                    }
//                }
//                // playback state needs to be updated because the "Favorite" icon on the
//                // custom action will change to reflect the new favorite state.
//                updatePlaybackState(null);
//            } else {
//                LogHelper.e(TAG, "Unsupported action: ", action);
//            }
//        }
//
//        /**
//         * Handle free and contextual searches.
//         * <p/>
//         * All voice searches on Android Auto are sent to this method through a connected
//         * {@link android.support.v4.media.session.MediaControllerCompat}.
//         * <p/>
//         * Threads and async handling:
//         * Search, as a potentially slow operation, should run in another thread.
//         * <p/>
//         * Since this method runs on the main thread, most apps with non-trivial metadata
//         * should defer the actual search to another thread (for example, by using
//         * an {@link AsyncTask} as we do here).
//         **/
//        @Override
//        public void onPlayFromSearch(final String query, final Bundle extras) {
//            LogHelper.d(TAG, "playFromSearch  query=", query, " extras=", extras);
//
//            mPlayback.setState(PlaybackStateCompat.STATE_CONNECTING);
//            mQueueManager.setQueueFromSearch(query, extras);
//            handlePlayRequest();
//            mQueueManager.updateMetadata();
//        }
//    }

    private PendingIntent createContentIntent() {
        Intent openUI = new Intent(this, PaginaRenderActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        openUI.putExtra(MusicPlayerActivity.EXTRA_START_FULLSCREEN, true);
//        if (description != null) {
//            openUI.putExtra(MusicPlayerActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION, description);
//        }
        return PendingIntent.getActivity(this, 9876, openUI,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    /**
     * Create a {@link PendingIntent} appropriate for a MediaStyle notification's action. Assumes
     * you are using a media button receiver.
     *
     * @param context       Context used to contruct the pending intent.
     * @param mediaKeyEvent KeyEvent code to send to your media button receiver.
     * @return An appropriate pending intent for sending a media button to your media button
     * receiver.
     */
    public static PendingIntent getActionIntent(
            Context context, int mediaKeyEvent) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.setPackage(context.getPackageName());
        intent.putExtra(Intent.EXTRA_KEY_EVENT,
                new KeyEvent(KeyEvent.ACTION_DOWN, mediaKeyEvent));
        return PendingIntent.getBroadcast(context, mediaKeyEvent, intent, 0);
    }

    private final MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            final String intentAction = mediaButtonEvent.getAction();
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intentAction)) {
//                Toast.makeText(MusicService.this, "Headphones disconnected.", Toast.LENGTH_SHORT).show();
                mTransportController.pause();
            } else if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
                final KeyEvent event = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (event == null) return super.onMediaButtonEvent(mediaButtonEvent);
                final int keycode = event.getKeyCode();
                final int action = event.getAction();
                if (event.getRepeatCount() == 0 && action == KeyEvent.ACTION_DOWN) {
                    Log.d(TAG, "onMediaButtonEvent: keycode " + keycode);
                    switch (keycode) {
                        case KeyEvent.KEYCODE_HEADSETHOOK:
                        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                            if (mState != State.Stopped) {
//                                if (mState == State.Paused || mState == State.Stopped)
                                if (mState == State.Paused)
                                    mTransportController.play();
                                else
                                    mTransportController.pause();
                            }
                            break;
                        case KeyEvent.KEYCODE_MEDIA_STOP:
                            mTransportController.stop();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_NEXT:
                            Toast.makeText(MusicService.this, "Not supported!", Toast.LENGTH_SHORT).show();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                            mTransportController.skipToPrevious();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PAUSE:
                            mTransportController.pause();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PLAY:
                            mTransportController.play();
                            break;
                    }
                }
            }
            return super.onMediaButtonEvent(mediaButtonEvent);
        }

        @Override
        public void onPlay() {
            Log.d(TAG, "onPause: ");
            super.onPlay();
            processPlayRequest();
        }

        @Override
        public void onPause() {
            Log.d(TAG, "onPause: ");
            super.onPause();
            processPauseRequest();
        }

        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            processRewindRequest();
        }

        @Override
        public void onStop() {
            super.onStop();
            processStopRequest();
        }

    };

    public static class Item {
        long id;
        String artist;
        String title;
        String album;
        long duration;
        public Item(long id, String artist, String title, String album, long duration) {
            this.id = id;
            this.artist = artist;
            this.title = title;
            this.album = album;
            this.duration = duration;
        }
        public long getId() {
            return id;
        }
        public String getArtist() {
            return artist;
        }
        public String getTitle() {
            return title;
        }
        public String getAlbum() {
            return album;
        }
        public long getDuration() {
            return duration;
        }
//        public Uri getURI() {
//            return ContentUris.withAppendedId(
//                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
//        }
    }
}