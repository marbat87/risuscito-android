package it.cammino.risuscito.slides;

import com.stephentuso.welcome.WelcomeScreenBuilder;
import com.stephentuso.welcome.ui.WelcomeActivity;
import com.stephentuso.welcome.util.WelcomeScreenConfiguration;

import it.cammino.risuscito.R;

public class IntroConsegnatiNew extends WelcomeActivity {
    @Override
    protected WelcomeScreenConfiguration configuration() {
        return new WelcomeScreenBuilder(this)
                .theme(R.style.WelcomeScreenTheme_SolidNavigation)
                .defaultTitleTypefacePath("fonts/Roboto-Medium.ttf")
                .defaultTitleTypefacePath("fonts/Roboto-Regular.ttf")
                .basicPage(R.drawable.intro_consegnati_0, getString(R.string.title_activity_consegnati), getString(R.string.showcase_consegnati_desc), R.color.intro_background)
                .basicPage(R.drawable.intro_consegnati_1, getString(R.string.title_activity_consegnati), getString(R.string.showcase_consegnati_howto), R.color.intro_background)
                .basicPage(R.drawable.intro_consegnati_2, getString(R.string.title_activity_consegnati), getString(R.string.showcase_consegnati_confirm), R.color.intro_background)
                .basicPage(R.drawable.intro_consegnati_3, getString(R.string.title_activity_consegnati), getString(R.string.showcase_consegnati_cancel), R.color.intro_background)
                .swipeToDismiss(true)
                .build();
    }

    public static String welcomeKey() {
        return "IntroConsegnatiNew";
    }
}
