package it.cammino.risuscito;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

public class Utility {

    //Costanti per le impostazioni
    public static final String SCREEN_ON = "sempre_acceso";
    public static final String SHOW_SECONDA = "mostra_seconda_lettura";
    public static final String SHOW_PACE = "mostra_canto_pace";
    public static final String SAVE_LOCATION = "memoria_salvataggio_scelta";
    public static final String DEFAULT_INDEX = "indice_predefinito";
    public static final String SHOW_SANTO = "mostra_santo";
    public static final String SHOW_AUDIO = "mostra_audio";
    public static final String SYSTEM_LANGUAGE = "lingua_sistema";
    public static final String DB_RESET = "db_reset";
    public static final String CHANGE_LANGUAGE = "changed";

    public static final int HIDE_DELAY = 1500;

    public static final long CLICK_DELAY = 1000;

    //Costanti per il passaggio dati alla pagina di visualizzazione canto in fullscreen
    public static final String URL_CANTO = "urlCanto";
    public static final String SPEED_VALUE = "speedValue";
    public static final String SCROLL_PLAYING = "scrollPlaying";
    public static final String ID_CANTO = "idCanto";
    public static final String TAG_TRANSIZIONE = "fullscreen";
    public static final String TRANS_PAGINA_RENDER = "paginarender";

    public static final String GIALLO = "#EBD0A5";
    public static final String BIANCO = "#FCFCFC";
    public static final String AZZURRO = "#6F949A";
    public static final String VERDE = "#8FC490";
    public static final String GRIGIO = "#CAC8BC";

    public static final int EXTERNAL_FILE_RC = 122;
    public static final int WRITE_STORAGE_RC = 123;
    public static final int PHONE_LISTENER_RC = 124;

    public static final String AUDIO_REQUESTED = "AUDIO_REQUESTED";

    //metodo che restituisce la stringa di input senza la pagina all'inizio
    public static String truncatePage(String input) {

        int length = input.length();
        int start;

        for (start = 0; start < length; start++) {

            if (input.charAt(start) == ')') {
                start += 2;
                break;
            }
        }

        return input.substring(start);
    }

    //metodo che duplica tutti gli apici presenti nella stringa
    public static String duplicaApostrofi(String input) {

        String result = input;
        int massimo  = result.length() - 1;
        char apice = '\'';

        for (int i = 0; i <= massimo; i++) {
            if (result.charAt(i) == apice ) {
                result = result.substring(0, i+1) + apice + result.substring(i+1);
                massimo++;
                i++;
            }
        }

        return result;
    }

    public static String intToString(int num, int digits) {
//        assert digits > 0 : "Invalid number of digits";
        if (BuildConfig.DEBUG && !(digits > 0))
            throw new AssertionError("Campo digits non valido");

        // create variable length array of zeros
        char[] zeros = new char[digits];
        Arrays.fill(zeros, '0');
        // format number as String
        DecimalFormat df = new DecimalFormat(String.valueOf(zeros));

        return df.format(num);
    }

    public static boolean isOnline(Activity activity) {
        ConnectivityManager cm =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /* Filtra il link di input per tenere solo il nome del file */
    public static String filterMediaLinkNew(String link) {
        if (link.length() == 0)
            return link;
        else {
            if (link.indexOf(".com") > 0) {
                int start = link.indexOf(".com/");
                return link.substring(start + 5).replaceAll("%20", "_");
            }
            else
            if (link.indexOf("ITALIANO/") > 0) {
                int start = link.indexOf("ITALIANO/");
                return link.substring(start + 9).replaceAll("%20", "_");
            }
            else
                return link;
        }
    }

    /* Filtra il link di input per tenere solo il nome del file */
    public static String filterMediaLink(String link) {
        if (link.length() == 0)
            return link;
        else {
            if (link.indexOf(".com") > 0) {
                int start = link.indexOf(".com/");
                return link.substring(start + 5);
            }
            else
            if (link.indexOf("ITALIANO/") > 0) {
                int start = link.indexOf("ITALIANO/");
                return link.substring(start + 9);
            }
            else
                return link;
        }
    }

    public static String retrieveMediaFileLink(Context activity, String link, boolean cercaEsterno) {

        if (isExternalStorageReadable() && cercaEsterno) {
//			File[] fileArray = ContextCompat.getExternalFilesDirs(activity, null);
//			File fileExt = new File(fileArray[0], filterMediaLink(link));
            //cerca file esterno con nuovi path e nome
            File fileExt = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MUSIC), "/Risuscitò/" + filterMediaLinkNew(link));
            if (fileExt.exists()) {
                Log.d("Utility.java", "FILE esterno: " + fileExt.getAbsolutePath());
                return fileExt.getAbsolutePath();
            } else {
                //cerca file esterno con vecchi path e nome
                File[] fileArray = ContextCompat.getExternalFilesDirs(activity, null);
                fileExt = new File(fileArray[0], filterMediaLink(link));
                if (fileExt.exists()) {
                    Log.d("Utility.java", "FILE esterno: " + fileExt.getAbsolutePath());
                    return fileExt.getAbsolutePath();
                }
                else
                    Log.d("Utility.java", "FILE ESTERNO NON TROVATO");
            }
        }
        else {
            Log.d("Utility.java", "isExternalStorageReadable: FALSE");
        }

        File fileInt = new File(activity.getFilesDir(), filterMediaLink(link));
        if (fileInt.exists()) {
			Log.d("Utility.java", "FILE interno: " + fileInt.getAbsolutePath());
            return fileInt.getAbsolutePath();
        }
        else
            Log.d("Utility.java", "FILE INTERNO NON TROVATO");
//		Log.i("FILE INTERNO:", "NON TROVATO");
        return "";
    }

    @SuppressLint("NewApi")
    public static void setupTransparentTints(Activity context, int color, boolean hasNavDrawer) {

//        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
//            SystemBarTintManager tintManager = new SystemBarTintManager(context);
//            tintManager.setStatusBarTintEnabled(true);
//            tintManager.setStatusBarTintColor(color);
//        }

        if (!hasNavDrawer && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            context.getWindow().setStatusBarColor(color);
    }

    @SuppressWarnings("ResourceType")
    public static void blockOrientation(Activity activity) {
        // Copied from Android docs, since we don't have these values in Froyo 2.2
        int SCREEN_ORIENTATION_REVERSE_LANDSCAPE = 8;
        int SCREEN_ORIENTATION_REVERSE_PORTRAIT = 9;

        Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        switch(activity.getResources().getConfiguration().orientation)
        {
            case Configuration.ORIENTATION_LANDSCAPE:
                if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90)
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                else
                    activity.setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270)
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                else
                    activity.setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        }
    }

    public static int random(int start, int end) {
        return ((new Random()).nextInt(end - start + 1) + start);
    }

    public static boolean hasMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isLowerCase(char ch) {
        return ch >= 'a' && ch <= 'z';
    }

    public static boolean isUpperCase(char ch) {
        return ch >= 'A' && ch <= 'Z';
    }

}
