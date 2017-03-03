package it.cammino.risuscito.slides;

import com.stephentuso.welcome.BasicPage;
import com.stephentuso.welcome.WelcomeActivity;
import com.stephentuso.welcome.WelcomeConfiguration;

import it.cammino.risuscito.R;

public class IntroMainNew extends WelcomeActivity {
    @Override
    protected WelcomeConfiguration configuration() {
        return new WelcomeConfiguration.Builder(this)
//                .theme(R.style.WelcomeScreenTheme_SolidNavigation)
                .defaultTitleTypefacePath("fonts/Roboto-Medium.ttf")
                .defaultDescriptionTypefacePath("fonts/Roboto-Regular.ttf")
                .defaultBackgroundColor(R.color.intro_background)
                .page(new BasicPage(R.drawable.intro_login_0, getString(R.string.login_intro_title), getString(R.string.login_intro_desc_0)))
                .page(new BasicPage(R.drawable.intro_login_1, getString(R.string.login_intro_title), getString(R.string.login_intro_desc_1)))
                .page(new BasicPage(R.drawable.intro_login_2, getString(R.string.login_intro_title), getString(R.string.login_intro_desc_2)))
                .page(new BasicPage(R.drawable.intro_login_3, getString(R.string.login_intro_title), getString(R.string.login_intro_desc_3)))
                .swipeToDismiss(true)
                .build();
    }

    public static String welcomeKey() {
        return "IntroMainNew";
    }
}
