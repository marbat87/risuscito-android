package it.cammino.risuscito;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Locale;

import it.cammino.risuscito.services.XmlImportService;
import it.cammino.risuscito.ui.ThemeableActivity;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ImportActivity extends AppCompatActivity {

    final String TAG = getClass().getCanonicalName();

    private BroadcastReceiver importFinishBRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Implement UI change code here once notification is received
            Log.d(getClass().getName(), "ACTION_FINISH");
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Uri data = getIntent().getData();
        if (data != null) {
            Log.d(TAG, "onCreate: data = " + data.toString());
            Log.d(TAG, "onCreate: schema = " + data.getScheme());
            getIntent().setData(null);
            MaterialDialog dialog = new MaterialDialog.Builder(this)
                    .title(R.string.app_name)
                    .content(R.string.dialog_import)
                    .positiveText(android.R.string.yes)
                    .negativeText(android.R.string.no)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            Intent i = new Intent(ImportActivity.this, XmlImportService.class);
                            i.setAction(XmlImportService.ACTION_URL);
                            i.setData(data);
                            startService(i);
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            finish();
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
                        finish();
                        return true;
                    }
                    return false;
                }
            });
            dialog.setCancelable(false);

            //registra un receiver per ricevere la notifica di completamento import e potersi terminare
            registerReceiver(importFinishBRec, new IntentFilter(
                    XmlImportService.ACTION_FINISH));
        }

    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(importFinishBRec);
        } catch (IllegalArgumentException e) {
            Log.e(getClass().getName(), e.getLocalizedMessage(), e);
        }
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {

        Configuration config = new Configuration();

        //lingua
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(newBase);
        String language = sp.getString(Utility.SYSTEM_LANGUAGE, "");
        Log.d(TAG, "attachBaseContext - language: " + language);
        //ho settato almeno una volta la lingua --> imposto quella
        if (!language.equals("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            ThemeableActivity.setSystemLocalWrapper(config, locale);
        }
        // non è ancora stata impostata nessuna lingua nelle impostazioni --> setto una lingua selezionabile oppure IT se non presente
        else {
            SharedPreferences.Editor mEditor = sp.edit();
            String mLanguage;
            switch (ThemeableActivity.getSystemLocalWrapper(newBase.getResources().getConfiguration()).getLanguage()) {
                case "uk":
                    mLanguage = "uk";
                    break;
                case "en":
                    mLanguage = "en";
                    break;
                default:
                    mLanguage = "it";
                    break;
            }
            mEditor.putString(Utility.SYSTEM_LANGUAGE, mLanguage);
            mEditor.apply();
            Locale locale = new Locale(mLanguage);
            Locale.setDefault(locale);
            ThemeableActivity.setSystemLocalWrapper(config, locale);
        }

        //fond dimension
        try {
            float actualScale = newBase.getResources().getConfiguration().fontScale;
            Log.d(getClass().toString(), "actualScale: " + actualScale);
            float systemScale = Settings.System.getFloat(getContentResolver(), Settings.System.FONT_SCALE);
            Log.d(getClass().toString(), "systemScale: " + systemScale);
            if (actualScale != systemScale)
                config.fontScale = systemScale;
        } catch (Settings.SettingNotFoundException e) {
            Log.e(getClass().toString(), "Settings.SettingNotFoundException - FUNZIONE RESIZE TESTO NON SUPPORTATA: " + e.getLocalizedMessage());
        }
        catch (NullPointerException e) {
            Log.e(getClass().toString(), "NullPointerException - FUNZIONE RESIZE TESTO NON SUPPORTATA: " + e.getLocalizedMessage());
        }

        if (LUtils.hasJB()) {
            newBase = newBase.createConfigurationContext(config);
        } else {
            //noinspection deprecation
            newBase.getResources().updateConfiguration(config, newBase.getResources().getDisplayMetrics());
        }

        //Calligraphy
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
