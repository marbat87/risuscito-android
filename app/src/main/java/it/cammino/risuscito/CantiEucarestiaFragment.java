package it.cammino.risuscito;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.melnykov.fab.FloatingActionButton;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.cammino.risuscito.adapters.CantoCardRecyclerAdapter;
import it.cammino.risuscito.utils.ThemeUtils;

public class CantiEucarestiaFragment extends Fragment {

    private int posizioneDaCanc;
    private String titoloDaCanc;
    private View rootView;
    private ShareActionProvider mShareActionProvider;
    private DatabaseCanti listaCanti;
    private SQLiteDatabase db;
//    private int prevOrientation;

    private LUtils mLUtils;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(
                R.layout.activity_canti_eucarestia, container, false);

        //crea un istanza dell'oggetto DatabaseCanti
        listaCanti = new DatabaseCanti(getActivity());

//        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_eucarestia);
//        fab.setColorNormal(getThemeUtils().accentColor());
//        fab.setColorPressed(getThemeUtils().accentColorDark());
//        fab.setColorRipple(getThemeUtils().accentColorDark());
//        fab.attachToScrollView((ObservableScrollView) rootView.findViewById(R.id.eucarestiaScrollView));
//        fab.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                prevOrientation = getActivity().getRequestedOrientation();
//                Utility.blockOrientation(getActivity());
////                AlertDialogPro.Builder builder = new AlertDialogPro.Builder(getActivity());
////                AlertDialogPro dialog = builder.setTitle(R.string.dialog_reset_list_title)
////                        .setMessage(R.string.reset_list_question)
////                        .setPositiveButton(R.string.confirm, new ButtonClickedListener(Utility.EUCAR_RESET_OK))
////                        .setNegativeButton(R.string.dismiss, new ButtonClickedListener(Utility.DISMISS))
////                        .show();
////                dialog.setOnKeyListener(new Dialog.OnKeyListener() {
////                    @Override
////                    public boolean onKey(DialogInterface arg0, int keyCode,
////                                         KeyEvent event) {
////                        if (keyCode == KeyEvent.KEYCODE_BACK
////                                && event.getAction() == KeyEvent.ACTION_UP) {
////                            arg0.dismiss();
////                            getActivity().setRequestedOrientation(prevOrientation);
////                            return true;
////                        }
////                        return false;
////                    }
////                });
////                dialog.setCancelable(false);
//                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
//                        .title(R.string.dialog_reset_list_title)
//                        .content(R.string.reset_list_question)
//                        .positiveText(R.string.confirm)
//                        .negativeText(R.string.dismiss)
//                        .callback(new MaterialDialog.ButtonCallback() {
//                            @Override
//                            public void onPositive(MaterialDialog dialog) {
//                                db = listaCanti.getReadableDatabase();
//                                String sql = "DELETE FROM CUST_LISTS" +
//                                        " WHERE _id =  2 ";
//                                db.execSQL(sql);
//                                db.close();
//                                updateLista();
//                                mShareActionProvider.setShareIntent(getDefaultIntent());
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

        rootView.findViewById(R.id.button_pulisci).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(getClass().toString(), "cantieucarestia");
                db = listaCanti.getReadableDatabase();
                String sql = "DELETE FROM CUST_LISTS" +
                        " WHERE _id =  2 ";
                db.execSQL(sql);
                db.close();
                updateLista();
                mShareActionProvider.setShareIntent(getDefaultIntent());
            }
        });

        ((ObservableScrollView) rootView.findViewById(R.id.eucarestiaScrollView)).setScrollViewCallbacks(new ObservableScrollViewCallbacks() {
            @Override
            public void onScrollChanged(int i, boolean b, boolean b1) {}

            @Override
            public void onDownMotionEvent() {}

            @Override
            public void onUpOrCancelMotionEvent(ScrollState scrollState) {
                FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab_pager);
//                Log.i(getClass().toString(), "scrollState: " + scrollState);
                if (scrollState == ScrollState.UP) {
                    if (!fab.isVisible()) {
                        fab.show();
                    }
                } else if (scrollState == ScrollState.DOWN) {
                    if (fab.isVisible()) {
                        fab.hide();
                    }
                }
            }
        });

        mLUtils = LUtils.getInstance(getActivity());

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser)
            ((FloatingActionButton) getActivity().findViewById(R.id.fab_pager)).show();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
//        Log.i("CANTI EUCARESTIA", "ON RESUME");
        super.onResume();
        updateLista();
    }

    @Override
    public void onDestroy() {
        if (listaCanti != null)
            listaCanti.close();
        super.onDestroy();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem shareItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        ViewPager tempPager = (ViewPager) getActivity().findViewById(R.id.view_pager);
        if (mShareActionProvider != null && tempPager.getCurrentItem() == 1)
            mShareActionProvider.setShareIntent(getDefaultIntent());
    }

    private Intent getDefaultIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, getTitlesList());
        intent.setType("text/plain");
        return intent;
    }

    private void startSubActivity(Bundle bundle) {
        Intent intent = new Intent(getActivity(), GeneralInsertSearch.class);
        intent.putExtras(bundle);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.hold_on);
    }

    private void openPagina(View v, int id) {
        // recupera il titolo della voce cliccata
        String cantoCliccato = ((TextView) v.findViewById(id)).getText().toString();
        cantoCliccato = Utility.duplicaApostrofi(cantoCliccato);

        // crea un manipolatore per il DB in modalit� READ
        db = listaCanti.getReadableDatabase();

        // esegue la query per il recupero del nome del file della pagina da visualizzare
        String query = "SELECT source, _id" +
                "  FROM ELENCO" +
                "  WHERE titolo =  '" + cantoCliccato + "'";
        Cursor cursor = db.rawQuery(query, null);

        // recupera il nome del file
        cursor.moveToFirst();
        String pagina = cursor.getString(0);
        int idCanto = cursor.getInt(1);

        // chiude il cursore
        cursor.close();
        db.close();

        // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da visualizzare
        Bundle bundle = new Bundle();
        bundle.putString("pagina", pagina);
        bundle.putInt("idCanto", idCanto);

        Intent intent = new Intent(getActivity(), PaginaRenderActivity.class);
        intent.putExtras(bundle);
        mLUtils.startActivityWithTransition(intent, v, Utility.TRANS_PAGINA_RENDER);
    }

    private void updateLista() {

        String[] titoloCanto = getTitoliFromPosition(1);

        if (titoloCanto.length == 0) {
            rootView.findViewById(R.id.addCantoIniziale1).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.cantoIniziale1Container).setVisibility(View.GONE);
            rootView.findViewById(R.id.addCantoIniziale1).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("fromAdd", 1);
                    bundle.putInt("idLista", 2);
                    bundle.putInt("position", 1);
                    startSubActivity(bundle);
                }
            });
        }
        else {
            rootView.findViewById(R.id.addCantoIniziale1).setVisibility(View.GONE);
            View view = rootView.findViewById(R.id.cantoIniziale1Container);
            view.setVisibility(View.VISIBLE);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    openPagina(v, R.id.cantoIniziale1Text);
                }
            });
            view.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    posizioneDaCanc = 1;
                    titoloDaCanc = Utility.duplicaApostrofi(((TextView) rootView.findViewById(R.id.cantoIniziale1Text)).getText().toString());
                    snackBarRimuoviCanto();
                    return true;
                }
            });

            TextView temp = (TextView) view.findViewById(R.id.cantoIniziale1Text);
            temp.setText(titoloCanto[0].substring(10));

            int tempPagina = Integer.valueOf(titoloCanto[0].substring(0,3));
            String pagina = String.valueOf(tempPagina);
            TextView textPage = (TextView) view.findViewById(R.id.cantoIniziale1Page);
            textPage.setText(pagina);

            String colore = titoloCanto[0].substring(3, 10);
            if (colore.equalsIgnoreCase(Utility.GIALLO))
                textPage.setBackgroundResource(R.drawable.bkg_round_yellow);
            if (colore.equalsIgnoreCase(Utility.GRIGIO))
                textPage.setBackgroundResource(R.drawable.bkg_round_grey);
            if (colore.equalsIgnoreCase(Utility.VERDE))
                textPage.setBackgroundResource(R.drawable.bkg_round_green);
            if (colore.equalsIgnoreCase(Utility.AZZURRO))
                textPage.setBackgroundResource(R.drawable.bkg_round_blue);
            if (colore.equalsIgnoreCase(Utility.BIANCO))
                textPage.setBackgroundResource(R.drawable.bkg_round_white);
        }

        SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean showSeconda = pref.getBoolean(Utility.SHOW_SECONDA, false);

        if (showSeconda) {

            rootView.findViewById(R.id.groupCantoSeconda).setVisibility(View.VISIBLE);

            titoloCanto = getTitoliFromPosition(6);

            if (titoloCanto.length == 0) {
                rootView.findViewById(R.id.addCantoSeconda).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.cantoSecondaContainer).setVisibility(View.GONE);
                rootView.findViewById(R.id.addCantoSeconda).setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("fromAdd", 1);
                        bundle.putInt("idLista", 2);
                        bundle.putInt("position", 6);
                        startSubActivity(bundle);
                    }
                });
            }
            else {
                rootView.findViewById(R.id.addCantoSeconda).setVisibility(View.GONE);
                View view = rootView.findViewById(R.id.cantoSecondaContainer);
                view.setVisibility(View.VISIBLE);
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openPagina(v, R.id.cantoSecondaText);
                    }
                });
                view.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        posizioneDaCanc = 6;
                        titoloDaCanc = Utility.duplicaApostrofi(((TextView) rootView.findViewById(R.id.cantoSecondaText)).getText().toString());
                        snackBarRimuoviCanto();
                        return true;
                    }
                });

                TextView temp = (TextView) view.findViewById(R.id.cantoSecondaText);
                temp.setText(titoloCanto[0].substring(10));

                int tempPagina = Integer.valueOf(titoloCanto[0].substring(0,3));
                String pagina = String.valueOf(tempPagina);
                TextView textPage = (TextView) view.findViewById(R.id.cantoSecondaPage);
                textPage.setText(pagina);

                String colore = titoloCanto[0].substring(3, 10);
                if (colore.equalsIgnoreCase(Utility.GIALLO))
                    textPage.setBackgroundResource(R.drawable.bkg_round_yellow);
                if (colore.equalsIgnoreCase(Utility.GRIGIO))
                    textPage.setBackgroundResource(R.drawable.bkg_round_grey);
                if (colore.equalsIgnoreCase(Utility.VERDE))
                    textPage.setBackgroundResource(R.drawable.bkg_round_green);
                if (colore.equalsIgnoreCase(Utility.AZZURRO))
                    textPage.setBackgroundResource(R.drawable.bkg_round_blue);
                if (colore.equalsIgnoreCase(Utility.BIANCO))
                    textPage.setBackgroundResource(R.drawable.bkg_round_white);
            }
        }
        else
            rootView.findViewById(R.id.groupCantoSeconda).setVisibility(View.GONE);

        titoloCanto = getTitoliFromPosition(2);

        if (titoloCanto.length == 0) {
            rootView.findViewById(R.id.addCantoPace).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.cantoPaceContainer).setVisibility(View.GONE);
            rootView.findViewById(R.id.addCantoPace).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("fromAdd", 1);
                    bundle.putInt("idLista", 2);
                    bundle.putInt("position", 2);
                    startSubActivity(bundle);
                }
            });
        }
        else {
            rootView.findViewById(R.id.addCantoPace).setVisibility(View.GONE);
            View view = rootView.findViewById(R.id.cantoPaceContainer);
            view.setVisibility(View.VISIBLE);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    openPagina(v, R.id.cantoPaceText);
                }
            });
            view.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    posizioneDaCanc = 2;
                    titoloDaCanc = Utility.duplicaApostrofi(((TextView) rootView.findViewById(R.id.cantoPaceText)).getText().toString());
                    snackBarRimuoviCanto();
                    return true;
                }
            });

            TextView temp = (TextView) view.findViewById(R.id.cantoPaceText);
            temp.setText(titoloCanto[0].substring(10));

            int tempPagina = Integer.valueOf(titoloCanto[0].substring(0,3));
            String pagina = String.valueOf(tempPagina);
            TextView textPage = (TextView) view.findViewById(R.id.cantoPagePage);
            textPage.setText(pagina);

            String colore = titoloCanto[0].substring(3, 10);
            if (colore.equalsIgnoreCase(Utility.GIALLO))
                textPage.setBackgroundResource(R.drawable.bkg_round_yellow);
            if (colore.equalsIgnoreCase(Utility.GRIGIO))
                textPage.setBackgroundResource(R.drawable.bkg_round_grey);
            if (colore.equalsIgnoreCase(Utility.VERDE))
                textPage.setBackgroundResource(R.drawable.bkg_round_green);
            if (colore.equalsIgnoreCase(Utility.AZZURRO))
                textPage.setBackgroundResource(R.drawable.bkg_round_blue);
            if (colore.equalsIgnoreCase(Utility.BIANCO))
                textPage.setBackgroundResource(R.drawable.bkg_round_white);
        }

        boolean showSanto = pref.getBoolean(Utility.SHOW_SANTO, false);

        if (showSanto) {

            rootView.findViewById(R.id.groupSanto).setVisibility(View.VISIBLE);

            titoloCanto = getTitoliFromPosition(7);

            if (titoloCanto.length == 0) {
                rootView.findViewById(R.id.addSanto).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.santoContainer).setVisibility(View.GONE);
                rootView.findViewById(R.id.addSanto).setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("fromAdd", 1);
                        bundle.putInt("idLista", 2);
                        bundle.putInt("position", 7);
                        startSubActivity(bundle);
                    }
                });
            }
            else {
                rootView.findViewById(R.id.addSanto).setVisibility(View.GONE);
                View view = rootView.findViewById(R.id.santoContainer);
                view.setVisibility(View.VISIBLE);
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openPagina(v, R.id.santoText);
                    }
                });
                view.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        posizioneDaCanc = 7;
                        titoloDaCanc = Utility.duplicaApostrofi(((TextView) rootView.findViewById(R.id.santoText)).getText().toString());
                        snackBarRimuoviCanto();
                        return true;
                    }
                });

                TextView temp = (TextView) view.findViewById(R.id.santoText);
                temp.setText(titoloCanto[0].substring(10));

                int tempPagina = Integer.valueOf(titoloCanto[0].substring(0,3));
                String pagina = String.valueOf(tempPagina);
                TextView textPage = (TextView) view.findViewById(R.id.santoPage);
                textPage.setText(pagina);

                String colore = titoloCanto[0].substring(3, 10);
                if (colore.equalsIgnoreCase(Utility.GIALLO))
                    textPage.setBackgroundResource(R.drawable.bkg_round_yellow);
                if (colore.equalsIgnoreCase(Utility.GRIGIO))
                    textPage.setBackgroundResource(R.drawable.bkg_round_grey);
                if (colore.equalsIgnoreCase(Utility.VERDE))
                    textPage.setBackgroundResource(R.drawable.bkg_round_green);
                if (colore.equalsIgnoreCase(Utility.AZZURRO))
                    textPage.setBackgroundResource(R.drawable.bkg_round_blue);
                if (colore.equalsIgnoreCase(Utility.BIANCO))
                    textPage.setBackgroundResource(R.drawable.bkg_round_white);
            }
        }
        else
            rootView.findViewById(R.id.groupSanto).setVisibility(View.GONE);

        OnClickListener clickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                openPagina(v, R.id.text_title);
            }
        };

        OnLongClickListener longClickListener = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                posizioneDaCanc = 3;
                titoloDaCanc = Utility.duplicaApostrofi(((TextView) v.findViewById(R.id.text_title)).getText().toString());
                snackBarRimuoviCanto();
                return true;
            }
        };

        List<CantoItem> dataItems = getTitoliListFromPosition(3);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.cantiPaneList);

        // Creating new adapter object
        recyclerView.setAdapter(new CantoCardRecyclerAdapter(dataItems, clickListener, longClickListener));

        // Setting the layoutManager
        recyclerView.setLayoutManager(new MyLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        longClickListener = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                posizioneDaCanc = 4;
                titoloDaCanc = Utility.duplicaApostrofi(((TextView) v.findViewById(R.id.text_title)).getText().toString());
                snackBarRimuoviCanto();
                return true;
            }
        };

        dataItems = getTitoliListFromPosition(4);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.cantiVinoList);

        // Creating new adapter object
        recyclerView.setAdapter(new CantoCardRecyclerAdapter(dataItems, clickListener, longClickListener));

        // Setting the layoutManager
        recyclerView.setLayoutManager(new MyLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        titoloCanto = getTitoliFromPosition(5);

        if (titoloCanto.length == 0) {
            rootView.findViewById(R.id.addCantoFinale1).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.cantoFinale1Container).setVisibility(View.GONE);
            rootView.findViewById(R.id.addCantoFinale1).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("fromAdd", 1);
                    bundle.putInt("idLista", 2);
                    bundle.putInt("position", 5);
                    startSubActivity(bundle);
                }
            });
        }
        else {
            rootView.findViewById(R.id.addCantoFinale1).setVisibility(View.GONE);
            View view = rootView.findViewById(R.id.cantoFinale1Container);
            view.setVisibility(View.VISIBLE);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    openPagina(v, R.id.cantoFinale1Text);
                }
            });
            view.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    posizioneDaCanc = 5;
                    titoloDaCanc = Utility.duplicaApostrofi(((TextView) rootView.findViewById(R.id.cantoFinale1Text)).getText().toString());
                    snackBarRimuoviCanto();
                    return true;
                }
            });

            TextView temp = (TextView) view.findViewById(R.id.cantoFinale1Text);
            temp.setText(titoloCanto[0].substring(10));

            int tempPagina = Integer.valueOf(titoloCanto[0].substring(0,3));
            String pagina = String.valueOf(tempPagina);
            TextView textPage = (TextView) view.findViewById(R.id.cantoFinale1Page);
            textPage.setText(pagina);

            String colore = titoloCanto[0].substring(3, 10);
            if (colore.equalsIgnoreCase(Utility.GIALLO))
                textPage.setBackgroundResource(R.drawable.bkg_round_yellow);
            if (colore.equalsIgnoreCase(Utility.GRIGIO))
                textPage.setBackgroundResource(R.drawable.bkg_round_grey);
            if (colore.equalsIgnoreCase(Utility.VERDE))
                textPage.setBackgroundResource(R.drawable.bkg_round_green);
            if (colore.equalsIgnoreCase(Utility.AZZURRO))
                textPage.setBackgroundResource(R.drawable.bkg_round_blue);
            if (colore.equalsIgnoreCase(Utility.BIANCO))
                textPage.setBackgroundResource(R.drawable.bkg_round_white);
        }

        rootView.findViewById(R.id.addCantoPane).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("fromAdd", 1);
                bundle.putInt("idLista", 2);
                bundle.putInt("position", 3);
                startSubActivity(bundle);
            }
        });

        rootView.findViewById(R.id.addCantoVino).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("fromAdd", 1);
                bundle.putInt("idLista", 2);
                bundle.putInt("position", 4);
                startSubActivity(bundle);
            }
        });

    }

    private String getTitlesList() {

        Locale l = getActivity().getResources().getConfiguration().locale;
        String result = "";
        String[] temp;

        //titolo
        result +=  "-- " + getString(R.string.title_activity_canti_eucarestia).toUpperCase(l) + " --\n";

        //canto iniziale
        temp = getTitoloToSendFromPosition(1);

        result += getResources().getString(R.string.canto_iniziale).toUpperCase(l);
        result += "\n";

        if (temp[0] == null || temp[0].equalsIgnoreCase(""))
            result += ">> " + getString(R.string.to_be_chosen) + " <<";
        else
            result += temp[0];

        result += "\n";

        //deve essere messa anche la seconda lettura? legge le impostazioni
        SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean showSeconda = pref.getBoolean(Utility.SHOW_SECONDA, false);

        if (showSeconda) {
            //canto alla seconda lettura
            temp = getTitoloToSendFromPosition(6);

            result += getResources().getString(R.string.seconda_lettura).toUpperCase(l);
            result += "\n";

            if (temp[0] == null || temp[0].equalsIgnoreCase(""))
                result += ">> " + getString(R.string.to_be_chosen) + " <<";
            else
                result += temp[0];

            result += "\n";
        }
//		else
//			Log.i("SECONDA LETTURA", "IGNORATA");

        //canto alla pace
        temp = getTitoloToSendFromPosition(2);

        result += getResources().getString(R.string.canto_pace).toUpperCase(l);
        result += "\n";

        if (temp[0] == null || temp[0].equalsIgnoreCase(""))
            result += ">> " + getString(R.string.to_be_chosen) + " <<";
        else
            result += temp[0];

        result += "\n";

        //deve essere messo anche il Santo? legge le impostazioni
        boolean showSanto = pref.getBoolean(Utility.SHOW_SANTO, false);

        if (showSanto) {
            //canto alla seconda lettura
            temp = getTitoloToSendFromPosition(7);

            result += getResources().getString(R.string.santo).toUpperCase(l);
            result += "\n";

            if (temp[0] == null || temp[0].equalsIgnoreCase(""))
                result += ">> " + getString(R.string.to_be_chosen) + " <<";
            else
                result += temp[0];

            result += "\n";
        }
//		else
//			Log.i("SANTO", "IGNORATO");

        //canti al pane
        temp = getTitoloToSendFromPosition(3);

        result += getResources().getString(R.string.canto_pane).toUpperCase(l);
        result += "\n";

        if (temp[0] == null || temp[0].equalsIgnoreCase("")) {
            result += ">> " + getString(R.string.to_be_chosen) + " <<";
            result += "\n";
        }
        else {
            for (String tempTitle: temp) {
                if (tempTitle != null && !tempTitle.equalsIgnoreCase("")) {
                    result += tempTitle;
                    result += "\n";
                }
                else
                    break;
            }
        }

        //canti al vino
        temp = getTitoloToSendFromPosition(4);

        result += getResources().getString(R.string.canto_vino).toUpperCase(l);
        result += "\n";

        if (temp[0] == null || temp[0].equalsIgnoreCase("")) {
            result += ">> " + getString(R.string.to_be_chosen) + " <<";
            result += "\n";
        }
        else {
            for (String tempTitle: temp) {
                if (tempTitle != null && !tempTitle.equalsIgnoreCase("")) {
                    result += tempTitle;
                    result += "\n";
                }
                else
                    break;
            }
        }

        //canto finale
        temp = getTitoloToSendFromPosition(5);

        result += getResources().getString(R.string.canto_fine).toUpperCase(l);
        result += "\n";

        if (temp[0] == null || temp[0].equalsIgnoreCase(""))
            result += ">> " + getString(R.string.to_be_chosen) + " <<";
        else
            result += temp[0];

        return result;

    }

    private String[] getTitoliFromPosition(int position) {

        db = listaCanti.getReadableDatabase();

        String query = "SELECT B.titolo, color, pagina" +
                "  FROM CUST_LISTS A" +
                "  	   , ELENCO B" +
                "  WHERE A._id = 2" +
                "  AND   A.position = " + position +
                "  AND   A.id_canto = B._id" +
                "  ORDER BY A.timestamp ASC";
        Cursor cursor = db.rawQuery(query, null);

        int total = cursor.getCount();

        String[] result = new String[total];

        cursor.moveToFirst();
        for (int i = 0; i < total; i++) {
//            result[i] =  cursor.getString(1) + cursor.getString(0);
            result[i] =  Utility.intToString(cursor.getInt(2), 3) + cursor.getString(1) + cursor.getString(0);
            cursor.moveToNext();
        }

        cursor.close();
        db.close();

        return result;
    }

    private List<CantoItem> getTitoliListFromPosition(int position) {

        List<CantoItem> result = new ArrayList<CantoItem>();

        db = listaCanti.getReadableDatabase();

        String query = "SELECT B.titolo, color, pagina" +
                "  FROM CUST_LISTS A" +
                "  	   , ELENCO B" +
                "  WHERE A._id = 2" +
                "  AND   A.position = " + position +
                "  AND   A.id_canto = B._id" +
                "  ORDER BY A.timestamp ASC";
        Cursor cursor = db.rawQuery(query, null);

        int total = cursor.getCount();

        cursor.moveToFirst();
        for (int i = 0; i < total; i++) {
            result.add(new CantoItem(Utility.intToString(cursor.getInt(2), 3) + cursor.getString(1) + cursor.getString(0)));
            cursor.moveToNext();
        }

        cursor.close();
        db.close();

        return result;
    }

    //recupera il titolo del canto in posizione "position" nella lista 2
    private String[] getTitoloToSendFromPosition(int position) {

        db = listaCanti.getReadableDatabase();

        String query = "SELECT B.titolo, B.pagina" +
                "  FROM CUST_LISTS A" +
                "  	   , ELENCO B" +
                "  WHERE A._id = 2" +
                "  AND   A.position = " + position +
                "  AND   A.id_canto = B._id" +
                "  ORDER BY A.timestamp ASC";
        Cursor cursor = db.rawQuery(query, null);

        int total = cursor.getCount();
        int resultLen = 1;
        if (total > 1)
            resultLen = total;

        String[] result = new String[resultLen];

        cursor.moveToFirst();
        for (int i = 0; i < total; i++) {
            result[i] =  cursor.getString(0) + " - " + getString(R.string.page_contracted) + cursor.getInt(1);
            cursor.moveToNext();
        }

        cursor.close();
        db.close();

        return result;
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
//                    getActivity().setRequestedOrientation(prevOrientation);
//                    break;
//                case Utility.EUCAR_RESET_OK:
//                    db = listaCanti.getReadableDatabase();
//                    String sql = "DELETE FROM CUST_LISTS" +
//                            " WHERE _id =  2 ";
//                    db.execSQL(sql);
//                    db.close();
//                    updateLista();
//                    mShareActionProvider.setShareIntent(getDefaultIntent());
//                    getActivity().setRequestedOrientation(prevOrientation);
//                default:
//                    getActivity().setRequestedOrientation(prevOrientation);
//                    break;
//            }
//        }
//    }

    public void snackBarRimuoviCanto() {
        SnackbarManager.show(
                Snackbar.with(getActivity())
                        .text(getString(R.string.list_remove))
                        .actionLabel(getString(R.string.snackbar_remove))
                        .actionListener(new ActionClickListener() {
                            @Override
                            public void onActionClicked(Snackbar snackbar) {
                                db = listaCanti.getReadableDatabase();
                                String sql = "DELETE FROM CUST_LISTS" +
                                        "  WHERE _id =  2 " +
                                        "    AND position = " + posizioneDaCanc +
                                        "	 AND id_canto = (SELECT _id FROM ELENCO" +
                                        "					WHERE titolo = '" + titoloDaCanc + "')";
                                db.execSQL(sql);
                                db.close();
                                updateLista();
                                mShareActionProvider.setShareIntent(getDefaultIntent());
                            }
                        })
                        .actionColor(getThemeUtils().accentColor())
                , getActivity());
    }

    private ThemeUtils getThemeUtils() {
        return ((MainActivity)getActivity()).getThemeUtils();
    }

}