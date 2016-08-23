package it.cammino.risuscito.slides;

import com.stephentuso.welcome.WelcomeScreenBuilder;
import com.stephentuso.welcome.ui.WelcomeActivity;
import com.stephentuso.welcome.util.WelcomeScreenConfiguration;

import it.cammino.risuscito.R;

public class IntroListePers extends WelcomeActivity {
    @Override
    protected WelcomeScreenConfiguration configuration() {
        return new WelcomeScreenBuilder(this)
                .theme(R.style.WelcomeScreenTheme_SolidNavigation)
                .defaultTitleTypefacePath("fonts/Roboto-Medium.ttf")
                .defaultDescriptionTypefacePath("fonts/Roboto-Regular.ttf")
                .basicPage(R.drawable.intro_listepers_1, getString(R.string.showcase_listepers_title), getString(R.string.showcase_listepers_desc1), R.color.intro_background)
                .basicPage(R.drawable.intro_listepers_2, getString(R.string.showcase_listepers_title), getString(R.string.showcase_listepers_desc2), R.color.intro_background)
                .basicPage(R.drawable.intro_listepers_3, getString(R.string.showcase_listepers_title), getString(R.string.showcase_listepers_desc3), R.color.intro_background)
                .swipeToDismiss(true)
                .build();
    }

    public static String welcomeKey() {
        return "IntroListePers";
    }
}
