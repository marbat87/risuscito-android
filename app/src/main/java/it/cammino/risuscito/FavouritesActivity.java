package it.cammino.risuscito;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.util.ArrayList;
import java.util.List;

import it.cammino.risuscito.utils.ThemeUtils;

public class FavouritesActivity extends Fragment {

    private DatabaseCanti listaCanti;
    private List<CantoItem> titoli;
    private String cantoDaCanc;
    private int posizDaCanc;
    private View rootView;
	private RecyclerView recyclerView;
    private CantoRecyclerAdapter cantoAdapter;

    private LUtils mLUtils;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.activity_favourites, container, false);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_activity_favourites);
        ((MainActivity) getActivity()).getSupportActionBar()
                .setElevation(dpToPx(getResources().getInteger(R.integer.toolbar_elevation)));

        //crea un istanza dell'oggetto DatabaseCanti
        listaCanti = new DatabaseCanti(getActivity());

        mLUtils = LUtils.getInstance(getActivity());

        return rootView;
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
        String query = "SELECT titolo, color, pagina" +
                "		FROM ELENCO" +
                "		WHERE favourite = 1" +
                "		ORDER BY TITOLO ASC";
        Cursor lista = db.rawQuery(query, null);

        //recupera il numero di record trovati
        int total = lista.getCount();

        //nel caso sia presente almeno un preferito, viene nascosto il testo di nessun canto presente
        TextView noResults = (TextView) rootView.findViewById(R.id.no_favourites);
        TextView hintRemove = (TextView) rootView.findViewById(R.id.hint_remove);
        if (total > 0) {
            noResults.setVisibility(View.GONE);
            hintRemove.setVisibility(View.VISIBLE);
        }
        else	{
            noResults.setVisibility(View.VISIBLE);
            hintRemove.setVisibility(View.GONE);
        }

        // crea un array e ci memorizza i titoli estratti
        titoli = new ArrayList<CantoItem>();
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

        View.OnLongClickListener longClickListener  = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                cantoDaCanc = ((TextView) v.findViewById(R.id.text_title)).getText().toString();
                cantoDaCanc = Utility.duplicaApostrofi(cantoDaCanc);
                posizDaCanc = recyclerView.getChildPosition(v);
                SnackbarManager.show(
                        Snackbar.with(getActivity())
                                .text(getString(R.string.favorite_remove))
                                .actionLabel(getString(R.string.snackbar_remove))
                                .actionListener(new ActionClickListener() {
                                    @Override
                                    public void onActionClicked(Snackbar snackbar) {
                                        SQLiteDatabase db = listaCanti.getReadableDatabase();
                                        String sql = "UPDATE ELENCO" +
                                                "  SET favourite = 0" +
                                                "  WHERE titolo =  '" + cantoDaCanc + "'";
                                        db.execSQL(sql);
                                        db.close();
                                        // updateFavouritesList();
										titoli.remove(posizDaCanc);
                                        cantoAdapter.notifyItemRemoved(posizDaCanc);
                                    }
                                })
                                .actionColor(getThemeUtils().accentColor())
                        , getActivity());
                return true;
            }
        };

        recyclerView = (RecyclerView) rootView.findViewById(R.id.favouritesList);

        // Creating new adapter object
        cantoAdapter = new CantoRecyclerAdapter(titoli, clickListener, longClickListener);
        recyclerView.setAdapter(cantoAdapter);

        // Setting the layoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

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
