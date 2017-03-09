package it.cammino.risuscito;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.stephentuso.welcome.WelcomeHelper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.cammino.risuscito.adapters.DraggableSwipeableAdapter;
import it.cammino.risuscito.dialogs.InputTextDialogFragment;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.objects.DraggableItem;
import it.cammino.risuscito.slides.IntroCreaListaNew;
import it.cammino.risuscito.ui.ThemeableActivity;

public class CreaListaActivity extends ThemeableActivity implements InputTextDialogFragment.SimpleInputCallback, SimpleDialogFragment.SimpleCallback {

    private ListaPersonalizzata celebrazione;
    private DatabaseCanti listaCanti;
    private ArrayList<DraggableItem> elementi;
    private String titoloLista;
    private boolean modifica;
    private int idModifica;
    private RetainedFragment dataFragment;
    private RetainedFragment dataFragment2;
    private RetainedFragment dataFragment3;
    private ArrayList<String> nomiCanti;
    private Bundle tempArgs;

    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;

    private EditText textfieldTitle;

    private int positionToRename;
    private WelcomeHelper mWelcomeScreen;

    private final String TEMP_TITLE = "temp_title";

    @BindView(R.id.risuscito_toolbar) Toolbar mToolbar;
    @BindView(R.id.collapsingToolbarLayout) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;

    @OnClick(R.id.fab_crea_lista)
    public void aggiuntiPosizione() {
        new InputTextDialogFragment.Builder(CreaListaActivity.this, CreaListaActivity.this, "ADD_POSITION")
                .title(R.string.posizione_add_desc)
                .positiveButton(R.string.aggiungi_confirm)
                .negativeButton(R.string.aggiungi_dismiss)
                .show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crea_lista);
        ButterKnife.bind(this);

//        Toolbar mToolbar = (Toolbar) findViewById(R.id.risuscito_toolbar);
        mToolbar.setBackgroundColor(getThemeUtils().primaryColor());
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.action_title_bar).setBackgroundColor(getThemeUtils().primaryColor());

        listaCanti = new DatabaseCanti(this);

//        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbarLayout);
        textfieldTitle = (EditText)findViewById(R.id.textfieldTitle);

        textfieldTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                collapsingToolbarLayout.setTitle(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        Bundle bundle = this.getIntent().getExtras();
        modifica = bundle.getBoolean("modifica");

        if (modifica) {
            SQLiteDatabase db = listaCanti.getReadableDatabase();

            idModifica = bundle.getInt("idDaModif");

            String query = "SELECT titolo_lista, lista"
                    + "  FROM LISTE_PERS"
                    + "  WHERE _id = " + idModifica;
            Cursor cursor = db.rawQuery(query, null);

            cursor.moveToFirst();
            titoloLista = cursor.getString(0);
            celebrazione = (ListaPersonalizzata) ListaPersonalizzata.deserializeObject(cursor.getBlob(1));
            cursor.close();
            db.close();
        }
        else
            titoloLista = bundle.getString("titolo");

        dataFragment = (RetainedFragment) getSupportFragmentManager().findFragmentByTag("nomiElementi");
        if (dataFragment != null) {
            elementi = dataFragment.getDataDrag();
        }
        else {
            elementi = new ArrayList<>();
            if (modifica) {
                for (int i = 0; i < celebrazione.getNumPosizioni(); i++)
                    elementi.add(new DraggableItem(celebrazione.getNomePosizione(i), Utility.random(1,500)));
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

        dataFragment3 = (RetainedFragment) getSupportFragmentManager().findFragmentByTag(TEMP_TITLE);
        if (dataFragment3 != null) {
            tempArgs = dataFragment3.getArguments();
            textfieldTitle.setText(tempArgs.getCharSequence(TEMP_TITLE));
            collapsingToolbarLayout.setTitle(tempArgs.getCharSequence(TEMP_TITLE));
        }
        else {
            textfieldTitle.setText(titoloLista);
            collapsingToolbarLayout.setTitle(titoloLista);
        }

        //noinspection ConstantConditions
//        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(CreaListaActivity.this);

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        // drag & drop manager
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
//        if (LUtils.hasL())
//            mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(
//                    (NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z3, getTheme()));
//        else
//            mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(
//                    (NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z3));
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(
                (NinePatchDrawable) ContextCompat.getDrawable(CreaListaActivity.this, R.drawable.material_shadow_z3));

        // swipe manager
        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        //adapter
        final DraggableSwipeableAdapter myItemAdapter = new DraggableSwipeableAdapter(CreaListaActivity.this
                ,elementi);
        myItemAdapter.setEventListener(new DraggableSwipeableAdapter.EventListener() {
            @Override
            public void onItemRemoved(int position) {
                if (modifica) {
                    nomiCanti.remove(position);
//                    	Log.i("RIMOSSO", which + "");
                }
                if (mAdapter.getItemCount() == 0)
                    findViewById(R.id.noElementsAdded).setVisibility(View.VISIBLE);
            }

            @Override
            public void onItemMoved(int from, int to)  {
                if (modifica) {
//                    	Log.i("SPOSTO CANTO", "da " + from + " a " + to);
                    nomiCanti.add(to, nomiCanti.remove(from));
                }
            }

            @Override
            public void onItemViewLongClicked(View v) {
                positionToRename = mRecyclerView.getChildAdapterPosition(v);
                new InputTextDialogFragment.Builder(CreaListaActivity.this, CreaListaActivity.this, "RENAME")
                        .title(R.string.posizione_rename)
                        .prefill(elementi.get(positionToRename).getTitolo())
                        .positiveButton(R.string.aggiungi_rename)
                        .negativeButton(R.string.aggiungi_dismiss)
                        .show();
            }

        });

        mAdapter = myItemAdapter;

        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(myItemAdapter);      // wrap for dragging
        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(mWrappedAdapter);      // wrap for swiping

        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();
        animator.setSupportsChangeAnimations(false);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(animator);

//        if (LUtils.hasL())
//            mRecyclerView.addItemDecoration(
//                    new SimpleListDividerDecorator(getResources().getDrawable(R.drawable.list_divider, getTheme()), true));
//        else
//            mRecyclerView.addItemDecoration(
//                    new SimpleListDividerDecorator(getResources().getDrawable(R.drawable.list_divider), true));
        mRecyclerView.addItemDecoration(
                new SimpleListDividerDecorator(ContextCompat.getDrawable(CreaListaActivity.this, R.drawable.list_divider), true));

        // NOTE:
        // The initialization order is very important! This order determines the priority of touch event handling.
        //
        // priority: TouchActionGuard > Swipe > DragAndDrop
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewSwipeManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);

        FloatingActionButton fabAdd = (FloatingActionButton) findViewById(R.id.fab_crea_lista);
        IconicsDrawable icon = new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_plus)
                .color(Color.WHITE)
                .sizeDp(24)
                .paddingDp(4);
        fabAdd.setImageDrawable(icon);
//        fabAdd.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                new InputTextDialogFragment.Builder(CreaListaActivity.this, CreaListaActivity.this, "ADD_POSITION")
//                        .title(R.string.posizione_add_desc)
//                        .positiveButton(R.string.aggiungi_confirm)
//                        .negativeButton(R.string.aggiungi_dismiss)
//                        .show();
//            }
//        });

        if (elementi.size() > 0)
            findViewById(R.id.noElementsAdded).setVisibility(View.GONE);

        mWelcomeScreen = new WelcomeHelper(this, IntroCreaListaNew.class);
        mWelcomeScreen.show(savedInstanceState);
        findViewById(R.id.textTitleDescription).requestFocus();

        if (savedInstanceState != null) {
            Log.d(getClass().getName(), "onCreate: RESTORING");
            positionToRename = savedInstanceState.getInt("positionToRename", 0);
            if (InputTextDialogFragment.findVisible(CreaListaActivity.this, "RENAME") != null)
                InputTextDialogFragment.findVisible(CreaListaActivity.this, "RENAME").setmCallback(CreaListaActivity.this);
            if (InputTextDialogFragment.findVisible(CreaListaActivity.this, "ADD_POSITION") != null)
                InputTextDialogFragment.findVisible(CreaListaActivity.this, "ADD_POSITION").setmCallback(CreaListaActivity.this);
            if (SimpleDialogFragment.findVisible(CreaListaActivity.this, "SAVE_LIST") != null)
                SimpleDialogFragment.findVisible(CreaListaActivity.this, "SAVE_LIST").setmCallback(CreaListaActivity.this);
        }

    }

    @Override
    public void onPause() {
        mRecyclerViewDragDropManager.cancelDrag();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
        }

        if (mRecyclerViewSwipeManager != null) {
            mRecyclerViewSwipeManager.release();
            mRecyclerViewSwipeManager = null;
        }

        if (mRecyclerViewTouchActionGuardManager != null) {
            mRecyclerViewTouchActionGuardManager.release();
            mRecyclerViewTouchActionGuardManager = null;
        }

        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        mAdapter = null;
        mLayoutManager = null;

        if (listaCanti != null)
            listaCanti.close();

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.crea_lista_menu, menu);
        menu.findItem(R.id.action_save_list).setIcon(
                new IconicsDrawable(CreaListaActivity.this, CommunityMaterial.Icon.cmd_content_save)
                        .sizeDp(24)
                        .paddingDp(2)
                        .color(Color.WHITE));
        menu.findItem(R.id.action_help).setIcon(
                new IconicsDrawable(CreaListaActivity.this, CommunityMaterial.Icon.cmd_help_circle)
                        .sizeDp(24)
                        .paddingDp(2)
                        .color(Color.WHITE));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
//                showHelp();
                mWelcomeScreen.forceShow();
                return true;
            case R.id.action_save_list:
                if (saveList()) {
                    setResult(Activity.RESULT_OK);
                    finish();
                    overridePendingTransition(0, R.anim.slide_out_bottom);
                }
                return true;
            case android.R.id.home:
                if (elementi.size() > 0) {
                    new SimpleDialogFragment.Builder(CreaListaActivity.this, CreaListaActivity.this, "SAVE_LIST")
                            .title(R.string.save_list_title)
                            .content(R.string.save_list_question)
                            .positiveButton(R.string.confirm)
                            .negativeButton(R.string.dismiss)
                            .neutralButton(R.string.cancel)
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
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (elementi.size() > 0) {
                new SimpleDialogFragment.Builder(CreaListaActivity.this, CreaListaActivity.this, "SAVE_LIST")
                        .title(R.string.save_list_title)
                        .content(R.string.save_list_question)
                        .positiveButton(R.string.confirm)
                        .negativeButton(R.string.dismiss)
                        .neutralButton(R.string.cancel)
                        .show();
                return true;
            }
            else {
                setResult(Activity.RESULT_CANCELED);
                finish();
                overridePendingTransition(0, R.anim.slide_out_bottom);
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    private boolean saveList()  {
        celebrazione = new ListaPersonalizzata();

        if (textfieldTitle.getText() != null
                && !textfieldTitle.getText()
                .toString().trim().equalsIgnoreCase("")) {
            titoloLista = textfieldTitle.getText().toString();
        }
        else {
            Toast toast = Toast.makeText(CreaListaActivity.this
                    , getString(R.string.no_title_edited), Toast.LENGTH_SHORT);
            toast.show();
        }

        celebrazione.setName(titoloLista);
        for (int i = 0; i < elementi.size(); i++) {
            if (celebrazione.addPosizione(elementi.get(i).getTitolo()) == -2) {
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
            for (int i = 0; i < elementi.size(); i++) {
//    			Log.i("SALVO CANTO", nomiCanti.get(i));
                celebrazione.addCanto(nomiCanti.get(i), i);
            }
        }

        SQLiteDatabase db = listaCanti.getReadableDatabase();

        ContentValues  values = new  ContentValues();
        values.put("titolo_lista" , titoloLista);
        values.put("lista" , ListaPersonalizzata.serializeObject(celebrazione));

        if (modifica)
            db.update("LISTE_PERS", values, "_id = " + idModifica, null);
        else
            db.insert("LISTE_PERS" , "" , values);

        db.close();
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        dataFragment = new RetainedFragment();
        getSupportFragmentManager().beginTransaction().add(dataFragment, "nomiElementi").commit();
        dataFragment.setDataDrag(elementi);

        if (modifica) {
            dataFragment2 = new RetainedFragment();
            getSupportFragmentManager().beginTransaction().add(dataFragment2, "nomiCanti").commit();
            dataFragment2.setData(nomiCanti);
        }

        dataFragment3 = new RetainedFragment();
        tempArgs = new Bundle();
        tempArgs.putCharSequence(TEMP_TITLE, textfieldTitle.getText());
        dataFragment3.setArguments(tempArgs);
        getSupportFragmentManager().beginTransaction().add(dataFragment3, TEMP_TITLE).commit();

        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("positionToRename", positionToRename);
        mWelcomeScreen.onSaveInstanceState(savedInstanceState);
    }

    public static class RetainedFragment extends Fragment {

        // data object we want to retain
        private ArrayList<String> data;
        private ArrayList<DraggableItem> dataDrag;

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

        public ArrayList<DraggableItem> getDataDrag() {
            return dataDrag;
        }

        public void setDataDrag(ArrayList<DraggableItem> dataDrag) {
            this.dataDrag = dataDrag;
        }
    }

    @Override
    public void onPositive(@NonNull String tag, @NonNull MaterialDialog dialog) {
        Log.d(getClass().getName(), "onPositive: " + tag);
        switch (tag) {
            case "RENAME":
                elementi.set(positionToRename, new DraggableItem(dialog.getInputEditText().getText().toString()
                        , elementi.get(positionToRename).getIdPosizione()));
                mAdapter.notifyDataSetChanged();
                break;
            case "ADD_POSITION":
                findViewById(R.id.noElementsAdded).setVisibility(View.GONE);
                elementi.add(new DraggableItem(dialog.getInputEditText().getText().toString(), Utility.random(1, 500)));
                if (modifica)
                    nomiCanti.add("");
                mAdapter.notifyItemInserted(elementi.size() - 1);
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
                if (saveList()) {
                    setResult(Activity.RESULT_OK);
                    finish();
                    overridePendingTransition(0, R.anim.slide_out_bottom);
                }
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
}