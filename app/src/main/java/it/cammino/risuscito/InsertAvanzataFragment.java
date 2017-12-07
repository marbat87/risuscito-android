package it.cammino.risuscito;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.mikepenz.fastadapter.commons.utils.FastAdapterDiffUtil;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.mikepenz.fastadapter.listeners.OnClickListener;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.sql.Date;
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
import it.cammino.risuscito.database.RisuscitoDatabase;
import it.cammino.risuscito.database.dao.CustomListDao;
import it.cammino.risuscito.database.dao.ListePersDao;
import it.cammino.risuscito.database.entities.Canto;
import it.cammino.risuscito.database.entities.CustomList;
import it.cammino.risuscito.database.entities.ListaPers;
import it.cammino.risuscito.items.InsertItem;
import it.cammino.risuscito.ui.ThemeableActivity;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class InsertAvanzataFragment extends Fragment {

  private static String[][] aTexts;
  private final String TAG = getClass().getCanonicalName();
  FastItemAdapter<InsertItem> cantoAdapter;

  @BindView(R.id.matchedList)
  RecyclerView mRecyclerView;

  @BindView(R.id.textfieldRicerca)
  EditText searchPar;

  @BindView(R.id.search_progress)
  MaterialProgressBar progress;

  @BindView(R.id.search_no_results)
  View mNoResults;

  @BindView(R.id.consegnati_only_check)
  SwitchCompat mConsegnatiOnlyCheck;
  //    private DatabaseCanti listaCanti;
  private List<InsertItem> titoli;
  private View rootView;
  private int fromAdd;
  private int idLista;
  private int listPosition;
  private SearchTask searchTask;
  private LUtils mLUtils;
  private long mLastClickTime = 0;
  private Unbinder mUnbinder;

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
      // to hide soft keyboard
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

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    rootView = inflater.inflate(R.layout.activity_ricerca_avanzata, container, false);
    mUnbinder = ButterKnife.bind(this, rootView);

    //        listaCanti = new DatabaseCanti(getActivity());

    OnClickListener<InsertItem> mOnClickListener =
        new OnClickListener<InsertItem>() {
          @Override
          public boolean onClick(
              View view, IAdapter<InsertItem> iAdapter, final InsertItem item, int i) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY) return true;
            mLastClickTime = SystemClock.elapsedRealtime();

            //                SQLiteDatabase db = listaCanti.getReadableDatabase();

            if (fromAdd == 1) {
              // chiamato da una lista predefinita
              //                    String query = "INSERT INTO CUST_LISTS ";
              //                    query+= "VALUES (" + idLista + ", "
              //                            + listPosition + ", "
              //                            + item.getId()
              //                            + ", CURRENT_TIMESTAMP)";
              //                    try {
              //                        db.execSQL(query);
              //                    } catch (SQLException e) {
              //                        Snackbar.make(rootView, R.string.present_yet,
              // Snackbar.LENGTH_SHORT)
              //                                .show();
              //                    }
              new Thread(
                      new Runnable() {
                        @Override
                        public void run() {
                          CustomListDao mDao =
                              RisuscitoDatabase.getInstance(getContext()).customListDao();
                          CustomList position = new CustomList();
                          position.id = idLista;
                          position.position = listPosition;
                          position.idCanto = item.getId();
                          position.timestamp = new Date(System.currentTimeMillis());
                          try {
                            mDao.insertPosition(position);
                          } catch (Exception e) {
                            Snackbar.make(rootView, R.string.present_yet, Snackbar.LENGTH_SHORT)
                                .show();
                          }
                          getActivity().setResult(Activity.RESULT_OK);
                          getActivity().finish();
                          getActivity().overridePendingTransition(0, R.anim.slide_out_right);
                        }
                      })
                  .start();
            } else {
              // chiamato da una lista personalizzata
              //                    String query = "SELECT lista" +
              //                            "  FROM LISTE_PERS" +
              //                            "  WHERE _id =  " + idLista;
              //                    Cursor cursor = db.rawQuery(query, null);
              //                    // recupera l'oggetto lista personalizzata
              //                    cursor.moveToFirst();
              //
              //                    ListaPersonalizzata listaPersonalizzata = (ListaPersonalizzata)
              // ListaPersonalizzata.
              //                            deserializeObject(cursor.getBlob(0));
              //
              //                    // chiude il cursore
              //                    cursor.close();
              //
              //                    if (listaPersonalizzata != null) {
              //                        // lancia la ricerca di tutti i titoli presenti in DB e li
              // dispone in ordine alfabetico
              //                        listaPersonalizzata.addCanto(String.valueOf(item.getId()),
              // listPosition);
              //
              //                        ContentValues values = new ContentValues();
              //                        values.put("lista",
              // ListaPersonalizzata.serializeObject(listaPersonalizzata));
              //                        db.update("LISTE_PERS", values, "_id = " + idLista, null);
              //                    }
              //                    db.close();
              new Thread(
                      new Runnable() {
                        @Override
                        public void run() {
                          ListePersDao mDao =
                              RisuscitoDatabase.getInstance(getContext()).listePersDao();
                          ListaPers listaPers = mDao.getListById(idLista);
                          if (listaPers != null && listaPers.lista != null) {
                            listaPers.lista.addCanto(String.valueOf(item.getId()), listPosition);
                            mDao.updateLista(listaPers);
                            getActivity().setResult(Activity.RESULT_OK);
                            getActivity().finish();
                            getActivity().overridePendingTransition(0, R.anim.slide_out_right);
                          }
                        }
                      })
                  .start();
            }

            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();
            getActivity().overridePendingTransition(0, R.anim.slide_out_right);
            return true;
          }
        };

    ClickEventHook hookListener =
        new ClickEventHook<InsertItem>() {
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
            if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY) return;
            mLastClickTime = SystemClock.elapsedRealtime();

            // crea un bundle e ci mette il parametro "pagina", contente il nome del file della
            // pagina da visualizzare
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
    cantoAdapter.withOnClickListener(mOnClickListener).withEventHook(hookListener);

    mRecyclerView.setAdapter(cantoAdapter);
    LinearLayoutManager llm = new LinearLayoutManager(getContext());
    mRecyclerView.setLayoutManager(llm);
    mRecyclerView.setHasFixedSize(true);
    DividerItemDecoration insetDivider =
        new DividerItemDecoration(getContext(), llm.getOrientation());
    insetDivider.setDrawable(
        ContextCompat.getDrawable(getContext(), R.drawable.material_inset_divider));
    mRecyclerView.addItemDecoration(insetDivider);

    searchPar.setText("");

    Bundle bundle = getArguments();
    fromAdd = bundle.getInt("fromAdd");
    idLista = bundle.getInt("idLista");
    listPosition = bundle.getInt("position");

    try {
      InputStream in;
      switch (ThemeableActivity.getSystemLocalWrapper(
              getActivity().getResources().getConfiguration())
          .getLanguage()) {
        case "uk":
          in = getActivity().getAssets().open("fileout_uk.xml");
          break;
        case "en":
          in = getActivity().getAssets().open("fileout_en.xml");
          break;
        default:
          in = getActivity().getAssets().open("fileout_new.xml");
          break;
      }
      CantiXmlParser parser = new CantiXmlParser();
      aTexts = parser.parse(in);
      in.close();
    } catch (XmlPullParserException | IOException e) {
      e.printStackTrace();
    }

    ((EditText) getActivity().findViewById(R.id.tempTextField))
        .addTextChangedListener(
            new TextWatcher() {

              @Override
              public void onTextChanged(CharSequence s, int start, int before, int count) {
                String tempText = searchPar.getText().toString();
                if (!tempText.equals(s.toString())) searchPar.setText(s);
              }

              @Override
              public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

              @Override
              public void afterTextChanged(Editable s) {}
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
   * Set a hint to the system about whether this fragment's UI is currently visible to the user.
   * This hint defaults to true and is persistent across fragment instance state save and restore.
   *
   * <p>
   *
   * <p>An app may set this to false to indicate that the fragment's UI is scrolled out of
   * visibility or is otherwise not directly visible to the user. This may be used by the system to
   * prioritize operations such as fragment lifecycle updates or loader ordering behavior.
   *
   * @param isVisibleToUser true if this fragment's UI is currently visible to the user (default),
   *     false if it is not.
   */
  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (isVisibleToUser) {
      Log.d(getClass().getName(), "VISIBLE");
      // to hide soft keyboard
      if (searchPar != null)
        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(searchPar.getWindowToken(), 0);
    }
  }

  @Override
  public void onDestroy() {
    Log.d(TAG, "onDestroy");
    if (searchTask != null && searchTask.getStatus() == Status.RUNNING) searchTask.cancel(true);
    //        if (listaCanti != null)
    //            listaCanti.close();
    super.onDestroy();
  }

  private void startSubActivity(Bundle bundle, View view) {
    Intent intent = new Intent(getActivity().getApplicationContext(), PaginaRenderActivity.class);
    intent.putExtras(bundle);
    mLUtils.startActivityWithTransition(intent, view, Utility.TRANS_PAGINA_RENDER);
  }

  private void ricercaStringa(String s, boolean onlyConsegnati) {
    String tempText =
        ((EditText) getActivity().findViewById(R.id.tempTextField)).getText().toString();
    if (!tempText.equals(s)) ((EditText) getActivity().findViewById(R.id.tempTextField)).setText(s);

    // abilita il pulsante solo se la stringa ha più di 3 caratteri, senza contare gli spazi
    if (s.trim().length() >= 3) {
      if (searchTask != null && searchTask.getStatus() == Status.RUNNING) searchTask.cancel(true);

      //      searchTask = new SearchTask(searchPar.getText().toString(), onlyConsegnati);
      searchTask = new SearchTask(InsertAvanzataFragment.this);
      searchTask.execute(searchPar.getText().toString(), String.valueOf(onlyConsegnati));
    } else {
      if (s.isEmpty()) {
        mNoResults.setVisibility(View.GONE);
        cantoAdapter.clear();
        progress.setVisibility(View.INVISIBLE);
      }
    }
  }

  private static class SearchTask extends AsyncTask<String, Void, Integer> {

    //    private String text;
    //    private boolean onlyConsegnati;
    //
    //    SearchTask(String text, boolean onlyConsegnati) {
    //      this.text = text;
    //      this.onlyConsegnati = onlyConsegnati;
    //    }

    private final String TAG = getClass().getCanonicalName();

    private WeakReference<InsertAvanzataFragment> fragmentReference;

    SearchTask(InsertAvanzataFragment fragment) {
      this.fragmentReference = new WeakReference<>(fragment);
    }

    @Override
    protected Integer doInBackground(String... params) {

      //            Log.d(getClass().getName(), "STRINGA: " + sSearchText[0]);
      Log.d(getClass().getName(), "STRINGA: " + params[0]);

      String[] words = params[0].split("\\W");

      String text;

      for (String[] aText : aTexts) {

        Log.d(TAG, "doInBackground: isCancelled? " + isCancelled());
        if (isCancelled()) break;

        if (aText[0] == null || aText[0].equalsIgnoreCase("")) break;

        boolean found = true;
        for (String word : words) {
          if (isCancelled()) break;
          if (word.trim().length() > 1) {
            text = word.trim();
            text =
                text.toLowerCase(
                    ThemeableActivity.getSystemLocalWrapper(
                        fragmentReference.get().getActivity().getResources().getConfiguration()));
            String nfdNormalizedString = Normalizer.normalize(text, Normalizer.Form.NFD);
            Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            text = pattern.matcher(nfdNormalizedString).replaceAll("");

            if (!aText[1].contains(text)) found = false;
          }
        }

        Log.d(TAG, "doInBackground: isCancelled? " + isCancelled());

        if (found && !isCancelled()) {
          //                    SQLiteDatabase db = listaCanti.getReadableDatabase();
          //                    // recupera il titolo colore e pagina del canto da aggiungere alla
          // lista
          //                    String query = "SELECT a.titolo, a.color, a.pagina, a._id,
          // a.source";
          //
          //                    if (onlyConsegnati)
          //                        query += " FROM ELENCO a, CANTI_CONSEGNATI b" +
          //                                " WHERE a._id = b.id_canto" +
          //                                " and a.source = '" + aText[0] + "'";
          //                    else
          //                        query += " FROM ELENCO a " +
          //                                 " WHERE a.source = '" + aText[0] + "'";
          //
          //                    query += " ORDER BY 1 ASC";
          //
          //                    Cursor lista = db.rawQuery(query, null);
          //
          //                    if (lista.getCount() > 0) {
          //                        lista.moveToFirst();
          //                        InsertItem insertItem = new InsertItem();
          //                        insertItem.withTitle(lista.getString(0))
          //                                .withColor(lista.getString(1))
          //                                .withPage(String.valueOf(lista.getInt(2)))
          //                                .withId(lista.getInt(3))
          //                                .withSource(lista.getString(4));
          //                        titoli.add(insertItem);
          //                    }
          //                    // chiude il cursore
          //                    lista.close();
          //                    db.close();
          RisuscitoDatabase mDb =
              RisuscitoDatabase.getInstance(fragmentReference.get().getActivity());
          List<Canto> elenco;
          boolean onlyConsegnati = Boolean.parseBoolean(params[1]);
          if (onlyConsegnati) elenco = mDb.cantoDao().getCantiWithSourceOnlyConsegnati(aText[0]);
          else elenco = mDb.cantoDao().getCantiWithSource(aText[0]);

          if (elenco != null) {
            for (Canto canto : elenco) {
              InsertItem insertItem = new InsertItem();
              insertItem
                  .withTitle(canto.titolo)
                  .withColor(canto.color)
                  .withPage(String.valueOf(canto.pagina))
                  .withId(canto.id)
                  .withSource(canto.source);
              fragmentReference.get().titoli.add(insertItem);
            }
          }
        }
      }

      return 0;
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      fragmentReference.get().mNoResults.setVisibility(View.GONE);
      fragmentReference.get().progress.setVisibility(View.VISIBLE);
      fragmentReference.get().titoli.clear();
//      fragmentReference.get().cantoAdapter.clear();
    }

    @Override
    protected void onPostExecute(Integer result) {
      super.onPostExecute(result);
      //      fragmentReference.get().cantoAdapter.add(fragmentReference.get().titoli);
      //      //      cantoAdapter.notifyAdapterDataSetChanged();
      //      fragmentReference.get().progress.setVisibility(View.INVISIBLE);
      //      if (fragmentReference.get().titoli.size() == 0)
      //        fragmentReference.get().mNoResults.setVisibility(View.VISIBLE);
      //      else fragmentReference.get().mNoResults.setVisibility(View.GONE)
      FastAdapterDiffUtil.set(fragmentReference.get().cantoAdapter, fragmentReference.get().titoli);
      fragmentReference.get().progress.setVisibility(View.INVISIBLE);
      fragmentReference
          .get()
          .mNoResults
          .setVisibility(
              fragmentReference.get().cantoAdapter.getAdapterItemCount() == 0
                  ? View.VISIBLE
                  : View.GONE);
    }
  }
}
