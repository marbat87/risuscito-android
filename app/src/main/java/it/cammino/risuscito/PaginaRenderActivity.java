package it.cammino.risuscito;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.folderselector.FileChooserDialog;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.services.DownloadService;
import it.cammino.risuscito.services.PdfExportService;
import it.cammino.risuscito.ui.BottomSheetFabCanto;
import it.cammino.risuscito.ui.BottomSheetFabListe;
import it.cammino.risuscito.ui.MediaBrowserProvider;
import it.cammino.risuscito.ui.ThemeableActivity;
import it.cammino.risuscito.utils.LogHelper;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class PaginaRenderActivity extends ThemeableActivity implements SimpleDialogFragment.SimpleCallback, FileChooserDialog.FileCallback, EasyPermissions.PermissionCallbacks, MediaBrowserProvider {

    //    final String TAG = getClass().getCanonicalName();
    private static final String TAG = LogHelper.makeLogTag(PaginaRenderActivity.class);

    private DatabaseCanti listaCanti;
    private String pagina;
    private int idCanto;
    //    private String titoloCanto;
    private String url;
    private String primaNota;
    private String notaSalvata;
    public static String notaCambio;
    private String primoBarre;
    private String barreSalvato;
    private static String barreCambio;
    //    private String personalUrl, localUrl,  playUrl;
    private String personalUrl, localUrl;

    private enum MP_State {Started, Stopped}
    private PlaybackStateCompat mLastPlaybackState;
    private ScheduledFuture<?> mScheduleFuture;

    private final ScheduledExecutorService mExecutorService =
            Executors.newSingleThreadScheduledExecutor();

    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;

    MP_State mediaPlayerState = MP_State.Stopped;

    private boolean localFile;

    private int defaultZoomLevel = 0;
    private int defaultScrollX = 0;
    private int defaultScrollY = 0;

    private Handler mHandler = new Handler();
    final Runnable mScrollDown = new Runnable() {
        public void run() {
            if (paginaView != null && speedValue != null) {
                try {
                    paginaView.scrollBy(0, Integer.valueOf(speedValue));
                } catch (NumberFormatException e) {
                    paginaView.scrollBy(0, 0);
                }
                mHandler.postDelayed(this, 700);
            }
            else
//                Log.d(TAG, "attività chiusa o annullato lo scroll");
                LogHelper.d(TAG, "attività chiusa o annullato lo scroll");
        }
    };

    public static String speedValue;
    private int savedSpeed;
    public static boolean scrollPlaying;

    private LUtils mLUtils;

    public static String mostraAudio;
    public boolean mostraAudioBool;
    public boolean mDownload;

    private MediaBrowserCompat mMediaBrowser;

    public final CambioAccordi cambioAccordi = new CambioAccordi(this);

//    private BroadcastReceiver gpsBRec = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            //Implement UI change code here once notification is received
//            try {
//                Log.d(TAG, "BROADCAST_PREPARING_COMPLETED");
//                Log.d(TAG, "DURATION RECEIVED: " + intent.getIntExtra(MusicService.DATA_DURATION, 0));
//                scroll_song_bar.setMax(intent.getIntExtra(MusicService.DATA_DURATION, 0));
//                scroll_song_bar.setEnabled(true);
//                SimpleDialogFragment sFragment = SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "BUFFERING");
//                if (sFragment != null)
//                    sFragment.dismiss();
//            }
//            catch (IllegalArgumentException e) {
//                Log.e(TAG, e.getLocalizedMessage(), e);
//            }
//        }
//    };

//    private BroadcastReceiver stopBRec = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            //Implement UI change code here once notification is received
//            Log.d(TAG, "BROADCAST_PLAYBACK_COMPLETED");
//            scroll_song_bar.setProgress(0);
//            scroll_song_bar.setEnabled(false);
//            showPlaying(false);
//            mediaPlayerState = MP_State.Stopped;
//        }
//    };

//    private BroadcastReceiver positionBRecc = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            //Implement UI change code here once notification is received
//            Log.d(TAG, "BROADCAST_PLAYER_POSITION");
//            try {
//                Log.d(TAG, "POSITION RECEIVED: " + intent.getIntExtra(MusicService.DATA_POSITION, 0));
//                scroll_song_bar.setProgress(intent.getIntExtra(MusicService.DATA_POSITION, 0));
//                scroll_song_bar.setEnabled(true);
//            }
//            catch (IllegalArgumentException e) {
//                Log.e(TAG, e.getLocalizedMessage(), e);
//            }
//        }
//    };

//    private BroadcastReceiver playBRec = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            //Implement UI change code here once notification is received
//            Log.d(TAG, "BROADCAST_PLAYER_POSITION");
//            showPlaying(true);
//            mediaPlayerState = MP_State.Started;
//            scroll_song_bar.setEnabled(true);
//        }
//    };

//    private BroadcastReceiver pauseBRec = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            //Implement UI change code here once notification is received
//            Log.d(TAG, "BROADCAST_PLAYBACK_PAUSED");
//            showPlaying(false);
//            scroll_song_bar.setEnabled(true);
//        }
//    };

    private BroadcastReceiver downloadPosBRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Implement UI change code here once notification is received
            try {
//                Log.d(TAG, "BROADCAST_DOWNLOAD_PROGRESS");
//                Log.d(TAG, "DATA_PROGRESS: " + intent.getIntExtra(DownloadService.DATA_PROGRESS, 0));
                LogHelper.i(TAG, "BROADCAST_DOWNLOAD_PROGRESS", "DATA_PROGRESS", intent.getIntExtra(DownloadService.DATA_PROGRESS, 0));
                SimpleDialogFragment sFragment = SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "DOWNLOAD_MP3");
                if (sFragment != null) {
                    sFragment.setProgress(intent.getIntExtra(DownloadService.DATA_PROGRESS, 0));
                }
            }
            catch (IllegalArgumentException e) {
//                Log.e(TAG, e.getLocalizedMessage(), e);
                LogHelper.e(TAG, e.getLocalizedMessage(), e);
            }
        }
    };

    private BroadcastReceiver downloadCompletedBRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Implement UI change code here once notification is received
            try {
//                Log.d(TAG, "BROADCAST_DOWNLOAD_COMPLETED");
                LogHelper.i(TAG, "BROADCAST_DOWNLOAD_COMPLETED");
                SimpleDialogFragment sFragment = SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "DOWNLOAD_MP3");
                if (sFragment != null)
                    sFragment.dismiss();
                SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(PaginaRenderActivity.this);
                int saveLocation = Integer.parseInt(pref.getString(Utility.SAVE_LOCATION, "0"));
                if (saveLocation == 1) {
                    // initiate media scan and put the new things into the path array to
                    // make the scanner aware of the location and the files you want to see
                    MediaScannerConnection.scanFile(context
                            , new String[] {Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_MUSIC).getAbsolutePath()
                                    + "/Risuscitò/" + Utility.filterMediaLinkNew(url)}
                            , null
                            , null);
                }
                Snackbar.make(findViewById(android.R.id.content),
                        R.string.download_completed
                        , Snackbar.LENGTH_SHORT)
                        .show();

                scroll_song_bar.setProgress(0);
                scroll_song_bar.setEnabled(false);
                showPlaying(false);
//                if (mediaPlayerState != MP_State.Stopped) {
//                    mediaPlayerState = MP_State.Stopped;
//                    Intent i = new Intent(getApplicationContext(), MusicService.class);
//                    stopService(i);
////                    i.setAction(MusicService.ACTION_STOP);
////                    startService(i);
////                    ContextCompat.startForegroundService(PaginaRenderActivity.this, i);
//                }
                MediaControllerCompat controller = MediaControllerCompat.getMediaController(PaginaRenderActivity.this);
                if (controller != null) {
                    controller.getTransportControls().stop();
                }

                checkExternalFilePermissions();
                localFile = true;
//                playUrl = localUrl;
                mDownload = true;
                recreate();
            }
            catch (IllegalArgumentException e) {
//                Log.e(TAG, e.getLocalizedMessage(), e);
                LogHelper.e(TAG, e.getLocalizedMessage(), e);
            }
        }
    };

    private BroadcastReceiver downloadErrorBRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Implement UI change code here once notification is received
            try {
//                Log.d(TAG, "BROADCAST_DOWNLOAD_ERROR");
//                Log.d(TAG, "DATA_ERROR: " + intent.getStringExtra(DownloadService.DATA_ERROR));
                LogHelper.i(TAG, "BROADCAST_DOWNLOAD_ERROR", "DATA_ERROR: ", intent.getStringExtra(DownloadService.DATA_ERROR));
                SimpleDialogFragment sFragment = SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "DOWNLOAD_MP3");
                if (sFragment != null)
                    sFragment.dismiss();
                Snackbar.make(findViewById(android.R.id.content)
                        , getString(R.string.download_error) + " " + intent.getStringExtra(DownloadService.DATA_ERROR)
                        , Snackbar.LENGTH_SHORT)
                        .show();
            }
            catch (IllegalArgumentException e) {
//                Log.e(TAG, e.getLocalizedMessage(), e);
                LogHelper.e(TAG, e.getLocalizedMessage(), e);
            }
        }
    };

    private BroadcastReceiver exportCompleted = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Implement UI change code here once notification is received
//            Log.d(TAG, "BROADCAST_EXPORT_COMPLETED");
//            Log.d(TAG, "DATA_PDF_PATH: " + intent.getStringExtra(PdfExportService.DATA_PDF_PATH));
            LogHelper.i(TAG, "BROADCAST_EXPORT_COMPLETED", "DATA_PDF_PATH: ", intent.getStringExtra(PdfExportService.DATA_PDF_PATH));
            SimpleDialogFragment sFragment = SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "EXPORT_PDF");
            if (sFragment != null)
                sFragment.dismiss();
            String localPDFPath = intent.getStringExtra(PdfExportService.DATA_PDF_PATH);
            File file = new File(localPDFPath);
            Intent target = new Intent(Intent.ACTION_VIEW);
//            target.setDataAndType(Uri.fromFile(file), "application/pdf");
            Uri pdfUri = FileProvider.getUriForFile(PaginaRenderActivity.this, "it.cammino.risuscito.fileprovider", file);
//            Log.d(TAG, "pdfUri: " + pdfUri);
            LogHelper.i(TAG, "pdfUri: ", pdfUri);
            target.setDataAndType(pdfUri, "application/pdf");
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent intent2 = Intent.createChooser(target, getString(R.string.open_pdf));
            try {
                startActivity(intent2);
            } catch (ActivityNotFoundException e) {
                Snackbar.make(findViewById(android.R.id.content)
                        , R.string.no_pdf_reader
                        , Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    };

    private BroadcastReceiver exportError = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Implement UI change code here once notification is received
            try {
//                Log.d(TAG, "BROADCAST_EXPORT_ERROR");
//                Log.d(TAG, "DATA_EXPORT_ERROR: " + intent.getStringExtra(PdfExportService.DATA_EXPORT_ERROR));
                LogHelper.i(TAG, "BROADCAST_EXPORT_ERROR", "DATA_EXPORT_ERROR: ",intent.getStringExtra(PdfExportService.DATA_EXPORT_ERROR) );
                SimpleDialogFragment sFragment = SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "EXPORT_PDF");
                if (sFragment != null)
                    sFragment.dismiss();
                Snackbar.make(findViewById(android.R.id.content)
                        , intent.getStringExtra(PdfExportService.DATA_EXPORT_ERROR)
                        , Snackbar.LENGTH_SHORT)
                        .show();
            }
            catch (IllegalArgumentException e) {
//                Log.e(TAG, e.getLocalizedMessage(), e);
                LogHelper.e(TAG, e.getLocalizedMessage(), e);
            }
        }
    };

    private BroadcastReceiver fabBRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Implement UI change code here once notification is received
            int clickedId = intent.getIntExtra(BottomSheetFabListe.DATA_ITEM_ID, 0);
            switch (clickedId) {
                case BottomSheetFabCanto.FULLSCREEN:
                    mHandler.removeCallbacks(mScrollDown);
                    saveZoom();
                    Bundle bundle = new Bundle();
                    bundle.putString(Utility.URL_CANTO, paginaView.getUrl());
                    bundle.putInt(Utility.SPEED_VALUE, scroll_speed_bar.getProgress());
                    bundle.putBoolean(Utility.SCROLL_PLAYING, scrollPlaying);
                    bundle.putInt(Utility.ID_CANTO, idCanto);

                    Intent intent2 = new Intent(PaginaRenderActivity.this, PaginaRenderFullScreen.class);
                    intent2.putExtras(bundle);
                    mLUtils.startActivityWithFadeIn(intent2);
                    break;
                case BottomSheetFabCanto.SOUND:
                    findViewById(R.id.music_controls).setVisibility(mostraAudioBool ? View.GONE : View.VISIBLE);
                    mostraAudioBool = !mostraAudioBool;
                    mostraAudio = String.valueOf(mostraAudioBool);
                    break;
                case BottomSheetFabCanto.SAVE_FILE:
                    if (!url.equalsIgnoreCase("")) {
                        if (mDownload) {
                            if (personalUrl.equalsIgnoreCase("")) {
                                new SimpleDialogFragment.Builder(PaginaRenderActivity.this, PaginaRenderActivity.this, "DELETE_MP3")
                                        .title(R.string.dialog_delete_mp3_title)
                                        .content(R.string.dialog_delete_mp3)
                                        .positiveButton(R.string.confirm)
                                        .negativeButton(R.string.dismiss)
                                        .show();
                            }
                            else {
                                new SimpleDialogFragment.Builder(PaginaRenderActivity.this, PaginaRenderActivity.this, "DELETE_LINK")
                                        .title(R.string.dialog_delete_link_title)
                                        .content(R.string.dialog_delete_link)
                                        .positiveButton(R.string.confirm)
                                        .negativeButton(R.string.dismiss)
                                        .show();
                            }
                        }
                        else {
                            new SimpleDialogFragment.Builder(PaginaRenderActivity.this, PaginaRenderActivity.this, "DOWNLINK_CHOOSE")
                                    .title(R.string.download_link_title)
                                    .content(R.string.downlink_message)
                                    .positiveButton(R.string.downlink_download)
                                    .negativeButton(R.string.downlink_choose)
                                    .neutralButton(R.string.cancel)
                                    .show();
                        }
                    }
                    else {
                        if (mDownload) {
                            new SimpleDialogFragment.Builder(PaginaRenderActivity.this, PaginaRenderActivity.this, "DELETE_LINK_2")
                                    .title(R.string.dialog_delete_link_title)
                                    .content(R.string.dialog_delete_link)
                                    .positiveButton(R.string.confirm)
                                    .negativeButton(R.string.dismiss)
                                    .show();
                        }
                        else {
                            new SimpleDialogFragment.Builder(PaginaRenderActivity.this, PaginaRenderActivity.this, "ONLY_LINK")
                                    .title(R.string.only_link_title)
                                    .content(R.string.only_link)
                                    .positiveButton(R.string.confirm)
                                    .negativeButton(R.string.dismiss)
                                    .show();
                        }
                    }
                    break;
                case BottomSheetFabCanto.FAVORITE:
                    boolean favoriteYet = selectFavouriteFromSource() == 1;
                    updateFavouriteFlag(favoriteYet? 0: 1);
                    Snackbar.make(findViewById(android.R.id.content)
                            , !favoriteYet ? R.string.favorite_added : R.string.favorite_removed
                            , Snackbar.LENGTH_SHORT)
                            .show();
                    break;
                default:
                    break;
            }
        }
    };

    @BindView(R.id.risuscito_toolbar) Toolbar mToolbar;
    @BindView(R.id.cantoView) WebView paginaView;
    @BindView(R.id.play_song) ImageButton play_button;
    @BindView(R.id.no_record) TextView no_records_text;
    @BindView(R.id.music_buttons) View music_buttons;
    @BindView(R.id.play_scroll) ImageButton play_scroll;
    @BindView(R.id.speed_seekbar) SeekBar scroll_speed_bar;
    @BindView(R.id.music_seekbar) SeekBar scroll_song_bar;
    @BindView(R.id.fab_canti) FloatingActionButton mFab;

    @OnClick(R.id.play_song)
    public void playPause() {
//        if (isPlaying()) {
//            showPlaying(false);
//            Intent i = new Intent(getApplicationContext(),MusicService.class);
//            i.setAction(MusicService.ACTION_PAUSE);
////            startService(i);
//            ContextCompat.startForegroundService(PaginaRenderActivity.this, i);
//        }
//        else {
        //controlla la presenza di una connessione internet
        if (!Utility.isOnline(PaginaRenderActivity.this)
                && !localFile) {
            Snackbar.make(findViewById(android.R.id.content)
                    , R.string.no_connection
                    , Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }

        MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
        PlaybackStateCompat stateObj = controller.getPlaybackState();
        final int state = stateObj == null ?
                PlaybackStateCompat.STATE_NONE : stateObj.getState();
//        Log.d(TAG, "playPause: Button pressed, in state " + state);
        LogHelper.i(TAG, "playPause: Button pressed, in state: " + state);

        if (state == PlaybackStateCompat.STATE_STOPPED ||
                state == PlaybackStateCompat.STATE_NONE) {
//            playUri(Uri.parse(playUrl));
            playFromId(String.valueOf(idCanto));
//            playMedia();
        } else if (state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_BUFFERING ||
                state == PlaybackStateCompat.STATE_CONNECTING) {
            pauseMedia();
        } else if (state == PlaybackStateCompat.STATE_PAUSED) {
            playMedia();
        }

//                .playFromMediaId(item.getMediaId(), null);

//            showPlaying(true);
//
//            Log.d(TAG, "mediaPlayerState" + mediaPlayerState);
//
//            if (mediaPlayerState == MP_State.Stopped) {
//                // Send an intent with the URL of the song to play. This is expected by
//                // MusicService.
//                mediaPlayerState = MP_State.Started;
//                Intent i = new Intent(getApplicationContext(), MusicService.class);
//                i.setAction(MusicService.ACTION_URL);
//                Uri uri = Uri.parse(playUrl);
//                i.setData(uri);
//                i.putExtra(MusicService.DATA_LOCAL, localFile);
//                i.putExtra(MusicService.DATA_COLOR, getThemeUtils().primaryColorDark());
//                i.putExtra(MusicService.DATA_TITLE, titoloCanto);
////                startService(i);
//                ContextCompat.startForegroundService(PaginaRenderActivity.this, i);
//                new SimpleDialogFragment.Builder(PaginaRenderActivity.this, PaginaRenderActivity.this, "BUFFERING")
//                        .content(R.string.wait)
//                        .showProgress()
//                        .progressIndeterminate(true)
//                        .progressMax(0)
//                        .show()
//                        .setCancelable(true);
//            } else {
//                Intent i = new Intent(getApplicationContext(), MusicService.class);
//                i.setAction(MusicService.ACTION_PLAY);
////                startService(i);
//                ContextCompat.startForegroundService(PaginaRenderActivity.this, i);
//            }
//        }
    }

    @OnClick(R.id.play_scroll)
    public void playPauseScroll(View v) {
        if (v.isSelected()) {
            showScrolling(false);
            scrollPlaying = false;
            mHandler.removeCallbacks(mScrollDown);
        } else {
            showScrolling(true);
            scrollPlaying = true;
            mScrollDown.run();
        }
    }
    @OnClick(R.id.fab_canti)
    public void FabOptions() {
        BottomSheetFabCanto bottomSheetDialog = BottomSheetFabCanto.newInstance(mostraAudioBool
                , mDownload, selectFavouriteFromSource() == 1
                , !url.equals("")
                , !personalUrl.equals(""));
        bottomSheetDialog.show(getSupportFragmentManager(), null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mMediaBrowser != null) {
            mMediaBrowser.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (MediaControllerCompat.getMediaController(this) != null)
            MediaControllerCompat.getMediaController(PaginaRenderActivity.this).getTransportControls().stop();
        if (mMediaBrowser != null) {
            mMediaBrowser.disconnect();
        }
        if (MediaControllerCompat.getMediaController(this) != null)
            MediaControllerCompat.getMediaController(this).unregisterCallback(mMediaControllerCallback);
    }

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    LogHelper.d(TAG, "onConnected");
                    try {
                        //                        connectToSession(mMediaBrowser.getSessionToken());
                        MediaControllerCompat mediaController =
                                new MediaControllerCompat(
                                        PaginaRenderActivity.this, mMediaBrowser.getSessionToken());
                        MediaControllerCompat.setMediaController(PaginaRenderActivity.this, mediaController);
                        mediaController.registerCallback(mMediaControllerCallback);
                    } catch (RemoteException e) {
                        LogHelper.e(TAG, e, "could not connect media controller");
//                        Log.e(TAG, "onConnected: could not connect media controller", e);
                        //                        hidePlaybackControls();
                    }
                }
            };

    // Callback that ensures that we are showing the controls
    private final MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
//                    Log.d(TAG, "onPlaybackStateChanged: a " + state.getState());
                    LogHelper.i(TAG, "onPlaybackStateChanged: ", state.getState());
                    mLastPlaybackState = state;
                    switch (state.getState()) {
                        case PlaybackStateCompat.STATE_PAUSED:
                            stopSeekbarUpdate();
                            showPlaying(false);
                            scroll_song_bar.setEnabled(true);
                            break;
                        case PlaybackStateCompat.STATE_STOPPED:
                            stopSeekbarUpdate();
                            scroll_song_bar.setProgress(0);
                            scroll_song_bar.setEnabled(false);
                            showPlaying(false);
                            break;
                        case PlaybackStateCompat.STATE_ERROR:
                            stopSeekbarUpdate();
                            scroll_song_bar.setProgress(0);
                            scroll_song_bar.setEnabled(false);
                            showPlaying(false);
//                            Log.e(TAG, "onPlaybackStateChanged: " + state.getErrorMessage());
                            LogHelper.e(TAG, "onPlaybackStateChanged: " + state.getErrorMessage());
                            Snackbar.make(
                                    findViewById(android.R.id.content),
                                    state.getErrorMessage(),
                                    Snackbar.LENGTH_SHORT)
                                    .show();
                            break;
                        case PlaybackStateCompat.STATE_PLAYING:
                            scheduleSeekbarUpdate();
                            showPlaying(true);
                            scroll_song_bar.setEnabled(true);
                            break;
                    }
                }

                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
//                    Log.d(TAG, "onMetadataChanged: ");
                    LogHelper.i(TAG, "onMetadataChanged");
                    int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
                    //                    mEnd.setText(DateUtils.formatElapsedTime(duration/1000));
                    scroll_song_bar.setMax(duration);
                    scroll_song_bar.setEnabled(true);
                    SimpleDialogFragment sFragment =
                            SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "BUFFERING");
                    if (sFragment != null) sFragment.dismiss();
                }
            };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagina_render);
        ButterKnife.bind(this);

        ((TextView)findViewById(R.id.main_toolbarTitle)).setText(R.string.canto_title_activity);
        mToolbar.setBackgroundColor(getThemeUtils().primaryColor());
        setSupportActionBar(mToolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.bottom_bar).setBackgroundColor(getThemeUtils().primaryColor());

        mLUtils = LUtils.getInstance(PaginaRenderActivity.this);

        listaCanti = new DatabaseCanti(this);

        IconicsDrawable icon = new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_plus)
                .color(Color.WHITE)
                .sizeDp(24)
                .paddingDp(4);
        mFab.setImageDrawable(icon);

        // recupera il numero della pagina da visualizzare dal parametro passato dalla chiamata
        Bundle bundle = this.getIntent().getExtras();
        pagina = bundle != null ? bundle.getCharSequence("pagina", "").toString() : null;
        idCanto = bundle != null ? bundle.getInt("idCanto") : 0;

        getRecordLink();

        try {
            primaNota = CambioAccordi.recuperaPrimoAccordo(getAssets().open(pagina + ".htm"), ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration()).getLanguage());
            primoBarre = cambioAccordi.recuperaBarre(getAssets().open(pagina + ".htm"), ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration()).getLanguage());
        }
        catch (IOException e) {
//            Log.e(TAG, e.getLocalizedMessage(), e);
            LogHelper.e(TAG, e.getLocalizedMessage(), e);
        }

        SQLiteDatabase db = listaCanti.getReadableDatabase();

//        String query = "SELECT saved_tab, saved_barre, saved_speed, titolo" +
        String query = "SELECT saved_tab, saved_barre, saved_speed" +
                "  FROM ELENCO" +
                "  WHERE _id =  " + idCanto;
        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();
        notaSalvata = cursor.getString(0);
        barreSalvato = cursor.getString(1);
        savedSpeed = cursor.getInt(2);
//        titoloCanto = cursor.getString(3);
        cursor.close();
        db.close();

        //recupera i pulsanti
        showPlaying(false);

        if (savedInstanceState != null) {
            mediaPlayerState = (MP_State) savedInstanceState.getSerializable("mediaPlayerState");
            showPlaying(savedInstanceState.getBoolean("playSelected"));
            scroll_song_bar.setMax(savedInstanceState.getInt("scroll_audio_max", 0));
        }

        if (mediaPlayerState == MP_State.Stopped) {
            scroll_song_bar.setEnabled(false);
            scroll_song_bar.setProgress(0);
        }

        if (!url.equalsIgnoreCase("")) {
            checkExternalFilePermissions();

            mDownload = !(localUrl.equalsIgnoreCase("") &&
                    personalUrl.equalsIgnoreCase(""));

            //mostra i pulsanti per il lettore musicale
            music_buttons.setVisibility(View.VISIBLE);
            no_records_text.setVisibility(View.INVISIBLE);

            localFile = !(localUrl.equalsIgnoreCase("")
                    && personalUrl.equalsIgnoreCase(""));

//            if (localUrl.equalsIgnoreCase("")
//                    && personalUrl.equalsIgnoreCase("")) {
//                localFile = false;
//                playUrl = url;
//            }
//            else {
//                localFile = true;
//                if (!localUrl.equals(""))
//                    playUrl = localUrl;
//                else
//                    playUrl = personalUrl;
//            }

        }
        else {
            localFile = true;
//            playUrl = personalUrl;

            if (!personalUrl.equalsIgnoreCase("")) {
                mDownload = true;

                //mostra i pulsanti per il lettore musicale
                music_buttons.setVisibility(View.VISIBLE);
                no_records_text.setVisibility(View.INVISIBLE);
            }
            else {
                // nasconde i pulsanti
                mDownload = false;
                music_buttons.setVisibility(View.INVISIBLE);
                no_records_text.setVisibility(View.VISIBLE);
            }

        }

        scroll_song_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                Log.d(TAG, "newValue: " + progress);
                LogHelper.d(TAG, "newValue: ", progress);
//                if (fromUser) {
//                    Intent i = new Intent(getApplicationContext(), MusicService.class);
//                    i.setAction(MusicService.ACTION_SEEK);
//                    Uri uri = Uri.parse(String.valueOf(progress));
//                    i.setData(uri);
////                    startService(i);
//                    ContextCompat.startForegroundService(PaginaRenderActivity.this, i);
//                }
                String time = String.format(ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration()), "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(progress),
                        TimeUnit.MILLISECONDS.toSeconds(progress) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(progress))
                );
                ((TextView) findViewById(R.id.time_text)).setText(time);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopSeekbarUpdate();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MediaControllerCompat.getMediaController(PaginaRenderActivity.this).getTransportControls().seekTo(seekBar.getProgress());
                scheduleSeekbarUpdate();
            }
        });

        scroll_speed_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speedValue = String.valueOf(progress);
                ((TextView) findViewById(R.id.slider_text)).setText(getString(R.string.percent_progress, progress));
//                Log.d(getClass().toString(), "speedValue cambiato! " + speedValue);
                LogHelper.d(TAG, "speedValue cambiato: ", speedValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        showScrolling(false);

        if (mostraAudio == null) {
            SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(this);
            mostraAudio = String.valueOf(pref.getBoolean(Utility.SHOW_AUDIO, true));
        }
        mostraAudioBool = Boolean.parseBoolean(mostraAudio);

        SimpleDialogFragment sFragment = SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "DOWNLOAD_MP3");
        if (sFragment != null)
            sFragment.setmCallback(PaginaRenderActivity.this);
        sFragment = SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "DELETE_LINK");
        if (sFragment != null)
            sFragment.setmCallback(PaginaRenderActivity.this);
        sFragment = SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "DELETE_LINK_2");
        if (sFragment != null)
            sFragment.setmCallback(PaginaRenderActivity.this);
        sFragment = SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "DOWNLINK_CHOOSE");
        if (sFragment != null)
            sFragment.setmCallback(PaginaRenderActivity.this);
        sFragment = SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "DELETE_MP3");
        if (sFragment != null)
            sFragment.setmCallback(PaginaRenderActivity.this);
        sFragment = SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "ONLY_LINK");
        if (sFragment != null)
            sFragment.setmCallback(PaginaRenderActivity.this);
        sFragment = SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "SAVE_TAB");
        if (sFragment != null)
            sFragment.setmCallback(PaginaRenderActivity.this);
        //        sFragment = SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "EXTERNAL_STORAGE_RATIONALE");
        //        if (sFragment != null)
        //            sFragment.setmCallback(PaginaRenderActivity.this);
        //        sFragment = SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "EXTERNAL_FILE_RATIONALE");
        //        if (sFragment != null)
        //            sFragment.setmCallback(PaginaRenderActivity.this);

        // Connect a media browser just to get the media session token. There are other ways
        // this can be done, for example by sharing the session token directly.
        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, it.cammino.risuscito.services.MusicService.class), mConnectionCallback, null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.canto, menu);
        menu.findItem(R.id.tonalita).setIcon(
                new IconicsDrawable(PaginaRenderActivity.this, CommunityMaterial.Icon.cmd_music_note)
                        .sizeDp(24)
                        .paddingDp(2)
                        .color(Color.WHITE));
        menu.findItem(R.id.action_trasporta).setIcon(
                new IconicsDrawable(PaginaRenderActivity.this, CommunityMaterial.Icon.cmd_swap_vertical)
                        .sizeDp(24)
                        .paddingDp(2)
                        .colorRes(R.color.icon_ative_black));
        menu.findItem(R.id.action_save_tab).setIcon(
                new IconicsDrawable(PaginaRenderActivity.this, CommunityMaterial.Icon.cmd_content_save)
                        .sizeDp(24)
                        .paddingDp(2)
                        .colorRes(R.color.icon_ative_black));
        menu.findItem(R.id.action_reset_tab).setIcon(
                new IconicsDrawable(PaginaRenderActivity.this, CommunityMaterial.Icon.cmd_refresh)
                        .sizeDp(24)
                        .paddingDp(2)
                        .colorRes(R.color.icon_ative_black));
        menu.findItem(R.id.barre).setIcon(
                new IconicsDrawable(PaginaRenderActivity.this, CommunityMaterial.Icon.cmd_guitar_electric)
                        .sizeDp(24)
                        .paddingDp(2)
                        .color(Color.WHITE));
        menu.findItem(R.id.action_trasporta_barre).setIcon(
                new IconicsDrawable(PaginaRenderActivity.this, CommunityMaterial.Icon.cmd_swap_vertical)
                        .sizeDp(24)
                        .paddingDp(2)
                        .colorRes(R.color.icon_ative_black));
        menu.findItem(R.id.action_save_barre).setIcon(
                new IconicsDrawable(PaginaRenderActivity.this, CommunityMaterial.Icon.cmd_content_save)
                        .sizeDp(24)
                        .paddingDp(2)
                        .colorRes(R.color.icon_ative_black));
        menu.findItem(R.id.action_reset_barre).setIcon(
                new IconicsDrawable(PaginaRenderActivity.this, CommunityMaterial.Icon.cmd_refresh)
                        .sizeDp(24)
                        .paddingDp(2)
                        .colorRes(R.color.icon_ative_black));
        menu.findItem(R.id.action_exp_pdf).setIcon(
                new IconicsDrawable(PaginaRenderActivity.this, CommunityMaterial.Icon.cmd_file_pdf_box)
                        .sizeDp(24)
                        .paddingDp(2)
                        .color(Color.WHITE));
        menu.findItem(R.id.action_help_canto).setIcon(
                new IconicsDrawable(PaginaRenderActivity.this, CommunityMaterial.Icon.cmd_help_circle)
                        .sizeDp(24)
                        .paddingDp(2)
                        .color(Color.WHITE));
        SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(PaginaRenderActivity.this);
//        Log.d(TAG, "onCreateOptionsMenu - INTRO_PAGINARENDER: " + mSharedPrefs.getBoolean(Utility.INTRO_PAGINARENDER, false));
        LogHelper.d(TAG, "onCreateOptionsMenu - INTRO_PAGINARENDER: ", mSharedPrefs.getBoolean(Utility.INTRO_PAGINARENDER, false));
        if (!mSharedPrefs.getBoolean(Utility.INTRO_PAGINARENDER, false)) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    if (music_buttons.getVisibility() == View.VISIBLE)
                        playIntroFull();
                    else
                        playIntroSmall();
                }
            }, 1500);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (notaCambio == null || notaSalvata == null
                        || barreCambio == null || barreSalvato == null
                        || (notaCambio.equals(notaSalvata)
                        && barreCambio.equals(barreSalvato))) {
                    pulisciVars();
                    mLUtils.closeActivityWithTransition();
                    return true;
                }
                else {
                    new SimpleDialogFragment.Builder(PaginaRenderActivity.this, PaginaRenderActivity.this, "SAVE_TAB")
                            .title(R.string.dialog_save_tab_title)
                            .content(R.string.dialog_save_tab)
                            .positiveButton(R.string.confirm)
                            .negativeButton(R.string.dismiss)
                            .show();
                    break;
                }
            case R.id.action_exp_pdf:
                new SimpleDialogFragment.Builder(PaginaRenderActivity.this, PaginaRenderActivity.this, "EXPORT_PDF")
                        .content(R.string.export_running)
                        .showProgress()
                        .progressIndeterminate(true)
                        .progressMax(0)
                        .show()
                        .setCancelable(true);
                Intent i = new Intent(getApplicationContext(), PdfExportService.class);
                i.putExtra(PdfExportService.DATA_PRIMA_NOTA, primaNota);
                i.putExtra(PdfExportService.DATA_NOTA_CAMBIO, notaCambio);
                i.putExtra(PdfExportService.DATA_PRIMO_BARRE, primoBarre);
                i.putExtra(PdfExportService.DATA_BARRE_CAMBIO, barreCambio);
                i.putExtra(PdfExportService.DATA_PAGINA, pagina);
                i.putExtra(PdfExportService.DATA_LINGUA, ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration()).getLanguage());
                startService(i);
                return true;
            case R.id.action_help_canto:
                if (music_buttons.getVisibility() == View.VISIBLE)
                    playIntroFull();
                else
                    playIntroSmall();
                return true;
            case R.id.action_save_tab:
                if (!notaSalvata.equalsIgnoreCase(notaCambio)) {
                    notaSalvata = notaCambio;
                    SQLiteDatabase db = listaCanti.getReadableDatabase();
                    String sql = "UPDATE ELENCO" +
                            "  SET saved_tab = \'" + notaCambio + "\' " +
                            "  WHERE _id =  " + idCanto;
                    db.execSQL(sql);
                    db.close();
                    Snackbar.make(findViewById(android.R.id.content)
                            , R.string.tab_saved
                            , Snackbar.LENGTH_SHORT)
                            .show();
                }
                else {
                    Snackbar.make(findViewById(android.R.id.content)
                            , R.string.tab_not_saved
                            , Snackbar.LENGTH_SHORT)
                            .show();
                }
                return true;
            case R.id.action_reset_tab:
                notaCambio = primaNota;
                HashMap<String, String> convMap = cambioAccordi.diffSemiToni(primaNota, notaCambio);
                HashMap<String, String> convMin = null;
                if (ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration()).getLanguage().equalsIgnoreCase("uk"))
                    convMin = cambioAccordi.diffSemiToniMin(primaNota, notaCambio);
                saveZoom();
                if (convMap != null) {
                    String nuovoFile = cambiaAccordi(convMap, barreCambio, convMin, true);
                    if (nuovoFile != null)
                        paginaView.loadUrl("file://" + nuovoFile);
                }
                else {
                    paginaView.loadUrl("file:///android_asset/" + pagina + ".htm");
                }
                if (defaultZoomLevel > 0)
                    paginaView.setInitialScale(defaultZoomLevel);
                paginaView.setWebViewClient(new MyWebViewClient());
                return true;
            case R.id.action_save_barre:
                if (!barreSalvato.equalsIgnoreCase(barreCambio)) {
                    barreSalvato = barreCambio;
                    SQLiteDatabase db = listaCanti.getReadableDatabase();
                    String sql = "UPDATE ELENCO" +
                            "  SET saved_barre = \'" + barreCambio + "\' " +
                            "  WHERE _id =  " + idCanto;
                    db.execSQL(sql);
                    db.close();
                    Snackbar.make(findViewById(android.R.id.content)
                            , R.string.barre_saved
                            , Snackbar.LENGTH_SHORT)
                            .show();
                }
                else {
                    Snackbar.make(findViewById(android.R.id.content)
                            , R.string.barre_not_saved
                            , Snackbar.LENGTH_SHORT)
                            .show();

                }
                return true;
            case R.id.action_reset_barre:
                barreCambio = primoBarre;
                HashMap<String, String> convMap1 = cambioAccordi.diffSemiToni(primaNota, notaCambio);
                HashMap<String, String> convMin1 = null;
                if (ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration()).getLanguage().equalsIgnoreCase("uk"))
                    convMin1 = cambioAccordi.diffSemiToniMin(primaNota, notaCambio);
                saveZoom();
                if (convMap1 != null) {
                    String nuovoFile = cambiaAccordi(convMap1, barreCambio, convMin1, true);
                    if (nuovoFile != null)
                        paginaView.loadUrl("file://" + nuovoFile);
                }
                else {
                    paginaView.loadUrl("file:///android_asset/" + pagina + ".htm");
                }
                if (defaultZoomLevel > 0)
                    paginaView.setInitialScale(defaultZoomLevel);
                paginaView.setWebViewClient(new MyWebViewClient());
                return true;
            default:
                if (item.getGroupId() == R.id.menu_gruppo_note) {
                    notaCambio = String.valueOf(item.getTitleCondensed());
                    HashMap<String, String> convMap2 = cambioAccordi.diffSemiToni(primaNota, notaCambio);
                    HashMap<String, String> convMin2 = null;
                    if (ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration()).getLanguage().equalsIgnoreCase("uk"))
                        convMin2 = cambioAccordi.diffSemiToniMin(primaNota, notaCambio);
                    saveZoom();
                    if (convMap2 != null) {
                        String nuovoFile = cambiaAccordi(convMap2, barreCambio, convMin2, true);
                        if (nuovoFile != null)
                            paginaView.loadUrl("file://" + nuovoFile);
                    }
                    else {
                        paginaView.loadUrl("file:///android_asset/" + pagina + ".htm");
                    }
                    if (defaultZoomLevel > 0)
                        paginaView.setInitialScale(defaultZoomLevel);
                    paginaView.setWebViewClient(new MyWebViewClient());
                    return true;
                }
                if (item.getGroupId() == R.id.menu_gruppo_barre) {
                    barreCambio = String.valueOf(item.getTitleCondensed());
                    HashMap<String, String> convMap3 = cambioAccordi.diffSemiToni(primaNota, notaCambio);
                    HashMap<String, String> convMin3 = null;
                    if (ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration()).getLanguage().equalsIgnoreCase("uk"))
                        convMin3 = cambioAccordi.diffSemiToniMin(primaNota, notaCambio);
                    saveZoom();
                    if (convMap3 != null) {
                        String nuovoFile = cambiaAccordi(convMap3, barreCambio, convMin3, true);
                        if (nuovoFile != null)
                            paginaView.loadUrl("file://" + nuovoFile);
                    }
                    else {
                        paginaView.loadUrl("file:///android_asset/" + pagina + ".htm");
                    }
                    if (defaultZoomLevel > 0)
                        paginaView.setInitialScale(defaultZoomLevel);
                    paginaView.setWebViewClient(new MyWebViewClient());
                    return true;
                }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
//        Log.d(TAG, "onBackPressed: ");
        LogHelper.d(TAG, "onBackPressed");
        if (notaCambio == null || notaSalvata == null
                || barreCambio == null || barreSalvato == null
                || (notaCambio.equals(notaSalvata)
                && barreCambio.equals(barreSalvato))) {
            pulisciVars();
            mLUtils.closeActivityWithTransition();
        }
        else {
            new SimpleDialogFragment.Builder(PaginaRenderActivity.this, PaginaRenderActivity.this, "SAVE_TAB")
                    .title(R.string.dialog_save_tab_title)
                    .content(R.string.dialog_save_tab)
                    .positiveButton(R.string.confirm)
                    .negativeButton(R.string.dismiss)
                    .show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (notaSalvata == null) {
            if (notaCambio == null)
                notaSalvata = notaCambio = primaNota;
            else
                notaSalvata = primaNota;
        }
        else {
//	    	Log.i("NOTA SALVATA", notaSalvata);
//	    	Log.i("AVVIO", "notaCambio = " + notaCambio);
            if (notaCambio == null)
                notaCambio = notaSalvata;
        }

        if (barreSalvato == null) {
            if (barreCambio == null)
                barreSalvato = barreCambio = primoBarre;
            else
                barreSalvato = primoBarre;
        }
        else {
//	    	Log.i("BARRESALVATO", barreSalvato);
            if (barreCambio == null)
                barreCambio = barreSalvato;
        }

        getZoom();

        //fix per crash su android 4.1
        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN)
            paginaView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        HashMap<String, String> convMap = cambioAccordi.diffSemiToni(primaNota, notaCambio);
        HashMap<String, String> convMin = null;
//        if (getResources().getConfiguration().locale.getLanguage().equalsIgnoreCase("uk"))
        if (ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration()).getLanguage().equalsIgnoreCase("uk"))
            convMin = cambioAccordi.diffSemiToniMin(primaNota, notaCambio);
        if (convMap != null) {
            String nuovoFile = cambiaAccordi(convMap, barreCambio, convMin, true);
            if (nuovoFile != null)
                paginaView.loadUrl("file://" + nuovoFile);
        }
        else
            paginaView.loadUrl("file:///android_asset/" + pagina + ".htm");

        WebSettings webSettings = paginaView.getSettings();
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(true);
        webSettings.setLoadWithOverviewMode(true);

        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        if (defaultZoomLevel > 0)
            paginaView.setInitialScale(defaultZoomLevel);
        paginaView.setWebViewClient(new MyWebViewClient());

        if (speedValue == null) {
//	    	Log.i("SONO APPENA ENTRATO", "setto " + savedSpeed);
            scroll_speed_bar.setProgress(savedSpeed);
        }
        else {
//	    	Log.i("ROTAZIONE", "setto " + speedValue);
            scroll_speed_bar.setProgress(Integer.valueOf(speedValue));
        }

//	    Log.i(this.getClass().toString(), "scrollPlaying? " + scrollPlaying);
        if (scrollPlaying) {
            showScrolling(true);
            mScrollDown.run();
        }

        findViewById(R.id.music_controls).setVisibility(mostraAudioBool ? View.VISIBLE : View.GONE);

        //registra un receiver per ricevere la notifica di preparazione della registrazione
//        registerReceiver(gpsBRec, new IntentFilter(
//                MusicService.BROADCAST_PREPARING_COMPLETED));
//        registerReceiver(stopBRec, new IntentFilter(
//                MusicService.BROADCAST_PLAYBACK_COMPLETED));
//        registerReceiver(positionBRecc, new IntentFilter(
//                MusicService.BROADCAST_PLAYER_POSITION));
//        registerReceiver(playBRec, new IntentFilter(
//                MusicService.BROADCAST_PLAYER_STARTED));
//        registerReceiver(pauseBRec, new IntentFilter(
//                MusicService.BROADCAST_PLAYBACK_PAUSED));
        registerReceiver(downloadPosBRec, new IntentFilter(
                DownloadService.BROADCAST_DOWNLOAD_PROGRESS));
        registerReceiver(downloadCompletedBRec, new IntentFilter(
                DownloadService.BROADCAST_DOWNLOAD_COMPLETED));
        registerReceiver(downloadErrorBRec, new IntentFilter(
                DownloadService.BROADCAST_DOWNLOAD_ERROR));
        registerReceiver(exportCompleted, new IntentFilter(
                PdfExportService.BROADCAST_EXPORT_COMPLETED));
        registerReceiver(exportError, new IntentFilter(
                PdfExportService.BROADCAST_EXPORT_ERROR));
        registerReceiver(fabBRec, new IntentFilter(
                BottomSheetFabCanto.CHOOSE_DONE));

//        Log.d(TAG, "onResume: ");
        LogHelper.d(TAG, "onResume");

    }

    @Override
    public void onDestroy() {
//        Log.d(TAG, "onDestroy()");
        LogHelper.d(TAG, "onDestroy");
        try {
//            unregisterReceiver(gpsBRec);
//            unregisterReceiver(stopBRec);
//            unregisterReceiver(positionBRecc);
//            unregisterReceiver(playBRec);
//            unregisterReceiver(pauseBRec);
            unregisterReceiver(downloadPosBRec);
            unregisterReceiver(downloadCompletedBRec);
            unregisterReceiver(downloadErrorBRec);
            unregisterReceiver(exportCompleted);
            unregisterReceiver(exportError);
            unregisterReceiver(fabBRec);
        } catch (IllegalArgumentException e) {
//            Log.e(TAG, e.getLocalizedMessage(), e);
            LogHelper.e(TAG, e.getLocalizedMessage(), e);
        }
        saveZoom();
//        Log.d(TAG, "onDestroy: isFinishing " + isFinishing());
//        Log.d(TAG, "onDestroy: mediaPlayerState " + mediaPlayerState);
//        if (isFinishing() && mediaPlayerState != MP_State.Stopped) {
//            Intent i = new Intent(getApplicationContext(), MusicService.class);
//            stopService(i);
//            i.setAction(MusicService.ACTION_STOP);
//            startService(i);
//            ContextCompat.startForegroundService(PaginaRenderActivity.this, i);
//        }
        if (listaCanti != null)
            listaCanti.close();
        super.onDestroy();
        stopSeekbarUpdate();
        mExecutorService.shutdown();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("mediaPlayerState", mediaPlayerState);
        outState.putBoolean("playSelected", isPlaying());
        outState.putInt("scroll_audio_max", scroll_song_bar.getMax());
    }

    public void pulisciVars() {
        saveZoom();

        notaCambio = null;
        barreCambio = null;

        SaveSpeed();
        if (scrollPlaying) {
            showScrolling(false);
            scrollPlaying = false;
            mHandler.removeCallbacks(mScrollDown);
        }
        speedValue = null;
        mostraAudio = null;
    }

    //recupera il flag preferito per la pagina
    public int selectFavouriteFromSource() {

        SQLiteDatabase db = listaCanti.getReadableDatabase();

        String query = "SELECT favourite" +
                "  FROM ELENCO" +
                "  WHERE _id =  " + idCanto;
        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();
        int favouriteFlag = cursor.getInt(0);

        cursor.close();
        db.close();
        return favouriteFlag;
    }

    //aggiorna il flag che indica se la pagina è tra i preferiti
    public void updateFavouriteFlag(int favouriteFlag) {

        SQLiteDatabase db = listaCanti.getReadableDatabase();

        String sql = "UPDATE ELENCO" +
                "  SET favourite = " + favouriteFlag + " " +
                "  WHERE _id =  " + idCanto;
        db.execSQL(sql);
        db.close();

    }

    //recupera e setta il record per la registrazione
    private void getRecordLink() {

        SQLiteDatabase db = listaCanti.getReadableDatabase();

        String query = "SELECT link" +
                "  FROM ELENCO" +
                "  WHERE _id =  " + idCanto;
        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();
        if (cursor.getString(0) != null && !cursor.getString(0).equals(""))
            url = cursor.getString(0);
        else
            url = "";

        cursor.close();

        query = "SELECT local_path" +
                "  FROM LOCAL_LINKS" +
                "  WHERE _id =  " + idCanto;
        cursor = db.rawQuery(query, null);

        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            personalUrl = cursor.getString(0);
        }
        else
            personalUrl = "";

        cursor.close();
        db.close();

    }

    //recupera e setta lo zoom
    private void getZoom() {

        SQLiteDatabase db = listaCanti.getReadableDatabase();

        String query = "SELECT zoom, scroll_x , scroll_y" +
                "  FROM ELENCO" +
                "  WHERE _id =  " + idCanto;
        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();
        defaultZoomLevel = cursor.getInt(0);
        defaultScrollX = cursor.getInt(1);
        defaultScrollY = cursor.getInt(2);

        cursor.close();
        db.close();

    }

    private void saveZoom(){
        //noinspection deprecation
        defaultZoomLevel = (int) (paginaView.getScale() *100);
        defaultScrollX = paginaView.getScrollX();
        defaultScrollY = paginaView.getScrollY();

        SQLiteDatabase db = listaCanti.getReadableDatabase();

        String sql = "UPDATE ELENCO" +
                "  SET zoom = " + defaultZoomLevel + " " +
                ", scroll_x = " + defaultScrollX + " " +
                ", scroll_y = " + defaultScrollY + " " +
                "  WHERE _id =  " + idCanto;
        db.execSQL(sql);
        db.close();
    }

    private void SaveSpeed(){
        SQLiteDatabase db = listaCanti.getReadableDatabase();
        String sql = "UPDATE ELENCO" +
                "  SET saved_speed = " + speedValue +
                "  WHERE _id =  " + idCanto;
        db.execSQL(sql);
        db.close();
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (defaultScrollX > 0 || defaultScrollY > 0)
                        paginaView.scrollTo(defaultScrollX, defaultScrollY);
                }
                // Delay the scrollTo to make it work
            }, 600);
            super.onPageFinished(view, url);
        }
    }

    @Nullable
    private String cambiaAccordi(HashMap<String, String> conversione, String barre, HashMap<String, String> conversioneMin, boolean higlightDiff) {
        String cantoTrasportato = this.getFilesDir() + "/temporaneo.htm";

        boolean barre_scritto = false;

        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            getAssets().open(pagina + ".htm"), "UTF-8"));

            String line = br.readLine();

            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(cantoTrasportato), "UTF-8"));

            String language = ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration()).getLanguage();

            Pattern pattern;
            Pattern patternMinore = null;

            switch (language) {
                case "it":
                    pattern = Pattern.compile("Do#|Do|Re|Mib|Mi|Fa#|Fa|Sol#|Sol|La|Sib|Si");
                    break;
                case "uk":
                    pattern = Pattern.compile("Cis|C|D|Eb|E|Fis|F|Gis|G|A|B|H");
                    //inserito spazio prima di "b" per evitare che venga confuso con "Eb" o "eb"
                    patternMinore = Pattern.compile("cis|c|d|eb|e|fis|f|gis|g|a| b|h");
                    break;
                case "en":
                    pattern = Pattern.compile("C#|C|D|Eb|E|F#|F|G#|G|A|Bb|B");
                    break;
                default:
                    pattern = Pattern.compile("Do#|Do|Re|Mib|Mi|Fa#|Fa|Sol#|Sol|La|Sib|Si");
                    break;
            }


            //serve per segnarsi se si è già evidenziato il primo accordo del testo
            boolean notaHighlighed = !higlightDiff;

            while (line != null) {
//                Log.d(TAG, "RIGA DA ELAB: " + line);
                LogHelper.d(TAG, "RIGA DA ELAB: ", line);
                if (line.contains("A13F3C") && !line.contains("<H2>") && !line.contains("<H4>")) {
                    if (language.equalsIgnoreCase("uk") || language.equalsIgnoreCase("en")) {
                        line = line.replaceAll("</FONT><FONT COLOR=\"#A13F3C\">", "<K>");
                        line = line.replaceAll("</FONT><FONT COLOR=\"#000000\">", "<K2>");
                    }
                    Matcher matcher = pattern.matcher(line);
                    StringBuffer sb = new StringBuffer();
                    StringBuffer sb2 = new StringBuffer();
                    while(matcher.find())
                        matcher.appendReplacement(sb, conversione.get(matcher.group(0)));
                    matcher.appendTail(sb);
                    if (language.equalsIgnoreCase("uk")) {
                        Matcher matcherMin = patternMinore.matcher(sb.toString());
                        while (matcherMin.find())
                            matcherMin.appendReplacement(sb2, conversioneMin.get(matcherMin.group(0)));
                        matcherMin.appendTail(sb2);
                        line = sb2.toString();
//                        Log.d(TAG, "RIGA ELAB 1: " + line);
//                        Log.d(TAG, "notaHighlighed: " + notaHighlighed);
//                        Log.d(TAG, "notaCambio: " + notaCambio);
//                        Log.d(TAG, "primaNota: " + primaNota);
                        if (!notaHighlighed) {
                            if (!primaNota.equalsIgnoreCase(notaCambio)) {
                                if (Utility.isLowerCase(primaNota.charAt(0))) {
                                    String notaCambioMin = notaCambio;
                                    if (notaCambioMin.length() == 1)
                                        notaCambioMin = notaCambioMin.toLowerCase();
                                    else
                                        notaCambioMin = notaCambioMin.substring(0,1).toLowerCase() + notaCambioMin.substring(1);
                                    line = line.replaceFirst(notaCambioMin, "<SPAN STYLE=\"BACKGROUND-COLOR:#FFFF00\">" + notaCambioMin + "</SPAN>");
                                }
                                else
                                    line = line.replaceFirst(notaCambio, "<SPAN STYLE=\"BACKGROUND-COLOR:#FFFF00\">" + notaCambio + "</SPAN>");
                                notaHighlighed = true;
                            }
                        }
//                        Log.d(TAG, "RIGA ELAB 2: " + line);
                        line = line.replaceAll("<K>", "</FONT><FONT COLOR='#A13F3C'>");
                        line = line.replaceAll("<K2>", "</FONT><FONT COLOR='#000000'>");
//                        Log.d(TAG, "RIGA ELAB 3: " + line);
                    }
                    else {
                        line = sb.toString();
                        if (!notaHighlighed) {
                            if (!primaNota.equalsIgnoreCase(notaCambio)) {
                                line = line.replaceFirst(notaCambio, "<SPAN STYLE=\"BACKGROUND-COLOR:#FFFF00\">" + notaCambio + "</SPAN>");
                                notaHighlighed = true;
                            }
                        }

                        if (language.equalsIgnoreCase("en")) {
                            line = line.replaceAll("<K>", "</FONT><FONT COLOR='#A13F3C'>");
                            line = line.replaceAll("<K2>", "</FONT><FONT COLOR='#000000'>");
                        }

                    }
                    out.write(line);
                    out.newLine();
                }
                else {
                    if (line.contains("<H3>")) {
                        if (barre != null && !barre.equals("0")) {
                            if (!barre_scritto) {
                                String oldLine;
                                if (higlightDiff && !barre.equalsIgnoreCase(primoBarre)) {
                                    oldLine = "<H4><SPAN STYLE=\"BACKGROUND-COLOR:#FFFF00\"><FONT COLOR=\"#A13F3C\"><I>"
                                            + getString(R.string.barre_al_tasto, barre)
                                            + "</I></FONT></SPAN></H4>";
                                }
                                else {
                                    oldLine = "<H4><FONT COLOR=\"#A13F3C\"><I>"
                                            + getString(R.string.barre_al_tasto, barre)
                                            + "</I></FONT></H4>";
                                }
                                out.write(oldLine);
                                out.newLine();
                                barre_scritto = true;
                            }
                        }
                        out.write(line);
                        out.newLine();
                    }
                    else {
                        if (!line.contains(getString(R.string.barre_search_string))) {
                            out.write(line);
                            out.newLine();
                        }
                    }
                }
                line = br.readLine();
            }
            br.close();
            out.flush();
            out.close();
            return cantoTrasportato;
        }
        catch(Exception e) {
//            Log.e(TAG, e.getLocalizedMessage(), e);
            LogHelper.e(TAG, e.getLocalizedMessage(), e);
            return null;
        }
    }

    @AfterPermissionGranted(Utility.WRITE_STORAGE_RC)
    private void checkStoragePermissions() {
//        Log.d(TAG, "checkStoragePermissions: ");
        LogHelper.d(TAG, "checkStoragePermissions");
        // Here, thisActivity is the current activity
//        if(ContextCompat.checkSelfPermission(PaginaRenderActivity.this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                !=PackageManager.PERMISSION_GRANTED) {
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(PaginaRenderActivity.this,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                // Show an expanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//                showRationaleForExternalDownload();
//            } else {
//                // No explanation needed, we can request the permission.
//                ActivityCompat.requestPermissions(PaginaRenderActivity.this,
//                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                        Utility.WRITE_STORAGE_RC);
//            }
//        }
//        else
//            startExternalDownload();
        if (EasyPermissions.hasPermissions(PaginaRenderActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Have permission, do the thing!
            startExternalDownload();
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(PaginaRenderActivity.this, getString(R.string.external_storage_rationale),
                    Utility.WRITE_STORAGE_RC, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    void startExternalDownload() {
//        Log.d(TAG, " WRITE_EXTERNAL_STORAGE OK");
        LogHelper.d(TAG, "WRITE_EXTERNAL_STORAGE", "OK");
        if (Utility.isExternalStorageWritable()) {
            if (new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MUSIC), "Risuscitò").mkdirs())
//                Log.d(TAG, "CARTELLA RISUSCITO CREATA");
                LogHelper.d(TAG, "CARTELLA RISUSCITO CREATA");
            else
//                Log.d(TAG, "CARTELLA RISUSCITO ESISTENTE");
                LogHelper.d(TAG, "CARTELLA RISUSCITO ESISTENTE");
            String localFile = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MUSIC).getAbsolutePath()
                    + "/Risuscitò/" + Utility.filterMediaLinkNew(url);
            new SimpleDialogFragment.Builder(PaginaRenderActivity.this, PaginaRenderActivity.this, "DOWNLOAD_MP3")
                    .title(R.string.download_running)
                    .showProgress()
                    .positiveButton(R.string.cancel)
                    .progressIndeterminate(false)
                    .progressMax(100)
                    .show();
            Intent i = new Intent(getApplicationContext(), DownloadService.class);
            i.setAction(DownloadService.ACTION_DOWNLOAD);
            Uri uri = Uri.parse(url);
            i.setData(uri);
            i.putExtra(DownloadService.DATA_DESTINATION_FILE, localFile);
            startService(i);
        } else
            Snackbar.make(findViewById(android.R.id.content)
                    , R.string.no_memory_writable
                    , Snackbar.LENGTH_SHORT)
                    .show();
    }

//    void showRationaleForExternalDownload() {
//        Log.d(TAG, "WRITE_EXTERNAL_STORAGE RATIONALE");
//        new SimpleDialogFragment.Builder(PaginaRenderActivity.this, PaginaRenderActivity.this, "EXTERNAL_STORAGE_RATIONALE")
//                .title(R.string.external_storage_title)
//                .content(R.string.external_storage_rationale)
//                .positiveButton(R.string.dialog_chiudi)
//                .setHasCancelListener()
//                .setCanceable()
//                .show();
//    }

    void startInternalDownload() {
        String localFile = PaginaRenderActivity.this.getFilesDir()
                + "/"
                + Utility.filterMediaLink(url);
        new SimpleDialogFragment.Builder(PaginaRenderActivity.this, PaginaRenderActivity.this, "DOWNLOAD_MP3")
                .title(R.string.download_running)
                .showProgress()
                .positiveButton(R.string.cancel)
                .progressIndeterminate(false)
                .progressMax(100)
                .show();
        Intent i = new Intent(getApplicationContext(), DownloadService.class);
        i.setAction(DownloadService.ACTION_DOWNLOAD);
        Uri uri = Uri.parse(url);
        i.setData(uri);
        i.putExtra(DownloadService.DATA_DESTINATION_FILE, localFile);
        startService(i);
    }

    @AfterPermissionGranted(Utility.EXTERNAL_FILE_RC)
    private void checkExternalFilePermissions() {
//        Log.d(TAG, "checkExternalFilePermissions: ");
        LogHelper.d(TAG, "checkExternalFilePermissions");
        // Here, thisActivity is the current activity
//        if(ContextCompat.checkSelfPermission(PaginaRenderActivity.this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                !=PackageManager.PERMISSION_GRANTED) {
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(PaginaRenderActivity.this,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                // Show an expanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//                showRationalForExternalFile();
//            } else {
//                // No explanation needed, we can request the permission.
//                ActivityCompat.requestPermissions(PaginaRenderActivity.this,
//                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                        Utility.EXTERNAL_FILE_RC);
//            }
//            localUrl =  Utility.retrieveMediaFileLink(PaginaRenderActivity.this, url, false);
//        }
//        else {
//            searchExternalFile(false);
//        }
        if (EasyPermissions.hasPermissions(PaginaRenderActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Have permission, do the thing!
            searchExternalFile(false);
        } else {
            // Ask for one permission
            localUrl =  Utility.retrieveMediaFileLink(PaginaRenderActivity.this, url, false);
            EasyPermissions.requestPermissions(PaginaRenderActivity.this, getString(R.string.external_file_rationale),
                    Utility.EXTERNAL_FILE_RC, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    void searchExternalFile(boolean recreate) {
        localUrl =  Utility.retrieveMediaFileLink(PaginaRenderActivity.this, url, true);
        if (recreate)
            recreate();
    }

//    void showRationalForExternalFile() {
//        Log.d(TAG, "EXTERNAL_FILE RATIONALE");
//        new SimpleDialogFragment.Builder(PaginaRenderActivity.this, PaginaRenderActivity.this, "EXTERNAL_FILE_RATIONALE")
//                .title(R.string.external_storage_title)
//                .content(R.string.external_file_rationale)
//                .positiveButton(R.string.dialog_chiudi)
//                .setHasCancelListener()
//                .setCanceable()
//                .show();
//    }

    void showDeniedForExternalFile() {
//        Log.d(TAG, " EXTERNAL_FILE DENIED");
        LogHelper.d(TAG, "EXTERNAL_FILE ", "DENIED");
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(PaginaRenderActivity.this)
                .edit();
        editor.putString(Utility.SAVE_LOCATION, "0");
        editor.apply();
        Snackbar.make(findViewById(android.R.id.content)
                , getString(R.string.external_storage_denied)
                , Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
//        Log.d(TAG, "onRequestPermissionsResult-request: " + requestCode);
////        Log.d(TAG, "onRequestPermissionsResult-result: " + grantResults[0]);
//        switch (requestCode) {
//            case Utility.WRITE_STORAGE_RC: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // permission was granted, yay! Do the task you need to do.
//                    startExternalDownload();
//                } else {
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                    SharedPreferences.Editor editor = PreferenceManager
//                            .getDefaultSharedPreferences(PaginaRenderActivity.this)
//                            .edit();
//                    editor.putString(Utility.SAVE_LOCATION, "0");
//                    editor.apply();
//                    Snackbar.make(findViewById(android.R.id.content)
//                            , R.string.forced_private
//                            , Snackbar.LENGTH_SHORT)
//                            .show();
//                    startInternalDownload();
//                }
//                return;
//            }
//            case Utility.EXTERNAL_FILE_RC: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // permission was granted, yay! Do the task you need to do.
//                    searchExternalFile(true);
//                } else {
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                    showDeniedForExternalFile();
//                }
//            }
//        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Some permissions have been granted
//        Log.d(TAG, "onPermissionsGranted: " + requestCode);
        LogHelper.d(TAG, "onPermissionsGranted: ", requestCode);
        switch (requestCode) {
            case Utility.WRITE_STORAGE_RC:
                startExternalDownload();
                return;
            case Utility.EXTERNAL_FILE_RC:
                searchExternalFile(true);
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Some permissions have been denied
//        Log.d(TAG, "onPermissionsDenied: " + requestCode);
        LogHelper.d(TAG, "onPermissionsDenied: ", requestCode);
        switch (requestCode) {
            case Utility.WRITE_STORAGE_RC:
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(PaginaRenderActivity.this)
                        .edit();
                editor.putString(Utility.SAVE_LOCATION, "0");
                editor.apply();
                Snackbar.make(findViewById(android.R.id.content)
                        , R.string.forced_private
                        , Snackbar.LENGTH_SHORT)
                        .show();
                startInternalDownload();
                return;
            case Utility.EXTERNAL_FILE_RC:
                showDeniedForExternalFile();
        }
    }

    private void showPlaying(boolean started) {
        play_button.setSelected(started);
        IconicsDrawable icon = new IconicsDrawable(PaginaRenderActivity.this)
                .icon(started ? CommunityMaterial.Icon.cmd_pause : CommunityMaterial.Icon.cmd_play)
                .colorRes(R.color.icon_ative_black)
                .sizeDp(24)
                .paddingDp(4);
        play_button.setImageDrawable(icon);
    }

    private void showScrolling(boolean scrolling) {
        play_scroll.setSelected(scrolling);
        IconicsDrawable icon = new IconicsDrawable(PaginaRenderActivity.this)
                .icon(scrolling ? CommunityMaterial.Icon.cmd_pause_circle_outline : CommunityMaterial.Icon.cmd_play_circle_outline)
                .color(Color.WHITE)
                .sizeDp(24)
                .paddingDp(2);
        play_scroll.setImageDrawable(icon);
    }


    private boolean isPlaying() {
        return play_button.isSelected();
    }

    @Override
    public void onPositive(@NonNull String tag) {
//        Log.d(TAG, "onPositive: " + tag);
        LogHelper.d(TAG, "onPositive: ", tag);
        switch (tag) {
            case "DOWNLOAD_MP3":
                sendBroadcast(new Intent(DownloadService.ACTION_CANCEL));
                break;
            case "DELETE_LINK":
                Snackbar.make(findViewById(android.R.id.content), R.string.delink_delete, Snackbar.LENGTH_SHORT)
                        .show();

                scroll_song_bar.setProgress(0);
                scroll_song_bar.setEnabled(false);
                showPlaying(false);
//                if (mediaPlayerState != MP_State.Stopped) {
//                    mediaPlayerState = MP_State.Stopped;
//                    Intent i = new Intent(getApplicationContext(), MusicService.class);
//                    stopService(i);
////                    i.setAction(MusicService.ACTION_STOP);
////                    startService(i);
////                    ContextCompat.startForegroundService(PaginaRenderActivity.this, i);
//                }

                if (mLastPlaybackState.getState() != PlaybackStateCompat.STATE_STOPPED) {
                    MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
                    if (controller != null) {
                        controller.getTransportControls().stop();
                    }
                }

                localFile = false;
                personalUrl = "";
//                playUrl = url;

                SQLiteDatabase db = listaCanti.getReadableDatabase();
                String sql = "DELETE FROM LOCAL_LINKS" +
                        "  WHERE _id =  " + idCanto;
                db.execSQL(sql);
                db.close();

                mDownload = false;
                recreate();
                break;
            case "DELETE_MP3":
                File fileToDelete = new File(localUrl);
                if (fileToDelete.delete()) {
                    if (fileToDelete.getAbsolutePath().contains("/Risuscit")) {
                        // initiate media scan and put the new things into the path array to
                        // make the scanner aware of the location and the files you want to see
                        MediaScannerConnection.scanFile(getApplicationContext()
                                , new String[]{fileToDelete.getAbsolutePath()}
                                , null
                                , null);
                    }
                    Snackbar.make(findViewById(android.R.id.content), R.string.file_delete, Snackbar.LENGTH_SHORT)
                            .show();
                }
                else
                    Snackbar.make(findViewById(android.R.id.content), R.string.error, Snackbar.LENGTH_SHORT)
                            .show();

                scroll_song_bar.setProgress(0);
                scroll_song_bar.setEnabled(false);
                showPlaying(false);
//                if (mediaPlayerState != MP_State.Stopped) {
//                    mediaPlayerState = MP_State.Stopped;
//                    Intent i = new Intent(getApplicationContext(), MusicService.class);
//                    stopService(i);
////                    i.setAction(MusicService.ACTION_STOP);
////                    startService(i);
////                    ContextCompat.startForegroundService(PaginaRenderActivity.this, i);
//                }
                if (mLastPlaybackState.getState() != PlaybackStateCompat.STATE_STOPPED) {
                    MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
                    if (controller != null) {
                        controller.getTransportControls().stop();
                    }
                }

                localFile = false;
//                playUrl = url;
                mDownload = false;
                recreate();
                break;
            case "DOWNLINK_CHOOSE":
                SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(PaginaRenderActivity.this);
                int saveLocation = Integer.parseInt(pref.getString(Utility.SAVE_LOCATION, "0"));
                if (saveLocation == 1)
                    checkStoragePermissions();
                else
                    startInternalDownload();
                break;
            case "DELETE_LINK_2":
                Snackbar.make(findViewById(android.R.id.content), R.string.delink_delete, Snackbar.LENGTH_SHORT)
                        .show();

                scroll_song_bar.setProgress(0);
                scroll_song_bar.setEnabled(false);
                showPlaying(false);
//                if (mediaPlayerState != MP_State.Stopped) {
//                    mediaPlayerState = MP_State.Stopped;
//                    Intent i = new Intent(getApplicationContext(), MusicService.class);
//                    stopService(i);
////                    i.setAction(MusicService.ACTION_STOP);
////                    startService(i);
////                    ContextCompat.startForegroundService(PaginaRenderActivity.this, i);
//                }
                if (mLastPlaybackState.getState() != PlaybackStateCompat.STATE_STOPPED) {
                    MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
                    if (controller != null) {
                        controller.getTransportControls().stop();
                    }
                }

                localFile = false;
                personalUrl = "";
//                playUrl = url;

                db = listaCanti.getReadableDatabase();
                sql = "DELETE FROM LOCAL_LINKS" +
                        "  WHERE _id =  " + idCanto;
                db.execSQL(sql);
                db.close();

                mDownload = false;

                music_buttons.setVisibility(View.INVISIBLE);
                no_records_text.setVisibility(View.VISIBLE);
                recreate();
                break;
            case "ONLY_LINK":
                new FileChooserDialog.Builder(PaginaRenderActivity.this)
                        .mimeType("audio/*") // Optional MIME type filter
                        .tag("optional-identifier")
                        .goUpLabel("Up") // custom go up label, default label is "..."
                        .show(PaginaRenderActivity.this);
                break;
            case "SAVE_TAB":
                db = listaCanti.getReadableDatabase();
                sql = "UPDATE ELENCO" +
                        "  SET saved_tab = \'" + notaCambio + "\' " +
                        "    , saved_barre = \'" + barreCambio + "\' " +
                        "  WHERE _id =  " + idCanto;
                db.execSQL(sql);
                db.close();
                pulisciVars();
                mLUtils.closeActivityWithTransition();
                break;
//            case "EXTERNAL_STORAGE_RATIONALE":
//                ActivityCompat.requestPermissions(PaginaRenderActivity.this,
//                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                        Utility.WRITE_STORAGE_RC);
//                break;
//            case "EXTERNAL_FILE_RATIONALE":
//                ActivityCompat.requestPermissions(PaginaRenderActivity.this,
//                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                        Utility.EXTERNAL_FILE_RC);
//                break;
        }
    }
    @Override
    public void onNegative(@NonNull String tag) {
//        Log.d(TAG, "onNegative: " + tag);
        LogHelper.d(TAG, "onNegative: ", tag);
        switch (tag) {
            case "DOWNLINK_CHOOSE":
                new FileChooserDialog.Builder(PaginaRenderActivity.this)
                        .mimeType("audio/*") // Optional MIME type filter
                        .tag("optional-identifier")
                        .goUpLabel("Up") // custom go up label, default label is "..."
                        .show(PaginaRenderActivity.this);
                break;
            case "SAVE_TAB":
                pulisciVars();
                mLUtils.closeActivityWithTransition();
                break;
        }
    }

    @Override
    public void onNeutral(@NonNull String tag) {}

    @Override
    public void onFileSelection(@NonNull FileChooserDialog dialog, @NonNull File file) {
        String path = file.getAbsolutePath();
        Snackbar.make(findViewById(android.R.id.content),
                getString(R.string.file_selected) + ": " + path
                , Snackbar.LENGTH_SHORT)
                .show();

        scroll_song_bar.setProgress(0);
        scroll_song_bar.setEnabled(false);
        showPlaying(false);
//        if (mediaPlayerState != MP_State.Stopped) {
//            mediaPlayerState = MP_State.Stopped;
//            Intent i = new Intent(getApplicationContext(), MusicService.class);
//            stopService(i);
////            i.setAction(MusicService.ACTION_STOP);
////            startService(i);
////            ContextCompat.startForegroundService(PaginaRenderActivity.this, i);
//        }
        if (mLastPlaybackState.getState() != PlaybackStateCompat.STATE_STOPPED) {
            MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
            if (controller != null) {
                controller.getTransportControls().stop();
            }
        }

        SQLiteDatabase db = listaCanti.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("_id", idCanto);
        values.put("local_path", path);
        db.insert("LOCAL_LINKS", null, values);
        db.close();

        localFile = true;
        personalUrl = path;

        mDownload = true;

        //mostra i pulsanti per il lettore musicale
        music_buttons.setVisibility(View.VISIBLE);
        no_records_text.setVisibility(View.INVISIBLE);
        recreate();
    }

    @Override
    public void onFileChooserDismissed(@NonNull FileChooserDialog dialog) {}

    private void playIntroSmall() {
        findViewById(R.id.music_controls).setVisibility(View.VISIBLE);
        new TapTargetSequence(PaginaRenderActivity.this)
                .continueOnCancel(true)
                .targets(
                        TapTarget.forToolbarMenuItem(mToolbar, R.id.tonalita
                                , getString(R.string.action_tonalita), getString(R.string.sc_tonalita_desc))
                                // All options below are optional
                                .outerCircleColorInt(getThemeUtils().primaryColor())     // Specify a color for the outer circle
                                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                                .textTypeface(Typeface.createFromAsset(getResources().getAssets(),"fonts/Roboto-Regular.ttf"))  // Specify a typeface for the text
                                .id(1)
                        ,
                        TapTarget.forToolbarMenuItem(mToolbar, R.id.barre
                                , getString(R.string.action_barre), getString(R.string.sc_barre_desc))
                                // All options below are optional
                                .outerCircleColorInt(getThemeUtils().primaryColor())     // Specify a color for the outer circle
                                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                                .textTypeface(Typeface.createFromAsset(getResources().getAssets(),"fonts/Roboto-Regular.ttf"))  // Specify a typeface for the text
                                .id(2)
                        ,
                        TapTarget.forView(play_scroll
                                , getString(R.string.sc_scroll_title), getString(R.string.sc_scroll_desc))
                                // All options below are optional
                                .outerCircleColorInt(getThemeUtils().primaryColor())     // Specify a color for the outer circle
                                .targetCircleColorInt(Color.WHITE)   // Specify a color for the target circle
                                .textTypeface(Typeface.createFromAsset(getResources().getAssets(),"fonts/Roboto-Regular.ttf"))  // Specify a typeface for the text
                                .id(3)
                        ,
                        TapTarget.forToolbarOverflow(mToolbar
                                , getString(R.string.showcase_end_title), getString(R.string.showcase_help_general))
                                // All options below are optional
                                .outerCircleColorInt(getThemeUtils().primaryColor())     // Specify a color for the outer circle
                                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                                .textTypeface(Typeface.createFromAsset(getResources().getAssets(),"fonts/Roboto-Regular.ttf"))  // Specify a typeface for the text
                                .id(4)
                )
                .listener(
                        new TapTargetSequence.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                            @Override
                            public void onSequenceFinish() {
//                                Log.d(TAG, "onSequenceFinish: ");
                                SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(PaginaRenderActivity.this).edit();
                                prefEditor.putBoolean(Utility.INTRO_PAGINARENDER, true);
                                prefEditor.apply();
                                findViewById(R.id.music_controls).setVisibility(mostraAudioBool ? View.VISIBLE : View.GONE);

                            }

                            @Override
                            public void onSequenceStep(TapTarget tapTarget, boolean b) {}

                            @Override
                            public void onSequenceCanceled(TapTarget tapTarget) {
//                                Log.d(TAG, "onSequenceCanceled: ");
                                SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(PaginaRenderActivity.this).edit();
                                prefEditor.putBoolean(Utility.INTRO_PAGINARENDER, true);
                                prefEditor.apply();
                                findViewById(R.id.music_controls).setVisibility(mostraAudioBool ? View.VISIBLE : View.GONE);
                            }
                        }).start();
    }

    private void playIntroFull() {
        findViewById(R.id.music_controls).setVisibility(View.VISIBLE);
        new TapTargetSequence(PaginaRenderActivity.this)
                .continueOnCancel(true)
                .targets(
                        TapTarget.forToolbarMenuItem(mToolbar, R.id.tonalita
                                , getString(R.string.action_tonalita), getString(R.string.sc_tonalita_desc))
                                // All options below are optional
                                .outerCircleColorInt(getThemeUtils().primaryColor())     // Specify a color for the outer circle
                                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                                .textTypeface(Typeface.createFromAsset(getResources().getAssets(),"fonts/Roboto-Regular.ttf"))  // Specify a typeface for the text
                                .id(1)
                        ,
                        TapTarget.forToolbarMenuItem(mToolbar, R.id.barre
                                , getString(R.string.action_barre), getString(R.string.sc_barre_desc))
                                // All options below are optional
                                .outerCircleColorInt(getThemeUtils().primaryColor())     // Specify a color for the outer circle
                                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                                .textTypeface(Typeface.createFromAsset(getResources().getAssets(),"fonts/Roboto-Regular.ttf"))  // Specify a typeface for the text
                                .id(2)
                        ,
                        TapTarget.forView(play_button
                                , getString(R.string.sc_audio_title), getString(R.string.sc_audio_desc))
                                // All options below are optional
                                .outerCircleColorInt(getThemeUtils().primaryColor())     // Specify a color for the outer circle
                                .textTypeface(Typeface.createFromAsset(getResources().getAssets(),"fonts/Roboto-Regular.ttf"))  // Specify a typeface for the text
                                .id(3)
                        ,
                        TapTarget.forView(play_scroll
                                , getString(R.string.sc_scroll_title), getString(R.string.sc_scroll_desc))
                                // All options below are optional
                                .outerCircleColorInt(getThemeUtils().primaryColor())     // Specify a color for the outer circle
                                .targetCircleColorInt(Color.WHITE)   // Specify a color for the target circle
                                .textTypeface(Typeface.createFromAsset(getResources().getAssets(),"fonts/Roboto-Regular.ttf"))  // Specify a typeface for the text
                                .id(4)
                        ,
                        TapTarget.forToolbarOverflow(mToolbar
                                , getString(R.string.showcase_end_title), getString(R.string.showcase_help_general))
                                // All options below are optional
                                .outerCircleColorInt(getThemeUtils().primaryColor())     // Specify a color for the outer circle
                                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                                .textTypeface(Typeface.createFromAsset(getResources().getAssets(),"fonts/Roboto-Regular.ttf"))  // Specify a typeface for the text
                                .id(5)
                )
                .listener(
                        new TapTargetSequence.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                            @Override
                            public void onSequenceFinish() {
//                                Log.d(TAG, "onSequenceFinish: ");
                                SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(PaginaRenderActivity.this).edit();
                                prefEditor.putBoolean(Utility.INTRO_PAGINARENDER, true);
                                prefEditor.apply();
                                findViewById(R.id.music_controls).setVisibility(mostraAudioBool ? View.VISIBLE : View.GONE);

                            }

                            @Override
                            public void onSequenceStep(TapTarget tapTarget, boolean b) {}

                            @Override
                            public void onSequenceCanceled(TapTarget tapTarget) {
//                                Log.d(TAG, "onSequenceCanceled: ");
                                SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(PaginaRenderActivity.this).edit();
                                prefEditor.putBoolean(Utility.INTRO_PAGINARENDER, true);
                                prefEditor.apply();
                                findViewById(R.id.music_controls).setVisibility(mostraAudioBool ? View.VISIBLE : View.GONE);
                            }
                        }).start();
    }

    @Override
    public MediaBrowserCompat getMediaBrowser() {
        return mMediaBrowser;
    }

    private void playMedia() {
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
        if (controller != null) {
            controller.getTransportControls().play();
        }
    }

    private void pauseMedia() {
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
        if (controller != null) {
            controller.getTransportControls().pause();
        }
    }

    private void playFromId(String id) {
        new SimpleDialogFragment.Builder(PaginaRenderActivity.this, PaginaRenderActivity.this, "BUFFERING")
                .content(R.string.wait)
                .showProgress()
                .progressIndeterminate(true)
                .progressMax(0)
                .show()
                .setCancelable(true);
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
        if (controller != null) {
            controller.getTransportControls().playFromMediaId(id, null);
        }
    }

    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

    private void scheduleSeekbarUpdate() {
        stopSeekbarUpdate();
        if (!mExecutorService.isShutdown()) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            mHandler.post(mUpdateProgressTask);
                        }
                    }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
        }
    }

    private void stopSeekbarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
    }

    private void updateProgress() {
        if (mLastPlaybackState == null) {
            return;
        }
        long currentPosition = mLastPlaybackState.getPosition();
//        Log.d(TAG, "updateProgress: " + currentPosition);
        LogHelper.d(TAG, "updateProgress: ", currentPosition);
        if (mLastPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            // Calculate the elapsed time between the last position update and now and unless
            // paused, we can assume (delta * speed) + current position is approximately the
            // latest position. This ensure that we do not repeatedly call the getPlaybackState()
            // on MediaControllerCompat.
            long timeDelta = SystemClock.elapsedRealtime() -
                    mLastPlaybackState.getLastPositionUpdateTime();
            currentPosition += (int) timeDelta * mLastPlaybackState.getPlaybackSpeed();
        }
        scroll_song_bar.setEnabled(true);
        scroll_song_bar.setProgress((int) currentPosition);
    }
}