package it.cammino.risuscito;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.internal.widget.TintEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alertdialogpro.AlertDialogPro;
import com.alertdialogpro.material.ProgressBarCompat;
import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.widgets.Dialog;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class RicercaAvanzataFragment extends Fragment {

	private DatabaseCanti listaCanti;
	private String[] titoli;
	private TintEditText searchPar;
	private View rootView;
	private static String[][] aTexts;
	ListView lv;
	private ProgressBarCompat progress;
	private int prevOrientation;
	private static Map<Character, Character> MAP_NORM;
	
	private String titoloDaAgg;
	private int idListaDaAgg;
	private int posizioneDaAgg;
	private ListaPersonalizzata[] listePers;
	private int[] idListe;
	private int idListaClick;
	private int idPosizioneClick;
	
	private final int ID_FITTIZIO = 99999999;
	private final int ID_BASE = 100;
		
	private LUtils mLUtils;
	
	private SearchTask searchTask ;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(
				R.layout.activity_ricerca_avanzata, container, false);
				
		searchPar = (TintEditText) rootView.findViewById(R.id.textfieldRicerca);
		listaCanti = new DatabaseCanti(getActivity());
				
		lv = (ListView) rootView.findViewById(R.id.matchedList);
		progress = (ProgressBarCompat) rootView.findViewById(R.id.search_progress);
		searchPar.setText("");

		try {
			InputStream in = getActivity().getAssets().open("fileout_new.xml");
        	CantiXmlParser parser = new CantiXmlParser();
            aTexts = parser.parse(in);
            in.close();
        } 	catch (XmlPullParserException | IOException e) {
        	e.printStackTrace();
        }

        searchPar.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
				String tempText = ((EditText) getActivity().findViewById(R.id.tempTextField)).getText().toString();
				if (!tempText.equals(s.toString()))
					((EditText) getActivity().findViewById(R.id.tempTextField)).setText(s);
				
				//abilita il pulsante solo se la stringa ha più di 3 caratteri, senza contare gli spazi
				if (s.toString().trim().length() >= 3) {
					if (searchTask != null && searchTask.getStatus() == Status.RUNNING)
						searchTask.cancel(true);
					searchTask = new SearchTask();
					searchTask.execute(searchPar.getText().toString());
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			
			@Override
			public void afterTextChanged(Editable s) { }
			
		});
		
	    ((EditText) getActivity().findViewById(R.id.tempTextField)).addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				String tempText = searchPar.getText().toString();
				if (!tempText.equals(s.toString()))
					searchPar.setText(s);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

			@Override
			public void afterTextChanged(Editable s) { }
			
	    });
		
		ButtonRectangle pulisci = (ButtonRectangle) rootView.findViewById(R.id.pulisci_ripple);
		pulisci.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                v.playSoundEffect(android.view.SoundEffectConstants.CLICK);
				searchPar.setText("");
				rootView.findViewById(R.id.search_no_results).setVisibility(View.GONE);
				lv.setVisibility(View.GONE);
				progress.setVisibility(View.GONE);
			}
		});
		
		SQLiteDatabase db = listaCanti.getReadableDatabase();
		String query = "SELECT _id, lista" +
				"		FROM LISTE_PERS" +
				"		ORDER BY _id ASC";
		Cursor lista = db.rawQuery(query, null);
				
		listePers = new ListaPersonalizzata[lista.getCount()];
		idListe = new int[lista.getCount()];
		
		lista.moveToFirst();    		    		
		for (int i = 0; i < lista.getCount(); i++) {
			idListe[i] = lista.getInt(0);
			listePers[i] = (ListaPersonalizzata) ListaPersonalizzata.
					deserializeObject(lista.getBlob(1));
			lista.moveToNext();
		}		
		lista.close();
		db.close();
		
		setHasOptionsMenu(true);
		
		mLUtils = LUtils.getInstance(getActivity());
		
		return rootView;
	}

	@Override
	public void onDestroy() {
		if (searchTask != null && searchTask.getStatus() == Status.RUNNING)
			searchTask.cancel(true);
		if (listaCanti != null)
			listaCanti.close();
		super.onDestroy();
	}
	
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        titoloDaAgg = ((TextView) info.targetView.findViewById(R.id.text_title)).getText().toString();
        menu.setHeaderTitle("Aggiungi canto a:");
        
        for (int i = 0; i < idListe.length; i++) {
        	SubMenu subMenu = menu.addSubMenu(ID_FITTIZIO, Menu.NONE, 10+i, listePers[i].getName());
        	for (int k = 0; k < listePers[i].getNumPosizioni(); k++) {
        		subMenu.add(ID_BASE + i, k, k, listePers[i].getNomePosizione(k));
        	}
        }
        
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.add_to, menu);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	if (getUserVisibleHint()) {
	        switch (item.getItemId()) {
	            case R.id.add_to_favorites:
	                addToFavorites(titoloDaAgg);
	                return true;
	            case R.id.add_to_p_iniziale:
	                addToListaNoDup(1, 1, titoloDaAgg);
	                return true;
	            case R.id.add_to_p_prima:
	            	addToListaNoDup(1, 2, titoloDaAgg);
	                return true;
	            case R.id.add_to_p_seconda:
	            	addToListaNoDup(1, 3, titoloDaAgg);
	                return true;
	            case R.id.add_to_p_terza:
	            	addToListaNoDup(1, 4, titoloDaAgg);
	                return true;
	            case R.id.add_to_p_fine:
	            	addToListaNoDup(1, 5, titoloDaAgg);
	                return true;
	            case R.id.add_to_e_iniziale:
	            	addToListaNoDup(2, 1, titoloDaAgg);
	                return true;
	            case R.id.add_to_e_pace:
	            	addToListaNoDup(2, 2, titoloDaAgg);
	                return true;
	            case R.id.add_to_e_pane:
	                addToListaDup(2, 3, titoloDaAgg);
	                return true;
	            case R.id.add_to_e_vino:
	            	addToListaDup(2, 4, titoloDaAgg);
	                return true;
	            case R.id.add_to_e_fine:
	            	addToListaNoDup(2, 5, titoloDaAgg);
	                return true;
	            default:
	            	idListaClick = item.getGroupId();
	            	idPosizioneClick = item.getItemId();
	            	if (idListaClick != ID_FITTIZIO && idListaClick >= 100) {
	            		idListaClick -= 100;
	            		if (listePers[idListaClick]
	            				.getCantoPosizione(idPosizioneClick).equalsIgnoreCase("")) {
		            		String cantoCliccatoNoApex = Utility.duplicaApostrofi(titoloDaAgg);		    			    
		    		        SQLiteDatabase db = listaCanti.getReadableDatabase();
		            		
		    		        String query = "SELECT color, pagina" +
				    				"		FROM ELENCO" +
				    				"		WHERE titolo = '" + cantoCliccatoNoApex + "'";
				    		Cursor cursor = db.rawQuery(query, null);
			    			
				    		cursor.moveToFirst();
				    							    		
				    		listePers[idListaClick].addCanto(Utility.intToString(
				    				cursor.getInt(1), 3) + cursor.getString(0) + titoloDaAgg, idPosizioneClick);
			    						    				
		    		    	ContentValues  values = new  ContentValues( );
		    		    	values.put("lista" , ListaPersonalizzata.serializeObject(listePers[idListaClick]));
		    		    	db.update("LISTE_PERS", values, "_id = " + idListe[idListaClick], null );	
		    		    	db.close();
		    		    	
		    	    		Toast.makeText(getActivity()
		    	    				, getString(R.string.list_added), Toast.LENGTH_SHORT).show();
	            		}
		    		    else {
		    		    	if (listePers[idListaClick].getCantoPosizione(idPosizioneClick).substring(10)
		    		    			.equalsIgnoreCase(titoloDaAgg)) {
		    		    		Toast toast = Toast.makeText(getActivity()
		    		    				, getString(R.string.present_yet), Toast.LENGTH_SHORT);
		    		    		toast.show();
		    		    	}
		    		    	else {
			    		    	blockOrientation();
			                    AlertDialogPro.Builder builder = new AlertDialogPro.Builder(getActivity());
			                    AlertDialogPro dialog = builder.setTitle(R.string.dialog_replace_title)
			    	        			.setMessage(getString(R.string.dialog_present_yet) + " " 
					    						+ listePers[idListaClick].getCantoPosizione(idPosizioneClick)
					    						.substring(10)
					    						+ getString(R.string.dialog_wonna_replace))
			    	                    .setPositiveButton(R.string.confirm, new ButtonClickedListener(Utility.AVANZATA_LISTAPERS_OK))
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
		    		    }	    		
		    		    return true;
	            	}
	            	else
	            		return super.onContextItemSelected(item);
	        }
    	}
    	else
    		return false;
    }
   
    //aggiunge il canto premuto ai preferiti
    public void addToFavorites(String titolo) {
    	
    	SQLiteDatabase db = listaCanti.getReadableDatabase();
    	
    	String titoloNoApex = Utility.duplicaApostrofi(titolo);
    	
		String sql = "UPDATE ELENCO" +
				"  SET favourite = 1" + 
				"  WHERE titolo =  \'" + titoloNoApex + "\'";
		db.execSQL(sql);
		db.close();
		
		Toast toast = Toast.makeText(getActivity()
				, getString(R.string.favorite_added), Toast.LENGTH_SHORT);
		toast.show();

    }
    
    //aggiunge il canto premuto ad una lista e in una posizione che ammetta duplicati
    public void addToListaDup(int idLista, int listPosition, String titolo) {
    	
    	String titoloNoApex = Utility.duplicaApostrofi(titolo);
    	
    	SQLiteDatabase db = listaCanti.getReadableDatabase();
    	
    	String sql = "INSERT INTO CUST_LISTS ";
    	sql+= "VALUES (" + idLista + ", " 
    			 + listPosition + ", "
    			 + "(SELECT _id FROM ELENCO"
    			 + " WHERE titolo = \'" + titoloNoApex + "\')"
    			 + ", CURRENT_TIMESTAMP)";
    	
    	try {
    		db.execSQL(sql);
    		Toast.makeText(getActivity()
    				, getString(R.string.list_added), Toast.LENGTH_SHORT).show();
    	} catch (SQLException e) {
    		Toast toast = Toast.makeText(getActivity()
    				, getString(R.string.present_yet), Toast.LENGTH_SHORT);
    		toast.show();
    	}
    	
    	db.close();		
    }
    
  //aggiunge il canto premuto ad una lista e in una posizione che NON ammetta duplicati
    public void addToListaNoDup(int idLista, int listPosition, String titolo) {
    	
    	String titoloNoApex = Utility.duplicaApostrofi(titolo);
    	
    	SQLiteDatabase db = listaCanti.getReadableDatabase();
    	
		// cerca se la posizione nella lista è già occupata
		String query = "SELECT B.titolo" +
				"		FROM CUST_LISTS A" +
				"		   , ELENCO B" +
				"		WHERE A._id = " + idLista +
				"         AND A.position = " + listPosition +
				"         AND A.id_canto = B._id";
		Cursor lista = db.rawQuery(query, null);
		
		int total = lista.getCount();
		
		if (total > 0) {
			lista.moveToFirst();    
			String titoloPresente = lista.getString(0);
    		lista.close();
    		db.close();
    		
			if (titolo.equalsIgnoreCase(titoloPresente)) {
	    		Toast toast = Toast.makeText(getActivity()
	    				, getString(R.string.present_yet), Toast.LENGTH_SHORT);
	    		toast.show();
			}
			else {
				idListaDaAgg = idLista;
				posizioneDaAgg = listPosition;
				
	    		blockOrientation();
                AlertDialogPro.Builder builder = new AlertDialogPro.Builder(getActivity());
                AlertDialogPro dialog = builder.setTitle(R.string.dialog_replace_title)
	        			.setMessage(getString(R.string.dialog_present_yet) + " " + titoloPresente
	    						+ getString(R.string.dialog_wonna_replace))
	                    .setPositiveButton(R.string.confirm, new ButtonClickedListener(Utility.AVANZATA_LISTAPRED_OK))
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
    		return;
		}
		
		lista.close();
		
    	String sql = "INSERT INTO CUST_LISTS "
    	         + "VALUES (" + idLista + ", " 
    			 + listPosition + ", "
    			 + "(SELECT _id FROM ELENCO"
    			 + " WHERE titolo = \'" + titoloNoApex + "\')"
    			 + ", CURRENT_TIMESTAMP)";
    	db.execSQL(sql);
    	db.close();	
    	
		Toast.makeText(getActivity()
				, getString(R.string.list_added), Toast.LENGTH_SHORT).show();
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
			case Utility.AVANZATA_LISTAPERS_OK:
				SQLiteDatabase db = listaCanti.getReadableDatabase();
            	String cantoCliccatoNoApex = Utility.duplicaApostrofi(titoloDaAgg);	
    	    	String query = "SELECT color, pagina" +
        				"		FROM ELENCO" +
        				"		WHERE titolo = '" + cantoCliccatoNoApex + "'";
        		Cursor cursor = db.rawQuery(query, null);
    			
        		cursor.moveToFirst();
        							    		
        		listePers[idListaClick].addCanto(Utility.intToString(
        				cursor.getInt(1), 3) + cursor.getString(0) + titoloDaAgg, idPosizioneClick);
    						    				
    	    	ContentValues  values = new  ContentValues( );
    	    	values.put("lista" , ListaPersonalizzata.serializeObject(listePers[idListaClick]));
    	    	db.update("LISTE_PERS", values, "_id = " + idListe[idListaClick], null );
    	    	getActivity().setRequestedOrientation(prevOrientation);
    			Toast.makeText(getActivity()
    					, getString(R.string.list_added), Toast.LENGTH_SHORT).show();
				break;
			case Utility.AVANZATA_LISTAPRED_OK:
				db = listaCanti.getReadableDatabase();
            	cantoCliccatoNoApex = Utility.duplicaApostrofi(titoloDaAgg);	
    	    	String sql = "UPDATE CUST_LISTS "
    	    			+ "SET id_canto = (SELECT _id  FROM ELENCO"
    	    			+ " WHERE titolo = \'" + cantoCliccatoNoApex + "\')"
    	    			+ "WHERE _id = " + idListaDaAgg 
    	    			+ "  AND position = " + posizioneDaAgg;	    	
    	    	db.execSQL(sql);
    	    	getActivity().setRequestedOrientation(prevOrientation);
    			Toast.makeText(getActivity()
    					, getString(R.string.list_added), Toast.LENGTH_SHORT).show();
    			break;
			default:
				getActivity().setRequestedOrientation(prevOrientation);
				break;
			}
        }
    }
    
    private void startSubActivity(Bundle bundle, View view) {
    	Intent intent = new Intent(getActivity().getApplicationContext(), PaginaRenderActivity.class);
    	intent.putExtras(bundle);
    	mLUtils.startActivityWithTransition(intent, view, Utility.TRANS_PAGINA_RENDER);
   	}
    
    private class SongRowAdapter extends ArrayAdapter<String> {
    	
    	SongRowAdapter() {
    		super(getActivity(), R.layout.row_item, R.id.text_title, titoli);
    	}
    	
    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		
    		View row=super.getView(position, convertView, parent);
    		
    		TextView canto = (TextView) row.findViewById(R.id.text_title);
    		
    		String totalString = canto.getText().toString();
    		
    		int tempPagina = Integer.valueOf(totalString.substring(0,3));
    		String pagina = String.valueOf(tempPagina);
    		String colore = totalString.substring(3, 10);

    		((TextView) row.findViewById(R.id.text_title))
    			.setText(totalString.substring(10));
    		    		
    		TextView textPage = (TextView) row.findViewById(R.id.text_page);
    		textPage.setText(pagina);
    		LinearLayout fullRow = (LinearLayout) row.findViewById(R.id.full_row);
    		fullRow.setBackgroundColor(Color.parseColor(colore));
    		
    		return(row);
    	}
    }
    
    private class SearchTask extends AsyncTask<String, Integer, String> {

        @SuppressLint("NewApi")
		@Override
        protected String doInBackground(String... sSearchText) {
			
			// crea un manipolatore per il Database in modalità READ
    		SQLiteDatabase db = listaCanti.getReadableDatabase();
        	
        	String[] words = sSearchText[0].split("\\W");
			
//			for (int j = 0; j < words.length; j++)
//				if (words[j].trim().length() > 2)
//					Log.i("PAROLA[" + j + "]:", words[j].trim());

			String text = "";
			String[] aResults = new String[300];
			int totalResults = 0;
			
			for (int k = 0; k < aTexts.length; k++) {
				
				if (aTexts[k][0] == null || aTexts[k][0].equalsIgnoreCase(""))
					break;
				
				boolean found = true;
				for (int j = 0; j < words.length; j++) {
					if (words[j].trim().length() > 1) {
						text = words[j].trim();
						text = text.toLowerCase(Locale.getDefault());
						
						if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
						    String nfdNormalizedString = Normalizer.normalize(text, Normalizer.Form.NFD); 
						    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
						    text =  pattern.matcher(nfdNormalizedString).replaceAll("");
						}
						else
							text = removeAccents(text);
						
						if (!aTexts[k][1].contains(text)) {
							found = false;
						}
					}
				}
				
				if (found) {
		    							
					// recupera il titolo colore e pagina del canto da aggiungere alla lista
		    		String query = "SELECT titolo, color, pagina"
		    		 +		"		FROM ELENCO"
		    		 +		"		WHERE source = '" + aTexts[k][0] + "'";
		    		
		    		Cursor lista = db.rawQuery(query, null);
	                
		    		if (lista.getCount() > 0) {
		    			lista.moveToFirst();
//		    			Log.i("TROVATO IN", aTexts[k][0]);
//		    			Log.i("LUNGHEZZA", aResults.length+"");
		    			aResults[totalResults++] = Utility.intToString(lista.getInt(2), 3) + lista.getString(1) + lista.getString(0);
					}
		    		// chiude il cursore
		    		lista.close();
//					Log.i("TROVATO DOPO", aResults[totalResults - 1]);
				}
			}
			
			titoli = new String[totalResults];
            System.arraycopy(aResults, 0, titoli, 0, totalResults);
			
            return null;
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            rootView.findViewById(R.id.search_no_results).setVisibility(View.GONE);
            lv.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String result) {
    		// crea un list adapter per l'oggetto di tipo ListView
    		lv.setAdapter(new SongRowAdapter());
    		
    		// setta l'azione al click su ogni voce dell'elenco
    		lv.setOnItemClickListener(new OnItemClickListener() {
    			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    			    
    				// recupera il titolo della voce cliccata
    				String cantoCliccato = ((TextView) view.findViewById(R.id.text_title))
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
	    			    
	    			// crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da visualizzare  
	    		    Bundle bundle = new Bundle();
	    			bundle.putString("pagina", pagina);
	    			bundle.putInt("idCanto", idCanto);
	    			    
	    			// lancia l'activity che visualizza il canto passando il parametro creato
	    			startSubActivity(bundle, view); 
	    			
    			}
    		});
            
    		progress.setVisibility(View.GONE);
    		
    		if (titoli.length == 0) {
    			rootView.findViewById(R.id.search_no_results).setVisibility(View.VISIBLE);
    			lv.setVisibility(View.GONE);
    		}
    		else {
    			rootView.findViewById(R.id.search_no_results).setVisibility(View.GONE);
    			lv.setVisibility(View.VISIBLE);
    			registerForContextMenu(lv);
			}
        }
        
    }
    
    public static String removeAccents(String value)
    {
        if (MAP_NORM == null || MAP_NORM.size() == 0)
        {
            MAP_NORM = new HashMap<Character, Character>();
            MAP_NORM.put('À', 'A');
            MAP_NORM.put('Á', 'A');
            MAP_NORM.put('Â', 'A');
            MAP_NORM.put('Ã', 'A');
            MAP_NORM.put('Ä', 'A');
            MAP_NORM.put('È', 'E');
            MAP_NORM.put('É', 'E');
            MAP_NORM.put('Ê', 'E');
            MAP_NORM.put('Ë', 'E');
            MAP_NORM.put('Í', 'I');
            MAP_NORM.put('Ì', 'I');
            MAP_NORM.put('Î', 'I');
            MAP_NORM.put('Ï', 'I');
            MAP_NORM.put('Ù', 'U');
            MAP_NORM.put('Ú', 'U');
            MAP_NORM.put('Û', 'U');
            MAP_NORM.put('Ü', 'U');
            MAP_NORM.put('Ò', 'O');
            MAP_NORM.put('Ó', 'O');
            MAP_NORM.put('Ô', 'O');
            MAP_NORM.put('Õ', 'O');
            MAP_NORM.put('Ö', 'O');
            MAP_NORM.put('Ñ', 'N');
            MAP_NORM.put('Ç', 'C');
            MAP_NORM.put('ª', 'A');
            MAP_NORM.put('º', 'O');
            MAP_NORM.put('§', 'S');
            MAP_NORM.put('³', '3');
            MAP_NORM.put('²', '2');
            MAP_NORM.put('¹', '1');
            MAP_NORM.put('à', 'a');
            MAP_NORM.put('á', 'a');
            MAP_NORM.put('â', 'a');
            MAP_NORM.put('ã', 'a');
            MAP_NORM.put('ä', 'a');
            MAP_NORM.put('è', 'e');
            MAP_NORM.put('é', 'e');
            MAP_NORM.put('ê', 'e');
            MAP_NORM.put('ë', 'e');
            MAP_NORM.put('í', 'i');
            MAP_NORM.put('ì', 'i');
            MAP_NORM.put('î', 'i');
            MAP_NORM.put('ï', 'i');
            MAP_NORM.put('ù', 'u');
            MAP_NORM.put('ú', 'u');
            MAP_NORM.put('û', 'u');
            MAP_NORM.put('ü', 'u');
            MAP_NORM.put('ò', 'o');
            MAP_NORM.put('ó', 'o');
            MAP_NORM.put('ô', 'o');
            MAP_NORM.put('õ', 'o');
            MAP_NORM.put('ö', 'o');
            MAP_NORM.put('ñ', 'n');
            MAP_NORM.put('ç', 'c');
        }

        if (value == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder(value);

        for(int i = 0; i < value.length(); i++) {
            Character c = MAP_NORM.get(sb.charAt(i));
            if(c != null) {
                sb.setCharAt(i, c.charValue());
            }
        }

        return sb.toString();
    }

}
