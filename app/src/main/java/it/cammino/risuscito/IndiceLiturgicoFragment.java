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

import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.expandable.ExpandableExtension;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.itemanimators.SlideDownAlphaAnimator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import it.cammino.risuscito.database.CantoArgomento;
import it.cammino.risuscito.database.CantoLiturgico;
import it.cammino.risuscito.database.RisuscitoDatabase;
import it.cammino.risuscito.database.dao.ArgomentiDao;
import it.cammino.risuscito.database.dao.CantoDao;
import it.cammino.risuscito.database.dao.CustomListDao;
import it.cammino.risuscito.database.dao.IndiceLiturgicoDao;
import it.cammino.risuscito.database.dao.ListePersDao;
import it.cammino.risuscito.database.entities.Canto;
import it.cammino.risuscito.database.entities.ListaPers;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.items.SimpleSubExpandableItem;
import it.cammino.risuscito.items.SimpleSubItem;
import it.cammino.risuscito.ui.HFFragment;
import it.cammino.risuscito.utils.ListeUtils;

public class IndiceLiturgicoFragment extends HFFragment
    implements View.OnCreateContextMenuListener, SimpleDialogFragment.SimpleCallback {

  private final String TAG = getClass().getCanonicalName();

  private final int ID_FITTIZIO = 99999999;

  @BindView(R.id.recycler_view)
  RecyclerView mRecyclerView;
  // create boolean for fetching data
  private boolean isViewShown = true;
  //    private DatabaseCanti listaCanti;
  private String titoloDaAgg;
  private int idDaAgg;
  private int idListaDaAgg;
  private int posizioneDaAgg;
  //    private ListaPersonalizzata[] listePers;
  //    private int[] idListe;
  private List<ListaPers> listePersonalizzate;
  private int idListaClick;
  private int idPosizioneClick;
  private View rootView;
  private LUtils mLUtils;
  private FastItemAdapter<IItem> mAdapter;
  private LinearLayoutManager mLayoutManager;
  private long mLastClickTime = 0;
  private Unbinder mUnbinder;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
    rootView = inflater.inflate(R.layout.layout_recycler, container, false);
    mUnbinder = ButterKnife.bind(this, rootView);

    final OnClickListener<SimpleSubItem> mOnClickListener =
        new OnClickListener<SimpleSubItem>() {
          @Override
          public boolean onClick(
              View view, IAdapter<SimpleSubItem> iAdapter, SimpleSubItem item, int i) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY) return false;
            mLastClickTime = SystemClock.elapsedRealtime();
            Bundle bundle = new Bundle();
            bundle.putCharSequence("pagina", item.getSource().getText());
            bundle.putInt("idCanto", item.getId());

            // lancia l'activity che visualizza il canto passando il parametro creato
            startSubActivity(bundle, view);
            return true;
          }
        };

    //        // crea un istanza dell'oggetto DatabaseCanti
    //        if (listaCanti == null) listaCanti = new DatabaseCanti(getActivity());
    //
    //        // crea un manipolatore per il Database in modalità READ
    //        SQLiteDatabase db = listaCanti.getReadableDatabase();
    //
    //        // lancia la ricerca di tutti i momenti litrurgici in DB e li dispone in ordine
    // alfabetico
    //        String query = "SELECT _id, nome" + "		FROM INDICE_LIT_NAMES" + "		ORDER BY nome ASC";
    //        Cursor arguments = db.rawQuery(query, null);
    //
    //        // recupera il numero dei momenti liturgici
    //        int total = arguments.getCount();
    //        arguments.moveToFirst();

    List<IItem> mItems = new ArrayList<>();

    //        for (int i = 0; i < total; i++) {
    //            String argId = String.valueOf(arguments.getInt(0));
    //            query =
    //                    "SELECT B._id, B.titolo, B.color, B.pagina, B.source"
    //                            + "		FROM INDICE_LIT A, ELENCO B "
    //                            + "       WHERE A._id = "
    //                            + argId
    //                            + "       AND A.id_canto = B._id "
    //                            + "		ORDER BY TITOLO ASC";
    //            Cursor argCanti = db.rawQuery(query, null);
    //
    //            // recupera il numero di canti per il momento liturgico
    //            int totCanti = argCanti.getCount();
    //            argCanti.moveToFirst();
    //
    //            SimpleSubExpandableItem expandableItem = new SimpleSubExpandableItem();
    //            expandableItem
    //                    .withTitle(arguments.getString(1) + " (" + totCanti + ")")
    //                    //                    .withColor(getThemeUtils().primaryColorDark())
    //                    .withOnClickListener(
    //                            new OnClickListener<SimpleSubExpandableItem>() {
    //                                @Override
    //                                public boolean onClick(
    //                                        View view,
    //                                        IAdapter<SimpleSubExpandableItem> iAdapter,
    //                                        SimpleSubExpandableItem item,
    //                                        int i) {
    //                                    if (item.isExpanded())
    // mLayoutManager.scrollToPositionWithOffset(i, 0);
    //                                    return false;
    //                                }
    //                            })
    //                    .withIdentifier(Integer.parseInt(argId));
    //            List<SimpleSubItem> subItems = new LinkedList<>();
    //
    //            for (int j = 1; j <= totCanti; j++) {
    //                //                Log.d(getClass().getName(), "onCreateView: " +
    // argCanti.getString(1));
    //                SimpleSubItem simpleItem =
    //                        new SimpleSubItem()
    //                                .withTitle(argCanti.getString(1))
    //                                .withPage(String.valueOf(argCanti.getInt(3)))
    //                                .withSource(argCanti.getString(4))
    //                                .withColor(argCanti.getString(2))
    //                                .withId(argCanti.getInt(0));
    //                // serve a non mettere il divisore sull'ultimo elemento della lista
    //                simpleItem.withHasDivider(j < totCanti);
    //                // noinspection unchecked
    //                simpleItem
    //                        .withContextMenuListener(IndiceLiturgicoFragment.this)
    //                        .withOnItemClickListener(mOnClickListener);
    //                simpleItem.withIdentifier(Integer.parseInt(argId) * 1000 + j);
    //                subItems.add(simpleItem);
    //                argCanti.moveToNext();
    //            }
    //            argCanti.close();
    //            // noinspection unchecked
    //            expandableItem.withSubItems(subItems);
    //
    //            mItems.add(expandableItem);
    //
    //            arguments.moveToNext();
    //        }
    //
    //        arguments.close();

    mLayoutManager = new LinearLayoutManager(getActivity());
    mRecyclerView.setLayoutManager(mLayoutManager);

    mAdapter = new FastItemAdapter<>();
    mAdapter.add(mItems);
    ExpandableExtension<IItem> itemExpandableExtension = new ExpandableExtension<>();
    itemExpandableExtension.withOnlyOneExpandedItem(true);
    mAdapter.addExtension(itemExpandableExtension);

    mRecyclerView.setAdapter(mAdapter);
    mRecyclerView.setHasFixedSize(true); // Size of RV will not change
    mRecyclerView.setItemAnimator(new SlideDownAlphaAnimator());

    new Thread(
            new Runnable() {
              @Override
              public void run() {
                IndiceLiturgicoDao mDao =
                    RisuscitoDatabase.getInstance(getContext()).indiceLiturgicoDao();
                List<CantoLiturgico> canti = mDao.getAll();
                List<IItem> titoli = new ArrayList<>();
                List<SimpleSubItem> subItems = new LinkedList<>();
                int totCanti = 0;

                for (int i = 0; i < canti.size(); i++) {
                  SimpleSubItem simpleItem =
                      new SimpleSubItem()
                          .withTitle(canti.get(i).titolo)
                          .withPage(String.valueOf(canti.get(i).pagina))
                          .withSource(canti.get(i).source)
                          .withColor(canti.get(i).color)
                          .withId(canti.get(i).id);
                  // noinspection unchecked
                  simpleItem
                      .withContextMenuListener(IndiceLiturgicoFragment.this)
                      .withOnItemClickListener(mOnClickListener);
                  simpleItem.withIdentifier(i * 1000);
                  subItems.add(simpleItem);
                  totCanti++;

                  if (i == (canti.size() - 1)
                      || canti.get(i).idIndice != canti.get(i + 1).idIndice) {
                    // serve a non mettere il divisore sull'ultimo elemento della lista
                    simpleItem.withHasDivider(false);
                    SimpleSubExpandableItem expandableItem = new SimpleSubExpandableItem();
                    expandableItem
                        .withTitle(canti.get(i).nome + " (" + totCanti + ")")
                        .withOnClickListener(
                            new OnClickListener<SimpleSubExpandableItem>() {
                              @Override
                              public boolean onClick(
                                  View view,
                                  IAdapter<SimpleSubExpandableItem> iAdapter,
                                  SimpleSubExpandableItem item,
                                  int i) {
                                if (item.isExpanded()) {
                                  Log.d(
                                      TAG,
                                      "onClick: " + mRecyclerView.getChildAdapterPosition(view));
                                  mLayoutManager.scrollToPositionWithOffset(
                                      mRecyclerView.getChildAdapterPosition(view), 0);
                                }
                                return false;
                              }
                            })
                        .withIdentifier(canti.get(i).idIndice);
                    // noinspection unchecked
                    expandableItem.withSubItems(subItems);
                    titoli.add(expandableItem);
                    subItems = new LinkedList<>();
                    totCanti = 0;
                  } else {
                    simpleItem.withHasDivider(true);
                  }
                }

                mAdapter.clear();
                mAdapter.add(titoli);
                mAdapter.notifyAdapterDataSetChanged();
                // restore selections (this has to be done after the items were added
                mAdapter.withSavedInstanceState(savedInstanceState);
              }
            })
        .start();

    // restore selections (this has to be done after the items were added
    //        mAdapter.withSavedInstanceState(savedInstanceState);

    mLUtils = LUtils.getInstance(getActivity());

    if (savedInstanceState != null) {
      Log.d(getClass().getName(), "onCreateView: RESTORING");
      idDaAgg = savedInstanceState.getInt("idDaAgg", 0);
      idPosizioneClick = savedInstanceState.getInt("idPosizioneClick", 0);
      idListaClick = savedInstanceState.getInt("idListaClick", 0);
      idListaDaAgg = savedInstanceState.getInt("idListaDaAgg", 0);
      posizioneDaAgg = savedInstanceState.getInt("posizioneDaAgg", 0);
      SimpleDialogFragment sFragment =
          SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "LITURGICO_REPLACE");
      if (sFragment != null) sFragment.setmCallback(IndiceLiturgicoFragment.this);
      sFragment =
          SimpleDialogFragment.findVisible(
              (AppCompatActivity) getActivity(), "LITURGICO_REPLACE_2");
      if (sFragment != null) sFragment.setmCallback(IndiceLiturgicoFragment.this);
    }

    if (!isViewShown) {
      //      query = "SELECT _id, lista" + "		FROM LISTE_PERS" + "		ORDER BY _id ASC";
      //      Cursor lista = db.rawQuery(query, null);
      //
      //      listePers = new ListaPersonalizzata[lista.getCount()];
      //      idListe = new int[lista.getCount()];
      //
      //      lista.moveToFirst();
      //      for (int i = 0; i < lista.getCount(); i++) {
      //        idListe[i] = lista.getInt(0);
      //        listePers[i] =
      //            (ListaPersonalizzata) ListaPersonalizzata.deserializeObject(lista.getBlob(1));
      //        lista.moveToNext();
      //      }
      //
      //      lista.close();
      new Thread(
              new Runnable() {
                @Override
                public void run() {
                  ListePersDao mDao = RisuscitoDatabase.getInstance(getContext()).listePersDao();
                  listePersonalizzate = mDao.getAll();
                }
              })
          .start();
    }

    //    db.close();

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
      if (getView() != null) {
        isViewShown = true;
        Log.d(getClass().getName(), "VISIBLE");
        //        if (listaCanti == null) listaCanti = new DatabaseCanti(getActivity());
        //        SQLiteDatabase db = listaCanti.getReadableDatabase();
        //        String query = "SELECT _id, lista" + "		FROM LISTE_PERS" + "		ORDER BY _id ASC";
        //        Cursor lista = db.rawQuery(query, null);
        //
        //        listePers = new ListaPersonalizzata[lista.getCount()];
        //        idListe = new int[lista.getCount()];
        //
        //        lista.moveToFirst();
        //        for (int i = 0; i < lista.getCount(); i++) {
        //          idListe[i] = lista.getInt(0);
        //          listePers[i] =
        //              (ListaPersonalizzata)
        // ListaPersonalizzata.deserializeObject(lista.getBlob(1));
        //          lista.moveToNext();
        //        }
        //
        //        lista.close();
        //        db.close();
        new Thread(
                new Runnable() {
                  @Override
                  public void run() {
                    ListePersDao mDao = RisuscitoDatabase.getInstance(getContext()).listePersDao();
                    listePersonalizzate = mDao.getAll();
                  }
                })
            .start();
      } else isViewShown = false;
    }
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
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

  //  @Override
  //  public void onDestroy() {
  //    if (listaCanti != null) listaCanti.close();
  //    super.onDestroy();
  //  }

  private void startSubActivity(Bundle bundle, View view) {
    Intent intent = new Intent(getActivity(), PaginaRenderActivity.class);
    intent.putExtras(bundle);
    mLUtils.startActivityWithTransition(intent, view, Utility.TRANS_PAGINA_RENDER);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    titoloDaAgg = ((TextView) v.findViewById(R.id.text_title)).getText().toString();
    idDaAgg = Integer.valueOf(((TextView) v.findViewById(R.id.text_id_canto)).getText().toString());
    menu.setHeaderTitle("Aggiungi canto a:");

    //    for (int i = 0; i < idListe.length; i++) {
    //      SubMenu subMenu = menu.addSubMenu(ID_FITTIZIO, Menu.NONE, 10 + i,
    // listePers[i].getName());
    //      for (int k = 0; k < listePers[i].getNumPosizioni(); k++)
    //        subMenu.add(100 + i, k, k, listePers[i].getNomePosizione(k));
    //    }
    for (int i = 0; i < listePersonalizzate.size(); i++) {
      SubMenu subMenu =
          menu.addSubMenu(
              ID_FITTIZIO, Menu.NONE, 10 + i, listePersonalizzate.get(i).lista.getName());
      for (int k = 0; k < listePersonalizzate.get(i).lista.getNumPosizioni(); k++) {
        subMenu.add(100 + i, k, k, listePersonalizzate.get(i).lista.getNomePosizione(k));
      }
    }

    MenuInflater inflater = getActivity().getMenuInflater();
    inflater.inflate(R.menu.add_to, menu);

    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
    menu.findItem(R.id.add_to_p_pace).setVisible(pref.getBoolean(Utility.SHOW_PACE, false));
    menu.findItem(R.id.add_to_e_seconda).setVisible(pref.getBoolean(Utility.SHOW_SECONDA, false));
    menu.findItem(R.id.add_to_e_offertorio)
        .setVisible(pref.getBoolean(Utility.SHOW_OFFERTORIO, false));
    menu.findItem(R.id.add_to_e_santo).setVisible(pref.getBoolean(Utility.SHOW_SANTO, false));
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    if (getUserVisibleHint()) {
      switch (item.getItemId()) {
        case R.id.add_to_favorites:
          //          addToFavorites();
          ListeUtils.addToFavorites(getContext(), rootView, idDaAgg);
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
          //          addToListaDup(2, 3);
          ListeUtils.addToListaDup(getContext(), rootView, 2, 3, idDaAgg);
          return true;
        case R.id.add_to_e_vino:
          //          addToListaDup(2, 4);
          ListeUtils.addToListaDup(getContext(), rootView, 2, 4, idDaAgg);
          return true;
        case R.id.add_to_e_fine:
          addToListaNoDup(2, 5);
          return true;
        default:
          idListaClick = item.getGroupId();
          idPosizioneClick = item.getItemId();
          if (idListaClick != ID_FITTIZIO && idListaClick >= 100) {
            idListaClick -= 100;

            //            SQLiteDatabase db = listaCanti.getReadableDatabase();
            //            if
            // (listePers[idListaClick].getCantoPosizione(idPosizioneClick).equals("")) {
            //              listePers[idListaClick].addCanto(String.valueOf(idDaAgg),
            // idPosizioneClick);
            //              ContentValues values = new ContentValues();
            //              values.put("lista",
            // ListaPersonalizzata.serializeObject(listePers[idListaClick]));
            //              db.update("LISTE_PERS", values, "_id = " + idListe[idListaClick], null);
            //              Snackbar.make(rootView, R.string.list_added,
            // Snackbar.LENGTH_SHORT).show();
            if (listePersonalizzate
                .get(idListaClick)
                .lista
                .getCantoPosizione(idPosizioneClick)
                .equals("")) {
              listePersonalizzate
                  .get(idListaClick)
                  .lista
                  .addCanto(String.valueOf(idDaAgg), idPosizioneClick);
              new Thread(
                      new Runnable() {
                        @Override
                        public void run() {
                          ListePersDao mDao =
                              RisuscitoDatabase.getInstance(getContext()).listePersDao();
                          mDao.updateLista(listePersonalizzate.get(idListaClick));
                          Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT)
                              .show();
                        }
                      })
                  .start();
            } else {
              //              if (listePers[idListaClick]
              if (listePersonalizzate
                  .get(idListaClick)
                  .lista
                  .getCantoPosizione(idPosizioneClick)
                  .equals(String.valueOf(idDaAgg))) {
                Snackbar.make(rootView, R.string.present_yet, Snackbar.LENGTH_SHORT).show();
              } else {
                // recupero titolo del canto presente
                //                String query =
                //                    "SELECT titolo"
                //                        + "		FROM ELENCO"
                //                        + "		WHERE _id = "
                //                        +
                // listePers[idListaClick].getCantoPosizione(idPosizioneClick);
                //                Cursor cursor = db.rawQuery(query, null);
                //                cursor.moveToFirst();
                //                new SimpleDialogFragment.Builder(
                //                        (AppCompatActivity) getActivity(),
                //                        IndiceLiturgicoFragment.this,
                //                        "LITURGICO_REPLACE")
                //                    .title(R.string.dialog_replace_title)
                //                    .content(
                //                        getString(R.string.dialog_present_yet)
                //                            + " "
                //                            + cursor.getString(0)
                //                            + getString(R.string.dialog_wonna_replace))
                //                    .positiveButton(android.R.string.yes)
                //                    .negativeButton(android.R.string.no)
                //                    .show();
                //                cursor.close();
                new Thread(
                        new Runnable() {
                          @Override
                          public void run() {
                            CantoDao mDao = RisuscitoDatabase.getInstance(getContext()).cantoDao();
                            Canto cantoPresente =
                                mDao.getCantoById(
                                    Integer.parseInt(
                                        listePersonalizzate
                                            .get(idListaClick)
                                            .lista
                                            .getCantoPosizione(idPosizioneClick)));
                            new SimpleDialogFragment.Builder(
                                    (AppCompatActivity) getActivity(),
                                    IndiceLiturgicoFragment.this,
                                    "LITURGICO_REPLACE")
                                .title(R.string.dialog_replace_title)
                                .content(
                                    getString(R.string.dialog_present_yet)
                                        + " "
                                        + cantoPresente.titolo
                                        + getString(R.string.dialog_wonna_replace))
                                .positiveButton(android.R.string.yes)
                                .negativeButton(android.R.string.no)
                                .show();
                          }
                        })
                    .start();
              }
            }
            //            db.close();
            return true;
          } else return super.onContextItemSelected(item);
      }
    } else return false;
  }

  // aggiunge il canto premuto ai preferiti
  //  public void addToFavorites() {
  //    SQLiteDatabase db = listaCanti.getReadableDatabase();
  //    String sql = "UPDATE ELENCO" + "  SET favourite = 1" + "  WHERE _id =  " + idDaAgg;
  //    db.execSQL(sql);
  //    db.close();
  //    Snackbar.make(rootView, R.string.favorite_added, Snackbar.LENGTH_SHORT).show();
  //  }

  // aggiunge il canto premuto ad una lista e in una posizione che ammetta duplicati
  //  public void addToListaDup(int idLista, int listPosition) {
  //    SQLiteDatabase db = listaCanti.getReadableDatabase();
  //
  //    String sql = "INSERT INTO CUST_LISTS ";
  //    sql += "VALUES (" + idLista + ", " + listPosition + ", " + idDaAgg + ", CURRENT_TIMESTAMP)";
  //
  //    try {
  //      db.execSQL(sql);
  //      Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT).show();
  //    } catch (SQLException e) {
  //      Snackbar.make(rootView, R.string.present_yet, Snackbar.LENGTH_SHORT).show();
  //    }
  //
  //    db.close();
  //  }

  // aggiunge il canto premuto ad una lista e in una posizione che NON ammetta duplicati
  //  public void addToListaNoDup(int idLista, int listPosition) {
  //
  //    SQLiteDatabase db = listaCanti.getReadableDatabase();
  //
  //    // cerca se la posizione nella lista è già occupata
  //    String query =
  //        "SELECT B.titolo"
  //            + "		FROM CUST_LISTS A"
  //            + "		   , ELENCO B"
  //            + "		WHERE A._id = "
  //            + idLista
  //            + "         AND A.position = "
  //            + listPosition
  //            + "         AND A.id_canto = B._id";
  //    Cursor lista = db.rawQuery(query, null);
  //
  //    int total = lista.getCount();
  //
  //    if (total > 0) {
  //      lista.moveToFirst();
  //      String titoloPresente = lista.getString(0);
  //      lista.close();
  //      db.close();
  //
  //      if (titoloDaAgg.equalsIgnoreCase(titoloPresente))
  //        Snackbar.make(rootView, R.string.present_yet, Snackbar.LENGTH_SHORT).show();
  //      else {
  //        idListaDaAgg = idLista;
  //        posizioneDaAgg = listPosition;
  //        new SimpleDialogFragment.Builder(
  //                (AppCompatActivity) getActivity(),
  //                IndiceLiturgicoFragment.this,
  //                "LITURGICO_REPLACE_2")
  //            .title(R.string.dialog_replace_title)
  //            .content(
  //                getString(R.string.dialog_present_yet)
  //                    + " "
  //                    + titoloPresente
  //                    + getString(R.string.dialog_wonna_replace))
  //            .positiveButton(android.R.string.yes)
  //            .negativeButton(android.R.string.no)
  //            .show();
  //      }
  //      return;
  //    }
  //
  //    lista.close();
  //
  //    String sql =
  //        "INSERT INTO CUST_LISTS"
  //            + " VALUES ("
  //            + idLista
  //            + ", "
  //            + listPosition
  //            + ", "
  //            + idDaAgg
  //            + ", CURRENT_TIMESTAMP)";
  //    db.execSQL(sql);
  //    db.close();
  //
  //    Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT).show();
  //  }

  @Override
  public void onPositive(@NonNull String tag) {
    Log.d(getClass().getName(), "onPositive: " + tag);
    switch (tag) {
      case "LITURGICO_REPLACE":
        //        SQLiteDatabase db = listaCanti.getReadableDatabase();
        //        listePers[idListaClick].addCanto(String.valueOf(idDaAgg), idPosizioneClick);
        //
        //        ContentValues values = new ContentValues();
        //        values.put("lista", ListaPersonalizzata.serializeObject(listePers[idListaClick]));
        //        db.update("LISTE_PERS", values, "_id = " + idListe[idListaClick], null);
        //        db.close();
        //        Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT).show();
        listePersonalizzate
            .get(idListaClick)
            .lista
            .addCanto(String.valueOf(idDaAgg), idPosizioneClick);
        new Thread(
                new Runnable() {
                  @Override
                  public void run() {
                    ListePersDao mDao = RisuscitoDatabase.getInstance(getContext()).listePersDao();
                    mDao.updateLista(listePersonalizzate.get(idListaClick));
                    Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT).show();
                  }
                })
            .start();
        break;
      case "LITURGICO_REPLACE_2":
        //        db = listaCanti.getReadableDatabase();
        //        String sql =
        //            "UPDATE CUST_LISTS "
        //                + "     SET id_canto = "
        //                + idDaAgg
        //                + "     WHERE _id = "
        //                + idListaDaAgg
        //                + "     AND position = "
        //                + posizioneDaAgg;
        //        db.execSQL(sql);
        //        db.close();
        //        Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT).show();
        new Thread(
                new Runnable() {
                  @Override
                  public void run() {
                    CustomListDao mCustomListDao =
                        RisuscitoDatabase.getInstance(getContext()).customListDao();
                    mCustomListDao.updatePositionNoTimestamp(idDaAgg, idListaDaAgg, posizioneDaAgg);
                    Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT).show();
                  }
                })
            .start();
        break;
    }
  }

  @Override
  public void onNegative(@NonNull String tag) {}

  @Override
  public void onNeutral(@NonNull String tag) {}

  private void addToListaNoDup(final int idLista, final int listPosition) {
    new Thread(
            new Runnable() {
              @Override
              public void run() {
                String titoloPresente =
                    ListeUtils.addToListaNoDup(
                        getActivity(), rootView, idLista, listPosition, titoloDaAgg, idDaAgg);
                if (!titoloPresente.isEmpty()) {
                  idListaDaAgg = idLista;
                  posizioneDaAgg = listPosition;
                  new SimpleDialogFragment.Builder(
                          (AppCompatActivity) getActivity(),
                          IndiceLiturgicoFragment.this,
                          "LITURGICO_REPLACE_2")
                      .title(R.string.dialog_replace_title)
                      .content(
                          getString(R.string.dialog_present_yet)
                              + " "
                              + titoloPresente
                              + getString(R.string.dialog_wonna_replace))
                      .positiveButton(android.R.string.yes)
                      .negativeButton(android.R.string.no)
                      .show();
                }
              }
            })
        .start();
  }
}
