package it.cammino.risuscito;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class PaginaRenderFullScreen extends ActionBarActivity {
    
	private DatabaseCanti listaCanti;
	private static String urlCanto;
    public static int speedValue;
    public static boolean scrollPlaying;
    public static int idCanto;
	
	private WebView pageView;
	private int defaultZoomLevel = 0;
	private int defaultScrollX = 0;
	private int defaultScrollY = 0;
	
	private Handler mHandler = new Handler();
	final Runnable mScrollDown = new Runnable()
	{
	    public void run()
	    {
	    	try {
	    		pageView.scrollBy(0, speedValue);
	    	}
	    	catch (NumberFormatException e) {
	    		pageView.scrollBy(0, 0);
	    	}
	    	
	        mHandler.postDelayed(this, 700);
	    }
	};

    private LUtils mLUtils;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        mLUtils = LUtils.getInstance(PaginaRenderFullScreen.this);
        mLUtils.goFullscreen();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagina_render_fullscreen);
        
        listaCanti = new DatabaseCanti(this);

        // recupera il numero della pagina da visualizzare dal parametro passato dalla chiamata
        Bundle bundle = this.getIntent().getExtras();
        urlCanto = bundle.getString(Utility.URL_CANTO);
        speedValue = bundle.getInt(Utility.SPEED_VALUE);
        scrollPlaying = bundle.getBoolean(Utility.SCROLL_PLAYING);
        idCanto = bundle.getInt(Utility.ID_CANTO);
//        Log.i(getClass().toString(), "urlCanto: " + urlCanto);
//        Log.i(getClass().toString(), "speedValue: " + speedValue);
//        Log.i(getClass().toString(), "scrollPlaying: " + scrollPlaying);
//        Log.i(getClass().toString(), "idCanto: " + idCanto);

        getSavedZoom();
        
		pageView = (WebView) findViewById(R.id.cantoView);
        ViewCompat.setTransitionName(pageView, Utility.TAG_TRANSIZIONE);

        findViewById(R.id.fab_fullscreen_off).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveZoom();
//                finish();
                mLUtils.closeActivityWithFadeOut();
            }
        });

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            finish();
            mLUtils.closeActivityWithFadeOut();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
    
    @Override
    public void onResume() {
    	super.onResume();

        checkScreenAwake();

        pageView.loadUrl(urlCanto);
//	    Log.i(this.getClass().toString(), "scrollPlaying? " + scrollPlaying);
	    if (scrollPlaying) {
			mScrollDown.run();
	    }

		WebSettings webSettings = pageView.getSettings();
		webSettings.setUseWideViewPort(true);
		webSettings.setSupportZoom(true);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setBuiltInZoomControls(true);
		if (defaultZoomLevel > 0)
            pageView.setInitialScale(defaultZoomLevel);
        pageView.setWebViewClient(new MyWebViewClient());
        
    }
    
	@Override
	public void onDestroy() {
//		saveZoom();
		if (listaCanti != null)
			listaCanti.close();
		super.onDestroy();
	}
    
    //recupera e setta lo zoom
    private void getSavedZoom() {
    	
    	SQLiteDatabase db = listaCanti.getReadableDatabase();
    	
	    String query = "SELECT zoom, scroll_x , scroll_y" +
	      		"  FROM ELENCO" +
	      		"  WHERE _id =  " + idCanto;   
	    Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

	    defaultZoomLevel = cursor.getInt(0);
	    defaultScrollX = cursor.getInt(1);
	    defaultScrollY = cursor.getInt(2);
	    
	    cursor.close();
	    db.close();
	    		
    }


    @SuppressWarnings("deprecation")
    private void saveZoom(){
        defaultZoomLevel = (int) (pageView.getScale() *100);
        defaultScrollX = pageView.getScrollX();
        defaultScrollY = pageView.getScrollY();

        SQLiteDatabase db = listaCanti.getReadableDatabase();

        String sql = "UPDATE ELENCO" +
                "  SET zoom = " + defaultZoomLevel + " " +
                ", scroll_x = " + defaultScrollX + " " +
                ", scroll_y = " + defaultScrollY + " " +
                "  WHERE _id =  " + idCanto;
        db.execSQL(sql);
        db.close();
    }
    
   	private class MyWebViewClient extends WebViewClient {
	    @Override
	    public void onPageFinished(WebView view, String url) {
	    	view.postDelayed(new Runnable() {
	    		@Override
	    		public void run() {
	    			if (defaultScrollX > 0 || defaultScrollY > 0)
	    				pageView.scrollTo(defaultScrollX, defaultScrollY);
	    		}
	    		// Delay the scrollTo to make it work
	    	}, 500);
	    	super.onPageFinished(view, url);
	    }
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

}