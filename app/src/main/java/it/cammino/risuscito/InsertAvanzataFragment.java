package it.cammino.risuscito;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.internal.MDTintHelper;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import it.cammino.risuscito.adapters.CantoInsertRecyclerAdapter;
import it.cammino.risuscito.objects.CantoInsert;
import it.cammino.risuscito.utils.ThemeUtils;
import me.zhanghai.android.materialprogressbar.IndeterminateProgressDrawable;

public class InsertAvanzataFragment extends Fragment {

    private DatabaseCanti listaCanti;
    private List<CantoInsert> titoli;
    private EditText searchPar;
    private View rootView;
    private static String[][] aTexts;
    RecyclerView recyclerView;
    CantoInsertRecyclerAdapter cantoAdapter;
    private ProgressBar progress;
//    private static Map<Character, Character> MAP_NORM;

    private int fromAdd;
    private int idLista;
    private int listPosition;

    private SearchTask searchTask;

    private LUtils mLUtils;

    private long mLastClickTime = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(
                R.layout.activity_ricerca_avanzata, container, false);

        searchPar = (EditText) rootView.findViewById(R.id.textfieldRicerca);
        listaCanti = new DatabaseCanti(getActivity());

        recyclerView = (RecyclerView) rootView.findViewById(R.id.matchedList);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
                    return;
                mLastClickTime = SystemClock.elapsedRealtime();

                SQLiteDatabase db = listaCanti.getReadableDatabase();

                String idCanto = ((TextView) v.findViewById(R.id.text_id_canto))
                        .getText().toString();

                if (fromAdd == 1)  {
                    // chiamato da una lista predefinita
                    String query = "INSERT INTO CUST_LISTS ";
                    query+= "VALUES (" + idLista + ", "
                            + listPosition + ", "
                            + idCanto
                            + ", CURRENT_TIMESTAMP)";
                    try {
                        db.execSQL(query);
                    } catch (SQLException e) {
                        Snackbar.make(rootView, R.string.present_yet, Snackbar.LENGTH_SHORT)
                                .show();
                    }
                }
                else {
                    //chiamato da una lista personalizzata
                    String query = "SELECT lista" +
                            "  FROM LISTE_PERS" +
                            "  WHERE _id =  " + idLista;
                    Cursor cursor = db.rawQuery(query, null);
                    // recupera l'oggetto lista personalizzata
                    cursor.moveToFirst();

                    ListaPersonalizzata listaPersonalizzata = (ListaPersonalizzata) ListaPersonalizzata.
                            deserializeObject(cursor.getBlob(0));

                    // chiude il cursore
                    cursor.close();

                    // lancia la ricerca di tutti i titoli presenti in DB e li dispone in ordine alfabetico
                    listaPersonalizzata.addCanto(String.valueOf(idCanto), listPosition);
                    cursor.close();

                    ContentValues  values = new  ContentValues( );
                    values.put("lista" , ListaPersonalizzata.serializeObject(listaPersonalizzata));
                    db.update("LISTE_PERS", values, "_id = " + idLista, null );
                    db.close();
                }

                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
                getActivity().overridePendingTransition(0, R.anim.slide_out_right);
            }
        };

        View.OnClickListener seeOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
                    return;
                mLastClickTime = SystemClock.elapsedRealtime();
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

        // Creating new adapter object
        titoli = new ArrayList<>();
        cantoAdapter = new CantoInsertRecyclerAdapter(getActivity(), titoli, clickListener, seeOnClickListener);
        recyclerView.setAdapter(cantoAdapter);

        // Setting the layoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        progress = (ProgressBar) rootView.findViewById(R.id.search_progress);
        if (LUtils.hasICS()) {
            IndeterminateProgressDrawable d = new IndeterminateProgressDrawable(getActivity());
            d.setTint(getThemeUtils().accentColor());
            progress.setProgressDrawable(d);
            progress.setIndeterminateDrawable(d);
        }
        else
            MDTintHelper.setTint(progress, getThemeUtils().accentColor());

        searchPar.setText("");

        Bundle bundle = getArguments();
        fromAdd = bundle.getInt("fromAdd");
        idLista = bundle.getInt("idLista");
        listPosition = bundle.getInt("position");

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
                else {
                    if (s.length() == 0) {
                        rootView.findViewById(R.id.search_no_results).setVisibility(View.GONE);
                        titoli.clear();
                        cantoAdapter.notifyDataSetChanged();
//                        progress.stop();
                        progress.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(Editable s) { }

        });

        searchPar.setOnEditorActionListener(new EditText.OnEditorActionListener() {
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(Editable s) { }

        });

        rootView.findViewById(R.id.pulisci_ripple).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchPar.setText("");
            }
        });

        mLUtils = LUtils.getInstance(getActivity());

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
            Log.d(getClass().getName(), "VISIBLE");
            //to hide soft keyboard
            if (searchPar != null)
                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(searchPar.getWindowToken(), 0);
        }
    }

    @Override
    public void onDestroy() {
        if (searchTask != null && searchTask.getStatus() == Status.RUNNING)
            searchTask.cancel(true);
        if (listaCanti != null)
            listaCanti.close();
        super.onDestroy();
    }

    private class SearchTask extends AsyncTask<String, Integer, String> {

        @SuppressLint("NewApi")
        @Override
        protected String doInBackground(String... sSearchText) {

            // crea un manipolatore per il Database in modalità READ
            SQLiteDatabase db = listaCanti.getReadableDatabase();

            String[] words = sSearchText[0].split("\\W");

            String text;
            titoli.clear();

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
                            text = Utility.removeAccents(text);

                        if (!aTexts[k][1].contains(text)) {
                            found = false;
                        }
                    }
                }

                if (found) {

                    // recupera il titolo colore e pagina del canto da aggiungere alla lista
                    String query = "SELECT titolo, color, pagina, _id, source"
                            +		"		FROM ELENCO"
                            +		"		WHERE source = '" + aTexts[k][0] + "'";

                    Cursor lista = db.rawQuery(query, null);

                    if (lista.getCount() > 0) {
                        lista.moveToFirst();
                        titoli.add(new CantoInsert(Utility.intToString(lista.getInt(2), 3) + lista.getString(1) + lista.getString(0)
                                                , lista.getInt(3)
                                                , lista.getString(4)));
                    }
                    // chiude il cursore
                    lista.close();
                }
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            rootView.findViewById(R.id.search_no_results).setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String result) {

            cantoAdapter.notifyDataSetChanged();

            progress.setVisibility(View.INVISIBLE);

            if (titoli.size() == 0) {
                rootView.findViewById(R.id.search_no_results).setVisibility(View.VISIBLE);
            }
            else {
                rootView.findViewById(R.id.search_no_results).setVisibility(View.GONE);
            }
        }

    }

//    public static String removeAccents(String value)
//    {
//        if (MAP_NORM == null || MAP_NORM.size() == 0)
//        {
//            MAP_NORM = new HashMap<>();
//            MAP_NORM.put('À', 'A');
//            MAP_NORM.put('Á', 'A');
//            MAP_NORM.put('Â', 'A');
//            MAP_NORM.put('Ã', 'A');
//            MAP_NORM.put('Ä', 'A');
//            MAP_NORM.put('È', 'E');
//            MAP_NORM.put('É', 'E');
//            MAP_NORM.put('Ê', 'E');
//            MAP_NORM.put('Ë', 'E');
//            MAP_NORM.put('Í', 'I');
//            MAP_NORM.put('Ì', 'I');
//            MAP_NORM.put('Î', 'I');
//            MAP_NORM.put('Ï', 'I');
//            MAP_NORM.put('Ù', 'U');
//            MAP_NORM.put('Ú', 'U');
//            MAP_NORM.put('Û', 'U');
//            MAP_NORM.put('Ü', 'U');
//            MAP_NORM.put('Ò', 'O');
//            MAP_NORM.put('Ó', 'O');
//            MAP_NORM.put('Ô', 'O');
//            MAP_NORM.put('Õ', 'O');
//            MAP_NORM.put('Ö', 'O');
//            MAP_NORM.put('Ñ', 'N');
//            MAP_NORM.put('Ç', 'C');
//            MAP_NORM.put('ª', 'A');
//            MAP_NORM.put('º', 'O');
//            MAP_NORM.put('§', 'S');
//            MAP_NORM.put('³', '3');
//            MAP_NORM.put('²', '2');
//            MAP_NORM.put('¹', '1');
//            MAP_NORM.put('à', 'a');
//            MAP_NORM.put('á', 'a');
//            MAP_NORM.put('â', 'a');
//            MAP_NORM.put('ã', 'a');
//            MAP_NORM.put('ä', 'a');
//            MAP_NORM.put('è', 'e');
//            MAP_NORM.put('é', 'e');
//            MAP_NORM.put('ê', 'e');
//            MAP_NORM.put('ë', 'e');
//            MAP_NORM.put('í', 'i');
//            MAP_NORM.put('ì', 'i');
//            MAP_NORM.put('î', 'i');
//            MAP_NORM.put('ï', 'i');
//            MAP_NORM.put('ù', 'u');
//            MAP_NORM.put('ú', 'u');
//            MAP_NORM.put('û', 'u');
//            MAP_NORM.put('ü', 'u');
//            MAP_NORM.put('ò', 'o');
//            MAP_NORM.put('ó', 'o');
//            MAP_NORM.put('ô', 'o');
//            MAP_NORM.put('õ', 'o');
//            MAP_NORM.put('ö', 'o');
//            MAP_NORM.put('ñ', 'n');
//            MAP_NORM.put('ç', 'c');
//        }
//
//        if (value == null) {
//            return "";
//        }
//
//        StringBuilder sb = new StringBuilder(value);
//
//        for(int i = 0; i < value.length(); i++) {
//            Character c = MAP_NORM.get(sb.charAt(i));
//            if(c != null)
//                sb.setCharAt(i, c);
//        }
//
//        return sb.toString();
//    }

    private void startSubActivity(Bundle bundle, View view) {
        Intent intent = new Intent(getActivity().getApplicationContext(),
                PaginaRenderActivity.class);
        intent.putExtras(bundle);
        mLUtils.startActivityWithTransition(intent, view, Utility.TRANS_PAGINA_RENDER);
    }

    private ThemeUtils getThemeUtils() {
        return ((GeneralInsertSearch)getActivity()).getThemeUtils();
    }

}
