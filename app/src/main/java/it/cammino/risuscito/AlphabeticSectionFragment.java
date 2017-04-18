package it.cammino.risuscito;

import android.content.ContentValues;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.turingtechnologies.materialscrollbar.CustomIndicator;
import com.turingtechnologies.materialscrollbar.DragScrollBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import it.cammino.risuscito.adapters.FastScrollIndicatorAdapter;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.items.SimpleItem;
import it.cammino.risuscito.ui.HFFragment;


public class AlphabeticSectionFragment extends HFFragment implements View.OnCreateContextMenuListener
        , SimpleDialogFragment.SimpleCallback {

    private final String TAG = getClass().getCanonicalName();

    // create boolean for fetching data
    private boolean isViewShown = true;

    private DatabaseCanti listaCanti;
    private String titoloDaAgg;
    private int idDaAgg;
    private int idListaDaAgg;
    private int posizioneDaAgg;
    private ListaPersonalizzata[] listePers;
    private int[] idListe;
    private int idListaClick;
    private int idPosizioneClick;
    private View rootView;

    private final int ID_FITTIZIO = 99999999;
//    private final int ID_BASE = 100;

    private LUtils mLUtils;

    private long mLastClickTime = 0;

//    private FastScrollIndicatorAdapter<SimpleItem> mAdapter;

    @BindView(R.id.cantiList) RecyclerView mRecyclerView;
    @BindView(R.id.dragScrollBar) DragScrollBar mDragScrollBar;
//    @BindView(R.id.fast_scroller) FastScroller mFastScroller;

    private Unbinder mUnbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_alphanum_index, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        //crea un istanza dell'oggetto DatabaseCanti
        if (listaCanti == null)
            listaCanti = new DatabaseCanti(getActivity());

        SQLiteDatabase db = listaCanti.getReadableDatabase();

        // lancia la ricerca di tutti i titoli presenti in DB e li dispone in ordine alfabetico
        String query = "SELECT _id, titolo, color, pagina, source" +
                "		FROM ELENCO" +
                "		ORDER BY TITOLO ASC";
        Cursor lista = db.rawQuery(query, null);

        //recupera il numero di record trovati
        int total = lista.getCount();

        // crea un array e ci memorizza i titoli estratti
//        List<CantoRecycled> titoli = new ArrayList<>();
//        List<AbstractFlexibleItem> mItems = new ArrayList<>();
        List<SimpleItem> mItems = new ArrayList<>();
        lista.moveToFirst();
        for (int i = 0; i < total; i++) {
//            titoli.add(new CantoRecycled(lista.getString(1)
//                    , lista.getInt(3)
//                    , lista.getString(2)
//                    , lista.getInt(0)
//                    , lista.getString(4)));
//            CantoItem mCanto = new CantoItem(String.valueOf(lista.getInt(0)), lista.getString(1));
//            mCanto.setCantoId(lista.getInt(0));
//            mCanto.setColor(lista.getString(2));
//            mCanto.setPage(String.valueOf(lista.getInt(3)));
//            mCanto.setSource(lista.getString(4));
//            mCanto.setActiveColor(getThemeUtils().accentColor());
//            mItems.add(mCanto);
            SimpleItem sampleItem = new SimpleItem();
            sampleItem
                    .withTitle(lista.getString(1))
                    .withPage(String.valueOf(lista.getInt(3)))
                    .withSource(lista.getString(4))
                    .withColor(lista.getString(2))
                    .withId(lista.getInt(0))
                    .withContextMenuListener(AlphabeticSectionFragment.this);
//                    .withIdentifier(lista.getInt(0));
            mItems.add(sampleItem);
            lista.moveToNext();
        }

        // chiude il cursore
        lista.close();

//        View.OnClickListener clickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
//                    return;
//                mLastClickTime = SystemClock.elapsedRealtime();
//                // recupera il titolo della voce cliccata
//                String idCanto = ((TextView) v.findViewById(R.id.text_id_canto))
//                        .getText().toString();
//                String source = ((TextView) v.findViewById(R.id.text_source_canto))
//                        .getText().toString();
//
//                // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da visualizzare
//                Bundle bundle = new Bundle();
//                bundle.putString("pagina", source);
//                bundle.putInt("idCanto", Integer.parseInt(idCanto));
//
//                // lancia l'activity che visualizza il canto passando il parametro creato
//                startSubActivity(bundle, v);
//            }
//        };

//        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.cantiList);

//        CantoAdapter adapter = new CantoAdapter(getActivity(), 0, titoli, clickListener, this);
//        adapter.setHasStableIds(true);
//        mRecyclerView.setAdapter(adapter);
//
//        // Setting the layoutManager
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        mRecyclerView.setHasFixedSize(true);
//
//        new DragScrollBar(getActivity(), mRecyclerView, true)

        FastAdapter.OnClickListener<SimpleItem> mOnClickListener = new FastAdapter.OnClickListener<SimpleItem>() {
            @Override
            public boolean onClick(View view, IAdapter<SimpleItem> iAdapter, SimpleItem item, int i) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
                    return false;
                mLastClickTime = SystemClock.elapsedRealtime();
                Bundle bundle = new Bundle();
                bundle.putString("pagina", item.getSource().getText());
                bundle.putInt("idCanto", item.getId());

                // lancia l'activity che visualizza il canto passando il parametro creato
                startSubActivity(bundle, view);
                return true;
            }
        };

        mDragScrollBar
                .setIndicator(new CustomIndicator(getActivity()), true);
//                .setHandleColour(getThemeUtils().accentColor())
//                .setHandleOffColour(getThemeUtils().accentColor());

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        mAdapter = new CantoBubbleAdapter(mItems, AlphabeticSectionFragment.this, 0);
        FastScrollIndicatorAdapter<SimpleItem> mAdapter = new FastScrollIndicatorAdapter<>(0);
        mAdapter.withOnClickListener(mOnClickListener)
                .setHasStableIds(true);
        mAdapter.add(mItems);
//        registerForContextMenu(mRecyclerView);

//        mRecyclerView.setAdapter(mAdapter);
//        mRecyclerView.setHasFixedSize(true); //Size of RV will not change
//        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(llm);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        DividerItemDecoration insetDivider = new DividerItemDecoration(getContext(), llm.getOrientation());
        insetDivider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.inset_divider_light));
        mRecyclerView.addItemDecoration(insetDivider);

//        mAdapter.setFastScroller(mFastScroller, getThemeUtils().accentColor());

        mLUtils = LUtils.getInstance(getActivity());

        if (savedInstanceState != null) {
            Log.d(getClass().getName(), "onCreateView: RESTORING");
            idDaAgg = savedInstanceState.getInt("idDaAgg", 0);
            idPosizioneClick = savedInstanceState.getInt("idPosizioneClick", 0);
            idListaClick = savedInstanceState.getInt("idListaClick", 0);
            idListaDaAgg = savedInstanceState.getInt("idListaDaAgg", 0);
            posizioneDaAgg = savedInstanceState.getInt("posizioneDaAgg", 0);
            SimpleDialogFragment fragment = SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "ALPHA_REPLACE");
            if (fragment != null)
                fragment.setmCallback(AlphabeticSectionFragment.this);
            fragment = SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "ALPHA_REPLACE_2");
            if (fragment != null)
                fragment.setmCallback(AlphabeticSectionFragment.this);
        }

        if (!isViewShown) {
            query = "SELECT _id, lista" +
                    "		FROM LISTE_PERS" +
                    "		ORDER BY _id ASC";
            lista = db.rawQuery(query, null);

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
        Log.d(TAG, "onContextItemSelected: " + item.getItemId());
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
                                Snackbar.make(rootView
                                        , R.string.present_yet
                                        , Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                            else {
                                //recupero titolo del canto presente
                                String query = "SELECT titolo" +
                                        "		FROM ELENCO" +
                                        "		WHERE _id = "
                                        + listePers[idListaClick].getCantoPosizione(idPosizioneClick);
                                Cursor cursor = db.rawQuery(query, null);
                                cursor.moveToFirst();
                                new SimpleDialogFragment.Builder((AppCompatActivity)getActivity(), AlphabeticSectionFragment.this, "ALPHA_REPLACE")
                                        .title(R.string.dialog_replace_title)
                                        .content(getString(R.string.dialog_present_yet) + " "
                                                + cursor.getString(0)
                                                + getString(R.string.dialog_wonna_replace))
                                        .positiveButton(R.string.confirm)
                                        .negativeButton(R.string.dismiss)
                                        .show();
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
            Snackbar.make(rootView, R.string.present_yet, Snackbar.LENGTH_SHORT)
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

            if (titoloDaAgg.equalsIgnoreCase(titoloPresente)) {
                Snackbar.make(rootView, R.string.present_yet, Snackbar.LENGTH_SHORT)
                        .show();
            }
            else {
                idListaDaAgg = idLista;
                posizioneDaAgg = listPosition;
                new SimpleDialogFragment.Builder((AppCompatActivity)getActivity(), AlphabeticSectionFragment.this, "ALPHA_REPLACE_2")
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

        String sql = "INSERT INTO CUST_LISTS "
                + "VALUES (" + idLista + ", "
                + listPosition + ", "
                + idDaAgg
                + ", CURRENT_TIMESTAMP)";
        db.execSQL(sql);
        db.close();

        Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onPositive(@NonNull String tag) {
        Log.d(getClass().getName(), "onPositive: " + tag);
        switch (tag) {
            case "ALPHA_REPLACE":
                SQLiteDatabase db = listaCanti.getReadableDatabase();
                listePers[idListaClick].addCanto(String.valueOf(idDaAgg), idPosizioneClick);

                ContentValues values = new ContentValues();
                values.put("lista", ListaPersonalizzata.serializeObject(listePers[idListaClick]));
                db.update("LISTE_PERS", values, "_id = " + idListe[idListaClick], null);
                db.close();
                Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT)
                        .show();
                break;
            case "ALPHA_REPLACE_2":
                db = listaCanti.getReadableDatabase();
                String sql = "UPDATE CUST_LISTS"
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

}