package it.cammino.risuscito;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import it.cammino.risuscito.adapters.CantoHistoryRecyclerAdapter;
import it.cammino.risuscito.objects.CantoHistory;
import it.cammino.risuscito.utils.ThemeUtils;

public class HistoryFragment extends Fragment {

    private DatabaseCanti listaCanti;
    private List<CantoHistory> titoli;
    private String cantoDaCanc;
    private int posizDaCanc;
    private View rootView;
	private RecyclerView recyclerView;
    private CantoHistoryRecyclerAdapter cantoAdapter;

    private LUtils mLUtils;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.layout_history, container, false);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_activity_history);
        ((MainActivity) getActivity()).getSupportActionBar()
                .setElevation(dpToPx(getResources().getInteger(R.integer.toolbar_elevation)));

        //crea un istanza dell'oggetto DatabaseCanti
        listaCanti = new DatabaseCanti(getActivity());

        mLUtils = LUtils.getInstance(getActivity());

        Typeface face=Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
        ((TextView) rootView.findViewById(R.id.favorites_text)).setTypeface(face);
        ((TextView) rootView.findViewById(R.id.hint_remove)).setTypeface(face);

        return rootView;
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
        View noResults = rootView.findViewById(R.id.no_history);
        noResults.setVisibility(total > 0 ? View.INVISIBLE : View.VISIBLE);

        // crea un array e ci memorizza i titoli estratti
        titoli = new ArrayList<>();
        lista.moveToFirst();
        for (int i = 0; i < total; i++) {

            Timestamp timestamp = Timestamp.valueOf(lista.getString(5));
            String dateResult = getString(R.string.last_open_date) + " " + timestamp.getDate() + " " + getString(R.string.last_open_hour) + ".";

            titoli.add(new CantoHistory(Utility.intToString(lista.getInt(3), 3) + lista.getString(2) + lista.getString(1)
                , lista.getInt(0)
                , lista.getString(5)
                , dateResult));
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

        View.OnLongClickListener longClickListener  = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                cantoDaCanc = ((TextView) v.findViewById(R.id.text_id_canto)).getText().toString();
                posizDaCanc = recyclerView.getChildAdapterPosition(v);
                SnackbarManager.show(
                        Snackbar.with(getActivity())
                                .text(getString(R.string.history_remove))
                                .actionLabel(getString(R.string.snackbar_remove))
                                .actionListener(new ActionClickListener() {
                                    @Override
                                    public void onActionClicked(Snackbar snackbar) {
                                        SQLiteDatabase db = listaCanti.getReadableDatabase();
                                        db.delete("CRONOLOGIA", "id_canto = " + cantoDaCanc, null);
                                        db.close();
										titoli.remove(posizDaCanc);
                                        cantoAdapter.notifyItemRemoved(posizDaCanc);
                                        //nel caso sia presente almeno un canto recente, viene nascosto il testo di nessun canto presente
                                        View noResults = rootView.findViewById(R.id.no_history);
                                        noResults.setVisibility(titoli.size() > 0 ? View.INVISIBLE : View.VISIBLE);
                                    }
                                })
                                .actionColor(getThemeUtils().accentColor())
                        , getActivity());
                return true;
            }
        };

        recyclerView = (RecyclerView) rootView.findViewById(R.id.favouritesList);

        // Creating new adapter object
        cantoAdapter = new CantoHistoryRecyclerAdapter(titoli, clickListener, longClickListener);
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
