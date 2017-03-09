package it.cammino.risuscito.ui;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.multidex.MultiDexApplication;
import android.widget.ImageView;

import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

import it.cammino.risuscito.R;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class RisuscitoApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
//                        .addCustomStyle(TextField.class, R.attr.textFieldStyle)
                        .build()
        );

        //initialize and create the image loader logic
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }
            @Override
            public void cancel(ImageView imageView) {
                Picasso.with(imageView.getContext()).cancelRequest(imageView);
            }
        });
    }
}
