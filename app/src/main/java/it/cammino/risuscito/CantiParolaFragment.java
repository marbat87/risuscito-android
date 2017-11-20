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
import com.google.firebase.crash.FirebaseCrash;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import it.cammino.risuscito.adapters.PosizioneRecyclerAdapter;
import it.cammino.risuscito.database.Posizione;
import it.cammino.risuscito.database.RisuscitoDatabase;
import it.cammino.risuscito.database.dao.CustomListDao;
import it.cammino.risuscito.database.entities.CustomList;
import it.cammino.risuscito.objects.PosizioneItem;
import it.cammino.risuscito.objects.PosizioneTitleItem;
import it.cammino.risuscito.ui.BottomSheetFragment;
import it.cammino.risuscito.ui.ThemeableActivity;
import it.cammino.risuscito.utils.ThemeUtils;
import it.cammino.risuscito.viewmodels.CantiParolaViewModel;

public class CantiParolaFragment extends Fragment implements MaterialCab.Callback {

    private final String TAG = getClass().getCanonicalName();

    private CantiParolaViewModel mCantiViewModel;

    // create boolean for fetching data
    private boolean isViewShown = true;


    private int posizioneDaCanc;
    private int idDaCanc;
    private String timestampDaCanc;
    private View rootView;
    //    private DatabaseCanti listaCanti;
//    private SQLiteDatabase db;
    private boolean mSwhitchMode;
    private List<Pair<PosizioneTitleItem, List<PosizioneItem>>> posizioniList;
    private int longclickedPos, longClickedChild;
    private PosizioneRecyclerAdapter cantoAdapter;
    private boolean actionModeOk;

    private MainActivity mMainActivity;

    private long mLastClickTime = 0;

    private static final int TAG_INSERT_PAROLA = 333;

    private LUtils mLUtils;

    @BindView(R.id.recycler_list) RecyclerView mRecyclerView;

    @OnClick(R.id.button_pulisci)
    public void pulisciLista() {
//                Log.i(getClass().toString(), "cantiparola");
//        db = listaCanti.getReadableDatabase();
//        String sql = "DELETE FROM CUST_LISTS" +
//                " WHERE _id =  1 ";
//        db.execSQL(sql);
//        db.close();
//        updateLista();
//        cantoAdapter.notifyDataSetChanged();
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        CustomListDao mDao = RisuscitoDatabase.getInstance(getContext()).customListDao();
                        mDao.deleteListById(1);
                    }
                })
                .start();
    }

    @OnClick(R.id.button_condividi)
    public void condividiLista() {
//                Log.i(getClass().toString(), "cantiparola");
        BottomSheetFragment bottomSheetDialog = BottomSheetFragment.newInstance(R.string.share_by, getDefaultIntent());
        bottomSheetDialog.show(getFragmentManager(), null);
    }

    private Unbinder mUnbinder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_lista_personalizzata, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        mMainActivity = (MainActivity) getActivity();

        //crea un istanza dell'oggetto DatabaseCanti
//        listaCanti = new DatabaseCanti(getActivity());

        mLUtils = LUtils.getInstance(getActivity());
        mSwhitchMode = false;

//        updateLista();

        OnClickListener click = new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
                    return;
                mLastClickTime = SystemClock.elapsedRealtime();
                final View parent = (View) v.getParent().getParent();
                if (parent.findViewById(R.id.addCantoGenerico).getVisibility() == View.VISIBLE) {
                    if (mSwhitchMode) {
                        mSwhitchMode = false;
                        actionModeOk = true;
                        mMainActivity.getMaterialCab().finish();
                        new Thread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        scambioConVuoto(parent, Integer.valueOf(((TextView) parent.findViewById(R.id.text_id_posizione)).getText().toString()));
                                    }
                                })
                                .start();
//                        scambioConVuoto(parent, Integer.valueOf(((TextView) parent.findViewById(R.id.text_id_posizione)).getText().toString()));
                    }
                    else {
                        if (!mMainActivity.getMaterialCab().isActive()) {
                            Bundle bundle = new Bundle();
                            bundle.putInt("fromAdd", 1);
                            bundle.putInt("idLista", 1);
                            bundle.putInt("position", Integer.valueOf(((TextView) parent.findViewById(R.id.text_id_posizione)).getText().toString()));
                            startSubActivity(bundle);
                        }
                    }
                }
                else {
                    if (!mSwhitchMode)
                        if (mMainActivity.getMaterialCab().isActive()) {
                            posizioneDaCanc = Integer.valueOf(((TextView) parent.findViewById(R.id.text_id_posizione)).getText().toString());
                            idDaCanc = Integer.valueOf(((TextView) v.findViewById(R.id.text_id_canto_card)).getText().toString());
                            timestampDaCanc = ((TextView) v.findViewById(R.id.text_timestamp)).getText().toString();
                            snackBarRimuoviCanto(v);
                        }
                        else
                            openPagina(v);
                    else {
                        mSwhitchMode = false;
                        actionModeOk = true;
                        mMainActivity.getMaterialCab().finish();
                        new Thread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        scambioCanto(v, Integer.valueOf(((TextView) parent.findViewById(R.id.text_id_posizione)).getText().toString()));
                                    }
                                })
                                .start();
//                        scambioCanto(v, Integer.valueOf(((TextView) parent.findViewById(R.id.text_id_posizione)).getText().toString()));
                    }
                }
            }
        };

        OnLongClickListener longClick = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                View parent = (View) v.getParent().getParent();
                posizioneDaCanc = Integer.valueOf(((TextView) parent.findViewById(R.id.text_id_posizione)).getText().toString());
                idDaCanc = Integer.valueOf(((TextView) v.findViewById(R.id.text_id_canto_card)).getText().toString());
                timestampDaCanc = ((TextView) v.findViewById(R.id.text_timestamp)).getText().toString();
                snackBarRimuoviCanto(v);
                return true;
            }
        };

        // Creating new adapter object
        posizioniList = new ArrayList<>();
        cantoAdapter = new PosizioneRecyclerAdapter(getThemeUtils().primaryColorDark(), posizioniList, click, longClick);
        mRecyclerView.setAdapter(cantoAdapter);

        // Setting the layoutManager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mCantiViewModel = ViewModelProviders.of(this).get(CantiParolaViewModel.class);
        populateDb();
        subscribeUiFavorites();

        if (!isViewShown) {
            if (mMainActivity.getMaterialCab().isActive())
                mMainActivity.getMaterialCab().finish();
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
                if (mMainActivity.getMaterialCab().isActive())
                    mMainActivity.getMaterialCab().finish();
                FloatingActionButton fab1 = ((CustomLists) getParentFragment()).getFab();
                fab1.show();
            }
            else
                isViewShown = false;
        }
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
////        Log.i(getClass().getName(), "requestCode: " + requestCode);
//        if (requestCode == TAG_INSERT_PAROLA && resultCode == Activity.RESULT_OK) {
//            updateLista();
//            cantoAdapter.notifyDataSetChanged();
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    @Override
    public void onDestroy() {
//        if (listaCanti != null)
//            listaCanti.close();
        if (mMainActivity.getMaterialCab().isActive())
            mMainActivity.getMaterialCab().finish();
        super.onDestroy();
    }

    private Intent getDefaultIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, getTitlesList());
        intent.setType("text/plain");
        return intent;
    }

//    private void updateLista() {
//
//        if (posizioniList == null)
//            posizioniList = new ArrayList<>();
//        else
//            posizioniList.clear();
//
//        posizioniList.add(getCantofromPosition(getString(R.string.canto_iniziale), 1, 0));
//        posizioniList.add(getCantofromPosition(getString(R.string.prima_lettura), 2, 1));
//        posizioniList.add(getCantofromPosition(getString(R.string.seconda_lettura), 3, 2));
//        posizioniList.add(getCantofromPosition(getString(R.string.terza_lettura), 4, 3));
//
//        SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(getActivity());
//        if (pref.getBoolean(Utility.SHOW_PACE, false)) {
//            posizioniList.add(getCantofromPosition(getString(R.string.canto_pace), 6, 4));
//            posizioniList.add(getCantofromPosition(getString(R.string.canto_fine), 5, 5));
//        }
//        else
//            posizioniList.add(getCantofromPosition(getString(R.string.canto_fine), 5, 4));
//
//    }

    private void startSubActivity(Bundle bundle) {
        Intent intent = new Intent(getActivity(), GeneralInsertSearch.class);
        intent.putExtras(bundle);
        getParentFragment().startActivityForResult(intent, TAG_INSERT_PAROLA);
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.hold_on);
    }

    private void openPagina(View v) {
        // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da visualizzare
        Bundle bundle = new Bundle();
        bundle.putString("pagina", ((TextView) v.findViewById(R.id.text_source_canto)).getText().toString());
        bundle.putInt("idCanto", Integer.valueOf(((TextView) v.findViewById(R.id.text_id_canto_card)).getText().toString()));

        Intent intent = new Intent(getActivity(), PaginaRenderActivity.class);
        intent.putExtras(bundle);
        mLUtils.startActivityWithTransition(intent, v, Utility.TRANS_PAGINA_RENDER);
    }

    //recupera il titolo del canto in posizione "position" nella lista
//    private Pair<PosizioneTitleItem, List<PosizioneItem>> getCantofromPosition(String titoloPosizione, int position, int tag) {
    private Pair<PosizioneTitleItem, List<PosizioneItem>> getCantofromPosition(List<Posizione> posizioni, String titoloPosizione, int position, int tag) {

//        db = listaCanti.getReadableDatabase();
//
//        String query = "SELECT B.titolo, B.color, B.pagina, B.source, B._id, A.timestamp" +
//                "  FROM CUST_LISTS A" +
//                "  	   , ELENCO B" +
//                "  WHERE A._id = 1" +
//                "  AND   A.position = " + position +
//                "  AND   A.id_canto = B._id";
//        Cursor cursor = db.rawQuery(query, null);

//        int total = cursor.getCount();

        List<PosizioneItem> list = new ArrayList<>();
//        if (total > 0) {
//            cursor.moveToFirst();
//
//            list.add(new PosizioneItem(
//                    cursor.getInt(2)
//                    , cursor.getString(0)
//                    , cursor.getString(1)
//                    , cursor.getInt(4)
//                    , cursor.getString(3)
//                    , cursor.getString(5)));
//
//            while (cursor.moveToNext()) {
//                list.add(new PosizioneItem(
//                        cursor.getInt(2)
//                        , cursor.getString(0)
//                        , cursor.getString(1)
//                        , cursor.getInt(4)
//                        , cursor.getString(3)
//                        , cursor.getString(5)));
//            }
//        }

        for (Posizione posizione: posizioni) {
            if (posizione.position == position) {
                list.add(new PosizioneItem(
                        posizione.pagina
                        , posizione.titolo
                        , posizione.color
                        , posizione.id
                        , posizione.source
                        , String.valueOf(posizione.timestamp.getTime())));
            }

        }

        //noinspection unchecked
        return new Pair(new PosizioneTitleItem(titoloPosizione
                , 1
                , position
                , tag
                , false), list);

//        cursor.close();
//        db.close();

//        return result;

    }

    private String getTitlesList() {

        Locale l = ThemeableActivity.getSystemLocalWrapper(getActivity().getResources().getConfiguration());
        StringBuilder result = new StringBuilder();

        //titolo
        result.append("-- ").append(getString(R.string.title_activity_canti_parola).toUpperCase(l)).append(" --\n");

        result.append(getResources().getString(R.string.canto_iniziale).toUpperCase(l));
        result.append("\n");

        result.append(getTitoloToSendFromPosition(0));
        result.append("\n");

        //prima lettura
        result.append(getResources().getString(R.string.prima_lettura).toUpperCase(l));
        result.append("\n");

        result.append(getTitoloToSendFromPosition(1));
        result.append("\n");

        //seconda lettura
        result.append(getResources().getString(R.string.seconda_lettura).toUpperCase(l));
        result.append("\n");

        result.append(getTitoloToSendFromPosition(2));
        result.append("\n");

        //terza lettura
        result.append(getResources().getString(R.string.terza_lettura).toUpperCase(l));
        result.append("\n");

        result.append(getTitoloToSendFromPosition(3));
        result.append("\n");

        //deve essere messo anche il canto alla pace? legge le impostazioni
        SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (pref.getBoolean(Utility.SHOW_PACE, false)) {
            //canto alla pace
            result.append(getResources().getString(R.string.canto_pace).toUpperCase(l));
            result.append("\n");

            result.append(getTitoloToSendFromPosition(4));
            result.append("\n");

            //canto finale
            result.append(getResources().getString(R.string.canto_fine).toUpperCase(l));
            result.append("\n");

            result.append(getTitoloToSendFromPosition(5));
        }
        else {
            //canto finale
            result.append(getResources().getString(R.string.canto_fine).toUpperCase(l));
            result.append("\n");

            result.append(getTitoloToSendFromPosition(4));
        }

        return result.toString();

    }

    //recupera il titolo del canto in posizione "position" nella lista "list"
    private String getTitoloToSendFromPosition(int position) {
        StringBuilder result = new StringBuilder();

        List<PosizioneItem> items = posizioniList.get(position).second;

        if (items.size() > 0) {
            for (PosizioneItem tempItem: items) {
                result.append(tempItem.getTitolo()).append(" - ").append(getString(R.string.page_contracted)).append(tempItem.getPagina());
                result.append("\n");
            }
        }
        else {
            result.append(">> ").append(getString(R.string.to_be_chosen)).append(" <<");
            result.append("\n");
        }

        return result.toString();
    }

    private void snackBarRimuoviCanto(View view) {
        if (mMainActivity.getMaterialCab().isActive())
            mMainActivity.getMaterialCab().finish();
        View parent = (View) view.getParent().getParent();
        longclickedPos = Integer.valueOf(((TextView)parent.findViewById(R.id.tag)).getText().toString());
        longClickedChild = Integer.valueOf(((TextView)view.findViewById(R.id.item_tag)).getText().toString());
        if (!mMainActivity.isOnTablet() && mMainActivity.getAppBarLayout() != null)
            mMainActivity.getAppBarLayout().setExpanded(true, true);
        mMainActivity.getMaterialCab().start(CantiParolaFragment.this);
    }

    private ThemeUtils getThemeUtils() {
        return ((MainActivity)getActivity()).getThemeUtils();
    }

    private void scambioCanto(View v, int position) {
//        db = listaCanti.getReadableDatabase();
        int idNew = Integer.valueOf(((TextView) v.findViewById(R.id.text_id_canto_card)).getText().toString());
        String timestampNew = ((TextView) v.findViewById(R.id.text_timestamp)).getText().toString();
        if (idNew != idDaCanc || posizioneDaCanc != position) {

//            db.delete("CUST_LISTS", "_id = 1 AND position = " + position + " AND id_canto = " + idNew, null);

            CustomList positionToDelete = new CustomList();
            positionToDelete.id = 1;
            positionToDelete.position = position;
            positionToDelete.idCanto = idNew;
            CustomListDao mDao = RisuscitoDatabase.getInstance(getContext()).customListDao();
            mDao.deletePosition(positionToDelete);

//            ContentValues values = new ContentValues();
//            values.put("id_canto", idNew);
//            db.update("CUST_LISTS", values, "_id = 1 AND position = " + posizioneDaCanc + " AND id_canto = " + idDaCanc, null);
            mDao.updatePositionNoTimestamp(idNew, 1, posizioneDaCanc, idDaCanc);

//            values = new ContentValues();
//            values.put("id_canto", idDaCanc);
//            values.put("timestamp", timestampNew);
//            values.put("_id", 1);
//            values.put("position", position);
//            db.insert("CUST_LISTS", null, values);
//            db.close();
            CustomList positionToInsert = new CustomList();
            positionToInsert.id = 1;
            positionToInsert.position = position;
            positionToInsert.idCanto = idDaCanc;
            positionToInsert.timestamp = new Date(Long.parseLong(timestampNew));
            mDao.insertPosition(positionToInsert);
//            updateLista();
//            View parent = (View) v.getParent().getParent();
//            cantoAdapter.notifyItemChanged(longclickedPos);
//            cantoAdapter.notifyItemChanged(Integer.valueOf(((TextView) parent.findViewById(R.id.tag)).getText().toString()));
            Snackbar.make(getActivity().findViewById(R.id.main_content), R.string.switch_done, Snackbar.LENGTH_SHORT)
                    .show();
        }
        else {
            Snackbar.make(rootView, R.string.switch_impossible, Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    private void scambioConVuoto(View parent, int position) {
//        Log.i(getClass().toString(), "posizioneDaCanc: " + posizioneDaCanc);
//        Log.i(getClass().toString(), "idDaCanc: " + idDaCanc);
//        Log.i(getClass().toString(), "timestampDaCanc: " + timestampDaCanc);
//        db = listaCanti.getReadableDatabase();
//        db.delete("CUST_LISTS", "_id = 1 AND position = " + posizioneDaCanc + " AND id_canto = " + idDaCanc, null);

        CustomList positionToDelete = new CustomList();
        positionToDelete.id = 1;
        positionToDelete.position = posizioneDaCanc;
        positionToDelete.idCanto = idDaCanc;
        CustomListDao mDao = RisuscitoDatabase.getInstance(getContext()).customListDao();
        mDao.deletePosition(positionToDelete);

//        ContentValues values = new ContentValues();
//        values.put("id_canto", idDaCanc);
//        values.put("timestamp", timestampDaCanc);
//        values.put("_id", 1);
//        values.put("position", position);
//        db.insert("CUST_LISTS", null, values);
//        db.close();

        CustomList positionToInsert = new CustomList();
        positionToInsert.id = 1;
        positionToInsert.position = position;
        positionToInsert.idCanto = idDaCanc;
        positionToInsert.timestamp = new Date(Long.parseLong(timestampDaCanc));
        mDao.insertPosition(positionToInsert);

//        updateLista();
//        cantoAdapter.notifyItemChanged(longclickedPos);
//        cantoAdapter.notifyItemChanged(Integer.valueOf(((TextView) parent.findViewById(R.id.tag)).getText().toString()));
        Snackbar.make(getActivity().findViewById(R.id.main_content), R.string.switch_done, Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public boolean onCabCreated(MaterialCab cab, Menu menu) {
        Log.d(TAG, "onCabCreated: ");
        cab.setMenu(R.menu.menu_actionmode_lists);
        cab.setTitle("");
        posizioniList.get(longclickedPos).second.get(longClickedChild).setmSelected(true);
        cantoAdapter.notifyItemChanged(longclickedPos);
        menu.findItem(R.id.action_switch_item).setIcon(
                new IconicsDrawable(getActivity(), CommunityMaterial.Icon.cmd_shuffle)
                        .sizeDp(24)
                        .paddingDp(2)
                        .colorRes(android.R.color.white));
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
//                db = listaCanti.getReadableDatabase();
//                db.delete("CUST_LISTS", "_id = 1 AND position = " + posizioneDaCanc + " AND id_canto = " + idDaCanc, null);
//                db.close();
//                updateLista();
//                cantoAdapter.notifyItemChanged(longclickedPos);
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                CustomList positionToDelete = new CustomList();
                                positionToDelete.id = 1;
                                positionToDelete.position = posizioneDaCanc;
                                positionToDelete.idCanto = idDaCanc;
                                CustomListDao mDao = RisuscitoDatabase.getInstance(getContext()).customListDao();
                                mDao.deletePosition(positionToDelete);
                            }
                        })
                        .start();

//                CustomList positionToDelete = new CustomList();
//                positionToDelete.id = 1;
//                positionToDelete.position = posizioneDaCanc;
//                positionToDelete.idCanto = idDaCanc;
//                CustomListDao mDao = RisuscitoDatabase.getInstance(getContext()).customListDao();
//                mDao.deletePosition(positionToDelete);
//
//                actionModeOk = true;
//                mMainActivity.getMaterialCab().finish();
//                Snackbar.make(getActivity().findViewById(R.id.main_content), R.string.song_removed, Snackbar.LENGTH_LONG)
//                        .setAction(getString(android.R.string.cancel).toUpperCase(), new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
////                                db = listaCanti.getReadableDatabase();
////                                ContentValues values = new ContentValues();
////                                values.put("_id", 1);
////                                values.put("position", posizioneDaCanc);
////                                values.put("id_canto", idDaCanc);
////                                values.put("timestamp", timestampDaCanc);
////                                db.insert("CUST_LISTS", null, values);
////                                db.close();
////                                updateLista();
////                                cantoAdapter.notifyItemChanged(longclickedPos);
//                                CustomList positionToInsert = new CustomList();
//                                positionToInsert.id = 1;
//                                positionToInsert.position = posizioneDaCanc;
//                                positionToInsert.idCanto = idDaCanc;
//                                positionToInsert.timestamp = new Date(Long.parseLong(timestampDaCanc));
//                                CustomListDao mDao = RisuscitoDatabase.getInstance(getContext()).customListDao();
//                                mDao.insertPosition(positionToInsert);
//
//                            }
//                        })
//                        .setActionTextColor(getThemeUtils().accentColor())
//                        .show();
                actionModeOk = true;
                mMainActivity.getMaterialCab().finish();
                Snackbar.make(getActivity().findViewById(R.id.main_content), R.string.song_removed, Snackbar.LENGTH_LONG)
                        .setAction(getString(android.R.string.cancel).toUpperCase(), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new Thread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                CustomList positionToInsert = new CustomList();
                                                positionToInsert.id = 1;
                                                positionToInsert.position = posizioneDaCanc;
                                                positionToInsert.idCanto = idDaCanc;
                                                positionToInsert.timestamp = new Date(Long.parseLong(timestampDaCanc));
                                                CustomListDao mDao = RisuscitoDatabase.getInstance(getContext()).customListDao();
                                                mDao.insertPosition(positionToInsert);
                                            }
                                        })
                                        .start();

                            }
                        })
                        .setActionTextColor(getThemeUtils().accentColor())
                        .show();
                mSwhitchMode = false;
                return true;
            case R.id.action_switch_item:
                mSwhitchMode = true;
                mMainActivity.getMaterialCab().setTitleRes(R.string.switch_started);
                Toast.makeText(getActivity()
                        , getResources().getString(R.string.switch_tooltip)
                        , Toast.LENGTH_SHORT).show();
                return true;
        }
        return true;
    }

    @Override
    public boolean onCabFinished(MaterialCab cab) {
        Log.d(TAG, "onCabFinished: ");
        mSwhitchMode = false;
        if (!actionModeOk) {
            try {
                posizioniList.get(longclickedPos).second.get(longClickedChild).setmSelected(false);
                cantoAdapter.notifyItemChanged(longclickedPos);
            }
            catch (Exception e){
                FirebaseCrash.log("Possibile crash - longclickedPos: " + longclickedPos);
            }
        }
        return true;
    }

    private void populateDb() {
        mCantiViewModel.createDb();
    }

    private void subscribeUiFavorites() {
        mCantiViewModel.getCantiParolaResult().observe(this, new Observer<List<Posizione>>() {
            @Override
            public void onChanged(@Nullable List<Posizione> canti) {
                posizioniList.clear();
                posizioniList.add(getCantofromPosition(canti, getString(R.string.canto_iniziale), 1, 0));
                posizioniList.add(getCantofromPosition(canti, getString(R.string.prima_lettura), 2, 1));
                posizioniList.add(getCantofromPosition(canti, getString(R.string.seconda_lettura), 3, 2));
                posizioniList.add(getCantofromPosition(canti, getString(R.string.terza_lettura), 4, 3));

                SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(getActivity());
                if (pref.getBoolean(Utility.SHOW_PACE, false)) {
                    posizioniList.add(getCantofromPosition(canti, getString(R.string.canto_pace), 6, 4));
                    posizioniList.add(getCantofromPosition(canti, getString(R.string.canto_fine), 5, 5));
                }
                else
                    posizioniList.add(getCantofromPosition(canti, getString(R.string.canto_fine), 5, 4));

                cantoAdapter.notifyDataSetChanged();
            }
        });
    }
}