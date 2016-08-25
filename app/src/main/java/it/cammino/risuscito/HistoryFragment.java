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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.cammino.risuscito.adapters.CantoHistoryRecyclerAdapter;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.objects.CantoHistory;
import it.cammino.risuscito.utils.ThemeUtils;

public class HistoryFragment extends Fragment implements SimpleDialogFragment.SimpleCallback{

    private DatabaseCanti listaCanti;
    private List<CantoHistory> titoli;
    private int posizDaCanc;
    private List<CantoHistory> removedItems;
    private View rootView;
//    private RecyclerView recyclerView;
    private CantoHistoryRecyclerAdapter cantoAdapter;
    private FloatingActionButton fabClear;
    private ActionMode mMode;
    private boolean actionModeOk;

    private String HISTORY_OPEN = "history_open";

    private MainActivity mMainActivity;

    private LUtils mLUtils;

    private long mLastClickTime = 0;

    @BindView(R.id.history_recycler) RecyclerView mRecyclerView;
    @BindView(R.id.no_history) View mNoHistory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.layout_history, container, false);
        ButterKnife.bind(this, rootView);

        mMainActivity = (MainActivity) getActivity();
        mMainActivity.setupToolbarTitle(R.string.title_activity_history);

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
                new SimpleDialogFragment.Builder((AppCompatActivity)getActivity(), HistoryFragment.this, "RESET_HISTORY")
                        .title(R.string.dialog_reset_history_title)
                        .content(R.string.dialog_reset_history_desc)
                        .positiveButton(R.string.confirm)
                        .negativeButton(R.string.dismiss)
                        .show();
            }
        });

        if(!PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getBoolean(HISTORY_OPEN, false)) {
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(getActivity())
                    .edit();
            editor.putBoolean(HISTORY_OPEN, true);
            editor.apply();
            android.os.Handler mHandler = new android.os.Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), getString(R.string.new_hint_remove), Toast.LENGTH_SHORT).show();
                }
            }, 250);
        }

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
        updateHistoryList();
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

    private void updateHistoryList() {

        // crea un manipolatore per il Database in modalità READ
        SQLiteDatabase db = listaCanti.getReadableDatabase();

        // lancia la ricerca della cronologia
        String query = "SELECT A._id, A.titolo, A.color, A.pagina, A.source, B.ultima_visita" +
                "		FROM ELENCO A" +
                "          , CRONOLOGIA B" +
                "		WHERE A._id = B.id_canto" +
                "		ORDER BY B.ultima_visita DESC";
        Cursor lista = db.rawQuery(query, null);

        // crea un array e ci memorizza i titoli estratti
        titoli = new ArrayList<>();
        lista.moveToFirst();
        for (int i = 0; i < lista.getCount(); i++) {

            titoli.add(new CantoHistory(Utility.intToString(lista.getInt(3), 3) + lista.getString(2) + lista.getString(1)
                    , lista.getInt(0)
                    , lista.getString(4)
                    , lista.getString(5)));
            lista.moveToNext();
        }
        // chiude il cursore
        lista.close();

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da visualizzare
                if (mMode == null) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
                        return;
                    mLastClickTime = SystemClock.elapsedRealtime();
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

        View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
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

//        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.history_recycler);

        // Creating new adapter object
        cantoAdapter = new CantoHistoryRecyclerAdapter(getActivity(), titoli, clickListener, longClickListener);
        mRecyclerView.setAdapter(cantoAdapter);

        // Setting the layoutManager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //nel caso sia presente almeno un canto visitato di recente, viene nascosto il testo di nessun canto presente
//        rootView.findViewById(R.id.no_history).setVisibility(titoli.size() > 0 ? View.INVISIBLE : View.VISIBLE);
        mNoHistory.setVisibility(titoli.size() > 0 ? View.INVISIBLE : View.VISIBLE);
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
            if (!actionModeOk) {
                for (CantoHistory canto : titoli) {
                    canto.setmSelected(false);
                    cantoAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch(item.getItemId()) {
                case R.id.action_remove_item:
                    SQLiteDatabase db = listaCanti.getReadableDatabase();
                    for (int i = 0; i < titoli.size(); i++) {
                        Log.d(getClass().getName(), "selezionato[" + i + "]: " + titoli.get(i).ismSelected());
                        if (titoli.get(i).ismSelected()) {
                            db.delete("CRONOLOGIA", "id_canto =  " + titoli.get(i).getIdCanto(), null);
                            titoli.get(i).setmSelected(false);
                            removedItems.add(titoli.remove(i));
                            cantoAdapter.notifyItemRemoved(i);
                            i--;
                        }
                    }
                    db.close();
//                    rootView.findViewById(R.id.no_history).setVisibility(titoli.size() > 0 ? View.INVISIBLE : View.VISIBLE);
                    mNoHistory.setVisibility(titoli.size() > 0 ? View.INVISIBLE : View.VISIBLE);
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
                                getString(R.string.histories_removed).replaceAll("%", String.valueOf(removedItems.size()))
                                : getString(R.string.history_removed);
                        Snackbar.make(getActivity().findViewById(R.id.main_content), message, Snackbar.LENGTH_LONG)
                                .setAction(R.string.cancel, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        SQLiteDatabase db = listaCanti.getReadableDatabase();
                                        ContentValues values = new ContentValues();
                                        for (CantoHistory cantoRemoved: removedItems) {
                                            values.put("id_canto", cantoRemoved.getIdCanto());
                                            values.put("ultima_visita", cantoRemoved.getTimestamp());
                                            db.insert("CRONOLOGIA", null, values);
                                        }
                                        db.close();
                                        updateHistoryList();
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
            case "RESET_HISTORY":
                SQLiteDatabase db = listaCanti.getReadableDatabase();
                db.delete("CRONOLOGIA", null, null);
                db.close();
                updateHistoryList();
                break;
        }
    }
    @Override
    public void onNegative(@NonNull String tag) {}
    @Override
    public void onNeutral(@NonNull String tag) {}
}
