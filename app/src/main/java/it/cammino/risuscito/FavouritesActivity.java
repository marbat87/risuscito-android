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
    private FastItemAdapter<SimpleItem> cantoAdapter;
    private boolean actionModeOk;

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

        mMainActivity.mTabLayout.setVisibility(View.GONE);

        //crea un istanza dell'oggetto DatabaseCanti
        listaCanti = new DatabaseCanti(getActivity());

        mLUtils = LUtils.getInstance(getActivity());

        mMainActivity.enableFab(true);
        if (!mMainActivity.isOnTablet())
            mMainActivity.enableBottombar(false);
        FloatingActionButton fabClear = getActivity().findViewById(R.id.fab_pager);
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
                        .positiveButton(android.R.string.yes)
                        .negativeButton(android.R.string.no)
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
            SimpleItem sampleItem = new SimpleItem();
            sampleItem
                    .withTitle(lista.getString(0))
                    .withPage(String.valueOf(lista.getInt(2)))
                    .withSource(lista.getString(4))
                    .withColor(lista.getString(1))
                    .withId(lista.getInt(3))
                    .withSelectedColor(getThemeUtils().primaryColorDark());
            titoli.add(sampleItem);
            lista.moveToNext();
        }

        // chiude il cursore
        lista.close();
        db.close();

        FastAdapter.OnClickListener<SimpleItem> mOnPreClickListener = new FastAdapter.OnClickListener<SimpleItem>() {
            @Override
            public boolean onClick(View view, IAdapter<SimpleItem> iAdapter, SimpleItem item, int i) {
                Log.d(TAG, "onClick: 2");
                if (mMainActivity.getMaterialCab().isActive()) {
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

        cantoAdapter = new FastItemAdapter<>();
        cantoAdapter.withSelectable(true)
                .withMultiSelect(true)
                .withSelectOnLongClick(true)
                .withOnPreClickListener(mOnPreClickListener)
                .withOnClickListener(mOnClickListener)
                .withOnPreLongClickListener(mOnPreLongClickListener)
                .setHasStableIds(true);
        cantoAdapter.add(titoli);

        mRecyclerView.setAdapter(cantoAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(llm);
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
                mMainActivity.enableFab(cantoAdapter.getAdapterItemCount() != 0);
//                if (cantoAdapter.getAdapterItemCount() == 0)
//                    mMainActivity.enableFab(false);
            }
        });

        //nel caso sia presente almeno un preferito, viene nascosto il testo di nessun canto presente
        mNoFavorites.setVisibility(titoli.size() > 0 ? View.INVISIBLE : View.VISIBLE);
        mMainActivity.enableFab(titoli.size() != 0);
//        if (titoli.size() == 0)
//            mMainActivity.enableFab(false);
//        else
//            mMainActivity.enableFab(true);
    }

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
        menu.findItem(R.id.action_remove_item).setIcon(
                new IconicsDrawable(getActivity(), CommunityMaterial.Icon.cmd_delete)
                        .sizeDp(24)
                        .paddingDp(2)
                        .colorRes(android.R.color.white));
        actionModeOk = false;
        return true;
    }

    @Override
    public boolean onCabItemClicked(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_remove_item:
                int iRemoved = cantoAdapter.getSelectedItems().size();
                Log.d(TAG, "onCabItemClicked: " + iRemoved);
                mUndoHelper.remove(getActivity().findViewById(R.id.main_content)
                        , getResources().getQuantityString(R.plurals.favorites_removed, iRemoved, iRemoved)
                        , getString(android.R.string.cancel).toUpperCase()
                        , Snackbar.LENGTH_SHORT
                        , cantoAdapter.getSelections());
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
