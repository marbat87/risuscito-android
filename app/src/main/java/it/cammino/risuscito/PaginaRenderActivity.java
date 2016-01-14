package it.cammino.risuscito;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.alexkolpa.fabtoolbar.FabToolbar;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.cammino.risuscito.filepicker.ThemedFilePickerActivity;
import it.cammino.risuscito.slides.IntroPaginaRender;
import it.cammino.risuscito.ui.ThemeableActivity;

public class PaginaRenderActivity extends ThemeableActivity {

    private DatabaseCanti listaCanti;
    private String pagina;
    private int idCanto;
    private static MediaPlayer mediaPlayer;
    private ImageButton play_scroll, play_button, save_file, fab_favorite, fab_sound_off;
    private TextView no_records_text;
    private View music_buttons;
    public FabToolbar mFab; // the floating blue add/paste button
    SeekBar scroll_speed_bar, scroll_song_bar;
    private MaterialDialog mProgressDialog, mp3Dialog, exportDialog;
    private PhoneStateListener phoneStateListener;
    private static OnAudioFocusChangeListener afChangeListener;
    private static AudioManager am;
    private String url;
    private int prevOrientation;
    private String primaNota;
    private String notaSalvata;
    public static String notaCambio;
    private String primoBarre;
    private String barreSalvato;
    private static String barreCambio;
    private String personalUrl;

    enum MP_State {
        Idle, Initialized, Prepared, Started, Paused,
        Stopped, PlaybackCompleted, End, Error, Preparing}

    static MP_State mediaPlayerState;

    private boolean localFile;
    private String localUrl;

    private WebView paginaView;
    private int defaultZoomLevel = 0;
    private int defaultScrollX = 0;
    private int defaultScrollY = 0;

    private static final String PREF_FIRST_OPEN_NEW = "prima_apertura_audio";

    private Handler mHandler = new Handler();
    final Runnable mScrollDown = new Runnable() {
        public void run() {
            if (paginaView != null && speedValue != null) {
                try {
                    paginaView.scrollBy(0, Integer.valueOf(speedValue));
                } catch (NumberFormatException e) {
                    paginaView.scrollBy(0, 0);
                }
                mHandler.postDelayed(this, SCROLL_SLEEP);
            }
            else
                Log.d(getClass().getName(), "attività chiusa o annullato lo scroll");
        }
    };
    final Runnable mScrollBar = new Runnable() {
        public void run() {
            if (mediaPlayer != null && mediaPlayerState == MP_State.Started) {
                scroll_song_bar.setProgress(mediaPlayer.getCurrentPosition());
                mHandler.postDelayed(this, SONG_STEP);
            }
            else
                Log.d(getClass().getName(), "mediaPlayer nullo o non avviato!");
        }
    };

    public static String speedValue;
    private int savedSpeed;
    public static boolean scrollPlaying;

    private final long SCROLL_SLEEP = 700;
    private final long SONG_STEP = 1000;

    private String localPDFPath;

    private static final int REQUEST_CODE = 6384;

    private LUtils mLUtils;

    public static String mostraAudio;
    public boolean mostraAudioBool;

    public boolean audioRequested = false;

    public final CambioAccordi cambioAccordi = new CambioAccordi(this);

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagina_render);

        Toolbar toolbar = (Toolbar) findViewById(R.id.risuscito_toolbar);
        toolbar.setTitle("");
        ((TextView)findViewById(R.id.main_toolbarTitle)).setText(R.string.canto_title_activity);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setBackgroundColor(getThemeUtils().primaryColor());
        setSupportActionBar(toolbar);
        findViewById(R.id.bottom_bar).setBackgroundColor(getThemeUtils().primaryColor());

        if (savedInstanceState != null)
            audioRequested = savedInstanceState.getBoolean(Utility.AUDIO_REQUESTED, false);

        listaCanti = new DatabaseCanti(this);

        // recupera il numero della pagina da visualizzare dal parametro passato dalla chiamata
        Bundle bundle = this.getIntent().getExtras();
        pagina = bundle.getString("pagina");
        idCanto = bundle.getInt("idCanto");

        getRecordLink();

        paginaView = (WebView) findViewById(R.id.cantoView);

        try {
            primaNota = CambioAccordi.recuperaPrimoAccordo(getAssets().open(pagina + ".htm"));
            primoBarre = cambioAccordi.recuperaBarre(getAssets().open(pagina + ".htm"));
        }
        catch (IOException e) {
            Log.e(getClass().getName(), e.getLocalizedMessage(), e);
        }

        SQLiteDatabase db = listaCanti.getReadableDatabase();

        String query = "SELECT saved_tab, saved_barre, saved_speed" +
                "  FROM ELENCO" +
                "  WHERE _id =  " + idCanto;
        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();
        notaSalvata = cursor.getString(0);
        barreSalvato = cursor.getString(1);
        savedSpeed = cursor.getInt(2);
        cursor.close();
        db.close();

        //recupera i pulsanti
        play_button = (ImageButton) findViewById(R.id.play_song);
        Drawable drawable = DrawableCompat.wrap(play_button.getDrawable());
        DrawableCompat.setTint(drawable, ContextCompat.getColor(PaginaRenderActivity.this, R.color.icon_ative_black));
        play_button.setImageDrawable(drawable);
        no_records_text = (TextView) findViewById(R.id.no_record);
        music_buttons = findViewById(R.id.music_buttons);
        save_file = (ImageButton) findViewById(R.id.save_file);
        drawable = DrawableCompat.wrap(save_file.getDrawable());
        DrawableCompat.setTint(drawable, ContextCompat.getColor(PaginaRenderActivity.this, android.R.color.white));
        play_scroll = (ImageButton) findViewById(R.id.play_scroll);
        drawable = DrawableCompat.wrap(play_scroll.getDrawable());
        DrawableCompat.setTint(drawable, ContextCompat.getColor(PaginaRenderActivity.this, android.R.color.white));
        scroll_speed_bar = (SeekBar) findViewById(R.id.speed_seekbar);
        scroll_song_bar = (SeekBar) findViewById(R.id.music_seekbar);

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        afChangeListener = new OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int focusChange) {
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                    // Lower the volume
                    if (mediaPlayerState == MP_State.Started) {
                        mediaPlayer.setVolume(0.1f, 0.1f);
                    }
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    // Raise it back to normal
                    if (mediaPlayerState == MP_State.Started) {
                        mediaPlayer.setVolume(1.0f, 1.0f);
                    }
                }
            }
        };


        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    //Incoming call: Pause music
                    if (mediaPlayerState == MP_State.Started)
                        cmdPause();
                } else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    //A call is dialing, active or on hold
                    if (mediaPlayerState == MP_State.Started)
                        cmdPause();
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
//        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//        if(mgr != null) {
//            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
//        }
//        Log.i(getClass().getName(), "STO PER...");
//        PaginaRenderActivityPermissionsDispatcher.attachPhoneListenerWithCheck(PaginaRenderActivity.this);
//        checkPhoneStatePermission();

        if (!url.equalsIgnoreCase("")) {

//            localUrl = Utility.retrieveMediaFileLink(this, url);
            checkExternalFilePermissions();

            if (localUrl.equalsIgnoreCase("") &&
                    personalUrl.equalsIgnoreCase(""))
                save_file.setSelected(false);
            else
                save_file.setSelected(true);

            //mostra i pulsanti per il lettore musicale
            music_buttons.setVisibility(View.VISIBLE);
            no_records_text.setVisibility(View.INVISIBLE);

            if (mediaPlayer == null) {
                scroll_song_bar.setEnabled(false);
                mediaPlayer = new MediaPlayer();
                mediaPlayerState = MP_State.Idle;
                mediaPlayer.setOnErrorListener(mediaPlayerOnErrorListener);

                if (localUrl.equalsIgnoreCase("")
                        && personalUrl.equalsIgnoreCase("")) {
                    localFile = false;
                    cmdSetDataSource(url);
                }
                else {
                    localFile = true;
                    if (!localUrl.equals(""))
                        cmdSetDataSource(localUrl);
                    else
                        cmdSetDataSource(personalUrl);
                }

            }
            else {
                switch (mediaPlayerState) {
                    case Started:
                        play_button.setSelected(true);
                        scroll_song_bar.setMax(mediaPlayer.getDuration());
                        scroll_song_bar.setEnabled(true);
                        mScrollBar.run();
                        break;
                    case Paused:
                        scroll_song_bar.setMax(mediaPlayer.getDuration());
                        scroll_song_bar.setEnabled(true);
                        play_button.setSelected(false);
                        break;
                    case Prepared:
                        scroll_song_bar.setMax(mediaPlayer.getDuration());
                        scroll_song_bar.setEnabled(true);
                        break;
                    default:
                        play_button.setSelected(false);
                        break;
                }
            }


            // aggiunge il clicklistener sul pulsante play
            play_button.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    //controlla la presenza di una connessione internet
                    if (!Utility.isOnline(PaginaRenderActivity.this)
                            && !localFile) {
                        Snackbar.make(findViewById(android.R.id.content)
                                , R.string.no_connection
                                , Snackbar.LENGTH_SHORT)
                                .show();
                        return;
                    }

                    switch (mediaPlayerState) {
                        case Paused:
                            cmdStart();
                            break;
                        case Started:
                            cmdPause();
                            break;
                        case Initialized:
                            cmdPrepare();
                            break;
                        case Stopped:
                        case PlaybackCompleted:
                        default:
//                            localUrl = Utility.retrieveMediaFileLink(getApplicationContext(), url);
                            checkExternalFilePermissions();
                            if (localUrl.equalsIgnoreCase("")) {
                                if (personalUrl.equalsIgnoreCase("")) {
                                    localFile = false;
                                    cmdSetDataSource(url);
                                    save_file.setSelected(false);
                                } else {
                                    localFile = true;
                                    cmdSetDataSource(personalUrl);
                                    save_file.setSelected(true);
                                }

                            } else {
                                localFile = true;
                                cmdSetDataSource(localUrl);
                                save_file.setSelected(true);
                            }

                            if (mediaPlayerState == MP_State.Initialized)
                                cmdPrepare();
                            break;
                    }
                }
            });

            scroll_song_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    Log.d(getClass().getName(), "newValue: " + progress);
                    if (fromUser)
                        mediaPlayer.seekTo(progress);
                    int seconds = progress / 1000 % 60;
                    Log.d(getClass().getName(), "seconds: " + seconds);
                    int minutes = (progress / (1000 * 60));
                    Log.d(getClass().getName(), "minutes: " + minutes);
                    ((TextView) findViewById(R.id.time_text)).setText(String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            save_file.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getFab().hide();
                    hideOuterFrame();
                    if (v.isSelected()) {
                        if (personalUrl.equalsIgnoreCase("")) {
                            prevOrientation = getRequestedOrientation();
                            Utility.blockOrientation(PaginaRenderActivity.this);
                            MaterialDialog dialog = new MaterialDialog.Builder(PaginaRenderActivity.this)
                                    .title(R.string.dialog_delete_mp3_title)
                                    .content(R.string.dialog_delete_mp3)
                                    .positiveText(R.string.confirm)
                                    .negativeText(R.string.dismiss)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                            File fileToDelete = new File(localUrl);
                                            fileToDelete.delete();
                                            if (fileToDelete.getAbsolutePath().contains("/Risuscit")) {
                                                // initiate media scan and put the new things into the path array to
                                                // make the scanner aware of the location and the files you want to see
                                                MediaScannerConnection.scanFile(getApplicationContext()
                                                        , new String[] {fileToDelete.getAbsolutePath()}
                                                        , null
                                                        , null);
                                            }
                                            Snackbar.make(findViewById(android.R.id.content), R.string.file_delete, Snackbar.LENGTH_SHORT)
                                                    .show();

                                            if (mediaPlayerState == MP_State.Started
                                                    || mediaPlayerState == MP_State.Paused)
                                                cmdStop();

                                            mediaPlayer = new MediaPlayer();
                                            mediaPlayerState = MP_State.Idle;
                                            mediaPlayer.setOnErrorListener(mediaPlayerOnErrorListener);

                                            localFile = false;
                                            cmdSetDataSource(url);
                                            save_file.setSelected(false);
                                            setRequestedOrientation(prevOrientation);
                                        }
                                    })
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                            setRequestedOrientation(prevOrientation);
                                        }
                                    })
                                    .show();
                            dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                                @Override
                                public boolean onKey(DialogInterface arg0, int keyCode,
                                                     KeyEvent event) {
                                    if (keyCode == KeyEvent.KEYCODE_BACK
                                            && event.getAction() == KeyEvent.ACTION_UP) {
                                        arg0.dismiss();
                                        setRequestedOrientation(prevOrientation);
                                        return true;
                                    }
                                    return false;
                                }
                            });
                            dialog.setCancelable(false);
                        }
                        else {
                            prevOrientation = getRequestedOrientation();
                            Utility.blockOrientation(PaginaRenderActivity.this);
                            MaterialDialog dialog = new MaterialDialog.Builder(PaginaRenderActivity.this)
                                    .title(R.string.dialog_delete_link_title)
                                    .content(R.string.dialog_delete_link)
                                    .positiveText(R.string.confirm)
                                    .negativeText(R.string.dismiss)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                            Snackbar.make(findViewById(android.R.id.content), R.string.delink_delete, Snackbar.LENGTH_SHORT)
                                                    .show();

                                            if (mediaPlayerState == MP_State.Started
                                                    || mediaPlayerState == MP_State.Paused)
                                                cmdStop();

                                            mediaPlayer = new MediaPlayer();
                                            mediaPlayerState = MP_State.Idle;
                                            mediaPlayer.setOnErrorListener(mediaPlayerOnErrorListener);

                                            localFile = false;
                                            personalUrl = "";

                                            SQLiteDatabase db = listaCanti.getReadableDatabase();
                                            String sql = "DELETE FROM LOCAL_LINKS" +
                                                    "  WHERE _id =  " + idCanto;
                                            db.execSQL(sql);
                                            db.close();

                                            save_file.setSelected(false);

                                            setRequestedOrientation(prevOrientation);
                                        }
                                    })
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                            setRequestedOrientation(prevOrientation);
                                        }
                                    })
                                    .show();
                            dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                                @Override
                                public boolean onKey(DialogInterface arg0, int keyCode,
                                                     KeyEvent event) {
                                    if (keyCode == KeyEvent.KEYCODE_BACK
                                            && event.getAction() == KeyEvent.ACTION_UP) {
                                        arg0.dismiss();
                                        setRequestedOrientation(prevOrientation);
                                        return true;
                                    }
                                    return false;
                                }
                            });
                            dialog.setCancelable(false);
                        }
                    }
                    else {
                        prevOrientation = getRequestedOrientation();
                        Utility.blockOrientation(PaginaRenderActivity.this);
                        MaterialDialog dialog = new MaterialDialog.Builder(PaginaRenderActivity.this)
                                .title(R.string.download_link_title)
                                .content(R.string.downlink_message)
                                .positiveText(R.string.downlink_download)
                                .negativeText(R.string.downlink_choose)
                                .neutralText(R.string.cancel)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                        SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(PaginaRenderActivity.this);
                                        int saveLocation = pref.getInt(Utility.SAVE_LOCATION, 0);
                                        if (saveLocation == 1)
//                                            PaginaRenderActivityPermissionsDispatcher.startExternalDownloadWithCheck(PaginaRenderActivity.this);
                                            checkStoragePermissions();
                                        else
                                            startInternalDownload();
                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
//                                        setRequestedOrientation(prevOrientation);
                                        // This always works
                                        Intent i = new Intent(getApplicationContext(), ThemedFilePickerActivity.class);
//                                        // Set these depending on your use case. These are the defaults.
                                        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                                        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                                        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                                        startActivityForResult(i, REQUEST_CODE);
                                    }
                                })
                                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                        setRequestedOrientation(prevOrientation);
                                    }
                                })
                                .show();
                        dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface arg0, int keyCode,
                                                 KeyEvent event) {
                                if (keyCode == KeyEvent.KEYCODE_BACK
                                        && event.getAction() == KeyEvent.ACTION_UP) {
                                    arg0.dismiss();
                                    setRequestedOrientation(prevOrientation);
                                    return true;
                                }
                                return false;
                            }
                        });
                        dialog.setCancelable(false);
                    }
                }
            });

        }
        else {

            // aggiunge il clicklistener sul pulsante play
            play_button.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    switch (mediaPlayerState) {
                        case Paused:
                            cmdStart();
                            break;
                        case Started:
                            cmdPause();
                            break;
                        case Initialized:
                            cmdPrepare();
                            break;
                        case Stopped:
                        case PlaybackCompleted:
                        default:
                            localFile = true;
                            cmdSetDataSource(personalUrl);
                            save_file.setSelected(true);

                            if (mediaPlayerState == MP_State.Initialized)
                                cmdPrepare();
                            break;
                    }
                }
            });

            save_file.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getFab().hide();
                    hideOuterFrame();
                    if (v.isSelected()) {
                        prevOrientation = getRequestedOrientation();
                        Utility.blockOrientation(PaginaRenderActivity.this);
                        MaterialDialog dialog = new MaterialDialog.Builder(PaginaRenderActivity.this)
                                .title(R.string.dialog_delete_link_title)
                                .content(R.string.dialog_delete_link)
                                .positiveText(R.string.confirm)
                                .negativeText(R.string.dismiss)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                        Snackbar.make(findViewById(android.R.id.content), R.string.delink_delete, Snackbar.LENGTH_SHORT)
                                                .show();

                                        if (mediaPlayerState == MP_State.Started
                                                || mediaPlayerState == MP_State.Paused)
                                            cmdStop();

                                        mediaPlayer = new MediaPlayer();
                                        mediaPlayerState = MP_State.Idle;
                                        mediaPlayer.setOnErrorListener(mediaPlayerOnErrorListener);

                                        localFile = false;
                                        personalUrl = "";

                                        SQLiteDatabase db = listaCanti.getReadableDatabase();
                                        String sql = "DELETE FROM LOCAL_LINKS" +
                                                "  WHERE _id =  " + idCanto;
                                        db.execSQL(sql);
                                        db.close();

                                        save_file.setSelected(false);

                                        music_buttons.setVisibility(View.INVISIBLE);
                                        no_records_text.setVisibility(View.VISIBLE);

                                        setRequestedOrientation(prevOrientation);
                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                        setRequestedOrientation(prevOrientation);
                                    }
                                })
                                .show();
                        dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface arg0, int keyCode,
                                                 KeyEvent event) {
                                if (keyCode == KeyEvent.KEYCODE_BACK
                                        && event.getAction() == KeyEvent.ACTION_UP) {
                                    arg0.dismiss();
                                    setRequestedOrientation(prevOrientation);
                                    return true;
                                }
                                return false;
                            }
                        });
                        dialog.setCancelable(false);
                    }
                    else {
                        prevOrientation = getRequestedOrientation();
                        Utility.blockOrientation(PaginaRenderActivity.this);
                        MaterialDialog dialog = new MaterialDialog.Builder(PaginaRenderActivity.this)
                                .title(R.string.only_link_title)
                                .content(R.string.only_link)
                                .positiveText(R.string.confirm)
                                .negativeText(R.string.dismiss)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                        setRequestedOrientation(prevOrientation);
                                        // This always works
                                        Intent i = new Intent(getApplicationContext(), ThemedFilePickerActivity.class);
                                        // Set these depending on your use case. These are the defaults.
                                        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                                        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                                        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                                        startActivityForResult(i, REQUEST_CODE);
                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                        setRequestedOrientation(prevOrientation);
                                    }
                                })
                                .show();
                        dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface arg0, int keyCode,
                                                 KeyEvent event) {
                                if (keyCode == KeyEvent.KEYCODE_BACK
                                        && event.getAction() == KeyEvent.ACTION_UP) {
                                    arg0.dismiss();
                                    setRequestedOrientation(prevOrientation);
                                    return true;
                                }
                                return false;
                            }
                        });
                        dialog.setCancelable(false);
                    }
                }
            });

            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayerState = MP_State.Idle;
                mediaPlayer.setOnErrorListener(mediaPlayerOnErrorListener);
            }
            else {
                switch (mediaPlayerState) {
                    case Started:
                        play_button.setSelected(true);
                        break;
                    case Paused:
                        play_button.setSelected(false);
                        break;
                    default:
                        play_button.setSelected(false);
                        break;
                }
            }

            if (!personalUrl.equalsIgnoreCase("")) {
                save_file.setSelected(true);

                //mostra i pulsanti per il lettore musicale
                music_buttons.setVisibility(View.VISIBLE);
                no_records_text.setVisibility(View.INVISIBLE);
            }
            else {
                // nasconde i pulsanti
                save_file.setSelected(false);
                music_buttons.setVisibility(View.INVISIBLE);
                no_records_text.setVisibility(View.VISIBLE);
            }

        }

        scroll_speed_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speedValue = String.valueOf(progress);
                ((TextView) findViewById(R.id.slider_text)).setText(String.valueOf(progress) + " %");
                Log.d(getClass().toString(), "speedValue cambiato! " + speedValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        play_scroll.setSelected(false);

        play_scroll.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    play_scroll.setSelected(false);
                    scrollPlaying = false;
                    mHandler.removeCallbacks(mScrollDown);
                } else {
                    play_scroll.setSelected(true);
                    scrollPlaying = true;
                    mScrollDown.run();
                }
            }
        });

        initializeLoadingDialogs();

        mLUtils = LUtils.getInstance(PaginaRenderActivity.this);
        ImageButton fab_fullscreen_on = (ImageButton) findViewById(R.id.fab_fullscreen_on);
        drawable = DrawableCompat.wrap(fab_fullscreen_on.getDrawable());
        DrawableCompat.setTint(drawable, ContextCompat.getColor(PaginaRenderActivity.this, android.R.color.white));
        fab_fullscreen_on.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getFab().hide();
                hideOuterFrame();
                mHandler.removeCallbacks(mScrollDown);
                saveZoom();
                Bundle bundle = new Bundle();
                bundle.putString(Utility.URL_CANTO, paginaView.getUrl());
                bundle.putInt(Utility.SPEED_VALUE, scroll_speed_bar.getProgress());
                bundle.putBoolean(Utility.SCROLL_PLAYING, scrollPlaying);
                bundle.putInt(Utility.ID_CANTO, idCanto);

                Intent intent = new Intent(PaginaRenderActivity.this, PaginaRenderFullScreen.class);
                intent.putExtras(bundle);

                mLUtils.startActivityWithFadeIn(intent, paginaView, Utility.TAG_TRANSIZIONE);
            }
        });

        fab_sound_off = (ImageButton) findViewById(R.id.fab_sound_off);
        drawable = DrawableCompat.wrap(fab_sound_off.getDrawable());
        DrawableCompat.setTint(drawable, ContextCompat.getColor(PaginaRenderActivity.this, android.R.color.white));
        fab_sound_off.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                getFab().hide();
                hideOuterFrame();
                findViewById(R.id.music_controls).setVisibility(v.isSelected() ? View.GONE : View.VISIBLE);
                mostraAudioBool = !v.isSelected();
                mostraAudio = String.valueOf(mostraAudioBool);
            }
        });

        fab_favorite = (ImageButton) findViewById(R.id.fab_favorite);
        drawable = DrawableCompat.wrap(fab_favorite.getDrawable());
        DrawableCompat.setTint(drawable, ContextCompat.getColor(PaginaRenderActivity.this, android.R.color.white));
        fab_favorite.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                updateFavouriteFlag(v.isSelected() ? 1 : 0);
                getFab().hide();
                hideOuterFrame();
                Snackbar.make(findViewById(android.R.id.content)
                        , v.isSelected() ? R.string.favorite_added : R.string.favorite_removed
                        , Snackbar.LENGTH_SHORT)
                        .show();
            }
        });

        getFab().setButtonOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showOuterFrame();
            }
        });

        if (mostraAudio == null) {
            SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(this);
            mostraAudio = String.valueOf(pref.getBoolean(Utility.SHOW_AUDIO, true));
        }
        mostraAudioBool = Boolean.parseBoolean(mostraAudio);

        boolean showHelp = PreferenceManager
                .getDefaultSharedPreferences(PaginaRenderActivity.this)
                .getBoolean(PREF_FIRST_OPEN_NEW, true);

        if(showHelp) {
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(PaginaRenderActivity.this)
                    .edit();
            editor.putBoolean(PREF_FIRST_OPEN_NEW, false);
            editor.apply();
            showHelp();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.canto, menu);
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
                    prevOrientation = getRequestedOrientation();
                    Utility.blockOrientation(PaginaRenderActivity.this);
                    MaterialDialog dialog = new MaterialDialog.Builder(PaginaRenderActivity.this)
                            .title(R.string.dialog_save_tab_title)
                            .content(R.string.dialog_save_tab)
                            .positiveText(R.string.confirm)
                            .negativeText(R.string.dismiss)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                    SQLiteDatabase db = listaCanti.getReadableDatabase();
                                    String sql = "UPDATE ELENCO" +
                                            "  SET saved_tab = \'" + notaCambio + "\' " +
                                            "    , saved_barre = \'" + barreCambio + "\' " +
                                            "  WHERE _id =  " + idCanto;
                                    db.execSQL(sql);
                                    db.close();
                                    pulisciVars();
                                    mLUtils.closeActivityWithTransition();
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                    pulisciVars();
                                    mLUtils.closeActivityWithTransition();
                                }
                            })
                            .show();
                    dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface arg0, int keyCode,
                                             KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK
                                    && event.getAction() == KeyEvent.ACTION_UP) {
                                arg0.dismiss();
                                setRequestedOrientation(prevOrientation);
                                return true;
                            }
                            return false;
                        }
                    });
                    dialog.setCancelable(false);
                    break;
                }
            case R.id.action_exp_pdf:
                (new PdfExportTask()).execute();
                return true;
            case R.id.action_help_canto:
                showHelp();
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
                if (getResources().getConfiguration().locale.getLanguage().equalsIgnoreCase("uk"))
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
                if (getResources().getConfiguration().locale.getLanguage().equalsIgnoreCase("uk"))
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
                    if (getResources().getConfiguration().locale.getLanguage().equalsIgnoreCase("uk"))
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
                    if (getResources().getConfiguration().locale.getLanguage().equalsIgnoreCase("uk"))
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
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (notaCambio == null || notaSalvata == null
                    || barreCambio == null || barreSalvato == null
                    || (notaCambio.equals(notaSalvata)
                    && barreCambio.equals(barreSalvato))) {
                pulisciVars();
                mLUtils.closeActivityWithTransition();
                return true;
            }
            else {
                prevOrientation = getRequestedOrientation();
                Utility.blockOrientation(PaginaRenderActivity.this);
                MaterialDialog dialog = new MaterialDialog.Builder(PaginaRenderActivity.this)
                        .title(R.string.dialog_save_tab_title)
                        .content(R.string.dialog_save_tab)
                        .positiveText(R.string.confirm)
                        .negativeText(R.string.dismiss)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                SQLiteDatabase db = listaCanti.getReadableDatabase();
                                String sql = "UPDATE ELENCO" +
                                        "  SET saved_tab = \'" + notaCambio + "\' " +
                                        "    , saved_barre = \'" + barreCambio + "\' " +
                                        "  WHERE _id =  " + idCanto;
                                db.execSQL(sql);
                                db.close();
                                pulisciVars();
                                mLUtils.closeActivityWithTransition();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                pulisciVars();
                                mLUtils.closeActivityWithTransition();
                            }
                        })
                        .show();
                dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK
                                && event.getAction() == KeyEvent.ACTION_UP) {
                            arg0.dismiss();
                            setRequestedOrientation(prevOrientation);
                            return true;
                        }
                        return false;
                    }
                });
                dialog.setCancelable(false);
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (am != null && mediaPlayerState == MP_State.Started) {
            am.requestAudioFocus(afChangeListener,
                    // Use the music stream.
                    AudioManager.STREAM_MUSIC,
                    // Request permanent focus.
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        }

        if (notaSalvata == null) {
            if (notaCambio == null) {
                notaSalvata = notaCambio = primaNota;
            }
            else {
                notaSalvata = primaNota;
            }
        }
        else {
//	    	Log.i("NOTA SALVATA", notaSalvata);
//	    	Log.i("AVVIO", "notaCambio = " + notaCambio);
            if (notaCambio == null) {
                notaCambio = notaSalvata;
            }
        }

        if (barreSalvato == null) {
            if (barreCambio == null) {
                barreSalvato = barreCambio = primoBarre;
            }
            else {
                barreSalvato = primoBarre;
            }
        }
        else {
//	    	Log.i("BARRESALVATO", barreSalvato);
            if (barreCambio == null) {
                barreCambio = barreSalvato;
            }
        }

        getZoom();

        //fix per crash su android 4.1
        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN)
            paginaView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        HashMap<String, String> convMap = cambioAccordi.diffSemiToni(primaNota, notaCambio);
        HashMap<String, String> convMin = null;
        if (getResources().getConfiguration().locale.getLanguage().equalsIgnoreCase("uk"))
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

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            webSettings.setBuiltInZoomControls(false);
        else {
            webSettings.setBuiltInZoomControls(true);
            webSettings.setDisplayZoomControls(false);
        }

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
            play_scroll.setSelected(true);
            mScrollDown.run();
        }

        findViewById(R.id.music_controls).setVisibility(mostraAudioBool ? View.VISIBLE : View.GONE);
        fab_sound_off.setSelected(!mostraAudioBool);
        fab_favorite.setSelected(selectFavouriteFromSource() == 1);
        if (getFab().isVisible()) {
            showOuterFrame();
        }

    }

    @Override
    public void onDestroy() {
        saveZoom();
        if (am != null)
            am.abandonAudioFocus(afChangeListener);
        if (listaCanti != null)
            listaCanti.close();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Utility.AUDIO_REQUESTED, audioRequested);
    }

    public FabToolbar getFab() {
        if (mFab == null) {
            mFab = (FabToolbar) findViewById(R.id.fab_toolbar);
            mFab.setColor(getThemeUtils().accentColor());
        }
        return mFab;
    }

    private void showOuterFrame() {
        View outerFrame = findViewById(R.id.outerFrame);
        outerFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFab().hide();
                hideOuterFrame();
            }
        });
        outerFrame.setVisibility(View.VISIBLE);
    }

    private void hideOuterFrame() {
        final View outerFrame = findViewById(R.id.outerFrame);
        outerFrame.setOnClickListener(null);
        outerFrame.setVisibility(View.GONE);
    }

    public void pulisciVars() {
        saveZoom();

        Log.i(getClass().getName(), "pulisciVar()");
        Log.i(getClass().getName(), "mediaPlayerState: " + mediaPlayerState);

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            mediaPlayerState = MP_State.Idle;
        }

        //cancello il listener sullo stato del telefono, solo se avevo il permesso di settarlo, altrimenti non serve
        if (ContextCompat.checkSelfPermission(PaginaRenderActivity.this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (mgr != null)
                mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        notaCambio = null;
        barreCambio = null;

        SaveSpeed();
        if (scrollPlaying) {
            play_scroll.setSelected(false);
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

    private void cmdSetDataSource(String path){
        if(mediaPlayerState == MP_State.Idle){
            try {
                if (!localFile)
                    mediaPlayer.setDataSource(path);
                else {

                    FileInputStream fileInputStream = new FileInputStream(path);
                    mediaPlayer.setDataSource(fileInputStream.getFD());
                    fileInputStream.close();
                }
                mediaPlayerState = MP_State.Initialized;
            } catch (IllegalArgumentException | IOException | IllegalStateException e) {
                Toast.makeText(PaginaRenderActivity.this,
                        e.toString(), Toast.LENGTH_SHORT).show();
                Log.e(getClass().getName(), e.getLocalizedMessage(), e);
            }
        }else{
            Toast.makeText(PaginaRenderActivity.this,
                    "Invalid State@cmdSetDataSource - skip",
                    Toast.LENGTH_SHORT).show();
        }

        if (mediaPlayerState != MP_State.Initialized)
            showMediaPlayerState();
    }

    private void cmdPrepare(){
        prevOrientation = getRequestedOrientation();
        Utility.blockOrientation(PaginaRenderActivity.this);
        mp3Dialog.show();
        mediaPlayer.setOnPreparedListener(mediaPlayerOnPreparedListener);
        mediaPlayer.setOnCompletionListener(mediaPlayerOnCompletedListener);

        if(mediaPlayerState == MP_State.Initialized
                ||mediaPlayerState == MP_State.Stopped
                || mediaPlayerState == MP_State.PlaybackCompleted){
            try {
                mediaPlayer.prepareAsync();
            } catch (IllegalStateException e) {
                Toast.makeText(PaginaRenderActivity.this,
                        e.toString(), Toast.LENGTH_SHORT).show();
                Log.e(getClass().getName(), e.getLocalizedMessage(), e);
            }
        }else{
            Toast.makeText(PaginaRenderActivity.this,
                    "Invalid State@cmdPrepare() - skip",
                    Toast.LENGTH_SHORT).show();
        }

        if (mediaPlayerState != MP_State.Prepared
                && mediaPlayerState != MP_State.Initialized)
            showMediaPlayerState();
    }

    private void cmdStart(){
        if(mediaPlayerState == MP_State.Prepared
                ||mediaPlayerState == MP_State.Started
                ||mediaPlayerState == MP_State.Paused
                ||mediaPlayerState == MP_State.PlaybackCompleted){

            //gestisce l'abbassamento del volume in caso di altre riproduzioni (sms, etc.)
            int result = am.requestAudioFocus(afChangeListener,
                    // Use the music stream.
                    AudioManager.STREAM_MUSIC,
                    // Request permanent focus.
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mediaPlayer.start();
                play_button.setSelected(true);
                mediaPlayerState = MP_State.Started;

                mScrollBar.run();

            }
            else {
                Snackbar.make(findViewById(android.R.id.content)
                        , R.string.focus_not_allowed
                        , Snackbar.LENGTH_SHORT)
                        .show();
            }

        }else{
            Toast.makeText(PaginaRenderActivity.this,
                    "Invalid State@cmdStart() - skip",
                    Toast.LENGTH_SHORT).show();
        }
        showMediaPlayerState();
    }

    private void cmdPause(){
        if(mediaPlayerState == MP_State.Started
                ||mediaPlayerState == MP_State.Paused){
            mediaPlayer.pause();
            am.abandonAudioFocus(afChangeListener);
            play_button.setSelected(false);
            mediaPlayerState = MP_State.Paused;
        }else{
            Toast.makeText(PaginaRenderActivity.this,
                    "Invalid State@cmdPause() - skip",
                    Toast.LENGTH_SHORT).show();
        }
        showMediaPlayerState();
    }

    private void cmdStop(){
        if(mediaPlayerState == MP_State.Started
                ||mediaPlayerState == MP_State.Paused) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            am.abandonAudioFocus(afChangeListener);
            play_button.setSelected(false);
            mediaPlayerState = MP_State.Stopped;
            showMediaPlayerState();
            mediaPlayerState = MP_State.Idle;
        }else{
            Toast.makeText(PaginaRenderActivity.this,
                    "Invalid State@cmdStop() - skip",
                    Toast.LENGTH_SHORT).show();
        }

        scroll_song_bar.setEnabled(false);
    }

    private void showMediaPlayerState(){

        String state;

        switch(mediaPlayerState){
            case Idle:
                state = "Idle";
                break;
            case Initialized:
                state = "Initialized";
                break;
            case Prepared:
                state = "Prepared";
                break;
            case Started:
                state = "Started";
                break;
            case Paused:
                state = "Paused";
                break;
            case Stopped:
                state = "Stopped";
                break;
            case PlaybackCompleted:
                state = "PlaybackCompleted";
                break;
            case End:
                state = "End";
                break;
            case Error:
                state = "Error";
                break;
            case Preparing:
                state = "Preparing";
                break;
            default:
                state = "Unknown!";
        }

        Toast.makeText(PaginaRenderActivity.this
                , getString(R.string.player_state) + " " + state, Toast.LENGTH_SHORT).show();
    }

    OnErrorListener mediaPlayerOnErrorListener
            = new OnErrorListener(){

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            try {
                if (mp3Dialog.isShowing())
                    mp3Dialog.dismiss();
            }
            catch (IllegalArgumentException e) {
                Log.e(getClass().getName(), e.getLocalizedMessage(), e);
            }
            mediaPlayerState = MP_State.Error;
            showMediaPlayerState();
            return false;
        }
    };

    OnPreparedListener mediaPlayerOnPreparedListener
            =  new OnPreparedListener() {

        @Override
        public void onPrepared(MediaPlayer mp) {
            try {
                if (mp3Dialog.isShowing())
                    mp3Dialog.dismiss();
            }
            catch (IllegalArgumentException e) {
                Log.e(getClass().toString(), e.getLocalizedMessage(), e);
            }
            mediaPlayerState = MP_State.Prepared;
            cmdStart();
//            scroll_song_bar.setValueRange(0, mediaPlayer.getDuration(), false);
            scroll_song_bar.setMax(mediaPlayer.getDuration());
            scroll_song_bar.setEnabled(true);
        }
    };

    OnCompletionListener mediaPlayerOnCompletedListener
            =  new OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            cmdStop();
            mediaPlayerState = MP_State.PlaybackCompleted;
            showMediaPlayerState();
            mediaPlayerState = MP_State.Idle;
        }
    };


    private void saveZoom(){
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setRequestedOrientation(prevOrientation);
        // If the file selection was successful
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                // Get the URI of the selected file
                final Uri uri = data.getData();
//                        Log.i(FILE_CHOOSER_TAG, "Uri = " + uri.toString());
//                        try {
                // Get the file path from the URI
                String path = uri.getPath();
                Snackbar.make(findViewById(android.R.id.content),
                        getString(R.string.file_selected) + ": " + path
                        , Snackbar.LENGTH_SHORT)
                        .show();

                if (mediaPlayerState == MP_State.Started
                        || mediaPlayerState == MP_State.Paused)
                    cmdStop();
                mediaPlayer = new MediaPlayer();
                mediaPlayerState = MP_State.Idle;
                mediaPlayer.setOnErrorListener(mediaPlayerOnErrorListener);

                SQLiteDatabase db = listaCanti.getReadableDatabase();
                ContentValues values = new ContentValues();
                values.put("_id", idCanto);
                values.put("local_path", path);
                db.insert("LOCAL_LINKS", null, values);
                db.close();

                localFile = true;
                personalUrl = path;

                save_file.setSelected(true);

                //mostra i pulsanti per il lettore musicale
                music_buttons.setVisibility(View.VISIBLE);
                no_records_text.setVisibility(View.INVISIBLE);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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

            String language = getResources().getConfiguration().locale.getLanguage();

            Pattern pattern = Pattern.compile("Do#|Do|Re|Mib|Mi|Fa#|Fa|Sol#|Sol|La|Sib|Si");
            Pattern patternMinore = null;
            if (language.equalsIgnoreCase("uk")) {
                pattern = Pattern.compile("Cis|C|D|Eb|E|Fis|F|Gis|G|A|B|H");
                //inserito spazio prima di "b" per evitare che venga confuso con "Eb" o "eb"
                patternMinore = Pattern.compile("cis|c|d|eb|e|fis|f|gis|g|a| b|h");
            }

            //serve per segnarsi se si è già evidenziato il primo accordo del testo
            boolean notaHighlighed = !higlightDiff;

            while (line != null) {
                Log.d(getClass().getName(), "RIGA DA ELAB: " + line);
                if (line.contains("A13F3C") && !line.contains("<H2>") && !line.contains("<H4>")) {
                    if (language.equalsIgnoreCase("uk")) {
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
//                        Log.d(getClass().getName(), "RIGA ELAB 1: " + line);
//                        Log.d(getClass().getName(), "notaHighlighed: " + notaHighlighed);
//                        Log.d(getClass().getName(), "notaCambio: " + notaCambio);
//                        Log.d(getClass().getName(), "primaNota: " + primaNota);
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
//                        Log.d(getClass().getName(), "RIGA ELAB 2: " + line);
                        line = line.replaceAll("<K>", "</FONT><FONT COLOR='#A13F3C'>");
                        line = line.replaceAll("<K2>", "</FONT><FONT COLOR='#000000'>");
//                        Log.d(getClass().getName(), "RIGA ELAB 3: " + line);
                    }
                    else {
                        line = sb.toString();
                        if (!notaHighlighed) {
                            if (!primaNota.equalsIgnoreCase(notaCambio)) {
                                line = line.replaceFirst(notaCambio, "<SPAN STYLE=\"BACKGROUND-COLOR:#FFFF00\">" + notaCambio + "</SPAN>");
                                notaHighlighed = true;
                            }
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
                                            + getString(R.string.barre_al_tasto_I)
                                            + " "
                                            + barre
                                            + " "
                                            + getString(R.string.barre_al_tasto_II)
                                            + "</I></FONT></SPAN></H4>";
                                }
                                else {
                                    oldLine = "<H4><FONT COLOR=\"#A13F3C\"><I>"
                                            + getString(R.string.barre_al_tasto_I)
                                            + " "
                                            + barre
                                            + " "
                                            + getString(R.string.barre_al_tasto_II)
                                            + "</I></FONT></H4>";
                                }
//                                String oldLine = "<H4><FONT COLOR=\"#A13F3C\"><I>Barrè al " + barre + " tasto</I></FONT></H4>";
//                                if (language.equalsIgnoreCase("uk"))
//                                    oldLine = "<H4><FONT COLOR=\"#A13F3C\"><I>Баре на " + barre + " лад</I></FONT></H4>";
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
//                        if (language.equalsIgnoreCase("uk")) {
//                            if (!line.contains("Баре")) {
                            out.write(line);
                            out.newLine();
                        }
//                        }
//                        else {
//                            if (!line.contains("Barrè") && !line.contains("Barr&#232;")) {
//                                out.write(line);
//                                out.newLine();
//                            }
//                        }
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
            Log.e(getClass().getName(), e.getLocalizedMessage(), e);
            return null;
        }
    }

    private void showHelp() {
        Intent intent = new Intent(PaginaRenderActivity.this, IntroPaginaRender.class);
        startActivity(intent);
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            wakelock.acquire();

            try {
                InputStream input = null;
                OutputStream output = null;
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(sUrl[0]);
//                    Log.i(PaginaRenderActivity.this.getClass().toString(), "URL[0]:" + sUrl[0]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    // expect HTTP 200 OK, so we don't mistakenly save error report
                    // instead of the file
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                        return "Server returned HTTP " + connection.getResponseCode()
                                + " " + connection.getResponseMessage();

                    // this will be useful to display download percentage
                    // might be -1: server did not report the length
                    int fileLength = connection.getContentLength();

                    // download the file
                    input = connection.getInputStream();
                    output = new FileOutputStream(sUrl[1]);
//                    Log.i(PaginaRenderActivity.this.getClass().toString(), "URL[1]:" + sUrl[1]);

                    byte data[] = new byte[4096];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        // allow canceling with back button
                        if (isCancelled()) {
                            try {
                                if (output != null)
                                    output.close();
                                if (input != null)
                                    input.close();
                                File fileToDelete = new File(sUrl[1]);
                                fileToDelete.delete();
                            }
                            catch (IOException ignored) {
//                                ignored.printStackTrace();
                                Log.e(getClass().toString(), ignored.getLocalizedMessage(), ignored);
                            }
                            if (connection != null)
                                connection.disconnect();

                            return null;
                        }
                        total += count;
                        // publishing the progress....
                        if (fileLength > 0) // only if total length is known
                            publishProgress((int) (total * 100 / fileLength));
                        output.write(data, 0, count);
                    }
                } catch (Exception e) {
                    Log.e(getClass().toString(), e.getLocalizedMessage(), e);
                    return e.toString();
                } finally {
                    try {
                        if (output != null)
                            output.close();
                        if (input != null)
                            input.close();
                    }
                    catch (IOException ignored) {
//                        ignored.printStackTrace();
                        Log.e(getClass().toString(), ignored.getLocalizedMessage(), ignored);
                    }

                    if (connection != null)
                        connection.disconnect();
                }
            } finally {
                wakelock.release();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
            mProgressDialog.setProgress(0);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            if (result != null) {
                Snackbar.make(findViewById(android.R.id.content)
                        , getString(R.string.download_error) + " " + result
                        , Snackbar.LENGTH_SHORT)
                        .show();
            }
            else {
                SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(PaginaRenderActivity.this);
                int saveLocation = pref.getInt(Utility.SAVE_LOCATION, 0);
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

                if (mediaPlayerState == MP_State.Started
                        || mediaPlayerState == MP_State.Paused)
                    cmdStop();
                mediaPlayer = new MediaPlayer();
                mediaPlayerState = MP_State.Idle;
                mediaPlayer.setOnErrorListener(mediaPlayerOnErrorListener);

//                localUrl = Utility.retrieveMediaFileLink(getApplicationContext(), url);
                checkExternalFilePermissions();
                localFile = true;
                cmdSetDataSource(localUrl);
                save_file.setSelected(true);
//	    		}
            }
        }
    }

    private class PdfExportTask extends AsyncTask<String, Integer, String> {

        public PdfExportTask() {}

        @Override
        protected String doInBackground(String... sUrl) {
            HashMap<String, String> testConv = cambioAccordi.diffSemiToni(primaNota, notaCambio);
            HashMap<String, String> testConvMin = null;
            if (getResources().getConfiguration().locale.getLanguage().equalsIgnoreCase("uk"))
                testConvMin = cambioAccordi.diffSemiToniMin(primaNota, notaCambio);
            String urlHtml = "";
            if (testConv != null) {
                String nuovoFile = cambiaAccordi(testConv, barreCambio, testConvMin, false);
                if (nuovoFile != null)
                    urlHtml = nuovoFile;
            }
            else {
                urlHtml = "file:///android_asset/" + pagina + ".htm";
            }
            // step 1
            Float margin = 15f;
            Document document = new Document(PageSize.A4, margin, margin, margin, margin);
            // step 2
            try {
                localPDFPath = "";
                if (Utility.isExternalStorageWritable()) {
                    File[] fileArray = ContextCompat.getExternalFilesDirs(PaginaRenderActivity.this, null);
                    localPDFPath = fileArray[0].getAbsolutePath();
                }
                else {
                    Snackbar.make(findViewById(android.R.id.content)
                            , R.string.no_memory_writable
                            , Snackbar.LENGTH_SHORT)
                            .show();
                    this.cancel(true);
                }
                localPDFPath += "/output.pdf";
//				Log.i(getClass().toString(), "localPath:" + localPDFPath);
                PdfWriter.getInstance(document, new FileOutputStream(localPDFPath));
                // step 3
                document.open();
                Font myFontColor = FontFactory.getFont("assets/fonts/DejaVuSansMono.ttf",
                        BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 14, Font.NORMAL, BaseColor.BLACK);
                // step 4
                try {
                    String line;
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(
                                    new FileInputStream(urlHtml), "UTF-8"));

                    line = br.readLine();
                    while (line != null) {
//                        Log.i(getClass().toString(), "line:" + line);
                        if ((line.contains("000000")
                                || line.contains("A13F3C"))
                                && !line.contains("BGCOLOR")) {
                            if (line.contains("000000")) {
                                myFontColor = FontFactory.getFont("assets/fonts/DejaVuSansMono.ttf",
                                        BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 14, Font.NORMAL, BaseColor.BLACK);
                            }

                            if (line.contains("A13F3C")) {
                                myFontColor = FontFactory.getFont("assets/fonts/DejaVuSansMono.ttf",
                                        BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 14, Font.NORMAL, BaseColor.RED);
                            }
                            line = line.replaceAll("<H4>", "");
                            line = line.replaceAll("</H4>", "");
                            line = line.replaceAll("<FONT COLOR=\"#000000\">", "");
                            line = line.replaceAll("<FONT COLOR=\"#A13F3C\">", "");
                            line = line.replaceAll("<FONT COLOR='#000000'>", "");
                            line = line.replaceAll("<FONT COLOR='#A13F3C'>", "");
                            line = line.replaceAll("</FONT>", "");
                            line = line.replaceAll("<H5>", "");
                            line = line.replaceAll("<H3>", "");
                            line = line.replaceAll("<H2>", "");
                            line = line.replaceAll("</H5>", "");
                            line = line.replaceAll("</H3>", "");
                            line = line.replaceAll("</H2>", "");
                            line = line.replaceAll("<I>", "");
                            line = line.replaceAll("</I>", "");
                            line = line.replaceAll("<i>", "");
                            line = line.replaceAll("</i>", "");
                            line = line.replaceAll("<u>", "");
                            line = line.replaceAll("</u>", "");
                            line = line.replaceAll("<B>", "");
                            line = line.replaceAll("</B>", "");
                            line = line.replaceAll("<br>", "");

                            if (line.equals(""))
                                document.add(Chunk.NEWLINE);
                            else {
//                                Log.i(getClass().toString(), "line filtered:" + line);
                                Paragraph paragraph = new Paragraph(line, myFontColor);
                                document.add(paragraph);
                            }
                        }
                        else {
                            if (line.equals(""))
                                document.add(Chunk.NEWLINE);
                        }

                        line = br.readLine();
                    }
                    br.close();

                } catch (IOException e) {
                    Log.e(getClass().getName(), e.getLocalizedMessage(), e);
                }
                //step 5
                document.close();

//		        Log.i("DONE", "PDF Created!");
            }
            catch (FileNotFoundException | DocumentException e) {
                Log.e(getClass().getName(), e.getLocalizedMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            prevOrientation = getRequestedOrientation();
            Utility.blockOrientation(PaginaRenderActivity.this);
            exportDialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            if (exportDialog.isShowing())
                exportDialog.dismiss();
            File file = new File(localPDFPath);
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(Uri.fromFile(file),"application/pdf");
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            Intent intent = Intent.createChooser(target, getString(R.string.open_pdf));
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Snackbar.make(findViewById(android.R.id.content)
                        , R.string.no_pdf_reader
                        , Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void initializeLoadingDialogs() {
        mp3Dialog = new MaterialDialog.Builder(PaginaRenderActivity.this)
                .content(R.string.wait)
                .progress(true, 0)
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        setRequestedOrientation(prevOrientation);
                    }
                })
                .build();

        exportDialog = new MaterialDialog.Builder(PaginaRenderActivity.this)
                .content(R.string.export_running)
                .progress(true, 0)
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        setRequestedOrientation(prevOrientation);
                    }
                })
                .build();

        mProgressDialog = new MaterialDialog.Builder(PaginaRenderActivity.this)
                .title(R.string.download_running)
                .progress(false, 100, false)
                .positiveText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        mProgressDialog.cancel();
                    }
                })
                .build();

    }

    private void checkStoragePermissions() {
        // Here, thisActivity is the current activity
        if(ContextCompat.checkSelfPermission(PaginaRenderActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                !=PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(PaginaRenderActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                showRationaleForExternalDownload();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(PaginaRenderActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Utility.WRITE_STORAGE_RC);
            }
        }
        else
            startExternalDownload();
    }

    void startExternalDownload() {
        Log.d(getClass().getName(), " WRITE_EXTERNAL_STORAGE OK");
        if (Utility.isExternalStorageWritable()) {
            final DownloadTask downloadTask = new DownloadTask(PaginaRenderActivity.this);
            new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MUSIC), "Risuscitò").mkdirs();
//                                                      Log.i(getClass().toString(), "RISUSCITO CREATA: " + folderCreated);
            String localFile = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MUSIC).getAbsolutePath()
                    + "/Risuscitò/" + Utility.filterMediaLinkNew(url);
//                                                      Log.i(getClass().toString(), "LOCAL FILE: " + localFile);
            downloadTask.execute(url, localFile);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    downloadTask.cancel(true);
                    Snackbar.make(findViewById(android.R.id.content)
                            , R.string.download_cancelled
                            , Snackbar.LENGTH_SHORT)
                            .show();
                    setRequestedOrientation(prevOrientation);
                }
            });
        } else
            Snackbar.make(findViewById(android.R.id.content)
                    , R.string.no_memory_writable
                    , Snackbar.LENGTH_SHORT)
                    .show();
    }

    void showRationaleForExternalDownload() {
        Log.d(getClass().getName(), "WRITE_EXTERNAL_STORAGE RATIONALE");
        prevOrientation = getRequestedOrientation();
        Utility.blockOrientation(PaginaRenderActivity.this);
        MaterialDialog dialog = new MaterialDialog.Builder(PaginaRenderActivity.this)
                .title(R.string.external_storage_title)
                .content(R.string.external_storage_rationale)
                .positiveText(R.string.dialog_chiudi)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        setRequestedOrientation(prevOrientation);
                        ActivityCompat.requestPermissions(PaginaRenderActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                Utility.WRITE_STORAGE_RC);
                    }
                })
//                .callback(new MaterialDialog.ButtonCallback() {
//                    @Override
//                    public void onPositive(MaterialDialog dialog) {
//                        setRequestedOrientation(prevOrientation);
//                    }
//                })
                .show();
        dialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_UP) {
                    arg0.dismiss();
                    setRequestedOrientation(prevOrientation);
                    ActivityCompat.requestPermissions(PaginaRenderActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            Utility.WRITE_STORAGE_RC);
                    return true;
                }
                return false;
            }
        });
        dialog.setCancelable(false);
    }

    void startInternalDownload() {
        Log.d(getClass().getName(), "WRITE_EXTERNAL_STORAGE DENIED or CHOOSED INTERNAL");
        final DownloadTask internalDownloadTask = new DownloadTask(PaginaRenderActivity.this);
        String localFile = PaginaRenderActivity.this.getFilesDir()
                + "/"
                + Utility.filterMediaLink(url);
        internalDownloadTask.execute(url, localFile);

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                internalDownloadTask.cancel(true);
                Snackbar.make(findViewById(android.R.id.content)
                        , R.string.download_cancelled
                        , Snackbar.LENGTH_SHORT)
                        .show();
                setRequestedOrientation(prevOrientation);
            }
        });
    }

    private void checkExternalFilePermissions() {
        // Here, thisActivity is the current activity
        if(ContextCompat.checkSelfPermission(PaginaRenderActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                !=PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(PaginaRenderActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                showRationalForExternalFile();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(PaginaRenderActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Utility.EXTERNAL_FILE_RC);
            }
//            showDeniedForExternalFile();
            localUrl =  Utility.retrieveMediaFileLink(PaginaRenderActivity.this, url, false);
        }
        else {
            searchExternalFile(false);
//            localUrl =  Utility.retrieveMediaFileLink(PaginaRenderActivity.this, url, true);
            if (!audioRequested)
                checkPhoneStatePermission();
        }
    }

    void searchExternalFile(boolean recreate) {
        localUrl =  Utility.retrieveMediaFileLink(PaginaRenderActivity.this, url, true);
        if (recreate) {
            if (android.os.Build.VERSION.SDK_INT >= 11) {
                recreate();
            } else {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        }
    }

    void showRationalForExternalFile() {
        Log.d(getClass().getName(), "EXTERNAL_FILE RATIONALE");
        prevOrientation = getRequestedOrientation();
        Utility.blockOrientation(PaginaRenderActivity.this);
        MaterialDialog dialog = new MaterialDialog.Builder(PaginaRenderActivity.this)
                .title(R.string.external_storage_title)
                .content(R.string.external_file_rationale)
                .positiveText(R.string.dialog_chiudi)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        setRequestedOrientation(prevOrientation);
                        ActivityCompat.requestPermissions(PaginaRenderActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                Utility.EXTERNAL_FILE_RC);
                    }
                })
                .show();
        dialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_UP) {
                    arg0.dismiss();
                    setRequestedOrientation(prevOrientation);
                    ActivityCompat.requestPermissions(PaginaRenderActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            Utility.EXTERNAL_FILE_RC);
                    return true;
                }
                return false;
            }
        });
        dialog.setCancelable(false);
    }

    void showDeniedForExternalFile() {
        Log.d(getClass().getName(), " READ_PHONE_STATE DENIED");
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(PaginaRenderActivity.this)
                .edit();
        editor.putInt(Utility.SAVE_LOCATION, 0);
        editor.apply();
//        localUrl =  Utility.retrieveMediaFileLink(PaginaRenderActivity.this, url, false);
        Snackbar.make(findViewById(android.R.id.content)
                , getString(R.string.external_storage_denied)
                , Snackbar.LENGTH_SHORT)
                .show();
    }

    private void checkPhoneStatePermission() {
        // Here, thisActivity is the current activity
        audioRequested = true;
        if(ContextCompat.checkSelfPermission(PaginaRenderActivity.this,
                Manifest.permission.READ_PHONE_STATE)
                !=PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(PaginaRenderActivity.this,
                    Manifest.permission.READ_PHONE_STATE)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                showRationaleForPhoneListener();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(PaginaRenderActivity.this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        Utility.PHONE_LISTENER_RC);
            }
        }
        else
            attachPhoneListener();
    }

    void attachPhoneListener() {
        Log.d(getClass().getName(), "READ_PHONE_STATE OK");
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    void showRationaleForPhoneListener() {
        Log.d(getClass().getName(), "READ_PHONE_STATE RATIONALE");
        prevOrientation = getRequestedOrientation();
        Utility.blockOrientation(PaginaRenderActivity.this);
        MaterialDialog dialog = new MaterialDialog.Builder(PaginaRenderActivity.this)
                .title(R.string.phone_listener_title)
                .content(R.string.phone_state_rationale)
                .positiveText(R.string.dialog_chiudi)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        setRequestedOrientation(prevOrientation);
                        ActivityCompat.requestPermissions(PaginaRenderActivity.this,
                                new String[]{Manifest.permission.READ_PHONE_STATE},
                                Utility.PHONE_LISTENER_RC);
                    }
                })
                .show();
        dialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_UP) {
                    arg0.dismiss();
                    setRequestedOrientation(prevOrientation);
                    ActivityCompat.requestPermissions(PaginaRenderActivity.this,
                            new String[]{Manifest.permission.READ_PHONE_STATE},
                            Utility.PHONE_LISTENER_RC);
                    return true;
                }
                return false;
            }
        });
        dialog.setCancelable(false);
    }

    void showDeniedForPhoneListener() {
        Log.d(getClass().getName(), " READ_PHONE_STATE DENIED");
        Snackbar.make(findViewById(android.R.id.content)
                , getString(R.string.phone_listener_denied)
                , Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.d(getClass().getName(), "onRequestPermissionsResult-request: " + requestCode);
        Log.d(getClass().getName(), "onRequestPermissionsResult-result: " + grantResults[0]);
        switch (requestCode) {
            case Utility.WRITE_STORAGE_RC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the task you need to do.
                    startExternalDownload();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(PaginaRenderActivity.this)
                            .edit();
                    editor.putInt(Utility.SAVE_LOCATION, 0);
                    editor.apply();
                    Snackbar.make(findViewById(android.R.id.content)
                            , R.string.forced_private
                            , Snackbar.LENGTH_SHORT)
                            .show();
                    startInternalDownload();
                }
                return;
            }
            case Utility.PHONE_LISTENER_RC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the task you need to do.
                    attachPhoneListener();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    showDeniedForPhoneListener();
                }
                return;
            }
            case Utility.EXTERNAL_FILE_RC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the task you need to do.
                    searchExternalFile(true);
//                    localUrl =  Utility.retrieveMediaFileLink(PaginaRenderActivity.this, url, true);
//                    if (android.os.Build.VERSION.SDK_INT >= 11) {
//                        recreate();
//                    }
//                    else {
//                        Intent intent = getIntent();
//                        finish();
//                        startActivity(intent);
//                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    showDeniedForExternalFile();
                    if (!audioRequested)
                        checkPhoneStatePermission();
//                    SharedPreferences.Editor editor = PreferenceManager
//                            .getDefaultSharedPreferences(PaginaRenderActivity.this)
//                            .edit();
//                    editor.putInt(Utility.SAVE_LOCATION, 0);
//                    editor.apply();
//                    localUrl =  Utility.retrieveMediaFileLink(PaginaRenderActivity.this, url, false);
                }
            }
        }
    }

}