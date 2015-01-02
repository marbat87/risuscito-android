package it.cammino.risuscito;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
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
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alertdialogpro.AlertDialogPro;

import it.cammino.utilities.quickscroll.QuickScroll;
import it.cammino.utilities.quickscroll.Scrollable;

public class NumericSectionFragment extends Fragment {

    private String[] titoli;
    private DatabaseCanti listaCanti;
    private SQLiteDatabase db;
    private String titoloDaAgg;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_alphanum_index, container, false);

        ListView lv = (ListView) rootView.findViewById(R.id.cantiList);

        //crea un istanza dell'oggetto DatabaseCanti
        listaCanti = new DatabaseCanti(getActivity());

        // crea un manipolatore per il Database in modalit� READ
        db = listaCanti.getReadableDatabase();

        // lancia la ricerca di tutti i titoli presenti in DB e li dispone in ordine alfabetico
        String query = "SELECT titolo, color, pagina" +
                "		FROM ELENCO" +
                "		ORDER BY pagina ASC, titolo ASC";
        Cursor lista = db.rawQuery(query, null);

        //recupera il numero di record trovati
        int total = lista.getCount();

        // crea un array e ci memorizza i titoli estratti
        titoli = new String[lista.getCount()];
        lista.moveToFirst();
        for (int i = 0; i < total; i++) {
            titoli[i] = Utility.intToString(lista.getInt(2), 3) + lista.getString(1) + lista.getString(0);
            lista.moveToNext();
        }

        // chiude il cursore
        lista.close();

        // crea un list adapter per l'oggetto di tipo ListView
        SongRowAdapter adapter = new SongRowAdapter();
        lv.setAdapter(adapter);


        // setta l'azione al click su ogni voce dell'elenco
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // recupera il titolo della voce cliccata troncando il numero pagina iniziale
                String cantoCliccato = ((TextView) view.findViewById(R.id.text_title))
                        .getText().toString();
                cantoCliccato = Utility.duplicaApostrofi(cantoCliccato);

                // crea un manipolatore per il Database in modalit� READ
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

                // lancia l'activity che visualizza il canto passando il parametro creato
                startSubActivity(bundle, view);

            }
        });

        final QuickScroll quickscroll = (QuickScroll) rootView.findViewById(R.id.quickscroll);
        quickscroll.init(QuickScroll.TYPE_INDICATOR_WITH_HANDLE, lv, adapter, QuickScroll.STYLE_HOLO);
        quickscroll.setHandlebarColor(getResources().getColor(R.color.theme_accent)
                , getResources().getColor(R.color.theme_accent)
                , getResources().getColor(R.color.theme_accent_semi_tr));
        quickscroll.setFixedSize(2);
        quickscroll.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 35);

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

        registerForContextMenu(lv);

        mLUtils = LUtils.getInstance(getActivity());

        return rootView;
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

    private class SongRowAdapter extends ArrayAdapter<String> implements Scrollable{

        SongRowAdapter() {
            super(getActivity(), R.layout.row_item, R.id.text_title, titoli);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row=super.getView(position, convertView, parent);
            TextView canto = (TextView) row.findViewById(R.id.text_title);

            String totalString = canto.getText().toString();
            int tempPagina = Integer.valueOf(totalString.substring(0,3));
            String pagina = String.valueOf(tempPagina);
            String colore = totalString.substring(3, 10);

            canto.setText(totalString.substring(10));

            TextView textPage = (TextView) row.findViewById(R.id.text_page);
            textPage.setText(pagina);
            row.findViewById(R.id.full_row).setBackgroundColor(Color.parseColor(colore));

            return(row);
        }

        @Override
        public String getIndicatorForPosition(int childposition, int groupposition) {
            int pagina = Integer.valueOf(titoli[childposition].substring(0,3));
            return String.valueOf(pagina);
        }

        @Override
        public int getScrollPosition(int childposition, int groupposition) {
            return childposition;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        titoloDaAgg = ((TextView) info.targetView.findViewById(R.id.text_title)).getText().toString();
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
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (getUserVisibleHint()) {
            switch (item.getItemId()) {
                case R.id.add_to_favorites:
                    addToFavorites(titoloDaAgg);
                    return true;
                case R.id.add_to_p_iniziale:
                    addToListaNoDup(1, 1, titoloDaAgg);
                    return true;
                case R.id.add_to_p_prima:
                    addToListaNoDup(1, 2, titoloDaAgg);
                    return true;
                case R.id.add_to_p_seconda:
                    addToListaNoDup(1, 3, titoloDaAgg);
                    return true;
                case R.id.add_to_p_terza:
                    addToListaNoDup(1, 4, titoloDaAgg);
                    return true;
                case R.id.add_to_p_pace:
                    addToListaNoDup(1, 6, titoloDaAgg);
                    return true;
                case R.id.add_to_p_fine:
                    addToListaNoDup(1, 5, titoloDaAgg);
                    return true;
                case R.id.add_to_e_iniziale:
                    addToListaNoDup(2, 1, titoloDaAgg);
                    return true;
                case R.id.add_to_e_seconda:
                    addToListaNoDup(2, 6, titoloDaAgg);
                    return true;
                case R.id.add_to_e_pace:
                    addToListaNoDup(2, 2, titoloDaAgg);
                    return true;
                case R.id.add_to_e_pane:
                    addToListaDup(2, 3, titoloDaAgg);
                    return true;
                case R.id.add_to_e_vino:
                    addToListaDup(2, 4, titoloDaAgg);
                    return true;
                case R.id.add_to_e_fine:
                    addToListaNoDup(2, 5, titoloDaAgg);
                    return true;
                default:
                    idListaClick = item.getGroupId();
                    idPosizioneClick = item.getItemId();
                    if (idListaClick != ID_FITTIZIO && idListaClick >= 100) {
                        idListaClick -= 100;
                        if (listePers[idListaClick]
                                .getCantoPosizione(idPosizioneClick).equalsIgnoreCase("")) {
                            String cantoCliccatoNoApex = Utility.duplicaApostrofi(titoloDaAgg);
                            SQLiteDatabase db = listaCanti.getReadableDatabase();

                            String query = "SELECT color, pagina" +
                                    "		FROM ELENCO" +
                                    "		WHERE titolo = '" + cantoCliccatoNoApex + "'";
                            Cursor cursor = db.rawQuery(query, null);

                            cursor.moveToFirst();

                            listePers[idListaClick].addCanto(Utility.intToString(
                                    cursor.getInt(1), 3) + cursor.getString(0) + titoloDaAgg, idPosizioneClick);

                            ContentValues  values = new  ContentValues( );
                            values.put("lista" , ListaPersonalizzata.serializeObject(listePers[idListaClick]));
                            db.update("LISTE_PERS", values, "_id = " + idListe[idListaClick], null );
                            db.close();

                            Toast.makeText(getActivity()
                                    , getString(R.string.list_added), Toast.LENGTH_SHORT).show();
                        }
                        else {
                            if (listePers[idListaClick].getCantoPosizione(idPosizioneClick).substring(10)
                                    .equalsIgnoreCase(titoloDaAgg)) {
                                Toast toast = Toast.makeText(getActivity()
                                        , getString(R.string.present_yet), Toast.LENGTH_SHORT);
                                toast.show();
                            }
                            else {
                                prevOrientation = getActivity().getRequestedOrientation();
                                Utility.blockOrientation(getActivity());
                                AlertDialogPro.Builder builder = new AlertDialogPro.Builder(getActivity());
                                AlertDialogPro dialog = builder.setTitle(R.string.dialog_replace_title)
                                        .setMessage(getString(R.string.dialog_present_yet) + " "
                                                + listePers[idListaClick].getCantoPosizione(idPosizioneClick)
                                                .substring(10)
                                                + getString(R.string.dialog_wonna_replace))
                                        .setPositiveButton(R.string.confirm, new ButtonClickedListener(Utility.NUM_LISTAPERS_OK))
                                        .setNegativeButton(R.string.dismiss, new ButtonClickedListener(Utility.DISMISS))
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
                        }
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
    public void addToFavorites(String titolo) {

        SQLiteDatabase db = listaCanti.getReadableDatabase();

        String titoloNoApex = Utility.duplicaApostrofi(titolo);

        String sql = "UPDATE ELENCO" +
                "  SET favourite = 1" +
                "  WHERE titolo =  \'" + titoloNoApex + "\'";
        db.execSQL(sql);
        db.close();

        Toast toast = Toast.makeText(getActivity()
                , getString(R.string.favorite_added), Toast.LENGTH_SHORT);
        toast.show();

        //permette di aggiornare il numero dei preferiti nel menu laterale
//		((MainActivity) getActivity()).onResume();

    }

    //aggiunge il canto premuto ad una lista e in una posizione che ammetta duplicati
    public void addToListaDup(int idLista, int listPosition, String titolo) {

        String titoloNoApex = Utility.duplicaApostrofi(titolo);

        SQLiteDatabase db = listaCanti.getReadableDatabase();

        String sql = "INSERT INTO CUST_LISTS ";
        sql+= "VALUES (" + idLista + ", "
                + listPosition + ", "
                + "(SELECT _id FROM ELENCO"
                + " WHERE titolo = \'" + titoloNoApex + "\')"
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
    public void addToListaNoDup(int idLista, int listPosition, String titolo) {

        String titoloNoApex = Utility.duplicaApostrofi(titolo);

        SQLiteDatabase db = listaCanti.getReadableDatabase();

        // cerca se la posizione nella lista � gi� occupata
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

            if (titolo.equalsIgnoreCase(titoloPresente)) {
                Toast toast = Toast.makeText(getActivity()
                        , getString(R.string.present_yet), Toast.LENGTH_SHORT);
                toast.show();
            }
            else {
                idListaDaAgg = idLista;
                posizioneDaAgg = listPosition;

                prevOrientation = getActivity().getRequestedOrientation();
                Utility.blockOrientation(getActivity());
                AlertDialogPro.Builder builder = new AlertDialogPro.Builder(getActivity());
                AlertDialogPro dialog = builder.setTitle(R.string.dialog_replace_title)
                        .setMessage(getString(R.string.dialog_present_yet) + " " + titoloPresente
                                + getString(R.string.dialog_wonna_replace))
                        .setPositiveButton(R.string.confirm, new ButtonClickedListener(Utility.NUM_LISTAPRED_OK))
                        .setNegativeButton(R.string.dismiss, new ButtonClickedListener(Utility.DISMISS))
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
                + "(SELECT _id FROM ELENCO"
                + " WHERE titolo = \'" + titoloNoApex + "\')"
                + ", CURRENT_TIMESTAMP)";
        db.execSQL(sql);
        db.close();

        Toast.makeText(getActivity()
                , getString(R.string.list_added), Toast.LENGTH_SHORT).show();
    }

    private class ButtonClickedListener implements DialogInterface.OnClickListener {
        private int clickedCode;

        public ButtonClickedListener(int code) {
            clickedCode = code;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (clickedCode) {
                case Utility.DISMISS:
                    getActivity().setRequestedOrientation(prevOrientation);
                    break;
                case Utility.NUM_LISTAPERS_OK:
                    SQLiteDatabase db = listaCanti.getReadableDatabase();
                    String cantoCliccatoNoApex = Utility.duplicaApostrofi(titoloDaAgg);
                    String query = "SELECT color, pagina" +
                            "		FROM ELENCO" +
                            "		WHERE titolo = '" + cantoCliccatoNoApex + "'";
                    Cursor cursor = db.rawQuery(query, null);

                    cursor.moveToFirst();

                    listePers[idListaClick].addCanto(Utility.intToString(
                            cursor.getInt(1), 3) + cursor.getString(0) + titoloDaAgg, idPosizioneClick);

                    ContentValues  values = new  ContentValues( );
                    values.put("lista" , ListaPersonalizzata.serializeObject(listePers[idListaClick]));
                    db.update("LISTE_PERS", values, "_id = " + idListe[idListaClick], null );
                    getActivity().setRequestedOrientation(prevOrientation);
                    Toast.makeText(getActivity()
                            , getString(R.string.list_added), Toast.LENGTH_SHORT).show();
                    break;
                case Utility.NUM_LISTAPRED_OK:
                    db = listaCanti.getReadableDatabase();
                    cantoCliccatoNoApex = Utility.duplicaApostrofi(titoloDaAgg);
                    String sql = "UPDATE CUST_LISTS "
                            + "SET id_canto = (SELECT _id  FROM ELENCO"
                            + " WHERE titolo = \'" + cantoCliccatoNoApex + "\')"
                            + "WHERE _id = " + idListaDaAgg
                            + "  AND position = " + posizioneDaAgg;
                    db.execSQL(sql);
                    getActivity().setRequestedOrientation(prevOrientation);
                    Toast.makeText(getActivity()
                            , getString(R.string.list_added), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    getActivity().setRequestedOrientation(prevOrientation);
                    break;
            }
        }
    }
}