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
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
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
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import it.cammino.risuscito.adapters.CantoHistoryRecyclerAdapter;
import it.cammino.risuscito.objects.CantoHistory;
import it.cammino.risuscito.utils.ThemeUtils;

public class HistoryFragment extends Fragment {

    private DatabaseCanti listaCanti;
    private List<CantoHistory> titoli;
//    private String cantoDaCanc;
    private int posizDaCanc;
    private CantoHistory removedItem;
    private View rootView;
    private RecyclerView recyclerView;
    private CantoHistoryRecyclerAdapter cantoAdapter;
    private int prevOrientation;
    private FloatingActionButton fabClear;
    private ActionMode mMode;

    private String HISTORY_OPEN = "history_open";

    private LUtils mLUtils;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.layout_history, container, false);
//        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_activity_history);
//        ((TextView)((MainActivity) getActivity()).findViewById(R.id.main_toolbarTitle)).setText(R.string.title_activity_history);
//        ((MainActivity) getActivity()).getSupportActionBar()
//                .setElevation(dpToPx(getResources().getInteger(R.integer.toolbar_elevation)));
        ((MainActivity) getActivity()).setupToolbar(rootView.findViewById(R.id.risuscito_toolbar), R.string.title_activity_history);

        //crea un istanza dell'oggetto DatabaseCanti
        listaCanti = new DatabaseCanti(getActivity());

        mLUtils = LUtils.getInstance(getActivity());
        mMode = null;

//        Typeface face=Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
//        ((TextView) rootView.findViewById(R.id.no_history_text)).setTypeface(face);

        fabClear = (FloatingActionButton) rootView.findViewById(R.id.fab_clear_history);
        fabClear.setColorNormal(getThemeUtils().accentColor());
        fabClear.setColorPressed(getThemeUtils().accentColorDark());
        fabClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevOrientation = getActivity().getRequestedOrientation();
                Utility.blockOrientation(getActivity());
                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .title(R.string.dialog_reset_history_title)
                        .content(R.string.dialog_reset_history_desc)
                        .positiveText(R.string.confirm)
                        .negativeText(R.string.dismiss)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                SQLiteDatabase db = listaCanti.getReadableDatabase();
                                db.delete("CRONOLOGIA", null, null);
                                db.close();
                                updateHistoryList();
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
                .getBoolean(HISTORY_OPEN, false)) {
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(getActivity())
                    .edit();
            editor.putBoolean(HISTORY_OPEN, true);
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

        //recupera il numero di record trovati
        int total = lista.getCount();

        //nel caso sia presente almeno un canto visitato di recente, viene nascosto il testo di nessun canto presente
        rootView.findViewById(R.id.no_history).setVisibility(total > 0 ? View.INVISIBLE : View.VISIBLE);
        if (total == 0) {
            fabClear.hide();
            fabClear.setmIgnoreLayoutChanges(true);
        }
        else
            fabClear.setmIgnoreLayoutChanges(false);

        // crea un array e ci memorizza i titoli estratti
        titoli = new ArrayList<>();
        lista.moveToFirst();
        for (int i = 0; i < total; i++) {

            //FORMATTO LA DATA IN BASE ALLA LOCALIZZAZIONE
//            DateFormat df = DateFormat.getDateTimeInstance(
//                    DateFormat.SHORT
//                    , DateFormat.MEDIUM
//                    , getActivity().getResources().getConfiguration().locale);
//            String timestamp = "";
//
//            if (df instanceof SimpleDateFormat)
//            {
////                Log.i(getClass().toString(), "is Simple");
//                SimpleDateFormat sdf = (SimpleDateFormat) df;
//                // To show Locale specific short date expression with full year
//                String pattern = sdf.toPattern().replaceAll("y+","yyyy");
//                sdf.applyPattern(pattern);
//                timestamp = sdf.format(Timestamp.valueOf(lista.getString(5)));
//            }
//            else {
////                Log.i(getClass().toString(), "is NOT Simple");
//                timestamp = df.format(Timestamp.valueOf(lista.getString(5)));
//            }
//            Log.i(getClass().toString(), "timestamp: " + timestamp);

            titoli.add(new CantoHistory(Utility.intToString(lista.getInt(3), 3) + lista.getString(2) + lista.getString(1)
                    , lista.getInt(0)
                    , lista.getString(4)
//                            , getString(R.string.last_open_date) + " " + lista.getString(5)));
//                    , getString(R.string.last_open_date) + " " + timestamp));
                    , lista.getString(5)));
            lista.moveToNext();
        }
        // chiude il cursore
        lista.close();

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // recupera il titolo della voce cliccata
                String idCanto = ((TextView) v.findViewById(R.id.text_id_canto))
                        .getText().toString();
                String source = ((TextView) v.findViewById(R.id.text_source_canto))
                        .getText().toString();

                // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da visualizzare
                Bundle bundle = new Bundle();
                bundle.putString("pagina", source);
                bundle.putInt("idCanto", Integer.parseInt(idCanto));

                // lancia l'activity che visualizza il canto passando il parametro creato
                startSubActivity(bundle, v);
            }
        };

        View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                cantoDaCanc = ((TextView) v.findViewById(R.id.text_id_canto)).getText().toString();
                posizDaCanc = recyclerView.getChildAdapterPosition(v);
//                Snackbar.make(rootView.findViewById(R.id.main_content), R.string.history_remove, Snackbar.LENGTH_LONG)
//                        .setAction(R.string.snackbar_remove, new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                SQLiteDatabase db = listaCanti.getReadableDatabase();
//                                db.delete("CRONOLOGIA", "id_canto = " + cantoDaCanc, null);
//                                db.close();
//                                titoli.remove(posizDaCanc);
//                                cantoAdapter.notifyItemRemoved(posizDaCanc);
//                                //nel caso sia presente almeno un canto recente, viene nascosto il testo di nessun canto presente
//                                rootView.findViewById(R.id.no_history).setVisibility(titoli.size() > 0 ? View.INVISIBLE : View.VISIBLE);
//                                if (titoli.size() == 0) {
//                                    fabClear.hide();
//                                    fabClear.setmIgnoreLayoutChanges(true);
//                                }
//                            }
//                        })
//                        .setActionTextColor(getThemeUtils().accentColor())
//                        .show();
                if (mMode == null)
                    mMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ModeCallback());
                else {
                    mMode.finish();
                    mMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ModeCallback());
                }
                return true;

            }
        };

        recyclerView = (RecyclerView) rootView.findViewById(R.id.history_recycler);

        // Creating new adapter object
        cantoAdapter = new CantoHistoryRecyclerAdapter(getActivity(), titoli, clickListener, longClickListener);
        recyclerView.setAdapter(cantoAdapter);

        // Setting the layoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //decide se mostrare o nascondere il floatin button in base allo scrolling
        /*
            SERVE SOLO PRIMA DELLE API 21, PERCHE' NON C'E' IL TOOLBARLAYOUT
        */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    float y = recyclerView.getScrollY();
                    super.onScrolled(recyclerView, dx, dy);
                    if (y < dy) {
                        if (titoli.size() > 0)
                            fabClear.hide();
                    } else {
                        if (titoli.size() > 0)
                            fabClear.show();
                    }
                }

            });
        }

    }

//    private int dpToPx(int dp) {
//        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
//        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
//        return px;
//    }

    private ThemeUtils getThemeUtils() {
        return ((MainActivity)getActivity()).getThemeUtils();
    }

    private final class ModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Create the menu from the xml file
//            MenuInflater inflater = getActivity().getMenuInflater();
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
//                ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
            getActivity().getMenuInflater().inflate(R.menu.menu_delete, menu);
            Drawable drawable = DrawableCompat.wrap(menu.findItem(R.id.action_remove_item).getIcon());
            DrawableCompat.setTint(drawable, getResources().getColor(R.color.icon_ative_black));
            menu.findItem(R.id.action_remove_item).setIcon(drawable);
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
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch(item.getItemId()) {
                case R.id.action_remove_item:
                    SQLiteDatabase db = listaCanti.getReadableDatabase();
                    removedItem = titoli.remove(posizDaCanc);
                    db.delete("CRONOLOGIA", "id_canto = " + removedItem.getIdCanto(), null);
                    db.close();
                    cantoAdapter.notifyItemRemoved(posizDaCanc);
                    rootView.findViewById(R.id.no_history).setVisibility(titoli.size() > 0 ? View.INVISIBLE : View.VISIBLE);
                    if (titoli.size() == 0) {
                        fabClear.hide();
                        fabClear.setmIgnoreLayoutChanges(true);
                    }
            }
            mode.finish();
            Snackbar.make(rootView.findViewById(R.id.main_content), R.string.history_removed, Snackbar.LENGTH_LONG)
                    .setAction(R.string.cancel, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            SQLiteDatabase db = listaCanti.getReadableDatabase();
                            ContentValues values = new ContentValues();
                            values.put("id_canto", removedItem.getIdCanto());
                            values.put("ultima_visita", removedItem.getTimestamp());
                            db.insert("CRONOLOGIA", null, values);
                            db.close();
                            titoli.add(posizDaCanc, removedItem);
                            cantoAdapter.notifyItemInserted(posizDaCanc);
                            //nel caso sia presente almeno un canto recente, viene nascosto il testo di nessun canto presente
                            rootView.findViewById(R.id.no_history).setVisibility(View.INVISIBLE);
                            fabClear.show();
                            fabClear.setmIgnoreLayoutChanges(false);
                        }
                    })
                    .setActionTextColor(getThemeUtils().accentColor())
                    .show();
            return true;
        }
    };

}
