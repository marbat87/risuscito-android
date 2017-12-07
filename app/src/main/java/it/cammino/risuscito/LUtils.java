package it.cammino.risuscito;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Interpolator;

import com.google.firebase.crash.FirebaseCrash;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import it.cammino.risuscito.database.RisuscitoDatabase;
import it.cammino.risuscito.database.dao.CronologiaDao;
import it.cammino.risuscito.database.entities.Cronologia;

public class LUtils {

    final String TAG = getClass().getCanonicalName();

    private static final Interpolator INTERPOLATOR = new FastOutSlowInInterpolator();

    private final static String FILE_FORMAT = ".risuscito";

    private Activity mActivity;

    private LUtils(Activity activity) {
        mActivity = activity;
    }

    public static LUtils getInstance(Activity activity) {
        return new LUtils(activity);
    }

    public static boolean hasL() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean hasO() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public void startActivityWithTransition(final Intent intent, final View clickedView,
                                            final String transitionName) {
//        ActivityOptions options = null;
//        if (hasL() && clickedView != null && !TextUtils.isEmpty(transitionName)) {
//            options = ActivityOptions.makeSceneTransitionAnimation(
//                    mActivity, clickedView, transitionName);
//            ActivityCompat.startActivity(mActivity, intent, options.toBundle());
//        } else {
        mActivity.startActivity(intent);
        mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.hold_on);
//        }

        //aggiorno la cronologia
//        DatabaseCanti listaCanti = new DatabaseCanti(mActivity);
//        SQLiteDatabase db = listaCanti.getReadableDatabase();
//
//        String select = "SELECT COUNT(*) FROM CRONOLOGIA" +
//                "        WHERE id_canto = " + intent.getExtras().getInt("idCanto");
//        Cursor lista = db.rawQuery(select, null);
//        lista.moveToFirst();
//
//        switch (lista.getInt(0)) {
//            case 1:
//                String update = "UPDATE CRONOLOGIA" +
//                        " SET ultima_visita = CURRENT_TIMESTAMP" +
//                        " WHERE id_canto = " + intent.getExtras().getInt("idCanto");
//                db.execSQL(update);
//                break;
//            case 0:
//                ContentValues values = new ContentValues();
//                values.put("id_canto", intent.getExtras().getInt("idCanto"));
//                db.insert("CRONOLOGIA", null, values);
//                break;
//        }
//
//        lista.close();
//        db.close();

        new Thread(new Runnable() {
            @Override
            public void run() {
                CronologiaDao mDao =
                        RisuscitoDatabase.getInstance(mActivity).cronologiaDao();
                Cronologia cronologia = new Cronologia();
                cronologia.idCanto =  intent.getExtras().getInt("idCanto");
                mDao.insertCronologia(cronologia);
            }
        }).start();

    }

//    public void startActivityWithTransition(Intent intent) {
//        mActivity.startActivity(intent);
//        mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.hold_on);
//    }


//    public void startActivityWithFadeIn(Intent intent, final View clickedView,
//                                        final String transitionName) {
////        ActivityOptions options = null;
////        if (hasL() && clickedView != null && !TextUtils.isEmpty(transitionName)) {
////            options = ActivityOptions.makeSceneTransitionAnimation(
////                    mActivity, clickedView, transitionName);
////            ActivityCompat.startActivity(mActivity, intent, options.toBundle());
////        } else {
//        mActivity.startActivity(intent);
//        mActivity.overridePendingTransition(R.anim.image_fade_in, R.anim.hold_on);
////        }
//    }

    void startActivityWithFadeIn(Intent intent) {
        mActivity.startActivity(intent);
        mActivity.overridePendingTransition(R.anim.image_fade_in, R.anim.hold_on);
    }

    void closeActivityWithTransition() {
//        if (hasL())
//            mActivity.finishAfterTransition();
//        else {
        mActivity.finish();
        mActivity.overridePendingTransition(0, R.anim.slide_out_right);
//        }
    }

    void closeActivityWithFadeOut() {
//        if (hasL())
//            mActivity.finishAfterTransition();
//        else {
        mActivity.finish();
        mActivity.overridePendingTransition(0, R.anim.image_fade_out);
//        }
    }

    void goFullscreen() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            mActivity.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else mActivity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }


//    public void applyFontedTab(ViewPager viewPager, TabLayout tabLayout) {
//        TabLayout.Tab mTab;
//        for (int i = 0; i < viewPager.getAdapter().getCount(); i++) {
//            TextView tv = (TextView) mActivity.getLayoutInflater().inflate(R.layout.item_tab, null);
//            if (i == viewPager.getCurrentItem()) tv.setSelected(true);
//            tv.setText(viewPager.getAdapter().getPageTitle(i));
//            mTab = tabLayout.getTabAt(i);
//            if (mTab != null)
//                mTab.setCustomView(tv);
//        }
//    }

//    public static boolean hasICS() {
//        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
//    }

    public static boolean hasJB() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    public static boolean hasN() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    Uri listToXML(@NonNull ListaPersonalizzata lista) {

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("list");
            rootElement.setAttribute("title", lista.getName());
            doc.appendChild(rootElement);

            for (int i = 0; i < lista.getNumPosizioni(); i++) {
                Element position = doc.createElement("position");
                position.setAttribute("name", lista.getNomePosizione(i));
                if (!lista.getCantoPosizione(i).equals(""))
                    position.appendChild(doc.createTextNode(lista.getCantoPosizione(i)));
                else
                    position.appendChild(doc.createTextNode("0"));
                rootElement.appendChild(position);
            }

            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            Log.d(TAG, "listToXML: " + writer.toString());
//            writer.toString();

            File exportFile = new File(mActivity.getCacheDir().getAbsolutePath() + "/" + lista.getName() + FILE_FORMAT);
            Log.d(TAG, "listToXML: exportFile = " + exportFile.getAbsolutePath());
            FileOutputStream fos = new FileOutputStream(exportFile);
            String dataWrite = writer.toString();
            fos.write(dataWrite.getBytes());
            fos.close();

            return FileProvider.getUriForFile(mActivity, "it.cammino.risuscito.fileprovider", exportFile);

        }
        catch (ParserConfigurationException e) {
            Log.e(TAG, "listToXML: " + e.getLocalizedMessage(), e);
            FirebaseCrash.log(e.getMessage());
            return null;
        }
        catch (TransformerConfigurationException e) {
            Log.e(TAG, "listToXML: " + e.getLocalizedMessage(), e);
            FirebaseCrash.log(e.getMessage());
            return null;
        }
        catch (TransformerException e) {
            Log.e(TAG, "listToXML: " + e.getLocalizedMessage(), e);
            FirebaseCrash.log(e.getMessage());
            return null;
        }
        catch (FileNotFoundException e) {
            Log.e(TAG, "listToXML: " + e.getLocalizedMessage(), e);
            FirebaseCrash.log(e.getMessage());
            return null;
        }
        catch (IOException e) {
            Log.e(TAG, "listToXML: " + e.getLocalizedMessage(), e);
            FirebaseCrash.log(e.getMessage());
            return null;
        }

    }

    public boolean isOnTablet() {
        return (mActivity.getResources().getBoolean(R.bool.is_tablet));

    }

    public void convertIntPreferences() {
        convert(Utility.DEFAULT_INDEX);
        convert(Utility.SAVE_LOCATION);
    }

    private void convert(String prefName) {
        SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(mActivity);
        try {
            pref.getString(prefName, "0");
            Log.d(TAG, "onCreateView: " + prefName + " STRING");
        }
        catch (ClassCastException e) {
            Log.d(TAG, "onCreateView: " + prefName + " INTEGER >> CONVERTO");
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(mActivity)
                    .edit();
            editor.putString(prefName, String.valueOf(pref.getInt(prefName, 0)));
            editor.apply();
        }
    }

    // Same animation that FloatingActionButton.Behavior uses to show the FAB when the AppBarLayout enters
    void animateIn(View view) {
//        if (view.getVisibility() == View.INVISIBLE) {
        view.setVisibility(View.VISIBLE);
        ViewCompat.animate(view)
                .setDuration(200)
                .translationY(0)
                .setInterpolator(INTERPOLATOR).withLayer().setListener(null)
                .start();
//        }
    }

    @SuppressWarnings("deprecation")
    private static Spanned fromHtmlLegacy(String input) {
        return Html.fromHtml(input);
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Spanned fromHtml(String input) {
        return Html.fromHtml(input, Html.FROM_HTML_MODE_LEGACY);
    }

    public static Spanned fromHtmlWrapper(String input) {
        if (LUtils.hasN())
            return fromHtml(input);
        else
            return fromHtmlLegacy(input);
    }

}