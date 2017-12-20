package it.cammino.risuscito;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.folderselector.FileChooserDialog;
import com.blunderer.easyanimatedvectordrawable.EasyAnimatedVectorDrawable;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.cammino.risuscito.database.RisuscitoDatabase;
import it.cammino.risuscito.database.dao.CantoDao;
import it.cammino.risuscito.database.dao.LocalLinksDao;
import it.cammino.risuscito.database.entities.LocalLink;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.playback.MusicService;
import it.cammino.risuscito.services.DownloadService;
import it.cammino.risuscito.services.PdfExportService;
import it.cammino.risuscito.ui.BottomSheetFabCanto;
import it.cammino.risuscito.ui.ThemeableActivity;
import it.cammino.risuscito.viewmodels.PaginaRenderViewModel;
import pub.devrel.easypermissions.EasyPermissions;

public class PaginaRenderActivity extends ThemeableActivity
    implements SimpleDialogFragment.SimpleCallback, FileChooserDialog.FileCallback {

  private static final long PROGRESS_UPDATE_INTERNAL = 1000;
  private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;
  public final CambioAccordi cambioAccordi = new CambioAccordi(this);
  final String TAG = getClass().getCanonicalName();
  private final ScheduledExecutorService mExecutorService =
      Executors.newSingleThreadScheduledExecutor();
  public boolean mostraAudioBool;
  public boolean mDownload;

  @BindView(R.id.risuscito_toolbar)
  Toolbar mToolbar;

  @BindView(R.id.cantoView)
  WebView paginaView;

  @BindView(R.id.play_song)
  ImageView play_button;

  @BindView(R.id.no_record)
  View no_records_text;

  @BindView(R.id.music_buttons)
  View music_buttons;

  @BindView(R.id.no_connection)
  View no_connection_text;

  @BindView(R.id.play_scroll)
  ImageButton play_scroll;

  @BindView(R.id.speed_seekbar)
  SeekBar scroll_speed_bar;

  @BindView(R.id.music_seekbar)
  SeekBar scroll_song_bar;

  @BindView(R.id.fab_canti)
  FloatingActionButton mFab;
  //    private String notaSalvata;
  //  public static String notaCambio;
  //  public static String speedValue;
  //  public static boolean scrollPlaying;
  //  public static String mostraAudio;
  //    private String barreSalvato;
  //  private static String barreCambio;
  private PaginaRenderViewModel mViewModel;
  //    private DatabaseCanti listaCanti;
  //  private Canto mCurrentCanto;
  private String pagina;
  private int idCanto;
  private String url;
  private String primaNota;
  private String primoBarre;
  private String personalUrl, localUrl;
  private PlaybackStateCompat mLastPlaybackState;
  private final Runnable mUpdateProgressTask =
      new Runnable() {
        @Override
        public void run() {
          updateProgress();
        }
      };
  private ScheduledFuture<?> mScheduleFuture;
  //  private int defaultZoomLevel = 0;
  //  private int defaultScrollX = 0;
  //  private int defaultScrollY = 0;
  private Handler mHandler = new Handler();
  final Runnable mScrollDown =
      new Runnable() {
        public void run() {
          if (paginaView != null && mViewModel.speedValue != null) {
            try {
              paginaView.scrollBy(0, Integer.valueOf(mViewModel.speedValue));
            } catch (NumberFormatException e) {
              paginaView.scrollBy(0, 0);
            }
            mHandler.postDelayed(this, 700);
          } else Log.d(TAG, "attività chiusa o annullato lo scroll");
        }
      };
  // Callback that ensures that we are showing the controls
  private final MediaControllerCompat.Callback mMediaControllerCallback =
      new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
          Log.d(TAG, "onPlaybackStateChanged: a " + state.getState());
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
              SimpleDialogFragment sFragment =
                  SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "BUFFERING");
              if (sFragment != null) sFragment.dismiss();
              stopSeekbarUpdate();
              scroll_song_bar.setProgress(0);
              scroll_song_bar.setEnabled(false);
              showPlaying(false);
              Log.e(TAG, "onPlaybackStateChanged: " + state.getErrorMessage());
              Snackbar.make(
                      findViewById(android.R.id.content),
                      state.getErrorMessage(),
                      Snackbar.LENGTH_SHORT)
                  .show();
              break;
            case PlaybackStateCompat.STATE_PLAYING:
              sFragment = SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "BUFFERING");
              if (sFragment != null) sFragment.dismiss();
              scheduleSeekbarUpdate();
              showPlaying(true);
              scroll_song_bar.setEnabled(true);
              break;
          }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
          Log.d(TAG, "onMetadataChanged");
          if (metadata != null) {
            int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
            //                    mEnd.setText(DateUtils.formatElapsedTime(duration/1000));
            scroll_song_bar.setMax(duration);
            scroll_song_bar.setEnabled(true);
          }
        }
      };
  //  private int savedSpeed;
  private LUtils mLUtils;
  private MediaBrowserCompat mMediaBrowser;
  private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
      new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
          Log.d(TAG, "onConnected");
          try {
            MediaControllerCompat mediaController =
                new MediaControllerCompat(
                    PaginaRenderActivity.this, mMediaBrowser.getSessionToken());
            MediaControllerCompat.setMediaController(PaginaRenderActivity.this, mediaController);
            mediaController.registerCallback(mMediaControllerCallback);
            mLastPlaybackState = mediaController.getPlaybackState();
            if (mLastPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
              scheduleSeekbarUpdate();
            }
            showPlaying(mLastPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING);
            scroll_song_bar.setEnabled(
                mLastPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING
                    || mLastPlaybackState.getState() == PlaybackStateCompat.STATE_PAUSED);

            if (mediaController.getMetadata() != null) {
              Log.d(
                  TAG,
                  "onConnected: duration "
                      + mediaController
                          .getMetadata()
                          .getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
              scroll_song_bar.setMax(
                  (int)
                      mediaController
                          .getMetadata()
                          .getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
            }
            Log.d(
                TAG,
                "onConnected: mLastPlaybackState.getPosition() "
                    + mLastPlaybackState.getPosition());
            scroll_song_bar.setProgress((int) mLastPlaybackState.getPosition());
          } catch (RemoteException e) {
            Log.e(TAG, "onConnected: could not connect media controller", e);
          }
        }

        @Override
        public void onConnectionFailed() {
          Log.e(TAG, "onConnectionFailed");
        }

        @Override
        public void onConnectionSuspended() {
          Log.d(TAG, "onConnectionSuspended");
          MediaControllerCompat mediaController =
              MediaControllerCompat.getMediaController(PaginaRenderActivity.this);
          if (mediaController != null) {
            mediaController.unregisterCallback(mMediaControllerCallback);

            MediaControllerCompat.setMediaController(PaginaRenderActivity.this, null);
          }
        }
      };
  private BroadcastReceiver downloadPosBRec =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          // Implement UI change code here once notification is received
          try {
            Log.v(TAG, "BROADCAST_DOWNLOAD_PROGRESS");
            Log.v(TAG, "DATA_PROGRESS: " + intent.getIntExtra(DownloadService.DATA_PROGRESS, 0));
            SimpleDialogFragment sFragment =
                SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "DOWNLOAD_MP3");
            if (sFragment != null) {
              sFragment.setProgress(intent.getIntExtra(DownloadService.DATA_PROGRESS, 0));
            }
          } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
          }
        }
      };
  private BroadcastReceiver downloadCompletedBRec =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          // Implement UI change code here once notification is received
          try {
            Log.d(TAG, "BROADCAST_DOWNLOAD_COMPLETED");
            SimpleDialogFragment sFragment =
                SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "DOWNLOAD_MP3");
            if (sFragment != null) sFragment.dismiss();
            SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(PaginaRenderActivity.this);
            int saveLocation = Integer.parseInt(pref.getString(Utility.SAVE_LOCATION, "0"));
            if (saveLocation == 1) {
              // initiate media scan and put the new things into the path array to
              // make the scanner aware of the location and the files you want to see
              MediaScannerConnection.scanFile(
                  context,
                  new String[] {
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                            .getAbsolutePath()
                        + "/Risuscitò/"
                        + Utility.filterMediaLinkNew(url)
                  },
                  null,
                  null);
            }
            Snackbar.make(
                    findViewById(android.R.id.content),
                    R.string.download_completed,
                    Snackbar.LENGTH_SHORT)
                .show();
            stopMedia();
            refreshCatalog();
            //            checkRecordsState();
            new RecordStateCheckerTask().execute();
          } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
          }
        }
      };
  private BroadcastReceiver downloadErrorBRec =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          // Implement UI change code here once notification is received
          try {
            Log.d(TAG, "BROADCAST_DOWNLOAD_ERROR");
            Log.d(TAG, "DATA_ERROR: " + intent.getStringExtra(DownloadService.DATA_ERROR));
            SimpleDialogFragment sFragment =
                SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "DOWNLOAD_MP3");
            if (sFragment != null) sFragment.dismiss();
            Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.download_error)
                        + " "
                        + intent.getStringExtra(DownloadService.DATA_ERROR),
                    Snackbar.LENGTH_SHORT)
                .show();
          } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
          }
        }
      };
  private BroadcastReceiver exportCompleted =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          // Implement UI change code here once notification is received
          Log.d(TAG, "BROADCAST_EXPORT_COMPLETED");
          Log.d(TAG, "DATA_PDF_PATH: " + intent.getStringExtra(PdfExportService.DATA_PDF_PATH));
          SimpleDialogFragment sFragment =
              SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "EXPORT_PDF");
          if (sFragment != null) sFragment.dismiss();
          String localPDFPath = intent.getStringExtra(PdfExportService.DATA_PDF_PATH);
          File file = new File(localPDFPath);
          Intent target = new Intent(Intent.ACTION_VIEW);
          Uri pdfUri =
              FileProvider.getUriForFile(
                  PaginaRenderActivity.this, "it.cammino.risuscito.fileprovider", file);
          Log.d(TAG, "pdfUri: " + pdfUri);
          target.setDataAndType(pdfUri, "application/pdf");
          target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);
          Intent intent2 = Intent.createChooser(target, getString(R.string.open_pdf));
          try {
            startActivity(intent2);
          } catch (ActivityNotFoundException e) {
            Snackbar.make(
                    findViewById(android.R.id.content),
                    R.string.no_pdf_reader,
                    Snackbar.LENGTH_SHORT)
                .show();
          }
        }
      };
  private BroadcastReceiver exportError =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          // Implement UI change code here once notification is received
          try {
            Log.d(TAG, "BROADCAST_EXPORT_ERROR");
            Log.d(
                TAG,
                "DATA_EXPORT_ERROR: " + intent.getStringExtra(PdfExportService.DATA_EXPORT_ERROR));
            SimpleDialogFragment sFragment =
                SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "EXPORT_PDF");
            if (sFragment != null) sFragment.dismiss();
            Snackbar.make(
                    findViewById(android.R.id.content),
                    intent.getStringExtra(PdfExportService.DATA_EXPORT_ERROR),
                    Snackbar.LENGTH_SHORT)
                .show();
          } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
          }
        }
      };
  private BroadcastReceiver fabBRec =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          // Implement UI change code here once notification is received
          int clickedId = intent.getIntExtra(BottomSheetFabCanto.DATA_ITEM_ID, 0);
          switch (clickedId) {
            case BottomSheetFabCanto.FULLSCREEN:
              mHandler.removeCallbacks(mScrollDown);
              saveZoom(false, false);
              Bundle bundle = new Bundle();
              bundle.putString(Utility.URL_CANTO, paginaView.getUrl());
              bundle.putInt(Utility.SPEED_VALUE, scroll_speed_bar.getProgress());
              bundle.putBoolean(Utility.SCROLL_PLAYING, mViewModel.scrollPlaying);
              bundle.putInt(Utility.ID_CANTO, idCanto);

              Intent intent2 = new Intent(PaginaRenderActivity.this, PaginaRenderFullScreen.class);
              intent2.putExtras(bundle);
              mLUtils.startActivityWithFadeIn(intent2);
              break;
            case BottomSheetFabCanto.SOUND:
              findViewById(R.id.music_controls)
                  .setVisibility(mostraAudioBool ? View.GONE : View.VISIBLE);
              mostraAudioBool = !mostraAudioBool;
              mViewModel.mostraAudio = String.valueOf(mostraAudioBool);
              break;
            case BottomSheetFabCanto.SAVE_FILE:
              if (!url.isEmpty()) {
                if (mDownload) {
                  if (personalUrl.isEmpty()) {
                    new SimpleDialogFragment.Builder(
                            PaginaRenderActivity.this, PaginaRenderActivity.this, "DELETE_MP3")
                        .title(R.string.dialog_delete_mp3_title)
                        .content(R.string.dialog_delete_mp3)
                        .positiveButton(android.R.string.yes)
                        .negativeButton(android.R.string.no)
                        .show();
                  } else {
                    new SimpleDialogFragment.Builder(
                            PaginaRenderActivity.this, PaginaRenderActivity.this, "DELETE_LINK")
                        .title(R.string.dialog_delete_link_title)
                        .content(R.string.dialog_delete_link)
                        .positiveButton(android.R.string.yes)
                        .negativeButton(android.R.string.no)
                        .show();
                  }
                } else {
                  new SimpleDialogFragment.Builder(
                          PaginaRenderActivity.this, PaginaRenderActivity.this, "DOWNLINK_CHOOSE")
                      .title(R.string.download_link_title)
                      .content(R.string.downlink_message)
                      .positiveButton(R.string.downlink_download)
                      .negativeButton(R.string.downlink_choose)
                      .neutralButton(android.R.string.cancel)
                      .show();
                }
              } else {
                if (mDownload) {
                  new SimpleDialogFragment.Builder(
                          PaginaRenderActivity.this, PaginaRenderActivity.this, "DELETE_LINK")
                      .title(R.string.dialog_delete_link_title)
                      .content(R.string.dialog_delete_link)
                      .positiveButton(android.R.string.yes)
                      .negativeButton(android.R.string.no)
                      .show();
                } else {
                  new SimpleDialogFragment.Builder(
                          PaginaRenderActivity.this, PaginaRenderActivity.this, "ONLY_LINK")
                      .title(R.string.only_link_title)
                      .content(R.string.only_link)
                      .positiveButton(android.R.string.yes)
                      .negativeButton(android.R.string.no)
                      .show();
                }
              }
              break;
            case BottomSheetFabCanto.FAVORITE:
              //              boolean favoriteYet = selectFavouriteFromSource() == 1;
              boolean favoriteYet = mViewModel.mCurrentCanto.favorite == 1;
              updateFavouriteFlag(favoriteYet ? 0 : 1);
              //              Snackbar.make(
              //                      findViewById(android.R.id.content),
              //                      !favoriteYet ? R.string.favorite_added :
              // R.string.favorite_removed,
              //                      Snackbar.LENGTH_SHORT)
              //                  .show();
              break;
            default:
              break;
          }
        }
      };
  private BroadcastReceiver catalogReadyBR =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          // Implement UI change code here once notification is received
          try {
            Log.d(TAG, MusicService.MSG_RETRIEVE_DONE);
            Boolean done = intent.getBooleanExtra(MusicService.MSG_RETRIEVE_DONE, false);
            Log.d(TAG, "MSG_RETRIEVE_DONE: " + done);
            showPlaying(false);
            play_button.setEnabled(done);

            Drawable playDrawable = play_button.getDrawable();
            playDrawable.setColorFilter(
                ContextCompat.getColor(
                    PaginaRenderActivity.this,
                    done ? R.color.text_color_secondary : R.color.text_color_secondary_disabled),
                PorterDuff.Mode.SRC_IN);

          } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
          }
        }
      };

  @OnClick(R.id.play_song)
  public void playPause() {
    MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
    PlaybackStateCompat stateObj = controller.getPlaybackState();
    final int state = stateObj == null ? PlaybackStateCompat.STATE_NONE : stateObj.getState();
    Log.d(TAG, "playPause: Button pressed, in state " + state);

    if (state == PlaybackStateCompat.STATE_STOPPED || state == PlaybackStateCompat.STATE_NONE) {
      playFromId(String.valueOf(idCanto));
    } else if (state == PlaybackStateCompat.STATE_PLAYING
        || state == PlaybackStateCompat.STATE_BUFFERING
        || state == PlaybackStateCompat.STATE_CONNECTING) {
      pauseMedia();
    } else if (state == PlaybackStateCompat.STATE_PAUSED) {
      playMedia();
    }
  }

  @OnClick(R.id.play_scroll)
  public void playPauseScroll(View v) {
    if (v.isSelected()) {
      showScrolling(false);
      mViewModel.scrollPlaying = false;
      mHandler.removeCallbacks(mScrollDown);
    } else {
      showScrolling(true);
      mViewModel.scrollPlaying = true;
      mScrollDown.run();
    }
  }

  @OnClick(R.id.fab_canti)
  public void FabOptions() {
    BottomSheetFabCanto bottomSheetDialog =
        BottomSheetFabCanto.newInstance(
            mostraAudioBool,
            mDownload,
            //            selectFavouriteFromSource() == 1,
            mViewModel.mCurrentCanto.favorite == 1,
            !url.equals(""),
            !personalUrl.equals(""));
    bottomSheetDialog.show(getSupportFragmentManager(), null);
  }

  @Override
  protected void onStart() {
    super.onStart();
    Log.d(TAG, "onStart: ");
    if (mMediaBrowser != null) {
      mMediaBrowser.connect();
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    Log.d(TAG, "onStop: ");
    if (mMediaBrowser != null) {
      mMediaBrowser.disconnect();
    }
    if (MediaControllerCompat.getMediaController(this) != null)
      MediaControllerCompat.getMediaController(this).unregisterCallback(mMediaControllerCallback);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_pagina_render);
    ButterKnife.bind(this);

    mViewModel = ViewModelProviders.of(this).get(PaginaRenderViewModel.class);

    ((TextView) findViewById(R.id.main_toolbarTitle)).setText(R.string.canto_title_activity);
    mToolbar.setBackgroundColor(getThemeUtils().primaryColor());
    setSupportActionBar(mToolbar);
    //noinspection ConstantConditions
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    findViewById(R.id.bottom_bar).setBackgroundColor(getThemeUtils().primaryColor());

    mLUtils = LUtils.getInstance(PaginaRenderActivity.this);

    //    listaCanti = new DatabaseCanti(this);

    IconicsDrawable icon =
        new IconicsDrawable(this)
            .icon(CommunityMaterial.Icon.cmd_plus)
            .color(Color.WHITE)
            .sizeDp(24)
            .paddingDp(4);
    mFab.setImageDrawable(icon);

    // recupera il numero della pagina da visualizzare dal parametro passato dalla chiamata
    Bundle bundle = this.getIntent().getExtras();
    pagina = bundle != null ? bundle.getCharSequence("pagina", "").toString() : null;
    idCanto = bundle != null ? bundle.getInt("idCanto") : 0;

    new DataRetrieverTask().execute(idCanto);

    try {
      primaNota =
          CambioAccordi.recuperaPrimoAccordo(
              getAssets().open(pagina + ".htm"),
              ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration())
                  .getLanguage());
      primoBarre =
          cambioAccordi.recuperaBarre(
              getAssets().open(pagina + ".htm"),
              ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration())
                  .getLanguage());
    } catch (IOException e) {
      Log.e(TAG, e.getLocalizedMessage(), e);
    }

    //    SQLiteDatabase db = listaCanti.getReadableDatabase();
    //
    //    String query =
    //        "SELECT saved_tab, saved_barre, saved_speed"
    //            + "  FROM ELENCO"
    //            + "  WHERE _id =  "
    //            + idCanto;
    //    Cursor cursor = db.rawQuery(query, null);
    //
    //    cursor.moveToFirst();
    //    notaSalvata = cursor.getString(0);
    //    barreSalvato = cursor.getString(1);
    //    savedSpeed = cursor.getInt(2);
    //    cursor.close();
    //    db.close();

    scroll_song_bar.setOnSeekBarChangeListener(
        new SeekBar.OnSeekBarChangeListener() {
          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            String time =
                String.format(
                    ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration()),
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(progress),
                    TimeUnit.MILLISECONDS.toSeconds(progress)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(progress)));
            ((TextView) findViewById(R.id.time_text)).setText(time);
          }

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {
            stopSeekbarUpdate();
          }

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {
            MediaControllerCompat.getMediaController(PaginaRenderActivity.this)
                .getTransportControls()
                .seekTo(seekBar.getProgress());
            scheduleSeekbarUpdate();
          }
        });

    scroll_speed_bar.setOnSeekBarChangeListener(
        new SeekBar.OnSeekBarChangeListener() {
          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mViewModel.speedValue = String.valueOf(progress);
            ((TextView) findViewById(R.id.slider_text))
                .setText(getString(R.string.percent_progress, progress));
            Log.d(getClass().toString(), "speedValue cambiato! " + mViewModel.speedValue);
          }

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {}

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {}
        });

    showScrolling(false);

    if (mViewModel.mostraAudio == null) {
      SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
      mViewModel.mostraAudio = String.valueOf(pref.getBoolean(Utility.SHOW_AUDIO, true));
    }
    mostraAudioBool = Boolean.parseBoolean(mViewModel.mostraAudio);

    SimpleDialogFragment sFragment =
        SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "DOWNLOAD_MP3");
    if (sFragment != null) sFragment.setmCallback(PaginaRenderActivity.this);
    sFragment = SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "DELETE_LINK");
    if (sFragment != null) sFragment.setmCallback(PaginaRenderActivity.this);
    sFragment = SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "DOWNLINK_CHOOSE");
    if (sFragment != null) sFragment.setmCallback(PaginaRenderActivity.this);
    sFragment = SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "DELETE_MP3");
    if (sFragment != null) sFragment.setmCallback(PaginaRenderActivity.this);
    sFragment = SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "ONLY_LINK");
    if (sFragment != null) sFragment.setmCallback(PaginaRenderActivity.this);
    sFragment = SimpleDialogFragment.findVisible(PaginaRenderActivity.this, "SAVE_TAB");
    if (sFragment != null) sFragment.setmCallback(PaginaRenderActivity.this);

    //    checkRecordsState();

    // Connect a media browser just to get the media session token. There are other ways
    // this can be done, for example by sharing the session token directly.
    mMediaBrowser =
        new MediaBrowserCompat(
            this, new ComponentName(this, MusicService.class), mConnectionCallback, null);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    IconicsMenuInflaterUtil.inflate(
        getMenuInflater(), PaginaRenderActivity.this, R.menu.canto, menu, true);
    super.onCreateOptionsMenu(menu);
    //        getMenuInflater().inflate(R.menu.canto, menu);
    //
    //        int iconColorId = R.color.text_color_secondary;
    //
    //        menu.findItem(R.id.tonalita).setIcon(
    //                new IconicsDrawable(PaginaRenderActivity.this,
    // CommunityMaterial.Icon.cmd_music_note).actionBar()
    //                        .sizeDp(24)
    //                        .paddingDp(2)
    //                        .color(Color.WHITE)
    //        );
    //        menu.findItem(R.id.action_trasporta).setIcon(
    //                new IconicsDrawable(PaginaRenderActivity.this,
    // CommunityMaterial.Icon.cmd_swap_vertical)
    //                        .sizeDp(24)
    //                        .paddingDp(2)
    ////                        .colorRes(iconColorId)
    //                        .color(ContextCompat.getColor(this, iconColorId))
    //        );
    //        menu.findItem(R.id.action_save_tab).setIcon(
    //                new IconicsDrawable(PaginaRenderActivity.this,
    // CommunityMaterial.Icon.cmd_content_save)
    //                        .sizeDp(24)
    //                        .paddingDp(2)
    ////                        .colorRes(iconColorId)
    //                        .color(ContextCompat.getColor(this, iconColorId))
    //        );
    //        menu.findItem(R.id.action_reset_tab).setIcon(
    //                new IconicsDrawable(PaginaRenderActivity.this,
    // CommunityMaterial.Icon.cmd_refresh)
    //                        .sizeDp(24)
    //                        .paddingDp(2)
    ////                        .colorRes(iconColorId)
    //                        .color(ContextCompat.getColor(this, iconColorId))
    //        );
    //        menu.findItem(R.id.barre).setIcon(
    //                new IconicsDrawable(PaginaRenderActivity.this,
    // CommunityMaterial.Icon.cmd_guitar_electric)
    //                        .sizeDp(24)
    //                        .paddingDp(2)
    //                        .color(Color.WHITE)
    //        );
    //        menu.findItem(R.id.action_trasporta_barre).setIcon(
    //                new IconicsDrawable(PaginaRenderActivity.this,
    // CommunityMaterial.Icon.cmd_swap_vertical)
    //                        .sizeDp(24)
    //                        .paddingDp(2)
    ////                        .colorRes(iconColorId)
    //                        .color(ContextCompat.getColor(this, iconColorId))
    //        );
    //        menu.findItem(R.id.action_save_barre).setIcon(
    //                new IconicsDrawable(PaginaRenderActivity.this,
    // CommunityMaterial.Icon.cmd_content_save)
    //                        .sizeDp(24)
    //                        .paddingDp(2)
    ////                        .colorRes(iconColorId)
    //                        .color(ContextCompat.getColor(this, iconColorId))
    //        );
    //        menu.findItem(R.id.action_reset_barre).setIcon(
    //                new IconicsDrawable(PaginaRenderActivity.this,
    // CommunityMaterial.Icon.cmd_refresh)
    //                        .sizeDp(24)
    //                        .paddingDp(2)
    ////                        .colorRes(iconColorId)
    //                        .color(ContextCompat.getColor(this, iconColorId))
    //        );
    //        menu.findItem(R.id.action_exp_pdf).setIcon(
    //                new IconicsDrawable(PaginaRenderActivity.this,
    // CommunityMaterial.Icon.cmd_file_pdf_box)
    //                        .sizeDp(24)
    //                        .paddingDp(2)
    //                        .color(Color.WHITE)
    //        );
    //        menu.findItem(R.id.action_help_canto).setIcon(
    //                new IconicsDrawable(PaginaRenderActivity.this,
    // CommunityMaterial.Icon.cmd_help_circle)
    //                        .sizeDp(24)
    //                        .paddingDp(2)
    //                        .color(Color.WHITE)
    //        );
    SharedPreferences mSharedPrefs =
        PreferenceManager.getDefaultSharedPreferences(PaginaRenderActivity.this);
    Log.d(
        TAG,
        "onCreateOptionsMenu - INTRO_PAGINARENDER: "
            + mSharedPrefs.getBoolean(Utility.INTRO_PAGINARENDER, false));
    if (!mSharedPrefs.getBoolean(Utility.INTRO_PAGINARENDER, false)) {
      final Handler handler = new Handler();
      handler.postDelayed(
          new Runnable() {
            @Override
            public void run() {
              // Do something after 5s = 5000ms
              if (music_buttons.getVisibility() == View.VISIBLE) playIntroFull();
              else playIntroSmall();
            }
          },
          1500);
    }

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        //                if (notaCambio == null || notaSalvata == null
        //                        || barreCambio == null || barreSalvato == null
        //                        || (notaCambio.equals(notaSalvata)
        //                        && barreCambio.equals(barreSalvato))) {
        if (mViewModel.notaCambio == null
            || mViewModel.mCurrentCanto.savedTab == null
            || mViewModel.barreCambio == null
            || mViewModel.mCurrentCanto.savedBarre == null
            || (mViewModel.notaCambio.equals(mViewModel.mCurrentCanto.savedTab)
                && mViewModel.barreCambio.equals(mViewModel.mCurrentCanto.savedBarre))) {
          if (mViewModel.scrollPlaying) {
            showScrolling(false);
            mHandler.removeCallbacks(mScrollDown);
          }
          saveZoom(true, false);
          //          pulisciVars(false);
          mLUtils.closeActivityWithTransition();
          return true;
        } else {
          new SimpleDialogFragment.Builder(
                  PaginaRenderActivity.this, PaginaRenderActivity.this, "SAVE_TAB")
              .title(R.string.dialog_save_tab_title)
              .content(R.string.dialog_save_tab)
              .positiveButton(android.R.string.yes)
              .negativeButton(android.R.string.no)
              .show();
          break;
        }
      case R.id.action_exp_pdf:
        new SimpleDialogFragment.Builder(
                PaginaRenderActivity.this, PaginaRenderActivity.this, "EXPORT_PDF")
            .content(R.string.export_running)
            .showProgress()
            .progressIndeterminate(true)
            .progressMax(0)
            .show()
            .setCancelable(true);
        Intent i = new Intent(getApplicationContext(), PdfExportService.class);
        i.putExtra(PdfExportService.DATA_PRIMA_NOTA, primaNota);
        i.putExtra(PdfExportService.DATA_NOTA_CAMBIO, mViewModel.notaCambio);
        i.putExtra(PdfExportService.DATA_PRIMO_BARRE, primoBarre);
        i.putExtra(PdfExportService.DATA_BARRE_CAMBIO, mViewModel.barreCambio);
        i.putExtra(PdfExportService.DATA_PAGINA, pagina);
        i.putExtra(
            PdfExportService.DATA_LINGUA,
            ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration())
                .getLanguage());
        startService(i);
        return true;
      case R.id.action_help_canto:
        if (music_buttons.getVisibility() == View.VISIBLE) playIntroFull();
        else playIntroSmall();
        return true;
      case R.id.action_save_tab:
        //                if (!notaSalvata.equalsIgnoreCase(notaCambio)) {
        //                    notaSalvata = notaCambio;
        //                    SQLiteDatabase db = listaCanti.getReadableDatabase();
        //                    String sql = "UPDATE ELENCO" +
        //                            "  SET saved_tab = \'" + notaCambio + "\' " +
        //                            "  WHERE _id =  " + idCanto;
        //                    db.execSQL(sql);
        //                    db.close();
        //                    Snackbar.make(findViewById(android.R.id.content)
        //                            , R.string.tab_saved
        //                            , Snackbar.LENGTH_SHORT)
        //                            .show();
        if (!mViewModel.mCurrentCanto.savedTab.equalsIgnoreCase(mViewModel.notaCambio)) {
          mViewModel.mCurrentCanto.savedTab = mViewModel.notaCambio;
          new Thread(
                  new Runnable() {
                    @Override
                    public void run() {
                      CantoDao mDao =
                          RisuscitoDatabase.getInstance(PaginaRenderActivity.this).cantoDao();
                      mDao.updateCanto(mViewModel.mCurrentCanto);
                      Snackbar.make(
                              findViewById(android.R.id.content),
                              R.string.tab_saved,
                              Snackbar.LENGTH_SHORT)
                          .show();
                    }
                  })
              .start();
        } else {
          Snackbar.make(
                  findViewById(android.R.id.content), R.string.tab_not_saved, Snackbar.LENGTH_SHORT)
              .show();
        }
        return true;
      case R.id.action_reset_tab:
        mViewModel.notaCambio = primaNota;
        HashMap<String, String> convMap =
            cambioAccordi.diffSemiToni(primaNota, mViewModel.notaCambio);
        HashMap<String, String> convMin = null;
        if (ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration())
            .getLanguage()
            .equalsIgnoreCase("uk"))
          convMin = cambioAccordi.diffSemiToniMin(primaNota, mViewModel.notaCambio);
        saveZoom(false, false);
        if (convMap != null) {
          String nuovoFile = cambiaAccordi(convMap, mViewModel.barreCambio, convMin, true);
          if (nuovoFile != null) paginaView.loadUrl("file://" + nuovoFile);
        } else {
          paginaView.loadUrl("file:///android_asset/" + pagina + ".htm");
        }
        //        if (defaultZoomLevel > 0) paginaView.setInitialScale(defaultZoomLevel);
        if (mViewModel.mCurrentCanto.zoom > 0)
          paginaView.setInitialScale(mViewModel.mCurrentCanto.zoom);
        paginaView.setWebViewClient(new MyWebViewClient());
        return true;
      case R.id.action_save_barre:
        //        if (!barreSalvato.equalsIgnoreCase(barreCambio)) {
        //          barreSalvato = barreCambio;
        //          SQLiteDatabase db = listaCanti.getReadableDatabase();
        //          String sql =
        //              "UPDATE ELENCO"
        //                  + "  SET saved_barre = \'"
        //                  + barreCambio
        //                  + "\' "
        //                  + "  WHERE _id =  "
        //                  + idCanto;
        //          db.execSQL(sql);
        //          db.close();
        //          Snackbar.make(
        //                  findViewById(android.R.id.content), R.string.barre_saved,
        // Snackbar.LENGTH_SHORT)
        //              .show();
        if (!mViewModel.mCurrentCanto.savedBarre.equalsIgnoreCase(mViewModel.barreCambio)) {
          mViewModel.mCurrentCanto.savedBarre = mViewModel.barreCambio;
          new Thread(
                  new Runnable() {
                    @Override
                    public void run() {
                      CantoDao mDao =
                          RisuscitoDatabase.getInstance(PaginaRenderActivity.this).cantoDao();
                      mDao.updateCanto(mViewModel.mCurrentCanto);
                      Snackbar.make(
                              findViewById(android.R.id.content),
                              R.string.barre_saved,
                              Snackbar.LENGTH_SHORT)
                          .show();
                    }
                  })
              .start();
        } else {
          Snackbar.make(
                  findViewById(android.R.id.content),
                  R.string.barre_not_saved,
                  Snackbar.LENGTH_SHORT)
              .show();
        }
        return true;
      case R.id.action_reset_barre:
        mViewModel.barreCambio = primoBarre;
        HashMap<String, String> convMap1 =
            cambioAccordi.diffSemiToni(primaNota, mViewModel.notaCambio);
        HashMap<String, String> convMin1 = null;
        if (ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration())
            .getLanguage()
            .equalsIgnoreCase("uk"))
          convMin1 = cambioAccordi.diffSemiToniMin(primaNota, mViewModel.notaCambio);
        saveZoom(false, false);
        if (convMap1 != null) {
          String nuovoFile = cambiaAccordi(convMap1, mViewModel.barreCambio, convMin1, true);
          if (nuovoFile != null) paginaView.loadUrl("file://" + nuovoFile);
        } else {
          paginaView.loadUrl("file:///android_asset/" + pagina + ".htm");
        }
        //        if (defaultZoomLevel > 0) paginaView.setInitialScale(defaultZoomLevel);
        if (mViewModel.mCurrentCanto.zoom > 0)
          paginaView.setInitialScale(mViewModel.mCurrentCanto.zoom);
        paginaView.setWebViewClient(new MyWebViewClient());
        return true;
      default:
        if (item.getGroupId() == R.id.menu_gruppo_note) {
          mViewModel.notaCambio = String.valueOf(item.getTitleCondensed());
          HashMap<String, String> convMap2 =
              cambioAccordi.diffSemiToni(primaNota, mViewModel.notaCambio);
          HashMap<String, String> convMin2 = null;
          if (ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration())
              .getLanguage()
              .equalsIgnoreCase("uk"))
            convMin2 = cambioAccordi.diffSemiToniMin(primaNota, mViewModel.notaCambio);
          saveZoom(false, false);
          if (convMap2 != null) {
            String nuovoFile = cambiaAccordi(convMap2, mViewModel.barreCambio, convMin2, true);
            if (nuovoFile != null) paginaView.loadUrl("file://" + nuovoFile);
          } else {
            paginaView.loadUrl("file:///android_asset/" + pagina + ".htm");
          }
          //          if (defaultZoomLevel > 0) paginaView.setInitialScale(defaultZoomLevel);
          if (mViewModel.mCurrentCanto.zoom > 0)
            paginaView.setInitialScale(mViewModel.mCurrentCanto.zoom);
          paginaView.setWebViewClient(new MyWebViewClient());
          return true;
        }
        if (item.getGroupId() == R.id.menu_gruppo_barre) {
          mViewModel.barreCambio = String.valueOf(item.getTitleCondensed());
          HashMap<String, String> convMap3 =
              cambioAccordi.diffSemiToni(primaNota, mViewModel.notaCambio);
          HashMap<String, String> convMin3 = null;
          if (ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration())
              .getLanguage()
              .equalsIgnoreCase("uk"))
            convMin3 = cambioAccordi.diffSemiToniMin(primaNota, mViewModel.notaCambio);
          saveZoom(false, false);
          if (convMap3 != null) {
            String nuovoFile = cambiaAccordi(convMap3, mViewModel.barreCambio, convMin3, true);
            if (nuovoFile != null) paginaView.loadUrl("file://" + nuovoFile);
          } else {
            paginaView.loadUrl("file:///android_asset/" + pagina + ".htm");
          }
          //          if (defaultZoomLevel > 0) paginaView.setInitialScale(defaultZoomLevel);
          if (mViewModel.mCurrentCanto.zoom > 0)
            paginaView.setInitialScale(mViewModel.mCurrentCanto.zoom);
          paginaView.setWebViewClient(new MyWebViewClient());
          return true;
        }
    }
    return false;
  }

  @Override
  public void onBackPressed() {
    Log.d(TAG, "onBackPressed: ");
    //    if (notaCambio == null
    //        || notaSalvata == null
    //        || barreCambio == null
    //        || barreSalvato == null
    //        || (notaCambio.equals(notaSalvata) && barreCambio.equals(barreSalvato))) {
    if (mViewModel.notaCambio == null
        || mViewModel.mCurrentCanto.savedTab == null
        || mViewModel.barreCambio == null
        || mViewModel.mCurrentCanto.savedBarre == null
        || (mViewModel.notaCambio.equals(mViewModel.mCurrentCanto.savedTab)
            && mViewModel.barreCambio.equals(mViewModel.mCurrentCanto.savedBarre))) {
      if (mViewModel.scrollPlaying) {
        showScrolling(false);
        mHandler.removeCallbacks(mScrollDown);
      }
      saveZoom(true, false);
      //      pulisciVars(false);
      mLUtils.closeActivityWithTransition();
    } else {
      new SimpleDialogFragment.Builder(
              PaginaRenderActivity.this, PaginaRenderActivity.this, "SAVE_TAB")
          .title(R.string.dialog_save_tab_title)
          .content(R.string.dialog_save_tab)
          .positiveButton(android.R.string.yes)
          .negativeButton(android.R.string.no)
          .show();
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    Log.d(TAG, "onResume: ");
    //    new DataRetrieverTask().execute(idCanto);

    //        if (mCastContext != null) {
    //            mCastContext.addCastStateListener(mCastStateListener);
    //        }

    //    if (notaSalvata == null) {
    //      if (notaCambio == null) notaSalvata = notaCambio = primaNota;
    //      else notaSalvata = primaNota;
    //    } else {
    //      //	    	Log.i("NOTA SALVATA", notaSalvata);
    //      //	    	Log.i("AVVIO", "notaCambio = " + notaCambio);
    //      if (notaCambio == null) notaCambio = notaSalvata;
    //    }
    //
    //    if (barreSalvato == null) {
    //      if (barreCambio == null) barreSalvato = barreCambio = primoBarre;
    //      else barreSalvato = primoBarre;
    //    } else {
    //      //	    	Log.i("BARRESALVATO", barreSalvato);
    //      if (barreCambio == null) barreCambio = barreSalvato;
    //    }
    //
    //    getZoom();
    //
    //    // fix per crash su android 4.1
    //    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN)
    //      paginaView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    //
    //    HashMap<String, String> convMap = cambioAccordi.diffSemiToni(primaNota, notaCambio);
    //    HashMap<String, String> convMin = null;
    //    if (ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration())
    //        .getLanguage()
    //        .equalsIgnoreCase("uk")) convMin = cambioAccordi.diffSemiToniMin(primaNota,
    // notaCambio);
    //    if (convMap != null) {
    //      String nuovoFile = cambiaAccordi(convMap, barreCambio, convMin, true);
    //      if (nuovoFile != null) paginaView.loadUrl("file://" + nuovoFile);
    //    } else paginaView.loadUrl("file:///android_asset/" + pagina + ".htm");
    //
    //    WebSettings webSettings = paginaView.getSettings();
    //    webSettings.setUseWideViewPort(true);
    //    webSettings.setSupportZoom(true);
    //    webSettings.setLoadWithOverviewMode(true);
    //
    //    webSettings.setBuiltInZoomControls(true);
    //    webSettings.setDisplayZoomControls(false);
    //
    //    if (defaultZoomLevel > 0) paginaView.setInitialScale(defaultZoomLevel);
    //    paginaView.setWebViewClient(new MyWebViewClient());
    //
    //    if (speedValue == null) {
    //      //	    	Log.i("SONO APPENA ENTRATO", "setto " + savedSpeed);
    //      scroll_speed_bar.setProgress(savedSpeed);
    //    } else {
    //      //	    	Log.i("ROTAZIONE", "setto " + speedValue);
    //      scroll_speed_bar.setProgress(Integer.valueOf(speedValue));
    //    }
    //
    //    //	    Log.i(this.getClass().toString(), "scrollPlaying? " + scrollPlaying);
    //    if (scrollPlaying) {
    //      showScrolling(true);
    //      mScrollDown.run();
    //    }

    findViewById(R.id.music_controls).setVisibility(mostraAudioBool ? View.VISIBLE : View.GONE);

    // registra un receiver per ricevere la notifica di preparazione della registrazione
    registerReceiver(
        downloadPosBRec, new IntentFilter(DownloadService.BROADCAST_DOWNLOAD_PROGRESS));
    registerReceiver(
        downloadCompletedBRec, new IntentFilter(DownloadService.BROADCAST_DOWNLOAD_COMPLETED));
    registerReceiver(downloadErrorBRec, new IntentFilter(DownloadService.BROADCAST_DOWNLOAD_ERROR));
    registerReceiver(
        exportCompleted, new IntentFilter(PdfExportService.BROADCAST_EXPORT_COMPLETED));
    registerReceiver(exportError, new IntentFilter(PdfExportService.BROADCAST_EXPORT_ERROR));
    registerReceiver(fabBRec, new IntentFilter(BottomSheetFabCanto.CHOOSE_DONE));
    registerReceiver(catalogReadyBR, new IntentFilter(MusicService.BROADCAST_RETRIEVE_ASYNC));
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.d(TAG, "onDestroy(): " + isFinishing());
    try {
      unregisterReceiver(downloadPosBRec);
      unregisterReceiver(downloadCompletedBRec);
      unregisterReceiver(downloadErrorBRec);
      unregisterReceiver(exportCompleted);
      unregisterReceiver(exportError);
      unregisterReceiver(fabBRec);
      unregisterReceiver(catalogReadyBR);
    } catch (IllegalArgumentException e) {
      Log.e(TAG, e.getLocalizedMessage(), e);
    }
    //    saveZoom(false, false);
    //    if (listaCanti != null) listaCanti.close();
    if (isFinishing()) stopMedia();
    stopSeekbarUpdate();
    mExecutorService.shutdown();
  }

  //  public void pulisciVars(boolean andSaveTabAlso) {
  //    saveZoom(true, andSaveTabAlso);

  //    notaCambio = null;
  //    barreCambio = null;

  //    SaveSpeed();
  //    if (scrollPlaying) {
  //      showScrolling(false);
  //      scrollPlaying = false;
  //      mHandler.removeCallbacks(mScrollDown);
  //    }
  //    speedValue = null;
  //    mostraAudio = null;
  //  }

  // recupera il flag preferito per la pagina
  //  public int selectFavouriteFromSource() {
  //
  //    SQLiteDatabase db = listaCanti.getReadableDatabase();
  //
  //    String query = "SELECT favourite" + "  FROM ELENCO" + "  WHERE _id =  " + idCanto;
  //    Cursor cursor = db.rawQuery(query, null);
  //
  //    cursor.moveToFirst();
  //    int favouriteFlag = cursor.getInt(0);
  //
  //    cursor.close();
  //    db.close();
  //    return favouriteFlag;
  //  }

  // aggiorna il flag che indica se la pagina è tra i preferiti
  public void updateFavouriteFlag(final int favouriteFlag) {

    //    SQLiteDatabase db = listaCanti.getReadableDatabase();
    //
    //    String sql =
    //        "UPDATE ELENCO" + "  SET favourite = " + favouriteFlag + " " + "  WHERE _id =  " +
    // idCanto;
    //    db.execSQL(sql);
    //    db.close();
    new Thread(
            new Runnable() {
              @Override
              public void run() {
                CantoDao mDao = RisuscitoDatabase.getInstance(PaginaRenderActivity.this).cantoDao();
                mViewModel.mCurrentCanto.favorite = favouriteFlag;
                mDao.updateCanto(mViewModel.mCurrentCanto);
                Snackbar.make(
                        findViewById(android.R.id.content),
                        favouriteFlag == 1 ? R.string.favorite_added : R.string.favorite_removed,
                        Snackbar.LENGTH_SHORT)
                    .show();
              }
            })
        .start();
  }

  // recupera e setta il record per la registrazione
  private void getRecordLink() {

    //    SQLiteDatabase db = listaCanti.getReadableDatabase();

    //    String query = "SELECT link" + "  FROM ELENCO" + "  WHERE _id =  " + idCanto;
    //    Cursor cursor = db.rawQuery(query, null);
    //
    //    cursor.moveToFirst();
    //    if (cursor.getString(0) != null && !cursor.getString(0).equals("")) url =
    // cursor.getString(0);
    if (mViewModel.mCurrentCanto.link != null && !mViewModel.mCurrentCanto.link.equals(""))
      url = mViewModel.mCurrentCanto.link;
    else url = "";

    //    cursor.close();

    //    query = "SELECT local_path" + "  FROM LOCAL_LINKS" + "  WHERE _id =  " + idCanto;
    //    cursor = db.rawQuery(query, null);

    LocalLinksDao mDao = RisuscitoDatabase.getInstance(PaginaRenderActivity.this).localLinksDao();
    LocalLink localLink = mDao.getLocalLinkByCantoId(idCanto);

    if (localLink != null && localLink.localPath != null && !localLink.localPath.isEmpty())
      personalUrl = localLink.localPath;
    else personalUrl = "";

    //    if (cursor.getCount() == 1) {
    //      cursor.moveToFirst();
    //      personalUrl = cursor.getString(0);
    //    } else personalUrl = "";
    //
    //    cursor.close();
    //    db.close();
  }

  // recupera e setta lo zoom
  //  private void getZoom() {
  //
  //    SQLiteDatabase db = listaCanti.getReadableDatabase();
  //
  //    String query =
  //        "SELECT zoom, scroll_x , scroll_y" + "  FROM ELENCO" + "  WHERE _id =  " + idCanto;
  //    Cursor cursor = db.rawQuery(query, null);
  //
  //    cursor.moveToFirst();
  //    defaultZoomLevel = cursor.getInt(0);
  //    defaultScrollX = cursor.getInt(1);
  //    defaultScrollY = cursor.getInt(2);
  //
  //    cursor.close();
  //    db.close();
  //  }

  @SuppressWarnings("deprecation")
  private void saveZoom(boolean andSpeedAlso, boolean andSaveTabAlso) {
    //    defaultZoomLevel = (int) (paginaView.getScale() * 100);
    //    defaultScrollX = paginaView.getScrollX();
    //    defaultScrollY = paginaView.getScrollY();
    if (mViewModel.mCurrentCanto != null) {
      mViewModel.mCurrentCanto.zoom = (int) (paginaView.getScale() * 100);
      mViewModel.mCurrentCanto.scrollX = paginaView.getScrollX();
      mViewModel.mCurrentCanto.scrollY = paginaView.getScrollY();

      if (andSpeedAlso) mViewModel.mCurrentCanto.savedSpeed = mViewModel.speedValue;

      if (andSaveTabAlso) {
        mViewModel.mCurrentCanto.savedBarre = mViewModel.barreCambio;
        mViewModel.mCurrentCanto.savedTab = mViewModel.notaCambio;
      }

      //    SQLiteDatabase db = listaCanti.getReadableDatabase();
      //
      //    String sql =
      //        "UPDATE ELENCO"
      //            + "  SET zoom = "
      //            + defaultZoomLevel
      //            + " "
      //            + ", scroll_x = "
      //            + defaultScrollX
      //            + " "
      //            + ", scroll_y = "
      //            + defaultScrollY
      //            + " "
      //            + "  WHERE _id =  "
      //            + idCanto;
      //    db.execSQL(sql);
      //    db.close();

      new Thread(
              new Runnable() {
                @Override
                public void run() {
                  CantoDao mDao = RisuscitoDatabase.getInstance(getApplicationContext()).cantoDao();
                  mDao.updateCanto(mViewModel.mCurrentCanto);
                }
              })
          .start();
    }
  }

  //  private void SaveSpeed() {
  //    SQLiteDatabase db = listaCanti.getReadableDatabase();
  //    String sql =
  //        "UPDATE ELENCO" + "  SET saved_speed = " + speedValue + "  WHERE _id =  " + idCanto;
  //    db.execSQL(sql);
  //    db.close();
  //  }

  @Nullable
  private String cambiaAccordi(
      HashMap<String, String> conversione,
      String barre,
      HashMap<String, String> conversioneMin,
      boolean higlightDiff) {
    String cantoTrasportato = this.getFilesDir() + "/temporaneo.htm";

    boolean barre_scritto = false;

    try {
      BufferedReader br =
          new BufferedReader(new InputStreamReader(getAssets().open(pagina + ".htm"), "UTF-8"));

      String line = br.readLine();

      BufferedWriter out =
          new BufferedWriter(
              new OutputStreamWriter(new FileOutputStream(cantoTrasportato), "UTF-8"));

      String language =
          ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration()).getLanguage();

      Pattern pattern;
      Pattern patternMinore = null;

      switch (language) {
        case "it":
          pattern = Pattern.compile("Do#|Do|Re|Mib|Mi|Fa#|Fa|Sol#|Sol|La|Sib|Si");
          break;
        case "uk":
          pattern = Pattern.compile("Cis|C|D|Eb|E|Fis|F|Gis|G|A|B|H");
          // inserito spazio prima di "b" per evitare che venga confuso con "Eb" o "eb"
          patternMinore = Pattern.compile("cis|c|d|eb|e|fis|f|gis|g|a| b|h");
          break;
        case "en":
          pattern = Pattern.compile("C#|C|D|Eb|E|F#|F|G#|G|A|Bb|B");
          break;
        default:
          pattern = Pattern.compile("Do#|Do|Re|Mib|Mi|Fa#|Fa|Sol#|Sol|La|Sib|Si");
          break;
      }

      // serve per segnarsi se si è già evidenziato il primo accordo del testo
      boolean notaHighlighed = !higlightDiff;

      while (line != null) {
        Log.v(TAG, "RIGA DA ELAB: " + line);
        if (line.contains("A13F3C") && !line.contains("<H2>") && !line.contains("<H4>")) {
          if (language.equalsIgnoreCase("uk") || language.equalsIgnoreCase("en")) {
            line = line.replaceAll("</FONT><FONT COLOR=\"#A13F3C\">", "<K>");
            line = line.replaceAll("</FONT><FONT COLOR=\"#000000\">", "<K2>");
          }
          Matcher matcher = pattern.matcher(line);
          StringBuffer sb = new StringBuffer();
          StringBuffer sb2 = new StringBuffer();
          while (matcher.find()) matcher.appendReplacement(sb, conversione.get(matcher.group(0)));
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
              if (!primaNota.equalsIgnoreCase(mViewModel.notaCambio)) {
                if (Utility.isLowerCase(primaNota.charAt(0))) {
                  String notaCambioMin = mViewModel.notaCambio;
                  if (notaCambioMin.length() == 1) notaCambioMin = notaCambioMin.toLowerCase();
                  else
                    notaCambioMin =
                        notaCambioMin.substring(0, 1).toLowerCase() + notaCambioMin.substring(1);
                  line =
                      line.replaceFirst(
                          notaCambioMin,
                          "<SPAN STYLE=\"BACKGROUND-COLOR:#FFFF00\">" + notaCambioMin + "</SPAN>");
                } else
                  line =
                      line.replaceFirst(
                          mViewModel.notaCambio,
                          "<SPAN STYLE=\"BACKGROUND-COLOR:#FFFF00\">"
                              + mViewModel.notaCambio
                              + "</SPAN>");
                notaHighlighed = true;
              }
            }
            //                        Log.d(TAG, "RIGA ELAB 2: " + line);
            line = line.replaceAll("<K>", "</FONT><FONT COLOR='#A13F3C'>");
            line = line.replaceAll("<K2>", "</FONT><FONT COLOR='#000000'>");
            //                        Log.d(TAG, "RIGA ELAB 3: " + line);
          } else {
            line = sb.toString();
            if (!notaHighlighed) {
              if (!primaNota.equalsIgnoreCase(mViewModel.notaCambio)) {
                line =
                    line.replaceFirst(
                        mViewModel.notaCambio,
                        "<SPAN STYLE=\"BACKGROUND-COLOR:#FFFF00\">"
                            + mViewModel.notaCambio
                            + "</SPAN>");
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
        } else {
          if (line.contains("<H3>")) {
            if (barre != null && !barre.equals("0")) {
              if (!barre_scritto) {
                String oldLine;
                if (higlightDiff && !barre.equalsIgnoreCase(primoBarre)) {
                  oldLine =
                      "<H4><SPAN STYLE=\"BACKGROUND-COLOR:#FFFF00\"><FONT COLOR=\"#A13F3C\"><I>"
                          + getString(R.string.barre_al_tasto, barre)
                          + "</I></FONT></SPAN></H4>";
                } else {
                  oldLine =
                      "<H4><FONT COLOR=\"#A13F3C\"><I>"
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
          } else {
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
    } catch (Exception e) {
      Log.e(TAG, e.getLocalizedMessage(), e);
      return null;
    }
  }

  void startExternalDownload() {
    Log.d(TAG, " WRITE_EXTERNAL_STORAGE OK");
    if (Utility.isExternalStorageWritable()) {
      if (new File(
              Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
              "Risuscitò")
          .mkdirs()) Log.d(TAG, "CARTELLA RISUSCITO CREATA");
      else Log.d(TAG, "CARTELLA RISUSCITO ESISTENTE");
      String localFilePath =
          Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                  .getAbsolutePath()
              + "/Risuscitò/"
              + Utility.filterMediaLinkNew(url);
      new SimpleDialogFragment.Builder(
              PaginaRenderActivity.this, PaginaRenderActivity.this, "DOWNLOAD_MP3")
          .title(R.string.download_running)
          .showProgress()
          .positiveButton(android.R.string.cancel)
          .progressIndeterminate(false)
          .progressMax(100)
          .show();
      Intent i = new Intent(getApplicationContext(), DownloadService.class);
      i.setAction(DownloadService.ACTION_DOWNLOAD);
      Uri uri = Uri.parse(url);
      i.setData(uri);
      i.putExtra(DownloadService.DATA_DESTINATION_FILE, localFilePath);
      startService(i);
    } else
      Snackbar.make(
              findViewById(android.R.id.content),
              R.string.no_memory_writable,
              Snackbar.LENGTH_SHORT)
          .show();
  }

  void startInternalDownload() {
    String localFilePath =
        PaginaRenderActivity.this.getFilesDir() + "/" + Utility.filterMediaLink(url);
    new SimpleDialogFragment.Builder(
            PaginaRenderActivity.this, PaginaRenderActivity.this, "DOWNLOAD_MP3")
        .title(R.string.download_running)
        .showProgress()
        .positiveButton(android.R.string.cancel)
        .progressIndeterminate(false)
        .progressMax(100)
        .show();
    Intent i = new Intent(getApplicationContext(), DownloadService.class);
    i.setAction(DownloadService.ACTION_DOWNLOAD);
    Uri uri = Uri.parse(url);
    i.setData(uri);
    i.putExtra(DownloadService.DATA_DESTINATION_FILE, localFilePath);
    startService(i);
  }

  private void showPlaying(boolean started) {
    Log.d(TAG, "showPlaying: ");
    EasyAnimatedVectorDrawable.setImageType(
        play_button,
        started ? EasyAnimatedVectorDrawable.Type.PAUSE : EasyAnimatedVectorDrawable.Type.PLAY,
        ContextCompat.getColor(PaginaRenderActivity.this, R.color.text_color_secondary));
  }

  private void showScrolling(boolean scrolling) {
    IconicsDrawable icon =
        new IconicsDrawable(PaginaRenderActivity.this)
            .icon(
                scrolling
                    ? CommunityMaterial.Icon.cmd_pause_circle_outline
                    : CommunityMaterial.Icon.cmd_play_circle_outline)
            .color(Color.WHITE)
            .sizeDp(24)
            .paddingDp(2);
    play_scroll.setImageDrawable(icon);
    play_scroll.setSelected(scrolling);
  }

  @Override
  public void onPositive(@NonNull String tag) {
    Log.d(TAG, "onPositive: " + tag);
    switch (tag) {
      case "DOWNLOAD_MP3":
        sendBroadcast(new Intent(DownloadService.ACTION_CANCEL));
        break;
      case "DELETE_LINK":
        Snackbar.make(
                findViewById(android.R.id.content), R.string.delink_delete, Snackbar.LENGTH_SHORT)
            .show();
        stopMedia();
        new DeleteLinkTask().execute(idCanto);
        //        SQLiteDatabase db = listaCanti.getReadableDatabase();
        //        String sql = "DELETE FROM LOCAL_LINKS" + "  WHERE _id =  " + idCanto;
        //        db.execSQL(sql);
        //        db.close();
        //        refreshCatalog();
        ////        checkRecordsState();
        break;
      case "DELETE_MP3":
        File fileToDelete = new File(localUrl);
        if (fileToDelete.delete()) {
          if (fileToDelete.getAbsolutePath().contains("/Risuscit")) {
            // initiate media scan and put the new things into the path array to
            // make the scanner aware of the location and the files you want to see
            MediaScannerConnection.scanFile(
                getApplicationContext(), new String[] {fileToDelete.getAbsolutePath()}, null, null);
          }
          Snackbar.make(
                  findViewById(android.R.id.content), R.string.file_delete, Snackbar.LENGTH_SHORT)
              .show();
        } else
          Snackbar.make(findViewById(android.R.id.content), R.string.error, Snackbar.LENGTH_SHORT)
              .show();
        stopMedia();
        refreshCatalog();
        //        checkRecordsState();
        new RecordStateCheckerTask().execute();
        break;
      case "DOWNLINK_CHOOSE":
        SharedPreferences pref =
            PreferenceManager.getDefaultSharedPreferences(PaginaRenderActivity.this);
        int saveLocation = Integer.parseInt(pref.getString(Utility.SAVE_LOCATION, "0"));
        if (saveLocation == 1) {
          //                    checkStoragePermissions();
          if (EasyPermissions.hasPermissions(
              PaginaRenderActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            // Have permission, do the thing!
            startExternalDownload();
          else {
            SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(PaginaRenderActivity.this).edit();
            editor.putString(Utility.SAVE_LOCATION, "0");
            editor.apply();
            Snackbar.make(
                    findViewById(android.R.id.content),
                    R.string.forced_private,
                    Snackbar.LENGTH_SHORT)
                .show();
            startInternalDownload();
          }
        } else startInternalDownload();
        break;
      case "ONLY_LINK":
        new FileChooserDialog.Builder(PaginaRenderActivity.this)
            .mimeType("audio/*") // Optional MIME type filter
            .tag("optional-identifier")
            .goUpLabel("Up") // custom go up label, default label is "..."
            .show(PaginaRenderActivity.this);
        break;
      case "SAVE_TAB":
        //        db = listaCanti.getReadableDatabase();
        //        sql =
        //            "UPDATE ELENCO"
        //                + "  SET saved_tab = \'"
        //                + notaCambio
        //                + "\' "
        //                + "    , saved_barre = \'"
        //                + barreCambio
        //                + "\' "
        //                + "  WHERE _id =  "
        //                + idCanto;
        //        db.execSQL(sql);
        //        db.close();
        if (mViewModel.scrollPlaying) {
          showScrolling(false);
          mHandler.removeCallbacks(mScrollDown);
        }
        saveZoom(true, true);
        //        pulisciVars(true);
        mLUtils.closeActivityWithTransition();
        break;
    }
  }

  @Override
  public void onNegative(@NonNull String tag) {
    Log.d(TAG, "onNegative: " + tag);
    switch (tag) {
      case "DOWNLINK_CHOOSE":
        new FileChooserDialog.Builder(PaginaRenderActivity.this)
            .mimeType("audio/*") // Optional MIME type filter
            .tag("optional-identifier")
            .goUpLabel("Up") // custom go up label, default label is "..."
            .show(PaginaRenderActivity.this);
        break;
      case "SAVE_TAB":
        if (mViewModel.scrollPlaying) {
          showScrolling(false);
          mHandler.removeCallbacks(mScrollDown);
        }
        saveZoom(true, false);
        //        pulisciVars(false);
        mLUtils.closeActivityWithTransition();
        break;
    }
  }

  @Override
  public void onNeutral(@NonNull String tag) {}

  @Override
  public void onFileSelection(@NonNull FileChooserDialog dialog, @NonNull File file) {
    String path = file.getAbsolutePath();
    Snackbar.make(
            findViewById(android.R.id.content),
            getString(R.string.file_selected) + ": " + path,
            Snackbar.LENGTH_SHORT)
        .show();
    stopMedia();
    new InsertLinkTask().execute(String.valueOf(idCanto), path);
    //    SQLiteDatabase db = listaCanti.getReadableDatabase();
    //    ContentValues values = new ContentValues();
    //    values.put("_id", idCanto);
    //    values.put("local_path", path);
    //    db.insert("LOCAL_LINKS", null, values);
    //    db.close();
    //    refreshCatalog();
    //    checkRecordsState();
    //        recreate();
  }

  @Override
  public void onFileChooserDismissed(@NonNull FileChooserDialog dialog) {}

  private void playIntroSmall() {
    findViewById(R.id.music_controls).setVisibility(View.VISIBLE);
    new TapTargetSequence(PaginaRenderActivity.this)
        .continueOnCancel(true)
        .targets(
            TapTarget.forToolbarMenuItem(
                    mToolbar,
                    R.id.tonalita,
                    getString(R.string.action_tonalita),
                    getString(R.string.sc_tonalita_desc))
                // All options below are optional
                .outerCircleColorInt(
                    getThemeUtils().primaryColor()) // Specify a color for the outer circle
                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                .textTypeface(
                    Typeface.createFromAsset(
                        getResources().getAssets(),
                        "fonts/Roboto-Regular.ttf")) // Specify a typeface for the text
                .titleTextColor(R.color.primary_text_default_material_dark)
                .textColor(R.color.secondary_text_default_material_dark)
                .id(1),
            TapTarget.forToolbarMenuItem(
                    mToolbar,
                    R.id.barre,
                    getString(R.string.action_barre),
                    getString(R.string.sc_barre_desc))
                // All options below are optional
                .outerCircleColorInt(
                    getThemeUtils().primaryColor()) // Specify a color for the outer circle
                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                .textTypeface(
                    Typeface.createFromAsset(
                        getResources().getAssets(),
                        "fonts/Roboto-Regular.ttf")) // Specify a typeface for the text
                .titleTextColor(R.color.primary_text_default_material_dark)
                .textColor(R.color.secondary_text_default_material_dark)
                .id(2),
            TapTarget.forView(
                    play_scroll,
                    getString(R.string.sc_scroll_title),
                    getString(R.string.sc_scroll_desc))
                // All options below are optional
                .outerCircleColorInt(
                    getThemeUtils().primaryColor()) // Specify a color for the outer circle
                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                .textTypeface(
                    Typeface.createFromAsset(
                        getResources().getAssets(),
                        "fonts/Roboto-Regular.ttf")) // Specify a typeface for the text
                .titleTextColor(R.color.primary_text_default_material_dark)
                .textColor(R.color.secondary_text_default_material_dark)
                .id(3),
            TapTarget.forToolbarOverflow(
                    mToolbar,
                    getString(R.string.showcase_end_title),
                    getString(R.string.showcase_help_general))
                // All options below are optional
                .outerCircleColorInt(
                    getThemeUtils().primaryColor()) // Specify a color for the outer circle
                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                .textTypeface(
                    Typeface.createFromAsset(
                        getResources().getAssets(),
                        "fonts/Roboto-Regular.ttf")) // Specify a typeface for the text
                .titleTextColor(R.color.primary_text_default_material_dark)
                .textColor(R.color.secondary_text_default_material_dark)
                .id(4))
        .listener(
            new TapTargetSequence
                .Listener() { // The listener can listen for regular clicks, long clicks or cancels
              @Override
              public void onSequenceFinish() {
                //                                Log.d(TAG, "onSequenceFinish: ");
                SharedPreferences.Editor prefEditor =
                    PreferenceManager.getDefaultSharedPreferences(PaginaRenderActivity.this).edit();
                prefEditor.putBoolean(Utility.INTRO_PAGINARENDER, true);
                prefEditor.apply();
                findViewById(R.id.music_controls)
                    .setVisibility(mostraAudioBool ? View.VISIBLE : View.GONE);
              }

              @Override
              public void onSequenceStep(TapTarget tapTarget, boolean b) {}

              @Override
              public void onSequenceCanceled(TapTarget tapTarget) {
                //                                Log.d(TAG, "onSequenceCanceled: ");
                SharedPreferences.Editor prefEditor =
                    PreferenceManager.getDefaultSharedPreferences(PaginaRenderActivity.this).edit();
                prefEditor.putBoolean(Utility.INTRO_PAGINARENDER, true);
                prefEditor.apply();
                findViewById(R.id.music_controls)
                    .setVisibility(mostraAudioBool ? View.VISIBLE : View.GONE);
              }
            })
        .start();
  }

  private void playIntroFull() {
    findViewById(R.id.music_controls).setVisibility(View.VISIBLE);
    new TapTargetSequence(PaginaRenderActivity.this)
        .continueOnCancel(true)
        .targets(
            TapTarget.forToolbarMenuItem(
                    mToolbar,
                    R.id.tonalita,
                    getString(R.string.action_tonalita),
                    getString(R.string.sc_tonalita_desc))
                // All options below are optional
                .outerCircleColorInt(
                    getThemeUtils().primaryColor()) // Specify a color for the outer circle
                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                .textTypeface(
                    Typeface.createFromAsset(
                        getResources().getAssets(),
                        "fonts/Roboto-Regular.ttf")) // Specify a typeface for the text
                .titleTextColor(R.color.primary_text_default_material_dark)
                .textColor(R.color.secondary_text_default_material_dark)
                .id(1),
            TapTarget.forToolbarMenuItem(
                    mToolbar,
                    R.id.barre,
                    getString(R.string.action_barre),
                    getString(R.string.sc_barre_desc))
                // All options below are optional
                .outerCircleColorInt(
                    getThemeUtils().primaryColor()) // Specify a color for the outer circle
                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                .textTypeface(
                    Typeface.createFromAsset(
                        getResources().getAssets(),
                        "fonts/Roboto-Regular.ttf")) // Specify a typeface for the text
                .titleTextColor(R.color.primary_text_default_material_dark)
                .textColor(R.color.secondary_text_default_material_dark)
                .id(2),
            TapTarget.forView(
                    play_button,
                    getString(R.string.sc_audio_title),
                    getString(R.string.sc_audio_desc))
                // All options below are optional
                .outerCircleColorInt(
                    getThemeUtils().primaryColor()) // Specify a color for the outer circle
                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                .textTypeface(
                    Typeface.createFromAsset(
                        getResources().getAssets(),
                        "fonts/Roboto-Regular.ttf")) // Specify a typeface for the text
                .titleTextColor(R.color.primary_text_default_material_dark)
                .textColor(R.color.secondary_text_default_material_dark)
                .id(3),
            TapTarget.forView(
                    play_scroll,
                    getString(R.string.sc_scroll_title),
                    getString(R.string.sc_scroll_desc))
                // All options below are optional
                .outerCircleColorInt(
                    getThemeUtils().primaryColor()) // Specify a color for the outer circle
                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                .textTypeface(
                    Typeface.createFromAsset(
                        getResources().getAssets(),
                        "fonts/Roboto-Regular.ttf")) // Specify a typeface for the text
                .titleTextColor(R.color.primary_text_default_material_dark)
                .textColor(R.color.secondary_text_default_material_dark)
                .id(4),
            TapTarget.forToolbarOverflow(
                    mToolbar,
                    getString(R.string.showcase_end_title),
                    getString(R.string.showcase_help_general))
                // All options below are optional
                .outerCircleColorInt(
                    getThemeUtils().primaryColor()) // Specify a color for the outer circle
                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                .textTypeface(
                    Typeface.createFromAsset(
                        getResources().getAssets(),
                        "fonts/Roboto-Regular.ttf")) // Specify a typeface for the text
                .titleTextColor(R.color.primary_text_default_material_dark)
                .textColor(R.color.secondary_text_default_material_dark)
                .id(5))
        .listener(
            new TapTargetSequence
                .Listener() { // The listener can listen for regular clicks, long clicks or cancels
              @Override
              public void onSequenceFinish() {
                //                                Log.d(TAG, "onSequenceFinish: ");
                SharedPreferences.Editor prefEditor =
                    PreferenceManager.getDefaultSharedPreferences(PaginaRenderActivity.this).edit();
                prefEditor.putBoolean(Utility.INTRO_PAGINARENDER, true);
                prefEditor.apply();
                findViewById(R.id.music_controls)
                    .setVisibility(mostraAudioBool ? View.VISIBLE : View.GONE);
              }

              @Override
              public void onSequenceStep(TapTarget tapTarget, boolean b) {}

              @Override
              public void onSequenceCanceled(TapTarget tapTarget) {
                //                                Log.d(TAG, "onSequenceCanceled: ");
                SharedPreferences.Editor prefEditor =
                    PreferenceManager.getDefaultSharedPreferences(PaginaRenderActivity.this).edit();
                prefEditor.putBoolean(Utility.INTRO_PAGINARENDER, true);
                prefEditor.apply();
                findViewById(R.id.music_controls)
                    .setVisibility(mostraAudioBool ? View.VISIBLE : View.GONE);
              }
            })
        .start();
  }

  private void playMedia() {
    Log.d(TAG, "playMedia: ");
    MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
    if (controller != null) {
      controller.getTransportControls().play();
    }
  }

  private void pauseMedia() {
    Log.d(TAG, "pauseMedia: ");
    MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
    if (controller != null) {
      controller.getTransportControls().pause();
    }
  }

  private void stopMedia() {
    Log.d(TAG, "stopMedia: ");
    if (mLastPlaybackState.getState() != PlaybackStateCompat.STATE_STOPPED) {
      MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
      if (controller != null) {
        controller.getTransportControls().stop();
      }
    }
  }

  private void playFromId(String id) {
    new SimpleDialogFragment.Builder(
            PaginaRenderActivity.this, PaginaRenderActivity.this, "BUFFERING")
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

  private void refreshCatalog() {
    Log.d(TAG, "refreshCatalog");
    MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
    if (controller != null) {
      controller.getTransportControls().sendCustomAction(MusicService.ACTION_REFRESH, null);
    }
  }

  private void scheduleSeekbarUpdate() {
    stopSeekbarUpdate();
    if (!mExecutorService.isShutdown()) {
      mScheduleFuture =
          mExecutorService.scheduleAtFixedRate(
              new Runnable() {
                @Override
                public void run() {
                  mHandler.post(mUpdateProgressTask);
                }
              },
              PROGRESS_UPDATE_INITIAL_INTERVAL,
              PROGRESS_UPDATE_INTERNAL,
              TimeUnit.MILLISECONDS);
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
    if (mLastPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
      // Calculate the elapsed time between the last position update and now and unless
      // paused, we can assume (delta * speed) + current position is approximately the
      // latest position. This ensure that we do not repeatedly call the getPlaybackState()
      // on MediaControllerCompat.
      long timeDelta =
          SystemClock.elapsedRealtime() - mLastPlaybackState.getLastPositionUpdateTime();
      currentPosition += (int) timeDelta * mLastPlaybackState.getPlaybackSpeed();
    }
    scroll_song_bar.setEnabled(true);
    scroll_song_bar.setProgress((int) currentPosition);
  }

  private void checkRecordsState() {
    //    getRecordLink();
    // c'è la registrazione online
    if (!url.equalsIgnoreCase("")) {
      //            checkExternalFilePermissions();
      // controllo se ho scaricato un file in locale
      SharedPreferences pref =
          PreferenceManager.getDefaultSharedPreferences(PaginaRenderActivity.this);
      int saveLocation = Integer.parseInt(pref.getString(Utility.SAVE_LOCATION, "0"));
      if (saveLocation == 1) {
        if (EasyPermissions.hasPermissions(
            PaginaRenderActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
          // Have permission, do the thing!
          localUrl = Utility.retrieveMediaFileLink(PaginaRenderActivity.this, url, true);
        } else {
          SharedPreferences.Editor editor =
              PreferenceManager.getDefaultSharedPreferences(PaginaRenderActivity.this).edit();
          editor.putString(Utility.SAVE_LOCATION, "0");
          editor.apply();
          Snackbar.make(
                  findViewById(android.R.id.content),
                  getString(R.string.external_storage_denied),
                  Snackbar.LENGTH_SHORT)
              .show();
          localUrl = Utility.retrieveMediaFileLink(PaginaRenderActivity.this, url, false);
        }
      } else localUrl = Utility.retrieveMediaFileLink(PaginaRenderActivity.this, url, false);

      mDownload = !(localUrl.equalsIgnoreCase("") && personalUrl.equalsIgnoreCase(""));

      // almeno una registrazione c'è, quindi nascondo il messaggio di no_records
      no_records_text.setVisibility(View.INVISIBLE);
      // mostra i pulsanti per il lettore musicale se ho una registrazione locale oppure se sono
      // online, altrimenti mostra il messaggio di mancata connessione
      music_buttons.setVisibility(
          Utility.isOnline(this) || mDownload ? View.VISIBLE : View.INVISIBLE);
      no_connection_text.setVisibility(
          Utility.isOnline(this) || mDownload ? View.INVISIBLE : View.VISIBLE);
    }
    // NON c'è la registrazione online
    else {
      mDownload = !personalUrl.isEmpty();
      // Se c'è una registrazione locale mostro i pulsanti
      music_buttons.setVisibility(mDownload ? View.VISIBLE : View.INVISIBLE);
      no_records_text.setVisibility(mDownload ? View.INVISIBLE : View.VISIBLE);
    }
  }

  private class MyWebViewClient extends WebViewClient {
    @Override
    public void onPageFinished(WebView view, String url) {
      view.postDelayed(
          new Runnable() {
            @Override
            public void run() {
              //              if (defaultScrollX > 0 || defaultScrollY > 0)
              //                paginaView.scrollTo(defaultScrollX, defaultScrollY);
              if (mViewModel.mCurrentCanto.scrollX > 0 || mViewModel.mCurrentCanto.scrollY > 0)
                paginaView.scrollTo(
                    mViewModel.mCurrentCanto.scrollX, mViewModel.mCurrentCanto.scrollY);
            }
            // Delay the scrollTo to make it work
          },
          600);
      super.onPageFinished(view, url);
    }
  }

  private class DataRetrieverTask extends AsyncTask<Integer, Void, Integer> {
    @Override
    protected Integer doInBackground(Integer... params) {
      Log.d(TAG, "doInBackground: ");
      CantoDao mDao = RisuscitoDatabase.getInstance(getApplicationContext()).cantoDao();
      mViewModel.mCurrentCanto = mDao.getCantoById(params[0]);
      getRecordLink();
      return 0;
    }

    @Override
    protected void onPostExecute(Integer integer) {
      super.onPostExecute(integer);
      if (mViewModel.mCurrentCanto.savedTab == null) {
        if (mViewModel.notaCambio == null)
          mViewModel.mCurrentCanto.savedTab = mViewModel.notaCambio = primaNota;
        else mViewModel.mCurrentCanto.savedTab = primaNota;
      } else if (mViewModel.notaCambio == null)
        mViewModel.notaCambio = mViewModel.mCurrentCanto.savedTab;

      if (mViewModel.mCurrentCanto.savedBarre == null) {
        if (mViewModel.barreCambio == null)
          mViewModel.mCurrentCanto.savedBarre = mViewModel.barreCambio = primoBarre;
        else mViewModel.mCurrentCanto.savedBarre = primoBarre;
      } else {
        //	    	Log.i("BARRESALVATO", barreSalvato);
        if (mViewModel.barreCambio == null)
          mViewModel.barreCambio = mViewModel.mCurrentCanto.savedBarre;
      }

      // fix per crash su android 4.1
      if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN)
        paginaView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

      HashMap<String, String> convMap =
          cambioAccordi.diffSemiToni(primaNota, mViewModel.notaCambio);
      HashMap<String, String> convMin = null;
      if (ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration())
          .getLanguage()
          .equalsIgnoreCase("uk"))
        convMin = cambioAccordi.diffSemiToniMin(primaNota, mViewModel.notaCambio);
      if (convMap != null) {
        String nuovoFile = cambiaAccordi(convMap, mViewModel.barreCambio, convMin, true);
        if (nuovoFile != null) paginaView.loadUrl("file://" + nuovoFile);
      } else paginaView.loadUrl("file:///android_asset/" + pagina + ".htm");

      WebSettings webSettings = paginaView.getSettings();
      webSettings.setUseWideViewPort(true);
      webSettings.setSupportZoom(true);
      webSettings.setLoadWithOverviewMode(true);

      webSettings.setBuiltInZoomControls(true);
      webSettings.setDisplayZoomControls(false);

      if (mViewModel.mCurrentCanto.zoom > 0)
        paginaView.setInitialScale(mViewModel.mCurrentCanto.zoom);
      paginaView.setWebViewClient(new MyWebViewClient());

      if (mViewModel.speedValue == null) {
        //	    	Log.i("SONO APPENA ENTRATO", "setto " + savedSpeed);
        scroll_speed_bar.setProgress(Integer.valueOf(mViewModel.mCurrentCanto.savedSpeed));
      } else {
        //	    	Log.i("ROTAZIONE", "setto " + speedValue);
        scroll_speed_bar.setProgress(Integer.valueOf(mViewModel.speedValue));
      }

      //	    Log.i(this.getClass().toString(), "scrollPlaying? " + scrollPlaying);
      if (mViewModel.scrollPlaying) {
        showScrolling(true);
        mScrollDown.run();
      }
      checkRecordsState();
    }
  }

  private class RecordStateCheckerTask extends AsyncTask<Void, Void, Integer> {
    @Override
    protected Integer doInBackground(Void... params) {
      getRecordLink();
      return 0;
    }

    @Override
    protected void onPostExecute(Integer integer) {
      super.onPostExecute(integer);
      checkRecordsState();
    }
  }

  private class DeleteLinkTask extends AsyncTask<Integer, Void, Integer> {
    @Override
    protected Integer doInBackground(Integer... params) {
      LocalLinksDao mDao = RisuscitoDatabase.getInstance(PaginaRenderActivity.this).localLinksDao();
      LocalLink linkToDelete = new LocalLink();
      linkToDelete.idCanto = params[0];
      mDao.deleteLocalLink(linkToDelete);
      getRecordLink();
      return 0;
    }

    @Override
    protected void onPostExecute(Integer integer) {
      super.onPostExecute(integer);
      refreshCatalog();
      checkRecordsState();
    }
  }

  private class InsertLinkTask extends AsyncTask<String, Void, Integer> {
    @Override
    protected Integer doInBackground(String... params) {
      LocalLinksDao mDao = RisuscitoDatabase.getInstance(PaginaRenderActivity.this).localLinksDao();
      LocalLink linkToInsert = new LocalLink();
      linkToInsert.idCanto = Integer.valueOf(params[0]);
      linkToInsert.localPath = params[1];
      mDao.insertLocalLink(linkToInsert);
      getRecordLink();
      return 0;
    }

    @Override
    protected void onPostExecute(Integer integer) {
      super.onPostExecute(integer);
      refreshCatalog();
      checkRecordsState();
    }
  }
}
