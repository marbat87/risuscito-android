package it.cammino.risuscito;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import java.util.ArrayList;

import it.cammino.risuscito.adapters.DraggableSwipeableAdapter;
import it.cammino.risuscito.objects.DraggableItem;
import it.cammino.risuscito.slides.IntroCreaLista;
import it.cammino.risuscito.ui.ThemeableActivity;

public class CreaListaActivity extends ThemeableActivity {

    private ListaPersonalizzata celebrazione;
    private DatabaseCanti listaCanti;
    //	private PositionAdapter adapter;
//	private ArrayList<String> nomiElementi;
    private ArrayList<DraggableItem> elementi;
    private String titoloLista;
    //	private DragSortListView lv;
    private int prevOrientation;
    private boolean modifica;
    private int idModifica;
    private RetainedFragment dataFragment;
    private RetainedFragment dataFragment2;
    private RetainedFragment dataFragment3;
    //	private RelativeLayout.LayoutParams lps;
//	private boolean fakeItemCreated;
//	private int screenWidth;
//	private int screenHeight;
    private ArrayList<String> nomiCanti;
    //	private int positionLI;
    private Bundle tempArgs;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;

    //    private FloatingActionButton mFab;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private EditText textfieldTitle;

    private static final String PREF_FIRST_OPEN = "prima_apertura_crealista_v2";

    private final String TEMP_TITLE = "temp_title";

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crea_lista);

        Toolbar toolbar = (Toolbar) findViewById(R.id.risuscito_toolbar);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setBackgroundColor(getThemeUtils().primaryColor());
        setSupportActionBar(toolbar);
        findViewById(R.id.action_title_bar).setBackgroundColor(getThemeUtils().primaryColor());

        listaCanti = new DatabaseCanti(this);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbarLayout);
        textfieldTitle = (EditText)findViewById(R.id.textfieldTitle);

        textfieldTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                collapsingToolbarLayout.setTitle(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

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

//		lv = (DragSortListView) findViewById(android.R.id.list);
//
//        lv.setDropListener(onDrop);
//        lv.setRemoveListener(onRemove);

        dataFragment = (RetainedFragment) getSupportFragmentManager().findFragmentByTag("nomiElementi");
        if (dataFragment != null) {
//            nomiElementi = dataFragment.getData();
            elementi = dataFragment.getDataDrag();
        }
        else {
//        	nomiElementi = new ArrayList<String>();
            elementi = new ArrayList<>();
            if (modifica) {
                for (int i = 0; i < celebrazione.getNumPosizioni(); i++)
//	        		nomiElementi.add(celebrazione.getNomePosizione(i));
                    elementi.add(new DraggableItem(celebrazione.getNomePosizione(i), Utility.random(1,500)));
            }
        }

        if (modifica) {
            dataFragment2 = (RetainedFragment) getSupportFragmentManager().findFragmentByTag("nomiCanti");
            if (dataFragment2 != null) {
                nomiCanti = dataFragment2.getData();
            }
            else {
                nomiCanti = new ArrayList<>();
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
            textfieldTitle.setText(tempArgs.getCharSequence(TEMP_TITLE));
            collapsingToolbarLayout.setTitle(tempArgs.getCharSequence(TEMP_TITLE));
        }
        else {
            textfieldTitle.setText(titoloLista);
            collapsingToolbarLayout.setTitle(titoloLista);
        }


//        positionLI = R.layout.position_list_item_light;
//
//        adapter = new PositionAdapter();
//        lv.setAdapter(adapter);
//
//		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
//			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//				prevOrientation = getRequestedOrientation();
//				Utility.blockOrientation(CreaListaActivity.this);
//				final int positionToRename = position;
//				final MaterialDialog dialog = new MaterialDialog.Builder(CreaListaActivity.this)
//						.title(R.string.posizione_rename)
//						.positiveText(R.string.aggiungi_rename)
//						.negativeText(R.string.aggiungi_dismiss)
//						.input("", "", false, new MaterialDialog.InputCallback() {
//							@Override
//							public void onInput(MaterialDialog dialog, CharSequence input) {
//							}
//						})
//						.callback(new MaterialDialog.ButtonCallback() {
//							@Override
//							public void onPositive(MaterialDialog dialog) {
//								nomiElementi.set(positionToRename, dialog.getInputEditText().getText().toString());
//								adapter.notifyDataSetChanged();
//								//to hide soft keyboard
//								((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
//										.hideSoftInputFromWindow(dialog.getInputEditText().getWindowToken(), 0);
//								setRequestedOrientation(prevOrientation);
//							}
//
//							@Override
//							public void onNegative(MaterialDialog dialog) {
//								//to hide soft keyboard
//								((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
//										.hideSoftInputFromWindow(dialog.getInputEditText().getWindowToken(), 0);
//								setRequestedOrientation(prevOrientation);
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
//							setRequestedOrientation(prevOrientation);
//							return true;
//						}
//						return false;
//					}
//				});
//				dialog.getInputEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
//				dialog.getInputEditText().setText(nomiElementi.get(positionToRename));
//				dialog.getInputEditText().selectAll();
//				dialog.setCancelable(false);
//				//to show soft keyboard
//				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
//						.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
//				return true;
//			}
//		});

        //noinspection ConstantConditions
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(CreaListaActivity.this);

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        // drag & drop manager
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        if (LUtils.hasL())
            mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(
                    (NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z3, getTheme()));
        else
            mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(
                    (NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z3));

        // swipe manager
        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        //adapter
        final DraggableSwipeableAdapter myItemAdapter = new DraggableSwipeableAdapter(CreaListaActivity.this
                ,elementi);
        myItemAdapter.setEventListener(new DraggableSwipeableAdapter.EventListener() {
            @Override
            public void onItemRemoved(int position) {
//				((DraggableSwipeableExampleActivity) getActivity()).onItemRemoved(position);
                if (modifica) {
                    nomiCanti.remove(position);
//                    	Log.i("RIMOSSO", which + "");
                }
                if (mAdapter.getItemCount() == 0)
                    findViewById(R.id.noElementsAdded).setVisibility(View.VISIBLE);
            }

            @Override
            public void onItemMoved(int from, int to)  {
                if (modifica) {
//                    	Log.i("SPOSTO CANTO", "da " + from + " a " + to);
                    nomiCanti.add(to, nomiCanti.remove(from));
                }
            }

            @Override
            public void onItemViewLongClicked(View v) {
                prevOrientation = getRequestedOrientation();
                Utility.blockOrientation(CreaListaActivity.this);
                final int positionToRename = mRecyclerView.getChildAdapterPosition(v);
                final MaterialDialog dialog = new MaterialDialog.Builder(CreaListaActivity.this)
                        .title(R.string.posizione_rename)
                        .positiveText(R.string.aggiungi_rename)
                        .negativeText(R.string.aggiungi_dismiss)
                        .input("", "", false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                            }
                        })
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                elementi.set(positionToRename, new DraggableItem(dialog.getInputEditText().getText().toString()
                                        , elementi.get(positionToRename).getIdPosizione()));
                                mAdapter.notifyDataSetChanged();
                                //to hide soft keyboard
                                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                                        .hideSoftInputFromWindow(dialog.getInputEditText().getWindowToken(), 0);
                                setRequestedOrientation(prevOrientation);
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                //to hide soft keyboard
                                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                                        .hideSoftInputFromWindow(dialog.getInputEditText().getWindowToken(), 0);
                                setRequestedOrientation(prevOrientation);
                            }
                        })
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
                dialog.getInputEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                dialog.getInputEditText().setText(elementi.get(positionToRename).getTitolo());
                dialog.getInputEditText().selectAll();
                dialog.setCancelable(false);
                //to show soft keyboard
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }

        });

        mAdapter = myItemAdapter;

        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(myItemAdapter);      // wrap for dragging
        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(mWrappedAdapter);      // wrap for swiping

        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();

        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        animator.setSupportsChangeAnimations(false);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(animator);

        // additional decorations
        //noinspection StatementWithEmptyBody
//		if (supportsViewElevation()) {
//			// Lollipop or later has native drop shadow feature. ItemShadowDecorator is not required.
//		} else {
//			mRecyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z1)));
//		}
        if (LUtils.hasL())
            mRecyclerView.addItemDecoration(
                    new SimpleListDividerDecorator(getResources().getDrawable(R.drawable.list_divider, getTheme()), true));
        else
            mRecyclerView.addItemDecoration(
                    new SimpleListDividerDecorator(getResources().getDrawable(R.drawable.list_divider), true));

        // NOTE:
        // The initialization order is very important! This order determines the priority of touch event handling.
        //
        // priority: TouchActionGuard > Swipe > DragAndDrop
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewSwipeManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);

//        getFab();
        findViewById(R.id.fab_crea_lista).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                prevOrientation = getRequestedOrientation();
                Utility.blockOrientation(CreaListaActivity.this);
                final MaterialDialog dialogAdd = new MaterialDialog.Builder(CreaListaActivity.this)
                        .title(R.string.posizione_add_desc)
                        .positiveText(R.string.aggiungi_confirm)
                        .negativeText(R.string.aggiungi_dismiss)
                        .input("", "", false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                            }
                        })
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                findViewById(R.id.noElementsAdded).setVisibility(View.GONE);
                                elementi.add(new DraggableItem(dialog.getInputEditText().getText().toString(), Utility.random(1, 500)));
                                if (modifica)
                                    nomiCanti.add("");
                                mAdapter.notifyItemInserted(elementi.size() - 1);
                                //to hide soft keyboard
                                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                                        .hideSoftInputFromWindow(dialog.getInputEditText().getWindowToken(), 0);
                                setRequestedOrientation(prevOrientation);
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                //to hide soft keyboard
                                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                                        .hideSoftInputFromWindow(dialog.getInputEditText().getWindowToken(), 0);
                                setRequestedOrientation(prevOrientation);
                            }
                        })
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
                dialogAdd.getInputEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                dialogAdd.setCancelable(false);
                //to show soft keyboard
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });

//        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                float y = recyclerView.getScrollY();
//                super.onScrolled(recyclerView, dx, dy);
//                if (y < dy)
//                    getFab().hide();
//                else
//                    getFab().show();
//            }
//
//        });

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_crea_lista);
//        fab.setColorNormal(getThemeUtils().accentColor());
//        fab.setColorPressed(getThemeUtils().accentColorDark());
//        fab.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                prevOrientation = getRequestedOrientation();
//                Utility.blockOrientation(CreaListaActivity.this);
//                final MaterialDialog dialogAdd = new MaterialDialog.Builder(CreaListaActivity.this)
//                        .title(R.string.posizione_add_desc)
//                        .positiveText(R.string.aggiungi_confirm)
//                        .negativeText(R.string.aggiungi_dismiss)
//                        .input("", "", false, new MaterialDialog.InputCallback() {
//                            @Override
//                            public void onInput(MaterialDialog dialog, CharSequence input) {
//                            }
//                        })
//                        .callback(new MaterialDialog.ButtonCallback() {
//                            @Override
//                            public void onPositive(MaterialDialog dialog) {
//                                findViewById(R.id.noElementsAdded).setVisibility(View.GONE);
////								nomiElementi.add(dialog.getInputEditText().getText().toString());
//                                elementi.add(new DraggableItem(dialog.getInputEditText().getText().toString(), Utility.random(1, 500)));
//                                if (modifica)
//                                    nomiCanti.add("");
////								adapter.notifyDataSetChanged();
//                                mAdapter.notifyItemInserted(elementi.size() - 1);
//                                //to hide soft keyboard
//                                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
//                                        .hideSoftInputFromWindow(dialog.getInputEditText().getWindowToken(), 0);
//                                setRequestedOrientation(prevOrientation);
//                            }
//
//                            @Override
//                            public void onNegative(MaterialDialog dialog) {
//                                //to hide soft keyboard
//                                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
//                                        .hideSoftInputFromWindow(dialog.getInputEditText().getWindowToken(), 0);
//                                setRequestedOrientation(prevOrientation);
//                            }
//                        })
//                        .show();
//                dialogAdd.setOnKeyListener(new Dialog.OnKeyListener() {
//                    @Override
//                    public boolean onKey(DialogInterface arg0, int keyCode,
//                                         KeyEvent event) {
//                        if (keyCode == KeyEvent.KEYCODE_BACK
//                                && event.getAction() == KeyEvent.ACTION_UP) {
//                            arg0.dismiss();
//                            setRequestedOrientation(prevOrientation);
//                            return true;
//                        }
//                        return false;
//                    }
//                });
//                dialogAdd.getInputEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
//                dialogAdd.setCancelable(false);
//                //to show soft keyboard
//                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
//                        .toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
//            }
//        });

//		if (nomiElementi.size() > 0)
        if (elementi.size() > 0)
            findViewById(R.id.noElementsAdded).setVisibility(View.GONE);

//		Display display = getWindowManager().getDefaultDisplay();
//		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
//			screenWidth = display.getWidth();
//			screenHeight = display.getHeight();
//		}
//		else {
//			Point size = new Point();
//			display.getSize(size);
//			screenWidth = size.x;
//			screenHeight = size.y;
//		}

        if(PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean(PREF_FIRST_OPEN, true)) {
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(CreaListaActivity.this)
                    .edit();
            editor.putBoolean(PREF_FIRST_OPEN, false);
            editor.apply();
            showHelp();
        }

        findViewById(R.id.textTitleDescription).requestFocus();

    }

    @Override
    public void onPause() {
        mRecyclerViewDragDropManager.cancelDrag();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
        }

        if (mRecyclerViewSwipeManager != null) {
            mRecyclerViewSwipeManager.release();
            mRecyclerViewSwipeManager = null;
        }

        if (mRecyclerViewTouchActionGuardManager != null) {
            mRecyclerViewTouchActionGuardManager.release();
            mRecyclerViewTouchActionGuardManager = null;
        }

        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        mAdapter = null;
        mLayoutManager = null;

        if (listaCanti != null)
            listaCanti.close();

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
                    setResult(Activity.RESULT_OK);
                    finish();
                    overridePendingTransition(0, R.anim.slide_out_bottom);
                }
                return true;
            case android.R.id.home:
//				if (nomiElementi.size() > 0) {
                if (elementi.size() > 0) {
                    prevOrientation = getRequestedOrientation();
                    Utility.blockOrientation(CreaListaActivity.this);
                    MaterialDialog dialog = new MaterialDialog.Builder(this)
                            .title(R.string.save_list_title)
                            .content(R.string.save_list_question)
                            .positiveText(R.string.confirm)
                            .negativeText(R.string.dismiss)
                            .neutralText(R.string.cancel)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    setRequestedOrientation(prevOrientation);
                                    if (saveList()) {
                                        setResult(Activity.RESULT_OK);
                                        finish();
                                        overridePendingTransition(0, R.anim.slide_out_bottom);
                                    }
                                }

                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    setRequestedOrientation(prevOrientation);
                                    setResult(Activity.RESULT_CANCELED);
                                    finish();
                                    overridePendingTransition(0, R.anim.slide_out_bottom);
                                }

                                @Override
                                public void onNeutral(MaterialDialog dialog) {
                                    setRequestedOrientation(prevOrientation);
                                }
                            })
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
                    setResult(Activity.RESULT_CANCELED);
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
//			if (nomiElementi.size() > 0) {
            if (elementi.size() > 0) {
                prevOrientation = getRequestedOrientation();
                Utility.blockOrientation(CreaListaActivity.this);
                MaterialDialog dialog = new MaterialDialog.Builder(this)
                        .title(R.string.save_list_title)
                        .content(R.string.save_list_question)
                        .positiveText(R.string.confirm)
                        .negativeText(R.string.dismiss)
                        .neutralText(R.string.cancel)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                setRequestedOrientation(prevOrientation);
                                if (saveList()) {
                                    setResult(Activity.RESULT_OK);
                                    finish();
                                    overridePendingTransition(0, R.anim.slide_out_bottom);
                                }
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                setRequestedOrientation(prevOrientation);
                                setResult(Activity.RESULT_CANCELED);
                                finish();
                                overridePendingTransition(0, R.anim.slide_out_bottom);
                            }

                            @Override
                            public void onNeutral(MaterialDialog dialog) {
                                setRequestedOrientation(prevOrientation);
                            }
                        })
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
                setResult(Activity.RESULT_CANCELED);
                finish();
                overridePendingTransition(0, R.anim.slide_out_bottom);
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

//	private DragSortListView.DropListener onDrop =
//			new DragSortListView.DropListener() {
//				@Override
//				public void drop(int from, int to) {
//					String item = adapter.getItem(from);
//
//					adapter.remove(item);
//					adapter.insert(item, to);
//
//					if (modifica) {
////                    	Log.i("SPOSTO CANTO", "da " + from + " a " + to);
//						String canto = nomiCanti.remove(from);
//						nomiCanti.add(to, canto);
//					}
//				}
//			};

//	private DragSortListView.RemoveListener onRemove =
//			new DragSortListView.RemoveListener() {
//				@Override
//				public void remove(int which) {
//					adapter.remove(adapter.getItem(which));
//
//					if (modifica) {
//						nomiCanti.remove(which);
////                    	Log.i("RIMOSSO", which + "");
//
//					}
//					if (adapter.getCount() == 0)
//						findViewById(R.id.noElementsAdded).setVisibility(View.VISIBLE);
//				}
//			};

//    public FloatingActionButton getFab() {
//        if (mFab == null) {
//            mFab = (FloatingActionButton) findViewById(R.id.fab_crea_lista);
//            mFab.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    prevOrientation = getRequestedOrientation();
//                    Utility.blockOrientation(CreaListaActivity.this);
//                    final MaterialDialog dialogAdd = new MaterialDialog.Builder(CreaListaActivity.this)
//                            .title(R.string.posizione_add_desc)
//                            .positiveText(R.string.aggiungi_confirm)
//                            .negativeText(R.string.aggiungi_dismiss)
//                            .input("", "", false, new MaterialDialog.InputCallback() {
//                                @Override
//                                public void onInput(MaterialDialog dialog, CharSequence input) {
//                                }
//                            })
//                            .callback(new MaterialDialog.ButtonCallback() {
//                                @Override
//                                public void onPositive(MaterialDialog dialog) {
//                                    findViewById(R.id.noElementsAdded).setVisibility(View.GONE);
//                                    elementi.add(new DraggableItem(dialog.getInputEditText().getText().toString(), Utility.random(1, 500)));
//                                    if (modifica)
//                                        nomiCanti.add("");
//                                    mAdapter.notifyItemInserted(elementi.size() - 1);
//                                    //to hide soft keyboard
//                                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
//                                            .hideSoftInputFromWindow(dialog.getInputEditText().getWindowToken(), 0);
//                                    setRequestedOrientation(prevOrientation);
//                                }
//
//                                @Override
//                                public void onNegative(MaterialDialog dialog) {
//                                    //to hide soft keyboard
//                                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
//                                            .hideSoftInputFromWindow(dialog.getInputEditText().getWindowToken(), 0);
//                                    setRequestedOrientation(prevOrientation);
//                                }
//                            })
//                            .show();
//                    dialogAdd.setOnKeyListener(new Dialog.OnKeyListener() {
//                        @Override
//                        public boolean onKey(DialogInterface arg0, int keyCode,
//                                             KeyEvent event) {
//                            if (keyCode == KeyEvent.KEYCODE_BACK
//                                    && event.getAction() == KeyEvent.ACTION_UP) {
//                                arg0.dismiss();
//                                setRequestedOrientation(prevOrientation);
//                                return true;
//                            }
//                            return false;
//                        }
//                    });
//                    dialogAdd.getInputEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
//                    dialogAdd.setCancelable(false);
//                    //to show soft keyboard
//                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
//                            .toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
//                }
//            });
//        }
//        return mFab;
//    }

    private boolean saveList()  {
        celebrazione = new ListaPersonalizzata();

        if (textfieldTitle.getText() != null
                && !textfieldTitle.getText()
                .toString().trim().equalsIgnoreCase("")) {
            titoloLista = textfieldTitle.getText().toString();
        }
        else {
            Toast toast = Toast.makeText(CreaListaActivity.this
                    , getString(R.string.no_title_edited), Toast.LENGTH_SHORT);
            toast.show();
        }

        celebrazione.setName(titoloLista);
//		for (int i = 0; i < nomiElementi.size(); i++) {
        for (int i = 0; i < elementi.size(); i++) {
//			if (celebrazione.addPosizione(nomiElementi.get(i)) == -2) {
            if (celebrazione.addPosizione(elementi.get(i).getTitolo()) == -2) {
//                Toast toast = Toast.makeText(getApplicationContext()
//                        , getString(R.string.lista_pers_piena), Toast.LENGTH_LONG);
//                toast.show();
                Snackbar.make(findViewById(android.R.id.content)
                        , R.string.lista_pers_piena, Snackbar.LENGTH_SHORT)
                        .show();
                return false;
            }
        }

        if (celebrazione.getNomePosizione(0).equalsIgnoreCase("")) {
//            Toast toast = Toast.makeText(getApplicationContext()
//                    , getString(R.string.lista_pers_vuota), Toast.LENGTH_LONG);
//            toast.show();
            Snackbar.make(findViewById(android.R.id.content)
                    , R.string.lista_pers_vuota, Snackbar.LENGTH_SHORT)
                    .show();
            return false;
        }

        if (modifica) {
//			for (int i = 0; i < nomiElementi.size(); i++) {
            for (int i = 0; i < elementi.size(); i++) {
//    			Log.i("SALVO CANTO", nomiCanti.get(i));
                celebrazione.addCanto(nomiCanti.get(i), i);
            }
        }

        SQLiteDatabase db = listaCanti.getReadableDatabase();

        ContentValues  values = new  ContentValues();
        values.put("titolo_lista" , titoloLista);
        values.put("lista" , ListaPersonalizzata.serializeObject(celebrazione));

        if (modifica)
            db.update("LISTE_PERS", values, "_id = " + idModifica, null);
        else
            db.insert("LISTE_PERS" , "" , values);

        db.close();
        return true;
    }

//	@Override
//	public void onDestroy() {
//		if (listaCanti != null)
//			listaCanti.close();
//		super.onDestroy();
//	}

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        dataFragment = new RetainedFragment();
        getSupportFragmentManager().beginTransaction().add(dataFragment, "nomiElementi").commit();
//		dataFragment.setData(nomiElementi);
        dataFragment.setDataDrag(elementi);

        if (modifica) {
            dataFragment2 = new RetainedFragment();
            getSupportFragmentManager().beginTransaction().add(dataFragment2, "nomiCanti").commit();
            dataFragment2.setData(nomiCanti);
        }

        dataFragment3 = new RetainedFragment();
        tempArgs = new Bundle();
        tempArgs.putCharSequence(TEMP_TITLE, textfieldTitle.getText());
        dataFragment3.setArguments(tempArgs);
        getSupportFragmentManager().beginTransaction().add(dataFragment3, TEMP_TITLE).commit();

        super.onSaveInstanceState(savedInstanceState);
    }

//    private class PositionAdapter extends ArrayAdapter<String> {
//        public PositionAdapter() {
//        	super(getApplicationContext(), positionLI, R.id.position_name, nomiElementi);
//        }
//    }

    public static class RetainedFragment extends Fragment {

        // data object we want to retain
        private ArrayList<String> data;
        private ArrayList<DraggableItem> dataDrag;

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

        public ArrayList<DraggableItem> getDataDrag() {
            return dataDrag;
        }

        public void setDataDrag(ArrayList<DraggableItem> dataDrag) {
            this.dataDrag = dataDrag;
        }
    }

    private void showHelp() {
        Intent intent = new Intent(CreaListaActivity.this, IntroCreaLista.class);
        startActivity(intent);
//		if (elementi.size() == 0) {
////   		if (nomiElementi.size() == 0) {
//			findViewById(R.id.noElementsAdded).setVisibility(View.GONE);
////   			nomiElementi.add(getResources().getString(R.string.example_title));
//			elementi.add(new DraggableItem(getResources().getString(R.string.example_title), elementi.size()));
////   			adapter.notifyDataSetChanged();
//			mAdapter.notifyItemInserted(0);
//			fakeItemCreated = true;
//		}
//		else {
//			fakeItemCreated = false;
//		}
//		prevOrientation = getRequestedOrientation();
//		Utility.blockOrientation(CreaListaActivity.this);
//		lps = new RelativeLayout.LayoutParams(
//				ViewGroup.LayoutParams.WRAP_CONTENT,
//				ViewGroup.LayoutParams.WRAP_CONTENT);
//		lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//		lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//		int margin = ((Number) (getApplicationContext().getResources().getDisplayMetrics().density * 12)).intValue();
//		int marginLeft = ((Number) (getApplicationContext().getResources().getDisplayMetrics().density * 12)).intValue();
//		int marginBottom = ((Number) (getApplicationContext().getResources().getDisplayMetrics().density * 12)).intValue();
//		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//			if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
//				marginBottom = ((Number) (getApplicationContext().getResources().getDisplayMetrics()
//						.density * 62)).intValue();
//			else
//				marginLeft = ((Number) (getApplicationContext().getResources().getDisplayMetrics()
//						.density * 62)).intValue();
//		}
//		lps.setMargins(marginLeft, margin, margin, marginBottom);
//
//		ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
//		co.buttonLayoutParams = lps;
//
//		//benvenuto del tutorial
//		ShowcaseView showcaseView = ShowcaseView.insertShowcaseView(
//				new ViewTarget(R.id.fab_crea_lista, CreaListaActivity.this)
//				, CreaListaActivity.this
//				, R.string.title_activity_nuova_lista
//				, R.string.showcase_welcome_crea
//				, co);
//		showcaseView.setShowcase(ShowcaseView.NONE);
//		showcaseView.setButtonText(getString(R.string.showcase_button_next));
//		showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {
//
//			@Override
//			public void onShowcaseViewShow(ShowcaseView showcaseView) { }
//
//			@Override
//			public void onShowcaseViewHide(ShowcaseView showcaseView) {
//				//spiegazione del pulsante aggiungi posizione
//				ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
//				co.buttonLayoutParams = lps;
//				showcaseView = ShowcaseView.insertShowcaseView(
//						new ViewTarget(R.id.fab_crea_lista, CreaListaActivity.this)
//						, CreaListaActivity.this
//						, R.string.add_position
//						, R.string.showcase_add_pos_desc
//						, co);
//				showcaseView.setButtonText(getString(R.string.showcase_button_next));
//				showcaseView.setScaleMultiplier(0.5f);
//				showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {
//
//					@Override
//					public void onShowcaseViewShow(ShowcaseView showcaseView) { }
//
//					@Override
//					public void onShowcaseViewHide(ShowcaseView showcaseView) {
//						//spi
//						// egazione di come spostare le posizioni
//						ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
//						co.buttonLayoutParams = lps;
////						ViewTarget listItem = new ViewTarget(
////								adapter.getView(0, lv, lv).findViewById(R.id.drag_handle));
//						ViewTarget listItem = new ViewTarget(
//								((DraggableSwipeableAdapter.MyViewHolder) mRecyclerView.findViewHolderForAdapterPosition(0)).mDragHandle);
//						showcaseView = ShowcaseView.insertShowcaseView(
//								listItem
//								, CreaListaActivity.this
//								, R.string.posizione_reorder
//								, R.string.showcase_reorder_desc
//								, co);
//						showcaseView.setButtonText(getString(R.string.showcase_button_next));
//						showcaseView.setScaleMultiplier(0.5f);
//						int[] coords = new int[2];
////						adapter.getView(0, lv, lv).getLocationOnScreen(coords);
//						((DraggableSwipeableAdapter.MyViewHolder) mRecyclerView.findViewHolderForAdapterPosition(0)).mContainer.getLocationOnScreen(coords);
//						coords[0] = (coords[0]*2 +
////								adapter.getView(0, lv, lv).findViewById(R.id.drag_handle).getWidth())
//								((DraggableSwipeableAdapter.MyViewHolder) mRecyclerView.findViewHolderForAdapterPosition(0)).mDragHandle.getWidth())
//								/ 2;
//						coords[1] = (coords[1]*2 +
////								adapter.getView(0, lv, lv).findViewById(R.id.drag_handle).getHeight())
//								((DraggableSwipeableAdapter.MyViewHolder) mRecyclerView.findViewHolderForAdapterPosition(0)).mDragHandle.getHeight())
//								/ 2;
//						showcaseView.animateGesture(coords[0], coords[1], coords[0], coords[1] + (screenHeight - coords[1])/3, true);
//						showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {
//
//							@Override
//							public void onShowcaseViewShow(ShowcaseView showcaseView) { }
//
//							@Override
//							public void onShowcaseViewHide(ShowcaseView showcaseView) {
//								//spiegazione di come rinominare le posizioni
//								ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
//								co.buttonLayoutParams = lps;
//								ViewTarget listItem = new ViewTarget(
////										adapter.getView(0, lv, lv).findViewById(R.id.position_name));
//										((DraggableSwipeableAdapter.MyViewHolder) mRecyclerView.findViewHolderForAdapterPosition(0)).mTextView);
//								showcaseView = ShowcaseView.insertShowcaseView(
//										listItem
//										, CreaListaActivity.this
//										, R.string.posizione_rename
//										, R.string.showcase_rename_desc
//										, co);
//								showcaseView.setButtonText(getString(R.string.showcase_button_next));
//								int[] coords = new int[2];
////								adapter.getView(0, lv, lv).getLocationOnScreen(coords);
//								((DraggableSwipeableAdapter.MyViewHolder) mRecyclerView.findViewHolderForAdapterPosition(0)).mContainer.getLocationOnScreen(coords);
//								coords[0] = (coords[0]*2 +
////										adapter.getView(0, lv, lv).findViewById(R.id.position_name).getWidth())
//										((DraggableSwipeableAdapter.MyViewHolder) mRecyclerView.findViewHolderForAdapterPosition(0)).mTextView.getWidth())
//										/ 2;
//								coords[1] = (coords[1]*2 +
////										adapter.getView(0, lv, lv).findViewById(R.id.position_name).getHeight())
//										((DraggableSwipeableAdapter.MyViewHolder) mRecyclerView.findViewHolderForAdapterPosition(0)).mTextView.getHeight())
//										/ 2;
//								showcaseView.animateGesture(coords[0], coords[1], coords[0], coords[1], true);
//								showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {
//
//									@Override
//									public void onShowcaseViewShow(ShowcaseView showcaseView) { }
//
//									@Override
//									public void onShowcaseViewHide(ShowcaseView showcaseView) {
//										//spiegazione di come cancellare le posizioni
//										ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
//										co.buttonLayoutParams = lps;
//										ViewTarget listItem = new ViewTarget(
////												adapter.getView(0, lv, lv).findViewById(R.id.position_name));
//												((DraggableSwipeableAdapter.MyViewHolder) mRecyclerView.findViewHolderForAdapterPosition(0)).mTextView);
//										showcaseView = ShowcaseView.insertShowcaseView(
//												listItem
//												, CreaListaActivity.this
//												, R.string.posizione_delete
//												, R.string.showcase_delete_desc
//												, co);
//										showcaseView.setButtonText(getString(R.string.showcase_button_next));
//										int[] coords = new int[2];
////										adapter.getView(0, lv, lv).getLocationOnScreen(coords);
//										((DraggableSwipeableAdapter.MyViewHolder) mRecyclerView.findViewHolderForAdapterPosition(0)).mContainer.getLocationOnScreen(coords);
//										coords[0] = (coords[0]*2 +
////												adapter.getView(0, lv, lv).findViewById(R.id.position_name).getWidth())
//												((DraggableSwipeableAdapter.MyViewHolder) mRecyclerView.findViewHolderForAdapterPosition(0)).mTextView.getWidth())
//												/ 2;
//										coords[1] = (coords[1]*2 +
////												adapter.getView(0, lv, lv).findViewById(R.id.position_name).getHeight())
//												((DraggableSwipeableAdapter.MyViewHolder) mRecyclerView.findViewHolderForAdapterPosition(0)).mTextView.getHeight())
//												/ 2;
//										showcaseView.animateGesture(coords[0], coords[1], coords[0] + (screenWidth - coords[0])/2, coords[1], true);
//										showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {
//
//											@Override
//											public void onShowcaseViewShow(ShowcaseView showcaseView) { }
//
//											@Override
//											public void onShowcaseViewHide(ShowcaseView showcaseView) {
//												//spiegazione di come salvare
//												ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
//												co.buttonLayoutParams = lps;
//												showcaseView = ShowcaseView.insertShowcaseView(
//														new ViewTarget(R.id.action_save_list, CreaListaActivity.this)
//														, CreaListaActivity.this
//														, R.string.list_save_exit
//														, R.string.showcase_saveexit_desc
//														, co);
//												showcaseView.setButtonText(getString(R.string.showcase_button_next));
//												showcaseView.setScaleMultiplier(0.3f);
//												showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {
//
//													@Override
//													public void onShowcaseViewShow(ShowcaseView showcaseView) { }
//
//													@Override
//													public void onShowcaseViewHide(ShowcaseView showcaseView) {
//														//spiegazione di come rivedere il tutorial
//														ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
//														co.buttonLayoutParams = lps;
//														showcaseView = ShowcaseView.insertShowcaseView(
//																new ViewTarget(R.id.action_help, CreaListaActivity.this)
//																, CreaListaActivity.this
//																, R.string.showcase_end_title
//																, R.string.showcase_help_general
//																, co);
//														showcaseView.setScaleMultiplier(0.3f);
//														showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {
//
//															@Override
//															public void onShowcaseViewShow(ShowcaseView showcaseView) { }
//
//															@Override
//															public void onShowcaseViewHide(ShowcaseView showcaseView) {
//																if (fakeItemCreated) {
//																	findViewById(R.id.noElementsAdded).setVisibility(View.VISIBLE);
////																	nomiElementi.remove(0);
//																	elementi.remove(0);
////																	adapter.notifyDataSetChanged();
//																	mAdapter.notifyItemRemoved(0);
//																	fakeItemCreated = false;
//																}
//																setRequestedOrientation(prevOrientation);
//															}
//															@Override
//															public void onShowcaseViewDidHide(ShowcaseView showcaseView) { }
//														});
//													}
//
//													@Override
//													public void onShowcaseViewDidHide(ShowcaseView showcaseView) { }
//												});
//											}
//
//											@Override
//											public void onShowcaseViewDidHide(ShowcaseView showcaseView) { }
//										});
//									}
//									@Override
//									public void onShowcaseViewDidHide(ShowcaseView showcaseView) { }
//								});
//							}
//							@Override
//							public void onShowcaseViewDidHide(ShowcaseView showcaseView) { }
//						});
//					}
//					@Override
//					public void onShowcaseViewDidHide(ShowcaseView showcaseView) { }
//				});
//			}
//			@Override
//			public void onShowcaseViewDidHide(ShowcaseView showcaseView) { }
//		});
    }
}