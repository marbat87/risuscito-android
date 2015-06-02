package it.cammino.risuscito;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import it.cammino.risuscito.adapters.CantoRecyclerAdapter;
import it.cammino.risuscito.adapters.CantoSelezionabileAdapter;
import it.cammino.risuscito.objects.Canto;
import it.cammino.risuscito.utils.ThemeUtils;
import it.cammino.utilities.showcaseview.OnShowcaseEventListener;
import it.cammino.utilities.showcaseview.ShowcaseView;
import it.cammino.utilities.showcaseview.targets.ViewTarget;

public class ConsegnatiFragment extends Fragment {

    private DatabaseCanti listaCanti;
    private List<Canto> titoliChoose;
    private View rootView;
    private CantoRecyclerAdapter cantoAdapter;
    private CantoSelezionabileAdapter selectableAdapter;

    private static final String EDIT_MODE = "editMode";
    public static final String TITOLI_CHOOSE = "titoliChoose";

    public static final int CIRCLE_DURATION = 500;

    private boolean editMode;
    private int prevOrientation;
    private MaterialDialog mProgressDialog;
    private int totalConsegnati;
    private RelativeLayout.LayoutParams lps;
    private boolean byGuide;

    private static final String PREF_FIRST_OPEN = "prima_apertura_consegnati";

    private LUtils mLUtils;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.layout_consegnati, container, false);
//        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_activity_consegnati);
//        ((TextView)((MainActivity) getActivity()).findViewById(R.id.main_toolbarTitle)).setText(R.string.title_activity_consegnati);
//        ((MainActivity) getActivity()).getSupportActionBar()
//                .setElevation(dpToPx(getResources().getInteger(R.integer.toolbar_elevation)));
        ((MainActivity) getActivity()).setupToolbar(rootView.findViewById(R.id.risuscito_toolbar), R.string.title_activity_consegnati);

        //crea un istanza dell'oggetto DatabaseCanti
        listaCanti = new DatabaseCanti(getActivity());

        mLUtils = LUtils.getInstance(getActivity());

        rootView.findViewById(R.id.bottom_bar).setBackgroundColor(getThemeUtils().primaryColor());
//        Typeface face=Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
//        ((TextView) rootView.findViewById(R.id.consegnati_text)).setTypeface(face);

        if (savedInstanceState == null)
            editMode = false;
        else {
            editMode = savedInstanceState.getBoolean(EDIT_MODE, false);
            if (editMode) {
                RetainedFragment dataFragment = (RetainedFragment) getActivity().getSupportFragmentManager().findFragmentByTag(TITOLI_CHOOSE);
                if (dataFragment != null)
                    titoliChoose = dataFragment.getData();
            }
        }

        if (editMode) {
            rootView.findViewById(R.id.choose_view).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.consegnati_view).setVisibility(View.INVISIBLE);
            updateChooseList(false);
        }
        else {
            rootView.findViewById(R.id.choose_view).setVisibility(View.INVISIBLE);
            rootView.findViewById(R.id.consegnati_view).setVisibility(View.VISIBLE);
            updateConsegnatiList(true);
        }

        ((ImageButton)rootView.findViewById(R.id.select_none)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Canto canto: titoliChoose) {
                    canto.setSelected(false);
                    selectableAdapter.notifyDataSetChanged();
                }
            }
        });

        ((ImageButton)rootView.findViewById(R.id.select_all)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Canto canto: titoliChoose) {
                    canto.setSelected(true);
                    selectableAdapter.notifyDataSetChanged();
                }
            }
        });

        ((ImageButton)rootView.findViewById(R.id.cancel_change)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editMode = false;
                updateConsegnatiList(true);
                rootView.findViewById(R.id.choose_view).setVisibility(View.INVISIBLE);
//                rootView.findViewById(R.id.consegnati_view).setVisibility(View.VISIBLE);
                View myView = rootView.findViewById(R.id.consegnati_view);
                myView.setVisibility(View.VISIBLE);

                // get the center for the clipping circle
                int cx = myView.getRight();
                int cy = myView.getBottom();

                // get the final radius for the clipping circle
                int finalRadius = Math.max(myView.getWidth(), myView.getHeight());

                SupportAnimator animator =
                        ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(CIRCLE_DURATION);
                animator.start();
            }
        });
        ((ImageButton)rootView.findViewById(R.id.confirm_changes)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                (new ConsegnatiSaveTask()).execute();
//                rootView.findViewById(R.id.no_consegnati).setVisibility(totalConsegnati > 0 ? View.GONE: View.VISIBLE);
//                editMode = false;
//                rootView.findViewById(R.id.choose_view).setVisibility(View.GONE);
////                rootView.findViewById(R.id.consegnati_view).setVisibility(View.VISIBLE);
//                View myView = rootView.findViewById(R.id.consegnati_view);
//                myView.setVisibility(View.VISIBLE);
//
//                // get the center for the clipping circle
//                int cx = myView.getRight();
//                int cy = myView.getBottom();
//
//                // get the final radius for the clipping circle
//                int finalRadius = Math.max(myView.getWidth(), myView.getHeight());
//
//                SupportAnimator animator =
//                        ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
//                animator.setInterpolator(new AccelerateDecelerateInterpolator());
//                animator.setDuration(CIRCLE_DURATION);
//                animator.start();
//
//                //aggiorno la lista dei consegnati
//                SQLiteDatabase db = listaCanti.getReadableDatabase();
//                db.delete("CANTI_CONSEGNATI", "", null);
//
//                List<Canto> choosedList = selectableAdapter.getCantiChoose();
//                for (int i = 0; i < choosedList.size(); i++) {
//                    Canto singoloCanto = choosedList.get(i);
//                    if (singoloCanto.isSelected()) {
//                        String sql = "INSERT INTO CANTI_CONSEGNATI" +
//                                "       (_id, id_canto)" +
//                                "   SELECT COALESCE(MAX(_id) + 1,1), " + singoloCanto.getIdCanto() +
//                                "             FROM CANTI_CONSEGNATI";
//
//                        try {
//                            db.execSQL(sql);
//                        } catch (SQLException e) {
//                            Log.e(getClass().toString(), "ERRORE INSERT:");
//                            e.printStackTrace();
//                        }
//                    }
//                }
//                db.close();
//
//                updateConsegnatiList();
            }
        });

        mProgressDialog = new MaterialDialog.Builder(getActivity())
                .content(R.string.save_consegnati_running)
                .progress(true, 0)
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        getActivity().setRequestedOrientation(prevOrientation);
                    }
                })
                .build();

        if(PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getBoolean(PREF_FIRST_OPEN, true)) {
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(getActivity())
                    .edit();
            editor.putBoolean(PREF_FIRST_OPEN, false);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                editor.commit();
            } else {
                editor.apply();
            }
            final Runnable mMyRunnable = new Runnable() {
                @Override
                public void run() {
                    showHelp();
                }
            };
            Handler myHandler = new Handler();
            myHandler.postDelayed(mMyRunnable, 1000);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(EDIT_MODE, editMode);
        super.onSaveInstanceState(savedInstanceState);
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
                updateChooseList(true);
                rootView.findViewById(R.id.consegnati_view).setVisibility(View.INVISIBLE);
//                rootView.findViewById(R.id.choose_view).setVisibility(View.VISIBLE);
                View myView = rootView.findViewById(R.id.choose_view);
                myView.setVisibility(View.VISIBLE);

                // get the center for the clipping circle
                int cx = myView.getRight();
                int cy = myView.getTop();

                // get the final radius for the clipping circle
                int finalRadius = Math.max(myView.getWidth(), myView.getHeight());

                SupportAnimator animator =
                        ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(CIRCLE_DURATION);
                animator.start();
                return true;
            case R.id.action_help:
                showHelp();
                return true;
        }
        return false;
    }

    private void startSubActivity(Bundle bundle, View view) {
        Intent intent = new Intent(getActivity(), PaginaRenderActivity.class);
        intent.putExtras(bundle);
        mLUtils.startActivityWithTransition(intent, view, Utility.TRANS_PAGINA_RENDER);
    }

    private void updateConsegnatiList(boolean updateView) {

        // crea un manipolatore per il Database in modalità READ
        SQLiteDatabase db = listaCanti.getReadableDatabase();

        // lancia la ricerca dei preferiti
        String query = "SELECT A.titolo, A.color, A.pagina" +
                "		FROM ELENCO A, CANTI_CONSEGNATI B" +
                "		WHERE A._id = B.id_canto" +
                "		ORDER BY TITOLO ASC";
        Cursor lista = db.rawQuery(query, null);

        //recupera il numero di record trovati
        totalConsegnati = lista.getCount();

        //nel caso sia presente almeno un preferito, viene nascosto il testo di nessun canto presente
        if (updateView)
            rootView.findViewById(R.id.no_consegnati).setVisibility(totalConsegnati > 0 ? View.INVISIBLE: View.VISIBLE);

        // crea un array e ci memorizza i titoli estratti
        List<CantoItem> titoli = new ArrayList<CantoItem>();
        lista.moveToFirst();
        for (int i = 0; i < totalConsegnati; i++) {
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

        RecyclerView cantiRecycler = (RecyclerView) rootView.findViewById(R.id.cantiRecycler);

        // Creating new adapter object
        cantoAdapter = new CantoRecyclerAdapter(titoli, clickListener);
        cantiRecycler.setAdapter(cantoAdapter);

        cantiRecycler.setHasFixedSize(true);

        // Setting the layoutManager
        cantiRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

    }

    private void updateChooseList(boolean reload) {

        if (reload) {
            // crea un manipolatore per il Database
            SQLiteDatabase db = listaCanti.getReadableDatabase();

            // lancia la ricerca dei canti
            String query = "SELECT A.titolo, A.color, A.pagina, A._id, coalesce(B._id,0)" +
                    "		FROM ELENCO A LEFT JOIN CANTI_CONSEGNATI B" +
                    "		ON A._id = B.id_canto" +
                    "		ORDER BY A.TITOLO ASC";
            Cursor lista = db.rawQuery(query, null);

            // crea un array e ci memorizza i titoli estratti
            titoliChoose = new ArrayList<Canto>();
            lista.moveToFirst();
            for (int i = 0; i < lista.getCount(); i++) {
//            Log.i(getClass().toString(), "CANTO: " + Utility.intToString(lista.getInt(2), 3) + lista.getString(1) + lista.getString(0));
//            Log.i(getClass().toString(), "ID: " + lista.getInt(3));
//            Log.i(getClass().toString(), "SELEZIONATO: " + lista.getInt(4));
                titoliChoose.add(new Canto(Utility.intToString(lista.getInt(2), 3) + lista.getString(1) + lista.getString(0)
                        , lista.getInt(3)
                        , lista.getInt(4) > 0));
                lista.moveToNext();
            }

            // chiude il cursore
            lista.close();
        }

        RecyclerView chooseRecycler = (RecyclerView) rootView.findViewById(R.id.chooseRecycler);

        // Creating new adapter object
        selectableAdapter = new CantoSelezionabileAdapter(titoliChoose);
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

    public static class RetainedFragment extends Fragment {

        // data object we want to retain
        private List<Canto> data;

        // this method is only called once for this fragment
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // retain this fragment
            setRetainInstance(true);
        }

        public void setData(List<Canto> data) {
            this.data = data;
        }

        public List<Canto> getData() {
            return data;
        }
    }

    public List<Canto> getTitoliChoose() {
        return titoliChoose;
    }

//    public void setTitoliChoose(List<Canto> titoliChoose) {
//        this.titoliChoose = titoliChoose;
//    }

    private class ConsegnatiSaveTask extends AsyncTask<String, Integer, String> {

        public ConsegnatiSaveTask() {}

        @Override
        protected String doInBackground(String... sUrl) {
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
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            editMode = false;
            prevOrientation = getActivity().getRequestedOrientation();
            Utility.blockOrientation(getActivity());
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            if (mProgressDialog.isShowing())
                mProgressDialog.dismiss();
            updateConsegnatiList(true);
            rootView.findViewById(R.id.choose_view).setVisibility(View.INVISIBLE);
//            rootView.findViewById(R.id.consegnati_view).setVisibility(View.VISIBLE);
            View myView = rootView.findViewById(R.id.consegnati_view);
            myView.setVisibility(View.VISIBLE);

            // get the center for the clipping circle
            int cx = myView.getRight();
            int cy = myView.getBottom();

            // get the final radius for the clipping circle
            int finalRadius = Math.max(myView.getWidth(), myView.getHeight());

            SupportAnimator animator =
                    ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(CIRCLE_DURATION);
            animator.start();
        }
    }

    private void showHelp() {
        prevOrientation = getActivity().getRequestedOrientation();
        Utility.blockOrientation(getActivity());

        lps = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        int margin = ((Number) (getActivity().getApplicationContext().getResources().getDisplayMetrics().density * 12)).intValue();
        int marginLeft = ((Number) (getActivity().getApplicationContext().getResources().getDisplayMetrics().density * 12)).intValue();
        int marginBottom = ((Number) (getActivity().getApplicationContext().getResources().getDisplayMetrics().density * 12)).intValue();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                marginBottom = ((Number) (getActivity().getApplicationContext().getResources().getDisplayMetrics()
                        .density * 62)).intValue();
            else
                marginLeft = ((Number) (getActivity().getApplicationContext().getResources().getDisplayMetrics()
                        .density * 62)).intValue();
        }
        lps.setMargins(marginLeft, margin, margin, marginBottom);

        ShowcaseView showCase = ShowcaseView.insertShowcaseView(
                new ViewTarget(R.id.action_edit_choose, getActivity())
                , getActivity()
                , R.string.title_activity_consegnati
                , R.string.showcase_consegnati_desc);
        showCase.setButtonText(getString(R.string.showcase_button_next));
        showCase.setShowcase(ShowcaseView.NONE);
        showCase.setOnShowcaseEventListener(new OnShowcaseEventListener() {

            @Override
            public void onShowcaseViewShow(ShowcaseView showcaseView) {
            }

            @Override
            public void onShowcaseViewHide(ShowcaseView showcaseView) {
                ShowcaseView showCase = ShowcaseView.insertShowcaseView(
                        new ViewTarget(R.id.action_edit_choose, getActivity())
                        , getActivity()
                        , R.string.title_activity_consegnati
                        , R.string.showcase_consegnati_howto);
                showCase.setButtonText(getString(R.string.showcase_button_next));
                showCase.setScaleMultiplier(0.3f);
                if (rootView.findViewById(R.id.choose_view).getVisibility() != View.VISIBLE) {
                    byGuide = true;
                    updateChooseList(true);
                    rootView.findViewById(R.id.consegnati_view).setVisibility(View.INVISIBLE);
                    rootView.findViewById(R.id.choose_view).setVisibility(View.VISIBLE);
                } else {
                    byGuide = false;
                }
                showCase.setOnShowcaseEventListener(new OnShowcaseEventListener() {

                    @Override
                    public void onShowcaseViewShow(ShowcaseView showcaseView) {
                    }

                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                        ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
                        co.buttonLayoutParams = lps;
                        ShowcaseView showCase = ShowcaseView.insertShowcaseView(
                                new ViewTarget(R.id.confirm_changes, getActivity())
                                , getActivity()
                                , R.string.title_activity_consegnati
                                , R.string.single_choice_ok
                                , co);
                        showCase.setButtonText(getString(R.string.showcase_button_next));
                        showCase.setScaleMultiplier(0.3f);
                        showCase.setOnShowcaseEventListener(new OnShowcaseEventListener() {

                            @Override
                            public void onShowcaseViewShow(ShowcaseView showcaseView) {
                            }

                            @Override
                            public void onShowcaseViewHide(ShowcaseView showcaseView) {
                                ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
                                co.buttonLayoutParams = lps;
                                ShowcaseView showCase = ShowcaseView.insertShowcaseView(
                                        new ViewTarget(R.id.cancel_change, getActivity())
                                        , getActivity()
                                        , R.string.title_activity_consegnati
                                        , R.string.cancel
                                        , co);
                                showCase.setScaleMultiplier(0.3f);
                                showCase.setOnShowcaseEventListener(new OnShowcaseEventListener() {

                                    @Override
                                    public void onShowcaseViewShow(ShowcaseView showcaseView) {
                                    }

                                    @Override
                                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                                        if (byGuide) {
                                            if (rootView.findViewById(R.id.choose_view).getVisibility() == View.VISIBLE)
                                                rootView.findViewById(R.id.cancel_change).performClick();
                                        }
                                        getActivity().setRequestedOrientation(prevOrientation);
                                    }

                                    @Override
                                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                                    }
                                });
                            }

                            @Override
                            public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                            }
                        });
                    }


                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                    }
                });

            }

            @Override
            public void onShowcaseViewDidHide(ShowcaseView showcaseView) {}
        });
    }

}
