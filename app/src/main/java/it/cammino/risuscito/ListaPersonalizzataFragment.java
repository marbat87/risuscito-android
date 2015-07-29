package it.cammino.risuscito;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.alexkolpa.fabtoolbar.FabToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.cammino.risuscito.adapters.PosizioneRecyclerAdapter;
import it.cammino.risuscito.objects.PosizioneItem;
import it.cammino.risuscito.utils.ThemeUtils;

public class ListaPersonalizzataFragment extends Fragment {

    private int posizioneDaCanc;
    private View rootView;
    private ShareActionProvider mShareActionProvider;
    private DatabaseCanti listaCanti;
    String cantoDaCanc;
    private SQLiteDatabase db;
    private int fragmentIndex;
    private int idLista;
    private ListaPersonalizzata listaPersonalizzata;
    public ActionMode mMode;
    private boolean mSwhitchMode;
    //    private View mActionModeView;
    private List<PosizioneItem> posizioniList;
    private int longclickedPos;
    private RecyclerView recyclerView;
    private PosizioneRecyclerAdapter cantoAdapter;
//	private int prevOrientation;

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

//		((ObservableScrollView) rootView.findViewById(R.id.personalizzataScrollView)).setScrollViewCallbacks(new ObservableScrollViewCallbacks() {
//			@Override
//			public void onScrollChanged(int i, boolean b, boolean b1) {}
//
//			@Override
//			public void onDownMotionEvent() {}
//
//			@Override
//			public void onUpOrCancelMotionEvent(ScrollState scrollState) {
//				FloatingActionsMenu fab1 = ((CustomLists) getParentFragment()).getFab1();
////                Log.i(getClass().toString(), "scrollState: " + scrollState);
//				if (scrollState == ScrollState.UP) {
//					if (fab1.isVisible())
//						fab1.hide();
//				} else if (scrollState == ScrollState.DOWN) {
//					if (!fab1.isVisible())
//						fab1.show();
//				}
//			}
//		});

        mLUtils = LUtils.getInstance(getActivity());
        mMode = null;
        mSwhitchMode = false;

        fragmentIndex = getArguments().getInt("position");
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

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
//            ((CustomLists) getParentFragment()).fabDelete.setEnabled(true);
//            ((CustomLists) getParentFragment()).fabEdit.setEnabled(true);
//            if (LUtils.hasHoneycomb()) {
            ((CustomLists) getParentFragment()).fabDelete.setVisibility(View.VISIBLE);
            ((CustomLists) getParentFragment()).fabEdit.setVisibility(View.VISIBLE);
//            }
            FabToolbar fab1 = ((CustomLists) getParentFragment()).getFab();
//            if (!fab1.isShowing())
            fab1.scrollUp();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

//    @Override
//    public void onResume() {
////		Log.i("LISTA PERS", "ON RESUME");
//        super.onResume();
//        fragmentIndex = getArguments().getInt("position");
//        idLista = getArguments().getInt("idLista");
////		Log.i("fragmentIndex", fragmentIndex+"");
////		Log.i("idLista", idLista+"");
//
//        db = listaCanti.getReadableDatabase();
//
//        String query = "SELECT lista" +
//                "  FROM LISTE_PERS" +
//                "  WHERE _id =  " + idLista;
//        Cursor cursor = db.rawQuery(query, null);
//
//        // recupera l'oggetto lista personalizzata
//        cursor.moveToFirst();
//
//        listaPersonalizzata = (ListaPersonalizzata) ListaPersonalizzata.
//                deserializeObject(cursor.getBlob(0));
//
//        updateLista();
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.i(getClass().getName(), "requestCode: " + requestCode);
        if (requestCode == TAG_INSERT_PERS + idLista && resultCode == Activity.RESULT_OK) {
//            Log.i("LISTA PERS", "ON RESUME");
            fragmentIndex = getArguments().getInt("position");
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

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
//		inflater.inflate(R.menu.list_with_delete, menu);
        MenuItem shareItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        ViewPager tempPager = (ViewPager) getActivity().findViewById(R.id.view_pager);
        if (listaPersonalizzata != null && mShareActionProvider != null && tempPager.getCurrentItem() == fragmentIndex)
            mShareActionProvider.setShareIntent(getDefaultIntent());
    }

    private Intent getDefaultIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, getTitlesList());
        intent.setType("text/plain");
        return intent;
    }

    private void openPagina(View v) {
        // recupera il titolo della voce cliccata
//        String cantoCliccato = ((TextView) v.findViewById(R.id.text_title)).getText().toString();
//        cantoCliccato = Utility.duplicaApostrofi(cantoCliccato);
//
//        // crea un manipolatore per il DB in modalità READ
//        db = listaCanti.getReadableDatabase();
//
//        // esegue la query per il recupero del nome del file della pagina da visualizzare
//        String query = "SELECT source, _id" +
//                "  FROM ELENCO" +
//                "  WHERE titolo =  '" + cantoCliccato + "'";
//        Cursor cursor = db.rawQuery(query, null);
//
//        // recupera il nome del file
//        cursor.moveToFirst();
//        String pagina = cursor.getString(0);
//        int idCanto = cursor.getInt(1);
//
//        // chiude il cursore
//        cursor.close();
//        db.close();

        // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da visualizzare
        Bundle bundle = new Bundle();
//        bundle.putString("pagina", pagina);
//        bundle.putInt("idCanto", idCanto);
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

//        LinearLayout linLayout = (LinearLayout) rootView.findViewById(R.id.listaScroll);
//        linLayout.removeAllViews();

        posizioniList = new ArrayList<>();

        OnClickListener click = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
                    return;
                mLastClickTime = SystemClock.elapsedRealtime();
                View parent = (View) v.getParent();
                if (parent.findViewById(R.id.cantoGenericoContainer).getVisibility() == View.GONE) {
                    if (mSwhitchMode)
                        scambioConVuoto(Integer.valueOf(((TextView) parent.findViewById(R.id.text_id_posizione)).getText().toString()));
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
                            snackBarRimuoviCanto(parent);
                        }
                        else
                            openPagina(parent);
                    else {
                        scambioCanto(Integer.valueOf(((TextView) parent.findViewById(R.id.text_id_posizione)).getText().toString()));
                    }
                }
            }
        };

        OnLongClickListener longClick = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                View parent = (View) v.getParent();
                posizioneDaCanc = Integer.valueOf(((TextView) parent.findViewById(R.id.text_id_posizione)).getText().toString());
                snackBarRimuoviCanto(parent);
                return true;
            }
        };

        for (int cantoIndex = 0; cantoIndex < listaPersonalizzata.getNumPosizioni(); cantoIndex++) {
//            View view = getActivity().getLayoutInflater().inflate(R.layout.oggetto_lista_generico, linLayout, false);
//
//            ((TextView) view.findViewById(R.id.titoloPosizioneGenerica))
//                    .setText(listaPersonalizzata.getNomePosizione(cantoIndex));
//
//            ((TextView) view.findViewById(R.id.id_posizione))
//                    .setText(String.valueOf(cantoIndex));

//	   		Log.i("CANTO[" + cantoIndex + "]", listaPersonalizzata.getCantoPosizione(cantoIndex) + " ");

            PosizioneItem result;

            if (listaPersonalizzata.getCantoPosizione(cantoIndex).length() == 0) {

//                view.findViewById(R.id.addCantoGenerico).setVisibility(View.VISIBLE);
//                view.findViewById(R.id.cantoGenericoContainer).setVisibility(View.GONE);
//
//                view.findViewById(R.id.addCantoGenerico).setOnClickListener(new OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
//                            return;
//                        mLastClickTime = SystemClock.elapsedRealtime();
//                        if (mSwhitchMode)
//                            scambioConVuoto(v);
//                        else {
//                            if (mMode == null) {
//                                Bundle bundle = new Bundle();
//                                bundle.putInt("fromAdd", 0);
//                                bundle.putInt("idLista", idLista);
//                                bundle.putInt("position", (Integer.valueOf(
//                                        ((TextView) v.findViewById(R.id.id_posizione))
//                                                .getText().toString())));
//                                Intent intent = new Intent(getActivity(), GeneralInsertSearch.class);
//                                intent.putExtras(bundle);
//                                getParentFragment().startActivityForResult(intent, TAG_INSERT_PERS + idLista);
//                                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.hold_on);
//                            }
//                        }
//                    }
//                });
//
                result = new PosizioneItem(listaPersonalizzata.getNomePosizione(cantoIndex)
                        , idLista
                        , cantoIndex
                        , cantoIndex);
            }
            else {

//                //setto l'id del canto nell'apposito canto
//                ((TextView) view.findViewById(R.id.id_da_canc))
//                        .setText(String.valueOf(cantoIndex));
//
//                view.findViewById(R.id.addCantoGenerico).setVisibility(View.GONE);
//                View temp = view.findViewById(R.id.cantoGenericoContainer);
//                temp.setVisibility(View.VISIBLE);
//                temp.setOnClickListener(new OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
//                            return;
//                        mLastClickTime = SystemClock.elapsedRealtime();
//                        if (mSwhitchMode)
//                            scambioCanto(view);
//                        else {
//                            if (mMode != null) {
//                                posizioneDaCanc = Integer.valueOf(
//                                        ((TextView) view.findViewById(R.id.id_da_canc))
//                                                .getText().toString());
//                                snackBarRimuoviCanto(view);
//                            }
//                            else
//                                openPagina(view);
//                        }
//                    }
//                });
//                // setta l'azione tenendo premuto sul canto
//                temp.setOnLongClickListener(new OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View view) {
//                        posizioneDaCanc = Integer.valueOf(
////                                ((TextView) ((ViewGroup) view.getParent()).findViewById(R.id.id_da_canc))
//                                ((TextView) view.findViewById(R.id.id_da_canc))
//                                        .getText().toString());
////						Log.i("canto da rimuovere", posizioneDaCanc + " ");
//                        snackBarRimuoviCanto(view);
//                        return true;
//                    }
//                });

                db = listaCanti.getReadableDatabase();

                String query = "SELECT _id, titolo, pagina, color, source" +
                        "  FROM ELENCO" +
                        "  WHERE _id =  " + listaPersonalizzata.getCantoPosizione(cantoIndex);
                Cursor cursor = db.rawQuery(query, null);
                cursor.moveToFirst();

                //setto il titolo del canto
//                ((TextView) view.findViewById(R.id.text_title))
//                        .setText(cursor.getString(0));
//
//                //setto la pagina
//                int tempPagina = cursor.getInt(1);
//                String pagina = String.valueOf(tempPagina);
//                TextView textPage = (TextView) view.findViewById(R.id.text_page);
//                textPage.setText(pagina);
//
//                //setto il colore
//                String colore = cursor.getString(2);
//                if (colore.equalsIgnoreCase(Utility.GIALLO))
//                    textPage.setBackgroundResource(R.drawable.bkg_round_yellow);
//                if (colore.equalsIgnoreCase(Utility.GRIGIO))
//                    textPage.setBackgroundResource(R.drawable.bkg_round_grey);
//                if (colore.equalsIgnoreCase(Utility.VERDE))
//                    textPage.setBackgroundResource(R.drawable.bkg_round_green);
//                if (colore.equalsIgnoreCase(Utility.AZZURRO))
//                    textPage.setBackgroundResource(R.drawable.bkg_round_blue);
//                if (colore.equalsIgnoreCase(Utility.BIANCO))
//                    textPage.setBackgroundResource(R.drawable.bkg_round_white);

                result = new PosizioneItem(listaPersonalizzata.getNomePosizione(cantoIndex)
                        , 1
                        , cantoIndex
                        , cursor.getInt(2)
                        , cursor.getString(1)
                        , cursor.getString(3)
                        , cursor.getInt(0)
                        , cursor.getString(4)
                        , ""
                        , cantoIndex);

                cursor.close();
                db.close();

            }

//            linLayout.addView(view);
            posizioniList.add(result);
        }

        recyclerView = (RecyclerView) rootView.findViewById(R.id.parolaList);

        // Creating new adapter object
        cantoAdapter = new PosizioneRecyclerAdapter(getActivity(), posizioniList, click, longClick);
        recyclerView.setAdapter(cantoAdapter);

        // Setting the layoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

//        View view = getActivity().getLayoutInflater().inflate(R.layout.lista_pers_button, linLayout, true);
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
                mShareActionProvider.setShareIntent(getDefaultIntent());
            }
        });

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
//                db = listaCanti.getReadableDatabase();
//
//                String query = "SELECT titolo, pagina" +
//                        "  FROM ELENCO" +
//                        "  WHERE _id =  " + listaPersonalizzata.getCantoPosizione(i);
//                Cursor cursor = db.rawQuery(query, null);
//                cursor.moveToFirst();
//
//                result += cursor.getString(0)
//                        + " - " + getString(R.string.page_contracted) + cursor.getInt(1);
//
//                cursor.close();
//                db.close();
                result += posizioniList.get(i).getTitolo()
                        + " - " + getString(R.string.page_contracted) + posizioniList.get(i).getPagina();
            }
            else
                result += ">> " + getString(R.string.to_be_chosen) + " <<";
            if (i < listaPersonalizzata.getNumPosizioni() - 1)
                result += "\n";
        }

        return result;

    }

    public void snackBarRimuoviCanto(View view) {
//        Snackbar.make(getActivity().findViewById(R.id.main_content), R.string.list_remove, Snackbar.LENGTH_LONG)
//                .setAction(R.string.snackbar_remove, new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        db = listaCanti.getReadableDatabase();
//                        ContentValues  values = new  ContentValues( );
//                        listaPersonalizzata.removeCanto(posizioneDaCanc);
//                        values.put("lista" , ListaPersonalizzata.serializeObject(listaPersonalizzata));
//                        db.update("LISTE_PERS", values, "_id = " + idLista, null );
//                        db.close();
//                        updateLista();
//                        mShareActionProvider.setShareIntent(getDefaultIntent());
//                    }
//                })
//                .setActionTextColor(getThemeUtils().accentColor())
//                .show();
        if (mMode != null)
            mMode.finish();
//        mActionModeView = view;
        longclickedPos = Integer.valueOf(((TextView)view.findViewById(R.id.tag)).getText().toString());
        mMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ModeCallback());
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
//            mActionModeView.setBackgroundColor(getThemeUtils().accentColorLight());
            posizioniList.get(longclickedPos).setmSelected(true);
            cantoAdapter.notifyItemChanged(longclickedPos);
            getActivity().getMenuInflater().inflate(R.menu.menu_actionmode_lists, menu);
            Drawable drawable = DrawableCompat.wrap(menu.findItem(R.id.action_remove_item).getIcon());
            DrawableCompat.setTint(drawable, getResources().getColor(R.color.icon_ative_black));
            menu.findItem(R.id.action_remove_item).setIcon(drawable);
            drawable = DrawableCompat.wrap(menu.findItem(R.id.action_switch_item).getIcon());
            DrawableCompat.setTint(drawable, getResources().getColor(R.color.icon_ative_black));
            menu.findItem(R.id.action_switch_item).setIcon(drawable);
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
            mSwhitchMode = false;
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = getActivity().getTheme();
            theme.resolveAttribute(R.attr.customSelector, typedValue, true);
//            mActionModeView.setBackgroundResource(typedValue.resourceId);
//            mActionModeView = null;
            posizioniList.get(longclickedPos).setmSelected(false);
            cantoAdapter.notifyItemChanged(longclickedPos);
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
                    mShareActionProvider.setShareIntent(getDefaultIntent());
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
                                    mShareActionProvider.setShareIntent(getDefaultIntent());
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

    private void scambioCanto(int posizioneNew) {
//        int posizioneNew = Integer.valueOf(
//                ((TextView) v.findViewById(R.id.id_da_canc)).getText().toString());
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
            mMode.finish();
            mShareActionProvider.setShareIntent(getDefaultIntent());
            Toast.makeText(getActivity()
                    , getResources().getString(R.string.switch_done)
                    , Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getActivity()
                    , getResources().getString(R.string.switch_impossible)
                    , Toast.LENGTH_SHORT).show();
        }
    }

    private void scambioConVuoto(int posizioneNew) {
//        int posizioneNew = Integer.valueOf(
//                ((TextView) v.findViewById(R.id.id_posizione)).getText().toString());
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
        mMode.finish();
        mShareActionProvider.setShareIntent(getDefaultIntent());
        Toast.makeText(getActivity()
                , getResources().getString(R.string.switch_done)
                , Toast.LENGTH_SHORT).show();
    }

}