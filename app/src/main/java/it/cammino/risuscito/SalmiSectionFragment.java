package it.cammino.risuscito;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.utils.FastAdapterDiffUtil;
import com.mikepenz.fastadapter.listeners.OnClickListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import it.cammino.risuscito.adapters.FastScrollIndicatorAdapter;
import it.cammino.risuscito.database.RisuscitoDatabase;
import it.cammino.risuscito.database.SalmoCanto;
import it.cammino.risuscito.database.dao.CantoDao;
import it.cammino.risuscito.database.dao.CustomListDao;
import it.cammino.risuscito.database.dao.ListePersDao;
import it.cammino.risuscito.database.entities.Canto;
import it.cammino.risuscito.database.entities.ListaPers;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.items.SimpleItem;
import it.cammino.risuscito.ui.HFFragment;
import it.cammino.risuscito.utils.ListeUtils;
import it.cammino.risuscito.viewmodels.SalmiIndexViewModel;

public class SalmiSectionFragment extends HFFragment
    implements View.OnCreateContextMenuListener, SimpleDialogFragment.SimpleCallback {

  private final int ID_FITTIZIO = 99999999;

  FastScrollIndicatorAdapter<SimpleItem> mAdapter;

  @BindView(R.id.cantiList)
  RecyclerView mRecyclerView;

  //  @BindView(R.id.dragScrollBar_salmi)
  //  DragScrollBar mDragScrollBar;

  private SalmiIndexViewModel mCantiViewModel;
  // create boolean for fetching data
  private boolean isViewShown = true;
  //  private DatabaseCanti listaCanti;
  private String titoloDaAgg;
  //  private int idDaAgg;
  //  private int idListaDaAgg;
  //  private int posizioneDaAgg;
  //  private ListaPersonalizzata[] listePers;
  //  private int[] idListe;
  private List<ListaPers> listePersonalizzate;
  //  private int idListaClick;
  //  private int idPosizioneClick;
  private View rootView;
  private long mLastClickTime = 0;
  private LUtils mLUtils;
  private Unbinder mUnbinder;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    rootView = inflater.inflate(R.layout.index_fragment, container, false);
    mUnbinder = ButterKnife.bind(this, rootView);

    mCantiViewModel = ViewModelProviders.of(this).get(SalmiIndexViewModel.class);

    // crea un istanza dell'oggetto DatabaseCanti
    //    if (listaCanti == null) listaCanti = new DatabaseCanti(getActivity());
    //
    //    SQLiteDatabase db = listaCanti.getReadableDatabase();
    //
    //    // lancia la ricerca di tutti i titoli presenti in DB e li dispone in ordine alfabetico
    //    String query =
    //        "SELECT B._id, A.titolo_salmo, B.color, B.pagina, A.num_salmo, B.source"
    //            + "		FROM SALMI_MUSICA A"
    //            + "          , ELENCO B"
    //            + "       WHERE A._id = B._id"
    //            + "		ORDER BY A.num_salmo ASC, A.titolo_salmo ASC";
    //    Cursor lista = db.rawQuery(query, null);
    //
    //    // recupera il numero di record trovati
    //    int total = lista.getCount();

    // crea un array e ci memorizza i titoli estratti
    //    List<SimpleItem> mItems = new ArrayList<>();

    //    lista.moveToFirst();
    //    for (int i = 0; i < total; i++) {
    //      SimpleItem sampleItem = new SimpleItem();
    //      sampleItem
    //          .withTitle(lista.getString(1))
    //          .withPage(String.valueOf(lista.getInt(3)))
    //          .withSource(lista.getString(5))
    //          .withColor(lista.getString(2))
    //          .withId(lista.getInt(0))
    //          .withNumSalmo(lista.getString(4))
    //          .withContextMenuListener(SalmiSectionFragment.this);
    //      mItems.add(sampleItem);
    //      lista.moveToNext();
    //    }
    //
    //    // chiude il cursore
    //    lista.close();

    OnClickListener<SimpleItem> mOnClickListener =
        new OnClickListener<SimpleItem>() {
          @Override
          public boolean onClick(View view, IAdapter<SimpleItem> iAdapter, SimpleItem item, int i) {
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

    //    mDragScrollBar.setIndicator(new CustomIndicator(getActivity()), true);

    //    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    mAdapter = new FastScrollIndicatorAdapter<>(2);
    mAdapter.withOnClickListener(mOnClickListener).setHasStableIds(true);
    FastAdapterDiffUtil.set(mAdapter, mCantiViewModel.titoli);
    mRecyclerView.setAdapter(mAdapter);
    LinearLayoutManager llm = new LinearLayoutManager(getContext());
    mRecyclerView.setLayoutManager(llm);
    mRecyclerView.setHasFixedSize(true);
    DividerItemDecoration insetDivider =
        new DividerItemDecoration(getContext(), llm.getOrientation());
    insetDivider.setDrawable(
        ContextCompat.getDrawable(getContext(), R.drawable.material_inset_divider));
    mRecyclerView.addItemDecoration(insetDivider);

    mLUtils = LUtils.getInstance(getActivity());

    //    if (savedInstanceState != null) {
    //      Log.d(getClass().getName(), "onCreateView: RESTORING");
    //      idDaAgg = savedInstanceState.getInt("mCantiViewModel.idDaAgg", 0);
    //      idPosizioneClick = savedInstanceState.getInt("mCantiViewModel.idPosizioneClick", 0);
    //      idListaClick = savedInstanceState.getInt("mCantiViewModel.idListaClick", 0);
    //      idListaDaAgg = savedInstanceState.getInt("mCantiViewModel.idListaDaAgg", 0);
    //      posizioneDaAgg = savedInstanceState.getInt("mCantiViewModel.posizioneDaAgg", 0);
    SimpleDialogFragment sFragment =
        SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "SALMI_REPLACE");
    if (sFragment != null) sFragment.setmCallback(SalmiSectionFragment.this);
    sFragment =
        SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "SALMI_REPLACE_2");
    if (sFragment != null) sFragment.setmCallback(SalmiSectionFragment.this);
    //    }

    if (!isViewShown) {
      //      query = "SELECT _id, lista" + "		FROM LISTE_PERS" + "		ORDER BY _id ASC";
      //      lista = db.rawQuery(query, null);
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
      //      db.close();
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

    return rootView;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mUnbinder.unbind();
  }

  //  /** @param outState Bundle in which to place your saved state. */
  //  @Override
  //  public void onSaveInstanceState(@NonNull Bundle outState) {
  //    super.onSaveInstanceState(outState);
  //    outState.putInt("mCantiViewModel.idDaAgg", mCantiViewModel.idDaAgg);
  //    outState.putInt("mCantiViewModel.idPosizioneClick", mCantiViewModel.idPosizioneClick);
  //    outState.putInt("mCantiViewModel.idListaClick", mCantiViewModel.idListaClick);
  //    outState.putInt("mCantiViewModel.idListaDaAgg", mCantiViewModel.idListaDaAgg);
  //    outState.putInt("mCantiViewModel.posizioneDaAgg", mCantiViewModel.posizioneDaAgg);
  //  }

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
    mCantiViewModel.idDaAgg =
        Integer.valueOf(((TextView) v.findViewById(R.id.text_id_canto)).getText().toString());
    menu.setHeaderTitle("Aggiungi canto a:");

    //    for (int i = 0; i < idListe.length; i++) {
    //      SubMenu subMenu = menu.addSubMenu(ID_FITTIZIO, Menu.NONE, 10 + i,
    // listePers[i].getName());
    //      for (int k = 0; k < listePers[i].getNumPosizioni(); k++) {
    //        subMenu.add(100 + i, k, k, listePers[i].getNomePosizione(k));
    //      }
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
          ListeUtils.addToFavorites(getContext(), rootView, mCantiViewModel.idDaAgg);
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
          ListeUtils.addToListaDup(getContext(), rootView, 2, 3, mCantiViewModel.idDaAgg);
          return true;
        case R.id.add_to_e_vino:
          //          addToListaDup(2, 4);
          ListeUtils.addToListaDup(getContext(), rootView, 2, 4, mCantiViewModel.idDaAgg);
          return true;
        case R.id.add_to_e_fine:
          addToListaNoDup(2, 5);
          return true;
        default:
          mCantiViewModel.idListaClick = item.getGroupId();
          mCantiViewModel.idPosizioneClick = item.getItemId();
          if (mCantiViewModel.idListaClick != ID_FITTIZIO && mCantiViewModel.idListaClick >= 100) {
            mCantiViewModel.idListaClick -= 100;
            //            SQLiteDatabase db = listaCanti.getReadableDatabase();
            ////            if
            // (listePers[mCantiViewModel.idListaClick].getCantoPosizione(mCantiViewModel.idPosizioneClick).equals("")) {
            //
            // listePers[mCantiViewModel.idListaClick].addCanto(String.valueOf(mCantiViewModel.idDaAgg),
            // mCantiViewModel.idPosizioneClick);
            //              ContentValues values = new ContentValues();
            //              values.put("lista",
            // ListaPersonalizzata.serializeObject(listePers[mCantiViewModel.idListaClick]));
            //              db.update("LISTE_PERS", values, "_id = " +
            // idListe[mCantiViewModel.idListaClick], null);
            //              Snackbar.make(rootView, R.string.list_added,
            // Snackbar.LENGTH_SHORT).show();
            if (listePersonalizzate
                .get(mCantiViewModel.idListaClick)
                .lista
                .getCantoPosizione(mCantiViewModel.idPosizioneClick)
                .equals("")) {
              listePersonalizzate
                  .get(mCantiViewModel.idListaClick)
                  .lista
                  .addCanto(
                      String.valueOf(mCantiViewModel.idDaAgg), mCantiViewModel.idPosizioneClick);
              new Thread(
                      new Runnable() {
                        @Override
                        public void run() {
                          ListePersDao mDao =
                              RisuscitoDatabase.getInstance(getContext()).listePersDao();
                          mDao.updateLista(listePersonalizzate.get(mCantiViewModel.idListaClick));
                          Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT)
                              .show();
                        }
                      })
                  .start();
            } else {
              //              if (listePers[mCantiViewModel.idListaClick]
              if (listePersonalizzate
                  .get(mCantiViewModel.idListaClick)
                  .lista
                  .getCantoPosizione(mCantiViewModel.idPosizioneClick)
                  .equals(String.valueOf(mCantiViewModel.idDaAgg))) {
                Snackbar.make(rootView, R.string.present_yet, Snackbar.LENGTH_SHORT).show();
              } else {
                Log.d(getClass().getName(), "id presente: " + mCantiViewModel.idPosizioneClick);
                //                // recupero titolo del canto presente
                //                String query =
                //                    "SELECT titolo"
                //                        + "		FROM ELENCO"
                //                        + "		WHERE _id = "
                //                        +
                // listePers[mCantiViewModel.idListaClick].getCantoPosizione(mCantiViewModel.idPosizioneClick);
                //                Cursor cursor = db.rawQuery(query, null);
                //                cursor.moveToFirst();
                //                new SimpleDialogFragment.Builder(
                //                        (AppCompatActivity) getActivity(),
                //                        SalmiSectionFragment.this,
                //                        "SALMI_REPLACE")
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
                //              }
                //            }
                //            db.close();
                new Thread(
                        new Runnable() {
                          @Override
                          public void run() {
                            CantoDao mDao = RisuscitoDatabase.getInstance(getContext()).cantoDao();
                            Canto cantoPresente =
                                mDao.getCantoById(
                                    Integer.parseInt(
                                        listePersonalizzate
                                            .get(mCantiViewModel.idListaClick)
                                            .lista
                                            .getCantoPosizione(mCantiViewModel.idPosizioneClick)));
                            new SimpleDialogFragment.Builder(
                                    (AppCompatActivity) getActivity(),
                                    SalmiSectionFragment.this,
                                    "SALMI_REPLACE")
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
            return true;
          } else return super.onContextItemSelected(item);
      }
    } else return false;
  }

  // aggiunge il canto premuto ai preferiti
  //  public void addToFavorites() {
  //    SQLiteDatabase db = listaCanti.getReadableDatabase();
  //    String sql = "UPDATE ELENCO" + "  SET favourite = 1" + "   WHERE _id = " +
  // mCantiViewModel.idDaAgg;
  //    db.execSQL(sql);
  //    db.close();
  //    Snackbar.make(rootView, R.string.favorite_added, Snackbar.LENGTH_SHORT).show();
  //  }

  // aggiunge il canto premuto ad una lista e in una posizione che ammetta duplicati
  //  public void addToListaDup(int idLista, int listPosition) {
  //    SQLiteDatabase db = listaCanti.getReadableDatabase();
  //
  //    String sql = "INSERT INTO CUST_LISTS ";
  //    sql += "VALUES (" + idLista + ", " + listPosition + ", " + mCantiViewModel.idDaAgg + ",
  // CURRENT_TIMESTAMP)";
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
  //
  //  // aggiunge il canto premuto ad una lista e in una posizione che NON ammetta duplicati
  //  public void addToListaNoDup(int idLista, int listPosition) {
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
  //      if (titoloDaAgg.equalsIgnoreCase(titoloPresente)) {
  //        Snackbar.make(rootView, R.string.present_yet, Snackbar.LENGTH_SHORT).show();
  //      } else {
  //        mCantiViewModel.idListaDaAgg = idLista;
  //        mCantiViewModel.posizioneDaAgg = listPosition;
  //        new SimpleDialogFragment.Builder(
  //                (AppCompatActivity) getActivity(), SalmiSectionFragment.this, "SALMI_REPLACE_2")
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
  //        "INSERT INTO CUST_LISTS "
  //            + "VALUES ("
  //            + idLista
  //            + ", "
  //            + listPosition
  //            + ", "
  //            + mCantiViewModel.idDaAgg
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
      case "SALMI_REPLACE":
        //        SQLiteDatabase db = listaCanti.getReadableDatabase();
        //
        // listePers[mCantiViewModel.idListaClick].addCanto(String.valueOf(mCantiViewModel.idDaAgg),
        // mCantiViewModel.idPosizioneClick);
        //
        //        ContentValues values = new ContentValues();
        //        values.put("lista",
        // ListaPersonalizzata.serializeObject(listePers[mCantiViewModel.idListaClick]));
        //        db.update("LISTE_PERS", values, "_id = " + idListe[mCantiViewModel.idListaClick],
        // null);
        //        db.close();
        //        Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT).show();
        listePersonalizzate
            .get(mCantiViewModel.idListaClick)
            .lista
            .addCanto(String.valueOf(mCantiViewModel.idDaAgg), mCantiViewModel.idPosizioneClick);
        new Thread(
                new Runnable() {
                  @Override
                  public void run() {
                    ListePersDao mDao = RisuscitoDatabase.getInstance(getContext()).listePersDao();
                    mDao.updateLista(listePersonalizzate.get(mCantiViewModel.idListaClick));
                    Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT).show();
                  }
                })
            .start();
        break;
      case "SALMI_REPLACE_2":
        //        db = listaCanti.getReadableDatabase();
        //        String sql =
        //            "UPDATE CUST_LISTS "
        //                + "    SET id_canto = "
        //                + mCantiViewModel.idDaAgg
        //                + "    WHERE _id = "
        //                + mCantiViewModel.idListaDaAgg
        //                + "    AND position = "
        //                + mCantiViewModel.posizioneDaAgg;
        //        db.execSQL(sql);
        //        Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT).show();
        new Thread(
                new Runnable() {
                  @Override
                  public void run() {
                    CustomListDao mCustomListDao =
                        RisuscitoDatabase.getInstance(getContext()).customListDao();
                    mCustomListDao.updatePositionNoTimestamp(
                        mCantiViewModel.idDaAgg,
                        mCantiViewModel.idListaDaAgg,
                        mCantiViewModel.posizioneDaAgg);
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
                        getActivity(),
                        rootView,
                        idLista,
                        listPosition,
                        titoloDaAgg,
                        mCantiViewModel.idDaAgg);
                if (!titoloPresente.isEmpty()) {
                  mCantiViewModel.idListaDaAgg = idLista;
                  mCantiViewModel.posizioneDaAgg = listPosition;
                  new SimpleDialogFragment.Builder(
                          (AppCompatActivity) getActivity(),
                          SalmiSectionFragment.this,
                          "SALMI_REPLACE_2")
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

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    populateDb();
    subscribeUiFavorites();
  }

  private void populateDb() {
    mCantiViewModel.createDb();
  }

  private void subscribeUiFavorites() {
    mCantiViewModel
        .getIndexResult()
        .observe(
            this,
            new Observer<List<SalmoCanto>>() {
              @Override
              public void onChanged(@Nullable List<SalmoCanto> canti) {
                if (canti != null) {
                  mCantiViewModel.titoli.clear();
                  for (SalmoCanto canto : canti) {
                    SimpleItem sampleItem = new SimpleItem();
                    sampleItem
                        .withTitle(canto.titoloSalmo)
                        .withPage(String.valueOf(canto.pagina))
                        .withSource(canto.source)
                        .withColor(canto.color)
                        .withId(canto.id)
                        .withNumSalmo(canto.numSalmo)
                        .withContextMenuListener(SalmiSectionFragment.this);
                    mCantiViewModel.titoli.add(sampleItem);
                  }
                  FastAdapterDiffUtil.set(mAdapter, mCantiViewModel.titoli);
                }
              }
            });
  }
}
