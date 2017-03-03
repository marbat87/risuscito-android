package it.cammino.risuscito.slides;

import com.stephentuso.welcome.BasicPage;
import com.stephentuso.welcome.WelcomeActivity;
import com.stephentuso.welcome.WelcomeConfiguration;

import it.cammino.risuscito.R;

public class IntroConsegnatiNew extends WelcomeActivity {
    @Override
    protected WelcomeConfiguration configuration() {
        return new WelcomeConfiguration.Builder(this)
//                .theme(R.style.WelcomeScreenTheme_SolidNavigation)
                .defaultTitleTypefacePath("fonts/Roboto-Medium.ttf")
                .defaultDescriptionTypefacePath("fonts/Roboto-Regular.ttf")
                .defaultBackgroundColor(R.color.intro_background)
                .page(new BasicPage(R.drawable.intro_consegnati_0, getString(R.string.title_activity_consegnati), getString(R.string.showcase_consegnati_desc)))
                .page(new BasicPage(R.drawable.intro_consegnati_1, getString(R.string.title_activity_consegnati), getString(R.string.showcase_consegnati_howto)))
                .page(new BasicPage(R.drawable.intro_consegnati_2, getString(R.string.title_activity_consegnati), getString(R.string.showcase_consegnati_confirm)))
                .page(new BasicPage(R.drawable.intro_consegnati_3, getString(R.string.title_activity_consegnati), getString(R.string.showcase_consegnati_cancel)))
                .swipeToDismiss(true)
                .build();
    }

    public static String welcomeKey() {
        return "IntroConsegnatiNew";
    }
}
