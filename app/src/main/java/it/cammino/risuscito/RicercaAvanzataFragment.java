package it.cammino.risuscito;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.internal.widget.TintEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.Toast;

import com.alertdialogpro.AlertDialogPro;
import com.alertdialogpro.material.ProgressBarCompat;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import it.cammino.risuscito.utils.ThemeUtils;
import it.cammino.utilities.material.PaperButton;

public class RicercaAvanzataFragment extends Fragment implements View.OnCreateContextMenuListener {

    private DatabaseCanti listaCanti;
    private List<CantoItem> titoli;
    private TintEditText searchPar;
    private View rootView;
    private static String[][] aTexts;
    RecyclerView recyclerView;
    CantoRecyclerAdapter cantoAdapter;
    private ProgressBarCompat progress;
    private int prevOrientation;
    private static Map<Character, Character> MAP_NORM;

    private String titoloDaAgg;
    private int idDaAgg;
    private int idListaDaAgg;
    private int posizioneDaAgg;
    private ListaPersonalizzata[] listePers;
    private int[] idListe;
    private int idListaClick;
    private int idPosizioneClick;

    private final int ID_FITTIZIO = 99999999;
    private final int ID_BASE = 100;

    private LUtils mLUtils;

    private SearchTask searchTask ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(
                R.layout.activity_ricerca_avanzata, container, false);

        searchPar = (TintEditText) rootView.findViewById(R.id.textfieldRicerca);
        listaCanti = new DatabaseCanti(getActivity());

        recyclerView = (RecyclerView) rootView.findViewById(R.id.matchedList);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cantoCliccato = ((TextView) v.findViewById(R.id.text_title))
                        .getText().toString();
                cantoCliccato = Utility.duplicaApostrofi(cantoCliccato);

                // crea un manipolatore per il DB in modalità READ
                SQLiteDatabase db = listaCanti.getReadableDatabase();

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

                // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da visualizzare
                Bundle bundle = new Bundle();
                bundle.putString("pagina", pagina);
                bundle.putInt("idCanto", idCanto);

                // lancia l'activity che visualizza il canto passando il parametro creato
                startSubActivity(bundle, v);
            }
        };

        // Creating new adapter object
        titoli = new ArrayList<CantoItem>();
        cantoAdapter = new CantoRecyclerAdapter(titoli, clickListener, this);
        recyclerView.setAdapter(cantoAdapter);

        // Setting the layoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        progress = (ProgressBarCompat) rootView.findViewById(R.id.search_progress);
        searchPar.setText("");

        try {
            InputStream in = getActivity().getAssets().open("fileout_new.xml");
            if (getActivity().getResources().getConfiguration().locale.getLanguage().equalsIgnoreCase("uk"))
                in = getActivity().getAssets().open("fileout_uk.xml");
            CantiXmlParser parser = new CantiXmlParser();
            aTexts = parser.parse(in);
            in.close();
        } 	catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        searchPar.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String tempText = ((EditText) getActivity().findViewById(R.id.tempTextField)).getText().toString();
                if (!tempText.equals(s.toString()))
                    ((EditText) getActivity().findViewById(R.id.tempTextField)).setText(s);

                //abilita il pulsante solo se la stringa ha più di 3 caratteri, senza contare gli spazi
                if (s.toString().trim().length() >= 3) {
                    if (searchTask != null && searchTask.getStatus() == Status.RUNNING)
                        searchTask.cancel(true);
                    searchTask = new SearchTask();
                    searchTask.execute(searchPar.getText().toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(Editable s) { }

        });

        searchPar.setOnEditorActionListener(new TintEditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //to hide soft keyboard
                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(searchPar.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        ((EditText) getActivity().findViewById(R.id.tempTextField)).addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                String tempText = searchPar.getText().toString();
                if (!tempText.equals(s.toString()))
                    searchPar.setText(s);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });

        PaperButton paperPulisci = (PaperButton) rootView.findViewById(R.id.pulisci_ripple);
        paperPulisci.setColor(getThemeUtils().primaryColor());
        paperPulisci.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchPar.setText("");
                rootView.findViewById(R.id.search_no_results).setVisibility(View.GONE);
                titoli.clear();
                cantoAdapter.notifyDataSetChanged();
                progress.setVisibility(View.GONE);
            }
        });

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

        mLUtils = LUtils.getInstance(getActivity());

        return rootView;
    }

    @Override
    public void onDestroy() {
        if (searchTask != null && searchTask.getStatus() == Status.RUNNING)
            searchTask.cancel(true);
        if (listaCanti != null)
            listaCanti.close();
        super.onDestroy();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        titoloDaAgg = ((TextView) v.findViewById(R.id.text_title)).getText().toString();
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
                case R.id.add_to_e_santo:
                    addToListaNoDup(2, 7, titoloDaAgg);
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

                        //recupero ID del canto cliccato
                        String cantoCliccatoNoApex = Utility.duplicaApostrofi(titoloDaAgg);
                        SQLiteDatabase db = listaCanti.getReadableDatabase();
                        String query = "SELECT _id" +
                                "		FROM ELENCO" +
                                "		WHERE titolo = '" + cantoCliccatoNoApex + "'";
                        Cursor cursor = db.rawQuery(query, null);
                        cursor.moveToFirst();
                        idDaAgg = cursor.getInt(0);
                        cursor.close();

                        if (listePers[idListaClick]
                                .getCantoPosizione(idPosizioneClick).equals("")) {
//                            String cantoCliccatoNoApex = Utility.duplicaApostrofi(titoloDaAgg);
//                            SQLiteDatabase db = listaCanti.getReadableDatabase();
//
//                            String query = "SELECT color, pagina" +
//                                    "		FROM ELENCO" +
//                                    "		WHERE titolo = '" + cantoCliccatoNoApex + "'";
//                            Cursor cursor = db.rawQuery(query, null);
//
//                            cursor.moveToFirst();
//
//                            listePers[idListaClick].addCanto(Utility.intToString(
//                                    cursor.getInt(1), 3) + cursor.getString(0) + titoloDaAgg, idPosizioneClick);
//                            cursor.close();

                            listePers[idListaClick].addCanto(String.valueOf(idDaAgg), idPosizioneClick);
                            ContentValues  values = new  ContentValues( );
                            values.put("lista" , ListaPersonalizzata.serializeObject(listePers[idListaClick]));
                            db.update("LISTE_PERS", values, "_id = " + idListe[idListaClick], null );
                            db.close();

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
                                query = "SELECT titolo" +
                                        "		FROM ELENCO" +
                                        "		WHERE _id = "
                                        + listePers[idListaClick].getCantoPosizione(idPosizioneClick);
                                cursor = db.rawQuery(query, null);
                                cursor.moveToFirst();
                                AlertDialogPro.Builder builder = new AlertDialogPro.Builder(getActivity());
                                AlertDialogPro dialog = builder.setTitle(R.string.dialog_replace_title)
                                        .setMessage(getString(R.string.dialog_present_yet) + " "
//                                                + listePers[idListaClick].getCantoPosizione(idPosizioneClick)
//                                                .substring(10)
                                                + cursor.getString(0)
                                                + getString(R.string.dialog_wonna_replace))
                                        .setPositiveButton(R.string.confirm, new ButtonClickedListener(Utility.AVANZATA_LISTAPERS_OK))
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
                                cursor.close();
                                db.close();
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
                        .setPositiveButton(R.string.confirm, new ButtonClickedListener(Utility.AVANZATA_LISTAPRED_OK))
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
                case Utility.AVANZATA_LISTAPERS_OK:
                    SQLiteDatabase db = listaCanti.getReadableDatabase();
//                    String cantoCliccatoNoApex = Utility.duplicaApostrofi(titoloDaAgg);
//                    String query = "SELECT color, pagina" +
//                            "		FROM ELENCO" +
//                            "		WHERE titolo = '" + cantoCliccatoNoApex + "'";
//                    Cursor cursor = db.rawQuery(query, null);
//
//                    cursor.moveToFirst();
//
//                    listePers[idListaClick].addCanto(Utility.intToString(
//                            cursor.getInt(1), 3) + cursor.getString(0) + titoloDaAgg, idPosizioneClick);
                    listePers[idListaClick].addCanto(String.valueOf(idDaAgg), idPosizioneClick);

                    ContentValues  values = new  ContentValues( );
                    values.put("lista" , ListaPersonalizzata.serializeObject(listePers[idListaClick]));
                    db.update("LISTE_PERS", values, "_id = " + idListe[idListaClick], null );
                    getActivity().setRequestedOrientation(prevOrientation);
                    Toast.makeText(getActivity()
                            , getString(R.string.list_added), Toast.LENGTH_SHORT).show();
                    break;
                case Utility.AVANZATA_LISTAPRED_OK:
                    db = listaCanti.getReadableDatabase();
                    String cantoCliccatoNoApex = Utility.duplicaApostrofi(titoloDaAgg);
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

    private void startSubActivity(Bundle bundle, View view) {
        Intent intent = new Intent(getActivity().getApplicationContext(), PaginaRenderActivity.class);
        intent.putExtras(bundle);
        mLUtils.startActivityWithTransition(intent, view, Utility.TRANS_PAGINA_RENDER);
    }

    private class SearchTask extends AsyncTask<String, Integer, String> {

        @SuppressLint("NewApi")
        @Override
        protected String doInBackground(String... sSearchText) {

            // crea un manipolatore per il Database in modalità READ
            SQLiteDatabase db = listaCanti.getReadableDatabase();

            String[] words = sSearchText[0].split("\\W");

//			for (int j = 0; j < words.length; j++)
//				if (words[j].trim().length() > 2)
//					Log.i("PAROLA[" + j + "]:", words[j].trim());

            String text = "";
            String[] aResults = new String[300];
            int totalResults = 0;

            for (int k = 0; k < aTexts.length; k++) {

                if (aTexts[k][0] == null || aTexts[k][0].equalsIgnoreCase(""))
                    break;

                boolean found = true;
                for (int j = 0; j < words.length; j++) {
                    if (words[j].trim().length() > 1) {
                        text = words[j].trim();
                        text = text.toLowerCase(getActivity().getResources().getConfiguration().locale);

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
                            String nfdNormalizedString = Normalizer.normalize(text, Normalizer.Form.NFD);
                            Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
                            text =  pattern.matcher(nfdNormalizedString).replaceAll("");
                        }
                        else
                            text = removeAccents(text);

                        if (!aTexts[k][1].contains(text)) {
                            found = false;
                        }
                    }
                }

                if (found) {

                    // recupera il titolo colore e pagina del canto da aggiungere alla lista
                    String query = "SELECT titolo, color, pagina"
                            +		"		FROM ELENCO"
                            +		"		WHERE source = '" + aTexts[k][0] + "'";

                    Cursor lista = db.rawQuery(query, null);

                    if (lista.getCount() > 0) {
                        lista.moveToFirst();
//		    			Log.i("TROVATO IN", aTexts[k][0]);
//		    			Log.i("LUNGHEZZA", aResults.length+"");
                        aResults[totalResults++] = Utility.intToString(lista.getInt(2), 3) + lista.getString(1) + lista.getString(0);
                    }
                    // chiude il cursore
                    lista.close();
//					Log.i("TROVATO DOPO", aResults[totalResults - 1]);
                }
            }

            titoli.clear();
            for (int i = 0; i < aResults.length; i++) {
                if (aResults[i] == null)
                    break;
                titoli.add(new CantoItem(aResults[i]));
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            rootView.findViewById(R.id.search_no_results).setVisibility(View.GONE);
//            lv.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String result) {

            cantoAdapter.notifyDataSetChanged();
            progress.setVisibility(View.GONE);

            if (titoli.size() == 0) {
                rootView.findViewById(R.id.search_no_results).setVisibility(View.VISIBLE);
            }
            else {
                rootView.findViewById(R.id.search_no_results).setVisibility(View.GONE);
            }
        }

    }

    public static String removeAccents(String value)
    {
        if (MAP_NORM == null || MAP_NORM.size() == 0)
        {
            MAP_NORM = new HashMap<Character, Character>();
            MAP_NORM.put('À', 'A');
            MAP_NORM.put('Á', 'A');
            MAP_NORM.put('Â', 'A');
            MAP_NORM.put('Ã', 'A');
            MAP_NORM.put('Ä', 'A');
            MAP_NORM.put('È', 'E');
            MAP_NORM.put('É', 'E');
            MAP_NORM.put('Ê', 'E');
            MAP_NORM.put('Ë', 'E');
            MAP_NORM.put('Í', 'I');
            MAP_NORM.put('Ì', 'I');
            MAP_NORM.put('Î', 'I');
            MAP_NORM.put('Ï', 'I');
            MAP_NORM.put('Ù', 'U');
            MAP_NORM.put('Ú', 'U');
            MAP_NORM.put('Û', 'U');
            MAP_NORM.put('Ü', 'U');
            MAP_NORM.put('Ò', 'O');
            MAP_NORM.put('Ó', 'O');
            MAP_NORM.put('Ô', 'O');
            MAP_NORM.put('Õ', 'O');
            MAP_NORM.put('Ö', 'O');
            MAP_NORM.put('Ñ', 'N');
            MAP_NORM.put('Ç', 'C');
            MAP_NORM.put('ª', 'A');
            MAP_NORM.put('º', 'O');
            MAP_NORM.put('§', 'S');
            MAP_NORM.put('³', '3');
            MAP_NORM.put('²', '2');
            MAP_NORM.put('¹', '1');
            MAP_NORM.put('à', 'a');
            MAP_NORM.put('á', 'a');
            MAP_NORM.put('â', 'a');
            MAP_NORM.put('ã', 'a');
            MAP_NORM.put('ä', 'a');
            MAP_NORM.put('è', 'e');
            MAP_NORM.put('é', 'e');
            MAP_NORM.put('ê', 'e');
            MAP_NORM.put('ë', 'e');
            MAP_NORM.put('í', 'i');
            MAP_NORM.put('ì', 'i');
            MAP_NORM.put('î', 'i');
            MAP_NORM.put('ï', 'i');
            MAP_NORM.put('ù', 'u');
            MAP_NORM.put('ú', 'u');
            MAP_NORM.put('û', 'u');
            MAP_NORM.put('ü', 'u');
            MAP_NORM.put('ò', 'o');
            MAP_NORM.put('ó', 'o');
            MAP_NORM.put('ô', 'o');
            MAP_NORM.put('õ', 'o');
            MAP_NORM.put('ö', 'o');
            MAP_NORM.put('ñ', 'n');
            MAP_NORM.put('ç', 'c');
        }

        if (value == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder(value);

        for(int i = 0; i < value.length(); i++) {
            Character c = MAP_NORM.get(sb.charAt(i));
            if(c != null) {
                sb.setCharAt(i, c.charValue());
            }
        }

        return sb.toString();
    }

    private ThemeUtils getThemeUtils() {
        return ((MainActivity)getActivity()).getThemeUtils();
    }

}
