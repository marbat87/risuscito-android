package it.cammino.risuscito;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Xml;
import android.view.KeyEvent;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.crash.FirebaseCrash;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

import it.cammino.risuscito.services.XmlImportService;

public class ImportActivity extends AppCompatActivity {

    final String TAG = getClass().getCanonicalName();

    // We don't use namespaces
    private static final String ns = null;

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
                            .show();
                    dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface arg0, int keyCode,
                                             KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK
                                    && event.getAction() == KeyEvent.ACTION_UP) {
                                arg0.dismiss();
                                return true;
                            }
                            return false;
                        }
                    });
                    dialog.setCancelable(false);

//            getIntent().setData(null);
//            Intent i = new Intent(this, XmlImportService.class);
//            i.setAction(XmlImportService.ACTION_URL);
//            i.setData(data);
//            startService(i);
        }

    }

    private void importData(Uri data) {
        final String scheme = data.getScheme();

        if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            try {
                InputStream fis = getContentResolver().openInputStream(data);
                ListaPersonalizzata celebrazione = parse(fis);

                DatabaseCanti listaCanti = new DatabaseCanti(this);

                SQLiteDatabase db = listaCanti.getReadableDatabase();

                ContentValues values = new  ContentValues();
                values.put("titolo_lista" , celebrazione.getName());
                values.put("lista" , ListaPersonalizzata.serializeObject(celebrazione));

                db.insert("LISTE_PERS" , "" , values);

                db.close();

                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
            catch (XmlPullParserException e) {
                Log.e(TAG, "importData: " + e.getLocalizedMessage(), e);
                FirebaseCrash.log("importData: " + e.getMessage());
            }
            catch (IOException e) {
                Log.e(TAG, "importData: " + e.getLocalizedMessage(), e);
                FirebaseCrash.log("importData: " + e.getMessage());
            }
        }
    }

    public ListaPersonalizzata parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readLista(parser);
        } finally {
            in.close();
        }
    }

    private ListaPersonalizzata readLista(XmlPullParser parser) throws XmlPullParserException, IOException  {
        ListaPersonalizzata list = new ListaPersonalizzata();
        Position tempPos;

        parser.require(XmlPullParser.START_TAG, ns, "list");
        String title = parser.getAttributeValue(null, "title");
        if (title != null)
            list.setName(parser.getAttributeValue(null, "title"));
        else {

        }
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("position")) {
                tempPos = readPosition(parser);
                list.addPosizione(tempPos.getName());
                if (!tempPos.getCanto().equalsIgnoreCase("0"))
                    list.addCanto(tempPos.getCanto(), list.getNumPosizioni() - 1);
            } else {
                skip(parser);
            }
        }
        return list;
    }

    // Processes positions tags in the list.
    private Position readPosition(XmlPullParser parser) throws IOException, XmlPullParserException {
        Position result = new Position();
        parser.require(XmlPullParser.START_TAG, ns, "position");
        String name = parser.getAttributeValue(null, "name");
        String canto = readCanto(parser);
        parser.require(XmlPullParser.END_TAG, ns, "position");
        result.setName(name.trim());
        result.setCanto(canto.trim());
        return result;
    }

    // For the tags title and summary, extracts their text values.
    private String readCanto(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    static class Position {
        String name;
        String canto;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCanto() {
            return canto;
        }

        public void setCanto(String canto) {
            this.canto = canto;
        }
    }

}
