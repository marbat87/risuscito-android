package it.cammino.risuscito.slides;

import com.stephentuso.welcome.BasicPage;
import com.stephentuso.welcome.WelcomeActivity;
import com.stephentuso.welcome.WelcomeConfiguration;

import it.cammino.risuscito.R;

public class IntroCreaListaNew extends WelcomeActivity {
    @Override
    protected WelcomeConfiguration configuration() {
        return new WelcomeConfiguration.Builder(this)
//                .theme(R.style.WelcomeScreenTheme_SolidNavigation)
                .defaultTitleTypefacePath("fonts/Roboto-Medium.ttf")
                .defaultDescriptionTypefacePath("fonts/Roboto-Regular.ttf")
                .defaultBackgroundColor(R.color.intro_background)
                .page(new BasicPage(R.drawable.intro_crealista_0, getString(R.string.title_activity_nuova_lista), getString(R.string.showcase_welcome_crea)))
                .page(new BasicPage(R.drawable.intro_crealista_1, getString(R.string.add_position), getString(R.string.showcase_add_pos_desc)))
                .page(new BasicPage(R.drawable.intro_crealista_2, getString(R.string.posizione_reorder), getString(R.string.showcase_reorder_desc)))
                .page(new BasicPage(R.drawable.intro_crealista_3, getString(R.string.posizione_rename), getString(R.string.showcase_rename_desc)))
                .page(new BasicPage(R.drawable.intro_crealista_4, getString(R.string.posizione_delete), getString(R.string.showcase_delete_desc)))
                .page(new BasicPage(R.drawable.intro_crealista_5, getString(R.string.list_save_exit), getString(R.string.showcase_saveexit_desc)))
                .page(new BasicPage(R.drawable.intro_crealista_6, getString(R.string.showcase_end_title), getString(R.string.showcase_help_general)))
                .swipeToDismiss(true)
                .build();
    }

    public static String welcomeKey() {
        return "IntroCreaListaNew";
    }
}
