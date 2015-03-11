package it.cammino.risuscito;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import it.cammino.risuscito.ui.ThemeableActivity;
import it.cammino.risuscito.utils.ColorChooserDialog;

public class MainActivity extends ThemeableActivity implements ColorChooserDialog.ColorCallback {

    private DrawerLayout mDrawerLayout;
    private Toolbar mActionBarToolbar;

    // list of navdrawer items that were actually added to the navdrawer, in order
    private ArrayList<Integer> mNavDrawerItems = new ArrayList<Integer>();

    // views that correspond to each navdrawer item, null if not yet created
    private View[] mNavDrawerItemViews = null;

    protected static final String SELECTED_ITEM = "oggetto_selezionato";

    protected int selectedItem;

    protected static final int NAVDRAWER_ITEM_HOMEPAGE = 0;
    protected static final int NAVDRAWER_ITEM_SEARCH = 1;
    protected static final int NAVDRAWER_ITEM_INDEXES = 2;
    protected static final int NAVDRAWER_ITEM_LISTS = 3;
    protected static final int NAVDRAWER_ITEM_FAVORITES = 4;
    protected static final int NAVDRAWER_ITEM_SETTINGS = 5;
    protected static final int NAVDRAWER_ITEM_ABOUT = 6;
    protected static final int NAVDRAWER_ITEM_DONATE = 7;
    protected static final int NAVDRAWER_ITEM_INVALID = -1;
    protected static final int NAVDRAWER_ITEM_SEPARATOR = -2;

    // titles for navdrawer items (indices must correspond to the above)
    private static final int[] NAVDRAWER_TITLE_RES_ID = new int[]{
            R.string.activity_homepage,
            R.string.search_name_text,
            R.string.title_activity_general_index,
            R.string.title_activity_custom_lists,
            R.string.action_favourites,
            R.string.title_activity_settings,
            R.string.title_activity_about,
            R.string.title_activity_donate
    };

    // icons for navdrawer items (indices must correspond to above array)
    private static final int[] NAVDRAWER_ICON_RES_ID = new int[] {
            R.drawable.ic_action_home_dark,
            R.drawable.ic_action_search_dark,
            R.drawable.ic_action_view_as_list_dark,
            R.drawable.ic_action_add_to_queue_dark,
            R.drawable.ic_action_favorite_dark,
            R.drawable.ic_action_settings_dark,
            R.drawable.ic_action_about_dark,
            R.drawable.ic_action_good_dark
    };

    private static final int TALBLET_DP = 600;
    private static final int WIDTH_320 = 320;
    private static final int WIDTH_400 = 400;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.hasNavDrawer = true;
        super.alsoLollipop = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getIntent().getBooleanExtra(Utility.DB_RESET, false)) {
            DatabaseCanti listaCanti = new DatabaseCanti(this);
            SQLiteDatabase db = listaCanti.getReadableDatabase();
            DatabaseCanti.Backup[] backup = listaCanti.backupTables(db.getVersion(), db.getVersion(), db);
            DatabaseCanti.BackupLocalLink[] backupLink = listaCanti.backupLocalLink(db.getVersion(), db.getVersion(), db);
            listaCanti.reCreateDatabse(db);
            listaCanti.repopulateDB(db.getVersion(), db.getVersion(), db, backup, backupLink);
            convertTabs(db, getIntent().getStringExtra(Utility.CHANGE_LANGUAGE));
            db.close();
            listaCanti.close();
        }

        mActionBarToolbar = (Toolbar) findViewById(R.id.risuscito_toolbar);
        mActionBarToolbar.setBackgroundColor(getThemeUtils().primaryColor());
        setSupportActionBar(mActionBarToolbar);

        setupNavDrawer();

        if (findViewById(R.id.content_frame) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                setSelectedNavDrawerItem(savedInstanceState.getInt(SELECTED_ITEM));
                return;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new Risuscito(), String.valueOf(NAVDRAWER_ITEM_HOMEPAGE)).commit();
            setSelectedNavDrawerItem(NAVDRAWER_ITEM_HOMEPAGE);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(SELECTED_ITEM, selectedItem);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Returns the navigation drawer item that corresponds to this Activity. Subclasses
     * of BaseActivity override this to indicate what nav drawer item corresponds to them
     * Return NAVDRAWER_ITEM_INVALID to mean that this Activity should not have a Nav Drawer.
     */
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_INVALID;
    }

    /**
     * Sets up the navigation drawer as appropriate. Note that the nav drawer will be
     * different depending on whether the attendee indicated that they are attending the
     * event on-site vs. attending remotely.
     */
    private void setupNavDrawer() {

        mDrawerLayout = (DrawerLayout) findViewById(R.id.my_drawer_layout);
        if (mDrawerLayout == null) {
            return;
        }
        mDrawerLayout.setStatusBarBackgroundColor(getThemeUtils().primaryColorDark());

        if (mActionBarToolbar != null) {
            mActionBarToolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
            mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDrawerLayout.openDrawer(Gravity.START);
                }
            });
        }

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        int drawerWidth  = calculateDrawerWidth();

        DrawerLayout.LayoutParams lps = new DrawerLayout.LayoutParams(
                drawerWidth,
                DrawerLayout.LayoutParams.MATCH_PARENT);
        lps.gravity = Gravity.START;

        findViewById(R.id.navdrawer).setLayoutParams(lps);

        LinearLayout.LayoutParams lps2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                Math.round(drawerWidth * 9 / 19));
        lps2.gravity = Gravity.START | Gravity.CENTER_VERTICAL;

        findViewById(R.id.navdrawer_image).setLayoutParams(lps2);
//
        // populate the nav drawer with the correct items
        populateNavDrawer();


    }

    /** Populates the navigation drawer with the appropriate items. */
    private void populateNavDrawer() {
//        boolean attendeeAtVenue = PrefUtils.isAttendeeAtVenue(this);
        mNavDrawerItems.clear();

        mNavDrawerItems.add(NAVDRAWER_ITEM_HOMEPAGE);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SEARCH);
        mNavDrawerItems.add(NAVDRAWER_ITEM_INDEXES);
        mNavDrawerItems.add(NAVDRAWER_ITEM_LISTS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_FAVORITES);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SEPARATOR);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SETTINGS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_ABOUT);
        mNavDrawerItems.add(NAVDRAWER_ITEM_DONATE);

        createNavDrawerItems();
    }

    private void createNavDrawerItems() {
        ViewGroup mDrawerItemsListContainer = (ViewGroup) findViewById(R.id.navdrawer_items_list);
        if (mDrawerItemsListContainer == null) {
            return;
        }

        mNavDrawerItemViews = new View[mNavDrawerItems.size()];
        mDrawerItemsListContainer.removeAllViews();
        int i = 0;
        for (int itemId : mNavDrawerItems) {
            mNavDrawerItemViews[i] = makeNavDrawerItem(itemId, mDrawerItemsListContainer);
            mDrawerItemsListContainer.addView(mNavDrawerItemViews[i]);
            ++i;
        }
    }

    private View makeNavDrawerItem(final int itemId, ViewGroup container) {
        boolean selected = getSelfNavDrawerItem() == itemId;
        int layoutToInflate;
        if (itemId == NAVDRAWER_ITEM_SEPARATOR) {
            layoutToInflate = R.layout.navdrawer_separator;
        } else {
            layoutToInflate = R.layout.navdrawer_item;
        }
        View view = getLayoutInflater().inflate(layoutToInflate, container, false);

        if (isSeparator(itemId)) {
            // we are done
            Utility.setAccessibilityIgnore(view);
            return view;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        int iconId = itemId >= 0 && itemId < NAVDRAWER_ICON_RES_ID.length ?
                NAVDRAWER_ICON_RES_ID[itemId] : 0;
        int titleId = itemId >= 0 && itemId < NAVDRAWER_TITLE_RES_ID.length ?
                NAVDRAWER_TITLE_RES_ID[itemId] : 0;

        // set icon and text
        iconView.setVisibility(iconId > 0 ? View.VISIBLE : View.GONE);
        if (iconId > 0) {
            iconView.setImageResource(iconId);
        }
        titleView.setText(getString(titleId));

        formatNavDrawerItem(view, itemId, selected);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNavDrawerItemClicked(itemId);
            }
        });

        return view;
    }

    private boolean isSeparator(int itemId) {
        return itemId == NAVDRAWER_ITEM_SEPARATOR;
    }

    private void formatNavDrawerItem(View view, int itemId, boolean selected) {
        if (isSeparator(itemId)) {
            // not applicable
            return;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);

        // configure its appearance according to whether or not it's selected
        view.setBackgroundResource(selected ?
                R.drawable.selected_navdrawer_item_background :
                R.drawable.navdrawer_item_background);
        view.setSoundEffectsEnabled(!selected);
        titleView.setTextColor(selected ?
                getThemeUtils().primaryColor() :
                getResources().getColor(R.color.navdrawer_text_color));
        iconView.setColorFilter(selected ?
                getThemeUtils().primaryColor() :
                getResources().getColor(R.color.navdrawer_icon_tint));
    }

    private void onNavDrawerItemClicked(final int itemId) {
        if (itemId == getSelfNavDrawerItem()) {
            mDrawerLayout.closeDrawer(Gravity.START);
            return;
        }

        goToNavDrawerItem(itemId);
        setSelectedNavDrawerItem(itemId);

    }

    private void goToNavDrawerItem(int item) {

        Fragment fragment;

        switch (item) {
            case NAVDRAWER_ITEM_HOMEPAGE:
                fragment = new Risuscito();
                break;
            case NAVDRAWER_ITEM_SEARCH:
                fragment = new GeneralSearch();
                break;
            case NAVDRAWER_ITEM_INDEXES:
                fragment = new GeneralIndex();
                break;
            case NAVDRAWER_ITEM_LISTS:
                fragment = new CustomLists();
                break;
            case NAVDRAWER_ITEM_FAVORITES:
                fragment = new FavouritesActivity();
                break;
            case NAVDRAWER_ITEM_SETTINGS:
                fragment = new PreferencesFragment();
                break;
            case NAVDRAWER_ITEM_ABOUT:
                fragment = new AboutActivity();
                break;
            case NAVDRAWER_ITEM_DONATE:
                fragment = new DonateActivity();
                break;
            default:
                fragment = new Risuscito();
                break;
        }

        //creo il nuovo fragment solo se non è lo stesso che sto già visualizzando
        Fragment myFragment = getSupportFragmentManager().findFragmentByTag(String.valueOf(item));
        if (myFragment == null || !myFragment.isVisible()) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
            transaction.replace(R.id.content_frame, fragment, String.valueOf(item)).commit();

            android.os.Handler mHandler = new android.os.Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDrawerLayout.closeDrawer(Gravity.START);
                }
            }, 250);
        }
    }

    /**
     * Sets up the given navdrawer item's appearance to the selected state. Note: this could
     * also be accomplished (perhaps more cleanly) with state-based layouts.
     */
    private void setSelectedNavDrawerItem(int itemId) {
        selectedItem = itemId;
        if (mNavDrawerItemViews != null) {
            for (int i = 0; i < mNavDrawerItemViews.length; i++) {
                if (i < mNavDrawerItems.size()) {
                    int thisItemId = mNavDrawerItems.get(i);
                    formatNavDrawerItem(mNavDrawerItemViews[i], thisItemId, itemId == thisItemId);
                }
            }
        }
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
            Fragment myFragment = getSupportFragmentManager().findFragmentByTag(String.valueOf(NAVDRAWER_ITEM_HOMEPAGE));
            if (myFragment != null && myFragment.isVisible()) {
                finish();
            }
            else {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                transaction.replace(R.id.content_frame, new Risuscito(), String.valueOf(NAVDRAWER_ITEM_HOMEPAGE)).commit();

                setSelectedNavDrawerItem(NAVDRAWER_ITEM_HOMEPAGE);
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onColorSelection(int title, int color) {

        if (title == R.string.primary_color)
            getThemeUtils().primaryColor(color);
        else if (title == R.string.accent_color)
            getThemeUtils().accentColor(color);

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
        HashMap<String, String> mappa = new HashMap<String, String>();
        if (conversion.equalsIgnoreCase("it-uk")) {
            for (int i = 0; i < CambioAccordi.accordi_it.length; i++)
                mappa.put(CambioAccordi.accordi_it[i], CambioAccordi.accordi_uk[i]);
        }
        if (conversion.equalsIgnoreCase("uk-it")) {
            for (int i = 0; i < CambioAccordi.accordi_it.length; i++)
                mappa.put(CambioAccordi.accordi_uk[i], CambioAccordi.accordi_it[i]);
        }

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
