package it.cammino.risuscito;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import it.cammino.risuscito.adapters.CantoRecyclerAdapter;
import it.cammino.risuscito.objects.CantoRecycled;
import it.cammino.risuscito.utils.ThemeUtils;

public class FavouritesActivity extends Fragment {

    private DatabaseCanti listaCanti;
    private List<CantoRecycled> titoli;
    //    private String cantoDaCanc;
//    private int idDaCanc;
    private int posizDaCanc;
    //    private CantoRecycled removedItem;
    private List<CantoRecycled> removedItems;
    private View rootView;
    private RecyclerView recyclerView;
    private CantoRecyclerAdapter cantoAdapter;
    private int prevOrientation;
    private FloatingActionButton fabClear;
    private ActionMode mMode;
    private boolean actionModeOk;

    private String PREFERITI_OPEN = "preferiti_open";

    private LUtils mLUtils;

    private long mLastClickTime = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.activity_favourites, container, false);
//        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_activity_favourites);
//        ((TextView)((MainActivity) getActivity()).findViewById(R.id.main_toolbarTitle)).setText(R.string.title_activity_favourites);
//        ((MainActivity) getActivity()).getSupportActionBar()
//                .setElevation(dpToPx(getResources().getInteger(R.integer.toolbar_elevation)));
        ((MainActivity) getActivity()).setupToolbar(rootView.findViewById(R.id.risuscito_toolbar), R.string.title_activity_favourites);

        //crea un istanza dell'oggetto DatabaseCanti
        listaCanti = new DatabaseCanti(getActivity());

        mLUtils = LUtils.getInstance(getActivity());
        mMode = null;

        fabClear = (FloatingActionButton) rootView.findViewById(R.id.fab_clear_favorites);
//        fabClear.setColorNormal(getThemeUtils().accentColor());
//        fabClear.setColorPressed(getThemeUtils().accentColorDark());
        fabClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevOrientation = getActivity().getRequestedOrientation();
                Utility.blockOrientation(getActivity());
                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .title(R.string.dialog_reset_favorites_title)
                        .content(R.string.dialog_reset_favorites_desc)
                        .positiveText(R.string.confirm)
                        .negativeText(R.string.dismiss)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                SQLiteDatabase db = listaCanti.getReadableDatabase();
                                ContentValues  values = new  ContentValues();
                                values.put("favourite" , 0);
                                db.update("ELENCO", values,  null, null);
                                db.close();
                                updateFavouritesList();
//                                if (titoli.size() == 0)
//                                    fabClear.hide();
                                getActivity().setRequestedOrientation(prevOrientation);
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                getActivity().setRequestedOrientation(prevOrientation);
                            }
                        })
                        .show();
                dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK
                                && event.getAction() == KeyEvent.ACTION_UP) {
                            arg0.dismiss();
                            getActivity().setRequestedOrientation(prevOrientation);
                            return true;
                        }
                        return false;
                    }
                });
                dialog.setCancelable(false);
            }
        });

        if(!PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getBoolean(PREFERITI_OPEN, false)) {
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(getActivity())
                    .edit();
            editor.putBoolean(PREFERITI_OPEN, true);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                editor.commit();
            } else {
                editor.apply();
            }
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

        //recupera il numero di record trovati
//        int total = lista.getCount();

        // crea un array e ci memorizza i titoli estratti
        titoli = new ArrayList<>();
        lista.moveToFirst();
        for (int i = 0; i < lista.getCount(); i++) {
//            titoli.add(new CantoItem(Utility.intToString(lista.getInt(2), 3) + lista.getString(1) + lista.getString(0)));
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
                // recupera il titolo della voce cliccata
//                String cantoCliccato = ((TextView) v.findViewById(R.id.text_title))
//                        .getText().toString();
//                cantoCliccato = Utility.duplicaApostrofi(cantoCliccato);
//
//                // crea un manipolatore per il DB in modalità READ
//                SQLiteDatabase db = listaCanti.getReadableDatabase();
//
//                // esegue la query per il recupero del nome del file della pagina da visualizzare
//                String query = "SELECT source, _id" +
//                        "  FROM ELENCO" +
//                        "  WHERE titolo =  '" + cantoCliccato + "'";
//                Cursor cursor = db.rawQuery(query, null);
//
//                // recupera il nome del file
//                cursor.moveToFirst();
//                String pagina = cursor.getString(0);
//                int idCanto = cursor.getInt(1);
//
//                // chiude il cursore
//                cursor.close();
//                db.close();

                if (mMode == null) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
                        return;
                    mLastClickTime = SystemClock.elapsedRealtime();
                    // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da visualizzare
                    Bundle bundle = new Bundle();
//                bundle.putString("pagina", pagina);
                    bundle.putString("pagina", String.valueOf(((TextView) v.findViewById(R.id.text_source_canto)).getText()));
//                bundle.putInt("idCanto", idCanto);
                    bundle.putInt("idCanto", Integer.parseInt(
                            String.valueOf(((TextView) v.findViewById(R.id.text_id_canto)).getText())));

                    // lancia l'activity che visualizza il canto passando il parametro creato
                    startSubActivity(bundle, v);
                }
                else {
                    int tempPos = recyclerView.getChildAdapterPosition(v);
                    titoli.get(tempPos).setmSelected(!titoli.get(tempPos).ismSelected());
                    cantoAdapter.notifyItemChanged(tempPos);
                }
            }
        };

        View.OnLongClickListener longClickListener  = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                cantoDaCanc = ((TextView) v.findViewById(R.id.text_title)).getText().toString();
//                cantoDaCanc = Utility.duplicaApostrofi(cantoDaCanc);
//                posizDaCanc = recyclerView.getChildAdapterPosition(v);
//                Snackbar.make(rootView.findViewById(R.id.main_content), R.string.favorite_remove, Snackbar.LENGTH_LONG)
//                        .setAction(R.string.snackbar_remove, new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                SQLiteDatabase db = listaCanti.getReadableDatabase();
//                                ContentValues values = new ContentValues();
//                                values.put("favourite", 0);
//                                db.update("ELENCO", values, "titolo =  '" + cantoDaCanc + "'", null);
////                                String sql = "UPDATE ELENCO" +
////                                        "  SET favourite = 0" +
////                                        "  WHERE titolo =  '" + cantoDaCanc + "'";
////                                db.execSQL(sql);
//                                db.close();
//                                titoli.remove(posizDaCanc);
//                                cantoAdapter.notifyItemRemoved(posizDaCanc);
//                                rootView.findViewById(R.id.no_favourites).setVisibility(titoli.size() > 0 ? View.INVISIBLE : View.VISIBLE);
//                                if (titoli.size() == 0) {
//                                    fabClear.hide();
//                                    fabClear.setmIgnoreLayoutChanges(true);
//                                }
//                            }
//                        })
//                        .setActionTextColor(getThemeUtils().accentColor())
//                        .show();
//                idDaCanc = Integer.valueOf(String.valueOf(((TextView) v.findViewById(R.id.text_id_canto)).getText()));
                posizDaCanc = recyclerView.getChildAdapterPosition(v);
                if (mMode == null)
                    mMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ModeCallback());
                else {
                    mMode.finish();
                    mMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ModeCallback());
                }
                return true;
            }
        };

        recyclerView = (RecyclerView) rootView.findViewById(R.id.favouritesList);

        // Creating new adapter object
        cantoAdapter = new CantoRecyclerAdapter(getActivity(), titoli, clickListener, longClickListener);
        recyclerView.setAdapter(cantoAdapter);

        // Setting the layoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

//nel caso sia presente almeno un preferito, viene nascosto il testo di nessun canto presente
        rootView.findViewById(R.id.no_favourites).setVisibility(titoli.size() > 0 ? View.INVISIBLE : View.VISIBLE);
        if (titoli.size() == 0) {
            fabClear.hide();
//            fabClear.setmIgnoreLayoutChanges(true);
        }
        else {
            fabClear.show();
//            fabClear.setmIgnoreLayoutChanges(false);
        }

        //decide se mostrare o nascondere il floatin button in base allo scrolling
        /*
            SERVE SOLO PRIMA DELLE API 21, PERCHE' NON C'E' IL TOOLBARLAYOUT
        */
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
//            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//                @Override
//                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                    float y = recyclerView.getScrollY();
//                    super.onScrolled(recyclerView, dx, dy);
//                    if (y < dy) {
//                        if (titoli.size() > 0)
//                            fabClear.hide();
//                    } else {
//                        if (titoli.size() > 0)
//                            fabClear.show();
//                    }
//                }
//
//            });
//        }

    }

    private ThemeUtils getThemeUtils() {
        return ((MainActivity)getActivity()).getThemeUtils();
    }

    private final class ModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Create the menu from the xml file
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
//                ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
            getActivity().getMenuInflater().inflate(R.menu.menu_delete, menu);
            titoli.get(posizDaCanc).setmSelected(true);
            cantoAdapter.notifyItemChanged(posizDaCanc);
            removedItems = new ArrayList<>();
            Drawable drawable = DrawableCompat.wrap(menu.findItem(R.id.action_remove_item).getIcon());
            DrawableCompat.setTint(drawable, getResources().getColor(R.color.icon_ative_black));
            menu.findItem(R.id.action_remove_item).setIcon(drawable);
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
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
//                ((AppCompatActivity)getActivity()).getSupportActionBar().show();
            if (mode == mMode)
                mMode = null;
            Log.i(getClass().getName(), "actionModeOk: " + actionModeOk);
            if (!actionModeOk) {
                for (CantoRecycled canto : titoli) {
                    canto.setmSelected(false);
                    cantoAdapter.notifyDataSetChanged();
                }
            }
//            for (CantoRecycled canto: removedItems)
//                canto.setmSelected(false);
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch(item.getItemId()) {
                case R.id.action_remove_item:
                    Log.i(getClass().getName(), "CLICKED");
                    SQLiteDatabase db = listaCanti.getReadableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("favourite", 0);
//                    db.update("ELENCO", values, "titolo =  '" + cantoDaCanc + "'", null);
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
//                    removedItem = titoli.remove(posizDaCanc);
//                    cantoAdapter.notifyItemRemoved(posizDaCanc);
                    rootView.findViewById(R.id.no_favourites).setVisibility(titoli.size() > 0 ? View.INVISIBLE : View.VISIBLE);
                    if (titoli.size() == 0) {
                        fabClear.hide();
//                        fabClear.setmIgnoreLayoutChanges(true);
                    }
                    actionModeOk = true;
                    mode.finish();
                    if (removedItems.size() > 0) {
                        String message = removedItems.size() > 1 ?
                                getString(R.string.favorites_removed).replaceAll("%", String.valueOf(removedItems.size()))
                                : getString(R.string.favorite_removed);
                        Snackbar.make(rootView.findViewById(R.id.main_content), message, Snackbar.LENGTH_LONG)
                                .setAction(R.string.cancel, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        SQLiteDatabase db = listaCanti.getReadableDatabase();
                                        ContentValues values = new ContentValues();
                                        values.put("favourite", 1);
//                                    db.update("ELENCO", values, "titolo =  '" + cantoDaCanc + "'", null);
                                        for (CantoRecycled cantoRemoved: removedItems) {
                                            db.update("ELENCO", values, "_id =  " + cantoRemoved.getIdCanto(), null);
                                        }
                                        db.close();
                                        updateFavouritesList();
//                                    titoli.add(posizDaCanc, removedItem);
//                                    cantoAdapter.notifyItemInserted(posizDaCanc);
//                                        rootView.findViewById(R.id.no_favourites).setVisibility(View.INVISIBLE);
//                                        fabClear.show();
//                                        fabClear.setmIgnoreLayoutChanges(false);
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

//    public boolean onBackPressed() {
//        Log.i(getClass().getName(), "ENTRO1");
//        if (mMode != null) {
//            for (CantoRecycled canto: titoli) {
//                Log.i(getClass().getName(), "ENTRO2");
//                canto.setmSelected(false);
//                cantoAdapter.notifyDataSetChanged();
//            }
//            mMode.finish();
//            return true;
//        }
//        return false;
//    }

}
