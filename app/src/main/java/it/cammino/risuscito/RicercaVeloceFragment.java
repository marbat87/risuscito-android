package it.cammino.risuscito;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import butterknife.Unbinder;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.items.SimpleItem;

public class RicercaVeloceFragment extends Fragment implements View.OnCreateContextMenuListener, SimpleDialogFragment.SimpleCallback {

    // create boolean for fetching data
    private boolean isViewShown = true;

    private DatabaseCanti listaCanti;
    private View rootView;
    FastItemAdapter<SimpleItem> cantoAdapter;

    private String titoloDaAgg;
    private int idDaAgg;
    private int idListaDaAgg;
    private int posizioneDaAgg;
    private ListaPersonalizzata[] listePers;
    private int[] idListe;
    private int idListaClick;
    private int idPosizioneClick;

    private final int ID_FITTIZIO = 99999999;

    private LUtils mLUtils;

    private long mLastClickTime = 0;

    @BindView(R.id.matchedList) RecyclerView mRecyclerView;
    @BindView(R.id.textfieldRicerca) EditText searchPar;
    @BindView(R.id.search_no_results) View mNoResults;
    @BindView(R.id.consegnati_only_view) View mConsegnatiOnly;

    @OnClick(R.id.pulisci_ripple)
    public void pulisciRisultati() {
        searchPar.setText("");
        mNoResults.setVisibility(View.GONE);
    }

    @OnEditorAction(R.id.textfieldRicerca)
    public boolean nascondiTastiera(TextView v, int actionId, KeyEvent evemt) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            //to hide soft keyboard
            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(searchPar.getWindowToken(), 0);
            return true;
        }
        return false;
    }

    @OnTextChanged(value = R.id.textfieldRicerca, callback = OnTextChanged.Callback.TEXT_CHANGED)
    void ricercaCambiata(CharSequence s, int start, int before, int count) {
        ricercaStringa(s.toString());
    }

    private Unbinder mUnbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_ricerca_titolo, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        if (listaCanti == null)
            listaCanti = new DatabaseCanti(getActivity());

        mConsegnatiOnly.setVisibility(View.GONE);

        FastAdapter.OnClickListener<SimpleItem> mOnClickListener = new FastAdapter.OnClickListener<SimpleItem>() {
            @Override
            public boolean onClick(View view, IAdapter<SimpleItem> iAdapter, SimpleItem item, int i) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
                    return true;
                mLastClickTime = SystemClock.elapsedRealtime();
                Bundle bundle = new Bundle();
                bundle.putCharSequence("pagina", item.getSource().getText());
                bundle.putInt("idCanto", item.getId());

                // lancia l'activity che visualizza il canto passando il parametro creato
                startSubActivity(bundle, view);
                return true;
            }
        };

        cantoAdapter = new FastItemAdapter<>();
        cantoAdapter.setHasStableIds(true);
        cantoAdapter.withOnClickListener(mOnClickListener);

        mRecyclerView.setAdapter(cantoAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setHasFixedSize(true);
        DividerItemDecoration insetDivider = new DividerItemDecoration(getContext(), llm.getOrientation());
        insetDivider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.inset_divider_light));
        mRecyclerView.addItemDecoration(insetDivider);

//        searchPar.addTextChangedListener(new TextWatcher() {
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before,
//                                      int count) {
//
//                String tempText = ((EditText) getActivity().findViewById(R.id.tempTextField)).getText().toString();
//                if (!tempText.equals(s.toString()))
//                    ((EditText) getActivity().findViewById(R.id.tempTextField)).setText(s);
//
//
//                if (s.length() >= 3) {
//                    mNoResults.setVisibility(View.GONE);
//
//                    String stringa = Utility.removeAccents(s.toString()).toLowerCase();
//                    String titoloTemp;
//                    Log.d(getClass().getName(), "onTextChanged: stringa " + stringa);
//
//                    // crea un manipolatore per il Database in modalità READ
//                    SQLiteDatabase db = listaCanti.getReadableDatabase();
//
//                    // lancia la ricerca di tutti i titoli presenti in DB e li
//                    // dispone in ordine alfabetico
//                    String query = "SELECT titolo, color, pagina, _id, source"
//                            + "		FROM ELENCO ORDER BY titolo ASC";
//                    Cursor lista = db.rawQuery(query, null);
//
//                    // recupera il numero di record trovati
//                    int total = lista.getCount();
//
//                    // crea un array e ci memorizza i titoli estratti
//                    List<SimpleItem> titoli = new ArrayList<>();
//                    cantoAdapter.clear();
//
//                    lista.moveToFirst();
//                    for (int i = 0; i < total; i++) {
//                        titoloTemp = Utility.removeAccents(lista.getString(0).toLowerCase());
//                        if (titoloTemp.contains(stringa)) {
//                            SimpleItem simpleItem = new SimpleItem();
//                            simpleItem.withTitle(lista.getString(0))
//                                    .withColor(lista.getString(1))
//                                    .withPage(String.valueOf(lista.getInt(2)))
//                                    .withId(lista.getInt(3))
//                                    .withSource(lista.getString(4))
//                                    .withContextMenuListener(RicercaVeloceFragment.this);
//                            titoli.add(simpleItem);
//                        }
//                        lista.moveToNext();
//                    }
//
//                    // chiude il cursore
//                    lista.close();
//
//                    cantoAdapter.add(titoli);
//                    cantoAdapter.notifyDataSetChanged();
//
//                    if (total == 0)
////                        rootView.findViewById(R.id.search_no_results).setVisibility(View.VISIBLE);
//                        mNoResults.setVisibility(View.VISIBLE);
//                } else {
//                    if (s.length() == 0) {
//                        cantoAdapter.clear();
//                        mNoResults.setVisibility(View.GONE);
//                    }
//                }
//
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count,
//                                          int after) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//
//        });

//        searchPar.setOnEditorActionListener(new EditText.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    //to hide soft keyboard
//                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
//                            .hideSoftInputFromWindow(searchPar.getWindowToken(), 0);
//                    return true;
//                }
//                return false;
//            }
//        });

        ((EditText) getActivity().findViewById(R.id.tempTextField)).addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                String tempText = searchPar.getText().toString();
                if (!tempText.equals(s.toString()))
                    searchPar.setText(s);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(Editable s) { }

        });

        mLUtils = LUtils.getInstance(getActivity());

        if (savedInstanceState != null) {
            Log.d(getClass().getName(), "onCreateView: RESTORING");
            idDaAgg = savedInstanceState.getInt("idDaAgg", 0);
            idPosizioneClick = savedInstanceState.getInt("idPosizioneClick", 0);
            idListaClick = savedInstanceState.getInt("idListaClick", 0);
            idListaDaAgg = savedInstanceState.getInt("idListaDaAgg", 0);
            posizioneDaAgg = savedInstanceState.getInt("posizioneDaAgg", 0);
            SimpleDialogFragment sFragment = SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "VELOCE_REPLACE");
            if (sFragment != null)
                sFragment.setmCallback(RicercaVeloceFragment.this);
            sFragment = SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "VELOCE_REPLACE_2");
            if (sFragment != null)
                sFragment.setmCallback(RicercaVeloceFragment.this);
        }

        if (!isViewShown) {
            SQLiteDatabase db = listaCanti.getReadableDatabase();
            String query = "SELECT _id, lista" +
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
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    /**
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("idDaAgg", idDaAgg);
        outState.putInt("idPosizioneClick", idPosizioneClick);
        outState.putInt("idListaClick", idListaClick);
        outState.putInt("idListaDaAgg", idListaDaAgg);
        outState.putInt("posizioneDaAgg", posizioneDaAgg);
    }

    /**
     * Set a hint to the system about whether this fragment's UI is currently visible
     * to the user. This hint defaults to true and is persistent across fragment instance
     * state save and restore.
     * <p/>
     * <p>An app may set this to false to indicate that the fragment's UI is
     * scrolled out of visibility or is otherwise not directly visible to the user.
     * This may be used by the system to prioritize operations such as fragment lifecycle updates
     * or loader ordering behavior.</p>
     *
     * @param isVisibleToUser true if this fragment's UI is currently visible to the user (default),
     *                        false if it is not.
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getView() != null) {
                isViewShown = true;
                Log.d(getClass().getName(), "VISIBLE");
                if (listaCanti == null)
                    listaCanti = new DatabaseCanti(getActivity());
                SQLiteDatabase db = listaCanti.getReadableDatabase();
                String query = "SELECT _id, lista" +
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

                //to hide soft keyboard
                if (searchPar != null)
                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(searchPar.getWindowToken(), 0);
            }
            else
                isViewShown = false;
        }
    }

    @Override
    public void onDestroy() {
        if (listaCanti != null)
            listaCanti.close();
        super.onDestroy();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        titoloDaAgg = ((TextView) v.findViewById(R.id.text_title)) .getText().toString();
        idDaAgg = Integer.valueOf(((TextView) v.findViewById(R.id.text_id_canto)) .getText().toString());
        menu.setHeaderTitle("Aggiungi canto a:");

        for (int i = 0; i < idListe.length; i++) {
            SubMenu subMenu = menu.addSubMenu(ID_FITTIZIO, Menu.NONE, 10 + i,
                    listePers[i].getName());
            for (int k = 0; k < listePers[i].getNumPosizioni(); k++) {
                subMenu.add(100 + i, k, k, listePers[i].getNomePosizione(k));
            }
        }

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.add_to, menu);

        SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(getActivity());
        menu.findItem(R.id.add_to_p_pace).setVisible(pref.getBoolean(Utility.SHOW_PACE, false));
        menu.findItem(R.id.add_to_e_seconda).setVisible(pref.getBoolean(Utility.SHOW_SECONDA, false));
        menu.findItem(R.id.add_to_e_offertorio).setVisible(pref.getBoolean(Utility.SHOW_OFFERTORIO, false));
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
                case R.id.add_to_e_offertorio:
                    addToListaNoDup(2, 8);
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

                        //recupero ID del canto cliccato
                        SQLiteDatabase db = listaCanti.getReadableDatabase();
                        if (listePers[idListaClick]
                                .getCantoPosizione(idPosizioneClick).equals("")) {
                            listePers[idListaClick].addCanto(String.valueOf(idDaAgg), idPosizioneClick);
                            ContentValues  values = new  ContentValues( );
                            values.put("lista" , ListaPersonalizzata.serializeObject(listePers[idListaClick]));
                            db.update("LISTE_PERS", values, "_id = " + idListe[idListaClick], null);
                            db.close();

                            Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT)
                                    .show();
                        }
                        else {
                            if (listePers[idListaClick].getCantoPosizione(idPosizioneClick).equals(String.valueOf(idDaAgg)))
                                Snackbar.make(rootView
                                        , R.string.present_yet
                                        , Snackbar.LENGTH_SHORT)
                                        .show();
                            else {
                                //recupero titolo del canto presente
                                String query = "SELECT titolo" +
                                        "		FROM ELENCO" +
                                        "		WHERE _id = "
                                        + listePers[idListaClick].getCantoPosizione(idPosizioneClick);
                                Cursor cursor = db.rawQuery(query, null);
                                cursor.moveToFirst();
                                new SimpleDialogFragment.Builder((AppCompatActivity)getActivity(), RicercaVeloceFragment.this, "VELOCE_REPLACE")
                                        .title(R.string.dialog_replace_title)
                                        .content(getString(R.string.dialog_present_yet) + " "
                                                + cursor.getString(0)
                                                + getString(R.string.dialog_wonna_replace))
                                        .positiveButton(R.string.confirm)
                                        .negativeButton(R.string.dismiss)
                                        .show();
                                cursor.close();
                                db.close();
                                cursor.close();
                                db.close();
                            }
                        }
                        return true;
                    }
                    else
                        return super.onContextItemSelected(item);
            }
        } else
            return false;
    }

    // aggiunge il canto premuto ai preferiti
    public void addToFavorites() {
        SQLiteDatabase db = listaCanti.getReadableDatabase();
        String sql = "UPDATE ELENCO"
                + "  SET favourite = 1"
                + "  WHERE _id =  " + idDaAgg;
        db.execSQL(sql);
        db.close();
        Snackbar.make(rootView, R.string.favorite_added, Snackbar.LENGTH_SHORT)
                .show();
    }

    // aggiunge il canto premuto ad una lista e in una posizione che ammetta
    // duplicati
    public void addToListaDup(int idLista, int listPosition) {

        SQLiteDatabase db = listaCanti.getReadableDatabase();

        String sql = "INSERT INTO CUST_LISTS ";
        sql += "VALUES (" + idLista + ", " + listPosition + ", "
                + idDaAgg
                + ", CURRENT_TIMESTAMP)";

        try {
            db.execSQL(sql);
            Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT)
                    .show();
        } catch (SQLException e) {
            Snackbar.make(rootView
                    , R.string.present_yet
                    , Snackbar.LENGTH_SHORT)
                    .show();
        }

        db.close();
    }

    // aggiunge il canto premuto ad una lista e in una posizione che NON ammetta
    // duplicati
    public void addToListaNoDup(int idLista, int listPosition) {

        SQLiteDatabase db = listaCanti.getReadableDatabase();

        // cerca se la posizione nella lista è già occupata
        String query = "SELECT B.titolo" + "		FROM CUST_LISTS A"
                + "		   , ELENCO B" + "		WHERE A._id = " + idLista
                + "         AND A.position = " + listPosition
                + "         AND A.id_canto = B._id";
        Cursor lista = db.rawQuery(query, null);

        int total = lista.getCount();

        if (total > 0) {
            lista.moveToFirst();
            String titoloPresente = lista.getString(0);
            lista.close();
            db.close();

            if (titoloDaAgg.equalsIgnoreCase(titoloPresente)) {
                Snackbar.make(rootView
                        , R.string.present_yet
                        , Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                idListaDaAgg = idLista;
                posizioneDaAgg = listPosition;
                new SimpleDialogFragment.Builder((AppCompatActivity)getActivity(), RicercaVeloceFragment.this, "VELOCE_REPLACE_2")
                        .title(R.string.dialog_replace_title)
                        .content(getString(R.string.dialog_present_yet) + " " + titoloPresente
                                + getString(R.string.dialog_wonna_replace))
                        .positiveButton(R.string.confirm)
                        .negativeButton(R.string.dismiss)
                        .show();

            }
            return;
        }

        lista.close();

        String sql = "INSERT INTO CUST_LISTS " + "VALUES (" + idLista + ", "
                + listPosition + ", "
                + idDaAgg
                + ", CURRENT_TIMESTAMP)";
        db.execSQL(sql);
        db.close();

        Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT)
                .show();
    }

    private void startSubActivity(Bundle bundle, View view) {
        Intent intent = new Intent(getActivity().getApplicationContext(),
                PaginaRenderActivity.class);
        intent.putExtras(bundle);
        mLUtils.startActivityWithTransition(intent, view, Utility.TRANS_PAGINA_RENDER);
    }

    @Override
    public void onPositive(@NonNull String tag) {
        Log.d(getClass().getName(), "onPositive: " + tag);
        switch (tag) {
            case "VELOCE_REPLACE":
                SQLiteDatabase db = listaCanti.getReadableDatabase();
                listePers[idListaClick].addCanto(String.valueOf(idDaAgg), idPosizioneClick);

                ContentValues values = new ContentValues();
                values.put("lista", ListaPersonalizzata.serializeObject(listePers[idListaClick]));
                db.update("LISTE_PERS", values, "_id = " + idListe[idListaClick], null);
                db.close();
                Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT)
                        .show();
                break;
            case "VELOCE_REPLACE_2":
                db = listaCanti.getReadableDatabase();
                String sql = "UPDATE CUST_LISTS "
                        + " SET id_canto = " + idDaAgg
                        + " WHERE _id = " + idListaDaAgg
                        + " AND position = " + posizioneDaAgg;
                db.execSQL(sql);
                db.close();
                Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT)
                        .show();
                break;
        }
    }
    @Override
    public void onNegative(@NonNull String tag) {}
    @Override
    public void onNeutral(@NonNull String tag) {}

    private void ricercaStringa(String s) {
        String tempText = ((EditText) getActivity().findViewById(R.id.tempTextField)).getText().toString();
        if (!tempText.equals(s))
            ((EditText) getActivity().findViewById(R.id.tempTextField)).setText(s);

        if (s.length() >= 3) {
            mNoResults.setVisibility(View.GONE);

            String stringa = Utility.removeAccents(s).toLowerCase();
            String titoloTemp;
            Log.d(getClass().getName(), "onTextChanged: stringa " + stringa);

            // crea un manipolatore per il Database in modalità READ
            SQLiteDatabase db = listaCanti.getReadableDatabase();

            // lancia la ricerca di tutti i titoli presenti in DB e li
            // dispone in ordine alfabetico
            String query = "SELECT titolo, color, pagina, _id, source"
                    + "		FROM ELENCO ORDER BY titolo ASC";
            Cursor lista = db.rawQuery(query, null);

            // recupera il numero di record trovati
            int total = lista.getCount();

            // crea un array e ci memorizza i titoli estratti
            List<SimpleItem> titoli = new ArrayList<>();
            cantoAdapter.clear();

            lista.moveToFirst();
            for (int i = 0; i < total; i++) {
                titoloTemp = Utility.removeAccents(lista.getString(0).toLowerCase());
//                Log.d(getClass().getName(), "ricercaStringa: " + titoloTemp);
//                Log.d(getClass().getName(), "ricercaStringa: " + stringa);
                if (titoloTemp.contains(stringa)) {
                    SimpleItem simpleItem = new SimpleItem();
                    simpleItem.withTitle(lista.getString(0))
                            .withColor(lista.getString(1))
                            .withPage(String.valueOf(lista.getInt(2)))
                            .withId(lista.getInt(3))
                            .withSource(lista.getString(4))
                            .withNormalizedTitle(titoloTemp)
                            .withFilter(stringa)
                            .withContextMenuListener(RicercaVeloceFragment.this);
                    titoli.add(simpleItem);
                }
                lista.moveToNext();
            }

            // chiude il cursore
            lista.close();

            cantoAdapter.add(titoli);
            cantoAdapter.notifyDataSetChanged();

            if (total == 0)
//                        rootView.findViewById(R.id.search_no_results).setVisibility(View.VISIBLE);
                mNoResults.setVisibility(View.VISIBLE);
        } else {
            if (s.isEmpty()) {
                cantoAdapter.clear();
                mNoResults.setVisibility(View.GONE);
            }
        }
    }

}
