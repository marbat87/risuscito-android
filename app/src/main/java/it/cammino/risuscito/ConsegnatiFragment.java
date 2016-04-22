package it.cammino.risuscito;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.cammino.risuscito.adapters.CantoRecyclerAdapter;
import it.cammino.risuscito.adapters.CantoSelezionabileAdapter;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.objects.Canto;
import it.cammino.risuscito.objects.CantoRecycled;
import it.cammino.risuscito.services.ConsegnatiSaverService;
import it.cammino.risuscito.slides.IntroConsegnati;
import it.cammino.risuscito.utils.ThemeUtils;

public class ConsegnatiFragment extends Fragment implements SimpleDialogFragment.SimpleCallback {

    private DatabaseCanti listaCanti;
    private List<Canto> titoliChoose;
    private View rootView;
    private CantoRecyclerAdapter cantoAdapter;
    private CantoSelezionabileAdapter selectableAdapter;

    private static final String EDIT_MODE = "editMode";
    public static final String TITOLI_CHOOSE = "titoliChoose";

    public static final int CIRCLE_DURATION = 500;

    private boolean editMode;
//    private int prevOrientation;
//    private MaterialDialog mProgressDialog;
    private int totalConsegnati;

    private static final String PREF_FIRST_OPEN = "prima_apertura_consegnati";

    private LUtils mLUtils;

    private long mLastClickTime = 0;

    private BroadcastReceiver positionBRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Implement UI change code here once notification is received
            try {
                Log.d(getClass().getName(), "BROADCAST_SINGLE_COMPLETED");
                Log.d(getClass().getName(), "DATA_DONE: " + intent.getIntExtra(ConsegnatiSaverService.DATA_DONE, 0));
                if (SimpleDialogFragment.findVisible((AppCompatActivity)getActivity(), "CONSEGNATI_SAVING") != null) {
                    SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "CONSEGNATI_SAVING")
                            .setProgress(intent.getIntExtra(ConsegnatiSaverService.DATA_DONE, 0));
                }
            }
            catch (IllegalArgumentException e) {
                Log.e(getClass().getName(), e.getLocalizedMessage(), e);
            }
        }
    };

    private BroadcastReceiver completedBRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Implement UI change code here once notification is received
            try {
                Log.d(getClass().getName(), "BROADCAST_SAVING_COMPLETED");
                if (SimpleDialogFragment.findVisible((AppCompatActivity)getActivity(), "CONSEGNATI_SAVING") != null)
                    SimpleDialogFragment.findVisible((AppCompatActivity)getActivity(), "CONSEGNATI_SAVING").dismiss();
                updateConsegnatiList(true);
                rootView.findViewById(R.id.choose_view).setVisibility(View.INVISIBLE);
                View myView = rootView.findViewById(R.id.consegnati_view);
                if (LUtils.hasL())
                    enterReveal(myView, 1);
                else
                    myView.setVisibility(View.VISIBLE);
            }
            catch (IllegalArgumentException e) {
                Log.e(getClass().getName(), e.getLocalizedMessage(), e);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.layout_consegnati, container, false);
        ((MainActivity) getActivity()).setupToolbar(rootView.findViewById(R.id.risuscito_toolbar), R.string.title_activity_consegnati);

        //crea un istanza dell'oggetto DatabaseCanti
//        listaCanti = new DatabaseCanti(getActivity());

        mLUtils = LUtils.getInstance(getActivity());

        rootView.findViewById(R.id.bottom_bar).setBackgroundColor(getThemeUtils().primaryColor());

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

        (rootView.findViewById(R.id.select_none)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Canto canto: titoliChoose) {
                    canto.setSelected(false);
                    selectableAdapter.notifyDataSetChanged();
                }
            }
        });

        (rootView.findViewById(R.id.select_all)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Canto canto: titoliChoose) {
                    canto.setSelected(true);
                    selectableAdapter.notifyDataSetChanged();
                }
            }
        });

        ImageButton cancel_change = (ImageButton) rootView.findViewById(R.id.cancel_change);
        Drawable drawable = DrawableCompat.wrap(cancel_change.getDrawable());
        DrawableCompat.setTint(drawable, ContextCompat.getColor(getActivity(), android.R.color.white));
        cancel_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editMode = false;
                updateConsegnatiList(true);
                rootView.findViewById(R.id.choose_view).setVisibility(View.INVISIBLE);
//                rootView.findViewById(R.id.consegnati_view).setVisibility(View.VISIBLE);
                View myView = rootView.findViewById(R.id.consegnati_view);
                if (LUtils.hasL())
                    enterReveal(myView, 1);
                else
                    myView.setVisibility(View.VISIBLE);
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
            }
        });

        ImageButton confirm_changes = (ImageButton) rootView.findViewById(R.id.confirm_changes);
        drawable = DrawableCompat.wrap(confirm_changes.getDrawable());
        DrawableCompat.setTint(drawable, ContextCompat.getColor(getActivity(), android.R.color.white));
        confirm_changes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                (new ConsegnatiSaveTask()).execute();
                editMode = false;
                new SimpleDialogFragment.Builder((AppCompatActivity)getActivity(), ConsegnatiFragment.this, "CONSEGNATI_SAVING")
                        .content(R.string.save_consegnati_running)
                        .showProgress(true)
                        .progressIndeterminate(false)
                        .progressMax(selectableAdapter.getItemCount())
                        .show();
                Intent intent = new Intent(getActivity().getApplicationContext(), ConsegnatiSaverService.class);
//                intent.putExtra(ConsegnatiSaverService.IDS_CONSEGNATI, selectableAdapter.getChoosedIds());
                intent.putIntegerArrayListExtra(ConsegnatiSaverService.IDS_CONSEGNATI, selectableAdapter.getChoosedIds());
                getActivity().getApplicationContext().startService(intent);
            }
        });

//        mProgressDialog = new MaterialDialog.Builder(getActivity())
//                .content(R.string.save_consegnati_running)
//                .progress(true, 0)
//                .dismissListener(new DialogInterface.OnDismissListener() {
//                    @Override
//                    public void onDismiss(DialogInterface dialog) {
//                        getActivity().setRequestedOrientation(prevOrientation);
//                    }
//                })
//                .build();

        if(PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getBoolean(PREF_FIRST_OPEN, true)) {
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(getActivity())
                    .edit();
            editor.putBoolean(PREF_FIRST_OPEN, false);
            editor.apply();
            showHelp();
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(getClass().getName(), "onResume: ");
        getActivity().registerReceiver(positionBRec, new IntentFilter(
                ConsegnatiSaverService.BROADCAST_SINGLE_COMPLETED));
        getActivity().registerReceiver(completedBRec, new IntentFilter(
                ConsegnatiSaverService.BROADCAST_SAVING_COMPLETED));
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(getClass().getName(), "onPause: ");
        getActivity().unregisterReceiver(positionBRec);
        getActivity().unregisterReceiver(completedBRec);
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
                if (LUtils.hasL())
                    enterReveal(myView, 2);
                else
                    myView.setVisibility(View.VISIBLE);
//                myView.setVisibility(View.VISIBLE);
//
//                // get the center for the clipping circle
//                int cx = myView.getRight();
//                int cy = myView.getTop();
//
//                // get the final radius for the clipping circle
//                int finalRadius = Math.max(myView.getWidth(), myView.getHeight());
//
//                SupportAnimator animator =
//                        ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
//                animator.setInterpolator(new AccelerateDecelerateInterpolator());
//                animator.setDuration(CIRCLE_DURATION);
//                animator.start();
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
        listaCanti = new DatabaseCanti(getActivity());
        SQLiteDatabase db = listaCanti.getReadableDatabase();

        // lancia la ricerca dei preferiti
        String query = "SELECT A.titolo, A.color, A.pagina, A._id, A.source" +
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
        List<CantoRecycled> titoli = new ArrayList<>();
        lista.moveToFirst();
        for (int i = 0; i < totalConsegnati; i++) {
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
        db.close();
        if (listaCanti != null)
            listaCanti.close();

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
                    return;
                mLastClickTime = SystemClock.elapsedRealtime();

                // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da visualizzare
                Bundle bundle = new Bundle();
                bundle.putString("pagina", String.valueOf(((TextView) v.findViewById(R.id.text_source_canto)).getText()));
                bundle.putInt("idCanto", Integer.valueOf(
                        String.valueOf(((TextView) v.findViewById(R.id.text_id_canto)).getText())));

                // lancia l'activity che visualizza il canto passando il parametro creato
                startSubActivity(bundle, v);
            }
        };

        RecyclerView cantiRecycler = (RecyclerView) rootView.findViewById(R.id.cantiRecycler);

        // Creating new adapter object
        cantoAdapter = new CantoRecyclerAdapter(getActivity(), titoli, clickListener);
        cantiRecycler.setAdapter(cantoAdapter);

        cantiRecycler.setHasFixedSize(true);

        // Setting the layoutManager
        cantiRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

    }

    private void updateChooseList(boolean reload) {

        if (reload) {
            // crea un manipolatore per il Database
            listaCanti = new DatabaseCanti(getActivity());
            SQLiteDatabase db = listaCanti.getReadableDatabase();

            // lancia la ricerca dei canti
            String query = "SELECT A.titolo, A.color, A.pagina, A._id, coalesce(B._id,0)" +
                    "		FROM ELENCO A LEFT JOIN CANTI_CONSEGNATI B" +
                    "		ON A._id = B.id_canto" +
                    "		ORDER BY A.TITOLO ASC";
            Cursor lista = db.rawQuery(query, null);

            // crea un array e ci memorizza i titoli estratti
            titoliChoose = new ArrayList<>();
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
            db.close();
            if (listaCanti != null)
                listaCanti.close();
        }

        RecyclerView chooseRecycler = (RecyclerView) rootView.findViewById(R.id.chooseRecycler);

        // Creating new adapter object
        selectableAdapter = new CantoSelezionabileAdapter(getActivity(), titoliChoose);
        chooseRecycler.setAdapter(selectableAdapter);

        chooseRecycler.setHasFixedSize(true);

        // Setting the layoutManager
        chooseRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

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

//    private class ConsegnatiSaveTask extends AsyncTask<String, Integer, String> {
//
//        public ConsegnatiSaveTask() {}
//
//        @Override
//        protected String doInBackground(String... sUrl) {
//            //aggiorno la lista dei consegnati
//
//            DatabaseCanti privateListaCanti = new DatabaseCanti(getActivity().getApplicationContext());
//            SQLiteDatabase db = privateListaCanti.getReadableDatabase();
//            db.delete("CANTI_CONSEGNATI", "", null);
//
//            List<Canto> choosedList = selectableAdapter.getCantiChoose();
//            for (int i = 0; i < choosedList.size(); i++) {
//                Canto singoloCanto = choosedList.get(i);
//                if (singoloCanto.isSelected()) {
//                    String sql = "INSERT INTO CANTI_CONSEGNATI" +
//                            "       (_id, id_canto)" +
//                            "   SELECT COALESCE(MAX(_id) + 1,1), " + singoloCanto.getIdCanto() +
//                            "             FROM CANTI_CONSEGNATI";
//
//                    try {
//                        db.execSQL(sql);
//                    } catch (SQLException e) {
//                        Log.e(getClass().toString(), "ERRORE INSERT:");
//                        e.printStackTrace();
//                    }
//                }
//            }
//            db.close();
//            if (privateListaCanti != null)
//                privateListaCanti.close();
//            return null;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            editMode = false;
////            prevOrientation = getActivity().getRequestedOrientation();
////            Utility.blockOrientation(getActivity());
////            mProgressDialog.show();
//            new SimpleDialogFragment.Builder((AppCompatActivity)getActivity(), "CONSEGNATI_SAVING")
//                    .content(R.string.save_consegnati_running)
//                    .showProgress(true)
//                    .progressIndeterminate(true)
//                    .progressMax(0)
//                    .show();
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
////            if (mProgressDialog.isShowing())
////                mProgressDialog.dismiss();
//            if (SimpleDialogFragment.findVisible((AppCompatActivity)getActivity(), "CONSEGNATI_SAVING") != null)
//                SimpleDialogFragment.findVisible((AppCompatActivity)getActivity(), "CONSEGNATI_SAVING").dismiss();
//            updateConsegnatiList(true);
//            rootView.findViewById(R.id.choose_view).setVisibility(View.INVISIBLE);
////            rootView.findViewById(R.id.consegnati_view).setVisibility(View.VISIBLE);
//            View myView = rootView.findViewById(R.id.consegnati_view);
//            if (LUtils.hasL())
//                enterReveal(myView, 1);
//            else
//                myView.setVisibility(View.VISIBLE);
////            myView.setVisibility(View.VISIBLE);
////
////            // get the center for the clipping circle
////            int cx = myView.getRight();
////            int cy = myView.getBottom();
////
////            // get the final radius for the clipping circle
////            int finalRadius = Math.max(myView.getWidth(), myView.getHeight());
////
////            SupportAnimator animator =
////                    ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
////            animator.setInterpolator(new AccelerateDecelerateInterpolator());
////            animator.setDuration(CIRCLE_DURATION);
////            animator.start();
//        }
//    }

    private void showHelp() {
        Intent intent = new Intent(getActivity(), IntroConsegnati.class);
        startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void enterReveal(View view, int mode) {
        // previously invisible view
//        final View myView = findViewById(R.id.my_view);

        // get the center for the clipping circle

//        int cx = view.getMeasuredWidth() / 2;
//        int cy = view.getMeasuredHeight() / 2;
        int cx = view.getRight();
        int cy = view.getBottom();
        if (mode == 2) {
            cx = view.getRight();
            cy = view.getTop();
        }

        // get the final radius for the clipping circle
        int finalRadius = Math.max(view.getWidth(), view.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);

        // make the view visible and start the animation
        view.setVisibility(View.VISIBLE);
        anim.start();
    }

    @Override
    public void onPositive(@NonNull String tag) {}
    @Override
    public void onNegative(@NonNull String tag) {}
    @Override
    public void onNeutral(@NonNull String tag) {}

}
