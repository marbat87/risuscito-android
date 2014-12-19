package it.cammino.risuscito;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.widget.TintEditText;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alertdialogpro.AlertDialogPro;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;

import it.cammino.utilities.dslv.DragSortListView;
import it.cammino.utilities.showcaseview.OnShowcaseEventListener;
import it.cammino.utilities.showcaseview.ShowcaseView;
import it.cammino.utilities.showcaseview.targets.ViewTarget;

@SuppressLint("NewApi") @SuppressWarnings("deprecation")
public class CreaListaActivity extends ActionBarActivity {

	private ListaPersonalizzata celebrazione;
	private DatabaseCanti listaCanti;
	private PositionAdapter adapter;
	private ArrayList<String> nomiElementi;
	private String titoloLista;
	private DragSortListView lv;
	private int prevOrientation;
	private boolean modifica;
	private int idModifica;
	private RetainedFragment dataFragment;
	private RetainedFragment dataFragment2;
	private RetainedFragment dataFragment3;
	private int positionToRename;
	private RelativeLayout.LayoutParams lps;
	private boolean fakeItemCreated;
	private int screenWidth;
	private int screenHeight;
	private ArrayList<String> nomiCanti;
	private int positionLI;
	private Bundle tempArgs;

	private static final String PREF_FIRST_OPEN = "prima_apertura_crealista_v2";
	
	private final String TEMP_TITLE = "temp_title";
	
	private AlertDialogPro dialog, dialogAdd;
	
    private TintEditText titleInputRename, titleInputAdd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crea_lista);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.risuscito_toolbar);
		toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        setSupportActionBar(toolbar);
	
        // setta il colore della barra di stato, solo su KITKAT
        Utility.setupTransparentTints(CreaListaActivity.this);
		
		listaCanti = new DatabaseCanti(this);
		
        Bundle bundle = this.getIntent().getExtras();
        modifica = bundle.getBoolean("modifica");
        
        if (modifica) {
        	SQLiteDatabase db = listaCanti.getReadableDatabase();
        	
        	idModifica = bundle.getInt("idDaModif");
        	
    	    String query = "SELECT titolo_lista, lista"
    	      		+ "  FROM LISTE_PERS"
    	      		+ "  WHERE _id = " + idModifica;
    	    Cursor cursor = db.rawQuery(query, null);

    	    cursor.moveToFirst();
    	    titoloLista = cursor.getString(0);
    	    celebrazione = (ListaPersonalizzata) ListaPersonalizzata.deserializeObject(cursor.getBlob(1));
    	    cursor.close();
    	    db.close();
        }
        else
        	titoloLista = bundle.getString("titolo");
        
		lv = (DragSortListView) findViewById(android.R.id.list);
		
        lv.setDropListener(onDrop);
        lv.setRemoveListener(onRemove);

        dataFragment = (RetainedFragment) getSupportFragmentManager().findFragmentByTag("nomiElementi");
        if (dataFragment != null) {
            nomiElementi = dataFragment.getData();
        }
        else {
        	nomiElementi = new ArrayList<String>();
        	if (modifica) {
	        	for (int i = 0; i < celebrazione.getNumPosizioni(); i++)
	        		nomiElementi.add(celebrazione.getNomePosizione(i));
        	}
        }
        
        if (modifica) {
	        dataFragment2 = (RetainedFragment) getSupportFragmentManager().findFragmentByTag("nomiCanti");
	        if (dataFragment2 != null) {
	            nomiCanti = dataFragment2.getData();
	        }
	        else {
	        	nomiCanti = new ArrayList<String>();
	        	if (modifica) {
		        	for (int i = 0; i < celebrazione.getNumPosizioni(); i++) {
//		        		Log.i("CANTO", celebrazione.getCantoPosizione(i));
		        		nomiCanti.add(celebrazione.getCantoPosizione(i));
		        	}
	        	}
	        }
        }

        dataFragment3 = (RetainedFragment) getSupportFragmentManager().findFragmentByTag(TEMP_TITLE);
        if (dataFragment3 != null) {
        	tempArgs = dataFragment3.getArguments();
            ((TintEditText)findViewById(R.id.textfieldTitle))
            	.setText(tempArgs.getCharSequence(TEMP_TITLE));
        }
        else {
        	((TintEditText)findViewById(R.id.textfieldTitle))
        	.setText(titoloLista);
        }
        
        positionLI = R.layout.position_list_item_light;
        
        adapter = new PositionAdapter();
        lv.setAdapter(adapter);
        
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                prevOrientation = getRequestedOrientation();
                Utility.blockOrientation(CreaListaActivity.this);
				positionToRename = position;
		        AlertDialogPro.Builder builder = new AlertDialogPro.Builder(CreaListaActivity.this);
	        	dialog = builder.setTitle(R.string.posizione_rename)
	        			.setView(getLayoutInflater().inflate(R.layout.dialog_customview, null))
	                    .setPositiveButton(R.string.aggiungi_rename, new ButtonClickedListener(Utility.RENAME_CONFERMA))
	                    .setNegativeButton(R.string.aggiungi_dismiss, new ButtonClickedListener(Utility.DISMISS))
	                    .show();
	        	dialog.setOnKeyListener(new Dialog.OnKeyListener() {
			        @Override
			        public boolean onKey(DialogInterface arg0, int keyCode,
			        		KeyEvent event) {
			        	if (keyCode == KeyEvent.KEYCODE_BACK
			        			&& event.getAction() == KeyEvent.ACTION_UP) {
			        		arg0.dismiss();
			        		setRequestedOrientation(prevOrientation);
			        		return true;
			            }
			            return false;
			        }
		        });
	        	titleInputRename = (TintEditText)dialog.findViewById(R.id.list_title);
	        	titleInputRename.setText(nomiElementi.get(positionToRename));
	        	titleInputRename.selectAll();
	        	titleInputRename.addTextChangedListener(new TextWatcher() {
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
			}
		});	
        
		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_crea_lista);
		fab.attachToListView(lv);
		fab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                prevOrientation = getRequestedOrientation();
                Utility.blockOrientation(CreaListaActivity.this);
				AlertDialogPro.Builder builder = new AlertDialogPro.Builder(CreaListaActivity.this);
	        	dialogAdd = builder.setTitle(R.string.posizione_add_desc)
	        			.setView(getLayoutInflater().inflate(R.layout.dialog_customview, null))
	                    .setPositiveButton(R.string.aggiungi_confirm, new ButtonClickedListener(Utility.AGGIUNGI_CONFERMA))
	                    .setNegativeButton(R.string.aggiungi_dismiss, new ButtonClickedListener(Utility.DISMISS))
	                    .show();
	        	dialogAdd.setOnKeyListener(new Dialog.OnKeyListener() {
			        @Override
			        public boolean onKey(DialogInterface arg0, int keyCode,
			        		KeyEvent event) {
			        	if (keyCode == KeyEvent.KEYCODE_BACK
			        			&& event.getAction() == KeyEvent.ACTION_UP) {
			        		arg0.dismiss();
			        		setRequestedOrientation(prevOrientation);
			        		return true;
			            }
			            return false;
			        }
		        });
	        	dialogAdd.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
	        	titleInputAdd = (TintEditText)dialogAdd.findViewById(R.id.list_title);
	        	titleInputAdd.addTextChangedListener(new TextWatcher() {
			        @Override
			        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			        @Override
			        public void onTextChanged(CharSequence s, int start, int before, int count) {
			        	dialogAdd.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(s.toString().trim().length() > 0);
			        }
			
			        @Override
			        public void afterTextChanged(Editable s) {}
			    });
	        	dialogAdd.setCancelable(false);
			}
		});
		
		if (nomiElementi.size() > 0)
			findViewById(R.id.noElementsAdded).setVisibility(View.GONE);
		
		Display display = getWindowManager().getDefaultDisplay();
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
			screenWidth = display.getWidth();
			screenHeight = display.getHeight();
		}
		else {
			Point size = new Point();
			display.getSize(size);
			screenWidth = size.x;
			screenHeight = size.y;
		}
		
        if(PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean(PREF_FIRST_OPEN, true)) { 
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(CreaListaActivity.this)
                    .edit();
            editor.putBoolean(PREF_FIRST_OPEN, false);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            	editor.commit();
            } else {
            	editor.apply();
            }
        	showHelp();
        }
       	
        findViewById(R.id.textTitleDescription).requestFocus();
        
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.crea_lista_menu, menu);
		return true;
	}
	
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_help:
			showHelp();
	        return true;
		case R.id.action_save_list:
			if (saveList()) {
				finish();
				overridePendingTransition(0, R.anim.slide_out_bottom);
			}
			return true;
		case android.R.id.home:
			if (nomiElementi.size() > 0) {
                prevOrientation = getRequestedOrientation();
                Utility.blockOrientation(CreaListaActivity.this);
                AlertDialogPro.Builder builder = new AlertDialogPro.Builder(CreaListaActivity.this);
                AlertDialogPro dialog = builder.setTitle(R.string.save_list_title)
	        			.setMessage(R.string.save_list_question)
	                    .setPositiveButton(R.string.confirm, new ButtonClickedListener(Utility.SAVE_LIST_OK))
	                    .setNegativeButton(R.string.dismiss, new ButtonClickedListener(Utility.SAVE_LIST_KO))
	                    .setNeutralButton(R.string.cancel, new ButtonClickedListener(Utility.DISMISS))
	                    .show();
                dialog.setOnKeyListener(new Dialog.OnKeyListener() {
			        @Override
			        public boolean onKey(DialogInterface arg0, int keyCode,
			        		KeyEvent event) {
			        	if (keyCode == KeyEvent.KEYCODE_BACK
			        			&& event.getAction() == KeyEvent.ACTION_UP) {
			        		arg0.dismiss();
			        		setRequestedOrientation(prevOrientation);
			        		return true;
			            }
			            return false;
			        }
		        });
                dialog.setCancelable(false);
		        return true;
			}
			else {
				finish();
				overridePendingTransition(0, R.anim.slide_out_bottom);
			}
			return true;	
			}
		return false;
	}
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (nomiElementi.size() > 0) {
                prevOrientation = getRequestedOrientation();
                Utility.blockOrientation(CreaListaActivity.this);
				AlertDialogPro.Builder builder = new AlertDialogPro.Builder(CreaListaActivity.this);
                AlertDialogPro dialog = builder.setTitle(R.string.save_list_title)
	        			.setMessage(R.string.save_list_question)
	                    .setPositiveButton(R.string.confirm, new ButtonClickedListener(Utility.SAVE_LIST_OK))
	                    .setNegativeButton(R.string.dismiss, new ButtonClickedListener(Utility.SAVE_LIST_KO))
	                    .setNeutralButton(R.string.cancel, new ButtonClickedListener(Utility.DISMISS))
	                    .show();
                dialog.setOnKeyListener(new Dialog.OnKeyListener() {
			        @Override
			        public boolean onKey(DialogInterface arg0, int keyCode,
			        		KeyEvent event) {
			        	if (keyCode == KeyEvent.KEYCODE_BACK
			        			&& event.getAction() == KeyEvent.ACTION_UP) {
			        		arg0.dismiss();
			        		setRequestedOrientation(prevOrientation);
			        		return true;
			            }
			            return false;
			        }
		        });
                dialog.setCancelable(false);
		        return true;
			}
			else {
				finish();
				overridePendingTransition(0, R.anim.slide_out_bottom);
				return true;
			}
        }
        return super.onKeyUp(keyCode, event);
    }
	
    private DragSortListView.DropListener onDrop =
            new DragSortListView.DropListener() {
                @Override
                public void drop(int from, int to) {
                    String item = adapter.getItem(from);

                    adapter.remove(item);
                    adapter.insert(item, to);
                    
                    if (modifica) {
//                    	Log.i("SPOSTO CANTO", "da " + from + " a " + to);
                    	String canto = nomiCanti.remove(from);
                    	nomiCanti.add(to, canto);
                    }
                }
            };

    private DragSortListView.RemoveListener onRemove = 
            new DragSortListView.RemoveListener() {
                @Override
                public void remove(int which) {
                    adapter.remove(adapter.getItem(which));
                    
                    if (modifica) {
                    	nomiCanti.remove(which);
//                    	Log.i("RIMOSSO", which + "");

                    }
                    if (adapter.getCount() == 0)
                    	findViewById(R.id.noElementsAdded).setVisibility(View.VISIBLE);
                }
            };
	
    private boolean saveList()  {
		celebrazione = new ListaPersonalizzata();
		
		if (((TintEditText)findViewById(R.id.textfieldTitle)).getText() != null
				&& !((TintEditText)findViewById(R.id.textfieldTitle)).getText()
					.toString().trim().equalsIgnoreCase("")) {
    		titoloLista = ((TintEditText)findViewById(R.id.textfieldTitle)).getText().toString();
		}
		else {
    		Toast toast = Toast.makeText(CreaListaActivity.this
    				, getString(R.string.no_title_edited), Toast.LENGTH_SHORT);
    		toast.show();
		}
		
		celebrazione.setName(titoloLista);
		for (int i = 0; i < nomiElementi.size(); i++) {
			if (celebrazione.addPosizione(nomiElementi.get(i)) == -2) {
	    		Toast toast = Toast.makeText(getApplicationContext()
	    				, getString(R.string.lista_pers_piena), Toast.LENGTH_LONG);
	    		toast.show();
	    		return false;
			}
		}
		
		if (celebrazione.getNomePosizione(0).equalsIgnoreCase("")) {
    		Toast toast = Toast.makeText(getApplicationContext()
    				, getString(R.string.lista_pers_vuota), Toast.LENGTH_LONG);
    		toast.show();
    		return false;
		}
		
		if (modifica) {
    		for (int i = 0; i < nomiElementi.size(); i++) {
//    			Log.i("SALVO CANTO", nomiCanti.get(i));
    			celebrazione.addCanto(nomiCanti.get(i), i);
    		}
		}
		
    	SQLiteDatabase db = listaCanti.getReadableDatabase();
    	
    	ContentValues  values = new  ContentValues( );
    	values.put("titolo_lista" , titoloLista);
    	values.put("lista" , ListaPersonalizzata.serializeObject(celebrazione));
    	
    	if (modifica)
    		db.update("LISTE_PERS", values, "_id = " + idModifica, null);
    	else
    		db.insert("LISTE_PERS" , "" , values);
    	
    	db.close();
    	return true;
    }
            
            
    @Override
    public void onResume() {
    	super.onResume();
    	checkScreenAwake();
    }
    
	@Override
	public void onDestroy() {
		if (listaCanti != null)
			listaCanti.close();
		super.onDestroy();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		
		dataFragment = new RetainedFragment();
		getSupportFragmentManager().beginTransaction().add(dataFragment, "nomiElementi").commit();
		dataFragment.setData(nomiElementi);
	  
		if (modifica) {
			dataFragment2 = new RetainedFragment();
			getSupportFragmentManager().beginTransaction().add(dataFragment2, "nomiCanti").commit();
			dataFragment2.setData(nomiCanti);
		}
	  
		dataFragment3 = new RetainedFragment();
		tempArgs = new Bundle();
		tempArgs.putCharSequence(TEMP_TITLE, ((TintEditText)findViewById(R.id.textfieldTitle)).getText());
		dataFragment3.setArguments(tempArgs);
		getSupportFragmentManager().beginTransaction().add(dataFragment3, TEMP_TITLE).commit();
	  
		super.onSaveInstanceState(savedInstanceState);
	}
	
    //controlla se l'app deve mantenere lo schermo acceso
    public void checkScreenAwake() {
    	SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(this);
		boolean screenOn = pref.getBoolean(Utility.SCREEN_ON, false);
		if (screenOn)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		else
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
				setRequestedOrientation(prevOrientation);
				break;
			case Utility.RENAME_CONFERMA:
				nomiElementi.set(positionToRename, titleInputRename.getText().toString());
	            adapter.notifyDataSetChanged();
	            setRequestedOrientation(prevOrientation);
				break;
			case Utility.AGGIUNGI_CONFERMA:
				findViewById(R.id.noElementsAdded).setVisibility(View.GONE);
	    		nomiElementi.add(titleInputAdd.getText().toString());
	    		if (modifica)
	    			nomiCanti.add("");
	            adapter.notifyDataSetChanged();
	            setRequestedOrientation(prevOrientation);
				break;
			case Utility.SAVE_LIST_OK:
				setRequestedOrientation(prevOrientation);
            	if (saveList()) {
            		finish();
            		overridePendingTransition(0, R.anim.slide_out_bottom);
            	}
				break;
			case Utility.SAVE_LIST_KO:
				setRequestedOrientation(prevOrientation);
        		finish();
        		overridePendingTransition(0, R.anim.slide_out_bottom);
				break;
			default:
				setRequestedOrientation(prevOrientation);
				break;
			}
        }
    }
    
    private class PositionAdapter extends ArrayAdapter<String> {
        public PositionAdapter() {
        	super(getApplicationContext(), positionLI, R.id.position_name, nomiElementi);
        }
    }
    
    public static class RetainedFragment extends Fragment {

        // data object we want to retain
        private ArrayList<String> data;

        // this method is only called once for this fragment
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // retain this fragment
            setRetainInstance(true);
        }

        public void setData(ArrayList<String> data) {
            this.data = data;
        }

        public ArrayList<String> getData() {
            return data;
        }
    }
    
   	private void showHelp() {
   		if (nomiElementi.size() == 0) {
   			findViewById(R.id.noElementsAdded).setVisibility(View.GONE);
   			nomiElementi.add(getResources().getString(R.string.example_title));
   			adapter.notifyDataSetChanged();
   			fakeItemCreated = true;
   		}
   		else {
   			fakeItemCreated = false;
   		}
        prevOrientation = getRequestedOrientation();
        Utility.blockOrientation(CreaListaActivity.this);
	 	lps = new RelativeLayout.LayoutParams(
	 			ViewGroup.LayoutParams.WRAP_CONTENT,
	 			ViewGroup.LayoutParams.WRAP_CONTENT);
	 	lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
	 	lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		int margin = ((Number) (getApplicationContext().getResources().getDisplayMetrics().density * 12)).intValue();
		int marginLeft = ((Number) (getApplicationContext().getResources().getDisplayMetrics().density * 12)).intValue();
		int marginBottom = ((Number) (getApplicationContext().getResources().getDisplayMetrics().density * 12)).intValue();
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
				marginBottom = ((Number) (getApplicationContext().getResources().getDisplayMetrics()
        			.density * 62)).intValue();
			else
				marginLeft = ((Number) (getApplicationContext().getResources().getDisplayMetrics()
        			.density * 62)).intValue();
		}
		lps.setMargins(marginLeft, margin, margin, marginBottom);
		
		ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
		co.buttonLayoutParams = lps;
		
		//benvenuto del tutorial
   		ShowcaseView showcaseView = ShowcaseView.insertShowcaseView(
        		new ViewTarget(R.id.fab_crea_lista, CreaListaActivity.this)
        		, CreaListaActivity.this
        		, R.string.title_activity_nuova_lista
        		, R.string.showcase_welcome_crea
        		, co);
		showcaseView.setShowcase(ShowcaseView.NONE);
		showcaseView.setButtonText(getString(R.string.showcase_button_next));
		showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {
			
			@Override
			public void onShowcaseViewShow(ShowcaseView showcaseView) { }
			
			@Override
			public void onShowcaseViewHide(ShowcaseView showcaseView) {
            	//spiegazione del pulsante aggiungi posizione
        		ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
        		co.buttonLayoutParams = lps;
		   		showcaseView = ShowcaseView.insertShowcaseView(
		        		new ViewTarget(R.id.fab_crea_lista, CreaListaActivity.this)
		        		, CreaListaActivity.this
		        		, R.string.add_position
		        		, R.string.showcase_add_pos_desc
		        		, co);
				showcaseView.setButtonText(getString(R.string.showcase_button_next));
				showcaseView.setScaleMultiplier(0.5f);
				showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {

					@Override
					public void onShowcaseViewShow(ShowcaseView showcaseView) { }
					
					@Override
					public void onShowcaseViewHide(ShowcaseView showcaseView) {
						//spiegazione di come spostare le posizioni
		        		ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
		        		co.buttonLayoutParams = lps;
		        		ViewTarget listItem = new ViewTarget(
		        				adapter.getView(0, lv, lv).findViewById(R.id.drag_handle));
				   		showcaseView = ShowcaseView.insertShowcaseView(
				   				listItem
				        		, CreaListaActivity.this
				        		, R.string.posizione_reorder
				        		, R.string.showcase_reorder_desc
				        		, co);
						showcaseView.setButtonText(getString(R.string.showcase_button_next));
						showcaseView.setScaleMultiplier(0.5f);
						int[] coords = new int[2];
						adapter.getView(0, lv, lv).getLocationOnScreen(coords);
						coords[0] = (coords[0]*2 + 
								adapter.getView(0, lv, lv).findViewById(R.id.drag_handle).getWidth())
								/ 2;
						coords[1] = (coords[1]*2 + 
								adapter.getView(0, lv, lv).findViewById(R.id.drag_handle).getHeight())
								/ 2;
						showcaseView.animateGesture(coords[0], coords[1], coords[0], coords[1] + (screenHeight - coords[1])/3, true);
						showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {

							@Override
							public void onShowcaseViewShow(ShowcaseView showcaseView) { }
							
							@Override
							public void onShowcaseViewHide(ShowcaseView showcaseView) {
								//spiegazione di come rinominare le posizioni
				        		ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
				        		co.buttonLayoutParams = lps;
				        		ViewTarget listItem = new ViewTarget(
				        				adapter.getView(0, lv, lv).findViewById(R.id.position_name));
						   		showcaseView = ShowcaseView.insertShowcaseView(
						   				listItem
						        		, CreaListaActivity.this
						        		, R.string.posizione_rename
						        		, R.string.showcase_rename_desc
						        		, co);
								showcaseView.setButtonText(getString(R.string.showcase_button_next));
								int[] coords = new int[2];
								adapter.getView(0, lv, lv).getLocationOnScreen(coords);
								coords[0] = (coords[0]*2 + 
										adapter.getView(0, lv, lv).findViewById(R.id.position_name).getWidth())
										/ 2;
								coords[1] = (coords[1]*2 + 
										adapter.getView(0, lv, lv).findViewById(R.id.position_name).getHeight())
										/ 2;
								showcaseView.animateGesture(coords[0], coords[1], coords[0], coords[1], true);
								showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {

									@Override
									public void onShowcaseViewShow(ShowcaseView showcaseView) { }
									
									@Override
									public void onShowcaseViewHide(ShowcaseView showcaseView) {
										//spiegazione di come cancellare le posizioni
						        		ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
						        		co.buttonLayoutParams = lps;
						        		ViewTarget listItem = new ViewTarget(
						        				adapter.getView(0, lv, lv).findViewById(R.id.position_name));
								   		showcaseView = ShowcaseView.insertShowcaseView(
								   				listItem
								        		, CreaListaActivity.this
								        		, R.string.posizione_delete
								        		, R.string.showcase_delete_desc
								        		, co);
										showcaseView.setButtonText(getString(R.string.showcase_button_next));
										int[] coords = new int[2];
										adapter.getView(0, lv, lv).getLocationOnScreen(coords);
										coords[0] = (coords[0]*2 + 
												adapter.getView(0, lv, lv).findViewById(R.id.position_name).getWidth())
												/ 2;
										coords[1] = (coords[1]*2 + 
												adapter.getView(0, lv, lv).findViewById(R.id.position_name).getHeight())
												/ 2;
										showcaseView.animateGesture(coords[0], coords[1], coords[0] + (screenWidth - coords[0])/2, coords[1], true);
										showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {

											@Override
											public void onShowcaseViewShow(ShowcaseView showcaseView) { }
											
											@Override
											public void onShowcaseViewHide(ShowcaseView showcaseView) {
												//spiegazione di come salvare
								        		ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
								        		co.buttonLayoutParams = lps;
										   		showcaseView = ShowcaseView.insertShowcaseView(
										        		new ViewTarget(R.id.action_save_list, CreaListaActivity.this)
										        		, CreaListaActivity.this
										        		, R.string.list_save_exit
										        		, R.string.showcase_saveexit_desc
										        		, co);
												showcaseView.setButtonText(getString(R.string.showcase_button_next));
												showcaseView.setScaleMultiplier(0.3f);
												showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {
		
													@Override
													public void onShowcaseViewShow(ShowcaseView showcaseView) { }
													
													@Override
													public void onShowcaseViewHide(ShowcaseView showcaseView) {
														//spiegazione di come rivedere il tutorial
														ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
														co.buttonLayoutParams = lps;	
												   		showcaseView = ShowcaseView.insertShowcaseView(
												        		new ViewTarget(R.id.action_help, CreaListaActivity.this)
												        		, CreaListaActivity.this
												        		, R.string.showcase_end_title
												        		, R.string.showcase_help_general
												        		, co);
												        showcaseView.setScaleMultiplier(0.3f);
														showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {
		
															@Override
															public void onShowcaseViewShow(ShowcaseView showcaseView) { }
															
															@Override
															public void onShowcaseViewHide(ShowcaseView showcaseView) {
																if (fakeItemCreated) {
																	findViewById(R.id.noElementsAdded).setVisibility(View.VISIBLE);
																	nomiElementi.remove(0);
																	adapter.notifyDataSetChanged();
																	fakeItemCreated = false;
																}
																setRequestedOrientation(prevOrientation);
															}		
															@Override
															public void onShowcaseViewDidHide(ShowcaseView showcaseView) { }
														});		
													}
													
													@Override
													public void onShowcaseViewDidHide(ShowcaseView showcaseView) { }
												});
											}
											
											@Override
											public void onShowcaseViewDidHide(ShowcaseView showcaseView) { }
										});
									}			
									@Override
									public void onShowcaseViewDidHide(ShowcaseView showcaseView) { }
								});
							}	
							@Override
							public void onShowcaseViewDidHide(ShowcaseView showcaseView) { }
						});
					}
					@Override
					public void onShowcaseViewDidHide(ShowcaseView showcaseView) { }
				});
            }
			@Override
			public void onShowcaseViewDidHide(ShowcaseView showcaseView) { }
		});
   	}
}