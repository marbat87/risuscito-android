package it.cammino.risuscito.ui;

import android.app.Application;

import it.cammino.risuscito.R;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by marcello.battain on 08/05/2015.
 */
public class RisuscitoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
//                        .addCustomStyle(TextField.class, R.attr.textFieldStyle)
                        .build()
        );
    }
}
