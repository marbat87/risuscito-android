package it.cammino.risuscito;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import it.cammino.risuscito.services.XmlImportService;

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
            getIntent().setData(null);
            MaterialDialog dialog = new MaterialDialog.Builder(this)
                    .title(R.string.app_name)
                    .content(R.string.dialog_import)
                    .positiveText(R.string.confirm)
                    .negativeText(R.string.dismiss)
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

}
