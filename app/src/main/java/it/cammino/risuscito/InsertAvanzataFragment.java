package it.cammino.risuscito;

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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
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
import android.widget.TextView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.ClickEventHook;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import butterknife.Unbinder;
import it.cammino.risuscito.items.InsertItem;
import it.cammino.risuscito.ui.ThemeableActivity;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class InsertAvanzataFragment extends Fragment {

    private final String TAG = getClass().getCanonicalName();

    private DatabaseCanti listaCanti;
    private List<InsertItem> titoli;
    private View rootView;
    private static String[][] aTexts;
    FastItemAdapter<InsertItem> cantoAdapter;

    private int fromAdd;
    private int idLista;
    private int listPosition;

    private SearchTask searchTask;

    private LUtils mLUtils;

    private long mLastClickTime = 0;

    @BindView(R.id.matchedList) RecyclerView mRecyclerView;
    @BindView(R.id.textfieldRicerca) EditText searchPar;
    @BindView(R.id.search_progress) MaterialProgressBar progress;
    @BindView(R.id.search_no_results) View mNoResults;
    @BindView(R.id.consegnati_only_check) SwitchCompat mConsegnatiOnlyCheck;

    @OnClick(R.id.pulisci_ripple)
    public void pulisciRisultati() {
        searchPar.setText("");
    }

    @OnCheckedChanged(R.id.consegnati_only_check)
    public void aggiornaRicerca(boolean checked) {
        if (searchPar.getText() != null && !searchPar.getText().toString().isEmpty())
            ricercaStringa(searchPar.getText().toString(), checked);
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
        ricercaStringa(s.toString(), mConsegnatiOnlyCheck.isChecked());
    }

    private Unbinder mUnbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_ricerca_avanzata, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        listaCanti = new DatabaseCanti(getActivity());

        FastAdapter.OnClickListener<InsertItem> mOnClickListener = new FastAdapter.OnClickListener<InsertItem>() {
            @Override
            public boolean onClick(View view, IAdapter<InsertItem> iAdapter, InsertItem item, int i) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
                    return true;
                mLastClickTime = SystemClock.elapsedRealtime();

                SQLiteDatabase db = listaCanti.getReadableDatabase();

                if (fromAdd == 1)  {
                    // chiamato da una lista predefinita
                    String query = "INSERT INTO CUST_LISTS ";
                    query+= "VALUES (" + idLista + ", "
                            + listPosition + ", "
                            + item.getId()
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

                    if (listaPersonalizzata != null) {
                        // lancia la ricerca di tutti i titoli presenti in DB e li dispone in ordine alfabetico
                        listaPersonalizzata.addCanto(String.valueOf(item.getId()), listPosition);

                        ContentValues values = new ContentValues();
                        values.put("lista", ListaPersonalizzata.serializeObject(listaPersonalizzata));
                        db.update("LISTE_PERS", values, "_id = " + idLista, null);
                    }
                    db.close();
                }

                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
                getActivity().overridePendingTransition(0, R.anim.slide_out_right);
                return true;
            }
        };

        ClickEventHook hookListener = new ClickEventHook<InsertItem>() {
            @Nullable
            @Override
            public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof InsertItem.ViewHolder) {
                    return ((InsertItem.ViewHolder) viewHolder).mPreview;
                }
                return null;
            }

            @Override
            public void onClick(View view, int i, FastAdapter fastAdapter, InsertItem item) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
                    return;
                mLastClickTime = SystemClock.elapsedRealtime();

                // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da visualizzare
                Bundle bundle = new Bundle();
                bundle.putString("pagina", item.getSource().toString());
                bundle.putInt("idCanto", item.getId());

                // lancia l'activity che visualizza il canto passando il parametro creato
                startSubActivity(bundle, view);
            }
        };

        // Creating new adapter object
        titoli = new ArrayList<>();
        cantoAdapter = new FastItemAdapter<>();
        cantoAdapter.setHasStableIds(true);
        //noinspection unchecked
        cantoAdapter.withOnClickListener(mOnClickListener)
                .withItemEvent(hookListener);

        mRecyclerView.setAdapter(cantoAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setHasFixedSize(true);
        DividerItemDecoration insetDivider = new DividerItemDecoration(getContext(), llm.getOrientation());
        insetDivider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.inset_divider_light));
        mRecyclerView.addItemDecoration(insetDivider);

        searchPar.setText("");

        Bundle bundle = getArguments();
        fromAdd = bundle.getInt("fromAdd");
        idLista = bundle.getInt("idLista");
        listPosition = bundle.getInt("position");

        try {
            InputStream in = getActivity().getAssets().open("fileout_new.xml");
            if (ThemeableActivity.getSystemLocalWrapper(getActivity().getResources().getConfiguration()).getLanguage().equalsIgnoreCase("uk"))
                in = getActivity().getAssets().open("fileout_uk.xml");
            if (ThemeableActivity.getSystemLocalWrapper(getActivity().getResources().getConfiguration()).getLanguage().equalsIgnoreCase("en"))
                in = getActivity().getAssets().open("fileout_en.xml");
            CantiXmlParser parser = new CantiXmlParser();
            aTexts = parser.parse(in);
            in.close();
        } 	catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

//        searchPar.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                ricercaStringa(s.toString(), mConsegnatiOnlyCheck.isChecked());
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
//
//            @Override
//            public void afterTextChanged(Editable s) { }
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

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
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
        Log.d(TAG, "onDestroy");
        if (searchTask != null && searchTask.getStatus() == Status.RUNNING)
            searchTask.cancel(true);
        if (listaCanti != null)
            listaCanti.close();
        super.onDestroy();
    }

    private class SearchTask extends AsyncTask<String, Integer, String> {

        private String text;
        private boolean onlyConsegnati;

        SearchTask(String text, boolean onlyConsegnati) {
            this.text = text;
            this.onlyConsegnati = onlyConsegnati;
        }

        @Override
        protected String doInBackground(String... params) {

//            Log.d(getClass().getName(), "STRINGA: " + sSearchText[0]);
            Log.d(getClass().getName(), "STRINGA: " + text);

//            String[] words = sSearchText[0].split("\\W");
            String[] words = text.split("\\W");

            String text;

            for (String[] aText : aTexts) {

                Log.d(TAG, "doInBackground: isCancelled? " + isCancelled());
                if (isCancelled())
                    break;

                if (aText[0] == null || aText[0].equalsIgnoreCase(""))
                    break;

                boolean found = true;
                for (String word : words) {
                    if (isCancelled())
                        break;
                    if (word.trim().length() > 1) {
                        text = word.trim();
                        text = text.toLowerCase(ThemeableActivity.getSystemLocalWrapper(getActivity().getResources().getConfiguration()));
                        String nfdNormalizedString = Normalizer.normalize(text, Normalizer.Form.NFD);
                        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
                        text = pattern.matcher(nfdNormalizedString).replaceAll("");

                        if (!aText[1].contains(text))
                            found = false;
                    }
                }

                Log.d(TAG, "doInBackground: isCancelled? " + isCancelled());

                if (found && !isCancelled()) {
                    SQLiteDatabase db = listaCanti.getReadableDatabase();
                    // recupera il titolo colore e pagina del canto da aggiungere alla lista
//                    String query = "SELECT titolo, color, pagina, _id, source"
//                            + "		FROM ELENCO"
//                            + "		WHERE source = '" + aText[0] + "'";
                    String query = "SELECT a.titolo, a.color, a.pagina, a._id, a.source";

                    if (onlyConsegnati)
                        query += " FROM ELENCO a, CANTI_CONSEGNATI b" +
                                " WHERE a._id = b.id_canto" +
                                " and a.source = '" + aText[0] + "'";
                    else
                        query += " FROM ELENCO a " +
                                 " WHERE a.source = '" + aText[0] + "'";

                    query += " ORDER BY 1 ASC";

                    Cursor lista = db.rawQuery(query, null);

                    if (lista.getCount() > 0) {
                        lista.moveToFirst();
                        InsertItem insertItem = new InsertItem();
                        insertItem.withTitle(lista.getString(0))
                                .withColor(lista.getString(1))
                                .withPage(String.valueOf(lista.getInt(2)))
                                .withId(lista.getInt(3))
                                .withSource(lista.getString(4));
                        titoli.add(insertItem);
                    }
                    // chiude il cursore
                    lista.close();
                    db.close();
                }
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mNoResults.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            titoli.clear();
            cantoAdapter.clear();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            cantoAdapter.add(titoli);
            cantoAdapter.notifyAdapterDataSetChanged();
            progress.setVisibility(View.INVISIBLE);
            if (titoli.size() == 0)
                mNoResults.setVisibility(View.VISIBLE);
            else
                mNoResults.setVisibility(View.GONE);
        }

    }

    private void startSubActivity(Bundle bundle, View view) {
        Intent intent = new Intent(getActivity().getApplicationContext(),
                PaginaRenderActivity.class);
        intent.putExtras(bundle);
        mLUtils.startActivityWithTransition(intent, view, Utility.TRANS_PAGINA_RENDER);
    }

    private void ricercaStringa(String s, boolean onlyConsegnati) {
        String tempText = ((EditText) getActivity().findViewById(R.id.tempTextField)).getText().toString();
        if (!tempText.equals(s))
            ((EditText) getActivity().findViewById(R.id.tempTextField)).setText(s);

        //abilita il pulsante solo se la stringa ha più di 3 caratteri, senza contare gli spazi
        if (s.trim().length() >= 3) {
            if (searchTask != null && searchTask.getStatus() == Status.RUNNING)
                searchTask.cancel(true);
            searchTask = new SearchTask(searchPar.getText().toString(), onlyConsegnati);
//            searchTask.execute(searchPar.getText().toString());
            searchTask.execute();
        }
        else {
            if (s.isEmpty()) {
                mNoResults.setVisibility(View.GONE);
                cantoAdapter.clear();
                progress.setVisibility(View.INVISIBLE);
            }
        }
    }

}
