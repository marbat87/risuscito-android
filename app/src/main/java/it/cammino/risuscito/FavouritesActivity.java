package it.cammino.risuscito;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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
import com.mikepenz.fastadapter_extensions.UndoHelper;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.itemanimators.SlideLeftAlphaAnimator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.items.SimpleItem;
import it.cammino.risuscito.utils.ThemeUtils;

public class FavouritesActivity extends Fragment implements SimpleDialogFragment.SimpleCallback, MaterialCab.Callback {

    private final String TAG = getClass().getCanonicalName();

    private DatabaseCanti listaCanti;
    //    private List<SimpleItem> titoli;
    //    private int posizDaCanc;
//    private List<SimpleItem> removedItems;
    //    private RecyclerView recyclerView;
    private FastItemAdapter<SimpleItem> cantoAdapter;
    private FloatingActionButton fabClear;
    //    private ActionMode mMode;
    private boolean actionModeOk;

//    private String PREFERITI_OPEN = "preferiti_open";

    private MainActivity mMainActivity;

    private LUtils mLUtils;

    private long mLastClickTime = 0;

    private UndoHelper mUndoHelper;

    @BindView(R.id.favouritesList) RecyclerView mRecyclerView;
    @BindView(R.id.no_favourites) View mNoFavorites;

    private Unbinder mUnbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_favourites, container, false);
        mUnbinder  = ButterKnife.bind(this, rootView);

        mMainActivity = (MainActivity) getActivity();
        Log.d(TAG, "onCreateView: isOnTablet " + mMainActivity.isOnTablet());

        mMainActivity.setupToolbarTitle(R.string.title_activity_favourites);

//        getActivity().findViewById(R.id.material_tabs).setVisibility(View.GONE);
        mMainActivity.mTabLayout.setVisibility(View.GONE);

        //crea un istanza dell'oggetto DatabaseCanti
        listaCanti = new DatabaseCanti(getActivity());

        mLUtils = LUtils.getInstance(getActivity());
//        mMode = null;

        mMainActivity.enableFab(true);
        if (!mMainActivity.isOnTablet()) {
//            mMainActivity.enableFab(true);
            mMainActivity.enableBottombar(false);
        }
//        fabClear = mMainActivity.isOnTablet() ? (FloatingActionButton) rootView.findViewById(R.id.fab_pager) :
//                (FloatingActionButton) getActivity().findViewById(R.id.fab_pager);
        fabClear = (FloatingActionButton) getActivity().findViewById(R.id.fab_pager);
        IconicsDrawable icon = new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_eraser_variant)
                .color(Color.WHITE)
                .sizeDp(24)
                .paddingDp(2);
        fabClear.setImageDrawable(icon);
        fabClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SimpleDialogFragment.Builder((AppCompatActivity)getActivity(), FavouritesActivity.this, "FAVORITES_RESET")
                        .title(R.string.dialog_reset_favorites_title)
                        .content(R.string.dialog_reset_favorites_desc)
                        .positiveButton(R.string.confirm)
                        .negativeButton(R.string.dismiss)
                        .show();
            }
        });

        if(!PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getBoolean(Utility.PREFERITI_OPEN, false)) {
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(getActivity())
                    .edit();
            editor.putBoolean(Utility.PREFERITI_OPEN, true);
            editor.apply();
            android.os.Handler mHandler = new android.os.Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), getString(R.string.new_hint_remove), Toast.LENGTH_SHORT).show();
                }
            }, 250);
        }

        SimpleDialogFragment sFragment = SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "FAVORITES_RESET");
        if (sFragment != null)
            sFragment.setmCallback(FavouritesActivity.this);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.help_menu, menu);
        menu.findItem(R.id.action_help).setIcon(
                new IconicsDrawable(getActivity(), CommunityMaterial.Icon.cmd_help_circle)
                        .sizeDp(24)
                        .paddingDp(2)
                        .color(Color.WHITE));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                Toast.makeText(getActivity(), getString(R.string.new_hint_remove), Toast.LENGTH_SHORT).show();
                return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        updateFavouritesList();
        super.onResume();
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

    private void updateFavouritesList() {

        // crea un manipolatore per il Database in modalità READ
        SQLiteDatabase db = listaCanti.getReadableDatabase();

        // lancia la ricerca dei preferiti
        String query = "SELECT titolo, color, pagina, _id, source" +
                "		FROM ELENCO" +
                "		WHERE favourite = 1" +
                "		ORDER BY TITOLO ASC";
        Cursor lista = db.rawQuery(query, null);

        // crea un array e ci memorizza i titoli estratti
        List<SimpleItem> titoli = new ArrayList<>();
        lista.moveToFirst();
        for (int i = 0; i < lista.getCount(); i++) {
//            titoli.add(new CantoRecycled(lista.getString(0)
//                    , lista.getInt(2)
//                    , lista.getString(1)
//                    , lista.getInt(3)
//                    , lista.getString(4)));
            SimpleItem sampleItem = new SimpleItem();
            sampleItem
                    .withTitle(lista.getString(0))
                    .withPage(String.valueOf(lista.getInt(2)))
                    .withSource(lista.getString(4))
                    .withColor(lista.getString(1))
                    .withId(lista.getInt(3))
                    .withSelectedColor(getThemeUtils().primaryColorDark());
//                    .withIdentifier(lista.getInt(3));
            titoli.add(sampleItem);
            lista.moveToNext();
        }

        // chiude il cursore
        lista.close();
        db.close();

//        View.OnClickListener clickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                if (mMode == null) {
//                if (!mMainActivity.getMaterialCab().isActive()) {
//                    if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
//                        return;
//                    mLastClickTime = SystemClock.elapsedRealtime();
//                    // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da visualizzare
//                    Bundle bundle = new Bundle();
//                    bundle.putString("pagina", String.valueOf(((TextView) v.findViewById(R.id.text_source_canto)).getText()));
//                    bundle.putInt("idCanto", Integer.parseInt(
//                            String.valueOf(((TextView) v.findViewById(R.id.text_id_canto)).getText())));
//                    // lancia l'activity che visualizza il canto passando il parametro creato
//                    startSubActivity(bundle, v);
//                }
//                else {
//                    int tempPos = mRecyclerView.getChildAdapterPosition(v);
//                    titoli.get(tempPos).setmSelected(!titoli.get(tempPos).ismSelected());
//                    cantoAdapter.notifyItemChanged(tempPos);
//                }
//            }
//        };

        FastAdapter.OnClickListener<SimpleItem> mOnPreClickListener = new FastAdapter.OnClickListener<SimpleItem>() {
            @Override
            public boolean onClick(View view, IAdapter<SimpleItem> iAdapter, SimpleItem item, int i) {
                Log.d(TAG, "onClick: 2");
                if (mMainActivity.getMaterialCab().isActive()) {
//                    cantoAdapter.select(i);
                    if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY_SELECTION)
                        return true;
                    mLastClickTime = SystemClock.elapsedRealtime();
                    cantoAdapter.getAdapterItem(i).withSetSelected(!cantoAdapter.getAdapterItem(i).isSelected());
                    cantoAdapter.notifyAdapterItemChanged(i);
                    if (cantoAdapter.getSelectedItems().size() == 0)
                        mMainActivity.getMaterialCab().finish();
                    return true;
                }
                return false;
            }
        };

        FastAdapter.OnClickListener<SimpleItem> mOnClickListener = new FastAdapter.OnClickListener<SimpleItem>() {
            @Override
            public boolean onClick(View view, IAdapter<SimpleItem> iAdapter, SimpleItem item, int i) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
                    return true;
                mLastClickTime = SystemClock.elapsedRealtime();
                Bundle bundle = new Bundle();
                bundle.putCharSequence("pagina", item.getSource().getText());
                bundle.putInt("idCanto", item.getId());

                // lancia l'activity che visualizza il canto passando il parametro creato
                startSubActivity(bundle, view);
                return true;
            }
        };

        FastAdapter.OnLongClickListener<SimpleItem> mOnPreLongClickListener = new FastAdapter.OnLongClickListener<SimpleItem>() {
            @Override
            public boolean onLongClick(View view, IAdapter<SimpleItem> iAdapter, SimpleItem item, int i) {
                if (mMainActivity.getMaterialCab().isActive())
                    return true;
                if (!mMainActivity.isOnTablet() && mMainActivity.getAppBarLayout() != null)
                    mMainActivity.getAppBarLayout().setExpanded(true, true);
                mMainActivity.getMaterialCab().start(FavouritesActivity.this);
//                cantoAdapter.select(i);
                cantoAdapter.getAdapterItem(i).withSetSelected(true);
                cantoAdapter.notifyAdapterItemChanged(i);
                return true;
            }
        };

//        UndoHelper.UndoListener<SimpleItem> mUndoListener = new UndoHelper.UndoListener<SimpleItem>() {
//            @Override
//            public void commitRemove(Set<Integer> set, ArrayList<FastAdapter.RelativeInfo<SimpleItem>> arrayList) {
//                Log.d(TAG, "commitRemove: " + arrayList.size());
//                SQLiteDatabase db = listaCanti.getReadableDatabase();
//                for (Object item: arrayList) {
//                    SimpleItem mItem = (SimpleItem) ((FastAdapter.RelativeInfo)item).item;
//                    ContentValues values = new ContentValues();
//                    values.put("favourite", 0);
//                    db.update("ELENCO", values, "_id =  " + mItem.getId(), null);
//                }
//                db.close();
//                mNoFavorites.setVisibility(cantoAdapter.getAdapterItemCount() > 0 ? View.INVISIBLE : View.VISIBLE);
//                if (cantoAdapter.getAdapterItemCount() == 0) {
//                    if (mMainActivity.isOnTablet())
//                        fabClear.hide();
//                    else
//                        mMainActivity.enableFab(false);
//                }
//            }
//        };

//        View.OnLongClickListener longClickListener  = new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                posizDaCanc = mRecyclerView.getChildAdapterPosition(v);
////                if (mMode == null)
////                    mMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ModeCallback());
////                else {
////                    mMode.finish();
////                    mMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ModeCallback());
////                }
//                Log.d(TAG, "onLongClick: mMainActivity.getMaterialCab().isActive()" + mMainActivity.getMaterialCab().isActive());
//                if (mMainActivity.getMaterialCab().isActive()) {
//                    mMainActivity.getMaterialCab().finish();
//                    mMainActivity.getAppBarLayout().setExpanded(true, true);
//                    mMainActivity.getMaterialCab().start(FavouritesActivity.this);
//                }
//                else
//                    mMainActivity.getAppBarLayout().setExpanded(true, true);
//                    mMainActivity.getMaterialCab().start(FavouritesActivity.this);
//                return true;
//            }
//        };

//        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.favouritesList);

        // Creating new adapter object
//        cantoAdapter = new CantoRecyclerAdapter(getActivity(), titoli, clickListener, longClickListener);
        cantoAdapter = new FastItemAdapter<>();
        cantoAdapter.withSelectable(true)
                .withMultiSelect(true)
                .withSelectOnLongClick(true)
                .withOnPreClickListener(mOnPreClickListener)
                .withOnClickListener(mOnClickListener)
                .withOnPreLongClickListener(mOnPreLongClickListener)
                .setHasStableIds(true);
        cantoAdapter.add(titoli);

//        mRecyclerView.setAdapter(cantoAdapter);
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        mRecyclerView.setItemAnimator(new SlideLeftAlphaAnimator());
        mRecyclerView.setAdapter(cantoAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(llm);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        DividerItemDecoration insetDivider = new DividerItemDecoration(getContext(), llm.getOrientation());
        insetDivider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.inset_divider_light));
        mRecyclerView.addItemDecoration(insetDivider);
        mRecyclerView.setItemAnimator(new SlideLeftAlphaAnimator());

        //noinspection unchecked
        mUndoHelper = new UndoHelper(cantoAdapter, new UndoHelper.UndoListener<SimpleItem>() {
            @Override
            public void commitRemove(Set<Integer> set, ArrayList<FastAdapter.RelativeInfo<SimpleItem>> arrayList) {
                Log.d(TAG, "commitRemove: " + arrayList.size());
                SQLiteDatabase db = listaCanti.getReadableDatabase();
                for (Object item: arrayList) {
                    SimpleItem mItem = (SimpleItem) ((FastAdapter.RelativeInfo)item).item;
                    ContentValues values = new ContentValues();
                    values.put("favourite", 0);
                    db.update("ELENCO", values, "_id =  " + mItem.getId(), null);
                }
                db.close();
                mNoFavorites.setVisibility(cantoAdapter.getAdapterItemCount() > 0 ? View.INVISIBLE : View.VISIBLE);
                if (cantoAdapter.getAdapterItemCount() == 0) {
//                    if (mMainActivity.isOnTablet())
//                        fabClear.hide();
//                    else
                    mMainActivity.enableFab(false);
                }
            }
        });

//nel caso sia presente almeno un preferito, viene nascosto il testo di nessun canto presente
//        rootView.findViewById(R.id.no_favourites).setVisibility(titoli.size() > 0 ? View.INVISIBLE : View.VISIBLE);
        mNoFavorites.setVisibility(titoli.size() > 0 ? View.INVISIBLE : View.VISIBLE);
        if (titoli.size() == 0) {
//            if (mMainActivity.isOnTablet())
//                fabClear.hide();
//            else
            mMainActivity.enableFab(false);
        }
        else {
//            if (mMainActivity.isOnTablet())
//                fabClear.show();
//            else
            mMainActivity.enableFab(true);
        }
    }

//    private ThemeUtils getThemeUtils() {
//        return mMainActivity.getThemeUtils();
//    }

//    private final class ModeCallback implements ActionMode.Callback {
//
//        @Override
//        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//            // Create the menu from the xml file
//            getActivity().getMenuInflater().inflate(R.menu.menu_delete, menu);
//            titoli.get(posizDaCanc).setmSelected(true);
//            cantoAdapter.notifyItemChanged(posizDaCanc);
//            removedItems = new ArrayList<>();
//            menu.findItem(R.id.action_remove_item).setIcon(
//                    new IconicsDrawable(getActivity(), CommunityMaterial.Icon.cmd_delete)
//                            .sizeDp(24)
//                            .paddingDp(2)
//                            .colorRes(R.color.icon_ative_black));
//            actionModeOk = false;
//            return true;
//        }
//
//        @Override
//        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//            // Here, you can checked selected items to adapt available actions
//            return false;
//        }
//
//        @Override
//        public void onDestroyActionMode(ActionMode mode) {
//            if (mode == mMode)
//                mMode = null;
//            Log.i(getClass().getName(), "actionModeOk: " + actionModeOk);
//            if (!actionModeOk) {
//                for (CantoRecycled canto : titoli) {
//                    canto.setmSelected(false);
//                    cantoAdapter.notifyDataSetChanged();
//                }
//            }
//        }
//
//        @Override
//        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//            switch(item.getItemId()) {
//                case R.id.action_remove_item:
//                    Log.i(getClass().getName(), "CLICKED");
//                    SQLiteDatabase db = listaCanti.getReadableDatabase();
//                    ContentValues values = new ContentValues();
//                    values.put("favourite", 0);
//                    for(int i = 0; i < titoli.size(); i++) {
//                        Log.d(getClass().getName(), "selezionato[" + i + "]: " + titoli.get(i).ismSelected());
//                        if (titoli.get(i).ismSelected()) {
//                            db.update("ELENCO", values, "_id =  " + titoli.get(i).getIdCanto(), null);
//                            titoli.get(i).setmSelected(false);
//                            removedItems.add(titoli.remove(i));
//                            cantoAdapter.notifyItemRemoved(i);
//                            i--;
//                        }
//                    }
//                    db.close();
////                    rootView.findViewById(R.id.no_favourites).setVisibility(titoli.size() > 0 ? View.INVISIBLE : View.VISIBLE);
//                    mNoFavorites.setVisibility(titoli.size() > 0 ? View.INVISIBLE : View.VISIBLE);
//                    if (titoli.size() == 0) {
//                        if (mMainActivity.isOnTablet())
//                            fabClear.hide();
//                        else
//                            mMainActivity.enableFab(false);
//                    }
//                    actionModeOk = true;
//                    mode.finish();
//                    if (removedItems.size() > 0) {
//                        String message = removedItems.size() > 1 ?
//                                getString(R.string.favorites_removed).replaceAll("%", String.valueOf(removedItems.size()))
//                                : getString(R.string.favorite_removed);
//                        Snackbar.make(getActivity().findViewById(R.id.main_content), message, Snackbar.LENGTH_LONG)
//                                .setAction(R.string.cancel, new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View view) {
//                                        SQLiteDatabase db = listaCanti.getReadableDatabase();
//                                        ContentValues values = new ContentValues();
//                                        values.put("favourite", 1);
//                                        for (CantoRecycled cantoRemoved: removedItems) {
//                                            db.update("ELENCO", values, "_id =  " + cantoRemoved.getIdCanto(), null);
//                                        }
//                                        db.close();
//                                        updateFavouritesList();
//                                    }
//                                })
//                                .setActionTextColor(getThemeUtils().accentColor())
//                                .show();
//                    }
//                    return true;
//                default:
//                    return false;
//            }
//        }
//    }

    @Override
    public void onPositive(@NonNull String tag) {
        Log.d(getClass().getName(), "onPositive: " + tag);
        switch (tag) {
            case "FAVORITES_RESET":
                SQLiteDatabase db = listaCanti.getReadableDatabase();
                ContentValues  values = new  ContentValues();
                values.put("favourite" , 0);
                db.update("ELENCO", values,  null, null);
                db.close();
                updateFavouritesList();
                break;
        }
    }
    @Override
    public void onNegative(@NonNull String tag) {}
    @Override
    public void onNeutral(@NonNull String tag) {}

    @Override
    public boolean onCabCreated(MaterialCab cab, Menu menu) {
        cab.setMenu(R.menu.menu_delete);
        cab.setTitle("");
//        titoli.get(posizDaCanc).setmSelected(true);
//        cantoAdapter.notifyItemChanged(posizDaCanc);
//        removedItems = new ArrayList<>();
        menu.findItem(R.id.action_remove_item).setIcon(
                new IconicsDrawable(getActivity(), CommunityMaterial.Icon.cmd_delete)
                        .sizeDp(24)
                        .paddingDp(2)
                        .colorRes(android.R.color.white));
//        cab.getToolbar().setNavigationIcon(new IconicsDrawable(getActivity(), CommunityMaterial.Icon.cmd_arrow_left)
//                .sizeDp(24)
//                .paddingDp(2)
//                .colorRes(android.R.color.white));
        actionModeOk = false;
        return true;
    }

    @Override
    public boolean onCabItemClicked(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_remove_item:
//                SQLiteDatabase db = listaCanti.getReadableDatabase();
//                ContentValues values = new ContentValues();
//                values.put("favourite", 0);
//                for (int i = 0; i < cantoAdapter.getAdapterItems().size(); i++) {
////                    Log.d(getClass().getName(), "selezionato[" + i + "]: " + titoli.get(i).ismSelected());
//                    Log.d(getClass().getName(), "selezionato[" + i + "]: " + cantoAdapter.getAdapterItem(i).isSelected());
////                    if (titoli.get(i).ismSelected()) {
//                    if (cantoAdapter.getAdapterItem(i).isSelected()) {
////                        db.update("ELENCO", values, "_id =  " + titoli.get(i).getIdCanto(), null);
//                        db.update("ELENCO", values, "_id =  " + cantoAdapter.getAdapterItem(i).getId(), null);
////                        titoli.get(i).setmSelected(false);
//                        cantoAdapter.getAdapterItem(i).withSetSelected(false);
////                        removedItems.add(titoli.remove(i));
//                        removedItems.add(cantoAdapter.getAdapterItems().remove(i));
//                        cantoAdapter.notifyAdapterItemRemoved(i);
////                        cantoAdapter.notifyItemRemoved(i);
//                        i--;
//                    }
//                }
//                db.close();

                int iRemoved = cantoAdapter.getSelectedItems().size();
                Log.d(TAG, "onCabItemClicked: " + iRemoved);

                mUndoHelper.remove(getActivity().findViewById(R.id.main_content)
                        , getResources().getQuantityString(R.plurals.favorites_removed, iRemoved, iRemoved)
                        , getString(R.string.cancel)
                        , Snackbar.LENGTH_SHORT
                        , cantoAdapter.getSelections());
                cantoAdapter.deselect();
//                if (iRemoved == cantoAdapter.getAdapterItemCount()) {
//                    mNoFavorites.setVisibility(View.VISIBLE);
//                    if (mMainActivity.isOnTablet())
//                        fabClear.hide();
//                    else
//                        mMainActivity.enableFab(false);
//                }
//                else
//                    mNoFavorites.setVisibility(View.INVISIBLE);
                actionModeOk = true;
                mMainActivity.getMaterialCab().finish();
//                if (removedItems.size() > 0) {
////                    String message = removedItems.size() > 1 ?
////                            getString(R.string.favorites_removed, removedItems.size())
////                            : getString(R.string.favorite_removed);
//                    Snackbar.make(getActivity().findViewById(R.id.main_content)
//                            , getResources().getQuantityString(R.plurals.favorites_removed, removedItems.size(), removedItems.size())
//                            , Snackbar.LENGTH_LONG)
//                            .setAction(R.string.cancel, new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    SQLiteDatabase db = listaCanti.getReadableDatabase();
//                                    ContentValues values = new ContentValues();
//                                    values.put("favourite", 1);
//                                    for (CantoRecycled cantoRemoved : removedItems) {
//                                        db.update("ELENCO", values, "_id =  " + cantoRemoved.getIdCanto(), null);
//                                    }
//                                    db.close();
//                                    updateFavouritesList();
//                                }
//                            })
//                            .setActionTextColor(getThemeUtils().accentColor())
//                            .show();
//                }
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
//                for (CantoRecycled canto : titoli) {
//                    canto.setmSelected(false);
//                    cantoAdapter.notifyDataSetChanged();
//                }
            }
            catch (Exception e){
                FirebaseCrash.log("Possibile crash");
            }
        }
        return true;
    }

    private ThemeUtils getThemeUtils() {
        return ((MainActivity)getActivity()).getThemeUtils();
    }

}
