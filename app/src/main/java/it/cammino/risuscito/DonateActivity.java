package it.cammino.risuscito;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

public class DonateActivity extends Fragment {

	private final int TEXTZOOM = 90;
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.activity_donate, container, false);
		((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_activity_donate);
		
		WebView donateView = (WebView) rootView.findViewById(R.id.donate_text);
		donateView.setBackgroundColor(0);
		String text = "";
		
    	text = "<html><head>"
        + "<style type=\"text/css\">body{color: #000000; opacity: 0.87;}"
        + "</style></head>"
        + "<body>"                          
        + getString(R.string.donate_long_text)
        + "</body></html>";
		
//        if (Utility.getChoosedTheme(getActivity()) == 1
//        		|| Utility.getChoosedTheme(getActivity()) == 3
//        		|| Utility.getChoosedTheme(getActivity()) == 5
//        		|| Utility.getChoosedTheme(getActivity()) == 7
//        		|| Utility.getChoosedTheme(getActivity()) == 9
//        		|| Utility.getChoosedTheme(getActivity()) == 11) {
//        	text = "<html><head>"
//		          + "<style type=\"text/css\">body{color: #FFFFFF;}"
//		          + "</style></head>"
//		          + "<body>"                          
//		          + getString(R.string.donate_long_text)
//		          + "</body></html>";
//        }
//        else {
//        	text = "<html><head>"
//  		          + "<style type=\"text/css\">body{color: #000000;}"
//  		          + "</style></head>"
//  		          + "<body>"                          
//  		          + getString(R.string.donate_long_text)
//  		          + "</body></html>";
//        }
		
		donateView.loadData(text, "text/html; charset=utf-8", "UTF-8");

		WebSettings wSettings = donateView.getSettings();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			wSettings.setTextZoom(TEXTZOOM);
		else
			wSettings.setTextSize(WebSettings.TextSize.SMALLER);
		
		((Button) rootView.findViewById(R.id.donateButton)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String url = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=ENA7HP2LQKQ3G";

				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(browserIntent);
			}
		});
		
		return rootView;
	}

}
