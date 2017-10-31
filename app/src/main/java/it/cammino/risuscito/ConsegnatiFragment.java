package it.cammino.risuscito;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.ImageButton;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.itemanimators.SlideLeftAlphaAnimator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.items.CheckableItem;
import it.cammino.risuscito.items.SimpleItem;
import it.cammino.risuscito.services.ConsegnatiSaverService;
import it.cammino.risuscito.utils.ThemeUtils;

public class ConsegnatiFragment extends Fragment implements SimpleDialogFragment.SimpleCallback {

    private final String TAG = getClass().getCanonicalName();

    private DatabaseCanti listaCanti;
    private List<CheckableItem> titoliChoose;
    private View rootView;
    private FastItemAdapter<CheckableItem> selectableAdapter;
    private FloatingActionButton mFab;
    private View mBottomBar;

    private static final String EDIT_MODE = "editMode";
    public static final String TITOLI_CHOOSE = "titoliChoose";

    private MainActivity mMainActivity;

    private boolean editMode;

    private LUtils mLUtils;

    private long mLastClickTime = 0;

    private BroadcastReceiver positionBRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Implement UI change code here once notification is received
            try {
                Log.d(getClass().getName(), "BROADCAST_SINGLE_COMPLETED");
                Log.d(getClass().getName(), "DATA_DONE: " + intent.getIntExtra(ConsegnatiSaverService.DATA_DONE, 0));
                SimpleDialogFragment fragment = SimpleDialogFragment.findVisible((AppCompatActivity)getActivity(), "CONSEGNATI_SAVING");
                if (fragment != null)
                    fragment.setProgress(intent.getIntExtra(ConsegnatiSaverService.DATA_DONE, 0));
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
                SimpleDialogFragment fragment = SimpleDialogFragment.findVisible((AppCompatActivity)getActivity(), "CONSEGNATI_SAVING");
                if (fragment != null)
                    fragment.dismiss();
//                updateConsegnatiList(true);
                updateConsegnatiList();
                mChoosedRecyclerView.setVisibility(View.GONE);
                enableBottombar(false);
//                if (mMainActivity.isOnTablet())
//                    enableBottombar(false);
//                else
//                    mMainActivity.enableBottombar(false);
                mRecyclerView.setVisibility(View.VISIBLE);
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

    private Unbinder mUnbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.layout_consegnati, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        mMainActivity = (MainActivity) getActivity();

        mMainActivity.setupToolbarTitle(R.string.title_activity_consegnati);

        mBottomBar = mMainActivity.isOnTablet() ?
                rootView.findViewById(R.id.bottom_bar):
                getActivity().findViewById(R.id.bottom_bar);

        mMainActivity.mTabLayout.setVisibility(View.GONE);
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

        Log.d(TAG, "onCreateView - editMode: "+ editMode);
        View mSelectNone = mMainActivity.isOnTablet() ?
                rootView.findViewById(R.id.select_none):
                getActivity().findViewById(R.id.select_none);
        mSelectNone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectableAdapter.deselect();
            }
        });

        View mSelectAll = mMainActivity.isOnTablet() ?
                rootView.findViewById(R.id.select_all):
                getActivity().findViewById(R.id.select_all);
        mSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectableAdapter.select();
            }
        });

        ImageButton cancel_change = mMainActivity.isOnTablet() ?
                (ImageButton) rootView.findViewById(R.id.cancel_change):
                (ImageButton) getActivity().findViewById(R.id.cancel_change);
        cancel_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editMode = false;
//                updateConsegnatiList(true);
                updateConsegnatiList();
                mChoosedRecyclerView.setVisibility(View.INVISIBLE);
                enableBottombar(false);
//                if (mMainActivity.isOnTablet())
//                    enableBottombar(false);
//                else
//                    mMainActivity.enableBottombar(false);
                mRecyclerView.setVisibility(View.VISIBLE);
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
                        .showProgress()
                        .progressIndeterminate(false)
                        .progressMax(selectableAdapter.getItemCount())
                        .show();

                Set<CheckableItem> mSelected = selectableAdapter.getSelectedItems();
                ArrayList<Integer> mSelectedId = new ArrayList<>();
                for (CheckableItem item: mSelected) {
                    mSelectedId.add(item.getId());
                }

                Intent intent = new Intent(getActivity().getApplicationContext(), ConsegnatiSaverService.class);
                intent.putIntegerArrayListExtra(ConsegnatiSaverService.IDS_CONSEGNATI, mSelectedId);
                getActivity().getApplicationContext().startService(intent);
            }
        });

        getFab().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editMode = true;
                updateChooseList(true);
                mRecyclerView.setVisibility(View.GONE);
                mNoConsegnati.setVisibility(View.INVISIBLE);
                mChoosedRecyclerView.setVisibility(View.VISIBLE);
                enableBottombar(true);
//                if (mMainActivity.isOnTablet())
//                    enableBottombar(true);
//                else
//                    mMainActivity.enableBottombar(true);
                mMainActivity.enableFab(false);
                SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                Log.d(TAG, "onClick - INTRO_CONSEGNATI_2: " + mSharedPrefs.getBoolean(Utility.INTRO_CONSEGNATI_2, false));
                if (!mSharedPrefs.getBoolean(Utility.INTRO_CONSEGNATI_2, false)) {
                    managerIntro();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(getClass().getName(), "onResume: ");
        getActivity().registerReceiver(positionBRec, new IntentFilter(
                ConsegnatiSaverService.BROADCAST_SINGLE_COMPLETED));
        getActivity().registerReceiver(completedBRec, new IntentFilter(
                ConsegnatiSaverService.BROADCAST_SAVING_COMPLETED));
        if (editMode) {
            mChoosedRecyclerView.setVisibility(View.VISIBLE);
            enableBottombar(true);
//            if (mMainActivity.isOnTablet())
//                enableBottombar(true);
//            else
//                mMainActivity.enableBottombar(true);
            mRecyclerView.setVisibility(View.GONE);
            mNoConsegnati.setVisibility(View.INVISIBLE);
            mMainActivity.enableFab(false);

            updateChooseList(false);
        }
        else {
            mChoosedRecyclerView.setVisibility(View.GONE);
            enableBottombar(false);
//            if (mMainActivity.isOnTablet())
//                enableBottombar(false);
//            else
//                mMainActivity.enableBottombar(false);
            mRecyclerView.setVisibility(View.VISIBLE);
            mMainActivity.enableFab(true);
//            updateConsegnatiList(true);
            updateConsegnatiList();
        }
        SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Log.d(TAG, "onCreateView - INTRO_CONSEGNATI: " + mSharedPrefs.getBoolean(Utility.INTRO_CONSEGNATI, false));
        if (!mSharedPrefs.getBoolean(Utility.INTRO_CONSEGNATI, false)) {
            fabIntro();
        }

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
    }

    @Override
    public void onDestroy() {
        if (listaCanti != null)
            listaCanti.close();
        enableBottombar(false);
//        if (mMainActivity.isOnTablet())
//            enableBottombar(false);
//        else
//            mMainActivity.enableBottombar(false);
        super.onDestroy();
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
                if (editMode)
                    managerIntro();
                else
                    fabIntro();
                return true;
        }
        return false;
    }

    private void startSubActivity(Bundle bundle, View view) {
        Intent intent = new Intent(getActivity(), PaginaRenderActivity.class);
        intent.putExtras(bundle);
        mLUtils.startActivityWithTransition(intent, view, Utility.TRANS_PAGINA_RENDER);
    }

    //    private void updateConsegnatiList(boolean updateView) {
    private void updateConsegnatiList() {

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
//        if (updateView)
        mNoConsegnati.setVisibility(totalConsegnati > 0 ? View.INVISIBLE: View.VISIBLE);

        // crea un array e ci memorizza i titoli estratti
        List<SimpleItem> titoli = new ArrayList<>();
        lista.moveToFirst();
        for (int i = 0; i < totalConsegnati; i++) {
            SimpleItem sampleItem = new SimpleItem();
            sampleItem
                    .withTitle(lista.getString(0))
                    .withPage(String.valueOf(lista.getInt(2)))
                    .withSource(lista.getString(4))
                    .withColor(lista.getString(1))
                    .withId(lista.getInt(3));
            titoli.add(sampleItem);
            lista.moveToNext();
        }

        // chiude il cursore
        lista.close();
        db.close();
        if (listaCanti != null)
            listaCanti.close();

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

        // Creating new adapter object
        FastItemAdapter<SimpleItem> cantoAdapter = new FastItemAdapter<>();
        cantoAdapter.withOnClickListener(mOnClickListener);
        cantoAdapter.add(titoli);

        mRecyclerView.setAdapter(cantoAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setHasFixedSize(true);
        DividerItemDecoration insetDivider = new DividerItemDecoration(getContext(), llm.getOrientation());
        insetDivider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.material_inset_divider));
        mRecyclerView.addItemDecoration(insetDivider);
        mRecyclerView.setItemAnimator(new SlideLeftAlphaAnimator());

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
                CheckableItem checkableItem = new CheckableItem();
                checkableItem.withTitle(lista.getString(0))
                        .withPage(String.valueOf(lista.getInt(2)))
                        .withColor(lista.getString(1))
                        .withSetSelected(lista.getInt(4) > 0)
                        .withId(lista.getInt(3));
                titoliChoose.add(checkableItem);
                lista.moveToNext();
            }

            // chiude il cursore
            lista.close();
            db.close();
            if (listaCanti != null)
                listaCanti.close();
        }

        // Creating new adapter object
        selectableAdapter = new FastItemAdapter<>();
        selectableAdapter.withSelectable(true)
                .setHasStableIds(true);

        //init the ClickListenerHelper which simplifies custom click listeners on views of the Adapter
        selectableAdapter.withOnPreClickListener(new FastAdapter.OnClickListener<CheckableItem>() {
            @Override
            public boolean onClick(View v, IAdapter<CheckableItem> adapter, CheckableItem item, int position) {
                selectableAdapter.getAdapterItem(position).withSetSelected(!selectableAdapter.getAdapterItem(position).isSelected());
                selectableAdapter.notifyAdapterItemChanged(position);
                return true;
            }
        });
        selectableAdapter.withEventHook(new CheckableItem.CheckBoxClickEvent());
        selectableAdapter.add(titoliChoose);

        mChoosedRecyclerView.setAdapter(selectableAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        mChoosedRecyclerView.setLayoutManager(llm);
        mChoosedRecyclerView.setHasFixedSize(true);
        DividerItemDecoration insetDivider = new DividerItemDecoration(getContext(), llm.getOrientation());
        insetDivider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.material_inset_divider));
        mChoosedRecyclerView.addItemDecoration(insetDivider);
        mChoosedRecyclerView.setItemAnimator(new SlideLeftAlphaAnimator());
    }

    private ThemeUtils getThemeUtils() {
        return ((MainActivity)getActivity()).getThemeUtils();
    }

    public static class RetainedFragment extends Fragment {

        // data object we want to retain
        private List<CheckableItem> data;

        // this method is only called once for this fragment
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // retain this fragment
            setRetainInstance(true);
        }

        public void setData(List<CheckableItem> data) {
            this.data = data;
        }

        public List<CheckableItem> getData() {
            return data;
        }
    }

    public List<CheckableItem> getTitoliChoose() {
        return titoliChoose;
    }

    private FloatingActionButton getFab() {
        if (mFab == null) {
            mFab = getActivity().findViewById(R.id.fab_pager);
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

    private void enableBottombar(boolean enabled) {
//        mBottomBar.setVisibility(enabled ? View.VISIBLE : View.GONE);
        if (mMainActivity.isOnTablet())
            mBottomBar.setVisibility(enabled ? View.VISIBLE : View.GONE);
        else
            mMainActivity.enableBottombar(enabled);
    }

    @Override
    public void onPositive(@NonNull String tag) {}
    @Override
    public void onNegative(@NonNull String tag) {}
    @Override
    public void onNeutral(@NonNull String tag) {}

    private void fabIntro() {
        TapTargetView.showFor(getActivity(),                 // `this` is an Activity
                TapTarget.forView(getFab(), getString(R.string.title_activity_consegnati), getString(R.string.showcase_consegnati_howto))
                        .outerCircleColorInt(getThemeUtils().primaryColor())     // Specify a color for the outer circle
                        .targetCircleColorInt(Color.WHITE)   // Specify a color for the target circle
                        .textTypeface(Typeface.createFromAsset(getResources().getAssets(),"fonts/Roboto-Regular.ttf"))  // Specify a typeface for the text
                        .titleTextColor(R.color.primary_text_default_material_dark)
                        .textColor(R.color.secondary_text_default_material_dark)
                        .tintTarget(false)                   // Whether to tint the target view's color
                , new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                    @Override
                    public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                        super.onTargetDismissed(view, userInitiated);
                        Log.d(TAG, "onTargetDismissed: ");
                        SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                        prefEditor.putBoolean(Utility.INTRO_CONSEGNATI, true);
                        prefEditor.apply();
                    }
                });
    }

    private void managerIntro() {
        new TapTargetSequence(getActivity())
                .continueOnCancel(true)
                .targets(
                        TapTarget.forView(mMainActivity.isOnTablet() ?
                                        (ImageButton) rootView.findViewById(R.id.confirm_changes):
                                        (ImageButton) getActivity().findViewById(R.id.confirm_changes)
                                , getString(R.string.title_activity_consegnati), getString(R.string.showcase_consegnati_confirm))
                                .outerCircleColorInt(getThemeUtils().primaryColor())     // Specify a color for the outer circle
                                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                                .textTypeface(Typeface.createFromAsset(getResources().getAssets(),"fonts/Roboto-Regular.ttf"))  // Specify a typeface for the text
                                .titleTextColor(R.color.primary_text_default_material_dark)
                                .textColor(R.color.secondary_text_default_material_dark)
                        ,
                        TapTarget.forView(mMainActivity.isOnTablet() ?
                                        (ImageButton) rootView.findViewById(R.id.cancel_change):
                                        (ImageButton) getActivity().findViewById(R.id.cancel_change)
                                , getString(R.string.title_activity_consegnati), getString(R.string.showcase_consegnati_cancel))
                                .outerCircleColorInt(getThemeUtils().primaryColor())     // Specify a color for the outer circle
                                .targetCircleColorInt(Color.WHITE)   // Specify a color for the target circle
                                .textTypeface(Typeface.createFromAsset(getResources().getAssets(),"fonts/Roboto-Regular.ttf"))  // Specify a typeface for the text
                                .titleTextColor(R.color.primary_text_default_material_dark)
                                .textColor(R.color.secondary_text_default_material_dark)
                )
                .listener(
                        new TapTargetSequence.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                            @Override
                            public void onSequenceFinish() {
                                Log.d(TAG, "onSequenceFinish: ");
                                SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                                prefEditor.putBoolean(Utility.INTRO_CONSEGNATI_2, true);
                                prefEditor.apply();
                            }

                            @Override
                            public void onSequenceStep(TapTarget tapTarget, boolean b) {}

                            @Override
                            public void onSequenceCanceled(TapTarget tapTarget) {
                                Log.d(TAG, "onSequenceCanceled: ");
                                SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                                prefEditor.putBoolean(Utility.INTRO_CONSEGNATI_2, true);
                                prefEditor.apply();
                            }
                        }).start();
    }

}
