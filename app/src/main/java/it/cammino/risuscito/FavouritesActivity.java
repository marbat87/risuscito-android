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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.itemanimators.SlideLeftAlphaAnimator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.cammino.risuscito.adapters.CantoRecyclerAdapter;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.objects.CantoRecycled;
import it.cammino.risuscito.utils.ThemeUtils;

public class FavouritesActivity extends Fragment implements SimpleDialogFragment.SimpleCallback {

    private final String TAG = getClass().getCanonicalName();

    private DatabaseCanti listaCanti;
    private List<CantoRecycled> titoli;
    private int posizDaCanc;
    private List<CantoRecycled> removedItems;
    private View rootView;
//    private RecyclerView recyclerView;
    private CantoRecyclerAdapter cantoAdapter;
    private FloatingActionButton fabClear;
    private ActionMode mMode;
    private boolean actionModeOk;

    private String PREFERITI_OPEN = "preferiti_open";

    private MainActivity mMainActivity;

    private LUtils mLUtils;

    private long mLastClickTime = 0;

    @BindView(R.id.favouritesList) RecyclerView mRecyclerView;
    @BindView(R.id.no_favourites) View mNoFavorites;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_favourites, container, false);
        ButterKnife.bind(this, rootView);

        mMainActivity = (MainActivity) getActivity();
        Log.d(TAG, "onCreateView: isOnTablet " + mMainActivity.isOnTablet());

        mMainActivity.setupToolbarTitle(R.string.title_activity_favourites);

//        getActivity().findViewById(R.id.material_tabs).setVisibility(View.GONE);
        mMainActivity.mTabLayout.setVisibility(View.GONE);

        //crea un istanza dell'oggetto DatabaseCanti
        listaCanti = new DatabaseCanti(getActivity());

        mLUtils = LUtils.getInstance(getActivity());
        mMode = null;

        if (!mMainActivity.isOnTablet()) {
            mMainActivity.enableFab(true);
            mMainActivity.enableBottombar(false);
        }
        fabClear = mMainActivity.isOnTablet() ? (FloatingActionButton) rootView.findViewById(R.id.fab_pager) :
                (FloatingActionButton) getActivity().findViewById(R.id.fab_pager);
        IconicsDrawable icon = new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_eraser)
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
                .getBoolean(PREFERITI_OPEN, false)) {
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(getActivity())
                    .edit();
            editor.putBoolean(PREFERITI_OPEN, true);
            editor.apply();
            android.os.Handler mHandler = new android.os.Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), getString(R.string.new_hint_remove), Toast.LENGTH_SHORT).show();
                }
            }, 250);
        }

        if (SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "FAVORITES_RESET") != null)
            SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "FAVORITES_RESET").setmCallback(FavouritesActivity.this);

        return rootView;
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
        titoli = new ArrayList<>();
        lista.moveToFirst();
        for (int i = 0; i < lista.getCount(); i++) {
            titoli.add(new CantoRecycled(lista.getString(0)
                    , lista.getInt(2)
                    , lista.getString(1)
                    , lista.getInt(3)
                    , lista.getString(4)));
            lista.moveToNext();
        }

        // chiude il cursore
        lista.close();

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMode == null) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
                        return;
                    mLastClickTime = SystemClock.elapsedRealtime();
                    // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da visualizzare
                    Bundle bundle = new Bundle();
                    bundle.putString("pagina", String.valueOf(((TextView) v.findViewById(R.id.text_source_canto)).getText()));
                    bundle.putInt("idCanto", Integer.parseInt(
                            String.valueOf(((TextView) v.findViewById(R.id.text_id_canto)).getText())));
                    // lancia l'activity che visualizza il canto passando il parametro creato
                    startSubActivity(bundle, v);
                }
                else {
                    int tempPos = mRecyclerView.getChildAdapterPosition(v);
                    titoli.get(tempPos).setmSelected(!titoli.get(tempPos).ismSelected());
                    cantoAdapter.notifyItemChanged(tempPos);
                }
            }
        };

        View.OnLongClickListener longClickListener  = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                posizDaCanc = mRecyclerView.getChildAdapterPosition(v);
                if (mMode == null)
                    mMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ModeCallback());
                else {
                    mMode.finish();
                    mMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ModeCallback());
                }
                return true;
            }
        };

//        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.favouritesList);

        // Creating new adapter object
        cantoAdapter = new CantoRecyclerAdapter(getActivity(), titoli, clickListener, longClickListener);
        mRecyclerView.setAdapter(cantoAdapter);

        // Setting the layoutManager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new SlideLeftAlphaAnimator());

//nel caso sia presente almeno un preferito, viene nascosto il testo di nessun canto presente
//        rootView.findViewById(R.id.no_favourites).setVisibility(titoli.size() > 0 ? View.INVISIBLE : View.VISIBLE);
        mNoFavorites.setVisibility(titoli.size() > 0 ? View.INVISIBLE : View.VISIBLE);
        if (titoli.size() == 0) {
            if (mMainActivity.isOnTablet())
                fabClear.hide();
            else
                mMainActivity.enableFab(false);
        }
        else {
            if (mMainActivity.isOnTablet())
                fabClear.show();
            else
                mMainActivity.enableFab(true);
        }
    }

    private ThemeUtils getThemeUtils() {
        return mMainActivity.getThemeUtils();
    }

    private final class ModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Create the menu from the xml file
            getActivity().getMenuInflater().inflate(R.menu.menu_delete, menu);
            titoli.get(posizDaCanc).setmSelected(true);
            cantoAdapter.notifyItemChanged(posizDaCanc);
            removedItems = new ArrayList<>();
            menu.findItem(R.id.action_remove_item).setIcon(
                    new IconicsDrawable(getActivity(), CommunityMaterial.Icon.cmd_delete)
                            .sizeDp(24)
                            .paddingDp(2)
                            .colorRes(R.color.icon_ative_black));
            actionModeOk = false;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // Here, you can checked selected items to adapt available actions
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (mode == mMode)
                mMode = null;
            Log.i(getClass().getName(), "actionModeOk: " + actionModeOk);
            if (!actionModeOk) {
                for (CantoRecycled canto : titoli) {
                    canto.setmSelected(false);
                    cantoAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch(item.getItemId()) {
                case R.id.action_remove_item:
                    Log.i(getClass().getName(), "CLICKED");
                    SQLiteDatabase db = listaCanti.getReadableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("favourite", 0);
                    for(int i = 0; i < titoli.size(); i++) {
                        Log.d(getClass().getName(), "selezionato[" + i + "]: " + titoli.get(i).ismSelected());
                        if (titoli.get(i).ismSelected()) {
                            db.update("ELENCO", values, "_id =  " + titoli.get(i).getIdCanto(), null);
                            titoli.get(i).setmSelected(false);
                            removedItems.add(titoli.remove(i));
                            cantoAdapter.notifyItemRemoved(i);
                            i--;
                        }
                    }
                    db.close();
//                    rootView.findViewById(R.id.no_favourites).setVisibility(titoli.size() > 0 ? View.INVISIBLE : View.VISIBLE);
                    mNoFavorites.setVisibility(titoli.size() > 0 ? View.INVISIBLE : View.VISIBLE);
                    if (titoli.size() == 0) {
                        if (mMainActivity.isOnTablet())
                            fabClear.hide();
                        else
                            mMainActivity.enableFab(false);
                    }
                    actionModeOk = true;
                    mode.finish();
                    if (removedItems.size() > 0) {
                        String message = removedItems.size() > 1 ?
                                getString(R.string.favorites_removed).replaceAll("%", String.valueOf(removedItems.size()))
                                : getString(R.string.favorite_removed);
                        Snackbar.make(getActivity().findViewById(R.id.main_content), message, Snackbar.LENGTH_LONG)
                                .setAction(R.string.cancel, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        SQLiteDatabase db = listaCanti.getReadableDatabase();
                                        ContentValues values = new ContentValues();
                                        values.put("favourite", 1);
                                        for (CantoRecycled cantoRemoved: removedItems) {
                                            db.update("ELENCO", values, "_id =  " + cantoRemoved.getIdCanto(), null);
                                        }
                                        db.close();
                                        updateFavouritesList();
                                    }
                                })
                                .setActionTextColor(getThemeUtils().accentColor())
                                .show();
                    }
                    return true;
                default:
                    return false;
            }
        }
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
}
