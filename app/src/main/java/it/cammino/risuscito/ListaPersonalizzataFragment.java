package it.cammino.risuscito;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialcab.MaterialCab;
import com.crashlytics.android.Crashlytics;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import it.cammino.risuscito.adapters.PosizioneRecyclerAdapter;
import it.cammino.risuscito.database.RisuscitoDatabase;
import it.cammino.risuscito.database.dao.CantoDao;
import it.cammino.risuscito.database.dao.ListePersDao;
import it.cammino.risuscito.database.entities.Canto;
import it.cammino.risuscito.database.entities.ListaPers;
import it.cammino.risuscito.objects.PosizioneItem;
import it.cammino.risuscito.objects.PosizioneTitleItem;
import it.cammino.risuscito.ui.BottomSheetFragment;
import it.cammino.risuscito.ui.ThemeableActivity;
import it.cammino.risuscito.utils.ThemeUtils;

public class ListaPersonalizzataFragment extends Fragment implements MaterialCab.Callback {

  private static final int TAG_INSERT_PERS = 555;
  final String TAG = getClass().getCanonicalName();
  //    private DatabaseCanti listaCanti;
  String cantoDaCanc;

  @BindView(R.id.recycler_list)
  RecyclerView mRecyclerView;
  // create boolean for fetching data
  private boolean isViewShown = true;
  private int posizioneDaCanc;
  private View rootView;
  //    private SQLiteDatabase db;
  private int idLista;
  private ListaPersonalizzata listaPersonalizzata;
  private String listaPersonalizzataTitle;
  private boolean mSwhitchMode;
  private List<Pair<PosizioneTitleItem, List<PosizioneItem>>> posizioniList;
  private int longclickedPos, longClickedChild;
  private PosizioneRecyclerAdapter cantoAdapter;
  private boolean actionModeOk;
  private MainActivity mMainActivity;
  private LUtils mLUtils;
  private long mLastClickTime = 0;
  private Unbinder mUnbinder;

  @OnClick(R.id.button_pulisci)
  public void pulisciLista() {
    //				Log.i(getClass().toString(), "idLista: " + idLista);
    //        db = listaCanti.getReadableDatabase();
    //        ContentValues  values = new  ContentValues( );
    for (int i = 0; i < listaPersonalizzata.getNumPosizioni(); i++)
      listaPersonalizzata.removeCanto(i);
    //        values.put("lista" , ListaPersonalizzata.serializeObject(listaPersonalizzata));
    //        db.update("LISTE_PERS", values, "_id = " + idLista, null);
    //        db.close();
    //        updateLista();
    //        cantoAdapter.notifyDataSetChanged();
    runUpdate();
  }

  @OnClick(R.id.button_condividi)
  public void condividiLista() {
    //                Log.i(getClass().toString(), "idLista: " + idLista);
    BottomSheetFragment bottomSheetDialog =
        BottomSheetFragment.newInstance(R.string.share_by, getShareIntent());
    bottomSheetDialog.show(getFragmentManager(), null);
  }

  @OnClick(R.id.button_invia_file)
  public void inviaLista() {
    Uri exportUri = mLUtils.listToXML(listaPersonalizzata);
    Log.d(TAG, "onClick: exportUri = " + exportUri);
    if (exportUri != null) {
      BottomSheetFragment bottomSheetDialog =
          BottomSheetFragment.newInstance(R.string.share_by, getSendIntent(exportUri));
      bottomSheetDialog.show(getFragmentManager(), null);
    } else
      Snackbar.make(
              getActivity().findViewById(R.id.main_content),
              R.string.xml_error,
              Snackbar.LENGTH_LONG)
          .show();
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    rootView = inflater.inflate(R.layout.activity_lista_personalizzata, container, false);
    mUnbinder = ButterKnife.bind(this, rootView);

    mMainActivity = (MainActivity) getActivity();

    // crea un istanza dell'oggetto DatabaseCanti
    //        listaCanti = new DatabaseCanti(getActivity());

    mLUtils = LUtils.getInstance(getActivity());
    mSwhitchMode = false;

    idLista = getArguments().getInt("idLista");

    //        db = listaCanti.getReadableDatabase();
    //        String query = "SELECT lista" +
    //                "  FROM LISTE_PERS" +
    //                "  WHERE _id =  " + idLista;
    //        Cursor cursor = db.rawQuery(query, null);
    //        // recupera l'oggetto lista personalizzata
    //        cursor.moveToFirst();
    //        listaPersonalizzata = (ListaPersonalizzata) ListaPersonalizzata.
    //                deserializeObject(cursor.getBlob(0));
    //        cursor.close();
    //
    //        updateLista();

    OnClickListener click =
        new OnClickListener() {
          @Override
          public void onClick(final View v) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY) return;
            mLastClickTime = SystemClock.elapsedRealtime();
            final View parent = (View) v.getParent().getParent();
            if (parent.findViewById(R.id.addCantoGenerico).getVisibility() == View.VISIBLE) {
              if (mSwhitchMode) {
                scambioConVuoto(
                    parent,
                    Integer.valueOf(
                        ((TextView) parent.findViewById(R.id.text_id_posizione))
                            .getText()
                            .toString()));
              } else {
                if (!mMainActivity.getMaterialCab().isActive()) {
                  Bundle bundle = new Bundle();
                  bundle.putInt("fromAdd", 0);
                  bundle.putInt("idLista", idLista);
                  bundle.putInt(
                      "position",
                      Integer.valueOf(
                          ((TextView) parent.findViewById(R.id.text_id_posizione))
                              .getText()
                              .toString()));
                  Intent intent = new Intent(getActivity(), GeneralInsertSearch.class);
                  intent.putExtras(bundle);
                  getParentFragment().startActivityForResult(intent, TAG_INSERT_PERS + idLista);
                  getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.hold_on);
                }
              }
            } else {
              if (!mSwhitchMode)
                if (mMainActivity.getMaterialCab().isActive()) {
                  posizioneDaCanc =
                      Integer.valueOf(
                          ((TextView) parent.findViewById(R.id.text_id_posizione))
                              .getText()
                              .toString());
                  snackBarRimuoviCanto(v);
                } else openPagina(v);
              else {
                scambioCanto(
                    v,
                    Integer.valueOf(
                        ((TextView) parent.findViewById(R.id.text_id_posizione))
                            .getText()
                            .toString()));
              }
            }
          }
        };

    OnLongClickListener longClick =
        new OnLongClickListener() {
          @Override
          public boolean onLongClick(View v) {
            View parent = (View) v.getParent().getParent();
            posizioneDaCanc =
                Integer.valueOf(
                    ((TextView) parent.findViewById(R.id.text_id_posizione)).getText().toString());
            snackBarRimuoviCanto(v);
            return true;
          }
        };

    // Creating new adapter object
    posizioniList = new ArrayList<>();
    cantoAdapter =
        new PosizioneRecyclerAdapter(
            getThemeUtils().primaryColorDark(), posizioniList, click, longClick);
    mRecyclerView.setAdapter(cantoAdapter);

    // Setting the layoutManager
    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    new UpdateListTask(ListaPersonalizzataFragment.this).execute();

    if (!isViewShown) {
      if (mMainActivity.getMaterialCab().isActive()) mMainActivity.getMaterialCab().finish();
      FloatingActionButton fab1 = ((CustomLists) getParentFragment()).getFab();
      fab1.show();
    }

    return rootView;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mUnbinder.unbind();
  }

  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (isVisibleToUser) {
      if (getView() != null) {
        isViewShown = true;
        if (mMainActivity.getMaterialCab().isActive()) mMainActivity.getMaterialCab().finish();
        FloatingActionButton fab1 = ((CustomLists) getParentFragment()).getFab();
        fab1.show();
      } else isViewShown = false;
    }
  }

  //    @Override
  //    public void onActivityResult(int requestCode, int resultCode, Intent data) {
  ////        Log.i(getClass().getName(), "requestCode: " + requestCode);
  //        if (requestCode == TAG_INSERT_PERS + idLista && resultCode == Activity.RESULT_OK) {
  ////            Log.i("LISTA PERS", "ON RESUME");
  //            idLista = getArguments().getInt("idLista");
  ////		Log.i("fragmentIndex", fragmentIndex+"");
  ////		Log.i("idLista", idLista+"");
  //
  //            db = listaCanti.getReadableDatabase();
  //
  //            String query = "SELECT lista" +
  //                    "  FROM LISTE_PERS" +
  //                    "  WHERE _id =  " + idLista;
  //            Cursor cursor = db.rawQuery(query, null);
  //
  //            // recupera l'oggetto lista personalizzata
  //            cursor.moveToFirst();
  //
  //            listaPersonalizzata = (ListaPersonalizzata) ListaPersonalizzata.
  //                    deserializeObject(cursor.getBlob(0));
  //
  //            cursor.close();
  //
  //            updateLista();
  //            cantoAdapter.notifyDataSetChanged();
  //        }
  //        super.onActivityResult(requestCode, resultCode, data);
  //    }

  @Override
  public void onDestroy() {
    //        if (listaCanti != null)
    //            listaCanti.close();
    if (mMainActivity.getMaterialCab().isActive()) mMainActivity.getMaterialCab().finish();
    super.onDestroy();
  }

  private Intent getShareIntent() {
    return new Intent(Intent.ACTION_SEND)
        .putExtra(Intent.EXTRA_TEXT, getTitlesList())
        .setType("text/plain");
  }

  private Intent getSendIntent(Uri exportUri) {
    return new Intent(Intent.ACTION_SEND)
        .putExtra(Intent.EXTRA_STREAM, exportUri)
        .setType("text/xml");
  }

  private void openPagina(View v) {
    // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da
    // visualizzare
    Bundle bundle = new Bundle();
    bundle.putString(
        "pagina", ((TextView) v.findViewById(R.id.text_source_canto)).getText().toString());
    bundle.putInt(
        "idCanto",
        Integer.valueOf(((TextView) v.findViewById(R.id.text_id_canto_card)).getText().toString()));

    Intent intent = new Intent(getActivity(), PaginaRenderActivity.class);
    intent.putExtras(bundle);
    mLUtils.startActivityWithTransition(intent, v, Utility.TRANS_PAGINA_RENDER);
  }

  //    private void updateLista() {
  //
  ////		Log.i("POSITION", fragmentIndex+" ");
  ////		Log.i("IDLISTA", idLista+" ");
  ////		Log.i("TITOLO", listaPersonalizzata.getName());
  //        if (posizioniList == null)
  //            posizioniList = new ArrayList<>();
  //        else
  //            posizioniList.clear();
  //
  //        for (int cantoIndex = 0; cantoIndex < listaPersonalizzata.getNumPosizioni();
  // cantoIndex++) {
  //            List<PosizioneItem> list = new ArrayList<>();
  //            if (listaPersonalizzata.getCantoPosizione(cantoIndex).length() > 0) {
  //                db = listaCanti.getReadableDatabase();
  //
  //                String query = "SELECT _id, titolo, pagina, color, source" +
  //                        "  FROM ELENCO" +
  //                        "  WHERE _id =  " + listaPersonalizzata.getCantoPosizione(cantoIndex);
  //                Cursor cursor = db.rawQuery(query, null);
  //                cursor.moveToFirst();
  //
  //                list.add(new PosizioneItem(
  //                        cursor.getInt(2)
  //                        , cursor.getString(1)
  //                        , cursor.getString(3)
  //                        , cursor.getInt(0)
  //                        , cursor.getString(4)
  //                        , ""));
  //
  //                cursor.close();
  //                db.close();
  //
  //            }
  //
  //            //noinspection unchecked
  //            Pair<PosizioneTitleItem, List<PosizioneItem>> result = new Pair(new
  // PosizioneTitleItem(listaPersonalizzata.getNomePosizione(cantoIndex)
  //                    , idLista
  //                    , cantoIndex
  //                    , cantoIndex
  //                    , false), list);
  //
  //            posizioniList.add(result);
  //        }
  //
  //    }

  private String getTitlesList() {

    Locale l =
        ThemeableActivity.getSystemLocalWrapper(getActivity().getResources().getConfiguration());
    StringBuilder result = new StringBuilder();

    // titolo
    result.append("-- ").append(listaPersonalizzata.getName().toUpperCase(l)).append(" --\n");

    // tutti i canti
    for (int i = 0; i < listaPersonalizzata.getNumPosizioni(); i++) {
      result.append(listaPersonalizzata.getNomePosizione(i).toUpperCase(l)).append("\n");
      if (!listaPersonalizzata.getCantoPosizione(i).equalsIgnoreCase("")) {
        for (PosizioneItem tempItem : posizioniList.get(i).second) {
          result
              .append(tempItem.getTitolo())
              .append(" - ")
              .append(getString(R.string.page_contracted))
              .append(tempItem.getPagina());
          result.append("\n");
        }
      } else {
        result.append(">> ").append(getString(R.string.to_be_chosen)).append(" <<");
        result.append("\n");
      }
      if (i < listaPersonalizzata.getNumPosizioni() - 1) result.append("\n");
    }

    return result.toString();
  }

  public void snackBarRimuoviCanto(View view) {
    if (mMainActivity.getMaterialCab().isActive()) mMainActivity.getMaterialCab().finish();
    View parent = (View) view.getParent().getParent();
    longclickedPos =
        Integer.valueOf(((TextView) parent.findViewById(R.id.tag)).getText().toString());
    longClickedChild =
        Integer.valueOf(((TextView) view.findViewById(R.id.item_tag)).getText().toString());
    if (!mMainActivity.isOnTablet() && mMainActivity.getAppBarLayout() != null)
      mMainActivity.getAppBarLayout().setExpanded(true, true);
    mMainActivity.getMaterialCab().start(ListaPersonalizzataFragment.this);
  }

  private ThemeUtils getThemeUtils() {
    return ((MainActivity) getActivity()).getThemeUtils();
  }

  private void scambioCanto(View v, int posizioneNew) {
    //        Log.i(getClass().toString(), "positioneNew: " + posizioneNew);
    //        Log.i(getClass().toString(), "posizioneDaCanc: " + posizioneDaCanc);
    if (posizioneNew != posizioneDaCanc) {

      String cantoTmp = listaPersonalizzata.getCantoPosizione(posizioneNew);
      listaPersonalizzata.addCanto(
          listaPersonalizzata.getCantoPosizione(posizioneDaCanc), posizioneNew);
      listaPersonalizzata.addCanto(cantoTmp, posizioneDaCanc);

      runUpdate();

      //            db = listaCanti.getReadableDatabase();
      //            ContentValues  values = new  ContentValues( );
      //            values.put("lista", ListaPersonalizzata.serializeObject(listaPersonalizzata));
      //            db.update("LISTE_PERS", values, "_id = " + idLista, null);
      //            db.close();
      //
      //            updateLista();
      //            View parent = (View) v.getParent().getParent();
      //            cantoAdapter.notifyItemChanged(longclickedPos);
      //
      // cantoAdapter.notifyItemChanged(Integer.valueOf(((TextView)parent.findViewById(R.id.tag)).getText().toString()));
      actionModeOk = true;
      mMainActivity.getMaterialCab().finish();
      Snackbar.make(
              getActivity().findViewById(R.id.main_content),
              R.string.switch_done,
              Snackbar.LENGTH_SHORT)
          .show();

    } else Snackbar.make(rootView, R.string.switch_impossible, Snackbar.LENGTH_SHORT).show();
  }

  private void scambioConVuoto(View parent, int posizioneNew) {
    //        Log.i(getClass().toString(), "positioneNew: " + posizioneNew);
    //        Log.i(getClass().toString(), "posizioneDaCanc: " + posizioneDaCanc);
    listaPersonalizzata.addCanto(
        listaPersonalizzata.getCantoPosizione(posizioneDaCanc), posizioneNew);
    listaPersonalizzata.removeCanto(posizioneDaCanc);

    //        ListePersDao mDao = RisuscitoDatabase.getInstance(getContext()).listePersDao();
    //        mDao.updateListaNoTitle(listaPersonalizzata, idLista);
    runUpdate();

    //        db = listaCanti.getReadableDatabase();
    //        ContentValues  values = new  ContentValues( );
    //        values.put("lista", ListaPersonalizzata.serializeObject(listaPersonalizzata));
    //        db.update("LISTE_PERS", values, "_id = " + idLista, null);
    //        db.close();
    //
    //        updateLista();
    //        cantoAdapter.notifyItemChanged(longclickedPos);
    //        cantoAdapter.notifyItemChanged(Integer.valueOf(((TextView)
    // parent.findViewById(R.id.tag)).getText().toString()));
    actionModeOk = true;
    mMainActivity.getMaterialCab().finish();
    Snackbar.make(
            getActivity().findViewById(R.id.main_content),
            R.string.switch_done,
            Snackbar.LENGTH_SHORT)
        .show();
  }

  @Override
  public boolean onCabCreated(MaterialCab cab, Menu menu) {
    Log.d(TAG, "onCabCreated: ");
    cab.setMenu(R.menu.menu_actionmode_lists);
    cab.setTitle("");
    posizioniList.get(longclickedPos).second.get(longClickedChild).setmSelected(true);
    cantoAdapter.notifyItemChanged(longclickedPos);
    menu.findItem(R.id.action_switch_item)
        .setIcon(
            new IconicsDrawable(getActivity(), CommunityMaterial.Icon.cmd_shuffle)
                .sizeDp(24)
                .paddingDp(2)
                .colorRes(android.R.color.white));
    menu.findItem(R.id.action_remove_item)
        .setIcon(
            new IconicsDrawable(getActivity(), CommunityMaterial.Icon.cmd_delete)
                .sizeDp(24)
                .paddingDp(2)
                .colorRes(android.R.color.white));
    actionModeOk = false;
    return true;
  }

  @Override
  public boolean onCabItemClicked(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_remove_item:
        //                db = listaCanti.getReadableDatabase();
        //                ContentValues  values = new  ContentValues( );
        //                cantoDaCanc = listaPersonalizzata.getCantoPosizione(posizioneDaCanc);
        //                listaPersonalizzata.removeCanto(posizioneDaCanc);
        //                values.put("lista",
        // ListaPersonalizzata.serializeObject(listaPersonalizzata));
        //                db.update("LISTE_PERS", values, "_id = " + idLista, null);
        //                db.close();
        //                updateLista();
        //                cantoAdapter.notifyItemChanged(longclickedPos);
        cantoDaCanc = listaPersonalizzata.getCantoPosizione(posizioneDaCanc);
        listaPersonalizzata.removeCanto(posizioneDaCanc);
        runUpdate();
        actionModeOk = true;
        mMainActivity.getMaterialCab().finish();
        Snackbar.make(
                getActivity().findViewById(R.id.main_content),
                R.string.song_removed,
                Snackbar.LENGTH_LONG)
            .setAction(
                getString(android.R.string.cancel).toUpperCase(),
                new View.OnClickListener() {
                  @Override
                  public void onClick(View view) {
                    //                                db = listaCanti.getReadableDatabase();
                    //                                ContentValues  values = new  ContentValues( );
                    listaPersonalizzata.addCanto(cantoDaCanc, posizioneDaCanc);
                    //                                values.put("lista",
                    // ListaPersonalizzata.serializeObject(listaPersonalizzata));
                    //                                db.update("LISTE_PERS", values, "_id = " +
                    // idLista, null);
                    //                                db.close();
                    //                                updateLista();
                    //
                    // cantoAdapter.notifyItemChanged(longclickedPos);
                    runUpdate();
                  }
                })
            .setActionTextColor(getThemeUtils().accentColor())
            .show();
        mSwhitchMode = false;
        break;
      case R.id.action_switch_item:
        mSwhitchMode = true;
        //                db = listaCanti.getReadableDatabase();
        cantoDaCanc = listaPersonalizzata.getCantoPosizione(posizioneDaCanc);
        mMainActivity.getMaterialCab().setTitleRes(R.string.switch_started);
        Toast.makeText(
                getActivity(),
                getResources().getString(R.string.switch_tooltip),
                Toast.LENGTH_SHORT)
            .show();
        break;
    }
    return true;
  }

  @Override
  public boolean onCabFinished(MaterialCab cab) {
    mSwhitchMode = false;
    if (!actionModeOk) {
      try {
        posizioniList.get(longclickedPos).second.get(longClickedChild).setmSelected(false);
        cantoAdapter.notifyItemChanged(longclickedPos);
      } catch (Exception e) {
        //        FirebaseCrash.log("Possibile crash - longclickedPos: " + longclickedPos);
        Crashlytics.logException(e);
      }
    }
    return true;
  }

  private void runUpdate() {
    new Thread(
            new Runnable() {
              @Override
              public void run() {
                ListaPers listaNew = new ListaPers();
                listaNew.lista = listaPersonalizzata;
                listaNew.id = idLista;
                listaNew.titolo = listaPersonalizzataTitle;
                ListePersDao mDao = RisuscitoDatabase.getInstance(getContext()).listePersDao();
                mDao.updateLista(listaNew);
                new UpdateListTask(ListaPersonalizzataFragment.this).execute();
              }
            })
        .start();
    //        ListaPers listaNew = new ListaPers();
    //        listaNew.lista = listaPersonalizzata;
    //        listaNew.id = idLista;
    //        listaNew.titolo = listaPersonalizzataTitle;
    //        ListePersDao mDao = RisuscitoDatabase.getInstance(getContext()).listePersDao();
    //        mDao.updateLista(listaNew);
    //        new UpdateListTask().execute();
  }

  private static class UpdateListTask extends AsyncTask<Void, Void, Integer> {

    private WeakReference<ListaPersonalizzataFragment> fragmentReference;

    UpdateListTask(ListaPersonalizzataFragment fragment) {
      this.fragmentReference = new WeakReference<>(fragment);
    }

    @Override
    protected Integer doInBackground(Void... params) {

      ListePersDao mDao =
          RisuscitoDatabase.getInstance(fragmentReference.get().getActivity()).listePersDao();
      ListaPers listaPers = mDao.getListById(fragmentReference.get().idLista);

      fragmentReference.get().listaPersonalizzata = listaPers.lista;
      fragmentReference.get().listaPersonalizzataTitle = listaPers.titolo;

      for (int cantoIndex = 0;
          cantoIndex < fragmentReference.get().listaPersonalizzata.getNumPosizioni();
          cantoIndex++) {
        List<PosizioneItem> list = new ArrayList<>();
        if (fragmentReference.get().listaPersonalizzata.getCantoPosizione(cantoIndex).length()
            > 0) {

          CantoDao mCantoDao =
              RisuscitoDatabase.getInstance(fragmentReference.get().getActivity()).cantoDao();
          Canto cantoTemp =
              mCantoDao.getCantoById(
                  Integer.parseInt(
                      fragmentReference.get().listaPersonalizzata.getCantoPosizione(cantoIndex)));

          list.add(
              new PosizioneItem(
                  cantoTemp.pagina,
                  cantoTemp.titolo,
                  cantoTemp.color,
                  cantoTemp.id,
                  cantoTemp.source,
                  ""));
        }

        //noinspection unchecked
        Pair<PosizioneTitleItem, List<PosizioneItem>> result =
            new Pair(
                new PosizioneTitleItem(
                    fragmentReference.get().listaPersonalizzata.getNomePosizione(cantoIndex),
                    fragmentReference.get().idLista,
                    cantoIndex,
                    cantoIndex,
                    false),
                list);

        fragmentReference.get().posizioniList.add(result);
      }

      return 0;
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      fragmentReference.get().posizioniList.clear();
    }

    @Override
    protected void onPostExecute(Integer result) {
      fragmentReference.get().cantoAdapter.notifyDataSetChanged();
    }
  }
}
