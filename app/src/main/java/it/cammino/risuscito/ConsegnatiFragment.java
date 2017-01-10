package it.cammino.risuscito;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.stephentuso.welcome.WelcomeHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.cammino.risuscito.adapters.CantoRecyclerAdapter;
import it.cammino.risuscito.adapters.CantoSelezionabileAdapter;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.objects.Canto;
import it.cammino.risuscito.objects.CantoRecycled;
import it.cammino.risuscito.services.ConsegnatiSaverService;
import it.cammino.risuscito.slides.IntroConsegnatiNew;
import it.cammino.risuscito.ui.QuickReturnFooterBehavior;
import it.cammino.risuscito.utils.ThemeUtils;

public class ConsegnatiFragment extends Fragment implements SimpleDialogFragment.SimpleCallback {

    private final String TAG = getClass().getCanonicalName();

    private DatabaseCanti listaCanti;
    private List<Canto> titoliChoose;
    private View rootView;
    private CantoSelezionabileAdapter selectableAdapter;
    private FloatingActionButton mFab;
    private View mBottomBar;

    private static final String EDIT_MODE = "editMode";
    public static final String TITOLI_CHOOSE = "titoliChoose";

    private MainActivity mMainActivity;

    private boolean editMode;

    private LUtils mLUtils;

    private long mLastClickTime = 0;
    private WelcomeHelper mWelcomeScreen;

    private BroadcastReceiver positionBRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Implement UI change code here once notification is received
            try {
                Log.d(getClass().getName(), "BROADCAST_SINGLE_COMPLETED");
                Log.d(getClass().getName(), "DATA_DONE: " + intent.getIntExtra(ConsegnatiSaverService.DATA_DONE, 0));
                if (SimpleDialogFragment.findVisible((AppCompatActivity)getActivity(), "CONSEGNATI_SAVING") != null) {
                    SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "CONSEGNATI_SAVING")
                            .setProgress(intent.getIntExtra(ConsegnatiSaverService.DATA_DONE, 0));
                }
            }
            catch (IllegalArgumentException e) {
                Log.e(getClass().getName(), e.getLocalizedMessage(), e);
            }
        }
    };

    private BroadcastReceiver completedBRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Implement UI change code here once notification is received
            try {
                Log.d(getClass().getName(), "BROADCAST_SAVING_COMPLETED");
                if (SimpleDialogFragment.findVisible((AppCompatActivity)getActivity(), "CONSEGNATI_SAVING") != null)
                    SimpleDialogFragment.findVisible((AppCompatActivity)getActivity(), "CONSEGNATI_SAVING").dismiss();
                updateConsegnatiList(true);
//                rootView.findViewById(R.id.chooseRecycler).setVisibility(View.INVISIBLE);
                mChoosedRecyclerView.setVisibility(View.INVISIBLE);
                if (mMainActivity.isOnTablet())
                    enableBottombar(false);
                else
                    mMainActivity.enableBottombar(false);
//                rootView.findViewById(R.id.cantiRecycler).setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.VISIBLE);
                if (mMainActivity.isOnTablet())
//                    showFab();
                    getFab().show();
                else
                    mMainActivity.enableFab(true);
            }
            catch (IllegalArgumentException e) {
                Log.e(getClass().getName(), e.getLocalizedMessage(), e);
            }
        }
    };

    @BindView(R.id.cantiRecycler) RecyclerView mRecyclerView;
    @BindView(R.id.chooseRecycler) RecyclerView mChoosedRecyclerView;
    @BindView(R.id.no_consegnati) View mNoConsegnati;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.layout_consegnati, container, false);
        ButterKnife.bind(this, rootView);

        mMainActivity = (MainActivity) getActivity();

        ((MainActivity) getActivity()).setupToolbarTitle(R.string.title_activity_consegnati);

//        Log.d(TAG, "onCreateView: isOnTablet " + isOnTablet);

        mBottomBar = mMainActivity.isOnTablet() ?
                rootView.findViewById(R.id.bottom_bar):
                getActivity().findViewById(R.id.bottom_bar);

//        getActivity().findViewById(R.id.material_tabs).setVisibility(View.GONE);
        mMainActivity.mTabLayout.setVisibility(View.GONE);
        if (!mMainActivity.isOnTablet())
            mMainActivity.enableFab(true);

        mLUtils = LUtils.getInstance(getActivity());

        mBottomBar.setBackgroundColor(getThemeUtils().primaryColor());

        if (savedInstanceState == null)
            editMode = false;
        else {
            editMode = savedInstanceState.getBoolean(EDIT_MODE, false);
            if (editMode) {
                RetainedFragment dataFragment = (RetainedFragment) getActivity().getSupportFragmentManager().findFragmentByTag(TITOLI_CHOOSE);
                if (dataFragment != null)
                    titoliChoose = dataFragment.getData();
            }
        }

        if (editMode) {
//            rootView.findViewById(R.id.chooseRecycler).setVisibility(View.VISIBLE);
            mChoosedRecyclerView.setVisibility(View.VISIBLE);
            if (mMainActivity.isOnTablet())
                enableBottombar(true);
            else
                mMainActivity.enableBottombar(true);
//            rootView.findViewById(R.id.cantiRecycler).setVisibility(View.INVISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);
//            rootView.findViewById(R.id.no_consegnati).setVisibility(View.INVISIBLE);
            mNoConsegnati.setVisibility(View.INVISIBLE);
            if (mMainActivity.isOnTablet())
//                hideFab();
                getFab().hide();
            else
                mMainActivity.enableFab(false);

            updateChooseList(false);
        }
        else {
//            rootView.findViewById(R.id.chooseRecycler).setVisibility(View.INVISIBLE);
            mChoosedRecyclerView.setVisibility(View.INVISIBLE);
            if (mMainActivity.isOnTablet())
                enableBottombar(false);
            else
                mMainActivity.enableBottombar(false);
//            rootView.findViewById(R.id.cantiRecycler).setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);
            if (mMainActivity.isOnTablet())
//                showFab();
                getFab().show();
            else
                mMainActivity.enableFab(true);
            updateConsegnatiList(true);
        }
        View mSelectNone = mMainActivity.isOnTablet() ?
                rootView.findViewById(R.id.select_none):
                getActivity().findViewById(R.id.select_none);
        mSelectNone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Canto canto: titoliChoose) {
                    canto.setSelected(false);
                    selectableAdapter.notifyDataSetChanged();
                }
            }
        });

        View mSelectAll = mMainActivity.isOnTablet() ?
                rootView.findViewById(R.id.select_all):
                getActivity().findViewById(R.id.select_all);
        mSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Canto canto: titoliChoose) {
                    canto.setSelected(true);
                    selectableAdapter.notifyDataSetChanged();
                }
            }
        });

        ImageButton cancel_change = mMainActivity.isOnTablet() ?
                (ImageButton) rootView.findViewById(R.id.cancel_change):
                (ImageButton) getActivity().findViewById(R.id.cancel_change);
        cancel_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editMode = false;
                updateConsegnatiList(true);
//                rootView.findViewById(R.id.chooseRecycler).setVisibility(View.INVISIBLE);
                mChoosedRecyclerView.setVisibility(View.INVISIBLE);
                if (mMainActivity.isOnTablet())
                    enableBottombar(false);
                else
                    mMainActivity.enableBottombar(false);
//                rootView.findViewById(R.id.cantiRecycler).setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.VISIBLE);
                if (mMainActivity.isOnTablet())
//                    showFab();
                    getFab().show();
                else
                    mMainActivity.enableFab(true);
            }
        });

        ImageButton confirm_changes = mMainActivity.isOnTablet() ?
                (ImageButton) rootView.findViewById(R.id.confirm_changes):
                (ImageButton) getActivity().findViewById(R.id.confirm_changes);
        confirm_changes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editMode = false;
                new SimpleDialogFragment.Builder((AppCompatActivity)getActivity(), ConsegnatiFragment.this, "CONSEGNATI_SAVING")
                        .content(R.string.save_consegnati_running)
                        .showProgress(true)
                        .progressIndeterminate(false)
                        .progressMax(selectableAdapter.getItemCount())
                        .show();
                Intent intent = new Intent(getActivity().getApplicationContext(), ConsegnatiSaverService.class);
                intent.putIntegerArrayListExtra(ConsegnatiSaverService.IDS_CONSEGNATI, selectableAdapter.getChoosedIds());
                getActivity().getApplicationContext().startService(intent);
            }
        });

        getFab().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editMode = true;
                updateChooseList(true);
//                rootView.findViewById(R.id.cantiRecycler).setVisibility(View.INVISIBLE);
                mRecyclerView.setVisibility(View.INVISIBLE);
//                rootView.findViewById(R.id.no_consegnati).setVisibility(View.INVISIBLE);
                mNoConsegnati.setVisibility(View.INVISIBLE);
//                rootView.findViewById(R.id.chooseRecycler).setVisibility(View.VISIBLE);
                mChoosedRecyclerView.setVisibility(View.VISIBLE);
                if (mMainActivity.isOnTablet())
                    enableBottombar(true);
                else
                    mMainActivity.enableBottombar(true);
                if (mMainActivity.isOnTablet())
//                    hideFab();
                    getFab().hide();
                else
                    mMainActivity.enableFab(false);
            }
        });

        mWelcomeScreen = new WelcomeHelper(getActivity(), IntroConsegnatiNew.class);
        mWelcomeScreen.show(savedInstanceState);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(getClass().getName(), "onResume: ");
        getActivity().registerReceiver(positionBRec, new IntentFilter(
                ConsegnatiSaverService.BROADCAST_SINGLE_COMPLETED));
        getActivity().registerReceiver(completedBRec, new IntentFilter(
                ConsegnatiSaverService.BROADCAST_SAVING_COMPLETED));
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(getClass().getName(), "onPause: ");
        getActivity().unregisterReceiver(positionBRec);
        getActivity().unregisterReceiver(completedBRec);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(EDIT_MODE, editMode);
        super.onSaveInstanceState(savedInstanceState);
        mWelcomeScreen.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        if (listaCanti != null)
            listaCanti.close();
        super.onDestroy();
        if (mMainActivity.isOnTablet())
            enableBottombar(false);
        else
            mMainActivity.enableBottombar(false);
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
                mWelcomeScreen.forceShow();
                return true;
        }
        return false;
    }

    private void startSubActivity(Bundle bundle, View view) {
        Intent intent = new Intent(getActivity(), PaginaRenderActivity.class);
        intent.putExtras(bundle);
        mLUtils.startActivityWithTransition(intent, view, Utility.TRANS_PAGINA_RENDER);
    }

    private void updateConsegnatiList(boolean updateView) {

        // crea un manipolatore per il Database in modalità READ
        listaCanti = new DatabaseCanti(getActivity());
        SQLiteDatabase db = listaCanti.getReadableDatabase();

        // lancia la ricerca dei preferiti
        String query = "SELECT A.titolo, A.color, A.pagina, A._id, A.source" +
                "		FROM ELENCO A, CANTI_CONSEGNATI B" +
                "		WHERE A._id = B.id_canto" +
                "		ORDER BY TITOLO ASC";
        Cursor lista = db.rawQuery(query, null);

        //recupera il numero di record trovati
        int totalConsegnati = lista.getCount();

        //nel caso sia presente almeno un preferito, viene nascosto il testo di nessun canto presente
        if (updateView)
//            rootView.findViewById(R.id.no_consegnati).setVisibility(totalConsegnati > 0 ? View.INVISIBLE: View.VISIBLE);
            mNoConsegnati.setVisibility(totalConsegnati > 0 ? View.INVISIBLE: View.VISIBLE);

        // crea un array e ci memorizza i titoli estratti
        List<CantoRecycled> titoli = new ArrayList<>();
        lista.moveToFirst();
        for (int i = 0; i < totalConsegnati; i++) {
            titoli.add(new CantoRecycled(lista.getString(0)
                    , lista.getInt(2)
                    , lista.getString(1)
                    , lista.getInt(3)
                    , lista.getString(4)));
            lista.moveToNext();
        }

        // chiude il cursore
        lista.close();
        db.close();
        if (listaCanti != null)
            listaCanti.close();

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
                    return;
                mLastClickTime = SystemClock.elapsedRealtime();

                // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da visualizzare
                Bundle bundle = new Bundle();
                bundle.putString("pagina", String.valueOf(((TextView) v.findViewById(R.id.text_source_canto)).getText()));
                bundle.putInt("idCanto", Integer.valueOf(
                        String.valueOf(((TextView) v.findViewById(R.id.text_id_canto)).getText())));

                // lancia l'activity che visualizza il canto passando il parametro creato
                startSubActivity(bundle, v);
            }
        };

//        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.cantiRecycler);

        // Creating new adapter object
        CantoRecyclerAdapter cantoAdapter = new CantoRecyclerAdapter(getActivity(), titoli, clickListener);
        mRecyclerView.setAdapter(cantoAdapter);

        mRecyclerView.setHasFixedSize(true);

        // Setting the layoutManager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    }

    private void updateChooseList(boolean reload) {

        if (reload) {
            // crea un manipolatore per il Database
            listaCanti = new DatabaseCanti(getActivity());
            SQLiteDatabase db = listaCanti.getReadableDatabase();

            // lancia la ricerca dei canti
            String query = "SELECT A.titolo, A.color, A.pagina, A._id, coalesce(B._id,0)" +
                    "		FROM ELENCO A LEFT JOIN CANTI_CONSEGNATI B" +
                    "		ON A._id = B.id_canto" +
                    "		ORDER BY A.TITOLO ASC";
            Cursor lista = db.rawQuery(query, null);

            // crea un array e ci memorizza i titoli estratti
            titoliChoose = new ArrayList<>();
            lista.moveToFirst();
            for (int i = 0; i < lista.getCount(); i++) {
//            Log.i(getClass().toString(), "CANTO: " + Utility.intToString(lista.getInt(2), 3) + lista.getString(1) + lista.getString(0));
//            Log.i(getClass().toString(), "ID: " + lista.getInt(3));
//            Log.i(getClass().toString(), "SELEZIONATO: " + lista.getInt(4));
                titoliChoose.add(new Canto(Utility.intToString(lista.getInt(2), 3) + lista.getString(1) + lista.getString(0)
                        , lista.getInt(3)
                        , lista.getInt(4) > 0));
                lista.moveToNext();
            }

            // chiude il cursore
            lista.close();
            db.close();
            if (listaCanti != null)
                listaCanti.close();
        }

//        RecyclerView mChoosedRecyclerView = (RecyclerView) rootView.findViewById(R.id.chooseRecycler);

        // Creating new adapter object
        selectableAdapter = new CantoSelezionabileAdapter(getActivity(), titoliChoose);
        mChoosedRecyclerView.setAdapter(selectableAdapter);

        mChoosedRecyclerView.setHasFixedSize(true);

        // Setting the layoutManager
        mChoosedRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    }

    private ThemeUtils getThemeUtils() {
        return ((MainActivity)getActivity()).getThemeUtils();
    }

    public static class RetainedFragment extends Fragment {

        // data object we want to retain
        private List<Canto> data;

        // this method is only called once for this fragment
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // retain this fragment
            setRetainInstance(true);
        }

        public void setData(List<Canto> data) {
            this.data = data;
        }

        public List<Canto> getData() {
            return data;
        }
    }

    public List<Canto> getTitoliChoose() {
        return titoliChoose;
    }

    private FloatingActionButton getFab() {
        if (mFab == null) {
            mFab = mMainActivity.isOnTablet() ? (FloatingActionButton) rootView.findViewById(R.id.fab_pager) :
                    (FloatingActionButton) getActivity().findViewById(R.id.fab_pager);
            mFab.setVisibility(View.VISIBLE);
            IconicsDrawable icon = new IconicsDrawable(getActivity())
                    .icon(CommunityMaterial.Icon.cmd_pencil)
                    .color(Color.WHITE)
                    .sizeDp(24)
                    .paddingDp(2);
            mFab.setImageDrawable(icon);
        }
        return mFab;
    }

//    private void hideFab() {
//        getFab().hide();
//        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) getFab().getLayoutParams();
//        params.setBehavior(null);
//        getFab().requestLayout();
//    }
//
//    private void showFab() {
//        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) getFab().getLayoutParams();
//        params.setBehavior(new ScrollAwareFABBehavior(getContext(), null));
//        getFab().requestLayout();
//        getFab().show();
//    }

    private void enableBottombar(boolean enabled) {
        mBottomBar.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mBottomBar.getLayoutParams();
        params.setBehavior(enabled ? new QuickReturnFooterBehavior(getContext(), null) : null);
        mBottomBar.requestLayout();
    }

    @Override
    public void onPositive(@NonNull String tag) {}
    @Override
    public void onNegative(@NonNull String tag) {}
    @Override
    public void onNeutral(@NonNull String tag) {}

}
