package it.cammino.risuscito;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import java.util.ArrayList;
import java.util.List;

import it.cammino.risuscito.adapters.CantoExpandableAdapter;
import it.cammino.risuscito.objects.CantoRecycled;
import it.cammino.risuscito.objects.ExpandableGroup;

public class IndiceLiturgicoFragment extends Fragment implements View.OnCreateContextMenuListener {

    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "RecyclerViewExpandableItemManagerLit";

    private DatabaseCanti listaCanti;
    private String titoloDaAgg;
    private int idDaAgg;
    private int idListaDaAgg;
    private int posizioneDaAgg;
    private ListaPersonalizzata[] listePers;
    private int[] idListe;
    private int idListaClick;
    private int idPosizioneClick;
    private int prevOrientation;
    private View rootView;

    private final int ID_FITTIZIO = 99999999;
    private final int ID_BASE = 100;

    private LUtils mLUtils;

    private long mLastClickTime = 0;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(
                R.layout.layout_recycler, container, false);

        //crea un istanza dell'oggetto DatabaseCanti
        listaCanti = new DatabaseCanti(getActivity());

        // crea un manipolatore per il Database in modalità READ
        SQLiteDatabase db = listaCanti.getReadableDatabase();

        // lancia la ricerca di tutti i momenti litrurgici in DB e li dispone in ordine alfabetico
        String query = "SELECT _id, nome" +
                "		FROM INDICE_LIT_NAMES" +
                "		ORDER BY nome ASC";
        Cursor arguments = db.rawQuery(query, null);

        //recupera il numero dei momenti liturgici
        int total = arguments.getCount();
        arguments.moveToFirst();

        List<Pair<ExpandableGroup, List<CantoRecycled>>> dataItems = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            int argId =  arguments.getInt(0);
            query = "SELECT B._id, B.titolo, B.color, B.pagina, B.source" +
                    "		FROM INDICE_LIT A, ELENCO B " +
                    "       WHERE A._id = " + argId +
                    "       AND A.id_canto = B._id " +
                    "		ORDER BY TITOLO ASC";
            Cursor argCanti = db.rawQuery(query, null);

            //recupera il numero di canti per il momento liturgico
            int totCanti = argCanti.getCount();
            argCanti.moveToFirst();

            List<CantoRecycled> children =  new ArrayList<>();

            for (int j = 0; j < totCanti; j++) {
                children.add(new CantoRecycled(argCanti.getString(1)
                        , argCanti.getInt(3)
                        , argCanti.getString(2)
                        , argCanti.getInt(0)
                        , argCanti.getString(4)));
                argCanti.moveToNext();
            }
            argCanti.close();

            dataItems.add(new Pair(
                    new ExpandableGroup(arguments.getString(1), arguments.getInt(0))
                    , children));

            arguments.moveToNext();

        }

        arguments.close();

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
                    return;
                mLastClickTime = SystemClock.elapsedRealtime();
                // recupera il titolo della voce cliccata
                String idCanto = String.valueOf(((TextView) v.findViewById(R.id.text_id_canto))
                        .getText());
                String source = String.valueOf(((TextView) v.findViewById(R.id.text_source_canto))
                        .getText());
                // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da visualizzare
                Bundle bundle = new Bundle();
                bundle.putString("pagina", source);
                bundle.putInt("idCanto", Integer.parseInt(idCanto));
                // lancia l'activity che visualizza il canto passando il parametro creato
                startSubActivity(bundle, v);
            }
        };

        //noinspection ConstantConditions
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());

        final Parcelable eimSavedState = (savedInstanceState != null) ? savedInstanceState.getParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER) : null;
        mRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(eimSavedState);

        //adapter
        CantoExpandableAdapter myItemAdapter = new CantoExpandableAdapter(getActivity(), dataItems, clickListener, IndiceLiturgicoFragment.this);
        mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(myItemAdapter);       // wrap for expanding

//        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();

        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Need to disable them when using animation indicator.
        final RecyclerView.ItemAnimator animator = mRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
//        animator.setSupportsChangeAnimations(false);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
//        mRecyclerView.setItemAnimator(animator);
        mRecyclerView.setHasFixedSize(false);

        mRecyclerViewExpandableItemManager.attachRecyclerView(mRecyclerView);

        query = "SELECT _id, lista" +
                "		FROM LISTE_PERS" +
                "		ORDER BY _id ASC";
        Cursor lista = db.rawQuery(query, null);

        listePers = new ListaPersonalizzata[lista.getCount()];
        idListe = new int[lista.getCount()];

        lista.moveToFirst();
        for (int i = 0; i < lista.getCount(); i++) {
            idListe[i] = lista.getInt(0);
            listePers[i] = (ListaPersonalizzata) ListaPersonalizzata.
                    deserializeObject(lista.getBlob(1));
            lista.moveToNext();
        }

        lista.close();
        db.close();

        mLUtils = LUtils.getInstance(getActivity());

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save current state to support screen rotation, etc...
        if (mRecyclerViewExpandableItemManager != null) {
            outState.putParcelable(
                    SAVED_STATE_EXPANDABLE_ITEM_MANAGER,
                    mRecyclerViewExpandableItemManager.getSavedState());
        }
    }

    @Override
    public void onDestroyView() {
        if (mRecyclerViewExpandableItemManager != null) {
            mRecyclerViewExpandableItemManager.release();
            mRecyclerViewExpandableItemManager = null;
        }

        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        mLayoutManager = null;

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (listaCanti != null)
            listaCanti.close();
        super.onDestroy();
    }

    private void startSubActivity(Bundle bundle, View view) {
        Intent intent = new Intent(getActivity(), PaginaRenderActivity.class);
        intent.putExtras(bundle);
        mLUtils.startActivityWithTransition(intent, view, Utility.TRANS_PAGINA_RENDER);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        titoloDaAgg = ((TextView) v.findViewById(R.id.text_title)).getText().toString();
        idDaAgg = Integer.valueOf(((TextView) v.findViewById(R.id.text_id_canto)).getText().toString());
        menu.setHeaderTitle("Aggiungi canto a:");

        for (int i = 0; i < idListe.length; i++) {
            SubMenu subMenu = menu.addSubMenu(ID_FITTIZIO, Menu.NONE, 10+i, listePers[i].getName());
            for (int k = 0; k < listePers[i].getNumPosizioni(); k++)
                subMenu.add(ID_BASE + i, k, k, listePers[i].getNomePosizione(k));
        }

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.add_to, menu);

        SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(getActivity());
        menu.findItem(R.id.add_to_p_pace).setVisible(pref.getBoolean(Utility.SHOW_PACE, false));
        menu.findItem(R.id.add_to_e_seconda).setVisible(pref.getBoolean(Utility.SHOW_SECONDA, false));
        menu.findItem(R.id.add_to_e_santo).setVisible(pref.getBoolean(Utility.SHOW_SANTO, false));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (getUserVisibleHint()) {
            switch (item.getItemId()) {
                case R.id.add_to_favorites:
                    addToFavorites();
                    return true;
                case R.id.add_to_p_iniziale:
                    addToListaNoDup(1, 1);
                    return true;
                case R.id.add_to_p_prima:
                    addToListaNoDup(1, 2);
                    return true;
                case R.id.add_to_p_seconda:
                    addToListaNoDup(1, 3);
                    return true;
                case R.id.add_to_p_terza:
                    addToListaNoDup(1, 4);
                    return true;
                case R.id.add_to_p_pace:
                    addToListaNoDup(1, 6);
                    return true;
                case R.id.add_to_p_fine:
                    addToListaNoDup(1, 5);
                    return true;
                case R.id.add_to_e_iniziale:
                    addToListaNoDup(2, 1);
                    return true;
                case R.id.add_to_e_seconda:
                    addToListaNoDup(2, 6);
                    return true;
                case R.id.add_to_e_pace:
                    addToListaNoDup(2, 2);
                    return true;
                case R.id.add_to_e_santo:
                    addToListaNoDup(2, 7);
                    return true;
                case R.id.add_to_e_pane:
                    addToListaDup(2, 3);
                    return true;
                case R.id.add_to_e_vino:
                    addToListaDup(2, 4);
                    return true;
                case R.id.add_to_e_fine:
                    addToListaNoDup(2, 5);
                    return true;
                default:
                    idListaClick = item.getGroupId();
                    idPosizioneClick = item.getItemId();
                    if (idListaClick != ID_FITTIZIO && idListaClick >= 100) {
                        idListaClick -= 100;

                        SQLiteDatabase db = listaCanti.getReadableDatabase();

                        if (listePers[idListaClick]
                                .getCantoPosizione(idPosizioneClick).equals("")) {
                            listePers[idListaClick].addCanto(String.valueOf(idDaAgg), idPosizioneClick);
                            ContentValues  values = new  ContentValues( );
                            values.put("lista" , ListaPersonalizzata.serializeObject(listePers[idListaClick]));
                            db.update("LISTE_PERS", values, "_id = " + idListe[idListaClick], null);
                            Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT)
                                    .show();
                        }
                        else {
                            if (listePers[idListaClick].getCantoPosizione(idPosizioneClick).equals(String.valueOf(idDaAgg))) {
                                Snackbar.make(rootView, R.string.present_yet, Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                            else {
                                prevOrientation = getActivity().getRequestedOrientation();
                                Utility.blockOrientation(getActivity());
                                //recupero titolo del canto presente
                                String query = "SELECT titolo" +
                                        "		FROM ELENCO" +
                                        "		WHERE _id = "
                                        + listePers[idListaClick].getCantoPosizione(idPosizioneClick);
                                Cursor cursor = db.rawQuery(query, null);
                                cursor.moveToFirst();
                                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                                        .title(R.string.dialog_replace_title)
                                        .content(getString(R.string.dialog_present_yet) + " "
                                                + listePers[idListaClick].getCantoPosizione(idPosizioneClick)
                                                .substring(10)
                                                + cursor.getString(0)
                                                + getString(R.string.dialog_wonna_replace))
                                        .positiveText(R.string.confirm)
                                        .negativeText(R.string.dismiss)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                                SQLiteDatabase db = listaCanti.getReadableDatabase();
                                                listePers[idListaClick].addCanto(String.valueOf(idDaAgg), idPosizioneClick);

                                                ContentValues values = new ContentValues();
                                                values.put("lista", ListaPersonalizzata.serializeObject(listePers[idListaClick]));
                                                db.update("LISTE_PERS", values, "_id = " + idListe[idListaClick], null);
                                                db.close();
                                                getActivity().setRequestedOrientation(prevOrientation);
                                                Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT)
                                                        .show();
                                            }
                                        })
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                                getActivity().setRequestedOrientation(prevOrientation);
                                            }
                                        })
//                                        .callback(new MaterialDialog.ButtonCallback() {
//                                            @Override
//                                            public void onPositive(MaterialDialog dialog) {
//                                                SQLiteDatabase db = listaCanti.getReadableDatabase();
//                                                listePers[idListaClick].addCanto(String.valueOf(idDaAgg), idPosizioneClick);
//
//                                                ContentValues values = new ContentValues();
//                                                values.put("lista", ListaPersonalizzata.serializeObject(listePers[idListaClick]));
//                                                db.update("LISTE_PERS", values, "_id = " + idListe[idListaClick], null);
//                                                db.close();
//                                                getActivity().setRequestedOrientation(prevOrientation);
//                                                Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT)
//                                                        .show();
//                                            }
//
//                                            @Override
//                                            public void onNegative(MaterialDialog dialog) {
//                                                getActivity().setRequestedOrientation(prevOrientation);
//                                            }
//                                        })
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
                                cursor.close();
                            }
                        }
                        db.close();
                        return true;
                    }
                    else
                        return super.onContextItemSelected(item);
            }
        }
        else
            return false;
    }

    //aggiunge il canto premuto ai preferiti
    public void addToFavorites() {
        SQLiteDatabase db = listaCanti.getReadableDatabase();
        String sql = "UPDATE ELENCO" +
                "  SET favourite = 1" +
                "  WHERE _id =  " + idDaAgg;
        db.execSQL(sql);
        db.close();
        Snackbar.make(rootView, R.string.favorite_added, Snackbar.LENGTH_SHORT)
                .show();

    }

    //aggiunge il canto premuto ad una lista e in una posizione che ammetta duplicati
    public void addToListaDup(int idLista, int listPosition) {
        SQLiteDatabase db = listaCanti.getReadableDatabase();

        String sql = "INSERT INTO CUST_LISTS ";
        sql+= "VALUES (" + idLista + ", "
                + listPosition + ", "
                + idDaAgg
                + ", CURRENT_TIMESTAMP)";

        try {
            db.execSQL(sql);
            Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT)
                    .show();
        } catch (SQLException e) {
            Snackbar.make(rootView
                    , R.string.present_yet, Snackbar.LENGTH_SHORT)
                    .show();
        }

        db.close();
    }

    //aggiunge il canto premuto ad una lista e in una posizione che NON ammetta duplicati
    public void addToListaNoDup(int idLista, int listPosition) {

        SQLiteDatabase db = listaCanti.getReadableDatabase();

        // cerca se la posizione nella lista è già occupata
        String query = "SELECT B.titolo" +
                "		FROM CUST_LISTS A" +
                "		   , ELENCO B" +
                "		WHERE A._id = " + idLista +
                "         AND A.position = " + listPosition +
                "         AND A.id_canto = B._id";
        Cursor lista = db.rawQuery(query, null);

        int total = lista.getCount();

        if (total > 0) {
            lista.moveToFirst();
            String titoloPresente = lista.getString(0);
            lista.close();
            db.close();

            if (titoloDaAgg.equalsIgnoreCase(titoloPresente))
                Snackbar.make(rootView, R.string.present_yet, Snackbar.LENGTH_SHORT)
                        .show();
            else {
                idListaDaAgg = idLista;
                posizioneDaAgg = listPosition;
                prevOrientation = getActivity().getRequestedOrientation();
                Utility.blockOrientation(getActivity());
                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .title(R.string.dialog_replace_title)
                        .content(getString(R.string.dialog_present_yet) + " " + titoloPresente
                                + getString(R.string.dialog_wonna_replace))
                        .positiveText(R.string.confirm)
                        .negativeText(R.string.dismiss)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                SQLiteDatabase db = listaCanti.getReadableDatabase();
                                String sql = "UPDATE CUST_LISTS "
                                        + "     SET id_canto = " + idDaAgg
                                        + "     WHERE _id = " + idListaDaAgg
                                        + "     AND position = " + posizioneDaAgg;
                                db.execSQL(sql);
                                db.close();
                                getActivity().setRequestedOrientation(prevOrientation);
                                Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                getActivity().setRequestedOrientation(prevOrientation);
                            }
                        })
//                        .callback(new MaterialDialog.ButtonCallback() {
//                            @Override
//                            public void onPositive(MaterialDialog dialog) {
//                                SQLiteDatabase db = listaCanti.getReadableDatabase();
////                                String cantoCliccatoNoApex = Utility.duplicaApostrofi(titoloDaAgg);
//                                String sql = "UPDATE CUST_LISTS "
////                                        + "SET id_canto = (SELECT _id  FROM ELENCO"
////                                        + " WHERE titolo = \'" + cantoCliccatoNoApex + "\')"
//                                        + "     SET id_canto = " + idDaAgg
//                                        + "     WHERE _id = " + idListaDaAgg
//                                        + "     AND position = " + posizioneDaAgg;
//                                db.execSQL(sql);
//                                db.close();
//                                getActivity().setRequestedOrientation(prevOrientation);
////                                Toast.makeText(getActivity()
////                                        , getString(R.string.list_added), Toast.LENGTH_SHORT).show();
//                                Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT)
//                                        .show();
//                            }
//
//                            @Override
//                            public void onNegative(MaterialDialog dialog) {
//                                getActivity().setRequestedOrientation(prevOrientation);
//                            }
//                        })
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
            return;
        }

        lista.close();

        String sql = "INSERT INTO CUST_LISTS"
                + " VALUES (" + idLista + ", "
                + listPosition + ", "
                + idDaAgg
                + ", CURRENT_TIMESTAMP)";
        db.execSQL(sql);
        db.close();

//        Toast.makeText(getActivity()
//                , getString(R.string.list_added), Toast.LENGTH_SHORT).show();
        Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT)
                .show();
    }

}