package it.cammino.risuscito;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.internal.widget.TintEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.alertdialogpro.AlertDialogPro;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.util.Locale;

public class CustomLists extends Fragment  {

	private SectionsPagerAdapter mSectionsPagerAdapter;
	private String[] titoliListe;
	private int[] idListe;
	private DatabaseCanti listaCanti;
	private int listaDaCanc;
	private int prevOrientation;
	private ViewPager mViewPager;
	SlidingTabLayout mSlidingTabLayout = null;
	
	private AlertDialogPro dialog;
    private TintEditText titleInput;
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.activity_custom_lists, container, false);
		((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_activity_custom_lists);
		
		//crea un istanza dell'oggetto DatabaseCanti
		listaCanti = new DatabaseCanti(getActivity());
		
		updateLista();
		
		// Create the adapter that will return a fragment for each of the three
		mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
		mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
	    mViewPager.setAdapter(mSectionsPagerAdapter);
	    
        mSlidingTabLayout = (SlidingTabLayout) rootView.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
	    
        Resources res = getResources();
        mSlidingTabLayout.setSelectedIndicatorColors(res.getColor(R.color.theme_accent));
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);
        
	    setHasOptionsMenu(true);
	    
        return rootView;
	}

    @Override
    public void onResume() {
    	super.onResume();
    	updateLista();
    	mSectionsPagerAdapter.notifyDataSetChanged();
    	mSlidingTabLayout.setViewPager(mViewPager);
    }
    
	@Override
	public void onDestroy() {
		if (listaCanti != null)
			listaCanti.close();
		super.onDestroy();
	}
    
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		getActivity().getMenuInflater().inflate(R.menu.custom_list, menu);
	    super.onCreateOptionsMenu(menu, inflater);
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add_list:
            prevOrientation = getActivity().getRequestedOrientation();
            Utility.blockOrientation(getActivity());
	        AlertDialogPro.Builder builder = new AlertDialogPro.Builder(getActivity());
        	dialog = builder.setTitle(R.string.lista_add_desc)
        			.setView(getActivity().getLayoutInflater().inflate(R.layout.dialog_customview, null))
                    .setPositiveButton(R.string.dialog_chiudi, new ButtonClickedListener(Utility.ADD_LIST_OK))
                    .setNegativeButton(R.string.cancel, new ButtonClickedListener(Utility.DISMISS))
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
        	dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        	titleInput = (TintEditText)dialog.findViewById(R.id.list_title);
        	titleInput.addTextChangedListener(new TextWatcher() {
		        @Override
		        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		
		        @Override
		        public void onTextChanged(CharSequence s, int start, int before, int count) {
		        	dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(s.toString().trim().length() > 0);
		        }
		
		        @Override
		        public void afterTextChanged(Editable s) {}
		    });
        	dialog.setCancelable(false);
			return true;
		case R.id.action_edit_list:
			Bundle bundle = new Bundle();
			bundle.putInt("idDaModif", idListe[mViewPager.getCurrentItem() - 2]);
			bundle.putBoolean("modifica", true);
			startActivity(new Intent(getActivity(), CreaListaActivity.class).putExtras(bundle));
			getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold_on);
			return true;
		case R.id.action_remove_list:
			listaDaCanc = mViewPager.getCurrentItem() - 2;
            SnackbarManager.show(
                    Snackbar.with(getActivity())
                            .text(getString(R.string.snackbar_list_delete) + titoliListe[listaDaCanc] + "'?")
                            .actionLabel(getString(R.string.snackbar_remove))
                            .actionListener(new ActionClickListener() {
                                @Override
                                public void onActionClicked(Snackbar snackbar) {
                                    SQLiteDatabase db = listaCanti.getReadableDatabase();

//					    	Log.i("INDICE DA CANC", listaDaCanc+" ");

                                    String sql = "DELETE FROM LISTE_PERS"
                                            + " WHERE _id = " + idListe[listaDaCanc];
                                    db.execSQL(sql);
                                    db.close();

                                    updateLista();
                                    mSectionsPagerAdapter.notifyDataSetChanged();
                                    mSlidingTabLayout.setViewPager(mViewPager);
                                }
                            })
                            .actionColor(getResources().getColor(R.color.theme_accent))
                    , getActivity());
			return true;
		}
		return false;
	}

    private void updateLista() {
		
    	SQLiteDatabase db = listaCanti.getReadableDatabase();
    	
	    String query = "SELECT titolo_lista, lista, _id"
	      		+ "  FROM LISTE_PERS A"
	      		+ "  ORDER BY _id ASC";
	    Cursor cursor = db.rawQuery(query, null);
	     
	    int total = cursor.getCount();
//	    Log.i("RISULTATI", total+"");
	    
	    titoliListe = new String[total];
	    idListe = new int[total];

	    cursor.moveToFirst();
	    for (int i = 0; i < total; i++) {
//    		Log.i("LISTA IN POS[" + i + "]:", cursor.getString(0));
	    	titoliListe[i] =  cursor.getString(0);
    		idListe[i] = cursor.getInt(2);
	    	cursor.moveToNext();
	    }
	    
	    cursor.close();
	    db.close();
    
    }
    
	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
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
            	Bundle bundle=new Bundle();
//            	Log.i("INVIO", "position = " + position);
//            	Log.i("INVIO", "idLista = " + idListe[position - 2]);
            	bundle.putInt("position", position);
            	bundle.putInt("idLista", idListe[position - 2]);
            	ListaPersonalizzataFragment listaPersFrag = new ListaPersonalizzataFragment();
            	listaPersFrag.setArguments(bundle);
            	return listaPersFrag;
            }
		}

		@Override
		public int getCount() {
			return 2 + titoliListe.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
					return getString(R.string.title_activity_canti_parola).toUpperCase(l);
				else
					return getString(R.string.title_activity_canti_parola);
			case 1:
				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
					return getString(R.string.title_activity_canti_eucarestia).toUpperCase(l);
				else
					return getString(R.string.title_activity_canti_eucarestia);
			default:
				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
					return titoliListe[position - 2].toUpperCase(l);
				else
					return titoliListe[position - 2];
			}
		}
		
	    @Override
	    public int getItemPosition(Object object){
	        return PagerAdapter.POSITION_NONE;
	    }
	}
	
    private class ButtonClickedListener implements DialogInterface.OnClickListener {
        private int clickedCode;

        public ButtonClickedListener(int code) {
        	clickedCode = code;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (clickedCode) {
			case Utility.DISMISS:
				getActivity().setRequestedOrientation(prevOrientation);
				break;
			case Utility.ADD_LIST_OK:
				getActivity().setRequestedOrientation(prevOrientation);
        		Bundle bundle = new Bundle();
        		bundle.putString("titolo", titleInput.getText().toString());
        		bundle.putBoolean("modifica", false);
        		startActivity(new Intent(getActivity(), CreaListaActivity.class).putExtras(bundle));
        		getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold_on);
				break;
			default:
				getActivity().setRequestedOrientation(prevOrientation);
				break;
			}
        }
    }

}
