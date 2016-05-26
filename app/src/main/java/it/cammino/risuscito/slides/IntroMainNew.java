package it.cammino.risuscito.slides;

import com.stephentuso.welcome.WelcomeScreenBuilder;
import com.stephentuso.welcome.ui.WelcomeActivity;
import com.stephentuso.welcome.util.WelcomeScreenConfiguration;

import it.cammino.risuscito.R;

public class IntroMainNew extends WelcomeActivity {
    @Override
    protected WelcomeScreenConfiguration configuration() {
        return new WelcomeScreenBuilder(this)
                .theme(R.style.WelcomeScreenTheme_SolidNavigation)
                .defaultTitleTypefacePath("fonts/Roboto-Medium.ttf")
                .defaultTitleTypefacePath("fonts/Roboto-Regular.ttf")
                .basicPage(R.drawable.intro_login_0, getString(R.string.login_intro_title), getString(R.string.login_intro_desc_0), R.color.intro_background)
                .basicPage(R.drawable.intro_login_1, getString(R.string.login_intro_title), getString(R.string.login_intro_desc_1), R.color.intro_background)
                .basicPage(R.drawable.intro_login_2, getString(R.string.login_intro_title), getString(R.string.login_intro_desc_2), R.color.intro_background)
                .basicPage(R.drawable.intro_login_3, getString(R.string.login_intro_title), getString(R.string.login_intro_desc_3), R.color.intro_background)
                .swipeToDismiss(true)
                .build();
    }

    public static String welcomeKey() {
        return "IntroMainNew";
    }
}
