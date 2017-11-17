package it.cammino.risuscito;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import it.cammino.risuscito.database.RisuscitoDatabase;
import it.cammino.risuscito.database.dao.ListePersDao;
import it.cammino.risuscito.database.entities.ListaPers;
import it.cammino.risuscito.dialogs.InputTextDialogFragment;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.ui.BottomSheetFabListe;
import it.cammino.risuscito.ui.ThemeableActivity;
import it.cammino.risuscito.utils.ThemeUtils;
import it.cammino.risuscito.viewmodels.CustomListsViewModel;

public class CustomLists extends Fragment
    implements InputTextDialogFragment.SimpleInputCallback, SimpleDialogFragment.SimpleCallback {

  public static final int TAG_CREA_LISTA = 111;
  public static final int TAG_MODIFICA_LISTA = 222;
  private static final String PAGE_EDITED = "pageEdited";

  @BindView(R.id.view_pager)
  ViewPager mViewPager;

  private String TAG = getClass().getCanonicalName();
  private CustomListsViewModel mCustomListsViewModel;
  private SectionsPagerAdapter mSectionsPagerAdapter;
  private String[] titoliListe;
  private int[] idListe;
  private int indexToShow;
  private boolean movePage;
  //    protected DatabaseCanti listaCanti;
  private int listaDaCanc, idDaCanc, indDaModif;
  private ListaPersonalizzata celebrazioneDaCanc;
  private String titoloDaCanc;
  private FloatingActionButton mFab;
  private TabLayout tabs;
  private BroadcastReceiver fabBRec =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          // Implement UI change code here once notification is received
          int clickedId = intent.getIntExtra(BottomSheetFabListe.DATA_ITEM_ID, 0);
          switch (clickedId) {
            case BottomSheetFabListe.CLEAN:
              new SimpleDialogFragment.Builder(
                      (AppCompatActivity) getActivity(), CustomLists.this, "RESET_LIST")
                  .title(R.string.dialog_reset_list_title)
                  .content(R.string.reset_list_question)
                  .positiveButton(android.R.string.yes)
                  .negativeButton(android.R.string.no)
                  .show();
              break;
            case BottomSheetFabListe.ADD_LIST:
              new InputTextDialogFragment.Builder(
                      (AppCompatActivity) getActivity(), CustomLists.this, "NEW_LIST")
                  .title(R.string.lista_add_desc)
                  .positiveButton(android.R.string.ok)
                  .negativeButton(android.R.string.cancel)
                  .show();
              break;
            case BottomSheetFabListe.SHARE_TEXT:
              View mView =
                  mSectionsPagerAdapter
                      .getRegisteredFragment(mViewPager.getCurrentItem())
                      .getView();
              if (mView != null) mView.findViewById(R.id.button_condividi).performClick();
              break;
            case BottomSheetFabListe.EDIT_LIST:
              Bundle bundle = new Bundle();
              bundle.putInt("idDaModif", idListe[mViewPager.getCurrentItem() - 2]);
              bundle.putBoolean("modifica", true);
              indDaModif = mViewPager.getCurrentItem();
              startActivityForResult(
                  new Intent(getActivity(), CreaListaActivity.class).putExtras(bundle),
                  TAG_MODIFICA_LISTA);
              getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold_on);
              break;
            case BottomSheetFabListe.DELETE_LIST:
              listaDaCanc = mViewPager.getCurrentItem() - 2;
              idDaCanc = idListe[listaDaCanc];
              //                    SQLiteDatabase db = listaCanti.getReadableDatabase();
              //
              //                    String query = "SELECT titolo_lista, lista"
              //                            + "  FROM LISTE_PERS"
              //                            + "  WHERE _id = " + idDaCanc;
              //                    Cursor cursor = db.rawQuery(query, null);
              //
              //                    cursor.moveToFirst();
              //                    titoloDaCanc = cursor.getString(0);
              //                    celebrazioneDaCanc = (ListaPersonalizzata)
              // ListaPersonalizzata.deserializeObject(cursor.getBlob(1));
              //                    cursor.close();
              //                    db.close();
              //
              //                    new
              // SimpleDialogFragment.Builder((AppCompatActivity)getActivity(), CustomLists.this,
              // "DELETE_LIST")
              //                            .title(R.string.action_remove_list)
              //                            .content(R.string.delete_list_dialog)
              //                            .positiveButton(android.R.string.yes)
              //                            .negativeButton(android.R.string.no)
              //                            .show();
              new Thread(
                      new Runnable() {
                        @Override
                        public void run() {
                          ListePersDao mDao =
                              RisuscitoDatabase.getInstance(getContext()).listePersDao();
                          ListaPers lista = mDao.getListById(idDaCanc);
                          titoloDaCanc = lista.titolo;
                          celebrazioneDaCanc = lista.lista;
                          new SimpleDialogFragment.Builder(
                                  (AppCompatActivity) getActivity(),
                                  CustomLists.this,
                                  "DELETE_LIST")
                              .title(R.string.action_remove_list)
                              .content(R.string.delete_list_dialog)
                              .positiveButton(android.R.string.yes)
                              .negativeButton(android.R.string.no)
                              .show();
                        }
                      })
                  .start();
              break;
            case BottomSheetFabListe.SHARE_FILE:
              mView =
                  mSectionsPagerAdapter
                      .getRegisteredFragment(mViewPager.getCurrentItem())
                      .getView();
              if (mView != null) mView.findViewById(R.id.button_invia_file).performClick();
              break;
            default:
              break;
          }
        }
      };
  private Unbinder mUnbinder;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    View rootView = inflater.inflate(R.layout.tabs_layout, container, false);
    mUnbinder = ButterKnife.bind(this, rootView);

    MainActivity mMainActivity = (MainActivity) getActivity();

    mMainActivity.setupToolbarTitle(R.string.title_activity_custom_lists);

    //        mLUtils = LUtils.getInstance(getActivity());

    // crea un istanza dell'oggetto DatabaseCanti
    //        listaCanti = new DatabaseCanti(getActivity());

    //        updateLista();

    // Create the adapter that will return a fragment for each of the three
    //        mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
    titoliListe = new String[0];
    idListe = new int[0];
    mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
    mViewPager.setAdapter(mSectionsPagerAdapter);

    if (savedInstanceState != null) {
      indDaModif = savedInstanceState.getInt(PAGE_EDITED, 0);
      movePage = true;
      indexToShow = savedInstanceState.getInt("indexToShow", 0);
    } else {
      indDaModif = 0;
      movePage = false;
      indexToShow = indDaModif;
    }

    mMainActivity.enableFab(true);
    if (!mMainActivity.isOnTablet()) mMainActivity.enableBottombar(false);

    tabs = mMainActivity.mTabLayout;
    tabs.setVisibility(View.VISIBLE);
    tabs.setupWithViewPager(mViewPager);

    mCustomListsViewModel = ViewModelProviders.of(this).get(CustomListsViewModel.class);
    populateDb();
    subscribeUiFavorites();

    getFab()
        .setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                boolean customList = mViewPager.getCurrentItem() >= 2;
                BottomSheetFabListe bottomSheetDialog = BottomSheetFabListe.newInstance(customList);
                bottomSheetDialog.show(getFragmentManager(), null);
              }
            });

    if (savedInstanceState != null) {
      Log.d(TAG, "onCreateView: RESTORING");
      idDaCanc = savedInstanceState.getInt("idDaCanc", 0);
      titoloDaCanc = savedInstanceState.getString("titoloDaCanc");
      listaDaCanc = savedInstanceState.getInt("listaDaCanc", 0);
      celebrazioneDaCanc =
          (ListaPersonalizzata) savedInstanceState.getSerializable("celebrazioneDaCanc");
      InputTextDialogFragment iFragment =
          InputTextDialogFragment.findVisible((AppCompatActivity) getActivity(), "NEW_LIST");
      if (iFragment != null) iFragment.setmCallback(CustomLists.this);
      SimpleDialogFragment sFragment =
          SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "RESET_LIST");
      if (sFragment != null) sFragment.setmCallback(CustomLists.this);
      sFragment =
          SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "DELETE_LIST");
      if (sFragment != null) sFragment.setmCallback(CustomLists.this);
    }

    getActivity().registerReceiver(fabBRec, new IntentFilter(BottomSheetFabListe.CHOOSE_DONE));

    SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    Log.d(
        TAG,
        "onCreate - INTRO_CUSTOMLISTS: "
            + mSharedPrefs.getBoolean(Utility.INTRO_CUSTOMLISTS, false));
    if (!mSharedPrefs.getBoolean(Utility.INTRO_CUSTOMLISTS, false)) playIntro();

    return rootView;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mUnbinder.unbind();
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    IconicsMenuInflaterUtil.inflate(getActivity().getMenuInflater(), getActivity(), R.menu.help_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
//    getActivity().getMenuInflater().inflate(R.menu.help_menu, menu);
//    menu.findItem(R.id.action_help)
//        .setIcon(
//            new IconicsDrawable(getActivity(), CommunityMaterial.Icon.cmd_help_circle)
//                .sizeDp(24)
//                .paddingDp(2)
//                .color(Color.WHITE));
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_help:
        playIntro();
        return true;
    }
    return false;
  }

  /** @param outState Bundle in which to place your saved state. */
  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(PAGE_EDITED, indDaModif);
    outState.putString("titoloDaCanc", titoloDaCanc);
    outState.putInt("idDaCanc", idDaCanc);
    outState.putSerializable("celebrazioneDaCanc", celebrazioneDaCanc);
    outState.putInt("listaDaCanc", listaDaCanc);
    outState.putInt("indexToShow", mViewPager.getCurrentItem());
  }

  @Override
  public void onDestroy() {
    //        if (listaCanti != null)
    //            listaCanti.close();
    getActivity().unregisterReceiver(fabBRec);
    super.onDestroy();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    //        Log.i(TAG, "requestCode: " + requestCode);
    super.onActivityResult(requestCode, resultCode, data);
    if ((requestCode == TAG_CREA_LISTA || requestCode == TAG_MODIFICA_LISTA)
        && resultCode == Activity.RESULT_OK) {
      indexToShow = indDaModif;
      movePage = true;
    }
    //              updateLista();
    //              mSectionsPagerAdapter.notifyDataSetChanged();
    //              tabs.setupWithViewPager(mViewPager);
    //              Handler myHandler = new Handler();
    //              final Runnable mMyRunnable2 = new Runnable() {
    //                  @SuppressWarnings("ConstantConditions")
    //                  @Override
    //                  public void run() {
    //                      tabs.getTabAt(indDaModif).select();
    //                  }
    //              };
    //              myHandler.postDelayed(mMyRunnable2, 200);
    //          }
    //          for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
    //              Fragment fragment = mSectionsPagerAdapter.getRegisteredFragment(i);
    //              if (fragment != null && fragment.isVisible())
    //                  fragment.onActivityResult(requestCode, resultCode, data);
    //          }
    //          super.onActivityResult(requestCode, resultCode, data);
  }

  public FloatingActionButton getFab() {
    if (mFab == null) {
      mFab = getActivity().findViewById(R.id.fab_pager);
      mFab.setVisibility(View.VISIBLE);
      IconicsDrawable icon =
          new IconicsDrawable(getActivity())
              .icon(CommunityMaterial.Icon.cmd_plus)
              .color(Color.WHITE)
              .sizeDp(24)
              .paddingDp(4);
      mFab.setImageDrawable(icon);
    }
    return mFab;
  }

  //    private void updateLista() {
  //
  //        SQLiteDatabase db = listaCanti.getReadableDatabase();
  //
  //        String query = "SELECT titolo_lista, lista, _id"
  //                + "  FROM LISTE_PERS A"
  //                + "  ORDER BY _id ASC";
  //        Cursor cursor = db.rawQuery(query, null);
  //
  //        int total = cursor.getCount();
  ////	    Log.i("RISULTATI", total+"");
  //
  //        titoliListe = new String[total];
  //        idListe = new int[total];
  //
  //        cursor.moveToFirst();
  //        for (int i = 0; i < total; i++) {
  ////    		Log.i("LISTA IN POS[" + i + "]:", cursor.getString(0));
  //            titoliListe[i] =  cursor.getString(0);
  //            idListe[i] = cursor.getInt(2);
  //            cursor.moveToNext();
  //        }
  //
  //        cursor.close();
  //        db.close();
  //
  //    }

  private ThemeUtils getThemeUtils() {
    return ((MainActivity) getActivity()).getThemeUtils();
  }

  @Override
  public void onPositive(@NonNull String tag, @NonNull MaterialDialog dialog) {
    Log.d(TAG, "onPositive: " + tag);
    switch (tag) {
      case "NEW_LIST":
        Bundle bundle = new Bundle();
        EditText mEditText = dialog.getInputEditText();
        bundle.putString(
            "titolo", mEditText != null ? dialog.getInputEditText().getText().toString() : "NULL");
        bundle.putBoolean("modifica", false);
        indDaModif = 2 + idListe.length;
        startActivityForResult(
            new Intent(getActivity(), CreaListaActivity.class).putExtras(bundle), TAG_CREA_LISTA);
        getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold_on);
        break;
    }
  }

  @Override
  public void onNegative(@NonNull String tag, @NonNull MaterialDialog dialog) {}

  @Override
  public void onNeutral(@NonNull String tag, @NonNull MaterialDialog dialog) {}

  @Override
  public void onPositive(@NonNull String tag) {
    Log.d(TAG, "onPositive: " + tag);
    switch (tag) {
      case "RESET_LIST":
        View mView =
            mSectionsPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem()).getView();
        if (mView != null) mView.findViewById(R.id.button_pulisci).performClick();
        break;
      case "DELETE_LIST":
        //                SQLiteDatabase db = listaCanti.getReadableDatabase();
        //                db.delete("LISTE_PERS", "_id = " + idDaCanc, null);
        //                db.close();

        //                updateLista();
        //                mSectionsPagerAdapter.notifyDataSetChanged();
        //                tabs.setupWithViewPager(mViewPager);
        //                Handler myHandler = new Handler();
        //                final Runnable mMyRunnable2 = new Runnable() {
        //                    @SuppressWarnings("ConstantConditions")
        //                    @Override
        //                    public void run() {
        //                        tabs.getTabAt(0).select();
        //                    }
        //                };
        //                myHandler.postDelayed(mMyRunnable2, 200);
        //                Snackbar.make(getActivity().findViewById(R.id.main_content),
        // getString(R.string.list_removed) + titoloDaCanc + "'!", Snackbar.LENGTH_LONG)
        //                        .setAction(android.R.string.cancel, new View.OnClickListener() {
        //                            @Override
        //                            public void onClick(View view) {
        ////					    	Log.i("INDICE DA CANC", listaDaCanc+" ");
        //                                SQLiteDatabase db = listaCanti.getReadableDatabase();
        //                                ContentValues values = new ContentValues();
        //                                values.put("_id", idDaCanc);
        //                                values.put("titolo_lista", titoloDaCanc);
        //                                values.put("lista",
        // ListaPersonalizzata.serializeObject(celebrazioneDaCanc));
        //                                db.insert("LISTE_PERS", "", values);
        //                                db.close();
        //
        //                                updateLista();
        //                                mSectionsPagerAdapter.notifyDataSetChanged();
        //                                tabs.setupWithViewPager(mViewPager);
        //                                Handler myHandler = new Handler();
        //                                final Runnable mMyRunnable2 = new Runnable() {
        //                                    @Override
        //                                    public void run() {
        //                                        mViewPager.setCurrentItem(listaDaCanc + 2, false);
        //                                    }
        //                                };
        //                                myHandler.postDelayed(mMyRunnable2, 200);
        //                            }
        //                        })
        //                        .setActionTextColor(getThemeUtils().accentColor())
        //                        .show();
        new Thread(
                new Runnable() {
                  @Override
                  public void run() {
                    ListePersDao mDao = RisuscitoDatabase.getInstance(getContext()).listePersDao();
                    ListaPers listToDelete = new ListaPers();
                    listToDelete.id = idDaCanc;
                    mDao.deleteList(listToDelete);
                    indexToShow = 0;
                    movePage = true;
                    Snackbar.make(
                            getActivity().findViewById(R.id.main_content),
                            getString(R.string.list_removed) + titoloDaCanc + "'!",
                            Snackbar.LENGTH_LONG)
                        .setAction(
                            getString(android.R.string.cancel).toUpperCase(),
                            new View.OnClickListener() {
                              @Override
                              public void onClick(View view) {
                                indexToShow = listaDaCanc + 2;
                                movePage = true;
                                new Thread(
                                        new Runnable() {
                                          @Override
                                          public void run() {
                                            ListePersDao mDao =
                                                RisuscitoDatabase.getInstance(getContext())
                                                    .listePersDao();
                                            ListaPers listaToRestore = new ListaPers();
                                            listaToRestore.id = idDaCanc;
                                            listaToRestore.titolo = titoloDaCanc;
                                            listaToRestore.lista = celebrazioneDaCanc;
                                            mDao.insertLista(listaToRestore);
                                          }
                                        })
                                    .start();
                              }
                            })
                        .setActionTextColor(getThemeUtils().accentColor())
                        .show();
                  }
                })
            .start();
        break;
    }
  }

  @Override
  public void onNegative(@NonNull String tag) {}

  @Override
  public void onNeutral(@NonNull String tag) {}

  private void playIntro() {
    getFab().show();
    Drawable doneDrawable =
        new IconicsDrawable(getActivity(), CommunityMaterial.Icon.cmd_check)
            .sizeDp(24)
            .paddingDp(2);
    new TapTargetSequence(getActivity())
        .continueOnCancel(true)
        .targets(
            TapTarget.forView(
                    getFab(),
                    getString(R.string.showcase_listepers_title),
                    getString(R.string.showcase_listepers_desc1))
                .outerCircleColorInt(
                    getThemeUtils().primaryColor()) // Specify a color for the outer circle
                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                .textTypeface(
                    Typeface.createFromAsset(
                        getResources().getAssets(),
                        "fonts/Roboto-Regular.ttf")) // Specify a typeface for the text
                .titleTextColor(R.color.primary_text_default_material_dark)
                .textColor(R.color.secondary_text_default_material_dark)
                .descriptionTextSize(15)
                .tintTarget(false) // Whether to tint the target view's color
            ,
            TapTarget.forView(
                    getFab(),
                    getString(R.string.showcase_listepers_title),
                    getString(R.string.showcase_listepers_desc3))
                .outerCircleColorInt(
                    getThemeUtils().primaryColor()) // Specify a color for the outer circle
                .targetCircleColorInt(Color.WHITE) // Specify a color for the target circle
                .icon(doneDrawable)
                .textTypeface(
                    Typeface.createFromAsset(
                        getResources().getAssets(),
                        "fonts/Roboto-Regular.ttf")) // Specify a typeface for the text
                .titleTextColor(R.color.primary_text_default_material_dark)
                .textColor(R.color.secondary_text_default_material_dark))
        .listener(
            new TapTargetSequence
                .Listener() { // The listener can listen for regular clicks, long clicks or cancels
              @Override
              public void onSequenceFinish() {
                Log.d(TAG, "onSequenceFinish: ");
                SharedPreferences.Editor prefEditor =
                    PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                prefEditor.putBoolean(Utility.INTRO_CUSTOMLISTS, true);
                prefEditor.apply();
              }

              @Override
              public void onSequenceStep(TapTarget tapTarget, boolean b) {}

              @Override
              public void onSequenceCanceled(TapTarget tapTarget) {
                Log.d(TAG, "onSequenceCanceled: ");
                SharedPreferences.Editor prefEditor =
                    PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                prefEditor.putBoolean(Utility.INTRO_CUSTOMLISTS, true);
                prefEditor.apply();
              }
            })
        .start();
  }

  private void populateDb() {
    mCustomListsViewModel.createDb();
  }

  private void subscribeUiFavorites() {
    mCustomListsViewModel
        .getCustomListResult()
        .observe(
            this,
            new Observer<List<ListaPers>>() {
              @Override
              public void onChanged(@Nullable List<ListaPers> list) {
                titoliListe = new String[list.size()];
                idListe = new int[list.size()];

                for (int i = 0; i < list.size(); i++) {
                  titoliListe[i] = list.get(i).titolo;
                  idListe[i] = list.get(i).id;
                }
                mSectionsPagerAdapter.notifyDataSetChanged();
                tabs.setupWithViewPager(mViewPager);
                if (movePage) {
                  Handler myHandler = new Handler();
                  final Runnable mMyRunnable2 =
                      new Runnable() {
                        @Override
                        public void run() {
                          tabs.getTabAt(indexToShow).select();
                          indexToShow = 0;
                          movePage = false;
                        }
                      };
                  myHandler.postDelayed(mMyRunnable2, 200);
                }
              }
            });
  }

  private class SectionsPagerAdapter extends FragmentStatePagerAdapter {
    SparseArray<Fragment> registeredFragments = new SparseArray<>();

    SectionsPagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int position) {
      switch (position) {
        case 0:
          return new CantiParolaFragment();
        case 1:
          return new CantiEucarestiaFragment();
        default:
          Bundle bundle = new Bundle();
          bundle.putInt("idLista", idListe[position - 2]);
          ListaPersonalizzataFragment listaPersFrag = new ListaPersonalizzataFragment();
          listaPersFrag.setArguments(bundle);
          return listaPersFrag;
      }
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
      Fragment fragment = (Fragment) super.instantiateItem(container, position);
      registeredFragments.put(position, fragment);
      return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
      registeredFragments.remove(position);
      super.destroyItem(container, position, object);
    }

    Fragment getRegisteredFragment(int position) {
      return registeredFragments.get(position);
    }

    @Override
    public int getCount() {
      return 2 + titoliListe.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      Locale l =
          ThemeableActivity.getSystemLocalWrapper(getActivity().getResources().getConfiguration());
      switch (position) {
        case 0:
          return getString(R.string.title_activity_canti_parola).toUpperCase(l);
        case 1:
          return getString(R.string.title_activity_canti_eucarestia).toUpperCase(l);
        default:
          return titoliListe[position - 2].toUpperCase(l);
      }
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
      return PagerAdapter.POSITION_NONE;
    }
  }
}
