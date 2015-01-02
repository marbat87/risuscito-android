package it.cammino.risuscito;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.view.KeyEvent;
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

import com.alertdialogpro.AlertDialogPro;
import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.ObservableScrollView;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.util.Locale;

public class CantiEucarestiaFragment extends Fragment {

	private int posizioneDaCanc;
	private String titoloDaCanc;
	private View rootView;
	private ShareActionProvider mShareActionProvider;
	private DatabaseCanti listaCanti;
	private SQLiteDatabase db;
    private int prevOrientation;
	
	private LUtils mLUtils;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(
		R.layout.activity_canti_eucarestia, container, false);
		
		//crea un istanza dell'oggetto DatabaseCanti
		listaCanti = new DatabaseCanti(getActivity());
		
		FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_eucarestia);
		fab.attachToScrollView((ObservableScrollView) rootView.findViewById(R.id.eucarestiaScrollView));
        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                prevOrientation = getActivity().getRequestedOrientation();
                Utility.blockOrientation(getActivity());
                AlertDialogPro.Builder builder = new AlertDialogPro.Builder(getActivity());
                AlertDialogPro dialog = builder.setTitle(R.string.dialog_reset_list_title)
                        .setMessage(R.string.reset_list_question)
                        .setPositiveButton(R.string.confirm, new ButtonClickedListener(Utility.EUCAR_RESET_OK))
                        .setNegativeButton(R.string.dismiss, new ButtonClickedListener(Utility.DISMISS))
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
			
		updateLista();
		
		setHasOptionsMenu(true);
		
		mLUtils = LUtils.getInstance(getActivity());
		
		return rootView;
	}
		   
    @Override
    public void onResume() {
    	super.onResume();
		updateLista();
    }
    
	@Override
	public void onDestroy() {
		if (listaCanti != null)
			listaCanti.close();
		super.onDestroy();
	}
	
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    MenuItem shareItem = menu.findItem(R.id.action_share);
	    mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
	    ViewPager tempPager = (ViewPager) getActivity().findViewById(R.id.view_pager);
	    if (mShareActionProvider != null && tempPager.getCurrentItem() == 1)
	    	mShareActionProvider.setShareIntent(getDefaultIntent());
	    super.onCreateOptionsMenu(menu, inflater);
	}
	
	private Intent getDefaultIntent() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_TEXT, getTitlesList());
		intent.setType("text/plain");
		return intent;
	}
	
    private void startSubActivity(Bundle bundle) {
    	Intent intent = new Intent(getActivity(), GeneralInsertSearch.class);
    	intent.putExtras(bundle);
    	startActivity(intent);
    	getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.hold_on);
   	}
    
    private void openPagina(View v) {
    	// recupera il titolo della voce cliccata
		String cantoCliccato = ((TextView) v).getText().toString();
		cantoCliccato = Utility.duplicaApostrofi(cantoCliccato);
        		
		// crea un manipolatore per il DB in modalit� READ
		db = listaCanti.getReadableDatabase();
	    
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
	    
    	Intent intent = new Intent(getActivity(), PaginaRenderActivity.class);
    	intent.putExtras(bundle);
    	mLUtils.startActivityWithTransition(intent, v, Utility.TRANS_PAGINA_RENDER);
    }
    
    private void updateLista() {
        	
		String[] titoloCanto = getTitoliFromPosition(1);
		
		if (titoloCanto.length == 0) {
			rootView.findViewById(R.id.addCantoIniziale1).setVisibility(View.VISIBLE);
			rootView.findViewById(R.id.cantoIniziale1).setVisibility(View.GONE);
			
			rootView.findViewById(R.id.addCantoIniziale1).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
				    Bundle bundle = new Bundle();
				    bundle.putInt("fromAdd", 1);
				    bundle.putInt("idLista", 2);
				    bundle.putInt("position", 1);
				    startSubActivity(bundle);
				}
			});
		}
		else {
			TextView temp = (TextView) rootView.findViewById(R.id.cantoIniziale1);
			rootView.findViewById(R.id.addCantoIniziale1).setVisibility(View.GONE);
			temp.setVisibility(View.VISIBLE);
			temp.setText(titoloCanto[0].substring(7));
			(rootView.findViewById(R.id.cantoIniziale1Container))
			.setBackgroundColor(Color.parseColor(titoloCanto[0].substring(0,7)));
			
			rootView.findViewById(R.id.cantoIniziale1).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					openPagina(v);
				}
			});
			
			// setta l'azione tenendo premuto sul canto
	   		rootView.findViewById(R.id.cantoIniziale1).setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					posizioneDaCanc = 1;
					titoloDaCanc = Utility.duplicaApostrofi(((TextView) view).getText().toString());
					snackBarRimuoviCanto();
					return true;
				}
			});
		}
		
		SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(getActivity());
		boolean showSeconda = pref.getBoolean(Utility.SHOW_SECONDA, false);
		
		if (showSeconda) {
			
			rootView.findViewById(R.id.groupCantoSeconda).setVisibility(View.VISIBLE);
			
			titoloCanto = getTitoliFromPosition(6);
			
			if (titoloCanto.length == 0) {
				rootView.findViewById(R.id.addCantoSeconda).setVisibility(View.VISIBLE);
				rootView.findViewById(R.id.cantoSeconda).setVisibility(View.GONE);
				
				rootView.findViewById(R.id.addCantoSeconda).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
					    Bundle bundle = new Bundle();
					    bundle.putInt("fromAdd", 1);
					    bundle.putInt("idLista", 2);
					    bundle.putInt("position", 6);
					    startSubActivity(bundle);
					}
				});
			}
			else {
				TextView temp = (TextView) rootView.findViewById(R.id.cantoSeconda);
				rootView.findViewById(R.id.addCantoSeconda).setVisibility(View.GONE);
				temp.setVisibility(View.VISIBLE);
				temp.setText(titoloCanto[0].substring(7));
				(rootView.findViewById(R.id.cantoSecondaContainer))
				.setBackgroundColor(Color.parseColor(titoloCanto[0].substring(0,7)));
				
				rootView.findViewById(R.id.cantoSeconda).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						openPagina(v);
					}
				});
				
				// setta l'azione tenendo premuto sul canto
		   		rootView.findViewById(R.id.cantoSeconda).setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View view) {
						posizioneDaCanc = 6;
						titoloDaCanc = Utility.duplicaApostrofi(((TextView) view).getText().toString());
						snackBarRimuoviCanto();
						return true;
					}
				});
			}
    	}
		else
			rootView.findViewById(R.id.groupCantoSeconda).setVisibility(View.GONE);
		
		titoloCanto = getTitoliFromPosition(2);
		
		if (titoloCanto.length == 0) {
			rootView.findViewById(R.id.addCantoPace).setVisibility(View.VISIBLE);
			rootView.findViewById(R.id.cantoPace).setVisibility(View.GONE);
			
			rootView.findViewById(R.id.addCantoPace).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
				    Bundle bundle = new Bundle();
				    bundle.putInt("fromAdd", 1);
				    bundle.putInt("idLista", 2);
				    bundle.putInt("position", 2);
				    startSubActivity(bundle);
				}
			});
		}
		else {
			TextView temp = (TextView) rootView.findViewById(R.id.cantoPace);
			rootView.findViewById(R.id.addCantoPace).setVisibility(View.GONE);
			temp.setVisibility(View.VISIBLE);
			temp.setText(titoloCanto[0].substring(7));
			(rootView.findViewById(R.id.cantoPaceContainer))
			.setBackgroundColor(Color.parseColor(titoloCanto[0].substring(0, 7)));
			
			rootView.findViewById(R.id.cantoPace).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					openPagina(v);
				}
			});
			
			// setta l'azione tenendo premuto sul canto
	   		rootView.findViewById(R.id.cantoPace).setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {		
					posizioneDaCanc = 2;
					titoloDaCanc = Utility.duplicaApostrofi(((TextView) view).getText().toString());
					snackBarRimuoviCanto();
					return true;
				}
			});
		}

        String[] titoliCanti = getTitoliFromPosition(3);
		
		LinearLayout lv = (LinearLayout) rootView.findViewById(R.id.cantiPaneList);
		lv.removeAllViews();
		
		LayoutInflater inflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (String titoloTemp: titoliCanti) {
//		for (int current = 0; current < titoliCanti.length; current++) {
			View view = inflater.inflate(R.layout.canto_added, lv, false);

		   //initialize the view
	   		((TextView) view.findViewById(R.id.canto))
//	   			.setText(titoliCanti[current].substring(7));
	   			.setText(titoloTemp.substring(7));

//	   		String colore = titoliCanti[current].substring(0, 7);
	   		String colore = titoloTemp.substring(0, 7);
	   		view.findViewById(R.id.canto_container).
	   				setBackgroundColor(Color.parseColor(colore));		   
		   
	   		view.findViewById(R.id.canto).setOnClickListener(new OnClickListener() {
			      @Override
			      public void onClick(View v) {
			    	  openPagina(v.findViewById(R.id.canto));
			      }
	   		});
		   		
			// setta l'azione tenendo premuto sul canto
	   		view.findViewById(R.id.canto).setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					posizioneDaCanc = 3;
					titoloDaCanc = Utility.duplicaApostrofi(((TextView) view).getText().toString());
					snackBarRimuoviCanto();
					return true;
				}
			});
	   		
	   		lv.addView(view);
		}
		
		titoliCanti = getTitoliFromPosition(4);
		
		lv = (LinearLayout) rootView.findViewById(R.id.cantiVinoList);
		lv.removeAllViews();
		
		inflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

//		for (int current = 0; current < titoliCanti.length; current++) {
        for (String titoloTemp: titoliCanti) {
			View view = inflater.inflate(R.layout.canto_added, lv, false);

			//initialize the view	   		
	   		((TextView) view.findViewById(R.id.canto))
//	   			.setText(titoliCanti[current].substring(7));
	   			.setText(titoloTemp.substring(7));

//	   		String colore = titoliCanti[current].substring(0, 7);
	   		String colore = titoloTemp.substring(0, 7);
	   		view.findViewById(R.id.canto_container).
   					setBackgroundColor(Color.parseColor(colore));
	   		
	   		// setta l'azione al click sul canto
	   		view.findViewById(R.id.canto).setOnClickListener(new OnClickListener() {
		      @Override
		      public void onClick(View v) {
		    	  openPagina(v.findViewById(R.id.canto));
		      }
	   		});
	   		
			// setta l'azione tenendo premuto sul canto
	   		view.findViewById(R.id.canto).setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					posizioneDaCanc = 4;
					titoloDaCanc = Utility.duplicaApostrofi(((TextView) view).getText().toString());
					snackBarRimuoviCanto();
					return true;	
				}
			});
	   		
		   lv.addView(view);
		}
		
		titoloCanto = getTitoliFromPosition(5);
		
		if (titoloCanto.length == 0) {
			rootView.findViewById(R.id.addCantoFinale1).setVisibility(View.VISIBLE);
			rootView.findViewById(R.id.cantoFinale1).setVisibility(View.GONE);
			
			rootView.findViewById(R.id.addCantoFinale1).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
				    Bundle bundle = new Bundle();
				    bundle.putInt("fromAdd", 1);
				    bundle.putInt("idLista", 2);
				    bundle.putInt("position", 5);
				    startSubActivity(bundle);
				}
			});
		}
		else {
			TextView temp = (TextView) rootView.findViewById(R.id.cantoFinale1);
			rootView.findViewById(R.id.addCantoFinale1).setVisibility(View.GONE);
			temp.setVisibility(View.VISIBLE);
			temp.setText(titoloCanto[0].substring(7));
			(rootView.findViewById(R.id.cantoFinale1Container))
			.setBackgroundColor(Color.parseColor(titoloCanto[0].substring(0,7)));
			
			rootView.findViewById(R.id.cantoFinale1).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					openPagina(v);
				}
			});
			
			// setta l'azione tenendo premuto sul canto
	   		rootView.findViewById(R.id.cantoFinale1).setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					posizioneDaCanc = 5;
					titoloDaCanc = Utility.duplicaApostrofi(((TextView) view).getText().toString());
					snackBarRimuoviCanto();
					return true;
				}
			});
		}
		
		rootView.findViewById(R.id.addCantoPane).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			    Bundle bundle = new Bundle();
			    bundle.putInt("fromAdd", 1);
			    bundle.putInt("idLista", 2);
			    bundle.putInt("position", 3);
			    startSubActivity(bundle);
			}
		});
		
		rootView.findViewById(R.id.addCantoVino).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			    Bundle bundle = new Bundle();
			    bundle.putInt("fromAdd", 1);
			    bundle.putInt("idLista", 2);
			    bundle.putInt("position", 4);
			    startSubActivity(bundle);
			}
		});
    
	}
    
    private String getTitlesList() {
    	
    	Locale l = Locale.getDefault();
    	String result = "";
    	String[] temp;
    	
    	//titolo
    	result +=  "-- CELEBRAZIONE DELL\'EUCARESTIA --\n";
    	
    	//canto iniziale
    	temp = getTitoloToSendFromPosition(1);
    	
    	result += getResources().getString(R.string.canto_iniziale).toUpperCase(l);
    	result += "\n";
    	
    	if (temp[0] == null || temp[0].equalsIgnoreCase(""))
    		result += ">> da scegliere <<";
    	else
    		result += temp[0];
    	
    	result += "\n";
    	
    	//deve essere messa anche la seconda lettura? legge le impostazioni
		SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(getActivity());
		boolean showSeconda = pref.getBoolean(Utility.SHOW_SECONDA, false);
    	
		if (showSeconda) {
    	//canto alla seconda lettura
	    	temp = getTitoloToSendFromPosition(6);
	    	
	    	result += getResources().getString(R.string.seconda_lettura).toUpperCase(l);
	    	result += "\n";
	    	
	    	if (temp[0] == null || temp[0].equalsIgnoreCase(""))
	    		result += ">> da scegliere <<";
	    	else
	    		result += temp[0];
	    	
	    	result += "\n";
		}
//		else
//			Log.i("SECONDA LETTURA", "IGNORATA");
		
    	//canto alla pace
    	temp = getTitoloToSendFromPosition(2);
    	
    	result += getResources().getString(R.string.canto_pace).toUpperCase(l);
    	result += "\n";
    	
    	if (temp[0] == null || temp[0].equalsIgnoreCase(""))
    		result += ">> da scegliere <<";
    	else
    		result += temp[0];
    	
    	result += "\n";
    	
    	//canti al pane
    	temp = getTitoloToSendFromPosition(3);
    	
    	result += getResources().getString(R.string.canto_pane).toUpperCase(l);
    	result += "\n";
    	
	    if (temp[0] == null || temp[0].equalsIgnoreCase("")) {
			result += ">> da scegliere <<";
			result += "\n";
	    }
		else {
//	    	for (int i = 0; i < temp.length; i++) {
            for (String tempTitle: temp) {
//		    	if (temp[i] != null && !temp[i].equalsIgnoreCase("")) {
//		    		result += temp[i];
                if (tempTitle != null && !tempTitle.equalsIgnoreCase("")) {
		    		result += tempTitle;
		    		result += "\n";
		    	}
		    	else
		    		break;
	    	}
		}

        //canti al vino
        temp = getTitoloToSendFromPosition(4);

        result += getResources().getString(R.string.canto_vino).toUpperCase(l);
        result += "\n";

        if (temp[0] == null || temp[0].equalsIgnoreCase("")) {
            result += ">> da scegliere <<";
            result += "\n";
        }
        else {
//	    	for (int i = 0; i < temp.length; i++) {
            for (String tempTitle: temp) {
//		    	if (temp[i] != null && !temp[i].equalsIgnoreCase("")) {
//		    		result += temp[i];
                if (tempTitle != null && !tempTitle.equalsIgnoreCase("")) {
                    result += tempTitle;
                    result += "\n";
                }
                else
                    break;
            }
        }
    	
    	//canto finale
    	temp = getTitoloToSendFromPosition(5);
    	
    	result += getResources().getString(R.string.canto_fine).toUpperCase(l);
    	result += "\n";
    	
    	if (temp[0] == null || temp[0].equalsIgnoreCase(""))
    		result += ">> da scegliere <<";
    	else
    		result += temp[0];	    	
    	    	
    	return result;
    	
    }
    
    private String[] getTitoliFromPosition(int position) {
		
    	db = listaCanti.getReadableDatabase();
    	
	    String query = "SELECT B.titolo, color" +
	      		"  FROM CUST_LISTS A" +
	      		"  	   , ELENCO B" +
	      		"  WHERE A._id = 2" +
	      		"  AND   A.position = " + position + 
	      		"  AND   A.id_canto = B._id" +
	      		"  ORDER BY A.timestamp ASC";
	    Cursor cursor = db.rawQuery(query, null);
	     
	    int total = cursor.getCount();
	    
	    String[] result = new String[total];   

	    cursor.moveToFirst();
	    for (int i = 0; i < total; i++) {
    		result[i] =  cursor.getString(1) + cursor.getString(0);
    		cursor.moveToNext();
	    }
	    
	    cursor.close();
	    db.close();
    
	    return result;
    }
    
    //recupera il titolo del canto in posizione "position" nella lista 2
    private String[] getTitoloToSendFromPosition(int position) {
		
    	db = listaCanti.getReadableDatabase();
    	
	    String query = "SELECT B.titolo, B.pagina" +
	      		"  FROM CUST_LISTS A" +
	      		"  	   , ELENCO B" +
	      		"  WHERE A._id = 2" +
	      		"  AND   A.position = " + position + 
	      		"  AND   A.id_canto = B._id" +
	      		"  ORDER BY A.timestamp ASC";
	    Cursor cursor = db.rawQuery(query, null);
	     
	    int total = cursor.getCount();
	    int resultLen = 1;
	    if (total > 1)
	    	resultLen = total;
	    
	    String[] result = new String[resultLen];
	    
	    cursor.moveToFirst();
	    for (int i = 0; i < total; i++) {
	    	result[i] =  cursor.getString(0) + " - PAG." + cursor.getInt(1);
	    	cursor.moveToNext();
	    }
	    
	    cursor.close();
	    db.close();
    
	    return result;
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
			case Utility.EUCAR_RESET_OK:
				db = listaCanti.getReadableDatabase();
        		String sql = "DELETE FROM CUST_LISTS" +
        				" WHERE _id =  2 ";
        		db.execSQL(sql);
        		db.close();
        		updateLista();
        		mShareActionProvider.setShareIntent(getDefaultIntent());
        		getActivity().setRequestedOrientation(prevOrientation);
			default:
				getActivity().setRequestedOrientation(prevOrientation);
				break;
			}
        }
    }

    public void snackBarRimuoviCanto() {
        SnackbarManager.show(
                Snackbar.with(getActivity())
                        .text(getString(R.string.list_remove))
                        .actionLabel(getString(R.string.snackbar_remove))
                        .actionListener(new ActionClickListener() {
                            @Override
                            public void onActionClicked(Snackbar snackbar) {
                                db = listaCanti.getReadableDatabase();
                                String sql = "DELETE FROM CUST_LISTS" +
                                        "  WHERE _id =  2 " +
                                        "    AND position = " + posizioneDaCanc +
                                        "	 AND id_canto = (SELECT _id FROM ELENCO" +
                                        "					WHERE titolo = '" + titoloDaCanc + "')";
                                db.execSQL(sql);
                                db.close();
                                updateLista();
                                mShareActionProvider.setShareIntent(getDefaultIntent());
                            }
                        })
                        .actionColor(getResources().getColor(R.color.theme_accent))
                , getActivity());
    }
    
}