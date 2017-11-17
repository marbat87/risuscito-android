package it.cammino.risuscito;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialcab.MaterialCab;
import com.google.firebase.crash.FirebaseCrash;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.fastadapter.listeners.OnLongClickListener;
import com.mikepenz.fastadapter_extensions.UndoHelper;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil;
import com.mikepenz.itemanimators.SlideLeftAlphaAnimator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import it.cammino.risuscito.database.CantoCronologia;
import it.cammino.risuscito.database.RisuscitoDatabase;
import it.cammino.risuscito.database.dao.CronologiaDao;
import it.cammino.risuscito.database.entities.Cronologia;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.items.SimpleHistoryItem;
import it.cammino.risuscito.utils.ThemeUtils;

public class HistoryFragment extends Fragment
    implements SimpleDialogFragment.SimpleCallback, MaterialCab.Callback {

  private final String TAG = getClass().getCanonicalName();

  @BindView(R.id.history_recycler)
  RecyclerView mRecyclerView;

  @BindView(R.id.no_history)
  View mNoHistory;

  //  private CronologiaViewModel mCronologiaViewModel;
  private List<SimpleHistoryItem> titoli;
  //    private DatabaseCanti listaCanti;
  private FastItemAdapter<SimpleHistoryItem> cantoAdapter;

  private boolean actionModeOk;

  private UndoHelper mUndoHelper;
  private MainActivity mMainActivity;
  private LUtils mLUtils;
  private long mLastClickTime = 0;
  private Unbinder mUnbinder;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.layout_history, container, false);
    mUnbinder = ButterKnife.bind(this, rootView);

    mMainActivity = (MainActivity) getActivity();
    mMainActivity.setupToolbarTitle(R.string.title_activity_history);

    mMainActivity.mTabLayout.setVisibility(View.GONE);

    // crea un istanza dell'oggetto DatabaseCanti
    //        listaCanti = new DatabaseCanti(getActivity());

    mLUtils = LUtils.getInstance(getActivity());

    mMainActivity.enableFab(true);
    if (!mMainActivity.isOnTablet()) mMainActivity.enableBottombar(false);
    FloatingActionButton fabClear = getActivity().findViewById(R.id.fab_pager);
    IconicsDrawable icon =
        new IconicsDrawable(getActivity())
            .icon(CommunityMaterial.Icon.cmd_eraser_variant)
            .color(Color.WHITE)
            .sizeDp(24)
            .paddingDp(2);
    fabClear.setImageDrawable(icon);
    fabClear.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            new SimpleDialogFragment.Builder(
                    (AppCompatActivity) getActivity(), HistoryFragment.this, "RESET_HISTORY")
                .title(R.string.dialog_reset_history_title)
                .content(R.string.dialog_reset_history_desc)
                .positiveButton(android.R.string.yes)
                .negativeButton(android.R.string.no)
                .show();
          }
        });

    if (!PreferenceManager.getDefaultSharedPreferences(getActivity())
        .getBoolean(Utility.HISTORY_OPEN, false)) {
      SharedPreferences.Editor editor =
          PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
      editor.putBoolean(Utility.HISTORY_OPEN, true);
      editor.apply();
      android.os.Handler mHandler = new android.os.Handler();
      mHandler.postDelayed(
          new Runnable() {
            @Override
            public void run() {
              Toast.makeText(getActivity(), getString(R.string.new_hint_remove), Toast.LENGTH_SHORT)
                  .show();
            }
          },
          250);
    }

    OnClickListener<SimpleHistoryItem> mOnPreClickListener =
        new OnClickListener<SimpleHistoryItem>() {
          @Override
          public boolean onClick(
              View view, IAdapter<SimpleHistoryItem> iAdapter, SimpleHistoryItem item, int i) {
            Log.d(TAG, "onClick: 2");
            if (mMainActivity.getMaterialCab().isActive()) {
              if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY_SELECTION)
                return true;
              mLastClickTime = SystemClock.elapsedRealtime();

              cantoAdapter
                  .getAdapterItem(i)
                  .withSetSelected(!cantoAdapter.getAdapterItem(i).isSelected());
              cantoAdapter.notifyAdapterItemChanged(i);
              if (cantoAdapter.getSelectedItems().size() == 0)
                mMainActivity.getMaterialCab().finish();
              return true;
            }
            return false;
          }
        };

    OnClickListener<SimpleHistoryItem> mOnClickListener =
        new OnClickListener<SimpleHistoryItem>() {
          @Override
          public boolean onClick(
              View view, IAdapter<SimpleHistoryItem> iAdapter, SimpleHistoryItem item, int i) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY) return true;
            mLastClickTime = SystemClock.elapsedRealtime();
            Bundle bundle = new Bundle();
            bundle.putCharSequence("pagina", item.getSource().getText());
            bundle.putInt("idCanto", item.getId());

            // lancia l'activity che visualizza il canto passando il parametro creato
            startSubActivity(bundle, view);
            return true;
          }
        };

    OnLongClickListener<SimpleHistoryItem> mOnPreLongClickListener =
        new OnLongClickListener<SimpleHistoryItem>() {
          @Override
          public boolean onLongClick(
              View view, IAdapter<SimpleHistoryItem> iAdapter, SimpleHistoryItem item, int i) {
            if (mMainActivity.getMaterialCab().isActive()) return true;
            if (!mMainActivity.isOnTablet() && mMainActivity.getAppBarLayout() != null)
              mMainActivity.getAppBarLayout().setExpanded(true, true);
            mMainActivity.getMaterialCab().start(HistoryFragment.this);
            cantoAdapter.getAdapterItem(i).withSetSelected(true);
            cantoAdapter.notifyAdapterItemChanged(i);
            return true;
          }
        };

    titoli = new ArrayList<>();
    // Creating new adapter object
    cantoAdapter = new FastItemAdapter<>();
    cantoAdapter
        .withSelectable(true)
        .withMultiSelect(true)
        .withSelectOnLongClick(true)
        .withOnPreClickListener(mOnPreClickListener)
        .withOnClickListener(mOnClickListener)
        .withOnPreLongClickListener(mOnPreLongClickListener)
        .setHasStableIds(true);
    //        cantoAdapter.add(titoli);

    mRecyclerView.setAdapter(cantoAdapter);
    LinearLayoutManager llm = new LinearLayoutManager(getContext());
    mRecyclerView.setLayoutManager(llm);
    mRecyclerView.setHasFixedSize(true);
    DividerItemDecoration insetDivider =
        new DividerItemDecoration(getContext(), llm.getOrientation());
    insetDivider.setDrawable(
        ContextCompat.getDrawable(getContext(), R.drawable.material_inset_divider));
    mRecyclerView.addItemDecoration(insetDivider);
    mRecyclerView.setItemAnimator(new SlideLeftAlphaAnimator());

    //noinspection unchecked
    mUndoHelper =
        new UndoHelper(
            cantoAdapter,
            new UndoHelper.UndoListener<SimpleHistoryItem>() {
              @Override
              public void commitRemove(
                  Set<Integer> set,
                  ArrayList<FastAdapter.RelativeInfo<SimpleHistoryItem>> arrayList) {
                Log.d(TAG, "commitRemove: " + arrayList.size());
                //                SQLiteDatabase db = listaCanti.getReadableDatabase();
                //                for (Object item : arrayList) {
                //                  SimpleHistoryItem mItem =
                //                      (SimpleHistoryItem) ((FastAdapter.RelativeInfo) item).item;
                //                  db.delete("CRONOLOGIA", "id_canto =  " + mItem.getId(), null);
                //                }
                //                db.close();
                //                mNoHistory.setVisibility(
                //                    cantoAdapter.getAdapterItemCount() > 0 ? View.INVISIBLE :
                // View.VISIBLE);
                //                mMainActivity.enableFab(cantoAdapter.getAdapterItemCount() != 0);
                //                new Thread(
                //                        new Runnable() {
                //                          @Override
                //                          public void run() {
                //                            List<Cronologia> removeList = new ArrayList<>();
                //                            for (FastAdapter.RelativeInfo<SimpleHistoryItem> item
                // : arrayList) {
                //                              SimpleHistoryItem mItem = item.item;
                //                              Cronologia cronologia = new Cronologia();
                //                              cronologia.idCanto = mItem.getId();
                //                              removeList.add(cronologia);
                //                            }
                //                            CronologiaDao mDao =
                //
                // RisuscitoDatabase.getInstance(getContext()).cronologiaDao();
                //                            mDao.deleteCronologia(removeList);
                //                            mNoHistory.setVisibility(
                //                                cantoAdapter.getAdapterItemCount() > 0
                //                                    ? View.INVISIBLE
                //                                    : View.VISIBLE);
                //
                // mMainActivity.enableFab(cantoAdapter.getAdapterItemCount() != 0);
                //                          }
                //                        })
                //                    .start();
                //noinspection unchecked
                new RemoveHistoryTask().execute(arrayList);
              }
            });

    //    mCronologiaViewModel = ViewModelProviders.of(this).get(CronologiaViewModel.class);
    //    populateDb();
    //    subscribeUiFavorites();

    return rootView;
  }

  @Override
  public void onResume() {
    updateHistoryList();
    super.onResume();
  }

  //    @Override
  //    public void onDestroy() {
  //        if (listaCanti != null)
  //            listaCanti.close();
  //        super.onDestroy();
  //    }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mUnbinder.unbind();
  }

  //    private void updateHistoryList() {
  //
  //        // crea un manipolatore per il Database in modalità READ
  //        SQLiteDatabase db = listaCanti.getReadableDatabase();
  //
  //        // lancia la ricerca della cronologia
  //        String query = "SELECT A._id, A.titolo, A.color, A.pagina, A.source, B.ultima_visita" +
  //                "		FROM ELENCO A" +
  //                "          , CRONOLOGIA B" +
  //                "		WHERE A._id = B.id_canto" +
  //                "		ORDER BY B.ultima_visita DESC";
  //        Cursor lista = db.rawQuery(query, null);
  //
  //        // crea un array e ci memorizza i titoli estratti
  //        List<SimpleHistoryItem> titoli = new ArrayList<>();
  //        lista.moveToFirst();
  //        for (int i = 0; i < lista.getCount(); i++) {
  //            SimpleHistoryItem sampleItem = new SimpleHistoryItem();
  //            sampleItem
  //                    .withTitle(lista.getString(1))
  //                    .withPage(String.valueOf(lista.getInt(3)))
  //                    .withSource(lista.getString(4))
  //                    .withColor(lista.getString(2))
  //                    .withTimestamp(lista.getString(5))
  //                    .withId(lista.getInt(0))
  //                    .withSelectedColor(getThemeUtils().primaryColorDark());
  //            titoli.add(sampleItem);
  //            lista.moveToNext();
  //        }
  //        // chiude il cursore
  //        lista.close();
  //        db.close();
  //
  //        OnClickListener<SimpleHistoryItem> mOnPreClickListener = new
  // OnClickListener<SimpleHistoryItem>() {
  //            @Override
  //            public boolean onClick(View view, IAdapter<SimpleHistoryItem> iAdapter,
  // SimpleHistoryItem item, int i) {
  //                Log.d(TAG, "onClick: 2");
  //                if (mMainActivity.getMaterialCab().isActive()) {
  //                    if (SystemClock.elapsedRealtime() - mLastClickTime <
  // Utility.CLICK_DELAY_SELECTION)
  //                        return true;
  //                    mLastClickTime = SystemClock.elapsedRealtime();
  //
  // cantoAdapter.getAdapterItem(i).withSetSelected(!cantoAdapter.getAdapterItem(i).isSelected());
  //                    cantoAdapter.notifyAdapterItemChanged(i);
  //                    if (cantoAdapter.getSelectedItems().size() == 0)
  //                        mMainActivity.getMaterialCab().finish();
  //                    return true;
  //                }
  //                return false;
  //            }
  //        };
  //
  //        OnClickListener<SimpleHistoryItem> mOnClickListener = new
  // OnClickListener<SimpleHistoryItem>() {
  //            @Override
  //            public boolean onClick(View view, IAdapter<SimpleHistoryItem> iAdapter,
  // SimpleHistoryItem item, int i) {
  //                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
  //                    return true;
  //                mLastClickTime = SystemClock.elapsedRealtime();
  //                Bundle bundle = new Bundle();
  //                bundle.putCharSequence("pagina", item.getSource().getText());
  //                bundle.putInt("idCanto", item.getId());
  //
  //                // lancia l'activity che visualizza il canto passando il parametro creato
  //                startSubActivity(bundle, view);
  //                return true;
  //            }
  //        };
  //
  //        OnLongClickListener<SimpleHistoryItem> mOnPreLongClickListener = new
  // OnLongClickListener<SimpleHistoryItem>() {
  //            @Override
  //            public boolean onLongClick(View view, IAdapter<SimpleHistoryItem> iAdapter,
  // SimpleHistoryItem item, int i) {
  //                if (mMainActivity.getMaterialCab().isActive())
  //                    return true;
  //                if (!mMainActivity.isOnTablet() && mMainActivity.getAppBarLayout() != null)
  //                    mMainActivity.getAppBarLayout().setExpanded(true, true);
  //                mMainActivity.getMaterialCab().start(HistoryFragment.this);
  //                cantoAdapter.getAdapterItem(i).withSetSelected(true);
  //                cantoAdapter.notifyAdapterItemChanged(i);
  //                return true;
  //            }
  //        };
  //
  //        // Creating new adapter object
  //        cantoAdapter = new FastItemAdapter<>();
  //        cantoAdapter.withSelectable(true)
  //                .withMultiSelect(true)
  //                .withSelectOnLongClick(true)
  //                .withOnPreClickListener(mOnPreClickListener)
  //                .withOnClickListener(mOnClickListener)
  //                .withOnPreLongClickListener(mOnPreLongClickListener)
  //                .setHasStableIds(true);
  //        cantoAdapter.add(titoli);
  //
  //        mRecyclerView.setAdapter(cantoAdapter);
  //        LinearLayoutManager llm = new LinearLayoutManager(getContext());
  //        mRecyclerView.setLayoutManager(llm);
  //        mRecyclerView.setHasFixedSize(true);
  //        DividerItemDecoration insetDivider = new DividerItemDecoration(getContext(),
  // llm.getOrientation());
  //        insetDivider.setDrawable(ContextCompat.getDrawable(getContext(),
  // R.drawable.material_inset_divider));
  //        mRecyclerView.addItemDecoration(insetDivider);
  //        mRecyclerView.setItemAnimator(new SlideLeftAlphaAnimator());
  //
  //        //noinspection unchecked
  //        mUndoHelper = new UndoHelper(cantoAdapter, new
  // UndoHelper.UndoListener<SimpleHistoryItem>() {
  //            @Override
  //            public void commitRemove(Set<Integer> set,
  // ArrayList<FastAdapter.RelativeInfo<SimpleHistoryItem>> arrayList) {
  //                Log.d(TAG, "commitRemove: " + arrayList.size());
  //                SQLiteDatabase db = listaCanti.getReadableDatabase();
  //                for (Object item: arrayList) {
  //                    SimpleHistoryItem mItem = (SimpleHistoryItem)
  // ((FastAdapter.RelativeInfo)item).item;
  //                    db.delete("CRONOLOGIA", "id_canto =  " + mItem.getId(), null);
  //                }
  //                db.close();
  //                mNoHistory.setVisibility(cantoAdapter.getAdapterItemCount() > 0 ? View.INVISIBLE
  // : View.VISIBLE);
  //                mMainActivity.enableFab(cantoAdapter.getAdapterItemCount() != 0);
  ////                if (cantoAdapter.getAdapterItemCount() == 0)
  ////                    mMainActivity.enableFab(false);
  //            }
  //        });
  //
  //        //nel caso sia presente almeno un canto visitato di recente, viene nascosto il testo di
  // nessun canto presente
  //        mNoHistory.setVisibility(titoli.size() > 0 ? View.INVISIBLE : View.VISIBLE);
  //        mMainActivity.enableFab(titoli.size() != 0);
  ////        if (titoli.size() == 0)
  ////            mMainActivity.enableFab(false);
  ////        else
  ////            mMainActivity.enableFab(true);
  //    }

  private void updateHistoryList() {
    new UpdateHistoryTask().execute();
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    IconicsMenuInflaterUtil.inflate(
        getActivity().getMenuInflater(), getActivity(), R.menu.help_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
    //        getActivity().getMenuInflater().inflate(R.menu.help_menu, menu);
    //        menu.findItem(R.id.action_help).setIcon(
    //                new IconicsDrawable(getActivity(), CommunityMaterial.Icon.cmd_help_circle)
    //                        .sizeDp(24)
    //                        .paddingDp(2)
    //                        .color(Color.WHITE));
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_help:
        Toast.makeText(getActivity(), getString(R.string.new_hint_remove), Toast.LENGTH_SHORT)
            .show();
        return true;
    }
    return false;
  }

  private void startSubActivity(Bundle bundle, View view) {
    Intent intent = new Intent(getActivity(), PaginaRenderActivity.class);
    intent.putExtras(bundle);
    mLUtils.startActivityWithTransition(intent, view, Utility.TRANS_PAGINA_RENDER);
  }

  @Override
  public void onPositive(@NonNull String tag) {
    Log.d(getClass().getName(), "onPositive: " + tag);
    switch (tag) {
      case "RESET_HISTORY":
        //                SQLiteDatabase db = listaCanti.getReadableDatabase();
        //                db.delete("CRONOLOGIA", null, null);
        //                db.close();
        //                updateHistoryList();
        new Thread(
                new Runnable() {
                  @Override
                  public void run() {
                    CronologiaDao mDao =
                        RisuscitoDatabase.getInstance(getContext()).cronologiaDao();
                    mDao.emptyCronologia();
                    updateHistoryList();
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

  @Override
  public boolean onCabCreated(MaterialCab cab, Menu menu) {
    Log.d(TAG, "onCabCreated: ");
    cab.setMenu(R.menu.menu_delete);
    cab.setTitle("");
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
    Log.d(TAG, "onCabCreated: ");
    switch (item.getItemId()) {
      case R.id.action_remove_item:
        int iRemoved = cantoAdapter.getSelectedItems().size();
        Log.d(TAG, "onCabItemClicked: " + iRemoved);
        mUndoHelper.remove(
            getActivity().findViewById(R.id.main_content),
            getResources().getQuantityString(R.plurals.histories_removed, iRemoved, iRemoved),
            getString(android.R.string.cancel).toUpperCase(),
            Snackbar.LENGTH_SHORT,
            cantoAdapter.getSelections());
        cantoAdapter.deselect();
        actionModeOk = true;
        mMainActivity.getMaterialCab().finish();
        return true;
    }
    return false;
  }

  @Override
  public boolean onCabFinished(MaterialCab cab) {
    Log.d(TAG, "onCabFinished: " + actionModeOk);
    if (!actionModeOk) {
      try {
        cantoAdapter.deselect();
      } catch (Exception e) {
        FirebaseCrash.log("Possibile crash");
      }
    }
    return true;
  }

  private ThemeUtils getThemeUtils() {
    return ((MainActivity) getActivity()).getThemeUtils();
  }

  //  private void populateDb() {
  //    mCronologiaViewModel.createDb();
  //  }
  //
  //  private void subscribeUiFavorites() {
  //    mCronologiaViewModel
  //        .getCronologiaResult()
  //        .observe(
  //            this,
  //            new Observer<List<CantoCronologia>>() {
  //              @Override
  //              public void onChanged(@Nullable List<CantoCronologia> canti) {
  //                Log.d(TAG, "onChanged: ");
  //                if (canti != null) {
  //                  List<SimpleHistoryItem> titoli = new ArrayList<>();
  //                  for (CantoCronologia canto : canti) {
  //                    SimpleHistoryItem sampleItem = new SimpleHistoryItem();
  //                    sampleItem
  //                        .withTitle(canto.titolo)
  //                        .withPage(String.valueOf(canto.pagina))
  //                        .withSource(canto.source)
  //                        .withColor(canto.color)
  //                        .withTimestamp(String.valueOf(canto.ultimaVisita.getTime()))
  //                        .withId(canto.id)
  //                        .withSelectedColor(getThemeUtils().primaryColorDark());
  //                    titoli.add(sampleItem);
  //                  }
  //
  //                  cantoAdapter.clear();
  //                  cantoAdapter.add(titoli);
  //                  cantoAdapter.notifyAdapterDataSetChanged();
  //
  //                  mNoHistory.setVisibility(titoli.size() > 0 ? View.INVISIBLE : View.VISIBLE);
  //                  mMainActivity.enableFab(titoli.size() != 0);
  //                }
  //              }
  //            });
  //  }

  private class UpdateHistoryTask extends AsyncTask<Void, Void, Integer> {

    @Override
    protected Integer doInBackground(Void... sSearchText) {

      CronologiaDao mDao = RisuscitoDatabase.getInstance(getContext()).cronologiaDao();
      List<CantoCronologia> canti = mDao.getCronologia();
      if (canti != null) {
        titoli = new ArrayList<>();
        for (CantoCronologia canto : canti) {
          SimpleHistoryItem sampleItem = new SimpleHistoryItem();
          sampleItem
              .withTitle(canto.titolo)
              .withPage(String.valueOf(canto.pagina))
              .withSource(canto.source)
              .withColor(canto.color)
              .withTimestamp(String.valueOf(canto.ultimaVisita.getTime()))
              .withId(canto.id)
              .withSelectedColor(getThemeUtils().primaryColorDark());
          titoli.add(sampleItem);
        }
      }
      return 0;
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      titoli.clear();
      cantoAdapter.clear();
    }

    @Override
    protected void onPostExecute(Integer result) {
      super.onPostExecute(result);
      cantoAdapter.add(titoli);
      cantoAdapter.notifyAdapterDataSetChanged();
      mNoHistory.setVisibility(
          cantoAdapter.getAdapterItemCount() > 0 ? View.INVISIBLE : View.VISIBLE);
      mMainActivity.enableFab(cantoAdapter.getAdapterItemCount() != 0);
    }
  }

  private class RemoveHistoryTask
      extends AsyncTask<ArrayList<FastAdapter.RelativeInfo<SimpleHistoryItem>>, Void, Integer> {

    @SafeVarargs
    @Override
    protected final Integer doInBackground(
            ArrayList<FastAdapter.RelativeInfo<SimpleHistoryItem>>... object) {

      List<Cronologia> removeList = new ArrayList<>();
      for (FastAdapter.RelativeInfo<SimpleHistoryItem> item : object[0]) {
        SimpleHistoryItem mItem = item.item;
        Cronologia cronologia = new Cronologia();
        cronologia.idCanto = mItem.getId();
        removeList.add(cronologia);
      }
      CronologiaDao mDao = RisuscitoDatabase.getInstance(getContext()).cronologiaDao();
      mDao.deleteCronologia(removeList);
      return 0;
    }

    @Override
    protected void onPostExecute(Integer result) {
      super.onPostExecute(result);
      mNoHistory.setVisibility(
          cantoAdapter.getAdapterItemCount() > 0 ? View.INVISIBLE : View.VISIBLE);
      mMainActivity.enableFab(cantoAdapter.getAdapterItemCount() != 0);
    }
  }
}
