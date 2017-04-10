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
import android.support.v7.app.AppCompatActivity;
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
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.itemanimators.SlideDownAlphaAnimator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.items.SimpleSubExpandableItem;
import it.cammino.risuscito.items.SimpleSubItem;
import it.cammino.risuscito.ui.HFFragment;
import it.cammino.risuscito.utils.ThemeUtils;

public class IndiceLiturgicoFragment extends HFFragment implements View.OnCreateContextMenuListener
        , SimpleDialogFragment.SimpleCallback {

    //    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "RecyclerViewExpandableItemManagerLit";
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
    private final int ID_BASE = 100;

    private LUtils mLUtils;

    private FastItemAdapter<IItem> mAdapter;
    private LinearLayoutManager mLayoutManager;

    private long mLastClickTime = 0;

//    private RecyclerView mRecyclerView;
//    private RecyclerView.LayoutManager mLayoutManager;
//    private RecyclerView.Adapter mWrappedAdapter;
//    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;

    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.layout_recycler, container, false);
        ButterKnife.bind(this, rootView);

        FastAdapter.OnClickListener<SimpleSubItem> mOnClickListener = new FastAdapter.OnClickListener<SimpleSubItem>() {
            @Override
            public boolean onClick(View view, IAdapter<SimpleSubItem> iAdapter, SimpleSubItem item, int i) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
                    return false;
                mLastClickTime = SystemClock.elapsedRealtime();
                Bundle bundle = new Bundle();
                bundle.putString("pagina", item.getSource().getText());
                bundle.putInt("idCanto", item.getId());

                // lancia l'activity che visualizza il canto passando il parametro creato
                startSubActivity(bundle);
                return true;
            }
        };

        //crea un istanza dell'oggetto DatabaseCanti
        if (listaCanti == null)
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

//        List<Pair<ExpandableGroup, List<CantoRecycled>>> dataItems = new ArrayList<>();
        List<IItem> mItems = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            String argId =  String.valueOf(arguments.getInt(0));
            query = "SELECT B._id, B.titolo, B.color, B.pagina, B.source" +
                    "		FROM INDICE_LIT A, ELENCO B " +
                    "       WHERE A._id = " + argId +
                    "       AND A.id_canto = B._id " +
                    "		ORDER BY TITOLO ASC";
            Cursor argCanti = db.rawQuery(query, null);

            //recupera il numero di canti per il momento liturgico
            int totCanti = argCanti.getCount();
            argCanti.moveToFirst();

//            List<CantoRecycled> children =  new ArrayList<>();
//
//            for (int j = 0; j < totCanti; j++) {
//                children.add(new CantoRecycled(argCanti.getString(1)
//                        , argCanti.getInt(3)
//                        , argCanti.getString(2)
//                        , argCanti.getInt(0)
//                        , argCanti.getString(4)));
//                argCanti.moveToNext();
//            }
//
            SimpleSubExpandableItem expandableItem = new SimpleSubExpandableItem();
            expandableItem
                    .withTitle(arguments.getString(1) + " (" + totCanti + ")")
                    .withColor(getThemeUtils().primaryColorDark())
                    .withOnClickListener(new FastAdapter.OnClickListener<SimpleSubExpandableItem>() {
                        @Override
                        public boolean onClick(View view, IAdapter<SimpleSubExpandableItem> iAdapter, SimpleSubExpandableItem item, int i) {
                            Log.d(getClass().getName(), "SimpleSubExpandableItem onClick: ");
                            if (item.isExpanded())
                                mLayoutManager.scrollToPositionWithOffset(i, 0);
                            return false;
                        }
                    })
                    .withIdentifier(Integer.parseInt(argId));
            List<SimpleSubItem> subItems = new LinkedList<>();

            for (int j = 1; j <= totCanti; j++) {
//                Log.d(getClass().getName(), "onCreateView: " + argCanti.getString(1));
                SimpleSubItem simpleItem = new SimpleSubItem()
                        .withTitle(argCanti.getString(1))
                        .withPage(String.valueOf(argCanti.getInt(3)))
                        .withSource(argCanti.getString(4))
                        .withColor(argCanti.getString(2))
                        .withId(argCanti.getInt(0));
                simpleItem.withContextMenuListener(IndiceLiturgicoFragment.this)
                        .withOnItemClickListener(mOnClickListener);
                simpleItem.withIdentifier(Integer.parseInt(argId) * 1000 + j);
                subItems.add(simpleItem);
                argCanti.moveToNext();
            }
            argCanti.close();
            expandableItem.withSubItems(subItems);

//            dataItems.add(new Pair(
//                    new ExpandableGroup(arguments.getString(1), arguments.getInt(0))
//                    , children));
            mItems.add(expandableItem);

            arguments.moveToNext();

        }

        arguments.close();

//        View.OnClickListener clickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
//                    return;
//                mLastClickTime = SystemClock.elapsedRealtime();
//                // recupera il titolo della voce cliccata
//                String idCanto = String.valueOf(((TextView) v.findViewById(R.id.text_id_canto))
//                        .getText());
//                String source = String.valueOf(((TextView) v.findViewById(R.id.text_source_canto))
//                        .getText());
//                // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da visualizzare
//                Bundle bundle = new Bundle();
//                bundle.putString("pagina", source);
//                bundle.putInt("idCanto", Integer.parseInt(idCanto));
//                // lancia l'activity che visualizza il canto passando il parametro creato
//                startSubActivity(bundle, v);
//            }
//        };

//        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
//        mLayoutManager = new LinearLayoutManager(getActivity());
//
//        final Parcelable eimSavedState = (savedInstanceState != null) ? savedInstanceState.getParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER) : null;
//        mRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(eimSavedState);
//
//        //adapter
//        CantoExpandableAdapter myItemAdapter = new CantoExpandableAdapter(getActivity(), dataItems, clickListener, IndiceLiturgicoFragment.this);
//        mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(myItemAdapter);       // wrap for expanding
//
//        // Change animations are enabled by default since support-v7-recyclerview v22.
//        // Need to disable them when using animation indicator.
//        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
//        animator.setSupportsChangeAnimations(false);
//
//        mRecyclerView.setLayoutManager(mLayoutManager);
//        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
//        mRecyclerView.setItemAnimator(animator);
//        mRecyclerView.setHasFixedSize(false);
//
//        mRecyclerViewExpandableItemManager.attachRecyclerView(mRecyclerView);
//        mRecyclerView.setLayoutManager(new SmoothScrollLinearLayoutManager(getActivity()));
//        mAdapter = new FlexibleAdapter(mItems, IndiceLiturgicoFragment.this);
//        registerForContextMenu(mRecyclerView);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new FastItemAdapter<>();
        mAdapter.add(mItems);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true); //Size of RV will not change
        mRecyclerView.setItemAnimator(new SlideDownAlphaAnimator());

        //restore selections (this has to be done after the items were added
        Log.d(TAG, "onCreateView: ");
        mAdapter.withSavedInstanceState(savedInstanceState);

        mLUtils = LUtils.getInstance(getActivity());

        if (savedInstanceState != null) {
            Log.d(getClass().getName(), "onCreateView: RESTORING");
            idDaAgg = savedInstanceState.getInt("idDaAgg", 0);
            idPosizioneClick = savedInstanceState.getInt("idPosizioneClick", 0);
            idListaClick = savedInstanceState.getInt("idListaClick", 0);
            idListaDaAgg = savedInstanceState.getInt("idListaDaAgg", 0);
            posizioneDaAgg = savedInstanceState.getInt("posizioneDaAgg", 0);
            SimpleDialogFragment sFragment = SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "LITURGICO_REPLACE");
            if (sFragment != null)
                sFragment.setmCallback(IndiceLiturgicoFragment.this);
            sFragment = SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "LITURGICO_REPLACE_2");
            if (sFragment != null)
                sFragment.setmCallback(IndiceLiturgicoFragment.this);
        }

        if (!isViewShown) {
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
        }

        db.close();

        return rootView;
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
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: ");
        if (getUserVisibleHint()) {
            outState = mAdapter.saveInstanceState(outState);
            outState.putInt("idDaAgg", idDaAgg);
            outState.putInt("idPosizioneClick", idPosizioneClick);
            outState.putInt("idListaClick", idListaClick);
            outState.putInt("idListaDaAgg", idListaDaAgg);
            outState.putInt("posizioneDaAgg", posizioneDaAgg);
        }
        super.onSaveInstanceState(outState);
    }

//    @Override
//    public void onDestroyView() {
//        if (mRecyclerViewExpandableItemManager != null) {
//            mRecyclerViewExpandableItemManager.release();
//            mRecyclerViewExpandableItemManager = null;
//        }
//
//        if (mRecyclerView != null) {
//            mRecyclerView.setItemAnimator(null);
//            mRecyclerView.setAdapter(null);
//            mRecyclerView = null;
//        }
//
//        if (mWrappedAdapter != null) {
//            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
//            mWrappedAdapter = null;
//        }
//        mLayoutManager = null;
//
//        super.onDestroyView();
//    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        if (listaCanti != null)
            listaCanti.close();
        super.onDestroy();
    }

//    private void startSubActivity(Bundle bundle, View view) {
//        Intent intent = new Intent(getActivity(), PaginaRenderActivity.class);
//        intent.putExtras(bundle);
//        mLUtils.startActivityWithTransition(intent, view, Utility.TRANS_PAGINA_RENDER);
//    }

    private void startSubActivity(Bundle bundle) {
        Intent intent = new Intent(getActivity(), PaginaRenderActivity.class);
        intent.putExtras(bundle);
        mLUtils.startActivityWithTransition(intent, null, Utility.TRANS_PAGINA_RENDER);
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
                                //recupero titolo del canto presente
                                String query = "SELECT titolo" +
                                        "		FROM ELENCO" +
                                        "		WHERE _id = "
                                        + listePers[idListaClick].getCantoPosizione(idPosizioneClick);
                                Cursor cursor = db.rawQuery(query, null);
                                cursor.moveToFirst();
                                new SimpleDialogFragment.Builder((AppCompatActivity)getActivity(), IndiceLiturgicoFragment.this, "LITURGICO_REPLACE")
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
                new SimpleDialogFragment.Builder((AppCompatActivity)getActivity(), IndiceLiturgicoFragment.this, "LITURGICO_REPLACE_2")
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

        String sql = "INSERT INTO CUST_LISTS"
                + " VALUES (" + idLista + ", "
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
            case "LITURGICO_REPLACE":
                SQLiteDatabase db = listaCanti.getReadableDatabase();
                listePers[idListaClick].addCanto(String.valueOf(idDaAgg), idPosizioneClick);

                ContentValues values = new ContentValues();
                values.put("lista", ListaPersonalizzata.serializeObject(listePers[idListaClick]));
                db.update("LISTE_PERS", values, "_id = " + idListe[idListaClick], null);
                db.close();
                Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT)
                        .show();
                break;
            case "LITURGICO_REPLACE_2":
                db = listaCanti.getReadableDatabase();
                String sql = "UPDATE CUST_LISTS "
                        + "     SET id_canto = " + idDaAgg
                        + "     WHERE _id = " + idListaDaAgg
                        + "     AND position = " + posizioneDaAgg;
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

//    @Override
//    public boolean onItemClick(int i) {
//        if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
//            return false;
//        mLastClickTime = SystemClock.elapsedRealtime();
//        IFlexible flexibleItem = mAdapter.getItem(i);
//        if (flexibleItem instanceof SubItem) {
//            SubItem subItem = (SubItem) flexibleItem;
//            Bundle bundle = new Bundle();
//            bundle.putString("pagina", subItem.getSource());
//            bundle.putInt("idCanto", subItem.getCantoId());
//
//            // lancia l'activity che visualizza il canto passando il parametro creato
//            startSubActivity(bundle);
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    public void onItemLongClick(int i) {
//        mContextIndex = i;
//        if (mAdapter.getItem(i) instanceof SubItem)
//            ((Activity) getContext()).openContextMenu(mRecyclerView);
//    }

    private ThemeUtils getThemeUtils() {
        return ((MainActivity)getActivity()).getThemeUtils();
    }
}