package it.cammino.risuscito;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rey.material.widget.Button;

import java.util.ArrayList;
import java.util.List;

import it.cammino.risuscito.adapters.CantoRecyclerAdapter;
import it.cammino.risuscito.adapters.CantoSelezionabileAdapter;
import it.cammino.risuscito.objects.Canto;
import it.cammino.risuscito.utils.ThemeUtils;

public class ConsegnatiFragment extends Fragment {

    private DatabaseCanti listaCanti;
//    private List<CantoItem> titoli;
//    private List<Canto> titoliChoose;
    private View rootView;
//    private RecyclerView cantiRecycler;
    private CantoRecyclerAdapter cantoAdapter;
//    private RecyclerView chooseRecycler;
    private CantoSelezionabileAdapter selectableAdapter;

    private boolean editMode = false;

    private LUtils mLUtils;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.layout_consegnati, container, false);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_activity_consegnati);
        ((MainActivity) getActivity()).getSupportActionBar()
                .setElevation(dpToPx(getResources().getInteger(R.integer.toolbar_elevation)));

        //crea un istanza dell'oggetto DatabaseCanti
        listaCanti = new DatabaseCanti(getActivity());

        mLUtils = LUtils.getInstance(getActivity());

        Typeface face=Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
        ((TextView) rootView.findViewById(R.id.consegnati_text)).setTypeface(face);

        if (editMode) {
            rootView.findViewById(R.id.choose_view).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.consegnati_view).setVisibility(View.GONE);
//            updateChooseList();
        }
        else {
            rootView.findViewById(R.id.choose_view).setVisibility(View.GONE);
            rootView.findViewById(R.id.consegnati_view).setVisibility(View.VISIBLE);
            updateConsegnatiList();
        }

        ((Button)rootView.findViewById(R.id.cancel_change)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editMode = false;
                rootView.findViewById(R.id.choose_view).setVisibility(View.GONE);
                rootView.findViewById(R.id.consegnati_view).setVisibility(View.VISIBLE);
                updateConsegnatiList();
            }
        });
        ((Button)rootView.findViewById(R.id.confirm_changes)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editMode = false;
                rootView.findViewById(R.id.choose_view).setVisibility(View.GONE);
                rootView.findViewById(R.id.consegnati_view).setVisibility(View.VISIBLE);

                //aggiorno la lista dei consegnati
                SQLiteDatabase db = listaCanti.getReadableDatabase();
                db.delete("CANTI_CONSEGNATI", "", null);

                List<Canto> choosedList = selectableAdapter.getCantiChoose();
                for (int i = 0; i < choosedList.size(); i++) {
                    Canto singoloCanto = choosedList.get(i);
                    if (singoloCanto.isSelected()) {
                        String sql = "INSERT INTO CANTI_CONSEGNATI" +
                                "       (_id, id_canto)" +
                                "   SELECT COALESCE(MAX(_id) + 1,1), " + singoloCanto.getIdCanto() +
                                "             FROM CANTI_CONSEGNATI";

                        try {
                            db.execSQL(sql);
                        } catch (SQLException e) {
                            Log.e(getClass().toString(), "ERRORE INSERT:");
                            e.printStackTrace();
                        }
                    }
                }
                db.close();

                updateConsegnatiList();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (listaCanti != null)
            listaCanti.close();
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
        getActivity().getMenuInflater().inflate(R.menu.consegnati_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_choose:
                editMode = true;
                rootView.findViewById(R.id.choose_view).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.consegnati_view).setVisibility(View.GONE);
                updateChooseList(null);
                return true;
        }
        return false;
    }

    private void startSubActivity(Bundle bundle, View view) {
        Intent intent = new Intent(getActivity(), PaginaRenderActivity.class);
        intent.putExtras(bundle);
        mLUtils.startActivityWithTransition(intent, view, Utility.TRANS_PAGINA_RENDER);
    }

    private void updateConsegnatiList() {

        // crea un manipolatore per il Database in modalità READ
        SQLiteDatabase db = listaCanti.getReadableDatabase();

        // lancia la ricerca dei preferiti
        String query = "SELECT A.titolo, A.color, A.pagina" +
                "		FROM ELENCO A, CANTI_CONSEGNATI B" +
                "		WHERE A._id = B.id_canto" +
                "		ORDER BY TITOLO ASC";
        Cursor lista = db.rawQuery(query, null);

        //recupera il numero di record trovati
        int total = lista.getCount();

        //nel caso sia presente almeno un preferito, viene nascosto il testo di nessun canto presente
        rootView.findViewById(R.id.no_consegnati).setVisibility(total > 0 ? View.GONE: View.VISIBLE);

        // crea un array e ci memorizza i titoli estratti
        List<CantoItem> titoli = new ArrayList<CantoItem>();
        lista.moveToFirst();
        for (int i = 0; i < total; i++) {
            titoli.add(new CantoItem(Utility.intToString(lista.getInt(2), 3) + lista.getString(1) + lista.getString(0)));
            lista.moveToNext();
        }

        // chiude il cursore
        lista.close();

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // recupera il titolo della voce cliccata
                String cantoCliccato = ((TextView) v.findViewById(R.id.text_title))
                        .getText().toString();
                cantoCliccato = Utility.duplicaApostrofi(cantoCliccato);

                // crea un manipolatore per il DB in modalità READ
                SQLiteDatabase db = listaCanti.getReadableDatabase();

                // esegue la query per il recupero del nome del file della pagina da visualizzare
                String query = "SELECT source, _id" +
                        "  FROM ELENCO" +
                        "  WHERE titolo =  '" + cantoCliccato + "'";
                Cursor cursor = db.rawQuery(query, null);

                // recupera il nome del file
                cursor.moveToFirst();
                String pagina = cursor.getString(0);
                int idCanto = cursor.getInt(1);

                // chiude il cursore
                cursor.close();
                db.close();

                // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da visualizzare
                Bundle bundle = new Bundle();
                bundle.putString("pagina", pagina);
                bundle.putInt("idCanto", idCanto);

                // lancia l'activity che visualizza il canto passando il parametro creato
                startSubActivity(bundle, v);
            }
        };

//        View.OnLongClickListener longClickListener  = new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                cantoDaCanc = ((TextView) v.findViewById(R.id.text_title)).getText().toString();
//                cantoDaCanc = Utility.duplicaApostrofi(cantoDaCanc);
//                posizDaCanc = recyclerView.getChildPosition(v);
//                SnackbarManager.show(
//                        Snackbar.with(getActivity())
//                                .text(getString(R.string.favorite_remove))
//                                .actionLabel(getString(R.string.snackbar_remove))
//                                .actionListener(new ActionClickListener() {
//                                    @Override
//                                    public void onActionClicked(Snackbar snackbar) {
//                                        SQLiteDatabase db = listaCanti.getReadableDatabase();
//                                        String sql = "UPDATE ELENCO" +
//                                                "  SET favourite = 0" +
//                                                "  WHERE titolo =  '" + cantoDaCanc + "'";
//                                        db.execSQL(sql);
//                                        db.close();
//                                        // updateFavouritesList();
//                                        titoli.remove(posizDaCanc);
//                                        cantoAdapter.notifyItemRemoved(posizDaCanc);
//                                        //nel caso sia presente almeno un preferito, viene nascosto il testo di nessun canto presente
//                                        View noResults = rootView.findViewById(R.id.no_favourites);
//                                        TextView hintRemove = (TextView) rootView.findViewById(R.id.hint_remove);
//                                        if (titoli.size() > 0) {
//                                            noResults.setVisibility(View.GONE);
//                                            hintRemove.setVisibility(View.VISIBLE);
//                                        }
//                                        else	{
//                                            noResults.setVisibility(View.VISIBLE);
//                                            hintRemove.setVisibility(View.GONE);
//                                        }
//                                    }
//                                })
//                                .actionColor(getThemeUtils().accentColor())
//                        , getActivity());
//                return true;
//            }
//        };

        RecyclerView cantiRecycler = (RecyclerView) rootView.findViewById(R.id.cantiRecycler);

        // Creating new adapter object
        cantoAdapter = new CantoRecyclerAdapter(titoli, clickListener);
        cantiRecycler.setAdapter(cantoAdapter);

        cantiRecycler.setHasFixedSize(true);

        // Setting the layoutManager
        cantiRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

    }

    private void updateChooseList(List<Canto> titoliChoose) {

        List<Canto> titoli = titoliChoose;

        if (titoli == null) {
            // crea un manipolatore per il Database
            SQLiteDatabase db = listaCanti.getReadableDatabase();

            // lancia la ricerca dei canti
            String query = "SELECT A.titolo, A.color, A.pagina, A._id, coalesce(B._id,0)" +
                    "		FROM ELENCO A LEFT JOIN CANTI_CONSEGNATI B" +
                    "		ON A._id = B.id_canto" +
                    "		ORDER BY A.TITOLO ASC";
            Cursor lista = db.rawQuery(query, null);

            // crea un array e ci memorizza i titoli estratti
            titoli = new ArrayList<Canto>();
            lista.moveToFirst();
            for (int i = 0; i < lista.getCount(); i++) {
//            Log.i(getClass().toString(), "CANTO: " + Utility.intToString(lista.getInt(2), 3) + lista.getString(1) + lista.getString(0));
//            Log.i(getClass().toString(), "ID: " + lista.getInt(3));
//            Log.i(getClass().toString(), "SELEZIONATO: " + lista.getInt(4));
                titoli.add(new Canto(Utility.intToString(lista.getInt(2), 3) + lista.getString(1) + lista.getString(0)
                        , lista.getInt(3)
                        , lista.getInt(4) > 0));
                lista.moveToNext();
            }

            // chiude il cursore
            lista.close();
        }

        RecyclerView chooseRecycler = (RecyclerView) rootView.findViewById(R.id.chooseRecycler);

        // Creating new adapter object
        selectableAdapter = new CantoSelezionabileAdapter(titoli);
        chooseRecycler.setAdapter(selectableAdapter);

        chooseRecycler.setHasFixedSize(true);

        // Setting the layoutManager
        chooseRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    private ThemeUtils getThemeUtils() {
        return ((MainActivity)getActivity()).getThemeUtils();
    }

}
