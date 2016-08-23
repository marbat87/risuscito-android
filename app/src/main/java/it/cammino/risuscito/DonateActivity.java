package it.cammino.risuscito;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import it.cammino.risuscito.ui.ThemeableActivity;

public class DonateActivity extends ThemeableActivity {

    private final int TEXTZOOM = 90;

    private LUtils mLUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        Toolbar toolbar = (Toolbar) findViewById(R.id.risuscito_toolbar);
//		toolbar.setTitle("");
        ((TextView)findViewById(R.id.main_toolbarTitle)).setText(R.string.title_activity_donate);
//		toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setBackgroundColor(getThemeUtils().primaryColor());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        WebView donateView = (WebView) findViewById(R.id.donate_text);
        donateView.setBackgroundColor(0);

        String text = "<html><head>"
                + "<style type=\"text/css\">body{color: #000000; opacity: 0.87;}"
                + "</style></head>"
                + "<body>"
                + getString(R.string.donate_long_text)
                + "</body></html>";

        donateView.loadData(text, "text/html; charset=utf-8", "UTF-8");

        WebSettings wSettings = donateView.getSettings();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            wSettings.setTextZoom(TEXTZOOM);
        else
            wSettings.setTextSize(WebSettings.TextSize.SMALLER);

        (findViewById(R.id.donateButton)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String url = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=ENA7HP2LQKQ3G";

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });

        mLUtils = LUtils.getInstance(DonateActivity.this);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mLUtils.closeActivityWithTransition();
            return true;
        }
        return  super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mLUtils.closeActivityWithTransition();
                return true;
            default:
                return false;
        }
    }

}
