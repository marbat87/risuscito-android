package it.cammino.risuscito;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
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

import io.karim.MaterialTabs;
import it.cammino.risuscito.ui.CustomViewPager;
import it.cammino.risuscito.utils.ThemeUtils;

public class CustomLists extends Fragment  {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private String[] titoliListe;
    private int[] idListe;
    protected DatabaseCanti listaCanti;
    private int listaDaCanc;
    private int prevOrientation;
    private CustomViewPager mViewPager;
    //    TabPageIndicator mSlidingTabLayout = null;
    private FloatingActionsMenu mFab1;
    public FloatingActionButton fabAddLista, fabPulisci, fabEdit, fabDelete;
    private View rootView;
    private static final String PAGE_VIEWED = "pageViewed";
    public static final int TAG_CREA_LISTA = 111;
    public static final int TAG_MODIFICA_LISTA = 222;
    private MaterialDialog dialog;
//    private TintEditText titleInput;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.tabs_layout_with_fab, container, false);
//        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_activity_custom_lists);
        ((TextView)((MainActivity) getActivity()).findViewById(R.id.main_toolbarTitle)).setText(R.string.title_activity_custom_lists);
        ((MainActivity) getActivity()).getSupportActionBar().setElevation(0);

        //crea un istanza dell'oggetto DatabaseCanti
        listaCanti = new DatabaseCanti(getActivity());

        updateLista();

        // Create the adapter that will return a fragment for each of the three
        mViewPager = (CustomViewPager) rootView.findViewById(R.id.view_pager);
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

        MaterialTabs tabs = (MaterialTabs) rootView.findViewById(R.id.material_tabs);
        tabs.setBackgroundColor(getThemeUtils().primaryColor());
        tabs.setViewPager(mViewPager);

        getFab1().setColorNormal(getThemeUtils().accentColor());
        getFab1().setColorPressed(getThemeUtils().accentColorDark());
        getFab1().setIcon(R.drawable.ic_add_white_24dp);
//        getFab2().setMenuButtonColorNormal(getThemeUtils().accentColor());
//        getFab2().setMenuButtonColorPressed(getThemeUtils().accentColorDark());
//        getFab2().setMenuButtonColorRipple(getThemeUtils().accentColorDark());
//        getFab2().setIcon(R.drawable.ic_add_white_24dp);
//        fab.attachToScrollView((ObservableScrollView) rootView.findViewById(R.id.personalizzataScrollView));
//        getFab1().setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                prevOrientation = getActivity().getRequestedOrientation();
//                Utility.blockOrientation(getActivity());
//                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
//                        .title(R.string.dialog_reset_list_title)
//                        .content(R.string.reset_list_question)
//                        .positiveText(R.string.confirm)
//                        .negativeText(R.string.dismiss)
//                        .callback(new MaterialDialog.ButtonCallback() {
//                            @Override
//                            public void onPositive(MaterialDialog dialog) {
//                                mSectionsPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem())
//                                        .getView().findViewById(R.id.button_pulisci).performClick();
//                                getActivity().setRequestedOrientation(prevOrientation);
//                            }
//
//                            @Override
//                            public void onNegative(MaterialDialog dialog) {
//                                getActivity().setRequestedOrientation(prevOrientation);
//                            }
//                        })
//                        .show();
//                dialog.setOnKeyListener(new Dialog.OnKeyListener() {
//                    @Override
//                    public boolean onKey(DialogInterface arg0, int keyCode,
//                                         KeyEvent event) {
//                        if (keyCode == KeyEvent.KEYCODE_BACK
//                                && event.getAction() == KeyEvent.ACTION_UP) {
//                            arg0.dismiss();
//                            getActivity().setRequestedOrientation(prevOrientation);
//                            return true;
//                        }
//                        return false;
//                    }
//                });
//                dialog.setCancelable(false);
//            }
//        });

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

//        getFab2().setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
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
//                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
//                dialog.getInputEditText().addTextChangedListener(new TextWatcher() {
//                    @Override
//                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                    }
//
//                    @Override
//                    public void onTextChanged(CharSequence s, int start, int before, int count) {
//                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(s.toString().trim().length() > 0);
//                    }
//
//                    @Override
//                    public void afterTextChanged(Editable s) {
//                    }
//                });
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

//        FloatingActionButton fabAddLista2 = (FloatingActionButton) rootView.findViewById(R.id.fab_add_lista2);
////        fabAddLista2.setColorNormal(getThemeUtils().accentColor());
////        fabAddLista2.setColorPressed(getThemeUtils().accentColorDark());
//        fabAddLista2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getFab2().toggle(true);
//                prevOrientation = getActivity().getRequestedOrientation();
//                Utility.blockOrientation(getActivity());
//                dialog = new MaterialDialog.Builder(getActivity())
//                        .title(R.string.lista_add_desc)
//                        .positiveText(R.string.dialog_chiudi)
//                        .negativeText(R.string.cancel)
//                        .input("", "", new MaterialDialog.InputCallback() {
//                            @Override
//                            public void onInput(MaterialDialog dialog, CharSequence input) {
//                            }
//                        })
//                        .callback(new MaterialDialog.ButtonCallback() {
//                            @Override
//                            public void onPositive(MaterialDialog dialog) {
//                                //to hide soft keyboard
//                                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
//                                        .hideSoftInputFromWindow(dialog.getInputEditText().getWindowToken(), 0);
//                                getActivity().setRequestedOrientation(prevOrientation);
//                                Bundle bundle = new Bundle();
//                                bundle.putString("titolo", dialog.getInputEditText().getText().toString());
//                                bundle.putBoolean("modifica", false);
////                                startActivity(new Intent(getActivity(), CreaListaActivity.class).putExtras(bundle));
//                                startActivityForResult(new Intent(getActivity(), CreaListaActivity.class).putExtras(bundle), TAG_CREA_LISTA);
//                                getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold_on);
//                            }
//
//                            @Override
//                            public void onNegative(MaterialDialog dialog) {
//                                //to hide soft keyboard
//                                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
//                                        .hideSoftInputFromWindow(dialog.getInputEditText().getWindowToken(), 0);
//                                getActivity().setRequestedOrientation(prevOrientation);
//                            }
//                        })
//                        .show();
//                dialog.setOnKeyListener(new Dialog.OnKeyListener() {
//                    @Override
//                    public boolean onKey(DialogInterface arg0, int keyCode,
//                                         KeyEvent event) {
//                        if (keyCode == KeyEvent.KEYCODE_BACK
//                                && event.getAction() == KeyEvent.ACTION_UP) {
//                            arg0.dismiss();
//                            getActivity().setRequestedOrientation(prevOrientation);
//                            return true;
//                        }
//                        return false;
//                    }
//                });
//                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
//                dialog.getInputEditText().addTextChangedListener(new TextWatcher() {
//                    @Override
//                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                    }
//
//                    @Override
//                    public void onTextChanged(CharSequence s, int start, int before, int count) {
//                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(s.toString().trim().length() > 0);
//                    }
//
//                    @Override
//                    public void afterTextChanged(Editable s) {
//                    }
//                });
//                dialog.setCancelable(false);
//                //to show soft keyboard
//                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
//                        .toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
//            }
//        });
//
//        FloatingActionButton fabPulisci2 = (FloatingActionButton) rootView.findViewById(R.id.fab_pulisci2);
//        fabPulisci2 = new FloatingActionButton(getActivity());
//        fabPulisci2.setColorNormal(getThemeUtils().accentColor());
//        fabPulisci2.setColorPressed(getThemeUtils().accentColorDark());
//        fabPulisci2.setSize(FloatingActionButton.SIZE_MINI);
//        fabPulisci2.setTitle(getString(R.string.button_clean_list));
//        fabPulisci2.setIcon(R.drawable.ic_icon_broom);
//        fabPulisci2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getFab1().toggle();
//                prevOrientation = getActivity().getRequestedOrientation();
//                Utility.blockOrientation(getActivity());
//                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
//                        .title(R.string.dialog_reset_list_title)
//                        .content(R.string.reset_list_question)
//                        .positiveText(R.string.confirm)
//                        .negativeText(R.string.dismiss)
//                        .callback(new MaterialDialog.ButtonCallback() {
//                            @Override
//                            public void onPositive(MaterialDialog dialog) {
//                                mSectionsPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem())
//                                        .getView().findViewById(R.id.button_pulisci).performClick();
//                                getActivity().setRequestedOrientation(prevOrientation);
//                            }
//
//                            @Override
//                            public void onNegative(MaterialDialog dialog) {
//                                getActivity().setRequestedOrientation(prevOrientation);
//                            }
//                        })
//                        .show();
//                dialog.setOnKeyListener(new Dialog.OnKeyListener() {
//                    @Override
//                    public boolean onKey(DialogInterface arg0, int keyCode,
//                                         KeyEvent event) {
//                        if (keyCode == KeyEvent.KEYCODE_BACK
//                                && event.getAction() == KeyEvent.ACTION_UP) {
//                            arg0.dismiss();
//                            getActivity().setRequestedOrientation(prevOrientation);
//                            return true;
//                        }
//                        return false;
//                    }
//                });
//                dialog.setCancelable(false);
//            }
//        });

        fabEdit = (FloatingActionButton) rootView.findViewById(R.id.fab_edit_lista);
        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFab1().toggle();
                Bundle bundle = new Bundle();
                bundle.putInt("idDaModif", idListe[mViewPager.getCurrentItem() - 2]);
                bundle.putBoolean("modifica", true);
//                startActivity(new Intent(getActivity(), CreaListaActivity.class).putExtras(bundle));
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
                Snackbar.make(rootView, getString(R.string.snackbar_list_delete) + titoliListe[listaDaCanc] + "'?", Snackbar.LENGTH_LONG)
                        .setAction(R.string.snackbar_remove, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                SQLiteDatabase db = listaCanti.getReadableDatabase();
//					    	Log.i("INDICE DA CANC", listaDaCanc+" ");
                                String sql = "DELETE FROM LISTE_PERS"
                                        + " WHERE _id = " + idListe[listaDaCanc];
                                db.execSQL(sql);
                                db.close();

                                updateLista();
                                mSectionsPagerAdapter.notifyDataSetChanged();
                            }
                        })
                        .setActionTextColor(getThemeUtils().accentColor())
                        .show();
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
        outState.putInt(PAGE_VIEWED, mViewPager.getCurrentItem());
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
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_add_list:
//                prevOrientation = getActivity().getRequestedOrientation();
//                Utility.blockOrientation(getActivity());
////                AlertDialogPro.Builder builder = new AlertDialogPro.Builder(getActivity());
////                dialog = builder.setTitle(R.string.lista_add_desc)
////                        .setView(R.layout.dialog_customview)
////                        .setPositiveButton(R.string.dialog_chiudi, new ButtonClickedListener(Utility.ADD_LIST_OK))
////                        .setNegativeButton(R.string.cancel, new ButtonClickedListener(Utility.DISMISS))
////                        .show();
//                dialog = new MaterialDialog.Builder(getActivity())
//                        .title(R.string.lista_add_desc)
//                        .positiveText(R.string.dialog_chiudi)
//                        .negativeText(R.string.cancel)
//                        .input("", "", new MaterialDialog.InputCallback() {
//                            @Override
//                            public void onInput(MaterialDialog dialog, CharSequence input) {}
//                        })
//                        .callback(new MaterialDialog.ButtonCallback() {
//                            @Override
//                            public void onPositive(MaterialDialog dialog) {
//                                //to hide soft keyboard
//                                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
//                                        .hideSoftInputFromWindow(dialog.getInputEditText().getWindowToken(), 0);
//                                getActivity().setRequestedOrientation(prevOrientation);
//                                Bundle bundle = new Bundle();
//                                bundle.putString("titolo", dialog.getInputEditText().getText().toString());
//                                bundle.putBoolean("modifica", false);
//                                startActivity(new Intent(getActivity(), CreaListaActivity.class).putExtras(bundle));
//                                getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold_on);
//                            }
//
//                            @Override
//                            public void onNegative(MaterialDialog dialog) {
//                                //to hide soft keyboard
//                                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
//                                        .hideSoftInputFromWindow(dialog.getInputEditText().getWindowToken(), 0);
//                                getActivity().setRequestedOrientation(prevOrientation);
//                            }
//                        })
//                        .show();
//                dialog.setOnKeyListener(new Dialog.OnKeyListener() {
//                    @Override
//                    public boolean onKey(DialogInterface arg0, int keyCode,
//                                         KeyEvent event) {
//                        if (keyCode == KeyEvent.KEYCODE_BACK
//                                && event.getAction() == KeyEvent.ACTION_UP) {
//                            arg0.dismiss();
//                            getActivity().setRequestedOrientation(prevOrientation);
//                            return true;
//                        }
//                        return false;
//                    }
//                });
//                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
//                dialog.getInputEditText().addTextChangedListener(new TextWatcher() {
//                    @Override
//                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//                    @Override
//                    public void onTextChanged(CharSequence s, int start, int before, int count) {
//                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(s.toString().trim().length() > 0);
//                    }
//
//                    @Override
//                    public void afterTextChanged(Editable s) {}
//                });
////                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
////                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(
////                        getResources().getColor(R.color.btn_disabled_text));
////                titleInput = (TintEditText)dialog.findViewById(R.id.list_title);
////                titleInput.addTextChangedListener(new TextWatcher() {
////                    @Override
////                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
////
////                    @Override
////                    public void onTextChanged(CharSequence s, int start, int before, int count) {
////                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(s.toString().trim().length() > 0);
////                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(
////                                s.toString().trim().length() > 0 ? getThemeUtils().accentColor():
////                                        getResources().getColor(R.color.btn_disabled_text));
////                    }
////
////                    @Override
////                    public void afterTextChanged(Editable s) {}
////                });
//                dialog.setCancelable(false);
//                //to show soft keyboard
//                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
//                        .toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
//                return true;
//            case R.id.action_edit_list:
//                Bundle bundle = new Bundle();
//                bundle.putInt("idDaModif", idListe[mViewPager.getCurrentItem() - 2]);
//                bundle.putBoolean("modifica", true);
//                startActivity(new Intent(getActivity(), CreaListaActivity.class).putExtras(bundle));
//                getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold_on);
//                return true;
//            case R.id.action_remove_list:
//                listaDaCanc = mViewPager.getCurrentItem() - 2;
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
//                                        mSlidingTabLayout.setViewPager(mViewPager);
//                                    }
//                                })
//                                .actionColor(getThemeUtils().accentColor())
//                        , getActivity());
//                return true;
//        }
//        return false;
//    }

    public FloatingActionsMenu getFab1() {
        if (mFab1 == null)
            mFab1 = (FloatingActionsMenu) rootView.findViewById(R.id.fab_pager);
        return mFab1;
    }

//    public FloatingActionsMenu getFab2() {
//        if (mFab2 == null)
//            mFab2 = (FloatingActionsMenu) rootView.findViewById(R.id.fab_pager2);
//        return mFab2;
//    }

    private void showOuterFrame() {
        View outerFrame = rootView.findViewById(R.id.outerFrame);
        outerFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFab1().collapse();
//                if (LUtils.hasHoneycomb()) {
//                    if (getFab1().isVisible())
//                        getFab1().collapse();
//                    if (getFab2().isVisible())
//                        getFab2().collapse();
//                }
//                else {
//                    if (getFab1().getVisibility() == View.VISIBLE)
//                        getFab1().collapse();
//                }
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

//    private class ButtonClickedListener implements DialogInterface.OnClickListener {
//        private int clickedCode;
//
//        public ButtonClickedListener(int code) {
//            clickedCode = code;
//        }
//
//        @Override
//        public void onClick(DialogInterface dialog, int which) {
//            switch (clickedCode) {
//                case Utility.DISMISS:
//                    //to hide soft keyboard
//                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
//                            .hideSoftInputFromWindow(titleInput.getWindowToken(), 0);
//                    getActivity().setRequestedOrientation(prevOrientation);
//                    break;
//                case Utility.ADD_LIST_OK:
//                    //to hide soft keyboard
//                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
//                            .hideSoftInputFromWindow(titleInput.getWindowToken(), 0);
//                    getActivity().setRequestedOrientation(prevOrientation);
//                    Bundle bundle = new Bundle();
//                    bundle.putString("titolo", titleInput.getText().toString());
//                    bundle.putBoolean("modifica", false);
//                    startActivity(new Intent(getActivity(), CreaListaActivity.class).putExtras(bundle));
//                    getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold_on);
//                    break;
//                default:
//                    getActivity().setRequestedOrientation(prevOrientation);
//                    break;
//            }
//        }
//    }

    private ThemeUtils getThemeUtils() {
        return ((MainActivity)getActivity()).getThemeUtils();
    }
}
