package it.cammino.risuscito;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.stephentuso.welcome.WelcomeScreenHelper;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.cammino.risuscito.dialogs.InputTextDialogFragment;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.slides.IntroListePers;
import it.cammino.risuscito.ui.BottomSheetFabListe;
import it.cammino.risuscito.utils.ThemeUtils;

public class CustomLists extends Fragment implements InputTextDialogFragment.SimpleInputCallback, SimpleDialogFragment.SimpleCallback {

    private String TAG  = getClass().getCanonicalName();

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private String[] titoliListe;
    private int[] idListe;
    protected DatabaseCanti listaCanti;
    private int listaDaCanc, idDaCanc, indDaModif;
    private ListaPersonalizzata celebrazioneDaCanc;
    private String titoloDaCanc;
//    private ViewPager mViewPager;
    private FloatingActionButton mFab;
    private View rootView;
    private static final String PAGE_EDITED = "pageEdited";
    public static final int TAG_CREA_LISTA = 111;
    public static final int TAG_MODIFICA_LISTA = 222;
    private TabLayout tabs;
    private LUtils mLUtils;

    private WelcomeScreenHelper mWelcomeScreen;

    private MainActivity mMainActivity;

    private BroadcastReceiver fabBRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Implement UI change code here once notification is received
            int clickedId = intent.getIntExtra(BottomSheetFabListe.DATA_ITEM_ID, 0);
            switch (clickedId) {
                case BottomSheetFabListe.CLEAN:
                    new SimpleDialogFragment.Builder((AppCompatActivity)getActivity(), CustomLists.this, "RESET_LIST")
                            .title(R.string.dialog_reset_list_title)
                            .content(R.string.reset_list_question)
                            .positiveButton(R.string.confirm)
                            .negativeButton(R.string.dismiss)
                            .show();
                    break;
                case BottomSheetFabListe.ADD_LIST:
                    new InputTextDialogFragment.Builder((AppCompatActivity)getActivity(), CustomLists.this, "NEW_LIST")
                            .title(R.string.lista_add_desc)
                            .positiveButton(R.string.dialog_chiudi)
                            .negativeButton(R.string.cancel)
                            .show();
                    break;
                case BottomSheetFabListe.SHARE_TEXT:
                    mSectionsPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem())
                            .getView().findViewById(R.id.button_condividi).performClick();
                    break;
                case BottomSheetFabListe.EDIT_LIST:
                    Bundle bundle = new Bundle();
                    bundle.putInt("idDaModif", idListe[mViewPager.getCurrentItem() - 2]);
                    bundle.putBoolean("modifica", true);
                    indDaModif = mViewPager.getCurrentItem();
                    startActivityForResult(new Intent(getActivity(), CreaListaActivity.class).putExtras(bundle), TAG_MODIFICA_LISTA);
                    getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold_on);
                    break;
                case BottomSheetFabListe.DELETE_LIST:
                    listaDaCanc = mViewPager.getCurrentItem() - 2;
                    idDaCanc = idListe[listaDaCanc];
                    SQLiteDatabase db = listaCanti.getReadableDatabase();

                    String query = "SELECT titolo_lista, lista"
                            + "  FROM LISTE_PERS"
                            + "  WHERE _id = " + idDaCanc;
                    Cursor cursor = db.rawQuery(query, null);

                    cursor.moveToFirst();
                    titoloDaCanc = cursor.getString(0);
                    celebrazioneDaCanc = (ListaPersonalizzata) ListaPersonalizzata.deserializeObject(cursor.getBlob(1));
                    cursor.close();
                    db.close();

                    new SimpleDialogFragment.Builder((AppCompatActivity)getActivity(), CustomLists.this, "DELETE_LIST")
                            .title(R.string.action_remove_list)
                            .content(R.string.delete_list_dialog)
                            .positiveButton(R.string.confirm)
                            .negativeButton(R.string.dismiss)
                            .show();
                    break;
                case BottomSheetFabListe.SHARE_FILE:
                    mSectionsPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem())
                            .getView().findViewById(R.id.button_invia_file).performClick();
                    break;
                default:
                    break;
            }
        }
    };

    @BindView(R.id.view_pager) ViewPager mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.tabs_layout_with_fab, container, false);
        ButterKnife.bind(this, rootView);

        mMainActivity = (MainActivity) getActivity();

        mMainActivity.setupToolbarTitle(R.string.title_activity_custom_lists);

        mLUtils = LUtils.getInstance(getActivity());

        //crea un istanza dell'oggetto DatabaseCanti
        listaCanti = new DatabaseCanti(getActivity());

        updateLista();

        // Create the adapter that will return a fragment for each of the three
//        mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        if (savedInstanceState != null)
            indDaModif = savedInstanceState.getInt(PAGE_EDITED, 0);
        else
            indDaModif = 0;

        if (!mMainActivity.isOnTablet()) {
            mMainActivity.enableFab(true);
            mMainActivity.enableBottombar(false);
        }

//        tabs = (TabLayout) getActivity().findViewById(R.id.material_tabs);
        tabs = mMainActivity.mTabLayout;
        tabs.setVisibility(View.VISIBLE);
        tabs.setBackgroundColor(getThemeUtils().primaryColor());
        tabs.setupWithViewPager(mViewPager);
        mLUtils.applyFontedTab(mViewPager, tabs);

        getFab().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean customList = mViewPager.getCurrentItem() >= 2;
                BottomSheetFabListe bottomSheetDialog = BottomSheetFabListe.newInstance(customList);
                bottomSheetDialog.show(getFragmentManager(), null);
            }
        });

        if (savedInstanceState != null) {
            Log.d(TAG, "onCreateView: RESTORING");
            idDaCanc = savedInstanceState.getInt("idDaCanc", 0);
            titoloDaCanc = savedInstanceState.getString("titoloDaCanc");
            listaDaCanc = savedInstanceState.getInt("listaDaCanc", 0);
            celebrazioneDaCanc = (ListaPersonalizzata) savedInstanceState.getSerializable("celebrazioneDaCanc");
            if (InputTextDialogFragment.findVisible((AppCompatActivity) getActivity(), "NEW_LIST") != null)
                InputTextDialogFragment.findVisible((AppCompatActivity) getActivity(), "NEW_LIST").setmCallback(CustomLists.this);
            if (SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "RESET_LIST") != null)
                SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "RESET_LIST").setmCallback(CustomLists.this);
            if (SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "DELETE_LIST") != null)
                SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "DELETE_LIST").setmCallback(CustomLists.this);
        }

        getActivity().registerReceiver(fabBRec, new IntentFilter(
                BottomSheetFabListe.CHOOSE_DONE));

        mWelcomeScreen = new WelcomeScreenHelper(getActivity(), IntroListePers.class);
        mWelcomeScreen.show(savedInstanceState);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.help_menu, menu);
        menu.findItem(R.id.action_help).setIcon(
                new IconicsDrawable(getActivity(), CommunityMaterial.Icon.cmd_help_circle)
                        .sizeDp(24)
                        .paddingDp(2)
                        .color(Color.WHITE));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                mWelcomeScreen.forceShow();
                return true;
        }
        return false;
    }

    /**
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PAGE_EDITED, indDaModif);
        outState.putString("titoloDaCanc", titoloDaCanc);
        outState.putInt("idDaCanc", idDaCanc);
        outState.putSerializable("celebrazioneDaCanc", celebrazioneDaCanc);
        outState.putInt("listaDaCanc", listaDaCanc);
        mWelcomeScreen.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        if (listaCanti != null)
            listaCanti.close();
        getActivity().unregisterReceiver(fabBRec);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.i(TAG, "requestCode: " + requestCode);
        if ((requestCode == TAG_CREA_LISTA || requestCode == TAG_MODIFICA_LISTA) && resultCode == Activity.RESULT_OK) {
            updateLista();
            mSectionsPagerAdapter.notifyDataSetChanged();
            tabs.setupWithViewPager(mViewPager);
            mLUtils.applyFontedTab(mViewPager, tabs);
            Handler myHandler = new Handler();
            final Runnable mMyRunnable2 = new Runnable() {
                @Override
                public void run() {
                    tabs.getTabAt(indDaModif).select();
                }
            };
            myHandler.postDelayed(mMyRunnable2, 200);
        }
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            Fragment fragment = mSectionsPagerAdapter.getRegisteredFragment(i);
            if (fragment != null && fragment.isVisible())
                fragment.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public FloatingActionButton getFab() {
        if (mFab == null) {
            mFab = mMainActivity.isOnTablet() ? (FloatingActionButton) rootView.findViewById(R.id.fab_pager) :
                    (FloatingActionButton) getActivity().findViewById(R.id.fab_pager);
            mFab.setVisibility(View.VISIBLE);
            IconicsDrawable icon = new IconicsDrawable(getActivity())
                    .icon(CommunityMaterial.Icon.cmd_plus)
                    .color(Color.WHITE)
                    .sizeDp(24)
                    .paddingDp(4);
            mFab.setImageDrawable(icon);

        }
        return mFab;
    }

    private void updateLista() {

        SQLiteDatabase db = listaCanti.getReadableDatabase();

        String query = "SELECT titolo_lista, lista, _id"
                + "  FROM LISTE_PERS A"
                + "  ORDER BY _id ASC";
        Cursor cursor = db.rawQuery(query, null);

        int total = cursor.getCount();
//	    Log.i("RISULTATI", total+"");

        titoliListe = new String[total];
        idListe = new int[total];

        cursor.moveToFirst();
        for (int i = 0; i < total; i++) {
//    		Log.i("LISTA IN POS[" + i + "]:", cursor.getString(0));
            titoliListe[i] =  cursor.getString(0);
            idListe[i] = cursor.getInt(2);
            cursor.moveToNext();
        }

        cursor.close();
        db.close();

    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        SparseArray<Fragment> registeredFragments = new SparseArray<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new CantiParolaFragment();
                case 1:
                    return new CantiEucarestiaFragment();
                default:
                    Bundle bundle=new Bundle();
//            	Log.i("INVIO", "idLista = " + idListe[position - 2]);
                    bundle.putInt("idLista", idListe[position - 2]);
                    ListaPersonalizzataFragment listaPersFrag = new ListaPersonalizzataFragment();
                    listaPersFrag.setArguments(bundle);
                    return listaPersFrag;
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }

        @Override
        public int getCount() {
            return 2 + titoliListe.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = getActivity().getResources().getConfiguration().locale;
            switch (position) {
                case 0:
                    return getString(R.string.title_activity_canti_parola).toUpperCase(l);
                case 1:
                    return getString(R.string.title_activity_canti_eucarestia).toUpperCase(l);
                default:
                    return titoliListe[position - 2].toUpperCase(l);
            }
        }

        @Override
        public int getItemPosition(Object object){
            return PagerAdapter.POSITION_NONE;
        }
    }

    private ThemeUtils getThemeUtils() {
        return ((MainActivity)getActivity()).getThemeUtils();
    }

    @Override
    public void onPositive(@NonNull String tag, @NonNull MaterialDialog dialog) {
        Log.d(TAG, "onPositive: " + tag);
        switch (tag) {
            case "NEW_LIST":
                Bundle bundle = new Bundle();
                bundle.putString("titolo", dialog.getInputEditText().getText().toString());
                bundle.putBoolean("modifica", false);
                indDaModif = 2 + idListe.length;
                startActivityForResult(new Intent(getActivity(), CreaListaActivity.class).putExtras(bundle), TAG_CREA_LISTA);
                getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold_on);
                break;
        }
    }
    @Override
    public void onNegative(@NonNull String tag, @NonNull MaterialDialog dialog) {}
    @Override
    public void onNeutral(@NonNull String tag, @NonNull MaterialDialog dialog) {}

    @Override
    public void onPositive(@NonNull String tag) {
        Log.d(TAG, "onPositive: " + tag);
        switch (tag) {
            case "RESET_LIST":
                mSectionsPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem())
                        .getView().findViewById(R.id.button_pulisci).performClick();
                break;
            case "DELETE_LIST":
                SQLiteDatabase db = listaCanti.getReadableDatabase();
                db.delete("LISTE_PERS", "_id = " + idDaCanc, null);
                db.close();

                updateLista();
                mSectionsPagerAdapter.notifyDataSetChanged();
                tabs.setupWithViewPager(mViewPager);
                mLUtils.applyFontedTab(mViewPager, tabs);
                Handler myHandler = new Handler();
                final Runnable mMyRunnable2 = new Runnable() {
                    @Override
                    public void run() {
                        tabs.getTabAt(0).select();
                    }
                };
                myHandler.postDelayed(mMyRunnable2, 200);
                Snackbar.make(getActivity().findViewById(R.id.main_content), getString(R.string.list_removed) + titoloDaCanc + "'!", Snackbar.LENGTH_LONG)
                        .setAction(R.string.cancel, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
//					    	Log.i("INDICE DA CANC", listaDaCanc+" ");
                                SQLiteDatabase db = listaCanti.getReadableDatabase();
                                ContentValues values = new ContentValues();
                                values.put("_id", idDaCanc);
                                values.put("titolo_lista", titoloDaCanc);
                                values.put("lista", ListaPersonalizzata.serializeObject(celebrazioneDaCanc));
                                db.insert("LISTE_PERS", "", values);
                                db.close();

                                updateLista();
                                mSectionsPagerAdapter.notifyDataSetChanged();
                                tabs.setupWithViewPager(mViewPager);
                                mLUtils.applyFontedTab(mViewPager, tabs);
                                Handler myHandler = new Handler();
                                final Runnable mMyRunnable2 = new Runnable() {
                                    @Override
                                    public void run() {
                                        mViewPager.setCurrentItem(listaDaCanc + 2, false);
                                    }
                                };
                                myHandler.postDelayed(mMyRunnable2, 200);
                            }
                        })
                        .setActionTextColor(getThemeUtils().accentColor())
                        .show();
                break;
        }
    }
    @Override
    public void onNegative(@NonNull String tag) {}
    @Override
    public void onNeutral(@NonNull String tag) {}
}
