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
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.alexkolpa.fabtoolbar.FabToolbar;

import java.util.Locale;

import it.cammino.risuscito.utils.ThemeUtils;

public class CustomLists extends Fragment  {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private String[] titoliListe;
    private int[] idListe;
    protected DatabaseCanti listaCanti;
    private int listaDaCanc, idDaCanc, indDaModif;
    private ListaPersonalizzata celebrazioneDaCanc;
    private String titoloDaCanc;
    private int prevOrientation;
    private ViewPager mViewPager;
    //    TabPageIndicator mSlidingTabLayout = null;
    private FabToolbar mFab;
    //    public FloatingActionButton fabAddLista, fabPulisci, fabEdit, fabDelete;
    public View fabEdit, fabDelete;
    private View rootView;
    private static final String PAGE_VIEWED = "pageViewed";
    private static final String PAGE_EDITED = "pageEdited";
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

        if (savedInstanceState != null) {
            lastPosition = savedInstanceState.getInt(PAGE_VIEWED, 0);
            indDaModif = savedInstanceState.getInt(PAGE_EDITED, 0);
        }
        else {
            lastPosition = 0;
            indDaModif = 0;
        }

        tabs = (TabLayout) rootView.findViewById(R.id.material_tabs);
        tabs.setBackgroundColor(getThemeUtils().primaryColor());
//        tabs.setupWithViewPager(mViewPager);
//        tabs.setupWithViewPager(mViewPager);
//        mLUtils.applyFontedTab(mViewPager, tabs);
        tabs.post(new Runnable() {
            @Override
            public void run() {
                tabs.setupWithViewPager(mViewPager);
                mLUtils.applyFontedTab(mViewPager, tabs);
            }
        });
//        final Runnable mMyRunnable = new Runnable() {
//            @Override
//            public void run() {
//                tabs.setupWithViewPager(mViewPager);
//                mLUtils.applyFontedTab(mViewPager, tabs);
//            }
//        };
        Handler myHandler = new Handler();
//        myHandler.postDelayed(mMyRunnable, 200);
        final Runnable mMyRunnable2 = new Runnable() {
            @Override
            public void run() {
                tabs.getTabAt(lastPosition).select();
            }
        };
        myHandler.postDelayed(mMyRunnable2, 200);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int arg0) {}
            public void onPageScrolled(int arg0, float arg1, int arg2) {}
            public void onPageSelected(int currentPage) {
                lastPosition = currentPage;
            }
        });

//        mLUtils.applyFontedTab(mViewPager, tabs);

//        getFab1().setColorNormal(getThemeUtils().accentColor());
//        getFab1().setColorPressed(getThemeUtils().accentColorDark());
//        getFab1().setIcon(R.drawable.ic_add_white_24dp);
//        getFab1().setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
//            @Override
//            public void onMenuExpanded() {
//                showOuterFrame();
//            }
//
//            @Override
//            public void onMenuCollapsed() {
//                hideOuterFrame();
//            }
//        });

//        fabAddLista = (FloatingActionButton) rootView.findViewById(R.id.fab_add_lista);
        rootView.findViewById(R.id.fab_add_lista).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFab().hide();
                hideOuterFrame();
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
                                indDaModif = 2 + idListe.length;
//                                startActivity(new Intent(getActivity(), CreaListaActivity.class).putExtras(bundle));
//                                lastPosition = mViewPager.getCurrentItem();
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
                dialog.getInputEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                dialog.setCancelable(false);
                //to show soft keyboard
                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });

//        fabPulisci = (FloatingActionButton) rootView.findViewById(R.id.fab_pulisci);
        rootView.findViewById(R.id.fab_pulisci).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFab().hide();
                hideOuterFrame();
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

        rootView.findViewById(R.id.fab_condividi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFab().hide();
                hideOuterFrame();
                mSectionsPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem())
                        .getView().findViewById(R.id.button_condividi).performClick();
            }
        });


        fabEdit = rootView.findViewById(R.id.fab_edit_lista);
        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFab().hide();
                hideOuterFrame();
                Bundle bundle = new Bundle();
                bundle.putInt("idDaModif", idListe[mViewPager.getCurrentItem() - 2]);
                bundle.putBoolean("modifica", true);
                indDaModif = mViewPager.getCurrentItem();
//                startActivity(new Intent(getActivity(), CreaListaActivity.class).putExtras(bundle));
//                lastPosition = mViewPager.getCurrentItem();
                startActivityForResult(new Intent(getActivity(), CreaListaActivity.class).putExtras(bundle), TAG_MODIFICA_LISTA);
                getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold_on);
            }
        });

        fabDelete = rootView.findViewById(R.id.fab_delete_lista);
        fabDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFab().hide();
                hideOuterFrame();
                prevOrientation = getActivity().getRequestedOrientation();
                Utility.blockOrientation(getActivity());

                listaDaCanc = mViewPager.getCurrentItem() - 2;
                idDaCanc = idListe[listaDaCanc];
//                lastPosition = mViewPager.getCurrentItem();
                SQLiteDatabase db = listaCanti.getReadableDatabase();

                String query = "SELECT titolo_lista, lista"
                        + "  FROM LISTE_PERS"
//                        + "  WHERE _id = " + idListe[listaDaCanc];
                        + "  WHERE _id = " + idDaCanc;
                Cursor cursor = db.rawQuery(query, null);

                cursor.moveToFirst();
                titoloDaCanc = cursor.getString(0);
                celebrazioneDaCanc = (ListaPersonalizzata) ListaPersonalizzata.deserializeObject(cursor.getBlob(1));
                cursor.close();
                db.close();

                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .title(R.string.action_remove_list)
                        .content(R.string.delete_list_dialog)
                        .positiveText(R.string.confirm)
                        .negativeText(R.string.dismiss)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                SQLiteDatabase db = listaCanti.getReadableDatabase();
                                db.delete("LISTE_PERS", "_id = " + idDaCanc, null);
                                db.close();

                                updateLista();
                                mSectionsPagerAdapter.notifyDataSetChanged();
//                                tabs.setupWithViewPager(mViewPager);
//                                mLUtils.applyFontedTab(mViewPager, tabs);
                                tabs.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        tabs.setupWithViewPager(mViewPager);
                                        mLUtils.applyFontedTab(mViewPager, tabs);
                                    }
                                });
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
//                                                tabs.setupWithViewPager(mViewPager);
//                                                mLUtils.applyFontedTab(mViewPager, tabs);
                                                tabs.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        tabs.setupWithViewPager(mViewPager);
                                                        mLUtils.applyFontedTab(mViewPager, tabs);
                                                    }
                                                });
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

////                db.delete("LISTE_PERS", "_id = " + idListe[listaDaCanc], null);
//                db.delete("LISTE_PERS", "_id = " + idDaCanc, null);
//                db.close();
//
//                updateLista();
//                mSectionsPagerAdapter.notifyDataSetChanged();
////                tabs.setupWithViewPager(mViewPager);
////                mLUtils.applyFontedTab(mViewPager, tabs);
//                tabs.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        tabs.setupWithViewPager(mViewPager);
//                        mLUtils.applyFontedTab(mViewPager, tabs);
//                    }
//                });
////                final Runnable mMyRunnable = new Runnable() {
////                    @Override
////                    public void run() {
////                        tabs.setupWithViewPager(mViewPager);
////                        mLUtils.applyFontedTab(mViewPager, tabs);
////                    }
////                };
//                Handler myHandler = new Handler();
////                myHandler.postDelayed(mMyRunnable, 200);
//                final Runnable mMyRunnable2 = new Runnable() {
//                    @Override
//                    public void run() {
//                        tabs.getTabAt(0).select();
//                    }
//                };
//                myHandler.postDelayed(mMyRunnable2, 200);
//                Snackbar.make(getActivity().findViewById(R.id.main_content), getString(R.string.list_removed) + titoloDaCanc + "'!", Snackbar.LENGTH_LONG)
//                        .setAction(R.string.cancel, new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
////					    	Log.i("INDICE DA CANC", listaDaCanc+" ");
//                                SQLiteDatabase db = listaCanti.getReadableDatabase();
//                                ContentValues values = new ContentValues();
//                                values.put("_id", idDaCanc);
//                                values.put("titolo_lista", titoloDaCanc);
//                                values.put("lista", ListaPersonalizzata.serializeObject(celebrazioneDaCanc));
//                                db.insert("LISTE_PERS", "", values);
//                                db.close();
//
//                                updateLista();
//                                mSectionsPagerAdapter.notifyDataSetChanged();
////                                tabs.setupWithViewPager(mViewPager);
////                                mLUtils.applyFontedTab(mViewPager, tabs);
//                                tabs.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        tabs.setupWithViewPager(mViewPager);
//                                        mLUtils.applyFontedTab(mViewPager, tabs);
//                                    }
//                                });
////                                final Runnable mMyRunnable = new Runnable() {
////                                    @Override
////                                    public void run() {
//////                                        tabs.getTabAt(0).select();
////                                        tabs.setupWithViewPager(mViewPager);
////                                        mLUtils.applyFontedTab(mViewPager, tabs);
////                                    }
////                                };
//                                Handler myHandler = new Handler();
////                                myHandler.postDelayed(mMyRunnable, 200);
//                                final Runnable mMyRunnable2 = new Runnable() {
//                                    @Override
//                                    public void run() {
////                                        tabs.getTabAt(0).select();
//                                        mViewPager.setCurrentItem(listaDaCanc + 2, false);
//                                    }
//                                };
//                                myHandler.postDelayed(mMyRunnable2, 200);
//                            }
//                        })
//                        .setActionTextColor(getThemeUtils().accentColor())
//                        .show();

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

        getFab().setButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOuterFrame();
            }
        });

        return rootView;
    }

//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        setHasOptionsMenu(true);
//    }

//    @Override
//    public void onResume() {
//        super.onResume();
////        updateLista();
////        mSectionsPagerAdapter.notifyDataSetChanged();
////        mSlidingTabLayout.setViewPager(mViewPager);
////        if (getFab1().isExpanded()) {
////            showOuterFrame();
////        }
//    }

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
        outState.putInt(PAGE_EDITED, indDaModif);
    }

//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        getActivity().getMenuInflater().inflate(R.menu.custom_list, menu);
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.i(getClass().getName(), "requestCode: " + requestCode);
        if ((requestCode == TAG_CREA_LISTA || requestCode == TAG_MODIFICA_LISTA) && resultCode == Activity.RESULT_OK) {
            updateLista();
            mSectionsPagerAdapter.notifyDataSetChanged();
            tabs.setupWithViewPager(mViewPager);
            mLUtils.applyFontedTab(mViewPager, tabs);
//            tabs.post(new Runnable() {
//                @Override
//                public void run() {
//                    tabs.setupWithViewPager(mViewPager);
//                    mLUtils.applyFontedTab(mViewPager, tabs);
//                }
//            });
//            final Runnable mMyRunnable = new Runnable() {
//                @Override
//                public void run() {
//                    tabs.setupWithViewPager(mViewPager);
//                    mLUtils.applyFontedTab(mViewPager, tabs);
//                }
//            };
            Handler myHandler = new Handler();
//            myHandler.postDelayed(mMyRunnable, 200);
            final Runnable mMyRunnable2 = new Runnable() {
                @Override
                public void run() {
                    mViewPager.setCurrentItem(indDaModif, false);
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

    public FabToolbar getFab() {
        if (mFab == null) {
            mFab = (FabToolbar) rootView.findViewById(R.id.fab_pager);
            mFab.setColor(getThemeUtils().accentColor());
        }
        return mFab;
    }

    private void showOuterFrame() {
        View outerFrame = rootView.findViewById(R.id.outerFrame);
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

//    public boolean onBackPressed() {
//        boolean result = false;
//
//        CantiParolaFragment fragment = (CantiParolaFragment) mSectionsPagerAdapter.getRegisteredFragment(0);
//        if (fragment != null && fragment.mMode != null) {
//            fragment.mMode.finish();
////            Log.i(getClass().getName(), "1");
//            result = true;
//        }
//
//        CantiEucarestiaFragment fragment2 = (CantiEucarestiaFragment) mSectionsPagerAdapter.getRegisteredFragment(1);
//        if (fragment2 != null && fragment2.mMode != null) {
//            fragment2.mMode.finish();
////            Log.i(getClass().getName(), "2");
//            result = true;
//        }
//
//        ListaPersonalizzataFragment fragmentPers;
////        Log.i(getClass().getName(), "mViewPager.getChildCount(): " + mViewPager.getChildCount());
////        Log.i(getClass().getName(), "mSectionsPagerAdapter.getCount(): " + mSectionsPagerAdapter.getCount());
//        for (int i = 2; i < mSectionsPagerAdapter.getCount(); i++) {
////            Log.i(getClass().getName(), "3, i: " + i);
//            fragmentPers = (ListaPersonalizzataFragment) mSectionsPagerAdapter.getRegisteredFragment(i);
//            if (fragmentPers != null && fragmentPers.mMode != null) {
//                fragmentPers.mMode.finish();
////                Log.i(getClass().getName(), "3 OK");
//                result = true;
//            }
//        }
//
//        return result;
//    }

}
