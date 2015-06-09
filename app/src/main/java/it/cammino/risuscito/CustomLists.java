package it.cammino.risuscito;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.Locale;

import it.cammino.risuscito.utils.ThemeUtils;

public class CustomLists extends Fragment  {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private String[] titoliListe;
    private int[] idListe;
    protected DatabaseCanti listaCanti;
    private int listaDaCanc;
    private ListaPersonalizzata celebrazioneDaCanc;
    private String titoloDaCanc;
    private int prevOrientation;
    private ViewPager mViewPager;
    //    TabPageIndicator mSlidingTabLayout = null;
    private FloatingActionsMenu mFab1;
    public FloatingActionButton fabAddLista, fabPulisci, fabEdit, fabDelete;
    private View rootView;
    private static final String PAGE_VIEWED = "pageViewed";
    public static final int TAG_CREA_LISTA = 111;
    public static final int TAG_MODIFICA_LISTA = 222;
    private MaterialDialog dialog;
    private TabLayout tabs;
    private int lastPosition;
    private LUtils mLUtils;
//    private TintEditText titleInput;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.tabs_layout_with_fab, container, false);
//        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_activity_custom_lists);
//        ((TextView)((MainActivity) getActivity()).findViewById(R.id.main_toolbarTitle)).setText(R.string.title_activity_custom_lists);
//        ((MainActivity) getActivity()).getSupportActionBar().setElevation(0);
        ((MainActivity) getActivity()).setupToolbar(rootView.findViewById(R.id.risuscito_toolbar), R.string.title_activity_custom_lists);

        mLUtils = LUtils.getInstance(getActivity());

        //crea un istanza dell'oggetto DatabaseCanti
        listaCanti = new DatabaseCanti(getActivity());

        updateLista();

        // Create the adapter that will return a fragment for each of the three
        mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

//        mSlidingTabLayout = (TabPageIndicator) rootView.findViewById(R.id.sliding_tabs);
//        mSlidingTabLayout.setBackgroundColor(getThemeUtils().primaryColor());
//        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);

//        Resources res = getResources();
//        mSlidingTabLayout.setSelectedIndicatorColors(res.getColor(android.R.color.white));
//        mSlidingTabLayout.setDistributeEvenly(false);
//        mSlidingTabLayout.setViewPager(mViewPager);

//        mSectionsPagerAdapter.notifyDataSetChanged();
//        if (savedInstanceState != null)
//            mSlidingTabLayout.setViewPager(mViewPager, savedInstanceState.getInt(PAGE_VIEWED, 0));
//        else {
//            mSlidingTabLayout.setViewPager(mViewPager, 1);
//        }

//        MaterialTabs tabs = (MaterialTabs) rootView.findViewById(R.id.material_tabs);
//        tabs.setBackgroundColor(getThemeUtils().primaryColor());
//        tabs.setViewPager(mViewPager);

        if (savedInstanceState != null)
            lastPosition = savedInstanceState.getInt(PAGE_VIEWED, 0);
        else
            lastPosition = 0;

        tabs = (TabLayout) rootView.findViewById(R.id.material_tabs);
        tabs.setBackgroundColor(getThemeUtils().primaryColor());
        tabs.setupWithViewPager(mViewPager);
        mLUtils.applyFontedTab(mViewPager, tabs);

        getFab1().setColorNormal(getThemeUtils().accentColor());
        getFab1().setColorPressed(getThemeUtils().accentColorDark());
        getFab1().setIcon(R.drawable.ic_add_white_24dp);
        getFab1().setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                showOuterFrame();
            }

            @Override
            public void onMenuCollapsed() {
                hideOuterFrame();
            }
        });

        fabAddLista = (FloatingActionButton) rootView.findViewById(R.id.fab_add_lista);
        fabAddLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFab1().toggle();
                prevOrientation = getActivity().getRequestedOrientation();
                Utility.blockOrientation(getActivity());
                dialog = new MaterialDialog.Builder(getActivity())
                        .title(R.string.lista_add_desc)
                        .positiveText(R.string.dialog_chiudi)
                        .negativeText(R.string.cancel)
                        .input("", "", false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                            }
                        })
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                //to hide soft keyboard
                                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                                        .hideSoftInputFromWindow(dialog.getInputEditText().getWindowToken(), 0);
                                getActivity().setRequestedOrientation(prevOrientation);
                                Bundle bundle = new Bundle();
                                bundle.putString("titolo", dialog.getInputEditText().getText().toString());
                                bundle.putBoolean("modifica", false);
//                                startActivity(new Intent(getActivity(), CreaListaActivity.class).putExtras(bundle));
                                lastPosition = mViewPager.getCurrentItem();
                                startActivityForResult(new Intent(getActivity(), CreaListaActivity.class).putExtras(bundle), TAG_CREA_LISTA);
                                getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold_on);
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                //to hide soft keyboard
                                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                                        .hideSoftInputFromWindow(dialog.getInputEditText().getWindowToken(), 0);
                                getActivity().setRequestedOrientation(prevOrientation);
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
                            getActivity().setRequestedOrientation(prevOrientation);
                            return true;
                        }
                        return false;
                    }
                });
                dialog.getInputEditText().setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                dialog.setCancelable(false);
                //to show soft keyboard
                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });

        fabPulisci = (FloatingActionButton) rootView.findViewById(R.id.fab_pulisci);
        fabPulisci.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFab1().toggle();
                prevOrientation = getActivity().getRequestedOrientation();
                Utility.blockOrientation(getActivity());
                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .title(R.string.dialog_reset_list_title)
                        .content(R.string.reset_list_question)
                        .positiveText(R.string.confirm)
                        .negativeText(R.string.dismiss)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                mSectionsPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem())
                                        .getView().findViewById(R.id.button_pulisci).performClick();
                                getActivity().setRequestedOrientation(prevOrientation);
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                getActivity().setRequestedOrientation(prevOrientation);
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
                            getActivity().setRequestedOrientation(prevOrientation);
                            return true;
                        }
                        return false;
                    }
                });
                dialog.setCancelable(false);
            }
        });


        fabEdit = (FloatingActionButton) rootView.findViewById(R.id.fab_edit_lista);
        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFab1().toggle();
                Bundle bundle = new Bundle();
                bundle.putInt("idDaModif", idListe[mViewPager.getCurrentItem() - 2]);
                bundle.putBoolean("modifica", true);
//                startActivity(new Intent(getActivity(), CreaListaActivity.class).putExtras(bundle));
                lastPosition = mViewPager.getCurrentItem();
                startActivityForResult(new Intent(getActivity(), CreaListaActivity.class).putExtras(bundle), TAG_MODIFICA_LISTA);
                getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold_on);
            }
        });

        fabDelete = (FloatingActionButton) rootView.findViewById(R.id.fab_delete_lista);
        fabDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFab1().toggle();
                listaDaCanc = mViewPager.getCurrentItem() - 2;
                lastPosition = mViewPager.getCurrentItem();
//                SnackbarManager.show(
//                        Snackbar.with(getActivity())
//                                .text(getString(R.string.snackbar_list_delete) + titoliListe[listaDaCanc] + "'?")
//                                .actionLabel(getString(R.string.snackbar_remove))
//                                .actionListener(new ActionClickListener() {
//                                    @Override
//                                    public void onActionClicked(Snackbar snackbar) {
//                                        SQLiteDatabase db = listaCanti.getReadableDatabase();
//
////					    	Log.i("INDICE DA CANC", listaDaCanc+" ");
//
//                                        String sql = "DELETE FROM LISTE_PERS"
//                                                + " WHERE _id = " + idListe[listaDaCanc];
//                                        db.execSQL(sql);
//                                        db.close();
//
//                                        updateLista();
//                                        mSectionsPagerAdapter.notifyDataSetChanged();
////                                        mSlidingTabLayout.setViewPager(mViewPager);
//                                    }
//                                })
//                                .actionColor(getThemeUtils().accentColor())
//                        , getActivity());
                SQLiteDatabase db = listaCanti.getReadableDatabase();

                String query = "SELECT titolo_lista, lista"
                        + "  FROM LISTE_PERS"
                        + "  WHERE _id = " + idListe[listaDaCanc];
                Cursor cursor = db.rawQuery(query, null);

                cursor.moveToFirst();
                titoloDaCanc = cursor.getString(0);
                celebrazioneDaCanc = (ListaPersonalizzata) ListaPersonalizzata.deserializeObject(cursor.getBlob(1));
                cursor.close();

                db.delete("LISTE_PERS", "_id = " + idListe[listaDaCanc], null);
                db.close();

                updateLista();
                mSectionsPagerAdapter.notifyDataSetChanged();
                tabs.setupWithViewPager(mViewPager);
                mLUtils.applyFontedTab(mViewPager, tabs);
                final Runnable mMyRunnable = new Runnable() {
                    @Override
                    public void run() {
                        tabs.getTabAt(0).select();
                    }
                };
                Handler myHandler = new Handler();
                myHandler.postDelayed(mMyRunnable, 200);
                Snackbar.make(getActivity().findViewById(R.id.main_content), getString(R.string.list_removed) + titoloDaCanc + "'!", Snackbar.LENGTH_LONG)
                        .setAction(R.string.cancel, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
//					    	Log.i("INDICE DA CANC", listaDaCanc+" ");
                                SQLiteDatabase db = listaCanti.getReadableDatabase();
                                ContentValues values = new  ContentValues();
                                values.put("titolo_lista" , titoloDaCanc);
                                values.put("lista", ListaPersonalizzata.serializeObject(celebrazioneDaCanc));
                                db.insert("LISTE_PERS", "", values);
                                db.close();

                                updateLista();
                                mSectionsPagerAdapter.notifyDataSetChanged();
                                tabs.setupWithViewPager(mViewPager);
                                mLUtils.applyFontedTab(mViewPager, tabs);
                                final Runnable mMyRunnable = new Runnable() {
                                    @Override
                                    public void run() {
//                                        tabs.getTabAt(0).select();
                                        mViewPager.setCurrentItem(lastPosition, false);
                                    }
                                };
                                Handler myHandler = new Handler();
                                myHandler.postDelayed(mMyRunnable, 200);
                            }
                        })
                        .setActionTextColor(getThemeUtils().accentColor())
                        .show();

//                Snackbar.make(getActivity().findViewById(R.id.main_content), getString(R.string.snackbar_list_delete) + titoliListe[listaDaCanc] + "'?", Snackbar.LENGTH_LONG)
//                        .setAction(R.string.snackbar_remove, new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                SQLiteDatabase db = listaCanti.getReadableDatabase();
////					    	Log.i("INDICE DA CANC", listaDaCanc+" ");
//                                String sql = "DELETE FROM LISTE_PERS"
//                                        + " WHERE _id = " + idListe[listaDaCanc];
//                                db.execSQL(sql);
//                                db.close();
//
//                                updateLista();
//                                mSectionsPagerAdapter.notifyDataSetChanged();
//                                mLUtils.applyFontedTab(mViewPager, tabs);
//                                final Runnable mMyRunnable = new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        tabs.getTabAt(0).select();
//                                    }
//                                };
//                                Handler myHandler = new Handler();
//                                myHandler.postDelayed(mMyRunnable, 200);
//                            }
//                        })
//                        .setActionTextColor(getThemeUtils().accentColor())
//                        .show();
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
//        updateLista();
//        mSectionsPagerAdapter.notifyDataSetChanged();
//        mSlidingTabLayout.setViewPager(mViewPager);
        if (getFab1().isExpanded()) {
            showOuterFrame();
        }
    }

    @Override
    public void onDestroy() {
        if (listaCanti != null)
            listaCanti.close();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PAGE_VIEWED, lastPosition);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.custom_list, menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == TAG_CREA_LISTA || requestCode == TAG_MODIFICA_LISTA) && resultCode == Activity.RESULT_OK) {
            updateLista();
            mSectionsPagerAdapter.notifyDataSetChanged();
            tabs.setupWithViewPager(mViewPager);
            mLUtils.applyFontedTab(mViewPager, tabs);
            final Runnable mMyRunnable = new Runnable() {
                @Override
                public void run() {
                    mViewPager.setCurrentItem(lastPosition, false);
                }
            };
            Handler myHandler = new Handler();
            myHandler.postDelayed(mMyRunnable, 400);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static void applyFontedTab(Activity activity, ViewPager viewPager, TabLayout tabLayout) {
        for (int i = 0; i < viewPager.getAdapter().getCount(); i++) {
            TextView tv = (TextView) activity.getLayoutInflater().inflate(R.layout.item_tab, null);
            if (i == viewPager.getCurrentItem()) tv.setSelected(true);
            tv.setText(viewPager.getAdapter().getPageTitle(i));
            tabLayout.getTabAt(i).setCustomView(tv);
        }
    }

    public FloatingActionsMenu getFab1() {
        if (mFab1 == null)
            mFab1 = (FloatingActionsMenu) rootView.findViewById(R.id.fab_pager);
        return mFab1;
    }

    private void showOuterFrame() {
        View outerFrame = rootView.findViewById(R.id.outerFrame);
        outerFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFab1().collapse();
            }
        });
        outerFrame.setVisibility(View.VISIBLE);
    }

    private void hideOuterFrame() {
        final View outerFrame = rootView.findViewById(R.id.outerFrame);
        outerFrame.setOnClickListener(null);
        outerFrame.setVisibility(View.GONE);
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
        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

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
//            	Log.i("INVIO", "position = " + position);
//            	Log.i("INVIO", "idLista = " + idListe[position - 2]);
                    bundle.putInt("position", position);
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
//				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                    return getString(R.string.title_activity_canti_parola).toUpperCase(l);
//				else
//					return getString(R.string.title_activity_canti_parola);
                case 1:
//				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                    return getString(R.string.title_activity_canti_eucarestia).toUpperCase(l);
//				else
//					return getString(R.string.title_activity_canti_eucarestia);
                default:
//				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                    return titoliListe[position - 2].toUpperCase(l);
//				else
//					return titoliListe[position - 2];
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
}
