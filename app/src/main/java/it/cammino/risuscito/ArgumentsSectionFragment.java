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
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import java.util.ArrayList;
import java.util.List;

import it.cammino.risuscito.adapters.CantoExpandableAdapter;
import it.cammino.risuscito.objects.CantoRecycled;
import it.cammino.risuscito.objects.ExpandableGroup;

public class ArgumentsSectionFragment extends Fragment implements View.OnCreateContextMenuListener   {

    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "RecyclerViewExpandableItemManager";

    private DatabaseCanti listaCanti;
    //    private List<Map<String, String>> groupData;
//    private List<List<Map<String, String>>> childData;
//    private static final String NAME = "NAME";
//    private ExpandableListView expList;
//    int lastExpandedGroupPosition = 0;
    private String titoloDaAgg;
    private int idDaAgg;
    private int idListaDaAgg;
    private int posizioneDaAgg;
    private ListaPersonalizzata[] listePers;
    private int[] idListe;
    private int idListaClick;
    private int idPosizioneClick;
    private int prevOrientation;

    private final int ID_FITTIZIO = 99999999;
    private final int ID_BASE = 100;

    private LUtils mLUtils;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    //    private RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;
    private CantoExpandableAdapter myItemAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.layout_recycler, container, false);

//        expList = (ExpandableListView) rootView.findViewById(R.id.argomentiList);

        //crea un istanza dell'oggetto DatabaseCanti
        listaCanti = new DatabaseCanti(getActivity());

        // crea un manipolatore per il Database in modalità READ
        SQLiteDatabase db = listaCanti.getReadableDatabase();

        // lancia la ricerca di tutti i gli argomenti in DB e li dispone in ordine alfabetico
        String query = "SELECT _id, nome" +
                "		FROM ARG_NAMES" +
                "		ORDER BY nome ASC";
        Cursor arguments = db.rawQuery(query, null);

        //recupera il numero di argomenti trovati
        int total = arguments.getCount();
        arguments.moveToFirst();

//        groupData = new ArrayList<Map<String, String>>();
//        childData = new ArrayList<List<Map<String, String>>>();
        List<Pair<ExpandableGroup, List<CantoRecycled>>> dataItems = new ArrayList<>();

        for (int i = 0; i < total; i++) {

//            Map<String, String> curGroupMap = new HashMap<String, String>();
//            groupData.add(curGroupMap);
//
//            curGroupMap.put(NAME, arguments.getString(1));
            int argId =  arguments.getInt(0);

            query = "SELECT B._id, B.titolo, B.color, B.pagina, B.source" +
                    "		FROM ARGOMENTI A, ELENCO B " +
                    "       WHERE A._id = " + argId +
                    "       AND A.id_canto = B._id " +
                    "		ORDER BY TITOLO ASC";
            Cursor argCanti = db.rawQuery(query, null);

            //recupera il numero di canti per l'argomento
            int totCanti = argCanti.getCount();
            argCanti.moveToFirst();

//            List<Map<String, String>> children = new ArrayList<Map<String, String>>();
            List<CantoRecycled> children =  new ArrayList<>();

            for (int j = 0; j < totCanti; j++) {
//                Map<String, String> curChildMap = new HashMap<String, String>();
//                children.add(curChildMap);
//                curChildMap.put(NAME, Utility.intToString(argCanti.getInt(2),3)
//                        + argCanti.getString(1) + argCanti.getString(0));
                children.add(new CantoRecycled(argCanti.getString(1)
                        , argCanti.getInt(3)
                        , argCanti.getString(2)
                        , argCanti.getInt(0)
                        , argCanti.getString(4)));
                argCanti.moveToNext();
            }
//            childData.add(children);
            argCanti.close();

            dataItems.add(new Pair(
                    new ExpandableGroup(arguments.getString(1), arguments.getInt(0))
                    , children));

            arguments.moveToNext();

        }

//        expList.setAdapter(new SongRowAdapter());

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // recupera il titolo della voce cliccata
                String idCanto = ((TextView) v.findViewById(R.id.text_id_canto))
                        .getText().toString();
                String source = ((TextView) v.findViewById(R.id.text_source_canto))
                        .getText().toString();

                // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da visualizzare
                Bundle bundle = new Bundle();
                bundle.putString("pagina", source);
                bundle.putInt("idCanto", Integer.parseInt(idCanto));

                // lancia l'activity che visualizza il canto passando il parametro creato
                startSubActivity(bundle, v);
            }
        };

        View.OnClickListener groupClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // recupera il titolo della voce cliccata
                String idGruppo = ((TextView) v.findViewById(R.id.text_id_gruppo))
                        .getText().toString();
                int gruppi = myItemAdapter.getGroupCount();
                for (int i = 0; i < gruppi; i++) {
                    if (myItemAdapter.getGroupId(i) == Integer.valueOf(idGruppo)) {
                        if (mRecyclerViewExpandableItemManager.isGroupExpanded(i))
                            mRecyclerViewExpandableItemManager.collapseGroup(i);
                        else
                            mRecyclerViewExpandableItemManager.expandGroup(i);
                    }
                    else
                        mRecyclerViewExpandableItemManager.collapseGroup(i);
                }
            }
        };

        arguments.close();

        //noinspection ConstantConditions
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());

        final Parcelable eimSavedState = (savedInstanceState != null) ? savedInstanceState.getParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER) : null;
        mRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(eimSavedState);

        //adapter
        myItemAdapter = new CantoExpandableAdapter(getActivity(), dataItems, clickListener, ArgumentsSectionFragment.this, groupClickListener);

//        mAdapter = myItemAdapter;

        mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(myItemAdapter);       // wrap for expanding

        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();

        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Need to disable them when using animation indicator.
        animator.setSupportsChangeAnimations(false);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(animator);
        mRecyclerView.setHasFixedSize(false);

        // additional decorations
        //noinspection StatementWithEmptyBody
//        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
//            // Lollipop or later has native drop shadow feature. ItemShadowDecorator is not required.
//        } else {
//            mRecyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z1)));
//        }
//        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(getResources().getDrawable(R.drawable.list_divider), true));

        mRecyclerViewExpandableItemManager.attachRecyclerView(mRecyclerView);

        // fa in modo che la visuale scolli al gruppo cliccato
//        expList.setOnGroupClickListener(new OnGroupClickListener() {
//
//            @Override
//            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition,
//                                        long id) {
//
//                parent.smoothScrollToPosition(groupPosition);
//
//                if (parent.isGroupExpanded(groupPosition)) {
//                    parent.collapseGroup(groupPosition);
//                } else {
//                    parent.expandGroup(groupPosition);
//                }
//
//                return true;
//            }
//        });
//
//        expList.setOnGroupExpandListener(new OnGroupExpandListener() {
//            @Override
//            public void onGroupExpand(int arg0) {
//                if(arg0 != lastExpandedGroupPosition){
//                    expList.collapseGroup(lastExpandedGroupPosition);
//                }
//                lastExpandedGroupPosition = arg0;
//            }
//        });

//        // setta l'azione al click su ogni voce dell'elenco
//        expList.setOnChildClickListener(new OnChildClickListener() {
//
//            @Override
//            public boolean onChildClick(ExpandableListView parent, View v,
//                                        int groupPosition, int childPosition, long id) {
//                TextView exptv = (TextView)v.findViewById(R.id.text_title);
//
//                String cantoCliccato = exptv.getText().toString();
//                cantoCliccato = Utility.duplicaApostrofi(cantoCliccato);
//
//                // crea un manipolatore per il Database in modalità READ
//                SQLiteDatabase db = listaCanti.getReadableDatabase();
//
//                // esegue la query per il recupero del nome del file della pagina da visualizzare
//                String query = "SELECT source, _id" +
//                        "  FROM ELENCO" +
//                        "  WHERE titolo =  '" + cantoCliccato + "'";
//                Cursor cursor = db.rawQuery(query, null);
//
//                // recupera il nome del file
//                cursor.moveToFirst();
//                String pagina = cursor.getString(0);
//                int idCanto = cursor.getInt(1);
//
//                // chiude il cursore
//                cursor.close();
//                db.close();
//
//                // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da visualizzare
//                Bundle bundle = new Bundle();
//                bundle.putString("pagina", pagina);
//                bundle.putInt("idCanto", idCanto);
//
//                // lancia l'activity che visualizza il canto passando il parametro creato
//                startSubActivity(bundle, v);
//
//                return false;
//            }
//
//        });

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

//        registerForContextMenu(expList);

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
//        mAdapter = null;
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

//    private class SongRowAdapter extends SimpleExpandableListAdapter {
//
//        SongRowAdapter() {
//            super(	getActivity(),
//                    groupData,
//                    R.layout.simple_expandable_list_item_1,
//                    new String[] { NAME },
//                    new int[] { android.R.id.text1 },
//                    childData,
//                    R.layout.row_item,
//                    new String[] { NAME },
//                    new int[] { R.id.text_title }
//            );
//        }
//
//        @Override
//        public View getChildView(int groupPosition, int childPosition,
//                                 boolean isLastChild, View convertView, ViewGroup parent) {
//
//            View row = super.getChildView(groupPosition, childPosition,
//                    isLastChild, convertView, parent);
//            TextView canto = (TextView) row.findViewById(R.id.text_title);
//
//            String totalString = canto.getText().toString();
//            int tempPagina = Integer.valueOf(totalString.substring(0,3));
//            String pagina = String.valueOf(tempPagina);
//            String colore = totalString.substring(3, 10);
//
//            canto.setText(totalString.substring(10));
//
//            TextView textPage = (TextView) row.findViewById(R.id.text_page);
//            textPage.setText(pagina);
////            row.findViewById(R.id.full_row).setBackgroundColor(Color.parseColor(colore));
//            if (colore.equalsIgnoreCase(Utility.GIALLO))
//                textPage.setBackgroundResource(R.drawable.bkg_round_yellow);
//            if (colore.equalsIgnoreCase(Utility.GRIGIO))
//                textPage.setBackgroundResource(R.drawable.bkg_round_grey);
//            if (colore.equalsIgnoreCase(Utility.VERDE))
//                textPage.setBackgroundResource(R.drawable.bkg_round_green);
//            if (colore.equalsIgnoreCase(Utility.AZZURRO))
//                textPage.setBackgroundResource(R.drawable.bkg_round_blue);
//            if (colore.equalsIgnoreCase(Utility.BIANCO))
//                textPage.setBackgroundResource(R.drawable.bkg_round_white);
//
//            return row;
//        }
//
//    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        ExpandableListView.ExpandableListContextMenuInfo info =
                (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
//        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
//        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD)  {
        titoloDaAgg = ((TextView) v.findViewById(R.id.text_title)).getText().toString();
        idDaAgg = Integer.valueOf(((TextView) v.findViewById(R.id.text_id_canto)).getText().toString());
        menu.setHeaderTitle("Aggiungi canto a:");

        for (int i = 0; i < idListe.length; i++) {
            SubMenu subMenu = menu.addSubMenu(ID_FITTIZIO, Menu.NONE, 10+i, listePers[i].getName());
            for (int k = 0; k < listePers[i].getNumPosizioni(); k++) {
                subMenu.add(ID_BASE + i, k, k, listePers[i].getNomePosizione(k));
            }
        }

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.add_to, menu);

        SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(getActivity());
        menu.findItem(R.id.add_to_p_pace).setVisible(pref.getBoolean(Utility.SHOW_PACE, false));
        menu.findItem(R.id.add_to_e_seconda).setVisible(pref.getBoolean(Utility.SHOW_SECONDA, false));
        menu.findItem(R.id.add_to_e_santo).setVisible(pref.getBoolean(Utility.SHOW_SANTO, false));
//        }
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

                        //recupero ID del canto cliccato
//                        String cantoCliccatoNoApex = Utility.duplicaApostrofi(titoloDaAgg);
//                        SQLiteDatabase db = listaCanti.getReadableDatabase();
//                        String query = "SELECT _id" +
//                                "		FROM ELENCO" +
//                                "		WHERE titolo = '" + cantoCliccatoNoApex + "'";
//                        Cursor cursor = db.rawQuery(query, null);
//                        cursor.moveToFirst();
//                        idDaAgg = cursor.getInt(0);
//                        cursor.close();

                        SQLiteDatabase db = listaCanti.getReadableDatabase();

                        if (listePers[idListaClick]
                                .getCantoPosizione(idPosizioneClick).equals("")) {
                            listePers[idListaClick].addCanto(String.valueOf(idDaAgg), idPosizioneClick);
                            ContentValues  values = new  ContentValues( );
                            values.put("lista" , ListaPersonalizzata.serializeObject(listePers[idListaClick]));
                            db.update("LISTE_PERS", values, "_id = " + idListe[idListaClick], null );

                            Toast.makeText(getActivity()
                                    , getString(R.string.list_added), Toast.LENGTH_SHORT).show();
                        }
                        else {
//                            if (listePers[idListaClick].getCantoPosizione(idPosizioneClick).substring(10)
//                                    .equalsIgnoreCase(titoloDaAgg)) {
                            if (listePers[idListaClick].getCantoPosizione(idPosizioneClick).equals(String.valueOf(idDaAgg))) {
                                Toast.makeText(getActivity()
                                        , getString(R.string.present_yet), Toast.LENGTH_SHORT).show();
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
                                        .callback(new MaterialDialog.ButtonCallback() {
                                            @Override
                                            public void onPositive(MaterialDialog dialog) {
                                                SQLiteDatabase db = listaCanti.getReadableDatabase();
                                                listePers[idListaClick].addCanto(String.valueOf(idDaAgg), idPosizioneClick);

                                                ContentValues  values = new  ContentValues( );
                                                values.put("lista", ListaPersonalizzata.serializeObject(listePers[idListaClick]));
                                                db.update("LISTE_PERS", values, "_id = " + idListe[idListaClick], null);
                                                db.close();
                                                getActivity().setRequestedOrientation(prevOrientation);
                                                Toast.makeText(getActivity()
                                                        , getString(R.string.list_added), Toast.LENGTH_SHORT).show();
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

//        String titoloNoApex = Utility.duplicaApostrofi(titolo);

        String sql = "UPDATE ELENCO" +
                "  SET favourite = 1" +
//                "  WHERE titolo =  \'" + titoloNoApex + "\'";
                "  WHERE _id =  " + idDaAgg;
        db.execSQL(sql);
        db.close();

        Toast toast = Toast.makeText(getActivity()
                , getString(R.string.favorite_added), Toast.LENGTH_SHORT);
        toast.show();

    }

    //aggiunge il canto premuto ad una lista e in una posizione che ammetta duplicati
    public void addToListaDup(int idLista, int listPosition) {

//        String titoloNoApex = Utility.duplicaApostrofi(titolo);

        SQLiteDatabase db = listaCanti.getReadableDatabase();

        String sql = "INSERT INTO CUST_LISTS ";
        sql+= "VALUES (" + idLista + ", "
                + listPosition + ", "
//                + "(SELECT _id FROM ELENCO"
//                + " WHERE titolo = \'" + titoloNoApex + "\')"
                + idDaAgg
                + ", CURRENT_TIMESTAMP)";

        try {
            db.execSQL(sql);
            Toast.makeText(getActivity()
                    , getString(R.string.list_added), Toast.LENGTH_SHORT).show();
        } catch (SQLException e) {
            Toast toast = Toast.makeText(getActivity()
                    , getString(R.string.present_yet), Toast.LENGTH_SHORT);
            toast.show();
        }

        db.close();
    }

    //aggiunge il canto premuto ad una lista e in una posizione che NON ammetta duplicati
    public void addToListaNoDup(int idLista, int listPosition) {

//        String titoloNoApex = Utility.duplicaApostrofi(titolo);

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
                Toast toast = Toast.makeText(getActivity()
                        , getString(R.string.present_yet), Toast.LENGTH_SHORT);
                toast.show();
            }
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
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                SQLiteDatabase db = listaCanti.getReadableDatabase();
                                String cantoCliccatoNoApex = Utility.duplicaApostrofi(titoloDaAgg);
                                String sql = "UPDATE CUST_LISTS "
//                                        + "SET id_canto = (SELECT _id  FROM ELENCO"
//                                        + " WHERE titolo = \'" + cantoCliccatoNoApex + "\')"
                                        + " SET id_canto = " + idDaAgg
                                        + "WHERE _id = " + idListaDaAgg
                                        + "  AND position = " + posizioneDaAgg;
                                db.execSQL(sql);
                                db.close();
                                getActivity().setRequestedOrientation(prevOrientation);
                                Toast.makeText(getActivity()
                                        , getString(R.string.list_added), Toast.LENGTH_SHORT).show();
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
            return;
        }

        lista.close();

        String sql = "INSERT INTO CUST_LISTS "
                + "VALUES (" + idLista + ", "
                + listPosition + ", "
//                + "(SELECT _id FROM ELENCO"
//                + " WHERE titolo = \'" + titoloNoApex + "\')"
                + idDaAgg
                + ", CURRENT_TIMESTAMP)";
        db.execSQL(sql);
        db.close();

        Toast.makeText(getActivity()
                , getString(R.string.list_added), Toast.LENGTH_SHORT).show();
    }

}
