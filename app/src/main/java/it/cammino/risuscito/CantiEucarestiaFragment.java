package it.cammino.risuscito;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
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
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialcab.MaterialCab;
import com.google.firebase.crash.FirebaseCrash;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.cammino.risuscito.adapters.PosizioneRecyclerAdapter;
import it.cammino.risuscito.objects.PosizioneItem;
import it.cammino.risuscito.objects.PosizioneTitleItem;
import it.cammino.risuscito.ui.BottomSheetFragment;
import it.cammino.risuscito.ui.ThemeableActivity;
import it.cammino.risuscito.utils.ThemeUtils;

public class CantiEucarestiaFragment extends Fragment implements MaterialCab.Callback {

    private final String TAG = getClass().getCanonicalName();

    // create boolean for fetching data
    private boolean isViewShown = true;

    private int posizioneDaCanc;
    private int idDaCanc;
    private String timestampDaCanc;
    private View rootView;
    private DatabaseCanti listaCanti;
    private SQLiteDatabase db;
    //    public ActionMode mMode;
    private boolean mSwhitchMode;
    private List<Pair<PosizioneTitleItem, List<PosizioneItem>>> posizioniList;
    private int longclickedPos, longClickedChild;
    private PosizioneRecyclerAdapter cantoAdapter;
    private boolean actionModeOk;

    private MainActivity mMainActivity;

    public static final int TAG_INSERT_EUCARESTIA = 444;

    private LUtils mLUtils;

    private long mLastClickTime = 0;

    @BindView(R.id.recycler_list) RecyclerView mRecyclerView;

    @OnClick(R.id.button_pulisci)
    public void pulisciLista() {
        db = listaCanti.getReadableDatabase();
        String sql = "DELETE FROM CUST_LISTS" +
                " WHERE _id =  2 ";
        db.execSQL(sql);
        db.close();
        updateLista();
        cantoAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.button_condividi)
    public void condividiLista() {
//                Log.i(getClass().toString(), "cantieucarestia");
        BottomSheetFragment bottomSheetDialog = BottomSheetFragment.newInstance(R.string.share_by, getDefaultIntent());
        bottomSheetDialog.show(getFragmentManager(), null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_lista_personalizzata, container, false);
        ButterKnife.bind(this, rootView);

        mMainActivity = (MainActivity) getActivity();

        //crea un istanza dell'oggetto DatabaseCanti
        listaCanti = new DatabaseCanti(getActivity());

//        rootView.findViewById(R.id.button_pulisci).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                Log.i(getClass().toString(), "cantieucarestia");
//                db = listaCanti.getReadableDatabase();
//                String sql = "DELETE FROM CUST_LISTS" +
//                        " WHERE _id =  2 ";
//                db.execSQL(sql);
//                db.close();
//                updateLista();
//                cantoAdapter.notifyDataSetChanged();
//            }
//        });

//        rootView.findViewById(R.id.button_condividi).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                Log.i(getClass().toString(), "cantieucarestia");
//                BottomSheetFragment bottomSheetDialog = BottomSheetFragment.newInstance(R.string.share_by, getDefaultIntent());
//                bottomSheetDialog.show(getFragmentManager(), null);
//            }
//        });

        mLUtils = LUtils.getInstance(getActivity());
//        mMode = null;
        mSwhitchMode = false;

        updateLista();

        OnClickListener click = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
                    return;
                mLastClickTime = SystemClock.elapsedRealtime();
                View parent = (View) v.getParent().getParent();
                if (v.getId() == R.id.addCantoGenerico) {
                    if (mSwhitchMode)
                        scambioConVuoto(parent, Integer.valueOf(((TextView) parent.findViewById(R.id.text_id_posizione)).getText().toString()));
                    else {
                        if (!mMainActivity.getMaterialCab().isActive()) {
//                        if (mMode == null) {
                            Bundle bundle = new Bundle();
                            bundle.putInt("fromAdd", 1);
                            bundle.putInt("idLista", 2);
                            bundle.putInt("position", Integer.valueOf(((TextView) parent.findViewById(R.id.text_id_posizione)).getText().toString()));
                            startSubActivity(bundle);
                        }
                    }
                }
                else {
                    if (!mSwhitchMode)
                        if (mMainActivity.getMaterialCab().isActive()) {
//                        if (mMode != null) {
                            posizioneDaCanc = Integer.valueOf(((TextView) parent.findViewById(R.id.text_id_posizione)).getText().toString());
                            idDaCanc = Integer.valueOf(((TextView) v.findViewById(R.id.text_id_canto)).getText().toString());
                            timestampDaCanc = ((TextView) v.findViewById(R.id.text_timestamp)).getText().toString();
                            snackBarRimuoviCanto(v);
                        }
                        else
                            openPagina(v);
                    else {
                        scambioCanto(v, Integer.valueOf(((TextView) parent.findViewById(R.id.text_id_posizione)).getText().toString()));
                    }
                }
            }
        };

        View.OnLongClickListener longClick = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                View parent = (View) v.getParent().getParent();
                posizioneDaCanc = Integer.valueOf(((TextView) parent.findViewById(R.id.text_id_posizione)).getText().toString());
                idDaCanc = Integer.valueOf(((TextView) v.findViewById(R.id.text_id_canto)).getText().toString());
                timestampDaCanc = ((TextView) v.findViewById(R.id.text_timestamp)).getText().toString();
                snackBarRimuoviCanto(v);
                return true;
            }
        };

//        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_list);

        // Creating new adapter object
        cantoAdapter = new PosizioneRecyclerAdapter(getActivity(), posizioniList, click, longClick);
        mRecyclerView.setAdapter(cantoAdapter);

        // Setting the layoutManager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (!isViewShown) {
            if (mMainActivity.getMaterialCab().isActive())
                mMainActivity.getMaterialCab().finish();
            FloatingActionButton fab1 = ((CustomLists) getParentFragment()).getFab();
            fab1.show();
//            mLUtils.animateIn(fab1);
        }


        return rootView;
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
//                mLUtils.animateIn(fab1);
            }
            else
                isViewShown = false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.i(getClass().getName(), "requestCode: " + requestCode);
        if (requestCode == TAG_INSERT_EUCARESTIA && resultCode == Activity.RESULT_OK) {
            updateLista();
            cantoAdapter.notifyDataSetChanged();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        if (listaCanti != null)
            listaCanti.close();
//        if (mMode != null)
//            mMode.finish();
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

    private void startSubActivity(Bundle bundle) {
        Intent intent = new Intent(getActivity(), GeneralInsertSearch.class);
        intent.putExtras(bundle);
        getParentFragment().startActivityForResult(intent, TAG_INSERT_EUCARESTIA);
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.hold_on);
    }

    private void openPagina(View v) {
        Bundle bundle = new Bundle();
        bundle.putString("pagina", ((TextView) v.findViewById(R.id.text_source_canto)).getText().toString());
        bundle.putInt("idCanto", Integer.valueOf(((TextView) v.findViewById(R.id.text_id_canto)).getText().toString()));

        Intent intent = new Intent(getActivity(), PaginaRenderActivity.class);
        intent.putExtras(bundle);
        mLUtils.startActivityWithTransition(intent, v, Utility.TRANS_PAGINA_RENDER);
    }

    private void updateLista() {

        if (posizioniList == null)
            posizioniList = new ArrayList<>();
        else
            posizioniList.clear();

        int progressiveTag = 0;
        SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(getActivity());

        posizioniList.add(getCantofromPosition(getString(R.string.canto_iniziale), 1, progressiveTag++));

        if (pref.getBoolean(Utility.SHOW_SECONDA, false))
            posizioniList.add(getCantofromPosition(getString(R.string.seconda_lettura), 6, progressiveTag++));

        posizioniList.add(getCantofromPosition(getString(R.string.canto_pace), 2, progressiveTag++));

        if (pref.getBoolean(Utility.SHOW_OFFERTORIO, false))
            posizioniList.add(getCantofromPosition(getString(R.string.canto_offertorio), 8, progressiveTag++));

        if (pref.getBoolean(Utility.SHOW_SANTO, false))
            posizioniList.add(getCantofromPosition(getString(R.string.santo), 7, progressiveTag++));

        posizioniList.add(getCantofromPosition(getString(R.string.canto_pane), 3, progressiveTag++));
        posizioniList.add(getCantofromPosition(getString(R.string.canto_vino), 4, progressiveTag++));
        posizioniList.add(getCantofromPosition(getString(R.string.canto_fine), 5, progressiveTag));

    }

    //recupera il titolo del canto in posizione "position" nella lista
    private Pair<PosizioneTitleItem, List<PosizioneItem>> getCantofromPosition(String titoloPosizione, int position, int tag) {

        db = listaCanti.getReadableDatabase();

        String query = "SELECT B.titolo, B.color, B.pagina, B.source, B._id, A.timestamp" +
                "  FROM CUST_LISTS A" +
                "  	   , ELENCO B" +
                "  WHERE A._id = 2" +
                "  AND   A.position = " + position +
                "  AND   A.id_canto = B._id";
        Cursor cursor = db.rawQuery(query, null);

        int total = cursor.getCount();

        List<PosizioneItem> list = new ArrayList<>();
        if (total > 0) {
            cursor.moveToFirst();

            list.add(new PosizioneItem(
                    cursor.getInt(2)
                    , cursor.getString(0)
                    , cursor.getString(1)
                    , cursor.getInt(4)
                    , cursor.getString(3)
                    , cursor.getString(5)));

            while (cursor.moveToNext()) {
                list.add(new PosizioneItem(
                        cursor.getInt(2)
                        , cursor.getString(0)
                        , cursor.getString(1)
                        , cursor.getInt(4)
                        , cursor.getString(3)
                        , cursor.getString(5)));
            }
        }

        Pair<PosizioneTitleItem, List<PosizioneItem>> result = new Pair(new PosizioneTitleItem(titoloPosizione
                , 2
                , position
                , tag
                , position == 4 || position == 3), list);

        cursor.close();
        db.close();

        return result;

    }

    private String getTitlesList() {

        int progressivePos = 0;

//        Locale l = getActivity().getResources().getConfiguration().locale;
        Locale l = ThemeableActivity.getSystemLocalWrapper(getActivity().getResources().getConfiguration());
        String result = "";
        String temp;

        //titolo
        result +=  "-- " + getString(R.string.title_activity_canti_eucarestia).toUpperCase(l) + " --\n";

        //canto iniziale
        temp = getTitoloToSendFromPosition(progressivePos++);

        result += getResources().getString(R.string.canto_iniziale).toUpperCase(l);
        result += "\n";

        if (temp.equalsIgnoreCase(""))
            result += ">> " + getString(R.string.to_be_chosen) + " <<";
        else
            result += temp;

        result += "\n";

        //deve essere messa anche la seconda lettura? legge le impostazioni
        SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(getActivity());
//        boolean showSeconda = pref.getBoolean(Utility.SHOW_SECONDA, false);

        //canto alla seconda lettura
        if (pref.getBoolean(Utility.SHOW_SECONDA, false)) {
            temp = getTitoloToSendFromPosition(progressivePos++);

            result += getResources().getString(R.string.seconda_lettura).toUpperCase(l);
            result += "\n";

            if (temp.equalsIgnoreCase(""))
                result += ">> " + getString(R.string.to_be_chosen) + " <<";
            else
                result += temp;

            result += "\n";
        }

        //canto alla pace
        temp = getTitoloToSendFromPosition(progressivePos++);

        result += getResources().getString(R.string.canto_pace).toUpperCase(l);
        result += "\n";

        if (temp.equalsIgnoreCase(""))
            result += ">> " + getString(R.string.to_be_chosen) + " <<";
        else
            result += temp;

        result += "\n";

//            boolean showOffertorio = pref.getBoolean(Utility.SHOW_OFFERTORIO, false);
        //offertorio
        if (pref.getBoolean(Utility.SHOW_OFFERTORIO, false)) {
            temp = getTitoloToSendFromPosition(progressivePos++);

            result += getResources().getString(R.string.canto_offertorio).toUpperCase(l);
            result += "\n";

            if (temp.equalsIgnoreCase(""))
                result += ">> " + getString(R.string.to_be_chosen) + " <<";
            else
                result += temp;

            result += "\n";
        }

//                boolean showSanto = pref.getBoolean(Utility.SHOW_SANTO, false);
        //santo
        if (pref.getBoolean(Utility.SHOW_SANTO, false)) {
            temp = getTitoloToSendFromPosition(progressivePos++);

            result += getResources().getString(R.string.santo).toUpperCase(l);
            result += "\n";

            if (temp.equalsIgnoreCase(""))
                result += ">> " + getString(R.string.to_be_chosen) + " <<";
            else
                result += temp;

            result += "\n";
        }

        //canti al pane
        temp = getTitoloToSendFromPosition(progressivePos++);

        result += getResources().getString(R.string.canto_pane).toUpperCase(l);
        result += "\n";

        if (temp.equalsIgnoreCase(""))
            result += ">> " + getString(R.string.to_be_chosen) + " <<";
        else
            result += temp;

        result += "\n";

        //canti al vino
        temp = getTitoloToSendFromPosition(progressivePos++);

        result += getResources().getString(R.string.canto_vino).toUpperCase(l);
        result += "\n";

        if (temp.equalsIgnoreCase(""))
            result += ">> " + getString(R.string.to_be_chosen) + " <<";
        else
            result += temp;

        result += "\n";

        //canto finale
        temp = getTitoloToSendFromPosition(progressivePos);

        result += getResources().getString(R.string.canto_fine).toUpperCase(l);
        result += "\n";

        if (temp.equalsIgnoreCase(""))
            result += ">> " + getString(R.string.to_be_chosen) + " <<";
        else
            result += temp;

        return result;

    }

    //recupera il titolo del canto in posizione "position" nella lista 2
    private String getTitoloToSendFromPosition(int position) {

        String result = "";

        List<PosizioneItem> items = posizioniList.get(position).second;

        if (items.size() > 0) {
            for (PosizioneItem tempItem: items) {
                result += tempItem.getTitolo() + " - " + getString(R.string.page_contracted) + tempItem.getPagina();
                result += "\n";
            }
        }

        return result;
    }

    public void snackBarRimuoviCanto(View view) {
//        if (mMode != null)
//            mMode.finish();
        if (mMainActivity.getMaterialCab().isActive())
            mMainActivity.getMaterialCab().finish();
        View parent = (View) view.getParent().getParent();
        longclickedPos = Integer.valueOf(((TextView)parent.findViewById(R.id.tag)).getText().toString());
        longClickedChild = Integer.valueOf(((TextView)view.findViewById(R.id.item_tag)).getText().toString());
//        mMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ModeCallback());
        mMainActivity.getAppBarLayout().setExpanded(true, true);
        mMainActivity.getMaterialCab().start(CantiEucarestiaFragment.this);
    }

    private ThemeUtils getThemeUtils() {
        return ((MainActivity)getActivity()).getThemeUtils();
    }

//    private final class ModeCallback implements ActionMode.Callback {
//
//        @Override
//        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//            // Create the menu from the xml file
//            posizioniList.get(longclickedPos).second.get(longClickedChild).setmSelected(true);
//            cantoAdapter.notifyItemChanged(longclickedPos);
//            getActivity().getMenuInflater().inflate(R.menu.menu_actionmode_lists, menu);
//            menu.findItem(R.id.action_switch_item).setIcon(
//                    new IconicsDrawable(getActivity(), CommunityMaterial.Icon.cmd_shuffle)
//                            .sizeDp(24)
//                            .paddingDp(2)
//                            .colorRes(R.color.icon_ative_black));
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
//            mSwhitchMode = false;
//            if (!actionModeOk) {
//                posizioniList.get(longclickedPos).second.get(longClickedChild).setmSelected(false);
//                cantoAdapter.notifyItemChanged(longclickedPos);
//            }
//            if (mode == mMode)
//                mMode = null;
//        }
//
//        @Override
//        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//            switch(item.getItemId()) {
//                case R.id.action_remove_item:
//                    db = listaCanti.getReadableDatabase();
//                    db.delete("CUST_LISTS", "_id = 2 AND position = " + posizioneDaCanc + " AND id_canto = " + idDaCanc, null);
//                    db.close();
//                    updateLista();
//                    cantoAdapter.notifyItemChanged(longclickedPos);
//                    actionModeOk = true;
//                    mode.finish();
//                    Snackbar.make(getActivity().findViewById(R.id.main_content), R.string.song_removed, Snackbar.LENGTH_LONG)
//                            .setAction(R.string.cancel, new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    db = listaCanti.getReadableDatabase();
//                                    ContentValues values = new ContentValues();
//                                    values.put("_id", 2);
//                                    values.put("position", posizioneDaCanc);
//                                    values.put("id_canto", idDaCanc);
//                                    values.put("timestamp", timestampDaCanc);
//                                    db.insert("CUST_LISTS", null, values);
//                                    db.close();
//                                    updateLista();
//                                    cantoAdapter.notifyItemChanged(longclickedPos);
//                                }
//                            })
//                            .setActionTextColor(getThemeUtils().accentColor())
//                            .show();
//                    mSwhitchMode = false;
//                    break;
//                case R.id.action_switch_item:
//                    mSwhitchMode = true;
//                    mode.setTitle(R.string.switch_started);
//                    Toast.makeText(getActivity()
//                            , getResources().getString(R.string.switch_tooltip)
//                            , Toast.LENGTH_SHORT).show();
//                    break;
//            }
//            return true;
//        }
//    }

    private void scambioCanto(View v, int position) {
        db = listaCanti.getReadableDatabase();
        int idNew = Integer.valueOf(((TextView) v.findViewById(R.id.text_id_canto)).getText().toString());
        String timestampNew = ((TextView) v.findViewById(R.id.text_timestamp)).getText().toString();
//        Log.i(getClass().toString(), "positionNew: " + position);
//        Log.i(getClass().toString(), "idNew: " + idNew);
//        Log.i(getClass().toString(), "timestampNew: " + timestampNew);
//        Log.i(getClass().toString(), "posizioneDaCanc: " + posizioneDaCanc);
//        Log.i(getClass().toString(), "idDaCanc: " + idDaCanc);
//        Log.i(getClass().toString(), "timestampDaCanc: " + timestampDaCanc);
        if (idNew != idDaCanc || posizioneDaCanc != position) {

            db.delete("CUST_LISTS", "_id = 2 AND position = " + position + " AND id_canto = " + idNew, null);

            ContentValues values = new ContentValues();
            values.put("id_canto", idNew);
            db.update("CUST_LISTS", values, "_id = 2 AND position = " + posizioneDaCanc + " AND id_canto = " + idDaCanc, null);

            values = new ContentValues();
            values.put("id_canto", idDaCanc);
            values.put("timestamp", timestampNew);
            values.put("_id", 2);
            values.put("position", position);
            db.insert("CUST_LISTS", null, values);
            db.close();

            mSwhitchMode = false;
            actionModeOk = true;
//            mMode.finish();
            mMainActivity.getMaterialCab().finish();
            updateLista();
            View parent = (View) v.getParent().getParent();
            cantoAdapter.notifyItemChanged(longclickedPos);
            cantoAdapter.notifyItemChanged(Integer.valueOf(((TextView)parent.findViewById(R.id.tag)).getText().toString()));
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
        db = listaCanti.getReadableDatabase();
        db.delete("CUST_LISTS", "_id = 2 AND position = " + posizioneDaCanc + " AND id_canto = " + idDaCanc, null);

        ContentValues values = new ContentValues();
        values.put("id_canto", idDaCanc);
        values.put("timestamp", timestampDaCanc);
        values.put("_id", 2);
        values.put("position", position);
        db.insert("CUST_LISTS", null, values);
        db.close();

        mSwhitchMode = false;
        actionModeOk = true;
//        mMode.finish();
        mMainActivity.getMaterialCab().finish();
        updateLista();
        cantoAdapter.notifyItemChanged(longclickedPos);
        cantoAdapter.notifyItemChanged(Integer.valueOf(((TextView) parent.findViewById(R.id.tag)).getText().toString()));
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
        cab.getToolbar().setNavigationIcon(new IconicsDrawable(getActivity(), CommunityMaterial.Icon.cmd_close_circle_outline)
                .sizeDp(24)
                .paddingDp(2)
                .colorRes(android.R.color.white));
        actionModeOk = false;
        return true;
    }

    @Override
    public boolean onCabItemClicked(MenuItem item) {
        Log.d(TAG, "onCabItemClicked: ");
        switch(item.getItemId()) {
            case R.id.action_remove_item:
                db = listaCanti.getReadableDatabase();
                db.delete("CUST_LISTS", "_id = 2 AND position = " + posizioneDaCanc + " AND id_canto = " + idDaCanc, null);
                db.close();
                updateLista();
                cantoAdapter.notifyItemChanged(longclickedPos);
                actionModeOk = true;
                mMainActivity.getMaterialCab().finish();
                Snackbar.make(getActivity().findViewById(R.id.main_content), R.string.song_removed, Snackbar.LENGTH_LONG)
                        .setAction(R.string.cancel, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                db = listaCanti.getReadableDatabase();
                                ContentValues values = new ContentValues();
                                values.put("_id", 2);
                                values.put("position", posizioneDaCanc);
                                values.put("id_canto", idDaCanc);
                                values.put("timestamp", timestampDaCanc);
                                db.insert("CUST_LISTS", null, values);
                                db.close();
                                updateLista();
                                cantoAdapter.notifyItemChanged(longclickedPos);
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
        return false;
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
}