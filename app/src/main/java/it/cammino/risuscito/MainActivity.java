package it.cammino.risuscito;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;

import java.util.HashMap;

import it.cammino.risuscito.ui.ThemeableActivity;

public class MainActivity extends ThemeableActivity implements ColorChooserDialog.ColorCallback {

    public DrawerLayout mDrawerLayout;
    protected static final String SELECTED_ITEM = "oggetto_selezionato";

    private int prevOrientation;

    private NavigationView mNavigationView;

    private static final int TALBLET_DP = 600;
    private static final int WIDTH_320 = 320;
    private static final int WIDTH_400 = 400;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.hasNavDrawer = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getIntent().getBooleanExtra(Utility.DB_RESET, false)) {
            (new TranslationTask()).execute();
        }

        setupNavDrawer();

        if (findViewById(R.id.content_frame) != null) {
            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null)
                mNavigationView.getMenu().getItem(savedInstanceState.getInt(SELECTED_ITEM)).setChecked(true);
            else
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new Risuscito(), String.valueOf(R.id.navigation_home)).commit();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        for (int i = 0; mNavigationView.getMenu().getItem(i) != null; i++) {
            if (mNavigationView.getMenu().getItem(i).isChecked()) {
                savedInstanceState.putInt(SELECTED_ITEM, i);
                break;
            }
        }
        //questo pezzo salva l'elenco dei titoli checkati del fragment ConsegnatiFragment, quando si ruota lo schermo
        ConsegnatiFragment consegnatiFragment = (ConsegnatiFragment)getSupportFragmentManager().findFragmentByTag(String.valueOf(R.id.navigation_consegnati));
        if (consegnatiFragment != null && consegnatiFragment.isVisible() && consegnatiFragment.getTitoliChoose() != null) {
            ConsegnatiFragment.RetainedFragment dataFragment = new ConsegnatiFragment.RetainedFragment();
            getSupportFragmentManager().beginTransaction().add(dataFragment, ConsegnatiFragment.TITOLI_CHOOSE).commit();
            dataFragment.setData(consegnatiFragment.getTitoliChoose());
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setupNavDrawer() {

        mDrawerLayout = (DrawerLayout) findViewById(R.id.my_drawer_layout);
        if (mDrawerLayout == null) {
            return;
        }
        mDrawerLayout.setStatusBarBackgroundColor(getThemeUtils().primaryColorDark());
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        int drawerWidth  = calculateDrawerWidth();

        DrawerLayout.LayoutParams lps = new DrawerLayout.LayoutParams(
                drawerWidth,
                DrawerLayout.LayoutParams.MATCH_PARENT);
        lps.gravity = Gravity.START;

        findViewById(R.id.navigation_view).setLayoutParams(lps);

        LinearLayout.LayoutParams lps2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                Math.round(drawerWidth * 9 / 19));
        lps2.gravity = Gravity.START | Gravity.CENTER_VERTICAL;

        findViewById(R.id.navdrawer_image).setLayoutParams(lps2);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                Fragment fragment;

                switch (menuItem.getItemId()) {
                    case R.id.navigation_home:
                        fragment = new Risuscito();
                        break;
                    case R.id.navigation_search:
                        fragment = new GeneralSearch();
                        break;
                    case R.id.navigation_indexes:
                        fragment = new GeneralIndex();
                        break;
                    case R.id.navitagion_lists:
                        fragment = new CustomLists();
                        break;
                    case R.id.navigation_favorites:
                        fragment = new FavouritesActivity();
                        break;
                    case R.id.navigation_settings:
                        fragment = new PreferencesFragment();
                        break;
                    case R.id.navigation_changelog:
                        fragment = new AboutActivity();
                        break;
                    case R.id.navigation_donate:
                        fragment = new DonateActivity();
                        break;
                    case R.id.navigation_consegnati:
                        fragment = new ConsegnatiFragment();
                        break;
                    case R.id.navigation_history:
                        fragment = new HistoryFragment();
                        break;
                    default:
                        fragment = new Risuscito();
                        break;
                }

                //creo il nuovo fragment solo se non è lo stesso che sto già visualizzando
                Fragment myFragment = getSupportFragmentManager().findFragmentByTag(String.valueOf(menuItem.getItemId()));
                if (myFragment == null || !myFragment.isVisible()) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                    transaction.replace(R.id.content_frame, fragment, String.valueOf(menuItem.getItemId())).commit();

                    android.os.Handler mHandler = new android.os.Handler();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mDrawerLayout.closeDrawer(GravityCompat.START);
                        }
                    }, 250);
                }
                return true;
            }

        });

        ColorStateList mIconStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked}, //1
                        new int[]{} //2
                },
                new int[] {
                        getThemeUtils().primaryColor(), //1
                        ContextCompat.getColor(MainActivity.this, R.color.navdrawer_icon_tint) // 2
                }
        );

        ColorStateList mTextStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked}, //1
                        new int[]{} //2
                },
                new int[] {
                        getThemeUtils().primaryColor(), //1
                        ContextCompat.getColor(MainActivity.this, R.color.navdrawer_text_color) //2
                }
        );

        mNavigationView.setItemIconTintList(mIconStateList);
        mNavigationView.setItemTextColor(mTextStateList);

    }

    private int calculateDrawerWidth() {

        //Recupero dp di larghezza e altezza dello schermo
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
//        Log.i(getClass().toString(), "dpHeight:" + dpHeight);
//        Log.i(getClass().toString(), "dpWidth:" + dpWidth);

        //recupero l'altezza dell'actionbar
        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(R.attr.actionBarSize, value, true);
        TypedValue.coerceToString(value.type, value.data);
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float actionBarSize = value.getDimension(displayMetrics) / displayMetrics.density;
//        Log.i(getClass().toString(), "actionBarSize:" + actionBarSize);

        // min(altezza, larghezza) - altezza actionbar
        float smallestDim = Math.min(dpWidth, dpHeight);
//        Log.i(getClass().toString(), "smallestDim:" + smallestDim);
        int difference = Math.round((smallestDim - actionBarSize) * displayMetrics.density);
//        Log.i(getClass().toString(), "difference:" + difference);

        int maxWidth = Math.round(WIDTH_320 * displayMetrics.density);
        if (smallestDim >= TALBLET_DP)
            maxWidth = Math.round(WIDTH_400 * displayMetrics.density);
//        Log.i(getClass().toString(), "maxWidth:" + maxWidth);

        return Math.min(difference, maxWidth);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            Fragment myFragment = getSupportFragmentManager().findFragmentByTag(String.valueOf(R.id.navigation_home));
            if (myFragment != null && myFragment.isVisible()) {
                finish();
                return true;
            }

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
            transaction.replace(R.id.content_frame, new Risuscito(), String.valueOf(R.id.navigation_home)).commit();
            mNavigationView.getMenu().getItem(0).setChecked(true);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog colorChooserDialog, @ColorInt int color) {
        if (colorChooserDialog.isAccentMode())
            getThemeUtils().accentColor(color);
        else
            getThemeUtils().primaryColor(color);

        if (android.os.Build.VERSION.SDK_INT >= 11) {
            recreate();
        }
        else {
            Intent i = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage( getBaseContext().getPackageName() );
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    }

    //converte gli accordi salvati dalla lingua vecchia alla nuova
    private void convertTabs(SQLiteDatabase db, String conversion) {
//        Log.i(getClass().toString(), "CONVERSION: " + conversion);
        HashMap<String, String> mappa = null;
        if (conversion.equalsIgnoreCase("it-uk")) {
            mappa = new HashMap<>();
            for (int i = 0; i < CambioAccordi.accordi_it.length; i++)
                mappa.put(CambioAccordi.accordi_it[i], CambioAccordi.accordi_uk[i]);
        }
        if (conversion.equalsIgnoreCase("uk-it")) {
            mappa = new HashMap<>();
            for (int i = 0; i < CambioAccordi.accordi_it.length; i++)
                mappa.put(CambioAccordi.accordi_uk[i], CambioAccordi.accordi_it[i]);
        }
        if (mappa != null) {
            String query = "SELECT _id, saved_tab" +
                    "  FROM ELENCO";
            Cursor cursor = db.rawQuery(query, null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                if (cursor.getString(1) != null && !cursor.getString(1).equals("")) {
//                Log.i(getClass().toString(),"ID " + cursor.getInt(0) +  " -> CONVERTO DA " + cursor.getString(1) + " A " + mappa.get(cursor.getString(1)) );
                    query = "UPDATE ELENCO" +
                            "  SET saved_tab = \'" + mappa.get(cursor.getString(1)) + "\' " +
                            "  WHERE _id =  " + cursor.getInt(0);
                    db.execSQL(query);
                }
                cursor.moveToNext();
            }
        }
    }

    private class TranslationTask extends AsyncTask<String, Integer, String> {

        public TranslationTask() {}

        private MaterialDialog translationDialog;

        @Override
        protected String doInBackground(String... sUrl) {
            getIntent().removeExtra(Utility.DB_RESET);
            DatabaseCanti listaCanti = new DatabaseCanti(MainActivity.this);
            SQLiteDatabase db = listaCanti.getReadableDatabase();
            DatabaseCanti.Backup[] backup = listaCanti.backupTables(db.getVersion(), db.getVersion(), db);
            DatabaseCanti.BackupLocalLink[] backupLink = listaCanti.backupLocalLink(db.getVersion(), db.getVersion(), db);
            listaCanti.reCreateDatabse(db);
            listaCanti.repopulateDB(db.getVersion(), db.getVersion(), db, backup, backupLink);
            convertTabs(db, getIntent().getStringExtra(Utility.CHANGE_LANGUAGE));
            db.close();
            listaCanti.close();
            return "";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            prevOrientation = getRequestedOrientation();
            Utility.blockOrientation(MainActivity.this);
            translationDialog = new MaterialDialog.Builder(MainActivity.this)
                    .content(R.string.translation_running)
                    .progress(true, 0)
                    .dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            setRequestedOrientation(prevOrientation);
                        }
                    })
                    .show();
        }

        @Override
        protected void onPostExecute(String result) {
            getIntent().removeExtra(Utility.CHANGE_LANGUAGE);
            try {
                if (translationDialog.isShowing())
                    translationDialog.dismiss();
            }
            catch (IllegalArgumentException e) {
                Log.e(getClass().getName(), e.getLocalizedMessage(), e);
            }
        }
    }

    public void setupToolbar(View toolbar, int titleResId) {
        Toolbar mActionToolbar = (Toolbar) toolbar;
        setSupportActionBar(mActionToolbar);
        mActionToolbar.setBackgroundColor(getThemeUtils().primaryColor());
        getSupportActionBar().setTitle("");
        ((TextView)toolbar.findViewById(R.id.main_toolbarTitle)).setText(titleResId);
        mActionToolbar.setNavigationIcon(R.drawable.ic_menu_24dp);
        Drawable drawable = DrawableCompat.wrap(mActionToolbar.getNavigationIcon());
        DrawableCompat.setTint(drawable, ContextCompat.getColor(MainActivity.this, android.R.color.white));
        mActionToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

}
