package it.cammino.risuscito;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.OnLongClickListener;
import com.mikepenz.fastadapter_extensions.drag.ItemTouchCallback;
import com.mikepenz.fastadapter_extensions.drag.SimpleDragCallback;
import com.mikepenz.fastadapter_extensions.swipe.SimpleSwipeCallback;
import com.mikepenz.fastadapter_extensions.swipe.SimpleSwipeDragCallback;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.cammino.risuscito.database.ListaPers;
import it.cammino.risuscito.database.RisuscitoDatabase;
import it.cammino.risuscito.database.dao.ListePersDao;
import it.cammino.risuscito.dialogs.InputTextDialogFragment;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.items.SwipeableItem;
import it.cammino.risuscito.ui.SwipeDismissTouchListener;
import it.cammino.risuscito.ui.ThemeableActivity;

public class CreaListaActivity extends ThemeableActivity implements InputTextDialogFragment.SimpleInputCallback
        , SimpleDialogFragment.SimpleCallback
        , ItemTouchCallback
        , SimpleSwipeCallback.ItemSwipeCallback {

    private final String TAG = getClass().getCanonicalName();

    private ListaPersonalizzata celebrazione;
//    private DatabaseCanti listaCanti;
    private String titoloLista;
    private boolean modifica;
    private int idModifica;
    private RetainedFragment dataFragment;
    private RetainedFragment dataFragment2;
    private ArrayList<String> nomiCanti;

    private FastItemAdapter<SwipeableItem> mAdapter;
    private List<SwipeableItem> elementi;

    //drag & drop
    private ItemTouchHelper touchHelper;

    private int positionToRename;

    private final String TEMP_TITLE = "temp_title";

    @BindView(R.id.risuscito_toolbar) Toolbar mToolbar;
    @BindView(R.id.collapsingToolbarLayout) @Nullable CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.fab_crea_lista) FloatingActionButton fabCreaLista;
    @BindView(R.id.noElementsAdded) View mNoElementsAdded;
    @BindView(R.id.main_hint_layout) View mMainHintLayout;
    @BindView(R.id.hint_text) TextView mHintText;
    @BindView(R.id.textTitleDescription) View mTitleDescr;
    @BindView(R.id.question_mark) View mQuestionMark;
    @BindView(R.id.tabletToolbarBackground) @Nullable View mTabletBG;
    @BindView(R.id.action_title_bar) View mActionTitleBar;
    @BindView(R.id.textfieldTitle) EditText textFieldTitle;

    @OnClick(R.id.fab_crea_lista)
    public void aggiuntiPosizione() {
        new InputTextDialogFragment.Builder(CreaListaActivity.this, CreaListaActivity.this, "ADD_POSITION")
                .title(R.string.posizione_add_desc)
                .positiveButton(R.string.aggiungi_confirm)
                .negativeButton(android.R.string.cancel)
                .show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crea_lista);
        ButterKnife.bind(this);

        mToolbar.setBackgroundColor(getThemeUtils().primaryColor());
        setSupportActionBar(mToolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (mTabletBG != null)
            mTabletBG.setBackgroundColor(getThemeUtils().primaryColor());
        mActionTitleBar.setBackgroundColor(getThemeUtils().primaryColor());

//        listaCanti = new DatabaseCanti(this);

        textFieldTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (collapsingToolbarLayout != null)
                    collapsingToolbarLayout.setTitle(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        Drawable leaveBehindDrawable = new IconicsDrawable(CreaListaActivity.this)
                .icon(CommunityMaterial.Icon.cmd_delete)
                .colorRes(android.R.color.white)
                .sizeDp(24)
                .paddingDp(2);

        SimpleDragCallback touchCallback = new SimpleSwipeDragCallback(
                this,
                this,
                leaveBehindDrawable,
                ItemTouchHelper.LEFT,
                ContextCompat.getColor(this, R.color.md_red_900)
        )
                .withBackgroundSwipeRight(ContextCompat.getColor(this, R.color.md_red_900))
                .withLeaveBehindSwipeRight(leaveBehindDrawable);
        touchCallback.setIsDragEnabled(false);

        touchHelper = new ItemTouchHelper(touchCallback); // Create ItemTouchHelper and pass with parameter the SimpleDragCallback

//        Bundle bundle = this.getIntent().getExtras();
//        modifica = bundle != null && bundle.getBoolean("modifica");
//
//        if (modifica) {
//            SQLiteDatabase db = listaCanti.getReadableDatabase();
//
//            idModifica = bundle.getInt("idDaModif");
//
//            String query = "SELECT titolo_lista, lista"
//                    + "  FROM LISTE_PERS"
//                    + "  WHERE _id = " + idModifica;
//            Cursor cursor = db.rawQuery(query, null);
//
//            cursor.moveToFirst();
//            titoloLista = cursor.getString(0);
//            celebrazione = (ListaPersonalizzata) ListaPersonalizzata.deserializeObject(cursor.getBlob(1));
//            cursor.close();
//            db.close();
//        }
//        else
//            titoloLista = bundle != null ? bundle.getString("titolo") : null;
//
//        dataFragment = (RetainedFragment) getSupportFragmentManager().findFragmentByTag("nomiElementi");
//        if (dataFragment != null) {
//            elementi = dataFragment.getDataFrag();
//            for (SwipeableItem elemento: elementi)
//                elemento.withTouchHelper(touchHelper);
//        }
//        else {
//            elementi = new ArrayList<>();
//            if (modifica) {
//                for (int i = 0; i < celebrazione.getNumPosizioni(); i++) {
//                    elementi.add(new SwipeableItem().withName(celebrazione.getNomePosizione(i)).withTouchHelper(touchHelper).withIdentifier(Utility.random(0, 5000)));
//                }
//            }
//        }
//
//        if (modifica) {
//            dataFragment2 = (RetainedFragment) getSupportFragmentManager().findFragmentByTag("nomiCanti");
//            if (dataFragment2 != null) {
//                nomiCanti = dataFragment2.getData();
//            }
//            else {
//                nomiCanti = new ArrayList<>();
//                if (modifica) {
//                    for (int i = 0; i < celebrazione.getNumPosizioni(); i++) {
////		        		Log.i("CANTO", celebrazione.getCantoPosizione(i));
//                        nomiCanti.add(celebrazione.getCantoPosizione(i));
//                    }
//                }
//            }
//        }
//
//        if (savedInstanceState != null) {
//            textFieldTitle.setText(savedInstanceState.getCharSequence(TEMP_TITLE));
//            if (collapsingToolbarLayout != null)
//                collapsingToolbarLayout.setTitle(savedInstanceState.getCharSequence(TEMP_TITLE));
//        }
//        else {
//            textFieldTitle.setText(titoloLista);
//            if (collapsingToolbarLayout != null)
//                collapsingToolbarLayout.setTitle(titoloLista);
//        }

        OnLongClickListener<SwipeableItem> mLongClickListener = new OnLongClickListener<SwipeableItem>() {
            @Override
            public boolean onLongClick(View view, IAdapter<SwipeableItem> iAdapter, SwipeableItem item, int i) {
                Log.d(TAG, "onItemLongClick: " + i);
                positionToRename = i;
                new InputTextDialogFragment.Builder(CreaListaActivity.this, CreaListaActivity.this, "RENAME")
                        .title(R.string.posizione_rename)
                        .prefill(item.getName().getText().toString())
                        .positiveButton(R.string.aggiungi_rename)
                        .negativeButton(android.R.string.cancel)
                        .show();
                return false;
            }
        };

        mAdapter = new FastItemAdapter<>();
        elementi = new ArrayList<>();
        mAdapter.add(elementi);
        mAdapter.withOnLongClickListener(mLongClickListener);

        LinearLayoutManager llm = new LinearLayoutManager(CreaListaActivity.this);
        mRecyclerView.setLayoutManager(llm);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true); //Size of RV will not change

        DividerItemDecoration insetDivider = new DividerItemDecoration(CreaListaActivity.this, llm.getOrientation());
        insetDivider.setDrawable(ContextCompat.getDrawable(CreaListaActivity.this, R.drawable.preference_list_divider_material));
        mRecyclerView.addItemDecoration(insetDivider);

        touchHelper.attachToRecyclerView(mRecyclerView); // Attach ItemTouchHelper to RecyclerView

        new SearchTask().execute(savedInstanceState);

        IconicsDrawable icon = new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_plus)
                .color(Color.WHITE)
                .sizeDp(24)
                .paddingDp(4);
        fabCreaLista.setImageDrawable(icon);

        mTitleDescr.requestFocus();

        if (savedInstanceState != null) {
            Log.d(getClass().getName(), "onCreate: RESTORING");
            positionToRename = savedInstanceState.getInt("positionToRename", 0);
            InputTextDialogFragment iFragment = InputTextDialogFragment.findVisible(CreaListaActivity.this, "RENAME");
            if (iFragment != null)
                iFragment.setmCallback(CreaListaActivity.this);
            iFragment = InputTextDialogFragment.findVisible(CreaListaActivity.this, "ADD_POSITION");
            if (iFragment != null)
                iFragment.setmCallback(CreaListaActivity.this);
            SimpleDialogFragment fragment = SimpleDialogFragment.findVisible(CreaListaActivity.this, "SAVE_LIST");
            if (fragment != null)
                fragment.setmCallback(CreaListaActivity.this);
        }

//        mHintText.setText(getString(R.string.showcase_rename_desc) + "\n" + getString(R.string.showcase_delete_desc));
        mHintText.setText(R.string.showcase_rename_desc);
        mHintText.append(System.getProperty("line.separator"));
        mHintText.append(getString(R.string.showcase_delete_desc));
        ViewCompat.setElevation(mQuestionMark, 1);
        mMainHintLayout.setOnTouchListener(new SwipeDismissTouchListener(
                mMainHintLayout,
                null,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }

                    @Override
                    public void onDismiss(View view, Object token) {
                        mMainHintLayout.setVisibility(View.GONE);
                        SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(CreaListaActivity.this).edit();
                        prefEditor.putBoolean(Utility.INTRO_CREALISTA_2, true);
                        prefEditor.apply();
                    }
                }));

    }

//    @Override
//    public void onDestroy() {
//        if (listaCanti != null)
//            listaCanti.close();
//        super.onDestroy();
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        IconicsMenuInflaterUtil.inflate(getMenuInflater(), CreaListaActivity.this, R.menu.crea_lista_menu, menu);
        super.onCreateOptionsMenu(menu);
//        getMenuInflater().inflate(R.menu.crea_lista_menu, menu);
//        menu.findItem(R.id.action_save_list).setIcon(
//                new IconicsDrawable(CreaListaActivity.this, CommunityMaterial.Icon.cmd_content_save)
//                        .sizeDp(24)
//                        .paddingDp(2)
//                        .color(Color.WHITE));
//        menu.findItem(R.id.action_help).setIcon(
//                new IconicsDrawable(CreaListaActivity.this, CommunityMaterial.Icon.cmd_help_circle)
//                        .sizeDp(24)
//                        .paddingDp(2)
//                        .color(Color.WHITE));
        SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(CreaListaActivity.this);
        Log.d(TAG, "onCreateOptionsMenu - INTRO_CREALISTA: " + mSharedPrefs.getBoolean(Utility.INTRO_CREALISTA, false));
        if (!mSharedPrefs.getBoolean(Utility.INTRO_CREALISTA, false)) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    playIntro();
                }
            }, 1500);
        }
        if (mAdapter.getAdapterItems() == null || mAdapter.getAdapterItems().size() == 0 || mSharedPrefs.getBoolean(Utility.INTRO_CREALISTA_2, false))
            mMainHintLayout.setVisibility(View.GONE);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                playIntro();
                if (mAdapter.getAdapterItems() != null && mAdapter.getAdapterItems().size() > 0)
                    mMainHintLayout.setVisibility(View.VISIBLE);
                return true;
            case R.id.action_save_list:
//                if (saveList()) {
//                    setResult(Activity.RESULT_OK);
//                    finish();
//                    overridePendingTransition(0, R.anim.slide_out_bottom);
//                }
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (saveList()) {
                                    setResult(Activity.RESULT_OK);
                                    finish();
                                    overridePendingTransition(0, R.anim.slide_out_bottom);
                                }
                            }
                        })
                        .start();
                return true;
            case android.R.id.home:
                if (mAdapter.getAdapterItems().size() > 0) {
                    new SimpleDialogFragment.Builder(CreaListaActivity.this, CreaListaActivity.this, "SAVE_LIST")
                            .title(R.string.save_list_title)
                            .content(R.string.save_list_question)
                            .positiveButton(R.string.confirm)
                            .negativeButton(R.string.dismiss)
                            .neutralButton(android.R.string.cancel)
                            .show();
                    return true;
                }
                else {
                    setResult(Activity.RESULT_CANCELED);
          finish();
                    overridePendingTransition(0, R.anim.slide_out_bottom);
                }
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: ");
        if (mAdapter.getAdapterItems().size() > 0) {
            new SimpleDialogFragment.Builder(CreaListaActivity.this, CreaListaActivity.this, "SAVE_LIST")
                    .title(R.string.save_list_title)
                    .content(R.string.save_list_question)
                    .positiveButton(R.string.confirm)
                    .negativeButton(R.string.dismiss)
                    .neutralButton(android.R.string.cancel)
                    .show();
        }
        else {
            setResult(Activity.RESULT_CANCELED);
            finish();
            overridePendingTransition(0, R.anim.slide_out_bottom);
        }
    }

    private boolean saveList()  {
        celebrazione = new ListaPersonalizzata();

        if (textFieldTitle.getText() != null
                && !textFieldTitle.getText()
                .toString().trim().equalsIgnoreCase("")) {
            titoloLista = textFieldTitle.getText().toString();
        }
        else {
            Toast toast = Toast.makeText(CreaListaActivity.this
                    , getString(R.string.no_title_edited), Toast.LENGTH_SHORT);
            toast.show();
        }

        celebrazione.setName(titoloLista);
        SwipeableItem mElement;
        Log.d(TAG, "saveList - elementi.size(): " + mAdapter.getAdapterItems().size());
        for (int i = 0; i < mAdapter.getAdapterItems().size(); i++) {
            mElement = mAdapter.getItem(i);
            if (celebrazione.addPosizione(mElement.getName().getText().toString()) == -2) {
                Snackbar.make(findViewById(android.R.id.content)
                        , R.string.lista_pers_piena, Snackbar.LENGTH_SHORT)
                        .show();
                return false;
            }
        }

        if (celebrazione.getNomePosizione(0).equalsIgnoreCase("")) {
            Snackbar.make(findViewById(android.R.id.content)
                    , R.string.lista_pers_vuota, Snackbar.LENGTH_SHORT)
                    .show();
            return false;
        }

        if (modifica) {
            for (int i = 0; i < mAdapter.getAdapterItems().size(); i++) {
//    			Log.i("SALVO CANTO", nomiCanti.get(i));
                celebrazione.addCanto(nomiCanti.get(i), i);
            }
        }

//        SQLiteDatabase db = listaCanti.getReadableDatabase();
//
//        ContentValues  values = new  ContentValues();
//        values.put("titolo_lista" , titoloLista);
//        values.put("lista" , ListaPersonalizzata.serializeObject(celebrazione));
//
//        if (modifica)
//            db.update("LISTE_PERS", values, "_id = " + idModifica, null);
//        else
//            db.insert("LISTE_PERS" , "" , values);
//
//        db.close();

        ListePersDao mDao = RisuscitoDatabase.getInstance(CreaListaActivity.this).listePersDao();
        ListaPers listaToUpdate = new ListaPers();
        listaToUpdate.lista = celebrazione;
        listaToUpdate.titolo = titoloLista;
        if (modifica) {
            listaToUpdate.id = idModifica;
            mDao.updateLista(listaToUpdate);
        }
        else
            mDao.insertLista(listaToUpdate);

        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        dataFragment = new RetainedFragment();
        getSupportFragmentManager().beginTransaction().add(dataFragment, "nomiElementi").commit();
        dataFragment.setDataDrag(mAdapter.getAdapterItems());

        if (modifica) {
            dataFragment2 = new RetainedFragment();
            getSupportFragmentManager().beginTransaction().add(dataFragment2, "nomiCanti").commit();
            dataFragment2.setData(nomiCanti);
        }

        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("positionToRename", positionToRename);
        savedInstanceState.putCharSequence(TEMP_TITLE, textFieldTitle.getText());
    }

    public static class RetainedFragment extends Fragment {

        // data object we want to retain
        private ArrayList<String> data;
        private List<SwipeableItem> dataDrag;

        // this method is only called once for this fragment
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // retain this fragment
            setRetainInstance(true);
        }

        public void setData(ArrayList<String> data) {
            this.data = data;
        }

        public ArrayList<String> getData() {
            return data;
        }

        public List<SwipeableItem> getDataFrag() {
            return dataDrag;
        }

        public void setDataDrag(List<SwipeableItem> dataDrag) {
            this.dataDrag = dataDrag;
        }
    }

    @Override
    public void onPositive(@NonNull String tag, @NonNull MaterialDialog dialog) {
        Log.d(getClass().getName(), "onPositive: " + tag);
        switch (tag) {
            case "RENAME":
                EditText mEditText = dialog.getInputEditText();
                SwipeableItem mElement = mAdapter.getAdapterItems().get(positionToRename);
                mElement.withName(mEditText != null ? mEditText.getText().toString() : "NULL");
                mAdapter.notifyAdapterItemChanged(positionToRename);
                break;
            case "ADD_POSITION":
                mNoElementsAdded.setVisibility(View.GONE);
                mEditText = dialog.getInputEditText();
                if (modifica)
                    nomiCanti.add("");
                if (mAdapter.getAdapterItemCount() == 0) {
                    elementi.clear();
                    elementi.add(new SwipeableItem().withName(mEditText != null ? mEditText.getText().toString() : "NULL").withTouchHelper(touchHelper).withIdentifier(Utility.random(0, 5000)));
                    mAdapter.add(elementi);
                    mAdapter.notifyItemInserted(0);
                }
                else {
                    int mSize = mAdapter.getAdapterItemCount();
                    mAdapter.getAdapterItems().add(new SwipeableItem().withName(mEditText != null ? mEditText.getText().toString() : "NULL").withTouchHelper(touchHelper).withIdentifier(Utility.random(0, 5000)));
                    mAdapter.notifyAdapterItemInserted(mSize);
                }
                Log.d(TAG, "onPositive - elementi.size(): " + mAdapter.getAdapterItems().size());
                SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(CreaListaActivity.this);
                Log.d(TAG, "onCreateOptionsMenu - INTRO_CREALISTA_2: " + mSharedPrefs.getBoolean(Utility.INTRO_CREALISTA_2, false));
                if (!mSharedPrefs.getBoolean(Utility.INTRO_CREALISTA_2, false)) {
                    mMainHintLayout.setVisibility(View.VISIBLE);
                }
                break;
        }
    }
    @Override
    public void onNegative(@NonNull String tag, @NonNull MaterialDialog dialog) {}
    @Override
    public void onNeutral(@NonNull String tag, @NonNull MaterialDialog dialog) {}

    @Override
    public void onPositive(@NonNull String tag) {
        Log.d(getClass().getName(), "onPositive: " + tag);
        switch (tag) {
            case "SAVE_LIST":
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (saveList()) {
                                    setResult(Activity.RESULT_OK);
                                    finish();
                                    overridePendingTransition(0, R.anim.slide_out_bottom);
                                }
                            }
                        })
                        .start();
//                if (saveList()) {
//                    setResult(Activity.RESULT_OK);
//                    finish();
//                    overridePendingTransition(0, R.anim.slide_out_bottom);
//                }
                break;
        }
    }
    @Override
    public void onNegative(@NonNull String tag) {
        Log.d(getClass().getName(), "onNegative: " + tag);
        switch (tag) {
            case "SAVE_LIST":
                setResult(Activity.RESULT_CANCELED);
                finish();
                overridePendingTransition(0, R.anim.slide_out_bottom);
                break;
        }
    }
    @Override
    public void onNeutral(@NonNull String tag) { }

    @Override
    public boolean itemTouchOnMove(int oldPosition, int newPosition) {
        if (modifica)
            Collections.swap(nomiCanti, oldPosition, newPosition); // change canto
        Collections.swap(mAdapter.getAdapterItems(), oldPosition, newPosition); // change position
        mAdapter.notifyAdapterItemMoved(oldPosition, newPosition);
        return true;
    }

    @Override
    public void itemTouchDropped(int i, int i1) {}

    @Override
    public void itemSwiped(int position, int direction) {
        // -- Option 1: Direct action --
        //do something when swiped such as: select, remove, update, ...:
        //A) fastItemAdapter.select(position);
        //B) fastItemAdapter.remove(position);
        //C) update item, set "read" if an email etc

        // -- Option 2: Delayed action --
        final SwipeableItem item = mAdapter.getItem(position);
        item.setSwipedDirection(direction);

        // This can vary depending on direction but remove & archive simulated here both results in
        // removal from list
        final Runnable removeRunnable = new Runnable() {
            @Override
            public void run() {
                item.setSwipedAction(null);
                int position = mAdapter.getAdapterPosition(item);
                if (position != RecyclerView.NO_POSITION) {
                    //this sample uses a filter. If a filter is used we should use the methods provided by the filter (to make sure filter and normal state is updated)
                    mAdapter.getAdapterItems().remove(position);
                    mAdapter.notifyAdapterItemRemoved(position);
                    if (modifica)
                        nomiCanti.remove(position);
                    if (mAdapter.getAdapterItemCount() == 0) {
                        mNoElementsAdded.setVisibility(View.VISIBLE);
                        mMainHintLayout.setVisibility(View.GONE);
                    }
                }
            }
        };
        mRecyclerView.postDelayed(removeRunnable, 2000);

        item.setSwipedAction(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.removeCallbacks(removeRunnable);
                item.setSwipedDirection(0);
                int position = mAdapter.getAdapterPosition(item);
                if (position != RecyclerView.NO_POSITION) {
                    mAdapter.notifyItemChanged(position);
                }
            }
        });

        mAdapter.notifyItemChanged(position);

    }

    private void playIntro() {
        fabCreaLista.show();
        new TapTargetSequence(CreaListaActivity.this)
                .continueOnCancel(true)
                .targets(
                        TapTarget.forView(fabCreaLista
                                , getString(R.string.add_position), getString(R.string.showcase_add_pos_desc))
                                // All options below are optional
                                .outerCircleColorInt(getThemeUtils().primaryColor())     // Specify a color for the outer circle
                                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                                .textTypeface(Typeface.createFromAsset(getResources().getAssets(),"fonts/Roboto-Regular.ttf"))  // Specify a typeface for the text
                                .titleTextColor(R.color.primary_text_default_material_dark)
                                .textColor(R.color.secondary_text_default_material_dark)
                                .tintTarget(false)
                                .id(1)
                        ,
                        TapTarget.forToolbarMenuItem(mToolbar, R.id.action_save_list
                                , getString(R.string.list_save_exit), getString(R.string.showcase_saveexit_desc))
                                // All options below are optional
                                .outerCircleColorInt(getThemeUtils().primaryColor())     // Specify a color for the outer circle
                                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                                .textTypeface(Typeface.createFromAsset(getResources().getAssets(),"fonts/Roboto-Regular.ttf"))  // Specify a typeface for the text
                                .titleTextColor(R.color.primary_text_default_material_dark)
                                .textColor(R.color.secondary_text_default_material_dark)
                                .id(2)
                        ,
                        TapTarget.forToolbarMenuItem(mToolbar, R.id.action_help
                                , getString(R.string.showcase_end_title), getString(R.string.showcase_help_general))
                                // All options below are optional
                                .outerCircleColorInt(getThemeUtils().primaryColor())     // Specify a color for the outer circle
                                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                                .textTypeface(Typeface.createFromAsset(getResources().getAssets(),"fonts/Roboto-Regular.ttf"))  // Specify a typeface for the text
                                .titleTextColor(R.color.primary_text_default_material_dark)
                                .textColor(R.color.secondary_text_default_material_dark)
                                .id(3)
                )
                .listener(
                        new TapTargetSequence.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                            @Override
                            public void onSequenceFinish() {
                                Log.d(TAG, "onSequenceFinish: ");
                                SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(CreaListaActivity.this).edit();
                                prefEditor.putBoolean(Utility.INTRO_CREALISTA, true);
                                prefEditor.apply();

                            }

                            @Override
                            public void onSequenceStep(TapTarget tapTarget, boolean b) {}

                            @Override
                            public void onSequenceCanceled(TapTarget tapTarget) {
                                Log.d(TAG, "onSequenceCanceled: ");
                                SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(CreaListaActivity.this).edit();
                                prefEditor.putBoolean(Utility.INTRO_CREALISTA, true);
                                prefEditor.apply();
                            }
                        }).start();
    }

    private class SearchTask extends AsyncTask<Bundle, Void, Integer> {

        @Override
        protected Integer doInBackground(Bundle... savedInstanceState) {

            Bundle bundle = CreaListaActivity.this.getIntent().getExtras();
            modifica = bundle != null && bundle.getBoolean("modifica");

            if (modifica) {
                idModifica = bundle.getInt("idDaModif");
                ListePersDao mDao = RisuscitoDatabase.getInstance(CreaListaActivity.this).listePersDao();
                ListaPers lista = mDao.getListById(idModifica);
                titoloLista = lista.titolo;
                celebrazione = lista.lista;
            }
            else
                titoloLista = bundle != null ? bundle.getString("titolo") : null;

            dataFragment = (RetainedFragment) getSupportFragmentManager().findFragmentByTag("nomiElementi");
            if (dataFragment != null) {
                elementi = dataFragment.getDataFrag();
                for (SwipeableItem elemento: elementi)
                    elemento.withTouchHelper(touchHelper);
            }
            else {
                elementi = new ArrayList<>();
                if (modifica) {
                    for (int i = 0; i < celebrazione.getNumPosizioni(); i++) {
                        elementi.add(new SwipeableItem().withName(celebrazione.getNomePosizione(i)).withTouchHelper(touchHelper).withIdentifier(Utility.random(0, 5000)));
                    }
                }
            }

            if (modifica) {
                dataFragment2 = (RetainedFragment) getSupportFragmentManager().findFragmentByTag("nomiCanti");
                if (dataFragment2 != null) {
                    nomiCanti = dataFragment2.getData();
                }
                else {
                    nomiCanti = new ArrayList<>();
                    if (modifica) {
                        for (int i = 0; i < celebrazione.getNumPosizioni(); i++) {
//		        		Log.i("CANTO", celebrazione.getCantoPosizione(i));
                            nomiCanti.add(celebrazione.getCantoPosizione(i));
                        }
                    }
                }
            }

            if (savedInstanceState[0] != null) {
                textFieldTitle.setText(savedInstanceState[0].getCharSequence(TEMP_TITLE));
                if (collapsingToolbarLayout != null)
                    collapsingToolbarLayout.setTitle(savedInstanceState[0].getCharSequence(TEMP_TITLE));
            }
            else {
                textFieldTitle.setText(titoloLista);
                if (collapsingToolbarLayout != null)
                    collapsingToolbarLayout.setTitle(titoloLista);
            }

            return 0;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mAdapter.clear();
        }

        @Override
        protected void onPostExecute(Integer result) {
            mAdapter.add(elementi);
            mAdapter.notifyAdapterDataSetChanged();
            if (elementi.size() > 0)
                mNoElementsAdded.setVisibility(View.GONE);
        }
    }

}