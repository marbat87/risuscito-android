package it.cammino.risuscito;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
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

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import java.util.Locale;

import it.cammino.risuscito.utils.ThemeUtils;

public class ListaPersonalizzataFragment extends Fragment {

	private int posizioneDaCanc;
	private View rootView;
	private ShareActionProvider mShareActionProvider;
	private DatabaseCanti listaCanti;
	private SQLiteDatabase db;
	private int fragmentIndex;
	private int idLista;
	private ListaPersonalizzata listaPersonalizzata;
//	private int prevOrientation;

	private LUtils mLUtils;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		rootView = inflater.inflate(
				R.layout.activity_lista_personalizzata, container, false);

		//crea un istanza dell'oggetto DatabaseCanti
		listaCanti = new DatabaseCanti(getActivity());

//		FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_personalizzata);
//		fab.setColorNormal(getThemeUtils().accentColor());
//		fab.setColorPressed(getThemeUtils().accentColorDark());
//		fab.setColorRipple(getThemeUtils().accentColorDark());
//		fab.attachToScrollView((ObservableScrollView) rootView.findViewById(R.id.personalizzataScrollView));
//		fab.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				prevOrientation = getActivity().getRequestedOrientation();
//				Utility.blockOrientation(getActivity());
////                AlertDialogPro.Builder builder = new AlertDialogPro.Builder(getActivity());
////                AlertDialogPro dialog = builder.setTitle(R.string.dialog_reset_list_title)
////	        			.setMessage(R.string.reset_list_question)
////	                    .setPositiveButton(R.string.confirm, new ButtonClickedListener(Utility.PERS_RESET_OK))
////	                    .setNegativeButton(R.string.dismiss, new ButtonClickedListener(Utility.DISMISS))
////	                    .show();
//				MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
//						.title(R.string.dialog_reset_list_title)
//						.content(R.string.reset_list_question)
//						.positiveText(R.string.confirm)
//						.negativeText(R.string.dismiss)
//						.callback(new MaterialDialog.ButtonCallback() {
//							@Override
//							public void onPositive(MaterialDialog dialog) {
//								db = listaCanti.getReadableDatabase();
//								ContentValues  values = new  ContentValues( );
//								for (int i = 0; i < listaPersonalizzata.getNumPosizioni(); i++)
//									listaPersonalizzata.removeCanto(i);
//								values.put("lista" , ListaPersonalizzata.serializeObject(listaPersonalizzata));
//								db.update("LISTE_PERS", values, "_id = " + idLista, null);
//								db.close();
//								updateLista();
//								mShareActionProvider.setShareIntent(getDefaultIntent());
//								getActivity().setRequestedOrientation(prevOrientation);
//							}
//
//							@Override
//							public void onNegative(MaterialDialog dialog) {
//								getActivity().setRequestedOrientation(prevOrientation);
//							}
//						})
//						.show();
//				dialog.setOnKeyListener(new Dialog.OnKeyListener() {
//					@Override
//					public boolean onKey(DialogInterface arg0, int keyCode,
//										 KeyEvent event) {
//						if (keyCode == KeyEvent.KEYCODE_BACK
//								&& event.getAction() == KeyEvent.ACTION_UP) {
//							arg0.dismiss();
//							getActivity().setRequestedOrientation(prevOrientation);
//							return true;
//						}
//						return false;
//					}
//				});
//				dialog.setCancelable(false);
//			}
//		});

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

		((ObservableScrollView) rootView.findViewById(R.id.personalizzataScrollView)).setScrollViewCallbacks(new ObservableScrollViewCallbacks() {
			@Override
			public void onScrollChanged(int i, boolean b, boolean b1) {}

			@Override
			public void onDownMotionEvent() {}

			@Override
			public void onUpOrCancelMotionEvent(ScrollState scrollState) {
				FloatingActionsMenu fab1 = ((CustomLists) getParentFragment()).getFab1();
//                Log.i(getClass().toString(), "scrollState: " + scrollState);
				if (scrollState == ScrollState.UP) {
					if (fab1.isVisible())
						fab1.hide();
				} else if (scrollState == ScrollState.DOWN) {
					if (!fab1.isVisible())
						fab1.show();
				}
			}
		});

//		setHasOptionsMenu(true);

		mLUtils = LUtils.getInstance(getActivity());

		return rootView;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			((CustomLists) getParentFragment()).fabDelete.setEnabled(true);
			((CustomLists) getParentFragment()).fabEdit.setEnabled(true);
			if (LUtils.hasHoneycomb()) {
				((CustomLists) getParentFragment()).fabDelete.setVisibility(View.VISIBLE);
				((CustomLists) getParentFragment()).fabEdit.setVisibility(View.VISIBLE);
			}
			FloatingActionsMenu fab1 = ((CustomLists) getParentFragment()).getFab1();
			if (!fab1.isVisible())
				fab1.show();
//			FloatingActionMenu fab2 = ((CustomLists) getParentFragment()).getFab2();
//			if (!fab1.isMenuButtonHidden()) {
//				fab1.hideMenuButton(false);
//				fab2.showMenuButton(false);
//			}
//			else
//				fab2.showMenuButton(true);
//			if (LUtils.hasHoneycomb()) {
//				if (fab1.isVisible()) {
//					fab1.hide(false);
//					fab2.show(false);
//				} else
//					fab2.show();
//			}
//			else {
//				fab1.setVisibility(View.GONE);
//				fab2.setVisibility(View.VISIBLE);
//			}
		}
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onResume() {
//		Log.i("LISTA PERS", "ON RESUME");
		super.onResume();
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

	@Override
	public void onDestroy() {
		if (listaCanti != null)
			listaCanti.close();
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
		String cantoCliccato = ((TextView) v.findViewById(R.id.text_title)).getText().toString();
		cantoCliccato = Utility.duplicaApostrofi(cantoCliccato);

		// crea un manipolatore per il DB in modalità READ
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

//		Log.i("POSITION", fragmentIndex+" ");
//		Log.i("IDLISTA", idLista+" ");
//		Log.i("TITOLO", listaPersonalizzata.getName());

		LinearLayout linLayout = (LinearLayout) rootView.findViewById(R.id.listaScroll);
		linLayout.removeAllViews();

		for (int cantoIndex = 0; cantoIndex < listaPersonalizzata.getNumPosizioni(); cantoIndex++) {
			View view = getActivity().getLayoutInflater().inflate(R.layout.oggetto_lista_generico, linLayout, false);

			((TextView) view.findViewById(R.id.titoloPosizioneGenerica))
					.setText(listaPersonalizzata.getNomePosizione(cantoIndex));

			((TextView) view.findViewById(R.id.id_posizione))
					.setText(String.valueOf(cantoIndex));

//	   		Log.i("CANTO[" + cantoIndex + "]", listaPersonalizzata.getCantoPosizione(cantoIndex) + " ");

			if (listaPersonalizzata.getCantoPosizione(cantoIndex).length() == 0) {

				view.findViewById(R.id.addCantoGenerico).setVisibility(View.VISIBLE);
				view.findViewById(R.id.cantoGenericoContainer).setVisibility(View.GONE);

				view.findViewById(R.id.addCantoGenerico).setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Bundle bundle = new Bundle();
						bundle.putInt("fromAdd", 0);
						bundle.putInt("idLista", idLista);
						bundle.putInt("position", (Integer.valueOf(
								((TextView) v.findViewById(R.id.id_posizione))
										.getText().toString())));
						Intent intent = new Intent(getActivity(), GeneralInsertSearch.class);
						intent.putExtras(bundle);
						startActivity(intent);
						getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.hold_on);
					}
				});

			}
			else {

				//setto l'id del canto nell'apposito canto
				((TextView) view.findViewById(R.id.id_da_canc))
						.setText(String.valueOf(cantoIndex));

				view.findViewById(R.id.addCantoGenerico).setVisibility(View.GONE);
				View temp = view.findViewById(R.id.cantoGenericoContainer);
				temp.setVisibility(View.VISIBLE);
				temp.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						openPagina(v);
					}
				});
				// setta l'azione tenendo premuto sul canto
				temp.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View view) {
						posizioneDaCanc = Integer.valueOf(
								((TextView) ((ViewGroup) view.getParent()).findViewById(R.id.id_da_canc))
										.getText().toString());
//						Log.i("canto da rimuovere", posizioneDaCanc + " ");
						snackBarRimuoviCanto();
						return true;
					}
				});

				db = listaCanti.getReadableDatabase();

				String query = "SELECT titolo, pagina, color" +
						"  FROM ELENCO" +
						"  WHERE _id =  " + listaPersonalizzata.getCantoPosizione(cantoIndex);
				Cursor cursor = db.rawQuery(query, null);
				cursor.moveToFirst();

				//setto il titolo del canto
//                ((TextView) view.findViewById(R.id.text_title))
//                    .setText(listaPersonalizzata.getCantoPosizione(cantoIndex).substring(10));
//
//                //setto la pagina
//                int tempPagina = Integer.valueOf(listaPersonalizzata.getCantoPosizione(cantoIndex).substring(0, 3));
//                String pagina = String.valueOf(tempPagina);
//                TextView textPage = (TextView) view.findViewById(R.id.text_page);
//                textPage.setText(pagina);
//
//                //setto il colore
//                String colore = listaPersonalizzata.getCantoPosizione(cantoIndex).substring(3, 10);
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

				//setto il titolo del canto
				((TextView) view.findViewById(R.id.text_title))
						.setText(cursor.getString(0));

				//setto la pagina
				int tempPagina = cursor.getInt(1);
				String pagina = String.valueOf(tempPagina);
				TextView textPage = (TextView) view.findViewById(R.id.text_page);
				textPage.setText(pagina);

				//setto il colore
				String colore = cursor.getString(2);
				if (colore.equalsIgnoreCase(Utility.GIALLO))
					textPage.setBackgroundResource(R.drawable.bkg_round_yellow);
				if (colore.equalsIgnoreCase(Utility.GRIGIO))
					textPage.setBackgroundResource(R.drawable.bkg_round_grey);
				if (colore.equalsIgnoreCase(Utility.VERDE))
					textPage.setBackgroundResource(R.drawable.bkg_round_green);
				if (colore.equalsIgnoreCase(Utility.AZZURRO))
					textPage.setBackgroundResource(R.drawable.bkg_round_blue);
				if (colore.equalsIgnoreCase(Utility.BIANCO))
					textPage.setBackgroundResource(R.drawable.bkg_round_white);

				cursor.close();
				db.close();

			}

			linLayout.addView(view);
		}

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
				db = listaCanti.getReadableDatabase();

				String query = "SELECT titolo, pagina" +
						"  FROM ELENCO" +
						"  WHERE _id =  " + listaPersonalizzata.getCantoPosizione(i);
				Cursor cursor = db.rawQuery(query, null);
				cursor.moveToFirst();

				result += cursor.getString(0)
						+ " - " + getString(R.string.page_contracted) + cursor.getInt(1);

				cursor.close();
				db.close();
			}
			else
				result += ">> " + getString(R.string.to_be_chosen) + " <<";
			if (i < listaPersonalizzata.getNumPosizioni() - 1)
				result += "\n";
		}

		return result;

	}

//    private class ButtonClickedListener implements DialogInterface.OnClickListener {
//        private int clickedCode;
//
//        public ButtonClickedListener(int code) {
//        	clickedCode = code;
//        }
//
//        @Override
//        public void onClick(DialogInterface dialog, int which) {
//            switch (clickedCode) {
//			case Utility.DISMISS:
//				getActivity().setRequestedOrientation(prevOrientation);
//				break;
//			case Utility.PERS_RESET_OK:
//				db = listaCanti.getReadableDatabase();
//            	ContentValues  values = new  ContentValues( );
//            	for (int i = 0; i < listaPersonalizzata.getNumPosizioni(); i++)
//            		listaPersonalizzata.removeCanto(i);
//            	values.put("lista" , ListaPersonalizzata.serializeObject(listaPersonalizzata));
//            	db.update("LISTE_PERS", values, "_id = " + idLista, null );
//        		db.close();
//        		updateLista();
//        		mShareActionProvider.setShareIntent(getDefaultIntent());
//        		getActivity().setRequestedOrientation(prevOrientation);
//			default:
//				getActivity().setRequestedOrientation(prevOrientation);
//				break;
//			}
//        }
//    }

	public void snackBarRimuoviCanto() {
//		SnackbarManager.show(
//				Snackbar.with(getActivity())
//						.text(getString(R.string.list_remove))
//						.actionLabel(getString(R.string.snackbar_remove))
//						.actionListener(new ActionClickListener() {
//							@Override
//							public void onActionClicked(Snackbar snackbar) {
//								db = listaCanti.getReadableDatabase();
//								ContentValues  values = new  ContentValues( );
//								listaPersonalizzata.removeCanto(posizioneDaCanc);
//								values.put("lista" , ListaPersonalizzata.serializeObject(listaPersonalizzata));
//								db.update("LISTE_PERS", values, "_id = " + idLista, null );
//								db.close();
//								updateLista();
//								mShareActionProvider.setShareIntent(getDefaultIntent());
//							}
//						})
//						.actionColor(getThemeUtils().accentColor())
//				, getActivity());
		Snackbar.make(rootView, R.string.list_remove, Snackbar.LENGTH_LONG)
				.setAction(R.string.snackbar_remove, new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						db = listaCanti.getReadableDatabase();
						ContentValues  values = new  ContentValues( );
						listaPersonalizzata.removeCanto(posizioneDaCanc);
						values.put("lista" , ListaPersonalizzata.serializeObject(listaPersonalizzata));
						db.update("LISTE_PERS", values, "_id = " + idLista, null );
						db.close();
						updateLista();
						mShareActionProvider.setShareIntent(getDefaultIntent());
					}
				})
				.setActionTextColor(getThemeUtils().accentColor())
				.show();
	}

	private ThemeUtils getThemeUtils() {
		return ((MainActivity)getActivity()).getThemeUtils();
	}

}