package it.cammino.risuscito;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.alexkolpa.fabtoolbar.FabToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.cammino.risuscito.adapters.PosizioneRecyclerAdapter;
import it.cammino.risuscito.objects.PosizioneItem;
import it.cammino.risuscito.objects.PosizioneTitleItem;
import it.cammino.risuscito.ui.BottomSheetHelper;
import it.cammino.risuscito.utils.ThemeUtils;

public class ListaPersonalizzataFragment extends Fragment {

    private int posizioneDaCanc;
    private View rootView;
    private DatabaseCanti listaCanti;
    String cantoDaCanc;
    private SQLiteDatabase db;
    private int idLista;
    private ListaPersonalizzata listaPersonalizzata;
    public ActionMode mMode;
    private boolean mSwhitchMode;
    private List<Pair<PosizioneTitleItem, List<PosizioneItem>>> posizioniList;
    private int longclickedPos, longClickedChild;
    private PosizioneRecyclerAdapter cantoAdapter;
    private boolean actionModeOk;

    private static final int TAG_INSERT_PERS = 555;

    private LUtils mLUtils;

    private long mLastClickTime = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(
                R.layout.activity_lista_personalizzata, container, false);

        //crea un istanza dell'oggetto DatabaseCanti
        listaCanti = new DatabaseCanti(getActivity());

        mLUtils = LUtils.getInstance(getActivity());
        mMode = null;
        mSwhitchMode = false;

        idLista = getArguments().getInt("idLista");

        db = listaCanti.getReadableDatabase();
        String query = "SELECT lista" +
                "  FROM LISTE_PERS" +
                "  WHERE _id =  " + idLista;
        Cursor cursor = db.rawQuery(query, null);
        // recupera l'oggetto lista personalizzata
        cursor.moveToFirst();
        listaPersonalizzata = (ListaPersonalizzata) ListaPersonalizzata.
                deserializeObject(cursor.getBlob(0));

        updateLista();

        OnClickListener click = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
                    return;
                mLastClickTime = SystemClock.elapsedRealtime();
                View parent = (View) v.getParent().getParent();
                if (parent.findViewById(R.id.addCantoGenerico).getVisibility() == View.VISIBLE) {
                    if (mSwhitchMode)
                        scambioConVuoto(parent, Integer.valueOf(((TextView) parent.findViewById(R.id.text_id_posizione)).getText().toString()));
                    else {
                        if (mMode == null) {
                            Bundle bundle = new Bundle();
                            bundle.putInt("fromAdd", 0);
                            bundle.putInt("idLista", idLista);
                            bundle.putInt("position", Integer.valueOf(((TextView) parent.findViewById(R.id.text_id_posizione)).getText().toString()));
                            Intent intent = new Intent(getActivity(), GeneralInsertSearch.class);
                            intent.putExtras(bundle);
                            getParentFragment().startActivityForResult(intent, TAG_INSERT_PERS + idLista);
                            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.hold_on);
                        }
                    }
                }
                else {
                    if (!mSwhitchMode)
                        if (mMode != null) {
                            posizioneDaCanc = Integer.valueOf(((TextView) parent.findViewById(R.id.text_id_posizione)).getText().toString());
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

        OnLongClickListener longClick = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                View parent = (View) v.getParent().getParent();
                posizioneDaCanc = Integer.valueOf(((TextView) parent.findViewById(R.id.text_id_posizione)).getText().toString());
                snackBarRimuoviCanto(v);
                return true;
            }
        };

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_list);

        // Creating new adapter object
        cantoAdapter = new PosizioneRecyclerAdapter(getActivity(), posizioniList, click, longClick);
        recyclerView.setAdapter(cantoAdapter);

        // Setting the layoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        rootView.findViewById(R.id.button_pulisci).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
//				Log.i(getClass().toString(), "idLista: " + idLista);
                db = listaCanti.getReadableDatabase();
                ContentValues  values = new  ContentValues( );
                for (int i = 0; i < listaPersonalizzata.getNumPosizioni(); i++)
                    listaPersonalizzata.removeCanto(i);
                values.put("lista" , ListaPersonalizzata.serializeObject(listaPersonalizzata));
                db.update("LISTE_PERS", values, "_id = " + idLista, null);
                db.close();
                updateLista();
                cantoAdapter.notifyDataSetChanged();
            }
        });

        rootView.findViewById(R.id.button_condividi).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
//                Log.i(getClass().toString(), "idLista: " + idLista);
                BottomSheetHelper.shareAction(getActivity(), getDefaultIntent())
                        .title(R.string.share_by)
                        .show();
            }
        });

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            ((CustomLists) getParentFragment()).fabDelete.setVisibility(View.VISIBLE);
            ((CustomLists) getParentFragment()).fabEdit.setVisibility(View.VISIBLE);
            FabToolbar fab1 = ((CustomLists) getParentFragment()).getFab();
            if (!fab1.isShowing())
                fab1.scrollUp();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.i(getClass().getName(), "requestCode: " + requestCode);
        if (requestCode == TAG_INSERT_PERS + idLista && resultCode == Activity.RESULT_OK) {
//            Log.i("LISTA PERS", "ON RESUME");
            idLista = getArguments().getInt("idLista");
//		Log.i("fragmentIndex", fragmentIndex+"");
//		Log.i("idLista", idLista+"");

            db = listaCanti.getReadableDatabase();

            String query = "SELECT lista" +
                    "  FROM LISTE_PERS" +
                    "  WHERE _id =  " + idLista;
            Cursor cursor = db.rawQuery(query, null);

            // recupera l'oggetto lista personalizzata
            cursor.moveToFirst();

            listaPersonalizzata = (ListaPersonalizzata) ListaPersonalizzata.
                    deserializeObject(cursor.getBlob(0));

            updateLista();
            cantoAdapter.notifyDataSetChanged();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        if (listaCanti != null)
            listaCanti.close();
        if (mMode != null)
            mMode.finish();
        super.onDestroy();
    }

    private Intent getDefaultIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, getTitlesList());
        intent.setType("text/plain");
        return intent;
    }

    private void openPagina(View v) {
        // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da visualizzare
        Bundle bundle = new Bundle();
        bundle.putString("pagina", ((TextView) v.findViewById(R.id.text_source_canto)).getText().toString());
        bundle.putInt("idCanto", Integer.valueOf(((TextView) v.findViewById(R.id.text_id_canto)).getText().toString()));

        Intent intent = new Intent(getActivity(), PaginaRenderActivity.class);
        intent.putExtras(bundle);
        mLUtils.startActivityWithTransition(intent, v, Utility.TRANS_PAGINA_RENDER);
    }

    private void updateLista() {

//		Log.i("POSITION", fragmentIndex+" ");
//		Log.i("IDLISTA", idLista+" ");
//		Log.i("TITOLO", listaPersonalizzata.getName());
        if (posizioniList == null)
            posizioniList = new ArrayList<>();
        else
            posizioniList.clear();

        for (int cantoIndex = 0; cantoIndex < listaPersonalizzata.getNumPosizioni(); cantoIndex++) {
            List<PosizioneItem> list = new ArrayList<>();
            if (listaPersonalizzata.getCantoPosizione(cantoIndex).length() > 0) {
                db = listaCanti.getReadableDatabase();

                String query = "SELECT _id, titolo, pagina, color, source" +
                        "  FROM ELENCO" +
                        "  WHERE _id =  " + listaPersonalizzata.getCantoPosizione(cantoIndex);
                Cursor cursor = db.rawQuery(query, null);
                cursor.moveToFirst();

                list.add(new PosizioneItem(
                        cursor.getInt(2)
                        , cursor.getString(1)
                        , cursor.getString(3)
                        , cursor.getInt(0)
                        , cursor.getString(4)
                        , ""));

                cursor.close();
                db.close();

            }

            Pair<PosizioneTitleItem, List<PosizioneItem>> result = new Pair(new PosizioneTitleItem(listaPersonalizzata.getNomePosizione(cantoIndex)
                    , idLista
                    , cantoIndex
                    , cantoIndex
                    , false), list);

            posizioniList.add(result);
        }

    }

    private String getTitlesList() {

        Locale l = getActivity().getResources().getConfiguration().locale;
        String result = "";

        //titolo
        result +=  "-- "  + listaPersonalizzata.getName().toUpperCase(l) + " --\n";

        //tutti i canti
        for (int i = 0; i < listaPersonalizzata.getNumPosizioni(); i++) {
            result += listaPersonalizzata.getNomePosizione(i).toUpperCase(l) + "\n";
            if (!listaPersonalizzata.getCantoPosizione(i).equalsIgnoreCase("")) {
                for (PosizioneItem tempItem: posizioniList.get(i).second) {
                    result += tempItem.getTitolo() + " - " + getString(R.string.page_contracted) + tempItem.getPagina();
                    result += "\n";
                }
            }
            else
                result += ">> " + getString(R.string.to_be_chosen) + " <<";
            if (i < listaPersonalizzata.getNumPosizioni() - 1)
                result += "\n";
        }

        return result;

    }

    public void snackBarRimuoviCanto(View view) {
        if (mMode != null)
            mMode.finish();
        View parent = (View) view.getParent().getParent();
        longclickedPos = Integer.valueOf(((TextView)parent.findViewById(R.id.tag)).getText().toString());
        longClickedChild = Integer.valueOf(((TextView)view.findViewById(R.id.item_tag)).getText().toString());
        mMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ModeCallback());
    }

    private ThemeUtils getThemeUtils() {
        return ((MainActivity)getActivity()).getThemeUtils();
    }

    private final class ModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Create the menu from the xml file
            posizioniList.get(longclickedPos).second.get(longClickedChild).setmSelected(true);
            cantoAdapter.notifyItemChanged(longclickedPos);
            getActivity().getMenuInflater().inflate(R.menu.menu_actionmode_lists, menu);
            Drawable drawable = DrawableCompat.wrap(menu.findItem(R.id.action_remove_item).getIcon());
            DrawableCompat.setTint(drawable, ContextCompat.getColor(getActivity(), R.color.icon_ative_black));
            menu.findItem(R.id.action_remove_item).setIcon(drawable);
            drawable = DrawableCompat.wrap(menu.findItem(R.id.action_switch_item).getIcon());
            DrawableCompat.setTint(drawable, ContextCompat.getColor(getActivity(), R.color.icon_ative_black));
            menu.findItem(R.id.action_switch_item).setIcon(drawable);
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
            mSwhitchMode = false;
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = getActivity().getTheme();
            theme.resolveAttribute(R.attr.customSelector, typedValue, true);
            if (!actionModeOk) {
                posizioniList.get(longclickedPos).second.get(longClickedChild).setmSelected(false);
                cantoAdapter.notifyItemChanged(longclickedPos);
            }
            if (mode == mMode)
                mMode = null;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch(item.getItemId()) {
                case R.id.action_remove_item:
                    db = listaCanti.getReadableDatabase();
                    ContentValues  values = new  ContentValues( );
                    cantoDaCanc = listaPersonalizzata.getCantoPosizione(posizioneDaCanc);
                    listaPersonalizzata.removeCanto(posizioneDaCanc);
                    values.put("lista", ListaPersonalizzata.serializeObject(listaPersonalizzata));
                    db.update("LISTE_PERS", values, "_id = " + idLista, null);
                    db.close();
                    updateLista();
                    cantoAdapter.notifyItemChanged(longclickedPos);
                    actionModeOk = true;
                    mode.finish();
                    Snackbar.make(getActivity().findViewById(R.id.main_content), R.string.song_removed, Snackbar.LENGTH_LONG)
                            .setAction(R.string.cancel, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    db = listaCanti.getReadableDatabase();
                                    ContentValues  values = new  ContentValues( );
                                    listaPersonalizzata.addCanto(cantoDaCanc, posizioneDaCanc);
                                    values.put("lista", ListaPersonalizzata.serializeObject(listaPersonalizzata));
                                    db.update("LISTE_PERS", values, "_id = " + idLista, null);
                                    db.close();
                                    updateLista();
                                    cantoAdapter.notifyItemChanged(longclickedPos);
                                }
                            })
                            .setActionTextColor(getThemeUtils().accentColor())
                            .show();
                    mSwhitchMode = false;
                    break;
                case R.id.action_switch_item:
                    mSwhitchMode = true;
                    db = listaCanti.getReadableDatabase();
                    cantoDaCanc = listaPersonalizzata.getCantoPosizione(posizioneDaCanc);
                    mode.setTitle(R.string.switch_started);
                    Toast.makeText(getActivity()
                            , getResources().getString(R.string.switch_tooltip)
                            , Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        }
    }

    private void scambioCanto(View v, int posizioneNew) {
//        Log.i(getClass().toString(), "positioneNew: " + posizioneNew);
//        Log.i(getClass().toString(), "posizioneDaCanc: " + posizioneDaCanc);
        if (posizioneNew != posizioneDaCanc) {

            String cantoTmp = listaPersonalizzata.getCantoPosizione(posizioneNew);
            listaPersonalizzata.addCanto(listaPersonalizzata.getCantoPosizione(posizioneDaCanc), posizioneNew);
            listaPersonalizzata.addCanto(cantoTmp, posizioneDaCanc);

            db = listaCanti.getReadableDatabase();
            ContentValues  values = new  ContentValues( );
            values.put("lista", ListaPersonalizzata.serializeObject(listaPersonalizzata));
            db.update("LISTE_PERS", values, "_id = " + idLista, null);
            db.close();

            updateLista();
            View parent = (View) v.getParent().getParent();
            cantoAdapter.notifyItemChanged(longclickedPos);
            cantoAdapter.notifyItemChanged(Integer.valueOf(((TextView)parent.findViewById(R.id.tag)).getText().toString()));
            actionModeOk = true;
            mMode.finish();
            Snackbar.make(getActivity().findViewById(R.id.main_content), R.string.switch_done, Snackbar.LENGTH_SHORT)
                    .show();

        }
        else
            Snackbar.make(rootView, R.string.switch_impossible, Snackbar.LENGTH_SHORT)
                    .show();
    }

    private void scambioConVuoto(View parent, int posizioneNew) {
//        Log.i(getClass().toString(), "positioneNew: " + posizioneNew);
//        Log.i(getClass().toString(), "posizioneDaCanc: " + posizioneDaCanc);
        listaPersonalizzata.addCanto(listaPersonalizzata.getCantoPosizione(posizioneDaCanc), posizioneNew);
        listaPersonalizzata.removeCanto(posizioneDaCanc);

        db = listaCanti.getReadableDatabase();
        ContentValues  values = new  ContentValues( );
        values.put("lista", ListaPersonalizzata.serializeObject(listaPersonalizzata));
        db.update("LISTE_PERS", values, "_id = " + idLista, null);
        db.close();

        updateLista();
        cantoAdapter.notifyItemChanged(longclickedPos);
        cantoAdapter.notifyItemChanged(Integer.valueOf(((TextView) parent.findViewById(R.id.tag)).getText().toString()));
        actionModeOk = true;
        mMode.finish();
        Snackbar.make(getActivity().findViewById(R.id.main_content), R.string.switch_done, Snackbar.LENGTH_SHORT)
                .show();
    }

}