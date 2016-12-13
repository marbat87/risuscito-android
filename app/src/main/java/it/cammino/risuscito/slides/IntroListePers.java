package it.cammino.risuscito.slides;

import com.stephentuso.welcome.BasicPage;
import com.stephentuso.welcome.WelcomeActivity;
import com.stephentuso.welcome.WelcomeConfiguration;

import it.cammino.risuscito.R;

public class IntroListePers extends WelcomeActivity {
    @Override
    protected WelcomeConfiguration configuration() {
        return new WelcomeConfiguration.Builder(this)
//                .theme(R.style.WelcomeScreenTheme_SolidNavigation)
                .defaultTitleTypefacePath("fonts/Roboto-Medium.ttf")
                .defaultDescriptionTypefacePath("fonts/Roboto-Regular.ttf")
                .defaultBackgroundColor(R.color.intro_background)
                .page(new BasicPage(R.drawable.intro_listepers_1, getString(R.string.showcase_listepers_title), getString(R.string.showcase_listepers_desc1)))
                .page(new BasicPage(R.drawable.intro_listepers_2, getString(R.string.showcase_listepers_title), getString(R.string.showcase_listepers_desc2)))
                .page(new BasicPage(R.drawable.intro_listepers_3, getString(R.string.showcase_listepers_title), getString(R.string.showcase_listepers_desc3)))
                .swipeToDismiss(true)
                .build();
    }

    public static String welcomeKey() {
        return "IntroListePers";
    }
}
