package it.cammino.risuscito;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
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
import android.widget.TextView;

import com.alertdialogpro.AlertDialogPro;
import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.ObservableScrollView;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.util.Locale;

public class CantiParolaFragment extends Fragment {

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
				R.layout.activity_canti_parola, container, false);
		
		//crea un istanza dell'oggetto DatabaseCanti
		listaCanti = new DatabaseCanti(getActivity());
		updateLista();
		
		rootView.findViewById(R.id.cantoIniziale).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openPagina(v);
			}
		});
		
		// setta l'azione tenendo premuto sul canto
   		rootView.findViewById(R.id.cantoIniziale).setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				posizioneDaCanc = 1;
				titoloDaCanc = Utility.duplicaApostrofi(((TextView) view).getText().toString());
				snackBarRimuoviCanto();
				return true;
			}
		});
		
		rootView.findViewById(R.id.addCantoIniziale).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			    Bundle bundle = new Bundle();
			    bundle.putInt("fromAdd", 1);
			    bundle.putInt("idLista", 1);
			    bundle.putInt("position", 1);
			    startSubActivity(bundle);
			}
		});
		
		rootView.findViewById(R.id.primaLettura).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openPagina(v);
			}
		});
		
		// setta l'azione tenendo premuto sul canto
   		rootView.findViewById(R.id.primaLettura).setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				posizioneDaCanc = 2;
				titoloDaCanc = Utility.duplicaApostrofi(((TextView) view).getText().toString());
				snackBarRimuoviCanto();
				return true;
			}
		});
		
		rootView.findViewById(R.id.addPrimaLettura).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			    Bundle bundle = new Bundle();
			    bundle.putInt("fromAdd", 1);
			    bundle.putInt("idLista", 1);
			    bundle.putInt("position", 2);
			    startSubActivity(bundle);
			}
		});
		
		rootView.findViewById(R.id.secondaLettura).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openPagina(v);
			}
		});
		
		// setta l'azione tenendo premuto sul canto
   		rootView.findViewById(R.id.secondaLettura).setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				posizioneDaCanc = 3;
				titoloDaCanc = Utility.duplicaApostrofi(((TextView) view).getText().toString());
				snackBarRimuoviCanto();
				return true;
			}
		});
		
		rootView.findViewById(R.id.addSecondaLettura).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			    Bundle bundle = new Bundle();
			    bundle.putInt("fromAdd", 1);
			    bundle.putInt("idLista", 1);
			    bundle.putInt("position", 3);
			    startSubActivity(bundle);
			}
		});
		
		rootView.findViewById(R.id.terzaLettura).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openPagina(v);
			}
		});
		
		// setta l'azione tenendo premuto sul canto
   		rootView.findViewById(R.id.terzaLettura).setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				posizioneDaCanc = 4;
				titoloDaCanc = Utility.duplicaApostrofi(((TextView) view).getText().toString());
				snackBarRimuoviCanto();
				return true;
			}
		});
		
		rootView.findViewById(R.id.addTerzaLettura).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			    Bundle bundle = new Bundle();
			    bundle.putInt("fromAdd", 1);
			    bundle.putInt("idLista", 1);
			    bundle.putInt("position", 4);
			    startSubActivity(bundle);
			}
		});
		
		rootView.findViewById(R.id.cantoFinale).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openPagina(v);
			}
		});
		
		// setta l'azione tenendo premuto sul canto
   		rootView.findViewById(R.id.cantoFinale).setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				posizioneDaCanc = 5;
				titoloDaCanc = Utility.duplicaApostrofi(((TextView) view).getText().toString());
				snackBarRimuoviCanto();
				return true;
			}
		});
		
		rootView.findViewById(R.id.addCantoFinale).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			    Bundle bundle = new Bundle();
			    bundle.putInt("fromAdd", 1);
			    bundle.putInt("idLista", 1);
			    bundle.putInt("position", 5);
			    startSubActivity(bundle);
			}
		});
		
		FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_parola);
		fab.attachToScrollView((ObservableScrollView) rootView.findViewById(R.id.parolaScrollView));
		fab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				blockOrientation();
				AlertDialogPro.Builder builder = new AlertDialogPro.Builder(getActivity());
                AlertDialogPro dialog = builder.setTitle(R.string.dialog_reset_list_title)
	        			.setMessage(R.string.reset_list_question)
	                    .setPositiveButton(R.string.confirm, new ButtonClickedListener(Utility.PAROLA_RESET_OK))
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
		
		setHasOptionsMenu(true);
		
		mLUtils = LUtils.getInstance(getActivity());
		
		return rootView;
	}
	
    @Override
    public void onResume() {
//    	Log.i("CANTI PAROLA", "ON RESUME");
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
	    if (mShareActionProvider != null && tempPager.getCurrentItem() == 0)
	    	mShareActionProvider.setShareIntent(getDefaultIntent());
	    super.onCreateOptionsMenu(menu, inflater);
	}

	private Intent getDefaultIntent() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_TEXT, getTitlesList());
		intent.setType("text/plain");
		return intent;
   }
	
    private void updateLista() {
        
		String titoloCanto = getTitoloFromPosition(1);
		
		if (titoloCanto.equalsIgnoreCase("")) {
			rootView.findViewById(R.id.addCantoIniziale).setVisibility(View.VISIBLE);
			rootView.findViewById(R.id.cantoIniziale).setVisibility(View.GONE);
		}
		else {
			TextView temp = (TextView) rootView.findViewById(R.id.cantoIniziale);
			rootView.findViewById(R.id.addCantoIniziale).setVisibility(View.GONE);
			temp.setVisibility(View.VISIBLE);
			temp.setText(titoloCanto.substring(7));
			(rootView.findViewById(R.id.cantoInizialeContainer))
					.setBackgroundColor(Color.parseColor(titoloCanto.substring(0,7)));
		}
		
		titoloCanto = getTitoloFromPosition(2);
		
		if (titoloCanto.equalsIgnoreCase("")) {
			rootView.findViewById(R.id.addPrimaLettura).setVisibility(View.VISIBLE);
			rootView.findViewById(R.id.primaLettura).setVisibility(View.GONE);
		}
		else {
			TextView temp = (TextView) rootView.findViewById(R.id.primaLettura);
			rootView.findViewById(R.id.addPrimaLettura).setVisibility(View.GONE);
			temp.setVisibility(View.VISIBLE);
			temp.setText(titoloCanto.substring(7));
			(rootView.findViewById(R.id.primaLetturaContainer))
			.setBackgroundColor(Color.parseColor(titoloCanto.substring(0,7)));
		}
		
		titoloCanto = getTitoloFromPosition(3);
		
		if (titoloCanto.equalsIgnoreCase("")) {
			rootView.findViewById(R.id.addSecondaLettura).setVisibility(View.VISIBLE);
			rootView.findViewById(R.id.secondaLettura).setVisibility(View.GONE);
		}
		else {
			TextView temp = (TextView) rootView.findViewById(R.id.secondaLettura);
			rootView.findViewById(R.id.addSecondaLettura).setVisibility(View.GONE);
			temp.setVisibility(View.VISIBLE);
			temp.setText(titoloCanto.substring(7));
			(rootView.findViewById(R.id.secondaLetturaContainer))
			.setBackgroundColor(Color.parseColor(titoloCanto.substring(0,7)));
		}
		
		titoloCanto = getTitoloFromPosition(4);
		
		if (titoloCanto.equalsIgnoreCase("")) {
			rootView.findViewById(R.id.addTerzaLettura).setVisibility(View.VISIBLE);
			rootView.findViewById(R.id.terzaLettura).setVisibility(View.GONE);
		}
		else {
			TextView temp = (TextView) rootView.findViewById(R.id.terzaLettura);
			rootView.findViewById(R.id.addTerzaLettura).setVisibility(View.GONE);
			temp.setVisibility(View.VISIBLE);
			temp.setText(titoloCanto.substring(7));
			(rootView.findViewById(R.id.terzaLetturaContainer))
			.setBackgroundColor(Color.parseColor(titoloCanto.substring(0,7)));
		}
		
		titoloCanto = getTitoloFromPosition(5);
		
		if (titoloCanto.equalsIgnoreCase("")) {
			rootView.findViewById(R.id.addCantoFinale).setVisibility(View.VISIBLE);
			rootView.findViewById(R.id.cantoFinale).setVisibility(View.GONE);
		}
		else {
			TextView temp = (TextView) rootView.findViewById(R.id.cantoFinale);
			rootView.findViewById(R.id.addCantoFinale).setVisibility(View.GONE);
			temp.setVisibility(View.VISIBLE);
			temp.setText(titoloCanto.substring(7));
			(rootView.findViewById(R.id.cantoFinaleContainer))
			.setBackgroundColor(Color.parseColor(titoloCanto.substring(0,7)));
		}
		
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
    
    //recupera il titolo del canto in posizione "position" nella lista
    private String getTitoloFromPosition(int position) {
		
    	db = listaCanti.getReadableDatabase();
    	
	    String query = "SELECT B.titolo, color" +
	      		"  FROM CUST_LISTS A" +
	      		"  	   , ELENCO B" +
	      		"  WHERE A._id = 1" +
	      		"  AND   A.position = " + position + 
	      		"  AND   A.id_canto = B._id";
	    Cursor cursor = db.rawQuery(query, null);
	     
	    int total = cursor.getCount();
	    String result = "";
	    
	    if (total == 1) {
	    	cursor.moveToFirst();
	    	result =  cursor.getString(1) + cursor.getString(0);
	    }
	    
	    cursor.close();
	    db.close();
    
	    return result;
    }
    
    private String getTitlesList() {
    	
    	Locale l = Locale.getDefault();
    	String result = "";
    	String temp;
    	
    	//titolo
    	result +=  "-- CELEBRAZIONE DELLA PAROLA --\n";
    	
    	//canto iniziale
    	temp = getTitoloToSendFromPosition(1);
    	
    	result += getResources().getString(R.string.canto_iniziale).toUpperCase(l);
    	result += "\n";
    	
    	if (temp.equalsIgnoreCase(""))
    		result += ">> da scegliere <<";
    	else
    		result += temp;
    	
    	result += "\n";
    	
    	//prima lettura
    	temp = getTitoloToSendFromPosition(2);
    	
    	result += getResources().getString(R.string.prima_lettura).toUpperCase(l);
    	result += "\n";
    	
    	if (temp.equalsIgnoreCase(""))
    		result += ">> da scegliere <<";
    	else
    		result += temp;
    	
    	result += "\n";
    	
    	//seconda lettura
    	temp = getTitoloToSendFromPosition(3);
    	
    	result += getResources().getString(R.string.seconda_lettura).toUpperCase(l);
    	result += "\n";
    	
    	if (temp.equalsIgnoreCase(""))
    		result += ">> da scegliere <<";
    	else
    		result += temp;
    	
    	result += "\n";
    	
    	//terza lettura
    	temp = getTitoloToSendFromPosition(4);
    	
    	result += getResources().getString(R.string.terza_lettura).toUpperCase(l);
    	result += "\n";
    	
    	if (temp.equalsIgnoreCase(""))
    		result += ">> da scegliere <<";
    	else
    		result += temp;
    	
    	result += "\n";
    	
    	//canto finale
    	temp = getTitoloToSendFromPosition(5);
    	
    	result += getResources().getString(R.string.canto_fine).toUpperCase(l);
    	result += "\n";
    	
    	if (temp.equalsIgnoreCase(""))
    		result += ">> da scegliere <<";
    	else
    		result += temp;	    	
    	    	
    	return result;
    	
    }
    
    //recupera il titolo del canto in posizione "position" nella lista "list"
    private String getTitoloToSendFromPosition(int position) {
		
    	db = listaCanti.getReadableDatabase();
    	
	    String query = "SELECT B.titolo, B.pagina" +
	      		"  FROM CUST_LISTS A" +
	      		"  	   , ELENCO B" +
	      		"  WHERE A._id = 1" +
	      		"  AND   A.position = " + position + 
	      		"  AND   A.id_canto = B._id";
	    Cursor cursor = db.rawQuery(query, null);
	     
	    int total = cursor.getCount();
	    String result = "";
	    
	    if (total == 1) {
	    	cursor.moveToFirst();
	    	result =  cursor.getString(0) + " - PAG." + cursor.getInt(1);
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
			case Utility.PAROLA_RESET_OK:
				db = listaCanti.getReadableDatabase();
        		String sql = "DELETE FROM CUST_LISTS" +
        				" WHERE _id =  1 ";
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
    
    public void blockOrientation() {
        prevOrientation = getActivity().getRequestedOrientation();
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        	getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
        	getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
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
                                        "  WHERE _id =  1 " +
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